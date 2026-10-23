package br.com.motiva.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record IntervencaoRequest(
        @NotNull(message = "idTrecho é obrigatório")
        Long idTrecho,

        // se vier vazio, usa a equipe responsável pelo trecho
        Long idEquipe,

        @NotBlank(message = "tipo é obrigatório")
        @Pattern(regexp = "ROCADA|PULVERIZACAO", message = "tipo deve ser ROCADA ou PULVERIZACAO")
        String tipo) {
}
