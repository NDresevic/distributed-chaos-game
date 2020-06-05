package app.lamport_mutex;

import app.AppConfig;
import app.models.ServentInfo;
import servent.message.BasicMessage;
import servent.message.Message;
import servent.message.lamport_mutex.ReleaseMessage;
import servent.message.lamport_mutex.RequestMessage;
import servent.message.util.MessageUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class LamportMutex {

    private final ServentInfo servent;
    private List<BasicMessage> requestList;
    private List<ServentInfo> pendingReplies;
    private boolean requestMade;

    public LamportMutex(ServentInfo servent) {
        this.servent = servent;
        requestList = Collections.synchronizedList(new ArrayList<>() {
            private static final long serialVersionUID = 3548358361360524760L;

            public synchronized boolean add(BasicMessage message) {
                boolean ret = super.add(message);
                Collections.sort(requestList);
                return ret;
            }
        });
        this.pendingReplies = new CopyOnWriteArrayList<>();
        this.requestMade = false;
    }

    public synchronized void addMessageRequest(Message message) {
        requestList.add((BasicMessage) message);
    }

    public synchronized void releaseMessageEvent(Message message) {
        String ipAddress = message.getSenderIpAddress();
        int port = message.getSenderPort();

        if (requestList.isEmpty()) {
            return;
        }

        if (requestList.get(0).getSenderPort() == port
                && requestList.get(0).getSenderIpAddress().equals(ipAddress)) { // the first message in the list should be the one to be released
            requestList.remove(0);
        }
//        else {
//            AppConfig.timestampedErrorPrint("Release message wasn't from the process first in the queue.");
//        }
    }

    public synchronized void releaseMyCriticalSection() { // called when the local node is done executing critical section
        AppConfig.timestampedStandardPrint("Releasing my critical section...");
        requestMade = false;
        requestList.remove(0);

        // broadcast release message to all servents
        for (Map.Entry<Integer, ServentInfo> entry: AppConfig.chordState.getAllNodeIdInfoMap().entrySet()) {
            int receiverId = entry.getKey();
            if (receiverId != AppConfig.myServentInfo.getId()) {
                ServentInfo intercessorServent = AppConfig.chordState.getNextNodeForServentId(receiverId);

                ReleaseMessage releaseMessage = new ReleaseMessage(AppConfig.myServentInfo.getFifoListenerPort(),
                        intercessorServent.getFifoListenerPort(), AppConfig.myServentInfo.getIpAddress(),
                        intercessorServent.getIpAddress(), AppConfig.lamportClock.getClock(), receiverId);
                MessageUtil.sendMessage(releaseMessage);
            }
        }
    }

    // on true, node can enter the critical section, on false node can not and then node blocks execution till it gets critical section
    private synchronized boolean requestCriticalSection() {
        if (!requestMade) {
            requestMade = true;

            for (Map.Entry<Integer, ServentInfo> entry: AppConfig.chordState.getAllNodeIdInfoMap().entrySet()) {
                if (entry.getKey() != AppConfig.myServentInfo.getId()) {
                    pendingReplies.add(entry.getValue());
                }
            }

            AppConfig.lamportClock.localEvent();
            for (ServentInfo receiverServent: pendingReplies) {     // send request message to all other servents
                int receiverId = receiverServent.getId();
                ServentInfo intercessorServent = AppConfig.chordState.getNextNodeForServentId(receiverId);

                RequestMessage requestMessage = new RequestMessage(AppConfig.myServentInfo.getFifoListenerPort(),
                        intercessorServent.getFifoListenerPort(), AppConfig.myServentInfo.getIpAddress(),
                        intercessorServent.getIpAddress(), AppConfig.lamportClock.getClock(), receiverId);
                MessageUtil.sendMessage(requestMessage);
            }

            // add to my request to request list
            RequestMessage myRequestMessage = new RequestMessage(AppConfig.myServentInfo.getFifoListenerPort(),
                    AppConfig.myServentInfo.getFifoListenerPort(), AppConfig.myServentInfo.getIpAddress(),
                    AppConfig.myServentInfo.getIpAddress(), AppConfig.lamportClock.getClock(),
                    AppConfig.myServentInfo.getId());
            this.addMessageRequest(myRequestMessage);
         }

        if (requestList.get(0).getSenderPort() == servent.getFifoListenerPort()
                && requestList.get(0).getSenderIpAddress().equals(servent.getIpAddress())) { // we are first, wait for all replies
            // execute critical section after you're done, call release my critical section
            return pendingReplies.isEmpty();
        }
        return false;
    }

    public synchronized void replyMessageEvent(Message message) {
        if (requestMade) {
            for (ServentInfo pendingServent: pendingReplies) {
                if (message.getSenderIpAddress().equals(pendingServent.getIpAddress())
                        && message.getSenderPort() == pendingServent.getFifoListenerPort()) {
                    pendingReplies.remove(pendingServent);
                    break;
                }
            }
            AppConfig.timestampedStandardPrint("Pending replies: " + pendingReplies);
        } else {
            AppConfig.timestampedErrorPrint("Got a reply message, but didn't request the critical section.");
        }
    }

    public void acquireLock() {
        AppConfig.timestampedStandardPrint("Waiting to acquire lock for my critical section...");
        while (!this.requestCriticalSection()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
