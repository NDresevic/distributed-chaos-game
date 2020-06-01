package servent.handler;

import app.AppConfig;
import app.models.FractalIdJob;
import app.models.ServentInfo;
import servent.message.IdleMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

import java.util.Map;

public class IdleHandler implements MessageHandler {

    private Message clientMessage;

    public IdleHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() != MessageType.IDLE) {
            AppConfig.timestampedErrorPrint("Idle handler got a message that is not IDLE");
            return;
        }

        IdleMessage idleMessage = (IdleMessage) clientMessage;
        int receiverId = idleMessage.getFinalReceiverId();
        Map<Integer, FractalIdJob> serventJobsMap = idleMessage.getServentJobsMap();

        // if I am not intended final receiver then just pass message further
        if (receiverId != AppConfig.myServentInfo.getId()) {
            ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(receiverId);

            IdleMessage im = new IdleMessage(idleMessage.getSenderPort(), nextServent.getListenerPort(),
                    idleMessage.getSenderIpAddress(), nextServent.getIpAddress(), serventJobsMap, receiverId);
            MessageUtil.sendMessage(im);
            return;
        }

        AppConfig.chordState.setServentJobs(serventJobsMap);
        AppConfig.timestampedStandardPrint("I am idle...");
    }
}
