package servent.handler.lamport_mutex;

import app.AppConfig;
import app.models.ServentInfo;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.lamport_mutex.ReleaseCriticalSectionMessage;
import servent.message.util.MessageUtil;

public class ReleaseCriticalSectionHandler implements MessageHandler {

    private Message clientMessage;
    private ReleaseCriticalSectionMessage releaseCriticalSectionMessage;

    private int receiverId;

    public ReleaseCriticalSectionHandler(Message clientMessage) {
        this.clientMessage = clientMessage;

        releaseCriticalSectionMessage = (ReleaseCriticalSectionMessage) clientMessage;
        receiverId = releaseCriticalSectionMessage.getFinalReceiverId();
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() != MessageType.RELEASE_CRITICAL_SECTION) {
            AppConfig.timestampedErrorPrint("Release critical section handler got a message that is not RELEASE_CRITICAL_SECTION");
            return;
        }

        if (receiverId != AppConfig.myServentInfo.getId()) {    // I am not intended receiver, forward message
            this.forwardMessage();
            return;
        }

        AppConfig.lamportMutex.releaseMyCriticalSection();
    }

    private void forwardMessage() {
        ServentInfo intercessorServent = AppConfig.chordState.getNextNodeForServentId(receiverId);

        ReleaseCriticalSectionMessage rcsm = new ReleaseCriticalSectionMessage(releaseCriticalSectionMessage.getSenderPort(),
                intercessorServent.getListenerPort(), releaseCriticalSectionMessage.getSenderIpAddress(),
                intercessorServent.getIpAddress(), receiverId);
        MessageUtil.sendMessage(rcsm);
    }
}
