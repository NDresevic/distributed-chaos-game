package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import app.models.*;
import servent.message.chord.AskGetMessage;
import servent.message.chord.PutMessage;
import servent.message.WelcomeMessage;
import servent.message.util.FifoSendWorker;
import servent.message.util.MessageUtil;

/**
 * This class implements all the logic required for Chord to function.
 * It has a static method <code>chordHash</code> which will calculate our chord ids.
 * It also has a static attribute <code>CHORD_SIZE</code> that tells us what the maximum
 * key is in our system.
 * 
 * Other public attributes and methods:
 * <ul>
 *   <li><code>chordLevel</code> - log_2(CHORD_SIZE) - size of <code>successorTable</code></li>
 *   <li><code>successorTable</code> - a map of shortcuts in the system.</li>
 *   <li><code>predecessorInfo</code> - who is our predecessor.</li>
 *   <li><code>valueMap</code> - DHT values stored on this node.</li>
 *   <li><code>init()</code> - should be invoked when we get the WELCOME message.</li>
 *   <li><code>isCollision(int chordId)</code> - checks if a servent with that Chord ID is already active.</li>
 *   <li><code>isKeyMine(int key)</code> - checks if we have a key locally.</li>
 *   <li><code>getNextNodeForKey(int key)</code> - if next node has this key, then return it, otherwise returns the nearest predecessor for this key from my successor table.</li>
 *   <li><code>addNodes(List<ServentInfo> nodes)</code> - updates the successor table.</li>
 *   <li><code>putValue(int key, int value)</code> - stores the value locally or sends it on further in the system.</li>
 *   <li><code>getValue(int key)</code> - gets the value locally, or sends a message to get it from somewhere else.</li>
 * </ul>
 * @author bmilojkovic
 *
 */
public class ChordState {

	public static int CHORD_SIZE;
	public static int chordHash(int value) {
		return 61 * value % CHORD_SIZE;
	}
	
	private int chordLevel; //log_2(CHORD_SIZE)
	
	private ServentInfo[] successorTable;
	private ServentInfo predecessorInfo;
	
	//we DO NOT use this to send messages, but only to construct the successor table
	private HashMap<Integer, ServentInfo> allNodeIdInfoMap;

	private Map<Integer, FifoSendWorker> fifoSendWorkerMap = new HashMap<>();

	private JobExecution executionJob;
	private List<Point> receivedComputedPoints = new ArrayList<>();
	private AtomicInteger receivedComputedPointsMessagesCount = new AtomicInteger(0);
	private AtomicInteger expectedComputedPointsMessagesCount = new AtomicInteger(0);

    private AtomicInteger receivedAckMessagesCount = new AtomicInteger(0);

	// [id -> fractalId + job]
	private Map<Integer, FractalIdJob> serventJobs;
	private List<Job> activeJobsList = new ArrayList<>();

	private Map<Integer, Integer> valueMap;
	
	public ChordState() {
		this.chordLevel = 1;
		int tmp = CHORD_SIZE;
		while (tmp != 2) {
			if (tmp % 2 != 0) { //not a power of 2
				throw new NumberFormatException();
			}
			tmp /= 2;
			this.chordLevel++;
		}
		
		successorTable = new ServentInfo[chordLevel];
		for (int i = 0; i < chordLevel; i++) {
			successorTable[i] = null;
		}
		
		predecessorInfo = null;
		valueMap = new HashMap<>();
		allNodeIdInfoMap = new HashMap<>();
		this.serventJobs = new HashMap<>();
	}
	
