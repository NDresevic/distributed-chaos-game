package cli.command.chaos_game;

import app.AppConfig;
import app.models.ServentInfo;
import cli.command.CLICommand;
import servent.message.chaos_game.AskStatusMessage;
import servent.message.util.MessageUtil;

public class StatusCommand implements CLICommand {

    @Override
    public String commandName() {
        return "status";
    }

    @Override
    public void execute(String args) {
        if (args == null || args.equals("")) { // get status for everything
            AskStatusMessage askStatusMessage = new AskStatusMessage(AppConfig.myServentInfo.getListenerPort(),
                    AppConfig.chordState.getNextNodePort(), AppConfig.myServentInfo.getIpAddress(),
                    AppConfig.chordState.getNextNodeIpAddress(), AppConfig.chordState.getFirstSuccessorId(), 2);
            MessageUtil.sendMessage(askStatusMessage);
            return;
        }

        String[] argsList = args.split(" ");
        String jobName = argsList[0];
        if (argsList.length == 1) { // get status for job
            int firstServentId = AppConfig.chordState.getFirstIdForJob(jobName);
            ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(firstServentId);

            AskStatusMessage askStatusMessage = new AskStatusMessage(AppConfig.myServentInfo.getListenerPort(),
                    nextServent.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
                    nextServent.getIpAddress(), firstServentId, jobName, 1);
            MessageUtil.sendMessage(askStatusMessage);
        } else { // get status for specific job and fractalID
            String fractalId = argsList[1];
            AppConfig.timestampedStandardPrint(AppConfig.chordState.getServentJobs().toString());
            int receiverId = AppConfig.chordState.getIdForFractalIDAndJob(fractalId, jobName);
            ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(receiverId);

            AskStatusMessage askStatusMessage = new AskStatusMessage(AppConfig.myServentInfo.getListenerPort(),
                    nextServent.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
                    nextServent.getIpAddress(), receiverId, jobName, fractalId, 0);
            MessageUtil.sendMessage(askStatusMessage);
        }
    }
}
