package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import servent.message.NewNodeMessage;
import servent.message.util.MessageUtil;

public class ServentInitializer implements Runnable {

	private String getLastAndFirstServent() {
		int bsPort = AppConfig.BOOTSTRAP_PORT;
		String bsIpAddress = AppConfig.BOOTSTRAP_IP_ADDRESS;
		
		String lastServent = "";
		String firstServent = "";
		
		try {
			Socket bsSocket = new Socket(bsIpAddress, bsPort);
			
			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("Hail\n" + AppConfig.myServentInfo.getIpAddress() + "\n" +
					AppConfig.myServentInfo.getListenerPort() + "\n");
			bsWriter.flush();

			Scanner bsScanner = new Scanner(bsSocket.getInputStream());
			lastServent = bsScanner.nextLine();
			firstServent = bsScanner.nextLine();

			bsSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return lastServent + " " + firstServent;
	}
	
	@Override
	public void run() {
		String servents = getLastAndFirstServent();
		String lastServent = servents.split(" ")[0];
		String firstServent = servents.split(" ")[1];
		
		if (lastServent.equals("")) {
			AppConfig.timestampedErrorPrint("Error in contacting bootstrap. Exiting...");
			System.exit(0);
		}
		if (lastServent.equals("-1")) { //bootstrap gave us -1 -> we are first
			AppConfig.myServentInfo.setId(0);
			AppConfig.chordState.getAllNodeIdInfoMap().put(AppConfig.myServentInfo.getId(), AppConfig.myServentInfo);
			AppConfig.timestampedStandardPrint("First node in distributed chaos-game system.");
		} else { //bootstrap gave us something else - let that node tell our successor that we are here
			NewNodeMessage nnm = new NewNodeMessage(AppConfig.myServentInfo.getListenerPort(),
					Integer.parseInt(lastServent.split(":")[1]), AppConfig.myServentInfo.getIpAddress(),
					lastServent.split(":")[0], firstServent);
			MessageUtil.sendMessage(nnm);
		}
	}

}
