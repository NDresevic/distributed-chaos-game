package servent.handler.chaos_game;

import app.AppConfig;
import app.models.*;
import app.util.GeometryUtil;
import app.util.JobUtil;
import servent.handler.MessageHandler;
import servent.message.chaos_game.ComputedPointsMessage;
import servent.message.chaos_game.JobExecutionMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JobExecutionHandler implements MessageHandler {

    private Message clientMessage;

    public JobExecutionHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() != MessageType.JOB_EXECUTION) {
            AppConfig.timestampedErrorPrint("Job execution handler got a message that is not JOB_EXECUTION");
            return;
        }

        JobExecutionMessage jobExecutionMessage = (JobExecutionMessage) clientMessage;
        int receiverId = jobExecutionMessage.getFinalReceiverId();
        List<String> fractalIds = jobExecutionMessage.getFractalIds();
        List<Point> pointList = jobExecutionMessage.getStartPoints();
        Job job = jobExecutionMessage.getJob();
        int currentLevel = jobExecutionMessage.getLevel();
        Map<FractalIdJob, FractalIdJob> mappedFractalJobs = jobExecutionMessage.getMappedFractalsJobs();
        JobScheduleType scheduleType = jobExecutionMessage.getScheduleType();
        AppConfig.chordState.setServentJobs(jobExecutionMessage.getServentJobsMap());

        // if I am not intended final receiver then just pass message further
        if (receiverId != AppConfig.myServentInfo.getId()) {
            ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(receiverId);
            JobExecutionMessage jem = new JobExecutionMessage(AppConfig.myServentInfo.getListenerPort(),
                    nextServent.getListenerPort(), AppConfig.myServentInfo.getIpAddress(), nextServent.getIpAddress(),
                    fractalIds, pointList, job, AppConfig.chordState.getServentJobs(), currentLevel, receiverId,
                    mappedFractalJobs, scheduleType);
            MessageUtil.sendMessage(jem);
            return;
        }

        AppConfig.timestampedStandardPrint("Fractal ids: " + fractalIds.toString());
        AppConfig.timestampedStandardPrint("Starting points: " + pointList.toString());

        // no further splitting, job execution can start
        if (fractalIds.size() == 1) {

            if (AppConfig.chordState.getExecutionJob() != null) {   // I am already executing a job
                sendMyCurrentData(mappedFractalJobs, scheduleType);
            }

            String myNewFractalID = fractalIds.get(0);
            FractalIdJob myNewFractalJob = new FractalIdJob(myNewFractalID, job.getName());
            if (scheduleType.equals(JobScheduleType.JOB_ADDED) || scheduleType.equals(JobScheduleType.SERVENT_REMOVED)) {

                // compute how many data messages I need to receive from other servents
                for (Map.Entry<FractalIdJob, FractalIdJob> entry: mappedFractalJobs.entrySet()) {
                    if (entry.getValue().equals(myNewFractalJob)) {
                        AppConfig.chordState.getExpectedComputedPointsMessagesCount().getAndIncrement();
                    }
                }
            } else if (mappedFractalJobs.containsKey(myNewFractalJob)) {
                AppConfig.chordState.getExpectedComputedPointsMessagesCount().set(1);
            }

            // wait for others to send me data
            int expectedMessagesCount = AppConfig.chordState.getExpectedComputedPointsMessagesCount().get();
            AppConfig.timestampedStandardPrint("Waiting for " + expectedMessagesCount +
                    " servents to send me their computed points...");
            while (true) {
                if (AppConfig.chordState.getReceivedComputedPointsMessagesCount().get() == expectedMessagesCount) {
                    break;
                }
            }

            AppConfig.chordState.addNewJob(job);
            JobExecution jobExecution = new JobExecution(job.getName(), myNewFractalID, job.getProportion(),
                    job.getWidth(), job.getHeight(), pointList);

            List<Point> receivedComputedPoints = new ArrayList<>(AppConfig.chordState.getReceivedComputedPoints());
            if (scheduleType.equals(JobScheduleType.JOB_ADDED) || scheduleType.equals(JobScheduleType.SERVENT_REMOVED)) {
                jobExecution.getComputedPoints().addAll(receivedComputedPoints);
            } else {
                List<Point> insidePoints = GeometryUtil.getPointsInsidePolygon(pointList, receivedComputedPoints);
                jobExecution.getComputedPoints().addAll(insidePoints);
            }

            AppConfig.chordState.setExecutionJob(jobExecution);
            Thread t = new Thread(jobExecution);
            t.start();

            // reset received data for next time
            AppConfig.chordState.resetAfterReceivedComputedPoints();
            return;
        }

        // split + send to others
        int level = jobExecutionMessage.getLevel() + 1;
        int pointsCount = job.getPointsCount();
        double proportion = job.getProportion();

        for (int i = 0; i < pointsCount; i++) {
            List<Point> regionPoints = JobUtil.computeRegionPoints(pointList, i, proportion);

            List<String> partialFractalIds = new ArrayList<>();
            for (String fractal: fractalIds) {
                if (fractal.charAt(level) - '0' == i) {
                    partialFractalIds.add(fractal);
                }
            }

            // send to one node partialFractalIds, regionPoints and job
            int finalReceiverId = AppConfig.chordState.getIdForFractalIDAndJob(partialFractalIds.get(0), job.getName());
            ServentInfo receiverServent = AppConfig.chordState.getNextNodeForServentId(finalReceiverId);

            JobExecutionMessage jem = new JobExecutionMessage(AppConfig.myServentInfo.getListenerPort(),
                    receiverServent.getListenerPort(), AppConfig.myServentInfo.getIpAddress(), receiverServent.getIpAddress(),
                    partialFractalIds, regionPoints, job, AppConfig.chordState.getServentJobs(), level, finalReceiverId,
                    mappedFractalJobs, scheduleType);
            MessageUtil.sendMessage(jem);
        }
    }

    static void sendMyCurrentData(Map<FractalIdJob, FractalIdJob> mappedFractalJobs, JobScheduleType scheduleType) {
        JobExecution je = AppConfig.chordState.getExecutionJob();
        List<Point> myComputedPoints = new ArrayList<>(je.getComputedPoints());
        FractalIdJob myOldFractalJob = new FractalIdJob(je.getFractalId(), je.getJobName());

        if (mappedFractalJobs.containsKey(myOldFractalJob) &&
                (scheduleType.equals(JobScheduleType.JOB_ADDED) ||
                        scheduleType.equals(JobScheduleType.SERVENT_REMOVED))) {

            sendComputedPointsMessage(myComputedPoints, myOldFractalJob, mappedFractalJobs.get(myOldFractalJob));
        } else {
            for (Map.Entry<FractalIdJob, FractalIdJob> entry: mappedFractalJobs.entrySet()) {
                if (entry.getValue().equals(myOldFractalJob)) {
                    sendComputedPointsMessage(myComputedPoints, myOldFractalJob, entry.getKey());
                }
            }
        }

        je.stop();
    }

    private static void sendComputedPointsMessage(List<Point> computedPoints, FractalIdJob myOldFractalJob,
                                           FractalIdJob receiverFractalJob) {
        int receiverId = AppConfig.chordState.getIdForFractalIDAndJob(receiverFractalJob.getFractalId(),
                receiverFractalJob.getJobName());
        ServentInfo intercessor = AppConfig.chordState.getNextNodeForServentId(receiverId);

        ComputedPointsMessage cpm = new ComputedPointsMessage(AppConfig.myServentInfo.getListenerPort(),
                intercessor.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
                intercessor.getIpAddress(), myOldFractalJob.getJobName(), myOldFractalJob.getFractalId(),
                computedPoints, receiverId);
        MessageUtil.sendMessage(cpm);
    }
}
