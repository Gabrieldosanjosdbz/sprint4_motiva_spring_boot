package br.com.motiva.service;

import br.com.motiva.exception.RegraNegocioException;
import br.com.motiva.model.IntervencaoOperacional;
import br.com.motiva.model.MonitoravelViaIoT;
import br.com.motiva.model.Prioridade;
import br.com.motiva.model.Pulverizacao;
import br.com.motiva.model.RocadaMecanizada;
import br.com.motiva.model.TrechoRodovia;
import org.springframework.stereotype.Component;

import java.util.List;

/** Motor de prioridade da Sprint 2/3: decide a intervenção pela faixa do trecho. */
@Component
public class MotorDeRegras {

    private final IntervencaoOperacional rocadaMecanizada = new RocadaMecanizada();
    private final IntervencaoOperacional pulverizacao = new Pulverizacao();

    public IntervencaoOperacional recomendarIntervencao(TrechoRodovia trecho) {
        return switch (trecho.prioridade()) {
            case URGENTE, CRITICO -> rocadaMecanizada;
            case ATENCAO -> pulverizacao;
            case NORMAL -> null;
        };
    }

    public String nomeRecomendacao(TrechoRodovia trecho) {
        IntervencaoOperacional intervencao = recomendarIntervencao(trecho);
        return intervencao == null ? "Sem ação (dentro do limite)" : intervencao.getNome();
    }

    public IntervencaoOperacional porCodigo(String codigoTipo) {
        if (rocadaMecanizada.codigoTipo().equals(codigoTipo)) {
            return rocadaMecanizada;
        }
        if (pulverizacao.codigoTipo().equals(codigoTipo)) {
            return pulverizacao;
        }
        throw new RegraNegocioException("Tipo de intervenção desconhecido: " + codigoTipo);
    }

    /** Lê os sensores dos trechos que têm IoT; devolve quantos transmitiram. */
    public int sincronizarSensores(List<? extends TrechoRodovia> trechos) {
        int sincronizados = 0;
        for (TrechoRodovia trecho : trechos) {
            if (trecho instanceof MonitoravelViaIoT sensor) {
                sensor.transmitirDadosSensor();
                sincronizados++;
            }
        }
        return sincronizados;
    }

    public boolean precisaDeIntervencao(TrechoRodovia trecho) {
        return trecho.prioridade() != Prioridade.NORMAL;
    }
}
