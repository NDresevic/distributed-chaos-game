package cli.command.chaos_game;

import app.AppConfig;
import cli.command.CLICommand;
import servent.message.chaos_game.StopJobMessage;
import servent.message.util.MessageUtil;

public class StopCommand implements CLICommand {
    @Override
    public String commandName() {
        return "stop";
    }

    @Override
    public void execute(String args) {
        AppConfig.lamportMutex.acquireLock();

        // send to first successor to stop the job
        if (AppConfig.chordState.getAllNodeIdInfoMap().size() > 1) {
            StopJobMessage message = new StopJobMessage(AppConfig.myServentInfo.getListenerPort(),
                    AppConfig.chordState.getNextNodePort(),
                    AppConfig.myServentInfo.getIpAddress(),
                    AppConfig.chordState.getNextNodeIpAddress(),
                    args);
            MessageUtil.sendMessage(message);
        } else { // send to myself cause I am the only node in the system
            StopJobMessage message = new StopJobMessage(AppConfig.myServentInfo.getListenerPort(),
                    AppConfig.myServentInfo.getListenerPort(),
                    AppConfig.myServentInfo.getIpAddress(),
                    AppConfig.myServentInfo.getIpAddress(),
                    args);
            MessageUtil.sendMessage(message);
        }
    }
}
