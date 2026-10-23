package br.com.motiva.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SECO")
public class TrechoSeco extends TrechoRodovia {

    private static final double TAXA_SEMANAL_CM = 6.0;

    protected TrechoSeco() {
    }

    public TrechoSeco(String identificacaoRodovia,
                      double quilometroInicial,
                      double quilometroFinal,
                      double nivelVegetacaoInicial) {
        super(identificacaoRodovia, quilometroInicial, quilometroFinal, nivelVegetacaoInicial);
    }

    @Override
    public double taxaCrescimentoSemanalCm() {
        return TAXA_SEMANAL_CM;
    }

    @Override
    public String perfilTerreno() {
        return "Seco";
    }

    @Override
    public String codigoPerfil() {
        return "SECO";
    }
}
