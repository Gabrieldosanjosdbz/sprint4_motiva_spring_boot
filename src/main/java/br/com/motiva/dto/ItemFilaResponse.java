package br.com.motiva.dto;

import br.com.motiva.model.Prioridade;

/** Uma linha da fila de prioridade montada na geração do relatório. */
public record ItemFilaResponse(
        Long idTrecho,
        String trecho,
        String perfil,
        Prioridade prioridade,
        Double nivelAntes,
        String acao,
        Double nivelDepois,
        Double custoEstimado) {
}
