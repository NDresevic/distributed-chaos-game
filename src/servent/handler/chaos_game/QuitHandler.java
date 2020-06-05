package servent.handler.chaos_game;

import app.AppConfig;
import app.ServentMain;
import app.models.FractalIdJob;
import app.models.JobScheduleType;
import app.models.Point;
import app.models.ServentInfo;
import app.util.JobUtil;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.chaos_game.ComputedPointsMessage;
import servent.message.chaos_game.QuitMessage;
import servent.message.util.FifoSendWorker;
import servent.message.util.MessageUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuitHandler implements MessageHandler {

    private Message clientMessage;

    private QuitMessage quitMessage;
    private int quitterId;
    private int myId;
    private String quitterJobName;
    private String quitterFractalId;
    private List<Point> quitterComputedPoints;

    public QuitHandler(Message clientMessage) {
        this.clientMessage = clientMessage;

        quitMessage = (QuitMessage) clientMessage;
        quitterId = quitMessage.getQuitterId();
        myId = AppConfig.myServentInfo.getId();
        quitterJobName = quitMessage.getJobName();
        quitterFractalId = quitMessage.getFractalId();
        quitterComputedPoints = quitMessage.getQuitterComputedPoints();
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() != MessageType.QUIT) {
            AppConfig.timestampedErrorPrint("Quit handler got a message that is not QUIT");
            return;
        }

        if (quitterId == myId ||
                !AppConfig.chordState.getAllNodeIdInfoMap().containsKey(quitterId) ||
                AppConfig.chordState.getAllNodeIdInfoMap().size() == 1) { // message made a circle or I am the only one
            AppConfig.timestampedStandardPrint("Quit message made a circle.");

//            AppConfig.lamportMutex.releaseMyCriticalSection();

            if (AppConfig.chordState.getActiveJobsCount() > 0) {    // reschedule and send quitter data
                int serventCount = AppConfig.chordState.getAllNodeIdInfoMap().size();
                Map<FractalIdJob, FractalIdJob> mappedFractals = JobUtil.executeJobScheduling(serventCount, JobScheduleType.SERVENT_REMOVED);

                if (!quitterJobName.equals("") && !quitterFractalId.equals("")) {     // send quitter data
                    FractalIdJob hisFractalJob = mappedFractals.get(new FractalIdJob(quitterFractalId, quitterJobName));
                    int dataReceiver = AppConfig.chordState.getIdForFractalIDAndJob(hisFractalJob.getFractalId(), hisFractalJob.getJobName());
                    ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(dataReceiver);

                    ComputedPointsMessage computedPointsMessage = new ComputedPointsMessage(quitMessage.getSenderPort(),
                            nextServent.getListenerPort(), quitMessage.getSenderIpAddress(), nextServent.getIpAddress(),
                            quitterJobName, quitterFractalId, quitterComputedPoints, dataReceiver);
                    MessageUtil.sendMessage(computedPointsMessage);
                }
            }
            return;
        }

        this.setNewMapOfAllServents();
//        if (AppConfig.myServentInfo.getId() == quitterId ||
//                (!AppConfig.chordState.getAllNodeIdInfoMap().containsKey(quitterId) && AppConfig.myServentInfo.getId() == 0)) {
//            AppConfig.lamportMutex.acquireLock();
//        }
        this.setNewMapOfAllFifoWorkers();

        QuitMessage newQuitMessage;
        if (AppConfig.chordState.getAllNodeIdInfoMap().size() == 1) {   // send to myself cause I am the only one
            newQuitMessage = new QuitMessage(quitMessage.getSenderPort(), AppConfig.myServentInfo.getListenerPort(),
                    quitMessage.getSenderIpAddress(), AppConfig.myServentInfo.getIpAddress(), quitterId,
                    quitterJobName, quitterFractalId, quitterComputedPoints);
        } else {  // send to first successor
            newQuitMessage = new QuitMessage(quitMessage.getSenderPort(), AppConfig.chordState.getNextNodePort(),
                    quitMessage.getSenderIpAddress(), AppConfig.chordState.getNextNodeIpAddress(), quitterId,
                    quitterJobName, quitterFractalId, quitterComputedPoints);
        }
        MessageUtil.sendMessage(newQuitMessage);
    }

    private void setNewMapOfAllServents() {
        Map<Integer, ServentInfo> newNodesMap = new HashMap<>();
        AppConfig.chordState.getAllNodeIdInfoMap().remove(quitterId);
        for (Map.Entry<Integer, ServentInfo> entry: AppConfig.chordState.getAllNodeIdInfoMap().entrySet()) {
            if (entry.getKey() > quitterId) {
                ServentInfo serventInfo = entry.getValue();
                serventInfo.setId(entry.getKey() - 1);
                newNodesMap.put(entry.getKey() - 1, serventInfo);
            } else {
                newNodesMap.put(entry.getKey(), entry.getValue());
            }
        }
        if (myId > quitterId) {
            AppConfig.myServentInfo.setId(myId - 1);
        }
        AppConfig.chordState.getAllNodeIdInfoMap().clear();
        AppConfig.chordState.addNodes(newNodesMap);
    }

    private void setNewMapOfAllFifoWorkers() {
        Map<Integer, FifoSendWorker> newFifoWorkersMap = new HashMap<>();
        if (AppConfig.chordState.getFifoSendWorkerMap().containsKey(quitterId)) { // stop and remove quitter fifo worker
            FifoSendWorker quitterSendWorker = AppConfig.chordState.getFifoSendWorkerMap().get(quitterId);
            quitterSendWorker.stop();
            AppConfig.chordState.getFifoSendWorkerMap().remove(quitterId);
        }
        for (Map.Entry<Integer, FifoSendWorker> entry: AppConfig.chordState.getFifoSendWorkerMap().entrySet()) {
            if (entry.getKey() > quitterId) {
                FifoSendWorker fifoSendWorker = entry.getValue();
                fifoSendWorker.setServentId(entry.getKey() - 1);
                newFifoWorkersMap.put(entry.getKey() - 1, fifoSendWorker);
            } else {
                newFifoWorkersMap.put(entry.getKey(), entry.getValue());
            }
        }
        AppConfig.chordState.setFifoSendWorkerMapWorkers(newFifoWorkersMap);
    }
}
