package br.com.motiva.controller;

import br.com.motiva.dto.RelatorioResponse;
import br.com.motiva.service.RelatorioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioService service;

    public RelatorioController(RelatorioService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RelatorioResponse> gerar() {
        RelatorioResponse gerado = service.gerar();
        URI local = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(gerado.id()).toUri();
        return ResponseEntity.created(local).body(gerado);
    }

    @GetMapping
    public List<RelatorioResponse> historico() {
        return service.listarHistorico();
    }

    @GetMapping("/{id}")
    public RelatorioResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping("/periodo")
    public List<RelatorioResponse> porPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return service.buscarPorPeriodo(inicio, fim);
    }
}
