package br.com.nexus.nexus_wms.infrastructure.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/public")
    public String publicAccess() {
        return "Conteúdo Público. Qualquer um pode acessar.";
    }

    @GetMapping("/user")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OPERADOR_ESTOQUE') or hasAuthority('SCOPE_GERENTE')")
    public String userAccess() {
        return "Conteúdo de Usuário. Token JWT validado!";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public String adminAccess() {
        return "Conteúdo Admin. Apenas ADMINS podem acessar!";
    }
}
