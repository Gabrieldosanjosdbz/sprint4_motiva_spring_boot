package br.com.motiva.dto;

import br.com.motiva.model.RelatorioPrioridade;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RelatorioResponse(
        Long id,
        LocalDateTime geradoEm,
        Integer qtUrgente,
        Integer qtCritico,
        Integer qtAtencao,
        Integer qtNormal,
        Integer totalTrechos,
        String resumo,
        // a fila só aparece na resposta do POST; o histórico guarda apenas as contagens
        List<ItemFilaResponse> fila) {

    public static RelatorioResponse de(RelatorioPrioridade relatorio) {
        return de(relatorio, null);
    }

    public static RelatorioResponse de(RelatorioPrioridade relatorio, List<ItemFilaResponse> fila) {
        return new RelatorioResponse(
                relatorio.getId(),
                relatorio.getGeradoEm(),
                relatorio.getQtUrgente(),
                relatorio.getQtCritico(),
                relatorio.getQtAtencao(),
                relatorio.getQtNormal(),
                relatorio.totalTrechos(),
                relatorio.getResumo(),
                fila);
    }
}
