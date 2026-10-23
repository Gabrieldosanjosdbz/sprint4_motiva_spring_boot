package br.com.motiva.model;

public class RocadaMecanizada extends IntervencaoOperacional {

    public RocadaMecanizada() {
        super("Roçada Mecanizada", 1200.0);
    }

    @Override
    public String executarServico(TrechoRodovia trecho) {
        double antes = trecho.getNivelVegetacao();
        trecho.aparar();
        return String.format("Roçada mecanizada concluída em %s (%.1f cm -> 0,0 cm)",
                trecho.descricao(), antes);
    }

    @Override
    public String codigoTipo() {
        return "ROCADA";
    }
}
