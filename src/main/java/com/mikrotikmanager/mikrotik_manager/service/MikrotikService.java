
package com.mikrotikmanager.mikrotik_manager.service;

import me.legrange.mikrotik.ApiConnection;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MikrotikService {

    private final String host = "192.168.56.2";
    private final String usuario = "admin";
    private final String senha = "123";

    /*
     * NOVO MÉTODO:
     * Testa a conexão usando os dados informados na tela inicial.
     * Não altera as credenciais usadas pelos métodos existentes.
     */
    public String testarConexao(
            String endereco,
            String usuarioConexao,
            String senhaConexao) {

        if (endereco == null || endereco.isBlank()) {
            throw new RuntimeException(
                    "Informe o endereço IP do MikroTik.");
        }

        if (usuarioConexao == null || usuarioConexao.isBlank()) {
            throw new RuntimeException(
                    "Informe o usuário do MikroTik.");
        }

        if (senhaConexao == null || senhaConexao.isBlank()) {
            throw new RuntimeException(
                    "Informe a senha do MikroTik.");
        }

        try (ApiConnection con = ApiConnection.connect(endereco.trim())) {
            con.login(usuarioConexao.trim(), senhaConexao);

            List<Map<String, String>> resultado =
                    con.execute("/system/identity/print");

            if (resultado.isEmpty()) {
                return "Conexão realizada, mas a identidade não foi encontrada.";
            }

            String identidade = resultado.get(0).get("name");

            return "Conexão realizada com sucesso! "
                    + "Nome do MikroTik: " + identidade;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Não foi possível conectar ao MikroTik. "
                            + "Verifique o IP, as credenciais e se o serviço "
                            + "API está habilitado. Detalhes: "
                            + e.getMessage(), e);
        }
    }

    public String consultarStatus() {
        try (ApiConnection con = ApiConnection.connect(host)) {
            con.login(usuario, senha);

            List<Map<String, String>> resultado =
                    con.execute("/system/identity/print");

            if (resultado.isEmpty()) {
                return "Conectou, mas não encontrou a identidade.";
            }

            String identidade = resultado.get(0).get("name");

            return "Conexão realizada com sucesso! "
                    + "Nome do MikroTik: " + identidade;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao consultar o MikroTik: "
                            + e.getMessage(), e);
        }
    }

    public List<Map<String, String>> consultarInterfaces() {
        try (ApiConnection con = ApiConnection.connect(host)) {
            con.login(usuario, senha);
            return con.execute("/interface/print");

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao consultar interfaces: "
                            + e.getMessage(), e);
        }
    }

    // Consulta a velocidade e o tráfego da ether2
    public Map<String, String> consultarTrafegoInterface() {
        try (ApiConnection con = ApiConnection.connect(host)) {
            con.login(usuario, senha);

            List<Map<String, String>> velocidade =
                    con.execute(
                            "/interface/monitor-traffic interface=ether2 once"
                    );

            List<Map<String, String>> interfaces =
                    con.execute("/interface/print");

            Map<String, String> trafego = new HashMap<>();

            trafego.put("interface", "ether2");

            if (!velocidade.isEmpty()) {
                Map<String, String> dadosVelocidade =
                        velocidade.get(0);

                trafego.put(
                        "rx-bits-per-second",
                        dadosVelocidade.getOrDefault(
                                "rx-bits-per-second", "0")
                );

                trafego.put(
                        "tx-bits-per-second",
                        dadosVelocidade.getOrDefault(
                                "tx-bits-per-second", "0")
                );
            } else {
                trafego.put("rx-bits-per-second", "0");
                trafego.put("tx-bits-per-second", "0");
            }

            boolean encontrada = false;

            for (Map<String, String> item : interfaces) {
                if ("ether2".equals(item.get("name"))) {
                    trafego.put(
                            "rx-byte",
                            item.getOrDefault("rx-byte", "0")
                    );

                    trafego.put(
                            "tx-byte",
                            item.getOrDefault("tx-byte", "0")
                    );

                    encontrada = true;
                    break;
                }
            }

            if (!encontrada) {
                throw new RuntimeException(
                        "A interface ether2 não foi encontrada.");
            }

            return trafego;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao consultar o tráfego da ether2: "
                            + e.getMessage(), e);
        }
    }

    public List<Map<String, String>> consultarUsuariosConectados() {
        try (ApiConnection con = ApiConnection.connect(host)) {
            con.login(usuario, senha);
            return con.execute("/ip/hotspot/active/print");

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao consultar usuários conectados: "
                            + e.getMessage(), e);
        }
    }

    public List<Map<String, String>> consultarUsuariosCadastrados() {
        try (ApiConnection con = ApiConnection.connect(host)) {
            con.login(usuario, senha);
            return con.execute("/ip/hotspot/user/print");

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao consultar usuários cadastrados: "
                            + e.getMessage(), e);
        }
    }

    public void cadastrarUsuario(
            String nomeUsuario, String senhaUsuario) {

        try (ApiConnection con = ApiConnection.connect(host)) {
            con.login(usuario, senha);

            List<Map<String, String>> existentes =
                    con.execute("/ip/hotspot/user/print");

            for (Map<String, String> existente : existentes) {
                if (nomeUsuario.equals(existente.get("name"))) {
                    throw new RuntimeException(
                            "Esse nome de usuário já está cadastrado.");
                }
            }

            con.execute(
                    "/ip/hotspot/user/add name="
                            + nomeUsuario
                            + " password="
                            + senhaUsuario
                            + " profile=default"
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao cadastrar usuário no MikroTik: "
                            + e.getMessage(), e);
        }
    }

    public void editarUsuario(
            String nomeAtual,
            String novoNome,
            String novaSenha) {

        if (nomeAtual == null || nomeAtual.isBlank()) {
            throw new RuntimeException(
                    "Informe o nome atual do usuário.");
        }

        if (novoNome == null || novoNome.isBlank()) {
            throw new RuntimeException(
                    "Informe o novo nome do usuário.");
        }

        try (ApiConnection con = ApiConnection.connect(host)) {
            con.login(usuario, senha);

            List<Map<String, String>> usuarios =
                    con.execute("/ip/hotspot/user/print");

            Map<String, String> usuarioEncontrado = null;

            for (Map<String, String> item : usuarios) {
                if (nomeAtual.equals(item.get("name"))) {
                    usuarioEncontrado = item;
                    break;
                }
            }

            if (usuarioEncontrado == null) {
                throw new RuntimeException(
                        "Usuário não encontrado no MikroTik.");
            }

            String id = usuarioEncontrado.get(".id");

            if (id == null || id.isBlank()) {
                throw new RuntimeException(
                        "Não foi possível identificar o usuário.");
            }

            // Verifica se o novo nome já pertence a outro usuário
            for (Map<String, String> item : usuarios) {
                if (novoNome.equals(item.get("name"))
                        && !nomeAtual.equals(item.get("name"))) {
                    throw new RuntimeException(
                            "Já existe um usuário com esse nome.");
                }
            }

            // Altera o nome
            con.execute(
                    "/ip/hotspot/user/set numbers=" + id
                            + " name=" + novoNome
            );

            // Altera a senha somente se uma nova senha foi informada
            if (novaSenha != null && !novaSenha.isBlank()) {
                con.execute(
                        "/ip/hotspot/user/set numbers=" + id
                                + " password=" + novaSenha
                );
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao editar usuário no MikroTik: "
                            + e.getMessage(), e);
        }
    }

    public void excluirUsuario(String nomeUsuario) {
        if (nomeUsuario == null || nomeUsuario.isBlank()) {
            throw new RuntimeException(
                    "Informe o nome do usuário que deseja excluir.");
        }

        try (ApiConnection con = ApiConnection.connect(host)) {
            con.login(usuario, senha);

            List<Map<String, String>> usuarios =
                    con.execute("/ip/hotspot/user/print");

            for (Map<String, String> usuario : usuarios) {
                if (nomeUsuario.equals(usuario.get("name"))) {
                    String id = usuario.get(".id");

                    if (id == null || id.isBlank()) {
                        throw new RuntimeException(
                                "Não foi possível identificar o usuário no MikroTik.");
                    }

                    con.execute(
                            "/ip/hotspot/user/remove numbers=" + id
                    );

                    return;
                }
            }

            throw new RuntimeException(
                    "Usuário não encontrado no MikroTik.");

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao excluir usuário no MikroTik: "
                            + e.getMessage(), e);
        }
    }

    public void bloquearUsuario(String nomeUsuario) {
        alterarBloqueioUsuario(nomeUsuario, true);
    }

    public void desbloquearUsuario(String nomeUsuario) {
        alterarBloqueioUsuario(nomeUsuario, false);
    }

    private void alterarBloqueioUsuario(
            String nomeUsuario, boolean bloquear) {

        if (nomeUsuario == null || nomeUsuario.isBlank()) {
            throw new RuntimeException(
                    "Informe o nome do usuário.");
        }

        try (ApiConnection con = ApiConnection.connect(host)) {
            con.login(usuario, senha);

            List<Map<String, String>> usuarios =
                    con.execute("/ip/hotspot/user/print");

            for (Map<String, String> usuario : usuarios) {
                if (nomeUsuario.equals(usuario.get("name"))) {
                    String id = usuario.get(".id");

                    if (id == null || id.isBlank()) {
                        throw new RuntimeException(
                                "Não foi possível identificar o usuário.");
                    }

                    String comando = bloquear
                            ? "/ip/hotspot/user/disable numbers=" + id
                            : "/ip/hotspot/user/enable numbers=" + id;

                    con.execute(comando);
                    return;
                }
            }

            throw new RuntimeException(
                    "Usuário não encontrado no MikroTik.");

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao " + (bloquear ? "bloquear" : "desbloquear")
                            + " usuário no MikroTik: "
                            + e.getMessage(), e);
        }
    }

    // Consulta os contadores de tráfego de um usuário conectado
    public Map<String, String> consultarTrafegoUsuario(
            String nomeUsuario) {

        if (nomeUsuario == null || nomeUsuario.isBlank()) {
            throw new RuntimeException(
                    "Informe o nome do usuário.");
        }

        try (ApiConnection con = ApiConnection.connect(host)) {
            con.login(usuario, senha);

            List<Map<String, String>> sessoes =
                    con.execute("/ip/hotspot/active/print");

            for (Map<String, String> sessao : sessoes) {
                if (nomeUsuario.equals(sessao.get("user"))) {

                    Map<String, String> dados = new HashMap<>();

                    dados.put("usuario", nomeUsuario);
                    dados.put(
                            "endereco",
                            sessao.getOrDefault("address", "")
                    );
                    dados.put(
                            "bytes-in",
                            sessao.getOrDefault("bytes-in", "0")
                    );
                    dados.put(
                            "bytes-out",
                            sessao.getOrDefault("bytes-out", "0")
                    );
                    dados.put("conectado", "true");

                    return dados;
                }
            }

            Map<String, String> dados = new HashMap<>();
            dados.put("usuario", nomeUsuario);
            dados.put("conectado", "false");
            dados.put("mensagem", "Usuário não está conectado.");

            return dados;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao consultar o tráfego do usuário: "
                            + e.getMessage(), e);
        }
    }
}
