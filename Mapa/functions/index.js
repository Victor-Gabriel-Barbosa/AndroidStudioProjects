/**
 * Importa as ferramentas da V2 do Cloud Functions
 */
const { onDocumentWritten, onDocumentCreated } = require("firebase-functions/v2/firestore");
const logger = require("firebase-functions/logger");
const admin = require("firebase-admin");

admin.initializeApp();

exports.cleanOrphanImgs = onDocumentWritten("locations/{locationId}", async (event) => {
    if (!event.data) return;

    // Pega os dados de antes e depois
    const oldData = event.data.before.data() || {};
    const newData = event.data.after.data() || {};

    // Garante que sejam arrays
    const oldUrls = oldData.imgUrls || [];
    const newUrls = newData.imgUrls || [];

    // Filtra o que foi removido
    const urlsToDelete = oldUrls.filter(url => !newUrls.includes(url));

    if (urlsToDelete.length === 0) {
        logger.info("Nenhuma imagem para limpar.");
        return;
    }

    logger.info(`Encontradas ${urlsToDelete.length} imagens para deletar.`);

    const bucket = admin.storage().bucket();
    const promises = [];

    urlsToDelete.forEach(url => {
        try {
            // Regex para extrair o caminho do arquivo
            const regex = /o\/(.*?)\?/;
            const match = url.match(regex);

            if (match && match[1]) {
                const filePath = decodeURIComponent(match[1]);
                logger.info(`Deletando: ${filePath}`);
                promises.push(bucket.file(filePath).delete());
            } else logger.warn(`Não foi possível extrair caminho da URL: ${url}`);
        } catch (error) {
            logger.error(`Erro ao processar URL ${url}:`, error);
        }
    });

    return Promise.all(promises);
});

exports.cleanMsgImgs = onDocumentWritten("chats/{id}/msgs/{msgId}", async (event) => {
    // Verificação de segurança padrão
    if (!event.data) return;

    // Pega os dados de antes e depois
    const oldData = event.data.before.data() || {};
    const newData = event.data.after.data() || {};

    // Garante que sejam arrays de URLs
    const oldUrls = oldData.imgUrls || [];
    const newUrls = newData.imgUrls || [];

    // Filtra: O que existia antes e NÃO existe mais agora?
    const urlsToDelete = oldUrls.filter(url => !newUrls.includes(url));

    if (urlsToDelete.length === 0) {
        logger.info("Nenhuma imagem para limpar.");
        return;
    }

    logger.info(`Limpando ${urlsToDelete.length} imagens da mensagem ${event.params.msgId}`);

    const bucket = admin.storage().bucket();
    const promises = [];

    urlsToDelete.forEach(url => {
        try {
            const regex = /o\/(.*?)\?/;
            const match = url.match(regex);

            if (match && match[1]) {
                const filePath = decodeURIComponent(match[1]);
                logger.info(`Deletando imagem de chat: ${filePath}`);
                promises.push(bucket.file(filePath).delete());
            }
        } catch (error) {
            logger.error(`Erro ao processar URL de chat ${url}:`, error);
        }
    });

    return Promise.all(promises);
});

exports.updateChatSummary = onDocumentWritten("chats/{id}/msgs/{msgId}", async (event) => {
    // Se o documento foi deletado, não faz nada
    if (!event.data) return;

    const id = event.params.id;
    const db = admin.firestore();

    // Referência para a coleção de chats e mensagens do chat
    const chatRef = db.collection("chats").doc(id);
    const msgRef = chatRef.collection("msgs");

    try {
      // Busca qual é a mensagem mais recente
      const chatSnap = await chatRef.get();
      if (!chatSnap.exists) {
        logger.info(`Chat ${id} não existe mais. Ignorando resumo.`);
        return;
      }

      const chatData = chatSnap.data() || {};

      // Se o chat estiver sendo deletado, não atualiza o resumo
      if (chatData.isDeleting === true) {
        logger.info(`Chat ${id} está sendo deletado. Ignorando resumo.`);
        return;
      }

      const snapshot = await msgRef.orderBy("timestamp", "desc").limit(1).get();

      if (snapshot.empty) {
        await chatRef.update({ lastMsg: null, lastTimestamp: 0 });
        return;
      }

      const doc = snapshot.docs[0];
      const lastMsg = { id: doc.id, ...doc.data() };

      // Atualiza o resumo do chat com as chaves do ChatDTO em inglês
      await chatRef.update({
        lastMsg: lastMsg,
        lastTimestamp: lastMsg.timestamp ?? 0,
      });
    } catch (error) {
      logger.error(`Erro ao atualizar resumo do chat ${id}:`, error);
    }
  }
);

