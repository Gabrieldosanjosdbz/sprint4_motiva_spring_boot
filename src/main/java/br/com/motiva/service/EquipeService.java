package br.com.motiva.service;

import br.com.motiva.dto.EquipeRequest;
import br.com.motiva.dto.EquipeResponse;
import br.com.motiva.exception.ConflitoException;
import br.com.motiva.exception.RecursoNaoEncontradoException;
import br.com.motiva.model.EquipeManutencao;
import br.com.motiva.repository.EquipeManutencaoRepository;
import br.com.motiva.repository.IntervencaoRepository;
import br.com.motiva.repository.TrechoRodoviaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EquipeService {

    private final EquipeManutencaoRepository equipeRepository;
    private final TrechoRodoviaRepository trechoRepository;
    private final IntervencaoRepository intervencaoRepository;

    public EquipeService(EquipeManutencaoRepository equipeRepository,
                         TrechoRodoviaRepository trechoRepository,
                         IntervencaoRepository intervencaoRepository) {
        this.equipeRepository = equipeRepository;
        this.trechoRepository = trechoRepository;
        this.intervencaoRepository = intervencaoRepository;
    }

    @Transactional(readOnly = true)
    public List<EquipeResponse> listar(Integer minimoOperadores) {
        List<EquipeManutencao> equipes = (minimoOperadores == null)
                ? equipeRepository.findAllByOrderByNomeAsc()
                : equipeRepository.findByQuantidadeOperadoresGreaterThanEqual(minimoOperadores);
        return equipes.stream().map(EquipeResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public EquipeResponse buscar(Long id) {
        return EquipeResponse.de(buscarEntidade(id));
    }

    @Transactional
    public EquipeResponse criar(EquipeRequest dados) {
        garantirNomeLivre(dados.nome(), null);
        EquipeManutencao equipe = new EquipeManutencao(dados.nome(), dados.quantidadeOperadores());
        return EquipeResponse.de(equipeRepository.save(equipe));
    }

    @Transactional
    public EquipeResponse atualizar(Long id, EquipeRequest dados) {
        EquipeManutencao equipe = buscarEntidade(id);
        garantirNomeLivre(dados.nome(), id);
        equipe.atualizar(dados.nome(), dados.quantidadeOperadores());
        return EquipeResponse.de(equipe);
    }

    @Transactional
    public void remover(Long id) {
        EquipeManutencao equipe = buscarEntidade(id);
        // na Sprint 3 isso estourava ORA-02292; agora a regra é checada antes
        if (trechoRepository.existsByEquipeResponsavelId(id)) {
            throw new ConflitoException("Equipe " + id + " ainda é responsável por trechos");
        }
        if (intervencaoRepository.existsByEquipeId(id)) {
            throw new ConflitoException("Equipe " + id + " tem intervenções registradas no histórico");
        }
        equipeRepository.delete(equipe);
    }

    EquipeManutencao buscarEntidade(Long id) {
        return equipeRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Equipe", id));
    }

    private void garantirNomeLivre(String nome, Long idAtual) {
        equipeRepository.findByNomeIgnoreCase(nome.trim())
                .filter(outra -> !outra.getId().equals(idAtual))
                .ifPresent(outra -> {
                    throw new ConflitoException("Já existe uma equipe chamada " + outra.getNome());
                });
    }
}
