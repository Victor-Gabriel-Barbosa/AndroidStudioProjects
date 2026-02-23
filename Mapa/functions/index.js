/**
 * Importa as ferramentas da V2 do Cloud Functions
 */
const { onDocumentWritten, onDocumentCreated } = require("firebase-functions/v2/firestore");
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
    // Se o documento foi deletado, não faz nada
    if (!event.data) return;

    const salaId = event.params.salaId;
    const db = admin.firestore();

    // Referência para a coleção de chats e mensagens do chat
    const chatRef = db.collection("chats").doc(salaId);
    const mensagensRef = chatRef.collection("mensagens");

    try {
      // Busca qual é a mensagem mais recente
      const chatSnap = await chatRef.get();
      if (!chatSnap.exists) {
        logger.info(`Chat ${salaId} não existe mais. Ignorando resumo.`);
        return;
      }

      const chatData = chatSnap.data() || {};

      // Se o chat estiver sendo deletado, não atualiza o resumo
      if (chatData.deletando === true) {
        logger.info(`Chat ${salaId} está sendo deletado. Ignorando resumo.`);
        return;
      }

      const snapshot = await mensagensRef.orderBy("timestamp", "desc").limit(1).get();

      if (snapshot.empty) {
        await chatRef.update({ ultimaMsg: null, ultimoTimestamp: 0 });
        return;
      }

      const doc = snapshot.docs[0];
      const ultimaMsg = { id: doc.id, ...doc.data() };

      // Atualiza o resumo do chat
      await chatRef.update({
        ultimaMsg,
        ultimoTimestamp: ultimaMsg.timestamp ?? 0,
      });
    } catch (erro) {
      logger.error(`Erro ao atualizar resumo do chat ${salaId}:`, erro);
    }
  }
);

exports.limparChatOculto = onDocumentWritten("chats/{salaId}", async (event) => {
    // Se o documento foi deletado, não faz nada
    if (!event.data) return;
    if (!event.data.after.exists) return;

    const dadosAntigos = event.data.before.data() || {};
    const dadosNovos = event.data.after.data() || {};

    // Evita rodar duas vezes (retrigger por causa do update deletando:true)
    if (dadosNovos.deletando === true) return;

    const visAntes = Array.isArray(dadosAntigos.visivelPara) ? dadosAntigos.visivelPara : [];
    const visDepois = Array.isArray(dadosNovos.visivelPara) ? dadosNovos.visivelPara : [];

    // Só executa quando o chat ficou oculto para todos (transição)
    if (!(visAntes.length > 0 && visDepois.length === 0)) return;

    const salaId = event.params.salaId;
    const db = admin.firestore();
    const chatRef = db.collection("chats").doc(salaId);
    const mensagensRef = chatRef.collection("mensagens");

    try {
        logger.info(`Chat ${salaId} oculto para todos. Iniciando exclusão definitiva.`);

        // Flag pra impedir que atualizarResumoChat mexa nesse chat durante a limpeza
        await chatRef.update({ deletando: true });

        // Deleta todas as mensagens em lotes
        while (true) {
          const snapshot = await mensagensRef.limit(500).get();
          if (snapshot.empty) break;

          const batch = db.batch();
          snapshot.docs.forEach((d) => batch.delete(d.ref));
          await batch.commit();
        }

        // Deleta o doc do chat
        await chatRef.delete();

        logger.info(`Chat ${salaId} deletado permanentemente.`);
    } catch (erro) {
        logger.error(`Erro ao deletar chat oculto ${salaId}:`, erro);
    }
});

exports.notificarNovaMensagem = onDocumentCreated("chats/{salaId}/mensagens/{mensagemId}", async (event) => {
    // Só prossegue se tiver dados reais
    if (!event.data) return;

    const msgNova = event.data.data();
    const salaId = event.params.salaId;
    const db = admin.firestore();

    try {
        // Busca os dados do chat para descobrir quem são os participantes
        const chatSnapshot = await db.collection("chats").doc(salaId).get();
        if (!chatSnapshot.exists) return;

        const participantes = chatSnapshot.data().participantes || [];

        // Isola o destinatário (quem NÃO é o autor da mensagem)
        const destinatarioUid = participantes.find(uid => uid !== msgNova.autorUid);
        if (!destinatarioUid) {
            logger.info("Nenhum destinatário encontrado.");
            return;
        }

        // Busca os perfis para pegar o token do destinatário e o nome do remetente
        const destinatarioSnapshot = await db.collection("usuarios").doc(destinatarioUid).get();
        const remetenteSnapshot = await db.collection("usuarios").doc(msgNova.autorUid).get();

        const tokenFcm = destinatarioSnapshot.data()?.fcmToken;
        const nomeRemetente = remetenteSnapshot.data()?.nome || "Novo usuário";

        // Se o destinatário não tiver token salvo, não tem como notificar
        if (!tokenFcm) {
            logger.info(`Usuário ${destinatarioUid} não possui token FCM registrado.`);
            return;
        }

        // Formata o texto (tratar caso seja só envio de imagem)
        const corpoNotificacao = msgNova.texto ? msgNova.texto : "📷 Nova imagem recebida";

        // Busca o localId no documento do chat
        const localId = chatSnapshot.data()?.localId || "";

        // Monta a carga útil (payload) da notificação
        const payload = {
            token: tokenFcm,
            notification: {
                title: nomeRemetente,
                body: corpoNotificacao
            },
            data: {
                contatoUid: msgNova.autorUid,
                localId: localId,
                tipo: "nova_mensagem"
            }
        };

        // Envia a notificação
        const response = await admin.messaging().send(payload);
        logger.info(`Notificação enviada com sucesso! MessageID: ${response}`);
    } catch (erro) {
        logger.error(`Erro ao disparar notificação para o chat ${salaId}:`, erro);
    }
});