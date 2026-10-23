# Registro de requisições

Saída real das chamadas, gerada em 22/10/2026 21:48 com a API rodando no perfil `local`
(H2 em modo Oracle executando `sql/script-criacao.sql` e `sql/script-dados.sql`).

## Equipes

### 1. Criar equipe (201)

```bash
curl -i -X POST "http://localhost:8080/api/equipes" -H 'Content-Type: application/json' -d '{"nome":"Equipe Delta","quantidadeOperadores":5}'
```

```http
HTTP/1.1 201 
Location: http://localhost:8080/api/equipes/4
```

```json
{
    "id": 4,
    "nome": "Equipe Delta",
    "quantidadeOperadores": 5
}
```

### 2. Listar equipes (200)

```bash
curl -i "http://localhost:8080/api/equipes"
```

```http
HTTP/1.1 200 
```

```json
[
    {
        "id": 1,
        "nome": "Equipe Alfa",
        "quantidadeOperadores": 4
    },
    {
        "id": 2,
        "nome": "Equipe Bravo",
        "quantidadeOperadores": 6
    },
    {
        "id": 3,
        "nome": "Equipe Charlie",
        "quantidadeOperadores": 3
    },
    {
        "id": 4,
        "nome": "Equipe Delta",
        "quantidadeOperadores": 5
    }
]
```

### 3. Atualizar equipe (200)

```bash
curl -i -X PUT "http://localhost:8080/api/equipes/4" -H 'Content-Type: application/json' -d '{"nome":"Equipe Delta","quantidadeOperadores":7}'
```

```http
HTTP/1.1 200 
```

```json
{
    "id": 4,
    "nome": "Equipe Delta",
    "quantidadeOperadores": 7
}
```

### 4. Criar equipe com dados inválidos (400 - Bean Validation)

```bash
curl -i -X POST "http://localhost:8080/api/equipes" -H 'Content-Type: application/json' -d '{"nome":"","quantidadeOperadores":0}'
```

```http
HTTP/1.1 400 
```

```json
{
    "timestamp": "2026-10-22T21:48:55",
    "status": 400,
    "erro": "Bad Request",
    "mensagem": "Dados inválidos",
    "caminho": "/api/equipes",
    "campos": {
        "quantidadeOperadores": "equipe precisa de pelo menos um operador",
        "nome": "nome é obrigatório"
    }
}
```

### 5. Criar equipe com nome repetido (409)

```bash
curl -i -X POST "http://localhost:8080/api/equipes" -H 'Content-Type: application/json' -d '{"nome":"equipe alfa","quantidadeOperadores":2}'
```

```http
HTTP/1.1 409 
```

```json
{
    "timestamp": "2026-10-22T21:48:55",
    "status": 409,
    "erro": "Conflict",
    "mensagem": "Já existe uma equipe chamada Equipe Alfa",
    "caminho": "/api/equipes"
}
```

### 6. Remover equipe que ainda tem trechos (409)

```bash
curl -i -X DELETE "http://localhost:8080/api/equipes/1"
```

```http
HTTP/1.1 409 
```

```json
{
    "timestamp": "2026-10-22T21:48:56",
    "status": 409,
    "erro": "Conflict",
    "mensagem": "Equipe 1 ainda é responsável por trechos",
    "caminho": "/api/equipes/1"
}
```

## Trechos

### 7. Criar trecho (201)

```bash
curl -i -X POST "http://localhost:8080/api/trechos" -H 'Content-Type: application/json' -d '{"rodovia":"SP-280","kmInicial":30.0,"kmFinal":36.5,"nivelVegetacao":52.0,"perfil":"UMIDO","idEquipe":4}'
```

```http
HTTP/1.1 201 
Location: http://localhost:8080/api/trechos/6
```

```json
{
    "id": 6,
    "rodovia": "SP-280",
    "kmInicial": 30.0,
    "kmFinal": 36.5,
    "extensaoKm": 6.5,
    "nivelVegetacao": 52.0,
    "perfil": "UMIDO",
    "prioridade": "CRITICO",
    "intervencaoRecomendada": "Roçada Mecanizada",
    "equipe": {
        "id": 4,
        "nome": "Equipe Delta",
        "quantidadeOperadores": 7
    }
}
```

### 8. Buscar trecho por id (200)

