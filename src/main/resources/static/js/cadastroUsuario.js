
const formulario = document.getElementById("formCadastro");
const mensagem = document.getElementById("mensagem");
const botao = document.getElementById("botaoCadastrar");

formulario.addEventListener("submit", async function (evento) {
    evento.preventDefault();

    const usuario = document.getElementById("usuario").value.trim();
    const confirmarUsuario = document.getElementById("confirmar-usuario").value.trim();
    const senha = document.getElementById("senha").value;
    const confirmarSenha = document.getElementById("confirmar-senha").value;

    // Verifica se os nomes de usuário são iguais
    if (usuario !== confirmarUsuario) {
        mensagem.textContent = "Os nomes de usuário não coincidem.";
        return;
    }

    // Verifica se as senhas são iguais
    if (senha !== confirmarSenha) {
        mensagem.textContent = "As senhas não coincidem.";
        return;
    }

    // Impede o envio de dados vazios
    if (!usuario || !senha) {
        mensagem.textContent = "Preencha todos os campos.";
        return;
    }

    mensagem.textContent = "";
    botao.disabled = true;
    botao.textContent = "Cadastrando...";

    try {
        const resposta = await fetch("/api/hotspot/usuarios/cadastrar", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                usuario: usuario,
                senha: senha
            })
        });

        const resultado = await resposta.text();

        if (!resposta.ok) {
            throw new Error(resultado || "Não foi possível cadastrar.");
        }

        mensagem.textContent = resultado || "Usuário cadastrado com sucesso!";
        formulario.reset();

    } catch (erro) {
        mensagem.textContent = "Erro no cadastro: " + erro.message;
    } finally {
        botao.disabled = false;
        botao.textContent = "Cadastrar";
    }
});
