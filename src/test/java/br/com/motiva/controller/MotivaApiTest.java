package br.com.motiva.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sobe a aplicação inteira no H2 (perfil local, com os scripts da Sprint 3)
 * e bate nos endpoints pelo MockMvc. Cada teste roda numa transação que é desfeita no fim.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class MotivaApiTest {

    // trecho 1 (BR-116, 62 cm, Equipe Alfa) e equipe 1 vêm do sql/script-dados.sql
    private static final String TRECHO_NOVO = """
            {"rodovia":"SP-280","kmInicial":30.0,"kmFinal":36.5,"nivelVegetacao":52.0,"perfil":"UMIDO","idEquipe":1}
            """;

    @Autowired
    private MockMvc mvc;

    @Test
    void criarTrechoDevolve201ComLocationECorpo() throws Exception {
        mvc.perform(post("/api/trechos").contentType(MediaType.APPLICATION_JSON).content(TRECHO_NOVO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("http://localhost/api/trechos/")))
                .andExpect(jsonPath("$.rodovia").value("SP-280"))
                .andExpect(jsonPath("$.prioridade").value("CRITICO"))
                .andExpect(jsonPath("$.equipe.nome").value("Equipe Alfa"));
    }

    @Test
    void buscarTrechoInexistenteDevolve404() throws Exception {
        mvc.perform(get("/api/trechos/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Trecho 9999 não encontrado(a)"));
    }

    @Test
    void nivelVegetacaoNegativoDevolve400() throws Exception {
        String corpo = TRECHO_NOVO.replace("52.0", "-1.0");
        mvc.perform(post("/api/trechos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nivelVegetacao").exists());
    }

    @Test
    void kmFinalMenorQueInicialDevolve400() throws Exception {
        String corpo = TRECHO_NOVO.replace("36.5", "20.0");
        mvc.perform(post("/api/trechos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Quilometragem final deve ser maior que a inicial"));
    }

    @Test
    void naoDeixaTrocarPerfilNoPut() throws Exception {
        String corpo = """
                {"rodovia":"BR-116","kmInicial":10.0,"kmFinal":15.0,"nivelVegetacao":62.0,"perfil":"SECO","idEquipe":1}
                """;
        mvc.perform(put("/api/trechos/1").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizarTrechoDevolve200() throws Exception {
        String corpo = """
                {"rodovia":"BR-116","kmInicial":10.0,"kmFinal":15.0,"nivelVegetacao":30.0,"perfil":"UMIDO"}
                """;
        mvc.perform(put("/api/trechos/1").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivelVegetacao").value(30.0))
                .andExpect(jsonPath("$.prioridade").value("ATENCAO"))
                .andExpect(jsonPath("$.equipe").doesNotExist());
    }

    @Test
    void removerTrechoDevolve204EDepois404() throws Exception {
        String resposta = mvc.perform(post("/api/trechos").contentType(MediaType.APPLICATION_JSON).content(TRECHO_NOVO))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(resposta, "$.id");

        mvc.perform(delete("/api/trechos/" + id)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/trechos/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void derivedQueryFiltraPorNivelMinimo() throws Exception {
        mvc.perform(get("/api/trechos").param("nivelMinimo", "40"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].nivelVegetacao", everyItem(greaterThanOrEqualTo(40.0))));
    }

    @Test
    void removerEquipeComTrechosDevolve409() throws Exception {
        mvc.perform(delete("/api/equipes/1")).andExpect(status().isConflict());
    }

    @Test
    void crudDeEquipe() throws Exception {
        String resposta = mvc.perform(post("/api/equipes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Equipe Delta\",\"quantidadeOperadores\":5}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(resposta, "$.id");

        mvc.perform(put("/api/equipes/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Equipe Delta\",\"quantidadeOperadores\":8}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeOperadores").value(8));

        mvc.perform(delete("/api/equipes/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/equipes/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void equipeComNomeRepetidoDevolve409() throws Exception {
        mvc.perform(post("/api/equipes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"EQUIPE ALFA\",\"quantidadeOperadores\":2}"))
                .andExpect(status().isConflict());
    }

    @Test
    void registrarRocadaZeraONivelDoTrecho() throws Exception {
        mvc.perform(post("/api/intervencoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idTrecho\":1,\"tipo\":\"ROCADA\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nivelAntes").value(62.0))
                .andExpect(jsonPath("$.nivelDepois").value(0.0))
                .andExpect(jsonPath("$.custoEstimado").value(6000.0))
                .andExpect(jsonPath("$.equipe").value("Equipe Alfa"));

        mvc.perform(get("/api/trechos/1"))
                .andExpect(jsonPath("$.nivelVegetacao").value(0.0))
                .andExpect(jsonPath("$.prioridade").value("NORMAL"));
    }

    @Test
    void intervencaoEmTrechoNormalDevolve400() throws Exception {
        mvc.perform(post("/api/intervencoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idTrecho\":4,\"tipo\":\"PULVERIZACAO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void derivedQueryFiltraIntervencaoPorTipo() throws Exception {
        mvc.perform(get("/api/intervencoes").param("tipo", "PULVERIZACAO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipo").value("PULVERIZACAO"));
    }

    @Test
    void gerarRelatorioPersisteNoHistorico() throws Exception {
        mvc.perform(post("/api/relatorios"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalTrechos").value(5))
                .andExpect(jsonPath("$.qtUrgente").value(1))
                .andExpect(jsonPath("$.fila", hasSize(5)))
                .andExpect(jsonPath("$.fila[0].prioridade").value("URGENTE"));

        // 1 do script de dados + o que acabou de ser gerado
        mvc.perform(get("/api/relatorios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].fila").doesNotExist());
    }

    @Test
    void consultaPorPeriodo() throws Exception {
        mvc.perform(post("/api/relatorios")).andExpect(status().isCreated());
        String hoje = java.time.LocalDate.now().toString();

        mvc.perform(get("/api/relatorios/periodo").param("inicio", hoje).param("fim", hoje))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/api/relatorios/periodo").param("inicio", "2020-01-01").param("fim", "2020-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void periodoInvalidoDevolve400() throws Exception {
        mvc.perform(get("/api/relatorios/periodo").param("inicio", "2026-10-10").param("fim", "2026-10-01"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/relatorios/periodo").param("inicio", "2026-10-10"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/relatorios/periodo").param("inicio", "ontem").param("fim", "hoje"))
                .andExpect(status().isBadRequest());
    }
}
