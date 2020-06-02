package cli.command.chaos_game;

import app.AppConfig;
import app.models.*;
import app.util.JobUtil;
import cli.command.CLICommand;

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
        JobUtil.executeJobScheduling(serventCount, JobScheduleType.JOB_ADDED);
    }
}
