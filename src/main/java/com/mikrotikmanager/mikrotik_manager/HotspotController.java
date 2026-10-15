package com.mikrotikmanager.mikrotik_manager;

import com.mikrotikmanager.mikrotik_manager.service.HotspotService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hotspot")
public class HotspotController {

    private final HotspotService hotspotService;

    public HotspotController(HotspotService hotspotService) {
        this.hotspotService = hotspotService;
    }

    @GetMapping("/status")
    public String status() {
        return hotspotService.consultarStatus();
    }

    @GetMapping("/interfaces")
    public List<Map<String, String>> interfaces() {
        return hotspotService.consultarInterfaces();
    }

    @GetMapping("/trafego")
    public ResponseEntity<?> trafego() {
        try {
            return ResponseEntity.ok(
                    hotspotService.consultarTrafegoInterface()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of(
                            "erro",
                            "Não foi possível consultar o tráfego: "
                                    + e.getMessage()
                    ));
        }
    }

    @GetMapping("/usuarios/{nomeUsuario}/trafego")
    public ResponseEntity<?> trafegoUsuario(
            @PathVariable String nomeUsuario) {

        try {
            return ResponseEntity.ok(
                    hotspotService.consultarTrafegoUsuario(nomeUsuario)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of(
                            "erro",
                            "Não foi possível consultar o tráfego do usuário: "
                                    + e.getMessage()
                    ));
        }
    }

    @GetMapping("/usuarios")
    public List<Map<String, String>> usuarios() {
        return hotspotService.consultarUsuariosConectados();
    }

    @GetMapping("/usuarios/cadastrados")
    public List<Map<String, String>> usuariosCadastrados() {
        return hotspotService.consultarUsuariosCadastrados();
    }

    @PostMapping("/usuarios/cadastrar")
    public ResponseEntity<String> cadastrarUsuario(
            @RequestBody Map<String, String> dados) {

        String usuario = dados.get("usuario");
        String senha = dados.get("senha");

        if (usuario == null || usuario.isBlank()
                || senha == null || senha.isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Preencha o nome de usuário e a senha.");
        }

        try {
            hotspotService.cadastrarUsuario(usuario, senha);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Usuário cadastrado com sucesso!");

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @PutMapping("/usuarios/{nomeAtual}")
    public ResponseEntity<String> editarUsuario(
            @PathVariable String nomeAtual,
            @RequestBody Map<String, String> dados) {

        String novoNome = dados.get("novoNome");
        String novaSenha = dados.get("novaSenha");

        if (nomeAtual == null || nomeAtual.isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Informe o nome atual do usuário.");
        }

        if (novoNome == null || novoNome.isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Informe o novo nome do usuário.");
        }

        try {
            hotspotService.editarUsuario(
                    nomeAtual,
                    novoNome,
                    novaSenha
            );

            return ResponseEntity.ok(
                    "Usuário atualizado com sucesso!");

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/usuarios/{nomeUsuario}")
    public ResponseEntity<String> excluirUsuario(
            @PathVariable String nomeUsuario) {

        if (nomeUsuario == null || nomeUsuario.isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Informe o usuário que deseja excluir.");
        }

        try {
            hotspotService.excluirUsuario(nomeUsuario);

            return ResponseEntity.ok(
                    "Usuário excluído com sucesso!");

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @PostMapping("/usuarios/{nomeUsuario}/bloquear")
    public ResponseEntity<String> bloquearUsuario(
            @PathVariable String nomeUsuario) {

        try {
            hotspotService.bloquearUsuario(nomeUsuario);

            return ResponseEntity.ok(
                    "Usuário bloqueado com sucesso!");

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @PostMapping("/usuarios/{nomeUsuario}/desbloquear")
    public ResponseEntity<String> desbloquearUsuario(
            @PathVariable String nomeUsuario) {

        try {
            hotspotService.desbloquearUsuario(nomeUsuario);

            return ResponseEntity.ok(
                    "Usuário desbloqueado com sucesso!");

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}