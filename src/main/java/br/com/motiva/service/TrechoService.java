package br.com.motiva.service;

import br.com.motiva.dto.TrechoRequest;
import br.com.motiva.dto.TrechoResponse;
import br.com.motiva.exception.ConflitoException;
import br.com.motiva.exception.RecursoNaoEncontradoException;
import br.com.motiva.exception.RegraNegocioException;
import br.com.motiva.model.EquipeManutencao;
import br.com.motiva.model.TrechoRodovia;
import br.com.motiva.model.TrechoSeco;
import br.com.motiva.model.TrechoUmido;
import br.com.motiva.repository.IntervencaoRepository;
import br.com.motiva.repository.TrechoRodoviaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrechoService {

    private final TrechoRodoviaRepository trechoRepository;
    private final IntervencaoRepository intervencaoRepository;
    private final EquipeService equipeService;
    private final MotorDeRegras motor;

    public TrechoService(TrechoRodoviaRepository trechoRepository,
                         IntervencaoRepository intervencaoRepository,
                         EquipeService equipeService,
                         MotorDeRegras motor) {
        this.trechoRepository = trechoRepository;
        this.intervencaoRepository = intervencaoRepository;
        this.equipeService = equipeService;
        this.motor = motor;
    }

    // um filtro por vez; se vierem vários, vale o primeiro na ordem nivelMinimo > perfil > rodovia
    @Transactional(readOnly = true)
    public List<TrechoResponse> listar(Double nivelMinimo, String perfil, String rodovia) {
        List<TrechoRodovia> trechos;
        if (nivelMinimo != null) {
            trechos = trechoRepository.findByNivelVegetacaoGreaterThanEqualOrderByNivelVegetacaoDesc(nivelMinimo);
        } else if (perfil != null) {
            trechos = trechoRepository.findByPerfil(perfil.toUpperCase());
        } else if (rodovia != null) {
            trechos = trechoRepository.findByIdentificacaoRodoviaIgnoreCase(rodovia);
        } else {
            trechos = trechoRepository.findAllByOrderByNivelVegetacaoDesc();
        }
        return trechos.stream().map(this::paraResposta).toList();
    }

    @Transactional(readOnly = true)
    public TrechoResponse buscar(Long id) {
        return paraResposta(buscarEntidade(id));
    }

    @Transactional
    public TrechoResponse criar(TrechoRequest dados) {
        validar(dados);
        garantirFaixaLivre(dados, null);

        TrechoRodovia trecho = switch (dados.perfil()) {
            case "UMIDO" -> new TrechoUmido(dados.rodovia(), dados.kmInicial(), dados.kmFinal(), dados.nivelVegetacao());
            case "SECO" -> new TrechoSeco(dados.rodovia(), dados.kmInicial(), dados.kmFinal(), dados.nivelVegetacao());
            default -> throw new RegraNegocioException("Perfil deve ser UMIDO ou SECO");
        };
        trecho.associarEquipe(equipeOuNulo(dados.idEquipe()));
        return paraResposta(trechoRepository.save(trecho));
    }

    @Transactional
    public TrechoResponse atualizar(Long id, TrechoRequest dados) {
        TrechoRodovia trecho = buscarEntidade(id);
        validar(dados);
        // a subclasse é fixa: trocar de perfil exigiria outra entidade
        if (!trecho.codigoPerfil().equals(dados.perfil())) {
            throw new RegraNegocioException("O perfil do trecho não pode ser alterado (atual: "
                    + trecho.codigoPerfil() + ")");
        }
        garantirFaixaLivre(dados, id);

        trecho.atualizarDados(dados.rodovia(), dados.kmInicial(), dados.kmFinal(), dados.nivelVegetacao());
        trecho.associarEquipe(equipeOuNulo(dados.idEquipe()));
        return paraResposta(trecho);
    }

    @Transactional
    public void remover(Long id) {
        TrechoRodovia trecho = buscarEntidade(id);
        if (intervencaoRepository.existsByTrechoId(id)) {
            throw new ConflitoException("Trecho " + id + " tem intervenções no histórico e não pode ser removido");
        }
        trechoRepository.delete(trecho);
    }

    TrechoRodovia buscarEntidade(Long id) {
        return trechoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Trecho", id));
    }

    TrechoResponse paraResposta(TrechoRodovia trecho) {
        return TrechoResponse.de(trecho, motor.nomeRecomendacao(trecho));
    }

    // regras da Sprint 1; o construtor da entidade também confere, mas aqui a mensagem sai mais cedo
    private void validar(TrechoRequest dados) {
        if (dados.nivelVegetacao() < 0) {
            throw new RegraNegocioException("Nível de vegetação não pode ser negativo");
        }
        if (dados.kmFinal() <= dados.kmInicial()) {
            throw new RegraNegocioException("Quilometragem final deve ser maior que a inicial");
        }
    }

    private void garantirFaixaLivre(TrechoRequest dados, Long idAtual) {
        String rodovia = dados.rodovia().trim().toUpperCase();
        boolean ocupada = trechoRepository.existsByIdentificacaoRodoviaAndQuilometroInicialAndQuilometroFinal(
                rodovia, dados.kmInicial(), dados.kmFinal());
        if (!ocupada) {
            return;
        }
        if (idAtual != null) {
            TrechoRodovia atual = buscarEntidade(idAtual);
            boolean mesmaFaixa = atual.getIdentificacaoRodovia().equals(rodovia)
                    && atual.getQuilometroInicial().equals(dados.kmInicial())
                    && atual.getQuilometroFinal().equals(dados.kmFinal());
            if (mesmaFaixa) {
                return;
            }
        }
        throw new ConflitoException(String.format("Já existe um trecho %s KM %.1f ao %.1f",
                rodovia, dados.kmInicial(), dados.kmFinal()));
    }

    private EquipeManutencao equipeOuNulo(Long idEquipe) {
        return idEquipe == null ? null : equipeService.buscarEntidade(idEquipe);
    }
}
