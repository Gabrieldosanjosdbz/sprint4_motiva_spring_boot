package br.com.motiva.model;

/** Faixas usadas no relatório de prioridade. */
public enum Prioridade {

    URGENTE(60.0),
    CRITICO(40.0),
    ATENCAO(20.0),
    NORMAL(0.0);

    private final double nivelMinimoCm;

    Prioridade(double nivelMinimoCm) {
        this.nivelMinimoCm = nivelMinimoCm;
    }

    public static Prioridade de(double nivelVegetacaoCm) {
        for (Prioridade prioridade : values()) {
            if (nivelVegetacaoCm >= prioridade.nivelMinimoCm) {
                return prioridade;
            }
        }
        return NORMAL;
    }

    public double getNivelMinimoCm() {
        return nivelMinimoCm;
    }
}
