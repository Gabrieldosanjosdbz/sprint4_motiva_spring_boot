package br.com.motiva.service;

import br.com.motiva.dto.ItemFilaResponse;
import br.com.motiva.dto.RelatorioResponse;
import br.com.motiva.exception.RecursoNaoEncontradoException;
import br.com.motiva.exception.RegraNegocioException;
import br.com.motiva.model.IntervencaoOperacional;
import br.com.motiva.model.Prioridade;
import br.com.motiva.model.RelatorioPrioridade;
import br.com.motiva.model.TrechoRodovia;
import br.com.motiva.repository.RelatorioPrioridadeRepository;
import br.com.motiva.repository.TrechoRodoviaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Antigo GeradorRelatorio: mesma lógica da Sprint 3, só que devolvendo JSON em vez de imprimir. */
@Service
public class RelatorioService {

    private final RelatorioPrioridadeRepository relatorioRepository;
    private final TrechoRodoviaRepository trechoRepository;
    private final IntervencaoService intervencaoService;
    private final MotorDeRegras motor;

    public RelatorioService(RelatorioPrioridadeRepository relatorioRepository,
                            TrechoRodoviaRepository trechoRepository,
                            IntervencaoService intervencaoService,
                            MotorDeRegras motor) {
        this.relatorioRepository = relatorioRepository;
        this.trechoRepository = trechoRepository;
        this.intervencaoService = intervencaoService;
        this.motor = motor;
    }

    /**
     * Um ciclo completo: sincroniza os sensores IoT, monta a fila por nível de vegetação,
     * executa as intervenções recomendadas e grava o resumo no histórico.
     */
    @Transactional
    public RelatorioResponse gerar() {
        List<TrechoRodovia> trechos = trechoRepository.findAll();
        motor.sincronizarSensores(trechos);

        List<TrechoRodovia> fila = new ArrayList<>(trechos);
        fila.sort((a, b) -> Double.compare(b.getNivelVegetacao(), a.getNivelVegetacao()));

        Map<Prioridade, Integer> contagem = new EnumMap<>(Prioridade.class);
        for (Prioridade prioridade : Prioridade.values()) {
            contagem.put(prioridade, 0);
        }

        List<ItemFilaResponse> itens = new ArrayList<>();
        double custoTotal = 0.0;

        for (TrechoRodovia trecho : fila) {
            Prioridade prioridade = trecho.prioridade();
            contagem.merge(prioridade, 1, Integer::sum);

            double nivelAntes = trecho.getNivelVegetacao();
            IntervencaoOperacional intervencao = motor.recomendarIntervencao(trecho);
            Double custo = null;
            if (intervencao != null) {
                custo = intervencaoService.executar(trecho, intervencao, trecho.getEquipeResponsavel())
                        .getCustoEstimado();
                custoTotal += custo;
            }

            itens.add(new ItemFilaResponse(trecho.getId(), trecho.descricao(), trecho.getPerfil(), prioridade,
                    nivelAntes, intervencao == null ? "Sem ação (dentro do limite)" : intervencao.getNome(),
                    trecho.getNivelVegetacao(), custo));
        }

        RelatorioPrioridade relatorio = new RelatorioPrioridade(
                contagem.get(Prioridade.URGENTE),
                contagem.get(Prioridade.CRITICO),
                contagem.get(Prioridade.ATENCAO),
                contagem.get(Prioridade.NORMAL),
                montarResumo(contagem, fila.size(), custoTotal));

        return RelatorioResponse.de(relatorioRepository.save(relatorio), itens);
    }

    @Transactional(readOnly = true)
    public List<RelatorioResponse> listarHistorico() {
        return relatorioRepository.findAllByOrderByGeradoEmDesc().stream()
                .map(RelatorioResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public RelatorioResponse buscar(Long id) {
        return relatorioRepository.findById(id)
                .map(RelatorioResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Relatório", id));
    }

    // período fechado nos dois lados: de 00:00 do início até 23:59:59 do fim
    @Transactional(readOnly = true)
    public List<RelatorioResponse> buscarPorPeriodo(LocalDate inicio, LocalDate fim) {
        if (fim.isBefore(inicio)) {
            throw new RegraNegocioException("A data de fim não pode ser anterior à de início");
        }
        LocalDateTime de = inicio.atStartOfDay();
        LocalDateTime ate = fim.atTime(LocalTime.MAX);
        return relatorioRepository.findByGeradoEmBetweenOrderByGeradoEmDesc(de, ate).stream()
                .map(RelatorioResponse::de)
                .toList();
    }

    private String montarResumo(Map<Prioridade, Integer> contagem, int total, double custoTotal) {
        return String.format(
                "%d trechos avaliados: %d urgente(s), %d crítico(s), %d em atenção e %d normal(is). "
                        + "Custo estimado do ciclo: R$ %.2f.",
                total,
                contagem.get(Prioridade.URGENTE),
                contagem.get(Prioridade.CRITICO),
                contagem.get(Prioridade.ATENCAO),
                contagem.get(Prioridade.NORMAL),
                custoTotal);
    }
}
