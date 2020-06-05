package servent.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.AppConfig;
import app.models.FractalIdJob;
import app.models.Job;
import app.models.JobScheduleType;
import app.models.ServentInfo;
import app.util.JobUtil;
import servent.handler.chaos_game.StopJobHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.UpdateMessage;
import servent.message.chaos_game.JobScheduleMessage;
import servent.message.lamport_mutex.ReleaseCriticalSectionMessage;
import servent.message.util.MessageUtil;

public class UpdateHandler implements MessageHandler {

	private Message clientMessage;
	private UpdateMessage updateMessage;

	private Map<Integer, ServentInfo> allNodes;
	private Map<Integer, FractalIdJob> serventJobsMap;
	private List<Job> activeJobs;
	
	public UpdateHandler(Message clientMessage) {
		this.clientMessage = clientMessage;

		updateMessage = (UpdateMessage) clientMessage;
		allNodes = updateMessage.getNodesMap();
		serventJobsMap = updateMessage.getServentJobsMap();
		activeJobs = updateMessage.getActiveJobs();
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() != MessageType.UPDATE) {
			AppConfig.timestampedErrorPrint("Update message handler got message that is not UPDATE");
			return;
		}

		allNodes.put(AppConfig.myServentInfo.getId(), AppConfig.myServentInfo);
		AppConfig.chordState.addNodes(allNodes);

		if (updateMessage.getSenderIpAddress().equals(AppConfig.myServentInfo.getIpAddress()) &&
				updateMessage.getSenderPort() == AppConfig.myServentInfo.getListenerPort()) { // update made a circle, came to the one who entered

			AppConfig.timestampedStandardPrint("Update message made a circle.");
			AppConfig.chordState.setServentJobs(serventJobsMap);	// set current job schedule
			AppConfig.chordState.addNewJobs(activeJobs);	// add all active jobs

			// todo: fix
//			this.sendRequestToReleaseCriticalSection();

			if (AppConfig.chordState.getActiveJobsCount() > 0) {        // need to reschedule jobs
				StopJobHandler.sendReschedulingMessage(JobScheduleType.SERVENT_ADDED);
			}
			return;
		}

		serventJobsMap = new HashMap<>(AppConfig.chordState.getServentJobs());
		activeJobs = new ArrayList<>(AppConfig.chordState.getActiveJobsList());
		// send to my first successor
		UpdateMessage nextUpdate = new UpdateMessage(updateMessage.getSenderPort(),
				AppConfig.chordState.getNextNodePort(), updateMessage.getSenderIpAddress(),
				AppConfig.chordState.getNextNodeIpAddress(), new HashMap<>(AppConfig.chordState.getAllNodeIdInfoMap()),
				serventJobsMap, activeJobs);
		MessageUtil.sendMessage(nextUpdate);
	}

	private void sendRequestToReleaseCriticalSection() {
		int receiverId = AppConfig.myServentInfo.getId() - 1;
		ServentInfo intercessorServent = AppConfig.chordState.getNextNodeForServentId(receiverId);

		ReleaseCriticalSectionMessage rcsm = new ReleaseCriticalSectionMessage(AppConfig.myServentInfo.getListenerPort(),
				intercessorServent.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
				intercessorServent.getIpAddress(), receiverId);
		MessageUtil.sendMessage(rcsm);
	}
}
