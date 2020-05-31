package cli.command;

import app.AppConfig;
import app.models.ServentInfo;
import servent.message.AskStatusMessage;
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
                    AppConfig.chordState.getNextNodeIpAddress());
            MessageUtil.sendMessage(askStatusMessage);
            return;
        }

        String[] argsList = args.split(" ");
        String jobName = argsList[0];
        if (argsList.length == 1) { // get status for job
            int firstServentId = AppConfig.chordState.getFirstIdForJob(jobName);

            ServentInfo firstServent = AppConfig.chordState.getAllNodeIdInfoMap().get(firstServentId);
            // todo: fix asap ne slati ovako
            AskStatusMessage askStatusMessage = new AskStatusMessage(AppConfig.myServentInfo.getListenerPort(),
                    firstServent.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
                    firstServent.getIpAddress(), jobName);
            MessageUtil.sendMessage(askStatusMessage);
        } else { // get status for specific job and fractalID
            String fractalId = argsList[1];
            int executorId = AppConfig.chordState.getIdForFractalIDAndJob(fractalId, jobName);
            ServentInfo executorServent = AppConfig.chordState.getAllNodeIdInfoMap().get(executorId);

            // todo: fix sending
            AskStatusMessage askStatusMessage = new AskStatusMessage(AppConfig.myServentInfo.getListenerPort(),
                    executorServent.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
                    executorServent.getIpAddress(), jobName, fractalId);
            MessageUtil.sendMessage(askStatusMessage);
        }
    }
}
