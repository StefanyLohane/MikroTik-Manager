package com.mikrotikmanager.mikrotik_manager;

import com.mikrotikmanager.mikrotik_manager.service.HotspotService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final HotspotService hotspotService;

    public AuthController(HotspotService hotspotService) {
        this.hotspotService = hotspotService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody Map<String, String> dados,
            HttpServletRequest request) {

        String endereco = dados.get("endereco");
        String usuario = dados.get("usuario");
        String senha = dados.get("senha");

        if (endereco == null || endereco.isBlank()
                || usuario == null || usuario.isBlank()
                || senha == null || senha.isBlank()) {

            return ResponseEntity.badRequest().body(Map.of(
                    "erro", "Preencha o endereço, o usuário e a senha."
            ));
        }

        try {
            String resultado = hotspotService.testarConexao(
                    endereco.trim(),
                    usuario.trim(),
                    senha
            );

            HttpSession sessaoAnterior = request.getSession(false);

            if (sessaoAnterior != null) {
                sessaoAnterior.invalidate();
            }

            HttpSession sessao = request.getSession(true);

            sessao.setAttribute("mikrotikEndereco", endereco.trim());
            sessao.setAttribute("mikrotikUsuario", usuario.trim());
            sessao.setAttribute("mikrotikSenha", senha);

            sessao.setMaxInactiveInterval(30 * 60);

            return ResponseEntity.ok(Map.of(
                    "mensagem", resultado
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "erro", "Não foi possível conectar. Confira o IP, "
                                    + "o usuário, a senha e a API do MikroTik."
                    ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            HttpServletRequest request) {

        HttpSession sessao = request.getSession(false);

        if (sessao == null) {
            return ResponseEntity.ok(Map.of(
                    "mensagem", "Nenhuma sessão ativa."
            ));
        }

        try {
            sessao.invalidate();

            return ResponseEntity.ok(Map.of(
                    "mensagem", "Sessão encerrada."
            ));

        } catch (IllegalStateException e) {
            return ResponseEntity.ok(Map.of(
                    "mensagem", "A sessão já estava encerrada."
            ));
        }
    }
}
