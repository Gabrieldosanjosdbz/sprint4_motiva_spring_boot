package br.com.motiva.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// testes de domínio da Sprint 3, agora em JUnit
class TrechoRodoviaTest {

    @Test
    void acumulaCrescimentoSobreONivelExistente() {
        TrechoRodovia trecho = new TrechoSeco("BR-116", 10.0, 15.0, 10.0);
        trecho.registrarCrescimento(5.0);
        assertEquals(15.0, trecho.getNivelVegetacao());
    }

    @Test
    void rejeitaNivelInicialNegativo() {
        assertThrows(IllegalArgumentException.class, () -> new TrechoUmido("BR-116", 10.0, 15.0, -5.0));
    }

    @Test
    void rejeitaKmFinalMenorQueInicial() {
        assertThrows(IllegalArgumentException.class, () -> new TrechoSeco("BR-116", 15.0, 10.0, 0.0));
    }

    @Test
    void umidoCresceMaisQueSecoNaMesmaSemana() {
        TrechoRodovia umido = new TrechoUmido("BR-116", 10.0, 15.0, 0.0);
        TrechoRodovia seco = new TrechoSeco("BR-116", 10.0, 15.0, 0.0);
        umido.simularSemana();
        seco.simularSemana();
        assertTrue(umido.getNivelVegetacao() > seco.getNivelVegetacao());
    }

    @Test
    void classificaNasQuatroFaixas() {
        assertEquals(Prioridade.URGENTE, Prioridade.de(60.0));
        assertEquals(Prioridade.CRITICO, Prioridade.de(59.9));
        assertEquals(Prioridade.CRITICO, Prioridade.de(40.0));
        assertEquals(Prioridade.ATENCAO, Prioridade.de(20.0));
        assertEquals(Prioridade.NORMAL, Prioridade.de(19.9));
    }

    @Test
    void perfilCasaComODiscriminadorDoBanco() {
        assertEquals("UMIDO", new TrechoUmido("BR-116", 10.0, 15.0, 0.0).getPerfil());
        assertEquals("SECO", new TrechoSeco("BR-116", 10.0, 15.0, 0.0).getPerfil());
        assertInstanceOf(MonitoravelViaIoT.class, new TrechoUmido("BR-116", 10.0, 15.0, 0.0));
    }

    @Test
    void pulverizacaoReduz60PorCentoERocadaZera() {
        TrechoRodovia trecho = new TrechoSeco("SP-330", 0.0, 10.0, 50.0);
        new Pulverizacao().executarServico(trecho);
        assertEquals(20.0, trecho.getNivelVegetacao(), 0.001);
        new RocadaMecanizada().executarServico(trecho);
        assertEquals(0.0, trecho.getNivelVegetacao());
    }

    @Test
    void custoEstimadoEhPorKm() {
        TrechoRodovia trecho = new TrechoSeco("SP-330", 0.0, 10.0, 50.0);
        assertEquals(12000.0, new RocadaMecanizada().custoEstimado(trecho));
        assertEquals(4500.0, new Pulverizacao().custoEstimado(trecho));
    }
}