```bash
curl -i "http://localhost:8080/api/trechos/6"
```

```http
HTTP/1.1 200 
```

```json
{
    "id": 6,
    "rodovia": "SP-280",
    "kmInicial": 30.0,
    "kmFinal": 36.5,
    "extensaoKm": 6.5,
    "nivelVegetacao": 52.0,
    "perfil": "UMIDO",
    "prioridade": "CRITICO",
    "intervencaoRecomendada": "Roçada Mecanizada",
    "equipe": {
        "id": 4,
        "nome": "Equipe Delta",
        "quantidadeOperadores": 7
    }
}
```

### 9. Derived query: trechos com nível >= 40 cm (200)

```bash
curl -i "http://localhost:8080/api/trechos?nivelMinimo=40"
```

```http
HTTP/1.1 200 
```

```json
[
    {
        "id": 1,
        "rodovia": "BR-116",
        "kmInicial": 10.0,
        "kmFinal": 15.0,
        "extensaoKm": 5.0,
        "nivelVegetacao": 62.0,
        "perfil": "UMIDO",
        "prioridade": "URGENTE",
        "intervencaoRecomendada": "Roçada Mecanizada",
        "equipe": {
            "id": 1,
            "nome": "Equipe Alfa",
            "quantidadeOperadores": 4
        }
    },
    {
        "id": 6,
        "rodovia": "SP-280",
        "kmInicial": 30.0,
        "kmFinal": 36.5,
        "extensaoKm": 6.5,
        "nivelVegetacao": 52.0,
        "perfil": "UMIDO",
        "prioridade": "CRITICO",
        "intervencaoRecomendada": "Roçada Mecanizada",
        "equipe": {
            "id": 4,
            "nome": "Equipe Delta",
            "quantidadeOperadores": 7
        }
    },
    {
        "id": 2,
        "rodovia": "SP-330",
        "kmInicial": 88.5,
        "kmFinal": 95.0,
        "extensaoKm": 6.5,
        "nivelVegetacao": 44.5,
        "perfil": "SECO",
        "prioridade": "CRITICO",
        "intervencaoRecomendada": "Roçada Mecanizada",
        "equipe": {
            "id": 2,
            "nome": "Equipe Bravo",
            "quantidadeOperadores": 6
        }
    }
]
```

### 10. Derived query: trechos por perfil (200)

```bash
curl -i "http://localhost:8080/api/trechos?perfil=SECO"
```

```http
HTTP/1.1 200 
```

```json
[
    {
        "id": 2,
        "rodovia": "SP-330",
        "kmInicial": 88.5,
        "kmFinal": 95.0,
        "extensaoKm": 6.5,
        "nivelVegetacao": 44.5,
        "perfil": "SECO",
        "prioridade": "CRITICO",
        "intervencaoRecomendada": "Roçada Mecanizada",
        "equipe": {
            "id": 2,
            "nome": "Equipe Bravo",
            "quantidadeOperadores": 6
        }
    },
    {
        "id": 4,
        "rodovia": "SP-070",
        "kmInicial": 12.0,
        "kmFinal": 18.0,
        "extensaoKm": 6.0,
        "nivelVegetacao": 8.0,
        "perfil": "SECO",
        "prioridade": "NORMAL",
        "intervencaoRecomendada": "Sem ação (dentro do limite)",
        "equipe": {
            "id": 3,
            "nome": "Equipe Charlie",
            "quantidadeOperadores": 3
        }
    },
    {
        "id": 5,
        "rodovia": "SP-348",
        "kmInicial": 55.0,
        "kmFinal": 61.5,
        "extensaoKm": 6.5,
        "nivelVegetacao": 18.0,
        "perfil": "SECO",
        "prioridade": "NORMAL",
        "intervencaoRecomendada": "Sem ação (dentro do limite)",
        "equipe": null
    }
]
```

### 11. Atualizar trecho (200)

```bash
curl -i -X PUT "http://localhost:8080/api/trechos/6" -H 'Content-Type: application/json' -d '{"rodovia":"SP-280","kmInicial":30.0,"kmFinal":37.0,"nivelVegetacao":55.0,"perfil":"UMIDO","idEquipe":4}'
```

```http
HTTP/1.1 200 
```