	/**
	 * This should be called once after we get <code>WELCOME</code> message.
	 * It sets up our initial value map and our first successor so we can send <code>UPDATE</code>.
	 * It also lets bootstrap know that we did not collide.
	 */
	public void init(WelcomeMessage welcomeMsg) {
		// set as predecessor the node who sent the message
		predecessorInfo =  new ServentInfo(welcomeMsg.getSenderIpAddress(), welcomeMsg.getSenderPort(), welcomeMsg.getSenderPort() + 10);
		// set as first successor servent with id 0, for sending of update message
		String[] firstServentInfo = welcomeMsg.getFirstServentIpAddressPort().split(":");
		successorTable[0] = new ServentInfo(firstServentInfo[0], Integer.parseInt(firstServentInfo[1]), Integer.parseInt(firstServentInfo[1]) + 10);

		allNodeIdInfoMap.put(AppConfig.myServentInfo.getId(), AppConfig.myServentInfo);
		AppConfig.timestampedStandardPrint(allNodeIdInfoMap.toString());

		this.valueMap = new HashMap<>();
		
		//tell bootstrap this node is not a collider
		try {
			Socket bsSocket = new Socket(AppConfig.BOOTSTRAP_IP_ADDRESS, AppConfig.BOOTSTRAP_PORT);
			
			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("New\n" +
					AppConfig.myServentInfo.getIpAddress() + "\n" +
					AppConfig.myServentInfo.getListenerPort() + "\n");
			bsWriter.flush();
			bsSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getChordLevel() {
		return chordLevel;
	}
	
	public ServentInfo[] getSuccessorTable() {
		return successorTable;
	}
	
	public int getNextNodePort() {
		if (successorTable[0] == null && allNodeIdInfoMap.size() <= 1) {
			return AppConfig.myServentInfo.getListenerPort();
		}
		return successorTable[0].getListenerPort();
	}

	public String getNextNodeIpAddress() {
		if (successorTable[0] == null && allNodeIdInfoMap.size() <= 1) {
			return AppConfig.myServentInfo.getIpAddress();
		}
		return successorTable[0].getIpAddress();
	}

	public ServentInfo getPredecessor() {
		return predecessorInfo;
	}
	
	public void setPredecessor(ServentInfo newNodeInfo) {
		this.predecessorInfo = newNodeInfo;
	}

	public Map<Integer, Integer> getValueMap() {
		return valueMap;
	}
	
	public void setValueMap(Map<Integer, Integer> valueMap) {
		this.valueMap = valueMap;
	}

	/**
	 * Returns true if we are the owner of the specified key.
	 */
	public boolean isKeyMine(int key) {
		if (predecessorInfo == null) {
			return true;
		}

		int predecessorChordId = predecessorInfo.getChordId();
		int myChordId = AppConfig.myServentInfo.getChordId();

		if (predecessorChordId < myChordId) { //no overflow
			if (key <= myChordId && key > predecessorChordId) {
				return true;
			}
		} else { //overflow
			if (key <= myChordId || key > predecessorChordId) {
				return true;
			}
		}

		return false;
	}

	public int getNodeIdForServentPortAndAddress(int port, String ipAddress) {
		for (Map.Entry<Integer, ServentInfo> entry: allNodeIdInfoMap.entrySet()) {
			if (entry.getValue().getListenerPort() == port && entry.getValue().getIpAddress().equals(ipAddress)) {
				return entry.getKey();
			}
		}
		return -1;
	}

	public int getNodeIdForFifoListenerPortAndAddress(int fifoPort, String ipAddress) {
		for (Map.Entry<Integer, ServentInfo> entry: allNodeIdInfoMap.entrySet()) {
			if (entry.getValue().getFifoListenerPort() == fifoPort && entry.getValue().getIpAddress().equals(ipAddress)) {
				return entry.getKey();
			}
		}
		return -1;
	}

	public int getFirstSuccessorId() {
		if (successorTable[0] == null && allNodeIdInfoMap.size() <= 1) {
			return AppConfig.myServentInfo.getId();
		}
		return successorTable[0].getId();
	}

	// returns true if we can send message directly to servent
	private boolean isServentMySuccessor(int serventId) {
		for (ServentInfo successor: successorTable) {
			if (successor.getId() == serventId) {
				return true;
			}
		}
		return false;
	}

	private boolean isBetweenNodes(int left, int right, int target) {
		int temp = target;
		while (true) {
			temp = (temp + 1) % allNodeIdInfoMap.size();
			if (temp == left) {
				return false;
			}
			if (temp == right) {
				return true;
			}
		}
	}

	public ServentInfo getNextNodeForServentId(int receiverId) {
		// if I am receiver return myself
		if (receiverId == AppConfig.myServentInfo.getId()) {
			return AppConfig.myServentInfo;
		}
		// if it is my successor send directly to it
		if (isServentMySuccessor(receiverId)) {
			return allNodeIdInfoMap.get(receiverId);
		}

		int leftId = successorTable[0].getId();
		for (int i = 1; i < successorTable.length; i++) {
			int rightId = successorTable[i].getId();
			if (isBetweenNodes(leftId, rightId, receiverId)) {
				return successorTable[i-1];
			}
			leftId = rightId;
		}

		if (isBetweenNodes(leftId, successorTable[0].getId(), receiverId)) {
			return successorTable[successorTable.length - 1];
		}

//		int successorId = successorTable[successorTable.length - 1].getId();
//		for (int i = successorTable.length - 2; i >= 0; i--) {
//			if (successorTable[i] == null) {
//				AppConfig.timestampedErrorPrint("Couldn't find successor to send message for " + receiverId);
//				break;
//			}
//
//			int currentId = successorTable[i].getId();
//			if (receiverId <= currentId && currentId < successorId) {
//				return successorTable[i+1];
//			}
//			if (successorId > receiverId && currentId < receiverId) {
//				return successorTable[i];
//			}
//		}
		return successorTable[0];
	}

	/**
	 * Main chord operation - find the nearest node to hop to to find a specific key.
	 * We have to take a value that is smaller than required to make sure we don't overshoot.
	 * We can only be certain we have found the required node when it is our first next node.
	 */
	public ServentInfo getNextNodeForKey(int key) {
		if (isKeyMine(key)) {
			return AppConfig.myServentInfo;
		}

		int previousId = successorTable[0].getChordId();
		for (int i = 1; i < successorTable.length; i++) {
			if (successorTable[i] == null) {
				AppConfig.timestampedErrorPrint("Couldn't find successor for " + key);
				break;
			}

			int successorId = successorTable[i].getChordId();

			if (successorId >= key) {
				return successorTable[i-1];
			}
			if (key > previousId && successorId < previousId) { //overflow
				return successorTable[i-1];
			}
			previousId = successorId;
		}
		//if we have only one node in all slots in the table, we might get here
		//then we can return any item
		return successorTable[0];
	}

	private void calculateChordLevel() {
		this.chordLevel = 1;
		int tmp = allNodeIdInfoMap.size();
		while (tmp > 2) {
			tmp /= 2;
			this.chordLevel++;
		}

		successorTable = new ServentInfo[chordLevel];
		for (int i = 0; i < chordLevel; i++) {
			successorTable[i] = null;
		}
	}

	private void updateSuccessorTable() {
		//first node after me has to be successorTable[0]
		AppConfig.timestampedStandardPrint(allNodeIdInfoMap.toString());

		calculateChordLevel();
		int firstSuccessorIndex = AppConfig.myServentInfo.getId() + 1;
		ServentInfo firstSuccessor = null;
		if (allNodeIdInfoMap.get(firstSuccessorIndex) != null) {
			firstSuccessor = allNodeIdInfoMap.get(firstSuccessorIndex);
		} else {
			if (AppConfig.myServentInfo.getId() != 0) {
				firstSuccessor = allNodeIdInfoMap.get(0);
			}
		}
		successorTable[0] = firstSuccessor;

		int currentIncrement = 2;
		//i is successorTable index
		int successorIndex = 1;
		for(int i = 1; i < chordLevel; i++, currentIncrement *= 2) {
			int id = (AppConfig.myServentInfo.getId() + (int)(Math.pow(2, i))) % allNodeIdInfoMap.size();
			if (allNodeIdInfoMap.containsKey(id)) {
				successorTable[successorIndex] = allNodeIdInfoMap.get(id);
				successorIndex++;
			}
		}
	}

	// add new servents and fifo workers
	public void addNodes(Map<Integer, ServentInfo> newNodes) {
		for (Map.Entry<Integer, ServentInfo> entry: newNodes.entrySet()) {
			int serventId = entry.getKey();
			allNodeIdInfoMap.put(serventId, entry.getValue());

			if (!fifoSendWorkerMap.containsKey(serventId)) {	// add fifo worker for servent
				FifoSendWorker senderWorker = new FifoSendWorker(serventId);
				Thread senderThread = new Thread(senderWorker);
				senderThread.start();
				fifoSendWorkerMap.put(serventId, senderWorker);
			}
		}

		MessageUtil.initializePendingMessages();
		updateSuccessorTable();
	}

	public void setFifoSendWorkerMapWorkers(Map<Integer, FifoSendWorker> newFifoWorkers) {
		for (Map.Entry<Integer, FifoSendWorker> entry: newFifoWorkers.entrySet()) {
			fifoSendWorkerMap.put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * The Chord put operation. Stores locally if key is ours, otherwise sends it on.
	 */
	public void putValue(int key, int value) {
		if (isKeyMine(key)) {
			valueMap.put(key, value);
		} else {
			ServentInfo nextNode = getNextNodeForKey(key);
			PutMessage pm = new PutMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(),
					AppConfig.myServentInfo.getIpAddress(), nextNode.getIpAddress(), key, value);
			MessageUtil.sendMessage(pm);
		}
	}
	
	/**
	 * The chord get operation. Gets the value locally if key is ours, otherwise asks someone else to give us the value.
	 * @return <ul>
	 *			<li>The value, if we have it</li>
	 *			<li>-1 if we own the key, but there is nothing there</li>
	 *			<li>-2 if we asked someone else</li>
	 *		   </ul>
	 */
	public int getValue(int key) {
		if (isKeyMine(key)) {
			if (valueMap.containsKey(key)) {
				return valueMap.get(key);
			} else {
				return -1;
			}
		}
		
		ServentInfo nextNode = getNextNodeForKey(key);
		AskGetMessage agm = new AskGetMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(),
				AppConfig.myServentInfo.getIpAddress(), nextNode.getIpAddress(), String.valueOf(key));
		MessageUtil.sendMessage(agm);
		
		return -2;
	}

	public HashMap<Integer, ServentInfo> getAllNodeIdInfoMap() {
		return allNodeIdInfoMap;
	}

	public JobExecution getExecutionJob() {
		return executionJob;
	}

	public void setExecutionJob(JobExecution executionJob) {
		receivedComputedPoints.clear();
		receivedComputedPointsMessagesCount.set(0);
		this.executionJob = executionJob;
	}

	public Map<Integer, FractalIdJob> getServentJobs() {
		return serventJobs;
	}

	public int getActiveJobsCount() {
		return activeJobsList.size();
	}

	public void setServentJobs(Map<Integer, FractalIdJob> serventJobs) {
		this.serventJobs = serventJobs;
	}

	public int getIdForFractalIDAndJob(String fractalId, String jobName) {
		FractalIdJob fractalIdJob = new FractalIdJob(fractalId, jobName);
		for (Map.Entry<Integer, FractalIdJob> entry: serventJobs.entrySet()) {
			if (entry.getValue().equals(fractalIdJob)) {
				return entry.getKey();
			}
		}
		return -1;
	}

	private List<Integer> getAllIdsForJob(String jobName) {
		List<Integer> ids = new ArrayList<>();
		for (Map.Entry<Integer, FractalIdJob> entry: serventJobs.entrySet()) {
			if (entry.getValue().getJobName().equals(jobName)) {
				ids.add(entry.getKey());
			}
		}
		return ids;
	}

	public int getFirstIdForJob(String jobName) {
		return Collections.min(getAllIdsForJob(jobName));
	}

	public int getLastIdForJob(String jobName) {
		return Collections.max(getAllIdsForJob(jobName));
	}

	public void removeJob(String jobName) {
		for (Job job: activeJobsList) {
			if (job.getName().equals(jobName)) {
				activeJobsList.remove(job);
				break;
			}
		}

		List<Integer> ids = new ArrayList<>();
		for (Map.Entry<Integer, FractalIdJob> entry: serventJobs.entrySet()) {
			if (entry.getValue().getJobName().equals(jobName)) {
				ids.add(entry.getKey());
			}
		}

		for (Integer id: ids) {
			serventJobs.remove(id);
		}

//		AppConfig.timestampedStandardPrint(serventJobs.toString());
	}

	public List<Job> getActiveJobsList() {
		return activeJobsList;
	}

	public boolean addNewJob(Job job) {
		if (!activeJobsList.contains(job)) {
			activeJobsList.add(job);
			return true;
		}
		return false;
	}

	public void addNewJobs(List<Job> jobs) {
		for (Job job: jobs) {
			addNewJob(job);
		}
	}

	public void resetAfterReceivedComputedPoints() {
		receivedComputedPoints.clear();
		receivedComputedPointsMessagesCount.set(0);
		expectedComputedPointsMessagesCount.set(0);
	}

	public void addComputedPoints(List<Point> newPoints) {
		receivedComputedPoints.addAll(newPoints);
		receivedComputedPointsMessagesCount.getAndIncrement();
	}

	public AtomicInteger getReceivedComputedPointsMessagesCount() {
		return receivedComputedPointsMessagesCount;
	}

	public AtomicInteger getExpectedComputedPointsMessagesCount() {
		return expectedComputedPointsMessagesCount;
	}

	public List<Point> getReceivedComputedPoints() {
		return receivedComputedPoints;
	}

	public Map<Integer, FifoSendWorker> getFifoSendWorkerMap() { return fifoSendWorkerMap; }

    public AtomicInteger getReceivedAckMessagesCount() { return receivedAckMessagesCount; }
}
