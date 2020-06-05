package app.models;

import app.ChordState;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an immutable class that holds all the information for a servent.
 *
 * @author bmilojkovic
 */
public class ServentInfo implements Serializable {

	private static final long serialVersionUID = 5304170042791281555L;
	private final String ipAddress;
	private final int listenerPort;
	private final int fifoListenerPort;
	private int weakFailureLimit;
	private int strongFailureLimit;
	private List<Job> jobs;

	private final int chordId;
	private int id;

	public ServentInfo(String ipAddress, int listenerPort, int fifoListenerPort) {
		this.ipAddress = ipAddress;
		this.listenerPort = listenerPort;
		this.fifoListenerPort = fifoListenerPort;
		this.weakFailureLimit = 1000;
		this.strongFailureLimit = 1000;
		this.jobs = new ArrayList<>();

		this.chordId = ChordState.chordHash(listenerPort);
		this.id = -1;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public int getListenerPort() {
		return listenerPort;
	}

	public int getFifoListenerPort() { return fifoListenerPort; }

	public int getChordId() {
		return chordId;
	}

	public int getWeakFailureLimit() {
		return weakFailureLimit;
	}

	public int getStrongFailureLimit() {
		return strongFailureLimit;
	}

	public void setWeakFailureLimit(int weakFailureLimit) {
		this.weakFailureLimit = weakFailureLimit;
	}

	public void setStrongFailureLimit(int strongFailureLimit) {
		this.strongFailureLimit = strongFailureLimit;
	}

	public List<Job> getJobs() {
		return jobs;
	}

	public void addJob(Job newJob) {
		if (!jobs.contains(newJob)) {
			jobs.add(newJob);
		}
	}

	public Job getJobForName(String name) {
		for (Job job: jobs) {
			if (job.getName().equals(name)) {
				return job;
			}
		}
		return null;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "ServentInfo{" + id + "|" + ipAddress + ":" + listenerPort + "}";
	}
}
