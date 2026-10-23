package br.com.motiva.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Correção de um lançamento: só equipe e custo podem mudar depois da execução. */
public record IntervencaoUpdateRequest(
        Long idEquipe,

        @NotNull(message = "custoEstimado é obrigatório")
        @PositiveOrZero(message = "custoEstimado não pode ser negativo")
        Double custoEstimado) {
}
