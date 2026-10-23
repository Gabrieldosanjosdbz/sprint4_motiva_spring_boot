package br.com.motiva.repository;

import br.com.motiva.model.EquipeManutencao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipeManutencaoRepository extends JpaRepository<EquipeManutencao, Long> {

    Optional<EquipeManutencao> findByNomeIgnoreCase(String nome);

    List<EquipeManutencao> findByQuantidadeOperadoresGreaterThanEqual(Integer minimo);

    List<EquipeManutencao> findAllByOrderByNomeAsc();
}
