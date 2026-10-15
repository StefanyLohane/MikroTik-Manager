
document.addEventListener("DOMContentLoaded", () => {
    carregarStatus();
    carregarResumoUsuarios();
    carregarUsuarios();
});

// Consulta a identidade/status do MikroTik.
async function carregarStatus() {
    const elemento = document.getElementById("status-mikrotik");

    if (!elemento) {
        console.error('Elemento "status-mikrotik" não encontrado.');
        return;
    }

    try {
        const resposta = await fetch("/api/hotspot/status");

        if (!resposta.ok) {
            throw new Error("Erro HTTP " + resposta.status);
        }

        const texto = await resposta.text();
        const nome = texto.match(/Nome do MikroTik:\s*(.+)/i);

        if (nome) {
            elemento.textContent = nome[1].trim();
        } else if (texto.trim()) {
            elemento.textContent = texto.trim();
        } else {
            elemento.textContent = "Resposta vazia";
        }
    } catch (erro) {
        console.error("Erro ao carregar status:", erro);
        elemento.textContent = "Indisponível";
    }
}

// Atualiza a quantidade de usuários cadastrados.
async function carregarResumoUsuarios() {
    const elemento = document.getElementById("total-usuarios");

    if (!elemento) return;

    try {
        const resposta = await fetch(
            "/api/hotspot/usuarios/cadastrados"
        );

        if (!resposta.ok) {
            throw new Error("Erro HTTP " + resposta.status);
        }

        const usuarios = await resposta.json();

        if (!Array.isArray(usuarios)) {
            throw new Error("A API não retornou uma lista de usuários.");
        }

        const usuariosCadastrados = usuarios.filter(usuario =>
            usuario.dynamic !== "true" &&
            usuario.dynamic !== true
        );

        elemento.textContent = usuariosCadastrados.length;
    } catch (erro) {
        console.error("Erro ao carregar usuários cadastrados:", erro);
        elemento.textContent = "Indisponível";
    }
}

