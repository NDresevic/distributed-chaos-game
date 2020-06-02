package cli.command.chaos_game;

import app.AppConfig;
import app.models.ServentInfo;
import cli.command.CLICommand;
import servent.message.chaos_game.AskJobFractalIDResultMessage;
import servent.message.chaos_game.AskJobResultMessage;
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
            ServentInfo receiverServent = AppConfig.chordState.getNextNodeForServentId(firstServentId);

            AskJobResultMessage askJobResultMessage = new AskJobResultMessage(
                    AppConfig.myServentInfo.getListenerPort(),
                    receiverServent.getListenerPort(),
                    AppConfig.myServentInfo.getIpAddress(),
                    receiverServent.getIpAddress(), jobName, lastServentId, firstServentId);
            MessageUtil.sendMessage(askJobResultMessage);
        }
        // get result for specific job and fractalId
        else {
            int receiverId = AppConfig.chordState.getIdForFractalIDAndJob(fractalId, jobName);
            ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(receiverId);

            AskJobFractalIDResultMessage message = new AskJobFractalIDResultMessage(
                    AppConfig.myServentInfo.getListenerPort(),
                    nextServent.getListenerPort(),
                    AppConfig.myServentInfo.getIpAddress(),
                    nextServent.getIpAddress(),
                    jobName, receiverId);
            MessageUtil.sendMessage(message);
        }

        // todo: BUG - kad se trazi rez na istom cvoru koji je zapoceo posao, ne radi -> NEKAD samo???
    }
}
