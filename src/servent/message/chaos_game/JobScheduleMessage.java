package servent.message.chaos_game;

import app.models.JobScheduleType;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class JobScheduleMessage extends BasicMessage {

    private static final long serialVersionUID = 138036264911712177L;

    private JobScheduleType scheduleType;
    private int finalReceiverId;

    public JobScheduleMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                              JobScheduleType scheduleType, int finalReceiverId) {
        super(MessageType.JOB_SCHEDULE, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.scheduleType = scheduleType;
        this.finalReceiverId = finalReceiverId;
    }

    public JobScheduleType getScheduleType() { return scheduleType; }

    public int getFinalReceiverId() { return finalReceiverId; }
}
