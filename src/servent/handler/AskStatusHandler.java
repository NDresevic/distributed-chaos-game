package servent.handler;

import app.AppConfig;
import app.models.ServentInfo;
import servent.message.AskStatusMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TellStatusMessage;
import servent.message.util.MessageUtil;

import java.util.HashMap;
import java.util.Map;

public class AskStatusHandler implements MessageHandler {

    private Message clientMessage;

    public AskStatusHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() != MessageType.ASK_STATUS) {
            AppConfig.timestampedErrorPrint("Ask status handler got a message that is not ASK_STATUS");
            return;
        }

        AskStatusMessage askStatusMessage = (AskStatusMessage) clientMessage;
        int receiverId = askStatusMessage.getFinalReceiverId();
        String jobName = askStatusMessage.getJobName();
        String fractalId = askStatusMessage.getFractalId();
        int version = askStatusMessage.getVersion();

        String myFractalId = "";
        int myPointsCount = 0;
        if (AppConfig.chordState.getExecutionJob() != null) {
            myFractalId = AppConfig.chordState.getExecutionJob().getFractalId();
            myPointsCount = AppConfig.chordState.getExecutionJob().getComputedPointsCount();
        }

        // if I am not intended final receiver then just pass message further
        if (receiverId != AppConfig.myServentInfo.getId()) {
            ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(receiverId);

            AskStatusMessage asm = new AskStatusMessage(askStatusMessage.getSenderPort(), nextServent.getListenerPort(),
                    askStatusMessage.getSenderIpAddress(), nextServent.getIpAddress(), receiverId,
                    jobName, fractalId, askStatusMessage.getResultMap(), version);
            MessageUtil.sendMessage(asm);
            return;
        }

        if (version == 0) { // sending back results for job and fractalID
            Map<String, Map<String, Integer>> resultMap = new HashMap<>();
            String myJobName = AppConfig.chordState.getExecutionJob().getJobName();
            resultMap.put(myJobName, new HashMap<>());
            resultMap.get(jobName).put(myFractalId, myPointsCount);

            int finalReceiverId = AppConfig.chordState.getNodeIdForServentPortAndAddress(askStatusMessage.getSenderPort(), askStatusMessage.getSenderIpAddress());
            ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(finalReceiverId);
            TellStatusMessage tellStatusMessage = new TellStatusMessage(AppConfig.myServentInfo.getListenerPort(),
                    nextServent.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
                    nextServent.getIpAddress(), finalReceiverId, resultMap, 0);
            MessageUtil.sendMessage(tellStatusMessage);
        } else if (version == 1) {
            int lastServentId = AppConfig.chordState.getLastIdForJob(jobName);

            // add my info to result map
            Map<String, Map<String, Integer>> resultMap = askStatusMessage.getResultMap();
            resultMap.putIfAbsent(jobName, new HashMap<>());
            resultMap.get(jobName).put(myFractalId, myPointsCount);

            if (AppConfig.myServentInfo.getId() == lastServentId) { // if I am the last one, send tell status message
                int finalReceiverId = AppConfig.chordState.getNodeIdForServentPortAndAddress(askStatusMessage.getSenderPort(), askStatusMessage.getSenderIpAddress());
                ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(finalReceiverId);

                TellStatusMessage tellStatusMessage = new TellStatusMessage(AppConfig.myServentInfo.getListenerPort(),
                        nextServent.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
                        nextServent.getIpAddress(), finalReceiverId, resultMap, 1);
                MessageUtil.sendMessage(tellStatusMessage);
            } else { // pass message to first successor
                AskStatusMessage asm = new AskStatusMessage(askStatusMessage.getSenderPort(),
                        AppConfig.chordState.getNextNodePort(), askStatusMessage.getSenderIpAddress(),
                        AppConfig.chordState.getNextNodeIpAddress(), AppConfig.chordState.getFirstSuccessorId(),
                        jobName, resultMap, 1);
                MessageUtil.sendMessage(asm);
            }
        } else { // getting results for all jobs
            Map<String, Map<String, Integer>> resultMap = askStatusMessage.getResultMap();
            if (AppConfig.chordState.getExecutionJob() != null) {   // add my results if I am executing
                String myJobName = AppConfig.chordState.getExecutionJob().getJobName();
                resultMap.putIfAbsent(myJobName, new HashMap<>());
                resultMap.get(myJobName).put(myFractalId, myPointsCount);
            }

            if (AppConfig.myServentInfo.getListenerPort() == askStatusMessage.getSenderPort()
                    && AppConfig.myServentInfo.getIpAddress().equals(askStatusMessage.getSenderIpAddress())) {
                // if I send message it made circle, send result to myself

                TellStatusMessage tellStatusMessage = new TellStatusMessage(AppConfig.myServentInfo.getListenerPort(),
                        AppConfig.myServentInfo.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
                        AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getId(), resultMap, version);
                MessageUtil.sendMessage(tellStatusMessage);
            } else { // else pass it to first successor
                AskStatusMessage asm = new AskStatusMessage(askStatusMessage.getSenderPort(),
                        AppConfig.chordState.getNextNodePort(), askStatusMessage.getSenderIpAddress(),
                        AppConfig.chordState.getNextNodeIpAddress(), AppConfig.chordState.getFirstSuccessorId(),
                        resultMap, version);
                MessageUtil.sendMessage(asm);
            }
        }
    }
}
