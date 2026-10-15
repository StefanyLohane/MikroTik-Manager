const parametros = new URLSearchParams(window.location.search);
const nomeUsuario = parametros.get("usuario");

const elementoNome = document.getElementById("nomeUsuario");
const botaoConfirmar = document.getElementById("botaoConfirmar");
const mensagem = document.getElementById("mensagem");

if (!nomeUsuario) {
    elementoNome.textContent = "Usuário não identificado";

    mensagem.textContent =
        "Não foi possível identificar o usuário selecionado.";

    mensagem.style.color = "#c0392b";
    botaoConfirmar.disabled = true;
} else {
    elementoNome.textContent = nomeUsuario;
}

botaoConfirmar.addEventListener("click", async () => {
    if (!nomeUsuario || botaoConfirmar.disabled) {
        return;
    }

    const confirmar = window.confirm(
        "Tem certeza de que deseja excluir o usuário "
        + nomeUsuario + "?"
    );

    if (!confirmar) {
        return;
    }

    botaoConfirmar.disabled = true;
    mensagem.textContent = "Excluindo usuário...";
    mensagem.style.color = "#34495e";

    try {
        const resposta = await fetch(
            "/api/hotspot/usuarios/"
            + encodeURIComponent(nomeUsuario),
            {
                method: "DELETE"
            }
        );

        const resultado = await resposta.text();

        if (!resposta.ok) {
            throw new Error(
                resultado || "Não foi possível excluir o usuário."
            );
        }

        mensagem.textContent = "Usuário excluído com sucesso!";
        mensagem.style.color = "#29965b";

        botaoConfirmar.textContent = "Excluído";

        // Volta ao painel após um breve intervalo.
        setTimeout(() => {
            window.location.href = "painel.html";
        }, 1500);

    } catch (erro) {
        mensagem.textContent =
            erro.message || "Erro ao excluir o usuário.";

        mensagem.style.color = "#c0392b";
        botaoConfirmar.disabled = false;

        console.error("Erro ao excluir usuário:", erro);
    }
});