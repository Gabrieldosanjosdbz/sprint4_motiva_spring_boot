package br.com.motiva.repository;

import br.com.motiva.model.RelatorioPrioridade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RelatorioPrioridadeRepository extends JpaRepository<RelatorioPrioridade, Long> {

    List<RelatorioPrioridade> findAllByOrderByGeradoEmDesc();

    List<RelatorioPrioridade> findByGeradoEmBetweenOrderByGeradoEmDesc(LocalDateTime inicio, LocalDateTime fim);
}
