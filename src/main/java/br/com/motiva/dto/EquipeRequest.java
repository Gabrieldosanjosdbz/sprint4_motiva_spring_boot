package br.com.motiva.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EquipeRequest(
        @NotBlank(message = "nome é obrigatório")
        @Size(max = 60, message = "nome pode ter no máximo 60 caracteres")
        String nome,

        @NotNull(message = "quantidadeOperadores é obrigatório")
        @Positive(message = "equipe precisa de pelo menos um operador")
        @Max(value = 999, message = "quantidadeOperadores pode ser no máximo 999")
        Integer quantidadeOperadores) {
}
