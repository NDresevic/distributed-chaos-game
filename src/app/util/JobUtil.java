package app.util;

import app.AppConfig;
import app.models.*;
import servent.message.chaos_game.IdleMessage;
import servent.message.chaos_game.JobExecutionMessage;
import servent.message.util.MessageUtil;

import java.util.*;

public class JobUtil {

    private static Map<Job, Integer> computeServentCountForJobs(int serventCount, List<Job> jobs) {
        Map<Job, Integer> result = new HashMap<>();
        int jobCount = jobs.size();
        for (int i = 0; i < jobCount; i++) {
            int assignedServentCount = serventCount / jobCount;
            if (i < serventCount % jobCount) {
                assignedServentCount++;
            }
            result.put(jobs.get(i), assignedServentCount);
        }
        return result;
    }

    private static int computeJobServentsCount(int serventCount, int pointsCount) {
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

    private static List<String> computeFractalIds(int serventCount, int pointsCount) {
        List<String> fractalIds = new ArrayList<>();
        int length = 0;
        String base = "";

        // if only one node is executing the job, no need to split fractalIds
        if (serventCount == 1) {
            fractalIds.add("0");
            return fractalIds;
        }

        while (serventCount > 0) {
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

                serventCount++;
            }

            for (int i = 0; i < pointsCount; i++) {
                fractalIds.add(base + i);
            }
            if (length == 0) {
                length++;
            }
            serventCount -= pointsCount;
        }
        Collections.sort(fractalIds);
        return fractalIds;
    }

    public static List<Point> computeRegionPoints(List<Point> jobPoints, int i, double proportion) {
        List<Point> regionPoints = new ArrayList<>();
        Point referentPoint = jobPoints.get(i);
        for (int j = 0; j < jobPoints.size(); j++) {
            if (i == j) {
                regionPoints.add(referentPoint);
                continue;
            }

            Point other = jobPoints.get(j);
            int newX = (int) (referentPoint.getX() + proportion * (other.getX() - referentPoint.getX()));
            int newY = (int) (referentPoint.getY() + proportion * (other.getY() - referentPoint.getY()));
            Point newPoint = new Point(newX, newY);

            regionPoints.add(newPoint);
        }
        return regionPoints;
    }

    private static Map<FractalIdJob, FractalIdJob> mapFractalsForJobSchedule(List<String> oldFractals,
                                                                             List<String> newFractals,
                                                                             String jobName,
                                                                             JobScheduleType scheduleType) {
        Map<FractalIdJob, FractalIdJob> result = new HashMap<>();

        switch (scheduleType) {
            //
            case JOB_ADDED:
            case SERVENT_REMOVED:

                for (String oldOne: oldFractals) {
                    if (newFractals.size() == 1) {   // only one servent is executing the job, map to all old ones
                        result.put(new FractalIdJob(oldOne, jobName), new FractalIdJob(newFractals.get(0), jobName));
                        continue;
                    }
                    for (String newOne: newFractals) {
                        if (oldOne.startsWith(newOne)) {
                            result.put(new FractalIdJob(oldOne, jobName), new FractalIdJob(newOne, jobName));
                        }
                    }
                }
                return result;

            // todo: implement
            case JOB_REMOVED:
            case SERVENT_ADDED:
                return result;
        }

        return result;
    }

