package br.com.motiva.service;

import br.com.motiva.dto.IntervencaoRequest;
import br.com.motiva.dto.IntervencaoResponse;
import br.com.motiva.dto.IntervencaoUpdateRequest;
import br.com.motiva.exception.RecursoNaoEncontradoException;
import br.com.motiva.exception.RegraNegocioException;
import br.com.motiva.model.EquipeManutencao;
import br.com.motiva.model.Intervencao;
import br.com.motiva.model.IntervencaoOperacional;
import br.com.motiva.model.TrechoRodovia;
import br.com.motiva.repository.IntervencaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IntervencaoService {

    private final IntervencaoRepository intervencaoRepository;
    private final TrechoService trechoService;
    private final EquipeService equipeService;
    private final MotorDeRegras motor;

    public IntervencaoService(IntervencaoRepository intervencaoRepository,
                              TrechoService trechoService,
                              EquipeService equipeService,
                              MotorDeRegras motor) {
        this.intervencaoRepository = intervencaoRepository;
        this.trechoService = trechoService;
        this.equipeService = equipeService;
        this.motor = motor;
    }

    @Transactional(readOnly = true)
    public List<IntervencaoResponse> listar(String tipo, Long idTrecho) {
        List<Intervencao> intervencoes;
        if (tipo != null) {
            intervencoes = intervencaoRepository.findByTipo(tipo.toUpperCase());
        } else if (idTrecho != null) {
            intervencoes = intervencaoRepository.findByTrechoIdOrderByDataExecucaoDesc(idTrecho);
        } else {
            intervencoes = intervencaoRepository.findAllByOrderByDataExecucaoDesc();
        }
        return intervencoes.stream().map(IntervencaoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public IntervencaoResponse buscar(Long id) {
        return IntervencaoResponse.de(buscarEntidade(id));
    }

    /** Executa a intervenção no trecho (reduz a vegetação) e grava o registro. */
    @Transactional
    public IntervencaoResponse registrar(IntervencaoRequest dados) {
        TrechoRodovia trecho = trechoService.buscarEntidade(dados.idTrecho());
        if (!motor.precisaDeIntervencao(trecho)) {
            throw new RegraNegocioException(String.format(
                    "Trecho %d está em NORMAL (%.1f cm) e não precisa de intervenção",
                    trecho.getId(), trecho.getNivelVegetacao()));
        }
        EquipeManutencao equipe = (dados.idEquipe() == null)
                ? trecho.getEquipeResponsavel()
                : equipeService.buscarEntidade(dados.idEquipe());

        Intervencao intervencao = executar(trecho, motor.porCodigo(dados.tipo()), equipe);
        return IntervencaoResponse.de(intervencao);
    }

    @Transactional
    public IntervencaoResponse atualizar(Long id, IntervencaoUpdateRequest dados) {
        Intervencao intervencao = buscarEntidade(id);
        EquipeManutencao equipe = (dados.idEquipe() == null) ? null : equipeService.buscarEntidade(dados.idEquipe());
        intervencao.corrigirLancamento(equipe, dados.custoEstimado());
        return IntervencaoResponse.de(intervencao);
    }

    @Transactional
    public void remover(Long id) {
        intervencaoRepository.delete(buscarEntidade(id));
    }

    // usado também pelo relatório; o nível novo do trecho vai pro banco pelo dirty checking do JPA
    Intervencao executar(TrechoRodovia trecho, IntervencaoOperacional tipo, EquipeManutencao equipe) {
        double nivelAntes = trecho.getNivelVegetacao();
        double custo = tipo.custoEstimado(trecho);
        tipo.executarServico(trecho);

        Intervencao intervencao = new Intervencao(trecho, equipe, tipo.codigoTipo(),
                nivelAntes, trecho.getNivelVegetacao(), custo);
        return intervencaoRepository.save(intervencao);
    }

    private Intervencao buscarEntidade(Long id) {
        return intervencaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Intervenção", id));
    }
}
