package br.com.motiva.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Snapshot de um relatório gerado (histórico em MT_RELATORIO_PRIORIDADE). */
@Entity
@Table(name = "MT_RELATORIO_PRIORIDADE")
public class RelatorioPrioridade {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqRelatorio")
    @SequenceGenerator(name = "sqRelatorio", sequenceName = "SQ_RELATORIO", allocationSize = 1)
    @Column(name = "ID_RELATORIO")
    private Long id;

    @Column(name = "DT_GERACAO", nullable = false)
    private LocalDateTime geradoEm;

    @Column(name = "QT_URGENTE", nullable = false)
    private Integer qtUrgente;

    @Column(name = "QT_CRITICO", nullable = false)
    private Integer qtCritico;

    @Column(name = "QT_ATENCAO", nullable = false)
    private Integer qtAtencao;

    @Column(name = "QT_NORMAL", nullable = false)
    private Integer qtNormal;

    @Column(name = "DS_RESUMO", length = 500)
    private String resumo;

    protected RelatorioPrioridade() {
        // exigido pelo JPA
    }

    public RelatorioPrioridade(int qtUrgente, int qtCritico, int qtAtencao, int qtNormal, String resumo) {
        this.qtUrgente = qtUrgente;
        this.qtCritico = qtCritico;
        this.qtAtencao = qtAtencao;
        this.qtNormal = qtNormal;
        this.resumo = resumo;
    }

    @PrePersist
    void antesDeGravar() {
        if (geradoEm == null) {
            geradoEm = LocalDateTime.now();
        }
    }

    public int totalTrechos() {
        return qtUrgente + qtCritico + qtAtencao + qtNormal;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getGeradoEm() {
        return geradoEm;
    }

    public Integer getQtUrgente() {
        return qtUrgente;
    }

    public Integer getQtCritico() {
        return qtCritico;
    }

    public Integer getQtAtencao() {
        return qtAtencao;
    }

    public Integer getQtNormal() {
        return qtNormal;
    }

    public String getResumo() {
        return resumo;
    }
}
