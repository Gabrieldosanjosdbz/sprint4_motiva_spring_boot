-- MOTIVA - Sprint 3
-- Criacao das tabelas

-- Sequences dos ids
CREATE SEQUENCE sq_equipe      START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_trecho      START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_intervencao START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_relatorio   START WITH 1 INCREMENT BY 1 NOCACHE;

-- Equipes de campo
CREATE TABLE mt_equipe_manutencao (
    id_equipe      NUMBER(10),
    nm_equipe      VARCHAR2(60)  NOT NULL,
    qt_operadores  NUMBER(3)     NOT NULL,
    CONSTRAINT pk_equipe        PRIMARY KEY (id_equipe),
    CONSTRAINT uk_equipe_nome   UNIQUE (nm_equipe),
    CONSTRAINT ck_equipe_oper   CHECK (qt_operadores > 0)
);

-- Trechos de rodovia monitorados
-- tp_perfil define a subclasse Java carregada (TrechoUmido / TrechoSeco)
CREATE TABLE mt_trecho_rodovia (
    id_trecho           NUMBER(10),
    ds_rodovia          VARCHAR2(20)   NOT NULL,
    nr_km_inicial       NUMBER(8,2)    NOT NULL,
    nr_km_final         NUMBER(8,2)    NOT NULL,
    nr_nivel_vegetacao  NUMBER(8,2)    DEFAULT 0 NOT NULL,
    tp_perfil           VARCHAR2(10)   NOT NULL,
    id_equipe           NUMBER(10),
    CONSTRAINT pk_trecho         PRIMARY KEY (id_trecho),
    CONSTRAINT uk_trecho_faixa   UNIQUE (ds_rodovia, nr_km_inicial, nr_km_final),
    CONSTRAINT ck_trecho_km      CHECK (nr_km_final > nr_km_inicial),
    CONSTRAINT ck_trecho_nivel   CHECK (nr_nivel_vegetacao >= 0),
    CONSTRAINT ck_trecho_perfil  CHECK (tp_perfil IN ('UMIDO', 'SECO')),
    CONSTRAINT fk_trecho_equipe  FOREIGN KEY (id_equipe)
        REFERENCES mt_equipe_manutencao (id_equipe)
);

-- Intervencoes executadas nos trechos
CREATE TABLE mt_intervencao (
    id_intervencao     NUMBER(10),
    id_trecho          NUMBER(10)     NOT NULL,
    id_equipe          NUMBER(10),
    tp_intervencao     VARCHAR2(20)   NOT NULL,
    nr_nivel_antes     NUMBER(8,2)    NOT NULL,
    nr_nivel_depois    NUMBER(8,2)    NOT NULL,
    vl_custo_estimado  NUMBER(10,2)   NOT NULL,
    dt_execucao        TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_intervencao        PRIMARY KEY (id_intervencao),
    CONSTRAINT ck_intervencao_tipo   CHECK (tp_intervencao IN ('ROCADA', 'PULVERIZACAO')),
    CONSTRAINT ck_intervencao_custo  CHECK (vl_custo_estimado >= 0),
    CONSTRAINT fk_intervencao_trecho FOREIGN KEY (id_trecho)
        REFERENCES mt_trecho_rodovia (id_trecho),
    CONSTRAINT fk_intervencao_equipe FOREIGN KEY (id_equipe)
        REFERENCES mt_equipe_manutencao (id_equipe)
);

-- Historico dos relatorios de prioridade gerados
CREATE TABLE mt_relatorio_prioridade (
    id_relatorio  NUMBER(10),
    dt_geracao    TIMESTAMP     DEFAULT SYSTIMESTAMP NOT NULL,
    qt_urgente    NUMBER(4)     DEFAULT 0 NOT NULL,
    qt_critico    NUMBER(4)     DEFAULT 0 NOT NULL,
    qt_atencao    NUMBER(4)     DEFAULT 0 NOT NULL,
    qt_normal     NUMBER(4)     DEFAULT 0 NOT NULL,
    ds_resumo     VARCHAR2(500),
    CONSTRAINT pk_relatorio PRIMARY KEY (id_relatorio),
    CONSTRAINT ck_relatorio_qt CHECK (qt_urgente >= 0 AND qt_critico >= 0
                                  AND qt_atencao >= 0 AND qt_normal >= 0)
);

-- indice usado pela listagem do historico (mais recentes primeiro)
CREATE INDEX ix_relatorio_data ON mt_relatorio_prioridade (dt_geracao DESC);

COMMIT;
