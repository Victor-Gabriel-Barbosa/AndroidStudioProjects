/**
 * Importa as ferramentas da V2 do Cloud Functions
 */
const { onDocumentWritten } = require("firebase-functions/v2/firestore");
const logger = require("firebase-functions/logger");
const admin = require("firebase-admin");

admin.initializeApp();

exports.limparImagensOrfans = onDocumentWritten("locais/{localId}", async (event) => {
    if (!event.data) return;

    // Pega os dados de antes e depois
    const dadosAntigos = event.data.before.data() || {};
    const dadosNovos = event.data.after.data() || {};

    // Garante que sejam arrays
    const urlsAntigas = dadosAntigos.imgUrls || [];
    const urlsNovas = dadosNovos.imgUrls || [];

    // Filtra o que foi removido
    const urlsParaDeletar = urlsAntigas.filter(url => !urlsNovas.includes(url));

    if (urlsParaDeletar.length === 0) {
        logger.info("Nenhuma imagem para limpar.");
        return;
    }

    logger.info(`Encontradas ${urlsParaDeletar.length} imagens para deletar.`);

    const bucket = admin.storage().bucket();
    const promessas = [];

    urlsParaDeletar.forEach(url => {
        try {
            // Regex para extrair o caminho do arquivo
            const regex = /o\/(.*?)\?/;
            const match = url.match(regex);

            if (match && match[1]) {
                const caminhoArquivo = decodeURIComponent(match[1]);
                logger.info(`Deletando: ${caminhoArquivo}`);
                promessas.push(bucket.file(caminhoArquivo).delete());
            } else logger.warn(`Não foi possível extrair caminho da URL: ${url}`);
        } catch (erro) {
            logger.error(`Erro ao processar URL ${url}:`, erro);
        }
    });

    return Promise.all(promessas);
});

exports.limparImagensMensagens = onDocumentWritten("chats/{salaId}/mensagens/{mensagemId}", async (event) => {
    // Verificação de segurança padrão
    if (!event.data) return;

    // Pega os dados de antes e depois
    const dadosAntigos = event.data.before.data() || {};
    const dadosNovos = event.data.after.data() || {};

    // Garante que sejam arrays de URLs
    const urlsAntigas = dadosAntigos.imgUrls || [];
    const urlsNovas = dadosNovos.imgUrls || [];

    // Filtra: O que existia antes e NÃO existe mais agora?
    const urlsParaDeletar = urlsAntigas.filter(url => !urlsNovas.includes(url));

    if (urlsParaDeletar.length === 0) {
        logger.info("Nenhuma imagem para limpar.");
        return;
    }

    logger.info(`Limpando ${urlsParaDeletar.length} imagens da mensagem ${event.params.mensagemId}`);

    const bucket = admin.storage().bucket();
    const promessas = [];

    urlsParaDeletar.forEach(url => {
        try {
            const regex = /o\/(.*?)\?/;
            const match = url.match(regex);

            if (match && match[1]) {
                const caminhoArquivo = decodeURIComponent(match[1]);
                logger.info(`Deletando imagem de chat: ${caminhoArquivo}`);
                promessas.push(bucket.file(caminhoArquivo).delete());
            }
        } catch (erro) {
            logger.error(`Erro ao processar URL de chat ${url}:`, erro);
        }
    });

    return Promise.all(promessas);
});

exports.atualizarResumoChat = onDocumentWritten("chats/{salaId}/mensagens/{mensagemId}", async (event) => {
    const salaId = event.params.salaId;
    const db = admin.firestore();

    // Referência para a coleção de mensagens do chat específico
    const mensagensRef = db.collection("chats").doc(salaId).collection("mensagens");

    try {
        // Busca qual é a mensagem mais recente
        const snapshot = await mensagensRef
            .orderBy("timestamp", "desc")
            .limit(1)
            .get();

        let ultimaMsg = null;
        let ultimoTimestamp = 0;

        if (!snapshot.empty) {
            const doc = snapshot.docs[0];
            ultimaMsg = doc.data();
            ultimaMsg.id = doc.id;
            ultimoTimestamp = ultimaMsg.timestamp;
        }

        // Atualiza o documento pai (o Chat) com a nova "ultimaMsg" real
        await db.collection("chats").doc(salaId).set({
            ultimaMsg: ultimaMsg,
            ultimoTimestamp: ultimoTimestamp
        }, { merge: true });

        logger.info(`Resumo do chat ${salaId} atualizado com sucesso.`);

    } catch (erro) {
        logger.error(`Erro ao atualizar resumo do chat ${salaId}:`, erro);
    }
});

exports.limparChatOculto = onDocumentWritten("chats/{salaId}", async (event) => {
    // Se o documento foi deletado, não faz nada
    if (!event.data.after.exists) return;

    const dadosAntigos = event.data.before.data() || {};
    const dadosNovos = event.data.after.data() || {};


    if (participantes.length > 0) return;

    const visivelPara = dadosNovos.visivelPara;

    // Se ainda houver participantes, encerra
    if (!visivelPara || visivelPara.length > 0) return;

    const salaId = event.params.salaId;
    logger.info(`Chat ${salaId} oculto para todos. Iniciando exclusão definitiva.`);

    const db = admin.firestore();
    const chatRef = db.collection("chats").doc(salaId);
    const mensagensRef = chatRef.collection("mensagens");

    try {
        // Deleta subcoleção de mensagens (em batches)
        const snapshot = await mensagensRef.limit(500).get();

        if (!snapshot.empty) {
            const batch = db.batch();
            snapshot.docs.forEach((doc) => {
                batch.delete(doc.ref);
            });
            await batch.commit();

            logger.info(`Mensagens do chat ${salaId} deletadas.`);
        }

        // Deleta o documento do Chat
        await chatRef.delete();
        logger.info(`Documento do chat ${salaId} deletado permanentemente.`);
    } catch (erro) {
        logger.error(`Erro ao deletar chat oculto ${salaId}:`, erro);
    }
});