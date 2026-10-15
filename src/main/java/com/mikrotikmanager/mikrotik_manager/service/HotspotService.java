
package com.mikrotikmanager.mikrotik_manager.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class HotspotService {

    private final MikrotikService mikrotikService;

    public HotspotService(MikrotikService mikrotikService) {
        this.mikrotikService = mikrotikService;
    }

    // Testa a conexão com os dados informados na tela inicial
    public String testarConexao(
            String endereco,
            String usuario,
            String senha) {
        return mikrotikService.testarConexao(
                endereco,
                usuario,
                senha
        );
    }

    public String consultarStatus() {
        return mikrotikService.consultarStatus();
    }

    public List<Map<String, String>> consultarInterfaces() {
        return mikrotikService.consultarInterfaces();
    }

    // Consulta velocidade e tráfego da interface
    public Map<String, String> consultarTrafegoInterface() {
        return mikrotikService.consultarTrafegoInterface();
    }

    // Consulta o tráfego individual de um usuário conectado
    public Map<String, String> consultarTrafegoUsuario(
            String nomeUsuario) {
        return mikrotikService.consultarTrafegoUsuario(nomeUsuario);
    }

    public List<Map<String, String>> consultarUsuariosConectados() {
        return mikrotikService.consultarUsuariosConectados();
    }

    public List<Map<String, String>> consultarUsuariosCadastrados() {
        return mikrotikService.consultarUsuariosCadastrados();
    }

    public void cadastrarUsuario(
            String nomeUsuario, String senhaUsuario) {
        mikrotikService.cadastrarUsuario(nomeUsuario, senhaUsuario);
    }

    // Edita o nome e, opcionalmente, a senha do usuário
    public void editarUsuario(
            String nomeAtual,
            String novoNome,
            String novaSenha) {
        mikrotikService.editarUsuario(
                nomeAtual,
                novoNome,
                novaSenha
        );
    }

    public void excluirUsuario(String nomeUsuario) {
        mikrotikService.excluirUsuario(nomeUsuario);
    }

    public void bloquearUsuario(String nomeUsuario) {
        mikrotikService.bloquearUsuario(nomeUsuario);
    }

    public void desbloquearUsuario(String nomeUsuario) {
        mikrotikService.desbloquearUsuario(nomeUsuario);
    }
}
