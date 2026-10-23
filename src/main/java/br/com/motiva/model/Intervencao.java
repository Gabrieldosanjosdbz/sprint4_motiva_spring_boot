package br.com.motiva.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Uma intervenção já executada em um trecho (era o record IntervencaoAplicada na Sprint 3). */
@Entity
@Table(name = "MT_INTERVENCAO")
public class Intervencao {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqIntervencao")
    @SequenceGenerator(name = "sqIntervencao", sequenceName = "SQ_INTERVENCAO", allocationSize = 1)
    @Column(name = "ID_INTERVENCAO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_TRECHO", nullable = false)
    private TrechoRodovia trecho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_EQUIPE")
    private EquipeManutencao equipe;

    @Column(name = "TP_INTERVENCAO", nullable = false, length = 20)
    private String tipo;

    @Column(name = "NR_NIVEL_ANTES", nullable = false)
    private Double nivelAntes;

    @Column(name = "NR_NIVEL_DEPOIS", nullable = false)
    private Double nivelDepois;

    @Column(name = "VL_CUSTO_ESTIMADO", nullable = false)
    private Double custoEstimado;

    @Column(name = "DT_EXECUCAO", nullable = false)
    private LocalDateTime dataExecucao;

    protected Intervencao() {
        // exigido pelo JPA
    }

    public Intervencao(TrechoRodovia trecho, EquipeManutencao equipe, String tipo,
                       double nivelAntes, double nivelDepois, double custoEstimado) {
        if (trecho == null) {
            throw new IllegalArgumentException("Trecho da intervenção é obrigatório");
        }
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("Tipo da intervenção é obrigatório");
        }
        this.trecho = trecho;
        this.tipo = tipo;
        this.nivelAntes = nivelAntes;
        this.nivelDepois = nivelDepois;
        corrigirLancamento(equipe, custoEstimado);
    }

    // só equipe e custo podem ser corrigidos depois; o resto é histórico do que aconteceu
    public void corrigirLancamento(EquipeManutencao equipe, double custoEstimado) {
        if (custoEstimado < 0) {
            throw new IllegalArgumentException("Custo estimado não pode ser negativo");
        }
        this.equipe = equipe;
        this.custoEstimado = custoEstimado;
    }

    @PrePersist
    void antesDeGravar() {
        if (dataExecucao == null) {
            dataExecucao = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public TrechoRodovia getTrecho() {
        return trecho;
    }

    public EquipeManutencao getEquipe() {
        return equipe;
    }

    public String getTipo() {
        return tipo;
    }

    public Double getNivelAntes() {
        return nivelAntes;
    }

    public Double getNivelDepois() {
        return nivelDepois;
    }

    public Double getCustoEstimado() {
        return custoEstimado;
    }

    public LocalDateTime getDataExecucao() {
        return dataExecucao;
    }
}
