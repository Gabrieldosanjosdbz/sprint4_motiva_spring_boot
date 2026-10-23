package br.com.motiva.repository;

import br.com.motiva.model.TrechoRodovia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrechoRodoviaRepository extends JpaRepository<TrechoRodovia, Long> {

    List<TrechoRodovia> findAllByOrderByNivelVegetacaoDesc();

    List<TrechoRodovia> findByNivelVegetacaoGreaterThanEqualOrderByNivelVegetacaoDesc(Double minimo);

    List<TrechoRodovia> findByPerfil(String perfil);

    List<TrechoRodovia> findByIdentificacaoRodoviaIgnoreCase(String rodovia);

    long countByEquipeResponsavelId(Long idEquipe);

    long countByIdentificacaoRodoviaAndQuilometroInicialAndQuilometroFinal(
            String rodovia, Double kmInicial, Double kmFinal);
}
