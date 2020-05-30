package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import servent.message.NewNodeMessage;
import servent.message.util.MessageUtil;

public class ServentInitializer implements Runnable {

	private String getLastAndFirstServentPort() {
		int bsPort = AppConfig.BOOTSTRAP_PORT;
		String bsIpAddress = AppConfig.BOOTSTRAP_IP_ADDRESS;
		
		int lastServentPort = -2;
		int firstServentPort = -2;
		
		try {
			Socket bsSocket = new Socket(bsIpAddress, bsPort);
			
			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("Hail\n" + AppConfig.myServentInfo.getListenerPort() + "\n");
			bsWriter.flush();

			Scanner bsScanner = new Scanner(bsSocket.getInputStream());
			lastServentPort = bsScanner.nextInt();
			firstServentPort = bsScanner.nextInt();

			bsSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return lastServentPort + " " + firstServentPort;
	}
	
	@Override
	public void run() {
		String ports = getLastAndFirstServentPort();
		int lastServentPort = Integer.parseInt(ports.split(" ")[0]);
		int firstServentPort = Integer.parseInt(ports.split(" ")[1]);
		
		if (lastServentPort == -2) {
			AppConfig.timestampedErrorPrint("Error in contacting bootstrap. Exiting...");
			System.exit(0);
		}
//		AppConfig.timestampedStandardPrint(lastServentPort + " " + firstServentPort);
		if (lastServentPort == -1) { //bootstrap gave us -1 -> we are first
			AppConfig.myServentInfo.setId(0);
			AppConfig.timestampedStandardPrint("First node in Chord system.");
		} else { //bootstrap gave us something else - let that node tell our successor that we are here
			NewNodeMessage nnm = new NewNodeMessage(AppConfig.myServentInfo.getListenerPort(), lastServentPort,
					AppConfig.myServentInfo.getIpAddress(), "localhost", firstServentPort);
			MessageUtil.sendMessage(nnm);
		}
	}

}
