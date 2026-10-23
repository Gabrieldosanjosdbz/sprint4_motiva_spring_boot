package br.com.motiva.model;

public abstract class IntervencaoOperacional {

    protected final String nome;
    protected final double custoPorKmReais;

    protected IntervencaoOperacional(String nome, double custoPorKmReais) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome da intervenção é obrigatório");
        }
        if (custoPorKmReais < 0) {
            throw new IllegalArgumentException("Custo por km não pode ser negativo");
        }
        this.nome = nome;
        this.custoPorKmReais = custoPorKmReais;
    }

    public abstract String executarServico(TrechoRodovia trecho);

    /** Valor gravado na coluna TP_INTERVENCAO. */
    public abstract String codigoTipo();

    public double custoEstimado(TrechoRodovia trecho) {
        return custoPorKmReais * trecho.extensaoKm();
    }

    public String getNome() {
        return nome;
    }

    public double getCustoPorKmReais() {
        return custoPorKmReais;
    }
}
