package servent.handler;

import servent.message.Message;

public class JobResultHandler implements MessageHandler {

    private Message clientMessage;

    public JobResultHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {

    }
}