exports.cleanHiddenChat = onDocumentWritten("chats/{id}", async (event) => {
    // Se o documento foi deletado, não faz nada
    if (!event.data) return;
    if (!event.data.after.exists) return;

    const oldData = event.data.before.data() || {};
    const newData = event.data.after.data() || {};

    // Evita rodar duas vezes (retrigger por causa do update isDeleting:true)
    if (newData.isDeleting === true) return;

    const visBefore = Array.isArray(oldData.visibleTo) ? oldData.visibleTo : [];
    const visAfter = Array.isArray(newData.visibleTo) ? newData.visibleTo : [];

    // Só executa quando o chat ficou oculto para todos (transição)
    if (!(visBefore.length > 0 && visAfter.length === 0)) return;

    const id = event.params.id;
    const db = admin.firestore();
    const chatRef = db.collection("chats").doc(id);
    const msgRef = chatRef.collection("msgs");

    try {
        logger.info(`Chat ${id} oculto para todos. Iniciando exclusão definitiva.`);

        // Flag pra impedir que updateChatSummary mexa nesse chat durante a limpeza
        await chatRef.update({ isDeleting: true });

        // Deleta todas as mensagens em lotes
        while (true) {
          const snapshot = await msgRef.limit(500).get();
          if (snapshot.empty) break;

          const batch = db.batch();
          snapshot.docs.forEach((d) => batch.delete(d.ref));
          await batch.commit();
        }

        // Deleta o doc do chat
        await chatRef.delete();

        logger.info(`Chat ${id} deletado permanentemente.`);
    } catch (error) {
        logger.error(`Erro ao deletar chat oculto ${id}:`, error);
    }
});

exports.notifyNewMsg = onDocumentCreated("chats/{id}/msgs/{msgId}", async (event) => {
    // Só prossegue se tiver dados reais
    if (!event.data) return;

    const newMsg = event.data.data();
    const id = event.params.id;
    const db = admin.firestore();

    try {
        // Busca os dados do chat para descobrir quem são os participantes
        const chatSnapshot = await db.collection("chats").doc(id).get();
        if (!chatSnapshot.exists) return;

        const participants = chatSnapshot.data().participants || [];

        // Isola o destinatário (quem NÃO é o autor da mensagem). 
        // Usando newMsg.uid conforme o MsgDTO
        const recipientUid = participants.find(uid => uid !== newMsg.uid);
        if (!recipientUid) {
            logger.info("Nenhum destinatário encontrado.");
            return;
        }

        // Busca os perfis para pegar o token do destinatário e o nome do remetente.
        // Se a coleção mudou para "users", altere aqui.
        const recipientSnapshot = await db.collection("users").doc(recipientUid).get();
        const senderSnapshot = await db.collection("users").doc(newMsg.uid).get();

        const fcmToken = recipientSnapshot.data()?.fcmToken;
        // Buscando 'name' conforme o UserDTO
        const senderName = senderSnapshot.data()?.name || "Novo usuário";

        // Se o destinatário não tiver token salvo, não tem como notificar
        if (!fcmToken) {
            logger.info(`Usuário ${recipientUid} não possui token FCM registrado.`);
            return;
        }

        // Formata o texto buscando por 'text' conforme o MsgDTO
        const notificationBody = newMsg.text ? newMsg.text : "📷 Nova imagem recebida";

        // Busca o locationId no documento do chat
        const locationId = chatSnapshot.data()?.locationId || "";

        // Pega a primeira imagem da mensagem, se existir, para usar como thumbnail
        const imgUrl = (Array.isArray(newMsg.imgUrls) && newMsg.imgUrls.length > 0) ? newMsg.imgUrls[0] : "";

        // Monta a carga útil (payload) da notificação com as chaves de data em inglês
        const payload = {
            token: fcmToken,
            data: {
                title: senderName,
                body: notificationBody,  
                contactUid: newMsg.uid,
                locationId: locationId,
                type: "new_message",
                imageUrl: imgUrl
            }
        };

        // Envia a notificação
        const response = await admin.messaging().send(payload);
        logger.info(`Notificação enviada com sucesso! MessageID: ${response}`);
    } catch (error) {
        logger.error(`Erro ao disparar notificação para o chat ${id}:`, error);
    }
});