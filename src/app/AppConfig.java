package app;

import app.lamport_mutex.LamportClock;
import app.lamport_mutex.LamportMutex;
import app.models.Job;
import app.models.Point;
import app.models.ServentInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * This class contains all the global application configuration stuff.
 * @author bmilojkovic
 *
 */
public class AppConfig {

	/**
	 * Convenience access for this servent's information
	 */
	public static ServentInfo myServentInfo;
	
	/**
	 * Print a message to stdout with a timestamp
	 * @param message message to print
	 */
	public static void timestampedStandardPrint(String message) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();
		
		System.out.println(timeFormat.format(now) + " - " + message);
	}
	
	/**
	 * Print a message to stderr with a timestamp
	 * @param message message to print
	 */
	public static void timestampedErrorPrint(String message) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();
		
		System.err.println(timeFormat.format(now) + " - " + message);
	}
	
	public static boolean INITIALIZED = false;
	public static String BOOTSTRAP_IP_ADDRESS;
	public static int BOOTSTRAP_PORT;

	public static ChordState chordState;

	public static LamportClock lamportClock;
	public static LamportMutex lamportMutex;
	
	/**
	 * Reads a config file. Should be called once at start of app.
	 * The config file should be of the following format:
	 * <br/>
	 * <code><br/>
	 * bootstrap.ip=localhost			- bootstrap server ip address <br/>
	 * bootstrap.port=2000				- bootstrap server listener port <br/>
	 * 
	 * </code>
	 * <br/>
	 *
	 * @param configName name of configuration file
	 */
	public static void readBootstrapConfig(String configName){
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(configName)));
			
		} catch (IOException e) {
			timestampedErrorPrint("Couldn't open properties file. Exiting...");
			System.exit(0);
		}

		BOOTSTRAP_IP_ADDRESS = properties.getProperty("bootstrap.ip");
		try {
			BOOTSTRAP_PORT = Integer.parseInt(properties.getProperty("bootstrap.port"));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading bootstrap_port. Exiting...");
			System.exit(0);
		}

		ChordState.CHORD_SIZE = 64;
		
		myServentInfo = new ServentInfo(BOOTSTRAP_IP_ADDRESS, BOOTSTRAP_PORT, 0);
		lamportClock = new LamportClock();
		lamportMutex = new LamportMutex(myServentInfo);
	}

	public static void readServentConfig(String configName, int serventId) {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(configName)));

		} catch (IOException e) {
			timestampedErrorPrint("Couldn't open properties file. Exiting...");
			System.exit(0);
		}

		ChordState.CHORD_SIZE = 64;
		chordState = new ChordState();

		try {
			String ipAddress = properties.getProperty("ip");
			int listenerPort = Integer.parseInt(properties.getProperty("port"));

			myServentInfo = new ServentInfo(ipAddress, listenerPort, listenerPort + 10);
			lamportClock = new LamportClock();
			lamportMutex = new LamportMutex(myServentInfo);
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading ip_address or port. Exiting...");
			System.exit(0);
		}

		BOOTSTRAP_IP_ADDRESS = properties.getProperty("bootstrap.ip");
		try {
			BOOTSTRAP_PORT = Integer.parseInt(properties.getProperty("bootstrap.port"));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading bootstrap_port. Exiting...");
			System.exit(0);
		}

		try {
			int weakFailureLimit = Integer.parseInt(properties.getProperty("weak_failure_limit"));
			int strongFailureLimit = Integer.parseInt(properties.getProperty("strong_failure_limit"));

			myServentInfo.setWeakFailureLimit(weakFailureLimit);
			myServentInfo.setStrongFailureLimit(strongFailureLimit);
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading ip_address or port. Exiting...");
			System.exit(0);
		}

		if (properties.getProperty("job_count") == null) {
			return;
		}

		int jobsCount = Integer.parseInt(properties.getProperty("job_count"));
		for (int i = 0; i < jobsCount; i++) {
			String jobName = properties.getProperty("job" + i + ".name");

			String[] pointsCoordinates = properties.getProperty("job" + i + ".points.coordinates").split(";");
			List<Point> points = new ArrayList<>();
			try {
				for (String coordinates: pointsCoordinates) {
					String[] xy = coordinates.substring(1, coordinates.length() - 1).split(",");
					points.add(new Point(Integer.parseInt(xy[0]), Integer.parseInt(xy[1])));
				}
			} catch (NumberFormatException e) {
				timestampedErrorPrint("Problem reading points for the job. Exiting...");
				System.exit(0);
			}

			try {
				int pointsCount = Integer.parseInt(properties.getProperty("job" + i + ".points.count"));
				double proportion = Double.parseDouble(properties.getProperty("job" + i + ".proportion"));
				int width = Integer.parseInt(properties.getProperty("job" + i + ".width"));
				int height = Integer.parseInt(properties.getProperty("job" + i + ".height"));

				Job job = new Job(jobName, pointsCount, proportion, width, height, points);
				myServentInfo.addJob(job);
			} catch (NumberFormatException e) {
				timestampedErrorPrint("Problem reading integer or double properties for the job. Exiting...");
				System.exit(0);
			}
		}
	}
	
}
