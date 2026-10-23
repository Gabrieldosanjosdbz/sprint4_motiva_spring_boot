package br.com.motiva.dto;

import br.com.motiva.model.EquipeManutencao;

public record EquipeResponse(Long id, String nome, Integer quantidadeOperadores) {

    public static EquipeResponse de(EquipeManutencao equipe) {
        if (equipe == null) {
            return null;
        }
        return new EquipeResponse(equipe.getId(), equipe.getNome(), equipe.getQuantidadeOperadores());
    }
}
