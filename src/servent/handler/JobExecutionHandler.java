package servent.handler;

import app.AppConfig;
import app.models.Job;
import app.models.JobExecution;
import app.models.Point;
import app.models.ServentInfo;
import servent.message.JobExecutionMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

import java.util.ArrayList;
import java.util.List;

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
        List<String> fractalIds = jobExecutionMessage.getFractalIds();
        List<Point> pointList = jobExecutionMessage.getStartPoints();
        AppConfig.timestampedStandardPrint("Fractal ids: " + fractalIds.toString());
        AppConfig.timestampedStandardPrint("Starting points: " + pointList.toString());
        AppConfig.chordState.setServentJobs(jobExecutionMessage.getServentJobsMap());

        Job job = jobExecutionMessage.getJob();
        // no further splitting, job execution can start
        if (fractalIds.size() == 1) {
            JobExecution jobExecution = new JobExecution(job.getName(), job.getProportion(), job.getWidth(),
                    job.getHeight(), pointList);
            AppConfig.chordState.getJobExecutionList().add(jobExecution);
            Thread t = new Thread(jobExecution);
            t.start();
            return;
        }

        // split + send to others
        int level = jobExecutionMessage.getLevel() + 1;
        int pointsCount = job.getPointsCount();
        double proportion = job.getProportion();

        for (int i = 0; i < pointsCount; i++) {
            List<Point> regionPoints = new ArrayList<>();

            Point startPoint = pointList.get(i);
            for (int j = 0; j < pointList.size(); j++) {
                if (i == j) {
                    regionPoints.add(startPoint);
                    continue;
                }

                Point other = pointList.get(j);
                int newX = (int) (startPoint.getX() + proportion * (other.getX() - startPoint.getX()));
                int newY = (int) (startPoint.getY() + proportion * (other.getY() - startPoint.getY()));
                Point newPoint = new Point(newX, newY);

                regionPoints.add(newPoint);
            }

            List<String> partialFractalIds = new ArrayList<>();
            for (String fractal: fractalIds) {
                if (fractal.charAt(level) - '0' == i) {
                    partialFractalIds.add(fractal);
                }
            }

            // todo: FIX THIS ASAP - send over successor table
            int executorId = AppConfig.chordState.getIdForFractalIDAndJob(partialFractalIds.get(0), job.getName());
            ServentInfo executorServent = AppConfig.chordState.getAllNodeIdInfoMap().get(executorId);
            // send to one node partialFractalIds, regionPoints and job
            JobExecutionMessage jem = new JobExecutionMessage(AppConfig.myServentInfo.getListenerPort(),
                    executorServent.getListenerPort(), AppConfig.myServentInfo.getIpAddress(), executorServent.getIpAddress(),
                    partialFractalIds, regionPoints, job, AppConfig.chordState.getServentJobs(), 0);
            MessageUtil.sendMessage(jem);
        }
    }
}
