-- MOTIVA - Sprint 3
-- Dados de teste (rodar depois do script-criacao.sql)

DELETE FROM mt_relatorio_prioridade;
DELETE FROM mt_intervencao;
DELETE FROM mt_trecho_rodovia;
DELETE FROM mt_equipe_manutencao;

INSERT INTO mt_equipe_manutencao (id_equipe, nm_equipe, qt_operadores)
VALUES (sq_equipe.NEXTVAL, 'Equipe Alfa', 4);
INSERT INTO mt_equipe_manutencao (id_equipe, nm_equipe, qt_operadores)
VALUES (sq_equipe.NEXTVAL, 'Equipe Bravo', 6);
INSERT INTO mt_equipe_manutencao (id_equipe, nm_equipe, qt_operadores)
VALUES (sq_equipe.NEXTVAL, 'Equipe Charlie', 3);

-- Trechos: mistura de perfis e niveis para o relatorio cair em todas as faixas
INSERT INTO mt_trecho_rodovia (id_trecho, ds_rodovia, nr_km_inicial, nr_km_final, nr_nivel_vegetacao, tp_perfil, id_equipe)
VALUES (sq_trecho.NEXTVAL, 'BR-116', 10.0, 15.0, 62.0, 'UMIDO',
        (SELECT id_equipe FROM mt_equipe_manutencao WHERE nm_equipe = 'Equipe Alfa'));

INSERT INTO mt_trecho_rodovia (id_trecho, ds_rodovia, nr_km_inicial, nr_km_final, nr_nivel_vegetacao, tp_perfil, id_equipe)
VALUES (sq_trecho.NEXTVAL, 'SP-330', 88.5, 95.0, 44.5, 'SECO',
        (SELECT id_equipe FROM mt_equipe_manutencao WHERE nm_equipe = 'Equipe Bravo'));

INSERT INTO mt_trecho_rodovia (id_trecho, ds_rodovia, nr_km_inicial, nr_km_final, nr_nivel_vegetacao, tp_perfil, id_equipe)
VALUES (sq_trecho.NEXTVAL, 'BR-101', 40.0, 47.0, 27.0, 'UMIDO',
        (SELECT id_equipe FROM mt_equipe_manutencao WHERE nm_equipe = 'Equipe Alfa'));

INSERT INTO mt_trecho_rodovia (id_trecho, ds_rodovia, nr_km_inicial, nr_km_final, nr_nivel_vegetacao, tp_perfil, id_equipe)
VALUES (sq_trecho.NEXTVAL, 'SP-070', 12.0, 18.0, 8.0, 'SECO',
        (SELECT id_equipe FROM mt_equipe_manutencao WHERE nm_equipe = 'Equipe Charlie'));

INSERT INTO mt_trecho_rodovia (id_trecho, ds_rodovia, nr_km_inicial, nr_km_final, nr_nivel_vegetacao, tp_perfil, id_equipe)
VALUES (sq_trecho.NEXTVAL, 'SP-348', 55.0, 61.5, 18.0, 'SECO', NULL);

-- Historico de intervencoes ja executadas em ciclos anteriores
INSERT INTO mt_intervencao (id_intervencao, id_trecho, id_equipe, tp_intervencao, nr_nivel_antes, nr_nivel_depois, vl_custo_estimado)
VALUES (sq_intervencao.NEXTVAL,
        (SELECT id_trecho FROM mt_trecho_rodovia WHERE ds_rodovia = 'BR-116' AND nr_km_inicial = 10.0),
        (SELECT id_equipe FROM mt_equipe_manutencao WHERE nm_equipe = 'Equipe Alfa'),
        'ROCADA', 55.0, 0.0, 6000.00);

INSERT INTO mt_intervencao (id_intervencao, id_trecho, id_equipe, tp_intervencao, nr_nivel_antes, nr_nivel_depois, vl_custo_estimado)
VALUES (sq_intervencao.NEXTVAL,
        (SELECT id_trecho FROM mt_trecho_rodovia WHERE ds_rodovia = 'SP-330' AND nr_km_inicial = 88.5),
        (SELECT id_equipe FROM mt_equipe_manutencao WHERE nm_equipe = 'Equipe Bravo'),
        'PULVERIZACAO', 30.0, 12.0, 2925.00);

-- Relatorio de um ciclo anterior, para o historico nao vir vazio
INSERT INTO mt_relatorio_prioridade (id_relatorio, qt_urgente, qt_critico, qt_atencao, qt_normal, ds_resumo)
VALUES (sq_relatorio.NEXTVAL, 0, 2, 1, 2, 'Ciclo anterior: 2 trechos criticos rocados e 1 pulverizado.');

COMMIT;

-- Conferencia rapida
SELECT t.id_trecho, t.ds_rodovia, t.nr_nivel_vegetacao, t.tp_perfil, e.nm_equipe
  FROM mt_trecho_rodovia t
  LEFT JOIN mt_equipe_manutencao e ON e.id_equipe = t.id_equipe
 ORDER BY t.nr_nivel_vegetacao DESC;
