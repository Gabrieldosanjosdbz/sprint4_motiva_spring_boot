package br.com.motiva.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("UMIDO")
public class TrechoUmido extends TrechoRodovia implements MonitoravelViaIoT {

    private static final double TAXA_SEMANAL_CM = 11.0;

    protected TrechoUmido() {
    }

    public TrechoUmido(String identificacaoRodovia,
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
        return "Úmido";
    }

    @Override
    public String codigoPerfil() {
        return "UMIDO";
    }

    @Override
    public double transmitirDadosSensor() {
        double crescimentoLido = taxaCrescimentoSemanalCm();
        registrarCrescimento(crescimentoLido);
        return crescimentoLido;
    }
}