```json
{
    "id": 6,
    "rodovia": "SP-280",
    "kmInicial": 30.0,
    "kmFinal": 37.0,
    "extensaoKm": 7.0,
    "nivelVegetacao": 55.0,
    "perfil": "UMIDO",
    "prioridade": "CRITICO",
    "intervencaoRecomendada": "Roçada Mecanizada",
    "equipe": {
        "id": 4,
        "nome": "Equipe Delta",
        "quantidadeOperadores": 7
    }
}
```

### 12. Nível de vegetação negativo (400)

```bash
curl -i -X POST "http://localhost:8080/api/trechos" -H 'Content-Type: application/json' -d '{"rodovia":"SP-280","kmInicial":40.0,"kmFinal":45.0,"nivelVegetacao":-3.0,"perfil":"SECO"}'
```

```http
HTTP/1.1 400 
```

```json
{
    "timestamp": "2026-10-22T21:48:56",
    "status": 400,
    "erro": "Bad Request",
    "mensagem": "Dados inválidos",
    "caminho": "/api/trechos",
    "campos": {
        "nivelVegetacao": "nivelVegetacao não pode ser negativo"
    }
}
```

### 13. Km final menor que o inicial (400 - regra do Service)

```bash
curl -i -X POST "http://localhost:8080/api/trechos" -H 'Content-Type: application/json' -d '{"rodovia":"SP-280","kmInicial":50.0,"kmFinal":45.0,"nivelVegetacao":3.0,"perfil":"SECO"}'
```

```http
HTTP/1.1 400 
```

```json
{
    "timestamp": "2026-10-22T21:48:56",
    "status": 400,
    "erro": "Bad Request",
    "mensagem": "Quilometragem final deve ser maior que a inicial",
    "caminho": "/api/trechos"
}
```

### 14. Buscar trecho inexistente (404)

```bash
curl -i "http://localhost:8080/api/trechos/999"
```

```http
HTTP/1.1 404 
```

```json
{
    "timestamp": "2026-10-22T21:48:56",
    "status": 404,
    "erro": "Not Found",
    "mensagem": "Trecho 999 não encontrado(a)",
    "caminho": "/api/trechos/999"
}
```

## Intervenções

### 15. Registrar intervenção: executa a roçada e baixa o nível do trecho (201)

```bash
curl -i -X POST "http://localhost:8080/api/intervencoes" -H 'Content-Type: application/json' -d '{"idTrecho":6,"tipo":"ROCADA"}'
```

```http
HTTP/1.1 201 
Location: http://localhost:8080/api/intervencoes/3
```

```json
{
    "id": 3,
    "idTrecho": 6,
    "trecho": "SP-280 KM 30.0 ao 37.0",
    "idEquipe": 4,
    "equipe": "Equipe Delta",
    "tipo": "ROCADA",
    "nivelAntes": 55.0,
    "nivelDepois": 0.0,
    "custoEstimado": 8400.0,
    "dataExecucao": "2026-10-22T21:48:56.385449572"
}
```

### 16. Trecho depois da roçada (200)

```bash
curl -i "http://localhost:8080/api/trechos/6"
```

```http
HTTP/1.1 200 
```

```json
{
    "id": 6,
    "rodovia": "SP-280",
    "kmInicial": 30.0,
    "kmFinal": 37.0,
    "extensaoKm": 7.0,
    "nivelVegetacao": 0.0,
    "perfil": "UMIDO",
    "prioridade": "NORMAL",
    "intervencaoRecomendada": "Sem ação (dentro do limite)",
    "equipe": {
        "id": 4,
        "nome": "Equipe Delta",
        "quantidadeOperadores": 7
    }
}
```

### 17. Intervenção em trecho NORMAL (400 - regra do Service)

```bash
curl -i -X POST "http://localhost:8080/api/intervencoes" -H 'Content-Type: application/json' -d '{"idTrecho":4,"tipo":"PULVERIZACAO"}'
```

```http
HTTP/1.1 400 
```

```json
{
    "timestamp": "2026-10-22T21:48:56",
    "status": 400,
    "erro": "Bad Request",
    "mensagem": "Trecho 4 está em NORMAL (8.0 cm) e não precisa de intervenção",
    "caminho": "/api/intervencoes"
}
```

### 18. Derived query: intervenções por tipo (200)

```bash
curl -i "http://localhost:8080/api/intervencoes?tipo=ROCADA"
```

