// Elementos da velocidade geral da conexão
const elementoStatus = document.getElementById("statusConexao");
const elementoDownload = document.getElementById("velocidadeDownload");
const elementoUpload = document.getElementById("velocidadeUpload");

// Elementos do tráfego geral da rede
const elementoEntrada = document.getElementById("trafegoEntrada");
const elementoSaida = document.getElementById("trafegoSaida");

// Converte bytes em unidades fáceis de ler
function formatarBytes(bytes) {
    const valor = Number(bytes);

    if (!Number.isFinite(valor) || valor < 0) {
        return "Indisponível";
    }

    if (valor < 1024) {
        return valor + " B";
    }

    if (valor < 1024 * 1024) {
        return (valor / 1024).toFixed(2) + " KB";
    }

    if (valor < 1024 * 1024 * 1024) {
        return (valor / (1024 * 1024)).toFixed(2) + " MB";
    }

    return (valor / (1024 * 1024 * 1024)).toFixed(2) + " GB";
}

// Converte bits por segundo em kbps ou Mbps
function formatarVelocidade(bitsPorSegundo) {
    const valor = Number(bitsPorSegundo);

    if (!Number.isFinite(valor) || valor < 0) {
        return "Indisponível";
    }

    if (valor >= 1000000) {
        return (valor / 1000000).toFixed(2) + " Mbps";
    }

    return (valor / 1000).toFixed(2) + " kbps";
}

// Consulta a velocidade e o tráfego geral da interface ether2
async function atualizarTrafego() {
    try {
        const resposta = await fetch("/api/hotspot/trafego");

        if (!resposta.ok) {
            throw new Error("Falha ao consultar o tráfego.");
        }

        const dados = await resposta.json();

        if (dados.erro) {
            throw new Error(dados.erro);
        }

        elementoDownload.textContent =
            formatarVelocidade(dados["rx-bits-per-second"]);

        elementoUpload.textContent =
            formatarVelocidade(dados["tx-bits-per-second"]);

        elementoEntrada.textContent =
            formatarBytes(dados["rx-byte"]);

        elementoSaida.textContent =
            formatarBytes(dados["tx-byte"]);

        elementoStatus.textContent =
            "Monitorando interface " + dados.interface;

    } catch (erro) {
        elementoStatus.textContent = "Falha na consulta";
        console.error("Erro ao consultar tráfego da rede:", erro);
    }
}

// Inicialização
atualizarTrafego();

// Atualiza os dados da rede a cada 2 segundos
setInterval(atualizarTrafego, 2000);