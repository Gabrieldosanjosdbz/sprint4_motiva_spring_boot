package br.com.motiva.dto;

import br.com.motiva.model.EquipeManutencao;
import br.com.motiva.model.Intervencao;

import java.time.LocalDateTime;

public record IntervencaoResponse(
        Long id,
        Long idTrecho,
        String trecho,
        Long idEquipe,
        String equipe,
        String tipo,
        Double nivelAntes,
        Double nivelDepois,
        Double custoEstimado,
        LocalDateTime dataExecucao) {

    public static IntervencaoResponse de(Intervencao intervencao) {
        EquipeManutencao equipe = intervencao.getEquipe();
        return new IntervencaoResponse(
                intervencao.getId(),
                intervencao.getTrecho().getId(),
                intervencao.getTrecho().descricao(),
                equipe == null ? null : equipe.getId(),
                equipe == null ? null : equipe.getNome(),
                intervencao.getTipo(),
                intervencao.getNivelAntes(),
                intervencao.getNivelDepois(),
                intervencao.getCustoEstimado(),
                intervencao.getDataExecucao());
    }
}
