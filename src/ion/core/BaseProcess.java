package ion.core;

import ion.core.messaging.IonMessage;
import ion.core.messaging.MessagingName;
import ion.core.messaging.MsgBrokerClient;


public class BaseProcess {
	MessagingName mProcessId;
	MsgBrokerClient mBrokerClient;
	String mInQueue;
	
	public BaseProcess(MsgBrokerClient brokercl) {
		mBrokerClient = brokercl;
		mProcessId = MessagingName.generateUniqueName();
	}
	
	public void spawn() {
		mInQueue = mBrokerClient.declareQueue(null);
        mBrokerClient.bindQueue(mInQueue, mProcessId, null);
        mBrokerClient.attachConsumer(mInQueue);
        System.out.println("Spawned process " + mProcessId);
	}
	
	public void init() {
		
	}
	
	public void send(MessagingName to, String op, Object content) {
		mBrokerClient.createSendMessage(mProcessId, to, op, content);
	}

	public IonMessage rpcSend(MessagingName to, String op, Object content) {
		mBrokerClient.createSendMessage(mProcessId, to, op, content);
        IonMessage msgin = mBrokerClient.consumeMessage(mInQueue);
        return msgin;
	}

	public IonMessage receive() {
        IonMessage msgin = mBrokerClient.consumeMessage(mInQueue);
        return msgin;
	}
	
	public void ackMessage(IonMessage msg) {
		mBrokerClient.ackMessage(msg);
	}
}
