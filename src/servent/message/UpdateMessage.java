package servent.message;

import app.models.FractalIdJob;
import app.models.Job;
import app.models.ServentInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateMessage extends BasicMessage {

	private static final long serialVersionUID = 3586102505319194978L;

	private Map<Integer, ServentInfo> nodesMap;
	private Map<Integer, FractalIdJob> serventJobsMap;
	private List<Job> activeJobs;

	public UpdateMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
						 Map<Integer, ServentInfo> nodesMap, Map<Integer, FractalIdJob> serventJobsMap,
						 List<Job> activeJobs) {
		super(MessageType.UPDATE, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
		this.nodesMap = nodesMap;
		this.serventJobsMap = serventJobsMap;
		this.activeJobs = activeJobs;
	}

	public UpdateMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
						 Map<Integer, ServentInfo> nodesMap) {
		this(senderPort, receiverPort, senderIpAddress, receiverIpAddress, nodesMap, new HashMap<>(), new ArrayList<>());
	}

	public Map<Integer, ServentInfo> getNodesMap() { return nodesMap; }

	public Map<Integer, FractalIdJob> getServentJobsMap() { return serventJobsMap; }

	public List<Job> getActiveJobs() { return activeJobs; }
}
