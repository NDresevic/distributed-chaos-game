package cli.command.chaos_game;

import app.AppConfig;
import app.models.FractalIdJob;
import app.models.Job;
import app.models.Point;
import app.models.ServentInfo;
import app.util.JobUtil;
import cli.command.CLICommand;
import servent.message.chaos_game.IdleMessage;
import servent.message.chaos_game.JobExecutionMessage;
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
        int activeJobsCount = AppConfig.chordState.getActiveJobsCount();
        if (serventCount < 1 || serventCount < activeJobsCount + 1) {
            AppConfig.timestampedErrorPrint("There are not enough servents to execute jobs.");
            return;
        }

        Job job = AppConfig.myServentInfo.getJobForName(args);
        if (job == null) {
            AppConfig.timestampedErrorPrint("Job with name \'" + args + "\" not found in the list of jobs.");
            return;
        }

        // compute number of servents needed for each job
        AppConfig.chordState.addNewJob(job);
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
            List<String> oldFractals = JobUtil.fractalsForJob(oldServentJobs, currentJobName);
            Map<FractalIdJob, FractalIdJob> currentMapped = JobUtil.mapFractalsForJob(oldFractals, entry.getValue(),
                    currentJobName);
            for (Map.Entry<FractalIdJob, FractalIdJob> e: currentMapped.entrySet()) {
                mappedFractals.put(e.getKey(), e.getValue());
            }
        }
        AppConfig.timestampedStandardPrint("Mapped fractals and jobs: \n" + mappedFractals);

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
                        0, finalReceiverId, mappedFractals);
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
                        0, finalReceiverId, mappedFractals);
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
    }
}
