
const parametros = new URLSearchParams(window.location.search);
const nomeUsuario = parametros.get("usuario");

const elementoNome = document.getElementById("nomeUsuario");
const elementoStatus = document.getElementById("statusUsuario");
const elementoIp = document.getElementById("ipUsuario");
const elementoTempo = document.getElementById("tempoUsuario");
const elementoAcesso = document.getElementById("acessoUsuario");
const mensagemAcoes = document.getElementById("mensagemAcoes");

const botaoBloquear = document.getElementById("botaoBloquear");
const textoBloquear = document.getElementById("textoBloquear");
const botaoVelocidade = document.getElementById("botaoVelocidade");

let usuarioBloqueado = false;

function normalizarNome(nome) {
    return String(nome ?? "").trim().toLowerCase();
}

function mostrarErro(mensagem) {
    elementoNome.textContent = nomeUsuario || "Usuário não identificado";
    elementoStatus.textContent = "Indisponível";
    elementoIp.textContent = "Não disponível";
    elementoTempo.textContent = "Não disponível";
    elementoAcesso.textContent = "Não confirmado";

    botaoBloquear.disabled = true;
    mensagemAcoes.textContent = mensagem;
}

async function consultarInformacoes() {
    if (!nomeUsuario) {
        mostrarErro("Não foi possível identificar o usuário selecionado.");
        return;
    }

    elementoNome.textContent = nomeUsuario;
    elementoStatus.textContent = "Consultando...";
    elementoIp.textContent = "Consultando...";
    elementoTempo.textContent = "Consultando...";
    elementoAcesso.textContent = "Consultando...";
    botaoBloquear.disabled = true;
    mensagemAcoes.textContent = "Carregando informações...";

    try {
        const [respostaCadastrados, respostaConectados] =
            await Promise.all([
                fetch("/api/hotspot/usuarios/cadastrados"),
                fetch("/api/hotspot/usuarios")
            ]);

        if (!respostaCadastrados.ok || !respostaConectados.ok) {
            throw new Error("Não foi possível consultar o MikroTik.");
        }

        const cadastrados = await respostaCadastrados.json();
        const conectados = await respostaConectados.json();

        if (!Array.isArray(cadastrados) ||
            !Array.isArray(conectados)) {
            throw new Error("A API retornou dados inválidos.");
        }

        const nomeProcurado = normalizarNome(nomeUsuario);

        const usuario = cadastrados.find(item =>
            normalizarNome(item.name ?? item.nome ?? item.usuario) ===
            nomeProcurado
        );

        const sessao = conectados.find(item =>
            normalizarNome(item.user ?? item.name ?? item.usuario) ===
            nomeProcurado
        );

        if (!usuario) {
            mostrarErro("Este usuário não foi encontrado no MikroTik.");
            return;
        }

        usuarioBloqueado =
            usuario.disabled === true ||
            String(usuario.disabled).toLowerCase() === "true";

        const conectado = Boolean(sessao) && !usuarioBloqueado;

        // Situação do cadastro e da conexão.
        elementoAcesso.textContent = usuarioBloqueado
            ? "Bloqueado"
            : "Ativado";

        elementoAcesso.style.color = usuarioBloqueado
            ? "#c0392b"
            : "#29965b";

        if (usuarioBloqueado) {
            elementoStatus.textContent = "Acesso bloqueado";
            elementoStatus.style.color = "#c0392b";
        } else if (conectado) {
            elementoStatus.textContent = "Conectado";
            elementoStatus.style.color = "#29965b";
        } else {
            elementoStatus.textContent = "Cadastrado, sem conexão";
            elementoStatus.style.color = "#d68910";
        }

        // Dados reais da sessão ativa.
        elementoIp.textContent = conectado
            ? (sessao.address || sessao.ip || "Não disponível")
            : "Sem conexão";

        elementoTempo.textContent = conectado
            ? (sessao.uptime || "Não disponível")
            : "Não conectado";

        // Atualiza a ação de acordo com o estado do usuário.
        textoBloquear.textContent = usuarioBloqueado
            ? "Desbloquear dispositivo"
            : "Bloquear dispositivo";

        botaoBloquear.disabled = false;
        mensagemAcoes.textContent = "";

    } catch (erro) {
        mostrarErro(
            "Não foi possível carregar as informações. Verifique a conexão com o MikroTik."
        );

        console.error("Erro ao consultar usuário:", erro);
    }
}

async function alterarBloqueio() {
    if (!nomeUsuario || botaoBloquear.disabled) {
        return;
    }

    const acao = usuarioBloqueado ? "desbloquear" : "bloquear";

    const confirmar = window.confirm(
        `Tem certeza de que deseja ${acao} o usuário "${nomeUsuario}"?`
    );

    if (!confirmar) {
        return;
    }

    botaoBloquear.disabled = true;
    mensagemAcoes.textContent = "Aplicando alteração...";

    try {
        const resposta = await fetch(
            `/api/hotspot/usuarios/${encodeURIComponent(nomeUsuario)}/${acao}`,
            {
                method: "POST"
            }
        );

        const mensagem = await resposta.text();

        if (!resposta.ok) {
            throw new Error(
                mensagem || "Não foi possível alterar o acesso."
            );
        }

        mensagemAcoes.textContent =
            mensagem || "Acesso atualizado com sucesso.";

        await consultarInformacoes();

    } catch (erro) {
        mensagemAcoes.textContent =
            erro.message || "Ocorreu um erro ao alterar o acesso.";

        console.error("Erro ao alterar o acesso:", erro);

        botaoBloquear.disabled = false;
    }
}

botaoBloquear.addEventListener("click", alterarBloqueio);

// Mantém o acesso à página de velocidade e tráfego.
botaoVelocidade.disabled = false;

consultarInformacoes();
