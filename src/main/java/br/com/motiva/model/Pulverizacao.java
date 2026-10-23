package br.com.motiva.model;

public class Pulverizacao extends IntervencaoOperacional {

    private static final double REDUCAO_PERCENTUAL = 0.6;

    public Pulverizacao() {
        super("Pulverização", 450.0);
    }

    @Override
    public String executarServico(TrechoRodovia trecho) {
        double antes = trecho.getNivelVegetacao();
        double reducao = antes * REDUCAO_PERCENTUAL;
        trecho.reduzirVegetacao(reducao);
        return String.format("Pulverização aplicada em %s (%.1f cm -> %.1f cm)",
                trecho.descricao(), antes, trecho.getNivelVegetacao());
    }

    @Override
    public String codigoTipo() {
        return "PULVERIZACAO";
    }
}