```http
HTTP/1.1 200 
```

```json
[
    {
        "id": 1,
        "idTrecho": 1,
        "trecho": "BR-116 KM 10.0 ao 15.0",
        "idEquipe": 1,
        "equipe": "Equipe Alfa",
        "tipo": "ROCADA",
        "nivelAntes": 55.0,
        "nivelDepois": 0.0,
        "custoEstimado": 6000.0,
        "dataExecucao": "2026-10-22T21:48:53.912508"
    },
    {
        "id": 3,
        "idTrecho": 6,
        "trecho": "SP-280 KM 30.0 ao 37.0",
        "idEquipe": 4,
        "equipe": "Equipe Delta",
        "tipo": "ROCADA",
        "nivelAntes": 55.0,
        "nivelDepois": 0.0,
        "custoEstimado": 8400.0,
        "dataExecucao": "2026-10-22T21:48:56.38545"
    }
]
```

### 19. Corrigir lançamento de intervenção (200)

```bash
curl -i -X PUT "http://localhost:8080/api/intervencoes/3" -H 'Content-Type: application/json' -d '{"idEquipe":4,"custoEstimado":7500.00}'
```

```http
HTTP/1.1 200 
```

```json
{
    "id": 3,
    "idTrecho": 6,
    "trecho": "SP-280 KM 30.0 ao 37.0",
    "idEquipe": 4,
    "equipe": "Equipe Delta",
    "tipo": "ROCADA",
    "nivelAntes": 55.0,
    "nivelDepois": 0.0,
    "custoEstimado": 7500.0,
    "dataExecucao": "2026-10-22T21:48:56.38545"
}
```

### 20. Remover intervenção (204)

```bash
curl -i -X DELETE "http://localhost:8080/api/intervencoes/3"
```

```http
HTTP/1.1 204 
```

### 21. Remover intervenção inexistente (404)

```bash
curl -i -X DELETE "http://localhost:8080/api/intervencoes/3"
```

```http
HTTP/1.1 404 
```

```json
{
    "timestamp": "2026-10-22T21:48:56",
    "status": 404,
    "erro": "Not Found",
    "mensagem": "Intervenção 3 não encontrado(a)",
    "caminho": "/api/intervencoes/3"
}
```

## Relatórios

### 22. Gerar relatório de prioridade (201)

```bash
curl -i -X POST "http://localhost:8080/api/relatorios"
```

```http
HTTP/1.1 201 
Location: http://localhost:8080/api/relatorios/2
```

```json
{
    "id": 2,
    "geradoEm": "2026-10-22T21:48:56.633382396",
    "qtUrgente": 1,
    "qtCritico": 1,
    "qtAtencao": 1,
    "qtNormal": 3,
    "totalTrechos": 6,
    "resumo": "6 trechos avaliados: 1 urgente(s), 1 crítico(s), 1 em atenção e 3 normal(is). Custo estimado do ciclo: R$ 16950.00.",
    "fila": [
        {
            "idTrecho": 1,
            "trecho": "BR-116 KM 10.0 ao 15.0",
            "perfil": "UMIDO",
            "prioridade": "URGENTE",
            "nivelAntes": 73.0,
            "acao": "Roçada Mecanizada",
            "nivelDepois": 0.0,
            "custoEstimado": 6000.0
        },
        {
            "idTrecho": 2,
            "trecho": "SP-330 KM 88.5 ao 95.0",
            "perfil": "SECO",
            "prioridade": "CRITICO",
            "nivelAntes": 44.5,
            "acao": "Roçada Mecanizada",
            "nivelDepois": 0.0,
            "custoEstimado": 7800.0
        },
        {
            "idTrecho": 3,
            "trecho": "BR-101 KM 40.0 ao 47.0",
            "perfil": "UMIDO",
            "prioridade": "ATENCAO",
            "nivelAntes": 38.0,
            "acao": "Pulverização",
            "nivelDepois": 15.2,
            "custoEstimado": 3150.0
        },
        {
            "idTrecho": 5,
            "trecho": "SP-348 KM 55.0 ao 61.5",
            "perfil": "SECO",
            "prioridade": "NORMAL",
            "nivelAntes": 18.0,
            "acao": "Sem ação (dentro do limite)",
            "nivelDepois": 18.0,
            "custoEstimado": null
        },
        {
            "idTrecho": 6,
            "trecho": "SP-280 KM 30.0 ao 37.0",
            "perfil": "UMIDO",
            "prioridade": "NORMAL",
            "nivelAntes": 11.0,
            "acao": "Sem ação (dentro do limite)",
            "nivelDepois": 11.0,
            "custoEstimado": null
        },
        {
            "idTrecho": 4,
            "trecho": "SP-070 KM 12.0 ao 18.0",
            "perfil": "SECO",
            "prioridade": "NORMAL",
            "nivelAntes": 8.0,
            "acao": "Sem ação (dentro do limite)",
            "nivelDepois": 8.0,
            "custoEstimado": null
        }
    ]
}
```

