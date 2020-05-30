package cli.command;

import app.AppConfig;
import app.models.ServentInfo;
import servent.message.AskJobResultMessage;
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

        if (fractalId == null) {
            // za posao samo ceo result
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
        } else {
            //todo: posao samo za taj fractalId
        }

    }
}
