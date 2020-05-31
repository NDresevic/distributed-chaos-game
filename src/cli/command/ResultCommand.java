package cli.command;

import app.AppConfig;
import app.models.ServentInfo;
import servent.message.AskJobFractalIDResultMessage;
import servent.message.AskJobResultMessage;
import servent.message.Message;
import servent.message.util.MessageUtil;

public class ResultCommand implements CLICommand {

    @Override
    public String commandName() {
        return "result";
    }

    @Override
    public void execute(String args) {
        String jobName = args.split(" ")[0];

        String fractalId = null;
        if (args.contains(" ")) {
            fractalId = args.split(" ")[1];
        }

        // get result for whole job
        if (fractalId == null) {
            AppConfig.timestampedStandardPrint("Collecting computed points for job \"" + jobName + "\"...");

            int firstServentId = AppConfig.chordState.getFirstIdForJob(jobName);
            int lastServentId = AppConfig.chordState.getLastIdForJob(jobName);
            ServentInfo firstServent = AppConfig.chordState.getAllNodeIdInfoMap().get(firstServentId);
            // todo: fix asap ne slati ovako
            AskJobResultMessage askJobResultMessage = new AskJobResultMessage(
                    AppConfig.myServentInfo.getListenerPort(),
                    firstServent.getListenerPort(),
                    AppConfig.myServentInfo.getIpAddress(),
                    firstServent.getIpAddress(), jobName, lastServentId);
            MessageUtil.sendMessage(askJobResultMessage);
        }
        // get result for specific job and fractalId
        else {
            int executorId = AppConfig.chordState.getIdForFractalIDAndJob(fractalId, jobName);
            ServentInfo executorServent = AppConfig.chordState.getAllNodeIdInfoMap().get(executorId);
            // todo: fix sending
            AskJobFractalIDResultMessage message = new AskJobFractalIDResultMessage(
                    AppConfig.myServentInfo.getListenerPort(),
                    executorServent.getListenerPort(),
                    AppConfig.myServentInfo.getIpAddress(),
                    executorServent.getIpAddress(),
                    jobName);
            MessageUtil.sendMessage(message);
        }


        // todo: BUG - kad se trazi rez na istom cvoru koji je zapoceo posao, ne radi
    }
}
