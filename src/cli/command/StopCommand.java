package cli.command;

import app.AppConfig;
import servent.message.StopJobMessage;
import servent.message.util.MessageUtil;

public class StopCommand implements CLICommand {
    @Override
    public String commandName() {
        return "stop";
    }

    @Override
    public void execute(String args) {
        // send to first successor to stop the job
        StopJobMessage message = new StopJobMessage(AppConfig.myServentInfo.getListenerPort(),
                AppConfig.chordState.getNextNodePort(),
                AppConfig.myServentInfo.getIpAddress(),
                AppConfig.chordState.getNextNodeIpAddress(),
                args);
        MessageUtil.sendMessage(message);
    }
}
