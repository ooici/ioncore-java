package ion.core;

import ion.core.messaging.MessagingName;
import ion.core.messaging.MsgBrokerAttachment;


public class BaseProcess {
	MessagingName mProcessId;
	MsgBrokerAttachment mBrokerAttach;
	
	public BaseProcess(MsgBrokerAttachment brokeratt) {
		mBrokerAttach = brokeratt;
		mProcessId = MessagingName.generateUniqueName();
	}
	
	public void spawn() {
		
	}
	
	public void init() {
		
	}
}