// Carrega usuários cadastrados e relaciona seus IPs ativos.
async function carregarUsuarios() {
    const tabela = document.getElementById("tabela-usuarios");

    if (!tabela) {
        console.error('Elemento "tabela-usuarios" não encontrado.');
        return;
    }

    // Remove somente as linhas de dados, preservando o cabeçalho.
    tabela.querySelectorAll(".linha:not(.tabela-cabecalho)")
        .forEach(linha => linha.remove());

    try {
        // Consulta os usuários cadastrados e os conectados.
        const [respostaCadastrados, respostaConectados] =
            await Promise.all([
                fetch("/api/hotspot/usuarios/cadastrados"),
                fetch("/api/hotspot/usuarios")
            ]);

        if (!respostaCadastrados.ok) {
            throw new Error(
                "Erro ao consultar cadastrados: HTTP " +
                respostaCadastrados.status
            );
        }

        if (!respostaConectados.ok) {
            throw new Error(
                "Erro ao consultar conectados: HTTP " +
                respostaConectados.status
            );
        }

        const cadastrados = await respostaCadastrados.json();
        const conectados = await respostaConectados.json();

        if (!Array.isArray(cadastrados) ||
            !Array.isArray(conectados)) {
            throw new Error("A API não retornou listas válidas.");
        }

        // Cria um mapa: nome do usuário -> endereço IP.
        const ipsPorUsuario = new Map();

        conectados.forEach(usuario => {
            const nome = (
                usuario.user ??
                usuario.name ??
                usuario.usuario ??
                ""
            ).trim().toLowerCase();

            const ip = (
                usuario.address ??
                usuario.ip ??
                ""
            ).trim();

            if (nome && ip) {
                ipsPorUsuario.set(nome, ip);
            }
        });

        // Não exibe contas dinâmicas na tabela.
        const usuariosCadastrados = cadastrados.filter(usuario =>
            usuario.dynamic !== "true" &&
            usuario.dynamic !== true
        );

        if (usuariosCadastrados.length === 0) {
            adicionarMensagem(tabela, "Nenhum usuário cadastrado.");
            return;
        }

        usuariosCadastrados.forEach(usuario => {
            const nome = (
                usuario.name ??
                usuario.nome ??
                usuario.usuario ??
                ""
            ).trim();

            const ip = ipsPorUsuario.get(nome.toLowerCase()) || "—";

            const bloqueado =
                usuario.disabled === true ||
                usuario.disabled === "true";

            const status = bloqueado
                ? "Bloqueado"
                : ip !== "—"
                    ? "Conectado"
                    : "Cadastrado";

            const linha = document.createElement("div");
            linha.classList.add("linha");

            const colunaNome = document.createElement("span");
            colunaNome.textContent = nome;

            const colunaIP = document.createElement("span");
            colunaIP.textContent = ip;

            const colunaStatus = document.createElement("span");
            colunaStatus.textContent = status;

            const colunaAcoes = document.createElement("span");
            const acoes = document.createElement("div");

            acoes.style.display = "flex";
            acoes.style.alignItems = "center";
            acoes.style.justifyContent = "flex-end";
            acoes.style.gap = "8px";

            const nomeURL = encodeURIComponent(nome);

            // Botão editar.
            const botaoEditar = criarBotao(
                "botao-editar",
                "Editar usuário",
                `<path d="M12 20h9"></path>
                 <path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L8 18l-4 1 1-4Z"></path>`
            );

            botaoEditar.addEventListener("click", () => {
                window.location.href =
                    `editarUsuario.html?usuario=${nomeURL}`;
            });

            // Botão informações.
            const botaoInfo = criarBotao(
                "botao-info",
                "Informações do usuário",
                `<circle cx="12" cy="12" r="10"></circle>
                 <path d="M12 16v-4"></path>
                 <path d="M12 8h.01"></path>`
            );

            botaoInfo.addEventListener("click", () => {
                window.location.href =
                    `informacoesUsuario.html?usuario=${nomeURL}`;
            });

            // Botão excluir.
            const botaoExcluir = criarBotao(
                "botao-excluir",
                "Excluir usuário",
                `<polyline points="3 6 5 6 21 6"></polyline>
                 <path d="M19 6l-1 14H6L5 6"></path>
                 <path d="M10 11v6"></path>
                 <path d="M14 11v6"></path>
                 <path d="M9 6V4h6v2"></path>`
            );

            botaoExcluir.addEventListener("click", () => {
                window.location.href =
                    `confirmarExclusao.html?usuario=${nomeURL}`;
            });

            // Mantém a ordem dos botões.
            acoes.append(botaoEditar, botaoInfo, botaoExcluir);
            colunaAcoes.appendChild(acoes);

            linha.append(
                colunaNome,
                colunaIP,
                colunaStatus,
                colunaAcoes
            );

            tabela.appendChild(linha);
        });

    } catch (erro) {
        console.error("Erro ao carregar usuários:", erro);

        adicionarMensagem(
            tabela,
            "Não foi possível carregar os usuários."
        );
    }
}

// Exibe mensagens sem apagar o cabeçalho.
function adicionarMensagem(tabela, texto) {
    const linha = document.createElement("div");
    linha.classList.add("linha", "mensagem-tabela");

    const mensagem = document.createElement("span");
    mensagem.textContent = texto;
    mensagem.style.gridColumn = "1 / -1";

    linha.appendChild(mensagem);
    tabela.appendChild(linha);
}

// Cria os botões com seus respectivos ícones.
function criarBotao(classe, titulo, desenho) {
    const botao = document.createElement("button");

    botao.type = "button";
    botao.classList.add(classe);
    botao.title = titulo;
    botao.setAttribute("aria-label", titulo);

    botao.innerHTML = `
        <svg xmlns="http://www.w3.org/2000/svg"
            width="20" height="20" viewBox="0 0 24 24"
            fill="none" stroke="currentColor"
            stroke-width="2" stroke-linecap="round"
            stroke-linejoin="round">
            ${desenho}
        </svg>
    `;

    return botao;
}
