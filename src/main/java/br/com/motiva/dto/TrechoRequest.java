package br.com.motiva.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record TrechoRequest(
        @NotBlank(message = "rodovia é obrigatória")
        @Size(max = 20, message = "rodovia pode ter no máximo 20 caracteres")
        String rodovia,

        @NotNull(message = "kmInicial é obrigatório")
        @PositiveOrZero(message = "kmInicial não pode ser negativo")
        Double kmInicial,

        @NotNull(message = "kmFinal é obrigatório")
        @Positive(message = "kmFinal deve ser positivo")
        Double kmFinal,

        @NotNull(message = "nivelVegetacao é obrigatório")
        @PositiveOrZero(message = "nivelVegetacao não pode ser negativo")
        Double nivelVegetacao,

        @NotBlank(message = "perfil é obrigatório")
        @Pattern(regexp = "UMIDO|SECO", message = "perfil deve ser UMIDO ou SECO")
        String perfil,

        // opcional: trecho pode ficar sem equipe
        Long idEquipe) {
}