### 23. Histórico de relatórios (200)

```bash
curl -i "http://localhost:8080/api/relatorios"
```

```http
HTTP/1.1 200 
```

```json
[
    {
        "id": 2,
        "geradoEm": "2026-10-22T21:48:56.633382",
        "qtUrgente": 1,
        "qtCritico": 1,
        "qtAtencao": 1,
        "qtNormal": 3,
        "totalTrechos": 6,
        "resumo": "6 trechos avaliados: 1 urgente(s), 1 crítico(s), 1 em atenção e 3 normal(is). Custo estimado do ciclo: R$ 16950.00."
    },
    {
        "id": 1,
        "geradoEm": "2026-10-22T21:48:53.913379",
        "qtUrgente": 0,
        "qtCritico": 2,
        "qtAtencao": 1,
        "qtNormal": 2,
        "totalTrechos": 5,
        "resumo": "Ciclo anterior: 2 trechos criticos rocados e 1 pulverizado."
    }
]
```

### 24. Relatórios por período (200)

```bash
curl -i "http://localhost:8080/api/relatorios/periodo?inicio=2026-10-22&fim=2026-10-22"
```

```http
HTTP/1.1 200 
```

```json
[
    {
        "id": 2,
        "geradoEm": "2026-10-22T21:48:56.633382",
        "qtUrgente": 1,
        "qtCritico": 1,
        "qtAtencao": 1,
        "qtNormal": 3,
        "totalTrechos": 6,
        "resumo": "6 trechos avaliados: 1 urgente(s), 1 crítico(s), 1 em atenção e 3 normal(is). Custo estimado do ciclo: R$ 16950.00."
    },
    {
        "id": 1,
        "geradoEm": "2026-10-22T21:48:53.913379",
        "qtUrgente": 0,
        "qtCritico": 2,
        "qtAtencao": 1,
        "qtNormal": 2,
        "totalTrechos": 5,
        "resumo": "Ciclo anterior: 2 trechos criticos rocados e 1 pulverizado."
    }
]
```

### 25. Período invertido (400)

```bash
curl -i "http://localhost:8080/api/relatorios/periodo?inicio=2026-10-22&fim=2020-01-01"
```

```http
HTTP/1.1 400 
```

```json
{
    "timestamp": "2026-10-22T21:48:56",
    "status": 400,
    "erro": "Bad Request",
    "mensagem": "A data de fim não pode ser anterior à de início",
    "caminho": "/api/relatorios/periodo"
}
```

### 26. Período sem parâmetro obrigatório (400)

```bash
curl -i "http://localhost:8080/api/relatorios/periodo?inicio=2026-10-22"
```

```http
HTTP/1.1 400 
```

```json
{
    "timestamp": "2026-10-22T21:48:56",
    "status": 400,
    "erro": "Bad Request",
    "mensagem": "Parâmetro obrigatório ausente: fim",
    "caminho": "/api/relatorios/periodo"
}
```

## Remoções

### 27. Remover trecho sem histórico (204)

```bash
curl -i -X DELETE "http://localhost:8080/api/trechos/5"
```

```http
HTTP/1.1 204 
```

### 28. Remover trecho já removido (404)

```bash
curl -i -X DELETE "http://localhost:8080/api/trechos/5"
```

```http
HTTP/1.1 404 
```

```json
{
    "timestamp": "2026-10-22T21:48:56",
    "status": 404,
    "erro": "Not Found",
    "mensagem": "Trecho 5 não encontrado(a)",
    "caminho": "/api/trechos/5"
}
```