    private static List<String> getFractalsForJob(Map<Integer, FractalIdJob> serventJobs, String jobName) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<Integer, FractalIdJob> entry: serventJobs.entrySet()) {
            if (entry.getValue().getJobName().equals(jobName)) {
                result.add(entry.getValue().getFractalId());
            }
        }
        Collections.sort(result);
        return result;
    }

    public static Map<FractalIdJob, FractalIdJob> executeJobScheduling(int serventCount, JobScheduleType scheduleType) {
        AppConfig.timestampedStandardPrint("Executing job scheduling for type: " + scheduleType);

        List<Job> activeJobsList = AppConfig.chordState.getActiveJobsList();
        Map<Job, Integer> jobServentCount = JobUtil.computeServentCountForJobs(serventCount, activeJobsList);
        AppConfig.timestampedStandardPrint("Servent count for all jobs:\n" + jobServentCount);

        List<FractalIdJob> fractalIdJobList = new ArrayList<>();
        Map<Job, List<String>> jobFractalsMap = new HashMap<>();
        for (Map.Entry<Job, Integer> entry : jobServentCount.entrySet()) {
            Job currentJob = entry.getKey();
            int assignedServentCount = entry.getValue();

            // compute number of servents needed to execute the job
            int currentJobServentCount = JobUtil.computeJobServentsCount(assignedServentCount, currentJob.getPointsCount());
            AppConfig.timestampedStandardPrint("Number of nodes for job \'" + currentJob.getName() + "\': " + assignedServentCount);

            // compute fractal ids for current job
            List<String> currentFractals = JobUtil.computeFractalIds(currentJobServentCount, currentJob.getPointsCount());
            AppConfig.timestampedStandardPrint("Fractal IDs for job \'" + currentJob.getName() + "\': " + currentFractals.toString());
            jobFractalsMap.put(currentJob, currentFractals);

            // add fractalIds and jobs
            for (String fractalId : currentFractals) {
                fractalIdJobList.add(new FractalIdJob(fractalId, currentJob.getName()));
            }
        }

        // map id -> fractal -> job
        Map<Integer, FractalIdJob> oldServentJobs = new HashMap<>(AppConfig.chordState.getServentJobs());
        Map<Integer, FractalIdJob> newServentJobsMap = new HashMap<>();
        for (Map.Entry<Integer, ServentInfo> entry : AppConfig.chordState.getAllNodeIdInfoMap().entrySet()) {
            newServentJobsMap.put(entry.getKey(), fractalIdJobList.remove(0));
            if (fractalIdJobList.isEmpty()) {
                break;
            }
        }
        AppConfig.chordState.setServentJobs(newServentJobsMap);
        AppConfig.timestampedStandardPrint("stari");
        AppConfig.timestampedStandardPrint(oldServentJobs.toString());
        AppConfig.timestampedStandardPrint("novi");
        AppConfig.timestampedStandardPrint(newServentJobsMap.toString());

        // map all old fractals to new ones
        Map<FractalIdJob, FractalIdJob> mappedFractals = new HashMap<>();
        for (Map.Entry<Job, List<String>> entry: jobFractalsMap.entrySet()) {
            String currentJobName = entry.getKey().getName();
            List<String> oldFractals = JobUtil.getFractalsForJob(oldServentJobs, currentJobName);
            Map<FractalIdJob, FractalIdJob> currentMapped = JobUtil.mapFractalsForJobSchedule(oldFractals,
                    entry.getValue(), currentJobName, scheduleType);
            for (Map.Entry<FractalIdJob, FractalIdJob> e: currentMapped.entrySet()) {
                mappedFractals.put(e.getKey(), e.getValue());
            }
        }
        AppConfig.timestampedStandardPrint("Mapped fractals: \n" + mappedFractals);

        for (Map.Entry<Job, List<String>> entry: jobFractalsMap.entrySet()) {
            Job currentJob = entry.getKey();
            List<String> fractals = entry.getValue();

            // compute initial job division and send messages
            List<Point> jobPoints = currentJob.getPoints();
            double proportion = currentJob.getProportion();

            if (fractals.size() == 1) { // only one node is executing the job
                int finalReceiverId = AppConfig.chordState.getIdForFractalIDAndJob(fractals.get(0), currentJob.getName());
                ServentInfo receiverServent = AppConfig.chordState.getNextNodeForServentId(finalReceiverId);

                JobExecutionMessage jem = new JobExecutionMessage(AppConfig.myServentInfo.getListenerPort(),
                        receiverServent.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
                        receiverServent.getIpAddress(), fractals, jobPoints, currentJob, newServentJobsMap,
                        0, finalReceiverId, mappedFractals, scheduleType);
                MessageUtil.sendMessage(jem);
                continue;
            }

            // split work and send to servents
            for (int i = 0; i < jobPoints.size(); i++) {
                List<Point> regionPoints = JobUtil.computeRegionPoints(jobPoints, i, proportion);

                List<String> partialFractalIds = new ArrayList<>();
                for (String fractal: fractals) {
                    if (fractal.startsWith(Integer.toString(i))) {
                        partialFractalIds.add(fractal);
                    }
                }

                // send to one node partialFractalIds, regionPoints and job
                int finalReceiverId = AppConfig.chordState.getIdForFractalIDAndJob(partialFractalIds.get(0), currentJob.getName());
                ServentInfo receiverServent = AppConfig.chordState.getNextNodeForServentId(finalReceiverId);

                JobExecutionMessage jobExecutionMessage = new JobExecutionMessage(AppConfig.myServentInfo.getListenerPort(),
                        receiverServent.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
                        receiverServent.getIpAddress(), partialFractalIds, regionPoints, currentJob, newServentJobsMap,
                        0, finalReceiverId, mappedFractals, scheduleType);
                MessageUtil.sendMessage(jobExecutionMessage);
            }
        }

        // send to idle nodes that they are idle and new job division
        for (Map.Entry<Integer, ServentInfo> entry: AppConfig.chordState.getAllNodeIdInfoMap().entrySet()) {
            int serventId = entry.getKey();
            if (!AppConfig.chordState.getServentJobs().containsKey(serventId)) {   // servent is idle
                ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(serventId);

                IdleMessage idleMessage = new IdleMessage(AppConfig.myServentInfo.getListenerPort(),
                        nextServent.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
                        nextServent.getIpAddress(), AppConfig.chordState.getServentJobs(), serventId, mappedFractals);
                MessageUtil.sendMessage(idleMessage);
            }
        }

        return mappedFractals;
    }
}
