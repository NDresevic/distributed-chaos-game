package cli.command.chaos_game;

import app.AppConfig;
import app.models.JobExecution;
import app.models.Point;
import cli.CLIParser;
import cli.command.CLICommand;
import servent.SimpleServentListener;
import servent.message.chaos_game.QuitMessage;
import servent.message.util.MessageUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class QuitCommand implements CLICommand {

    private CLIParser parser;
    private SimpleServentListener listener;

    public QuitCommand(CLIParser parser, SimpleServentListener listener) {
        this.parser = parser;
        this.listener = listener;
    }

    @Override
    public String commandName() {
        return "quit";
    }

    @Override
    public void execute(String args) {
        // inform successor so that it can delete you if you are not only node in the system
        if (AppConfig.chordState.getAllNodeIdInfoMap().size() > 1) {

            QuitMessage quitMessage;
            if (AppConfig.chordState.getExecutionJob() != null) { // add my computed data to message if I am executing
                JobExecution je = AppConfig.chordState.getExecutionJob();
                String myJobName = je.getJobName();
                String myFractalId = je.getFractalId();
                List<Point> myComputedPoints = new ArrayList<>(je.getComputedPoints());

                quitMessage = new QuitMessage(AppConfig.myServentInfo.getListenerPort(),
                        AppConfig.chordState.getNextNodePort(),
                        AppConfig.myServentInfo.getIpAddress(),
                        AppConfig.chordState.getNextNodeIpAddress(), AppConfig.myServentInfo.getId(),
                        myJobName, myFractalId, myComputedPoints);

                je.stop();
            } else {
                quitMessage = new QuitMessage(AppConfig.myServentInfo.getListenerPort(),
                        AppConfig.chordState.getNextNodePort(),
                        AppConfig.myServentInfo.getIpAddress(),
                        AppConfig.chordState.getNextNodeIpAddress(), AppConfig.myServentInfo.getId());
            }
            MessageUtil.sendMessage(quitMessage);
        }

        // send bootstrap server quit message - to remove us from active servents
        try {
            Socket bsSocket = new Socket(AppConfig.BOOTSTRAP_IP_ADDRESS, AppConfig.BOOTSTRAP_PORT);

            PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
            bsWriter.write("Quit\n" +
                    AppConfig.myServentInfo.getIpAddress() + "\n" +
                    AppConfig.myServentInfo.getListenerPort() + "\n");
            bsWriter.flush();

            bsSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        AppConfig.timestampedStandardPrint("Quitting...");
        parser.stop();
        listener.stop();
    }
}
