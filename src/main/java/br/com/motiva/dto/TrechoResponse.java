package br.com.motiva.dto;

import br.com.motiva.model.Prioridade;
import br.com.motiva.model.TrechoRodovia;

public record TrechoResponse(
        Long id,
        String rodovia,
        Double kmInicial,
        Double kmFinal,
        Double extensaoKm,
        Double nivelVegetacao,
        String perfil,
        Prioridade prioridade,
        String intervencaoRecomendada,
        EquipeResponse equipe) {

    public static TrechoResponse de(TrechoRodovia trecho, String intervencaoRecomendada) {
        return new TrechoResponse(
                trecho.getId(),
                trecho.getIdentificacaoRodovia(),
                trecho.getQuilometroInicial(),
                trecho.getQuilometroFinal(),
                trecho.extensaoKm(),
                trecho.getNivelVegetacao(),
                trecho.getPerfil(),
                trecho.prioridade(),
                intervencaoRecomendada,
                EquipeResponse.de(trecho.getEquipeResponsavel()));
    }
}
