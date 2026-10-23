package br.com.motiva.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * Trecho monitorado. Na Sprint 3 o DAO lia TP_PERFIL e decidia na mão qual subclasse
 * instanciar; agora o próprio JPA faz isso pelo discriminador (herança SINGLE_TABLE).
 */
@Entity
@Table(name = "MT_TRECHO_RODOVIA")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TP_PERFIL", discriminatorType = DiscriminatorType.STRING, length = 10)
public abstract class TrechoRodovia {

    private static final double NIVEL_CRITICO_CM = 40.0;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqTrecho")
    @SequenceGenerator(name = "sqTrecho", sequenceName = "SQ_TRECHO", allocationSize = 1)
    @Column(name = "ID_TRECHO")
    private Long id;

    @Column(name = "DS_RODOVIA", nullable = false, length = 20)
    private String identificacaoRodovia;

    @Column(name = "NR_KM_INICIAL", nullable = false)
    private Double quilometroInicial;

    @Column(name = "NR_KM_FINAL", nullable = false)
    private Double quilometroFinal;

    @Column(name = "NR_NIVEL_VEGETACAO", nullable = false)
    private Double nivelVegetacao;

    // só leitura: quem grava a coluna é o discriminador; aqui serve para o findByPerfil()
    @Column(name = "TP_PERFIL", insertable = false, updatable = false)
    private String perfil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_EQUIPE")
    private EquipeManutencao equipeResponsavel;

    protected TrechoRodovia() {
        // exigido pelo JPA
    }

    protected TrechoRodovia(String identificacaoRodovia,
                            double quilometroInicial,
                            double quilometroFinal,
                            double nivelVegetacaoInicial) {
        atualizarDados(identificacaoRodovia, quilometroInicial, quilometroFinal, nivelVegetacaoInicial);
        this.perfil = codigoPerfil();
    }

    public abstract double taxaCrescimentoSemanalCm();

    public abstract String perfilTerreno();

    /** Valor gravado na coluna TP_PERFIL. */
    public abstract String codigoPerfil();

    // validações da Sprint 1
    public void atualizarDados(String identificacaoRodovia,
                               double quilometroInicial,
                               double quilometroFinal,
                               double nivelVegetacao) {
        if (identificacaoRodovia == null || identificacaoRodovia.isBlank()) {
            throw new IllegalArgumentException("Identificação da rodovia é obrigatória");
        }
        if (quilometroFinal <= quilometroInicial) {
            throw new IllegalArgumentException("Quilometragem final deve ser maior que a inicial");
        }
        if (nivelVegetacao < 0) {
            throw new IllegalArgumentException("Nível de vegetação não pode ser negativo");
        }
        this.identificacaoRodovia = identificacaoRodovia.trim().toUpperCase();
        this.quilometroInicial = quilometroInicial;
        this.quilometroFinal = quilometroFinal;
        this.nivelVegetacao = nivelVegetacao;
    }

    public void simularSemana() {
        registrarCrescimento(taxaCrescimentoSemanalCm());
    }

    public void registrarCrescimento(double taxaCm) {
        if (taxaCm < 0) {
            throw new IllegalArgumentException("Taxa de crescimento não pode ser negativa");
        }
        this.nivelVegetacao += taxaCm;
    }

    public void reduzirVegetacao(double cm) {
        if (cm < 0) {
            throw new IllegalArgumentException("Redução não pode ser negativa");
        }
        this.nivelVegetacao = Math.max(0.0, this.nivelVegetacao - cm);
    }

    public void aparar() {
        this.nivelVegetacao = 0.0;
    }

    public boolean estaCritico() {
        return this.nivelVegetacao >= NIVEL_CRITICO_CM;
    }

    public Prioridade prioridade() {
        return Prioridade.de(this.nivelVegetacao);
    }

    public void associarEquipe(EquipeManutencao equipe) {
        this.equipeResponsavel = equipe; // null desassocia
    }

    public String descricao() {
        return String.format("%s KM %.1f ao %.1f", identificacaoRodovia, quilometroInicial, quilometroFinal);
    }

    public double extensaoKm() {
        return quilometroFinal - quilometroInicial;
    }

    public Long getId() {
        return id;
    }

    public String getIdentificacaoRodovia() {
        return identificacaoRodovia;
    }

    public Double getQuilometroInicial() {
        return quilometroInicial;
    }

    public Double getQuilometroFinal() {
        return quilometroFinal;
    }

    public Double getNivelVegetacao() {
        return nivelVegetacao;
    }

    public String getPerfil() {
        return codigoPerfil();
    }

    public EquipeManutencao getEquipeResponsavel() {
        return equipeResponsavel;
    }

    @Override
    public String toString() {
        return String.format("#%s [%s] %s | %.1f cm", id, perfilTerreno(), descricao(), nivelVegetacao);
    }
}
