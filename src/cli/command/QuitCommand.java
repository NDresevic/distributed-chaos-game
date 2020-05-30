package cli.command;

import app.AppConfig;
import cli.CLIParser;
import servent.SimpleServentListener;
import servent.message.QuitMessage;
import servent.message.util.MessageUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

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
        // inform successor so that it can delete you
        QuitMessage quitMessage = new QuitMessage(AppConfig.myServentInfo.getListenerPort(),
                AppConfig.chordState.getNextNodePort(),
                AppConfig.myServentInfo.getIpAddress(),
                AppConfig.chordState.getNextNodeIpAddress(), AppConfig.myServentInfo.getId(), AppConfig.myServentInfo.getId());
        MessageUtil.sendMessage(quitMessage);
        // send bootstrap server quit message - to remove us from active servents
        try {
            Socket bsSocket = new Socket(AppConfig.BOOTSTRAP_IP_ADDRESS, AppConfig.BOOTSTRAP_PORT);

            PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
            bsWriter.write("Quit\n" + AppConfig.myServentInfo.getListenerPort() + "\n");
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