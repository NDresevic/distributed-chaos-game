package cli.command;

import app.AppConfig;
import app.models.FractalIdJob;
import app.models.Job;
import app.models.Point;
import app.models.ServentInfo;
import servent.message.JobExecutionMessage;
import servent.message.util.MessageUtil;

import java.util.*;

public class StartCommand implements CLICommand {

    @Override
    public String commandName() {
        return "start";
    }

    @Override
    public void execute(String args) {
        if (args == null) {
            // todo: deo sa rucnim ucitavanjem posla
            // Ako se X izostavi, pitati korisnika da unese parametre za posao na konzoli. Proveriti da je ime posla
            // jedinstveno, kao i da su svi parametri validnih tipova.
        }

        int serventCount = AppConfig.chordState.getAllNodeIdInfoMap().size();
        if (serventCount < 1) {
            AppConfig.timestampedStandardPrint("There are no active servents to execute the job.");
            return;
        }
        int activeJobs = AppConfig.chordState.getActiveJobsCount();
        if (serventCount < activeJobs + 1) {
            AppConfig.timestampedStandardPrint("There is not enough active nodes to execute new job.");
            return;
        }

        Job job = null;
        for (Job j: AppConfig.myServentInfo.getJobs()) {
            if (j.getName().equals(args)) {
                job = j;
                break;
            }
        }
        if (job == null) {
            AppConfig.timestampedErrorPrint("Job with name \'" + args + "\" not found in the list of jobs.");
            return;
        }

        AppConfig.timestampedStandardPrint("Splitting work for \"" + job.getName() + "\"...");

        // compute number of servents needed to execute the job
        int jobNodesCount = computeJobNodesCount(serventCount, job.getPointsCount());
        AppConfig.timestampedStandardPrint("Number of nodes for job \"" + job.getName() + "\": " + jobNodesCount);

        // compute fractal ids
        List<String> fractalIds = computeFractalIds(jobNodesCount, job.getPointsCount());
        AppConfig.timestampedStandardPrint("Fractal IDs for job \"" + job.getName() + "\": " + fractalIds.toString());

        // update map that has ids, fractalIds and jobs
        List<FractalIdJob> fractalIdJobList = new ArrayList<>();
        for (String fractalId: fractalIds) {
            fractalIdJobList.add(new FractalIdJob(fractalId, job.getName()));
        }
        Map<Integer, FractalIdJob> serventJobs = new HashMap<>();
        for (Map.Entry<Integer, ServentInfo> entry: AppConfig.chordState.getAllNodeIdInfoMap().entrySet()) {
            // todo: videti sa ovim idle kako i sta
            if (entry.getValue().isIdle()) {
                serventJobs.put(entry.getKey(), fractalIdJobList.remove(0));
                if (fractalIdJobList.isEmpty()) {
                    break;
                }
            }
        }
        AppConfig.chordState.setServentJobs(serventJobs);
        AppConfig.timestampedStandardPrint(serventJobs.toString());

        // compute initial job division and send messages
        List<Point> jobPoints = job.getPoints();
        double proportion = job.getProportion();
        int pointsCount = job.getPointsCount();

        if (jobNodesCount < pointsCount) {
            // only one node is executing the job
            int executorId = AppConfig.chordState.getIdForFractalIDAndJob(fractalIds.get(0), job.getName());
            ServentInfo executorServent = AppConfig.chordState.getAllNodeIdInfoMap().get(executorId);

            JobExecutionMessage jobExecutionMessage = new JobExecutionMessage(AppConfig.myServentInfo.getListenerPort(),
                    executorServent.getListenerPort(), AppConfig.myServentInfo.getIpAddress(), executorServent.getIpAddress(),
                    fractalIds, jobPoints, job, serventJobs, 0);
            MessageUtil.sendMessage(jobExecutionMessage);
            return;
        }

        // split work and send to servents
        for (int i = 0; i < jobPoints.size(); i++) {
            List<Point> regionPoints = new ArrayList<>();

            Point startPoint = jobPoints.get(i);
            for (int j = 0; j < jobPoints.size(); j++) {
                if (i == j) {
                    regionPoints.add(startPoint);
                    continue;
                }

                Point other = jobPoints.get(j);
                int newX = (int) (startPoint.getX() + proportion * (other.getX() - startPoint.getX()));
                int newY = (int) (startPoint.getY() + proportion * (other.getY() - startPoint.getY()));
                Point newPoint = new Point(newX, newY);

                regionPoints.add(newPoint);
            }

            List<String> partialFractalIds = new ArrayList<>();
            for (String fractal: fractalIds) {
                if (fractal.startsWith(Integer.toString(i))) {
                    partialFractalIds.add(fractal);
                }
            }

            // todo: FIX THIS ASAP - send over successor table
            int executorId = AppConfig.chordState.getIdForFractalIDAndJob(partialFractalIds.get(0), job.getName());
            ServentInfo executorServent = AppConfig.chordState.getAllNodeIdInfoMap().get(executorId);
            // send to one node partialFractalIds, regionPoints and job
            JobExecutionMessage jobExecutionMessage = new JobExecutionMessage(AppConfig.myServentInfo.getListenerPort(),
                    executorServent.getListenerPort(), AppConfig.myServentInfo.getIpAddress(), executorServent.getIpAddress(),
                    partialFractalIds, regionPoints, job, serventJobs, 0);
            MessageUtil.sendMessage(jobExecutionMessage);
        }
    }

    private int computeJobNodesCount(int serventCount, int pointsCount) {
        int result = 1;
        int x = 0;
        while (true) {
            int possibleNodeCount = 1 + x * (pointsCount - 1);
            if (possibleNodeCount > serventCount) {
                break;
            }
            result = possibleNodeCount;
            x++;
        }
        return result;
    }

    private List<String> computeFractalIds(int nodesCount, int pointsCount) {
        List<String> fractalIds = new ArrayList<>();
        int length = 0;
        String base = "";

        while (nodesCount > 0) {
            if (length >= 1) {
                boolean hasLength = false;
                for (String fractalId: fractalIds) {
                    if (fractalId.length() == length) {
                        base = fractalId;
                        fractalIds.remove(fractalId);
                        hasLength = true;
                        break;
                    }
                }
                if (!hasLength) {
                    length++;
                    continue;
                }

                nodesCount++;
            }

            for (int i = 0; i < pointsCount; i++) {
                fractalIds.add(base + i);
            }
            if (length == 0) {
                length++;
            }
            nodesCount -= pointsCount;
        }
        Collections.sort(fractalIds);
        return fractalIds;
    }
}
