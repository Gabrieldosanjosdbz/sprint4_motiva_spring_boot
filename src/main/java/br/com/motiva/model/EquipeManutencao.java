package br.com.motiva.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "MT_EQUIPE_MANUTENCAO")
public class EquipeManutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqEquipe")
    @SequenceGenerator(name = "sqEquipe", sequenceName = "SQ_EQUIPE", allocationSize = 1)
    @Column(name = "ID_EQUIPE")
    private Long id;

    @Column(name = "NM_EQUIPE", nullable = false, unique = true, length = 60)
    private String nome;

    @Column(name = "QT_OPERADORES", nullable = false)
    private Integer quantidadeOperadores;

    protected EquipeManutencao() {
        // exigido pelo JPA
    }

    public EquipeManutencao(String nome, int quantidadeOperadores) {
        atualizar(nome, quantidadeOperadores);
    }

    // mesmas regras do record da Sprint 3
    public void atualizar(String nome, int quantidadeOperadores) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome da equipe é obrigatório");
        }
        if (quantidadeOperadores <= 0) {
            throw new IllegalArgumentException("Equipe precisa de pelo menos um operador");
        }
        this.nome = nome.trim();
        this.quantidadeOperadores = quantidadeOperadores;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Integer getQuantidadeOperadores() {
        return quantidadeOperadores;
    }

    @Override
    public String toString() {
        return String.format("Equipe #%s %s (%d operadores)", id, nome, quantidadeOperadores);
    }
}
