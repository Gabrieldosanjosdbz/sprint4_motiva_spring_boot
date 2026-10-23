package br.com.motiva.repository;

import br.com.motiva.model.Intervencao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface IntervencaoRepository extends JpaRepository<Intervencao, Long> {

    List<Intervencao> findAllByOrderByDataExecucaoDesc();

    List<Intervencao> findByTipo(String tipo);

    List<Intervencao> findByTrechoIdOrderByDataExecucaoDesc(Long idTrecho);

    List<Intervencao> findByDataExecucaoBetween(LocalDateTime inicio, LocalDateTime fim);

    boolean existsByTrechoId(Long idTrecho);

    boolean existsByEquipeId(Long idEquipe);
}
