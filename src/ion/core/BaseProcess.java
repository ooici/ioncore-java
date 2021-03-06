package ion.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ion.core.messaging.IonMessage;
import ion.core.messaging.MessagingName;
import ion.core.messaging.MsgBrokerClient;
import net.ooici.core.container.Container;


/**
 * ioncore-java base process. 
 *
 * @author mmeisinger
 * @author Stephen Pasco
 * @author Chris Mueller
 */
public class BaseProcess {
    
    private static final Logger log = LoggerFactory.getLogger(BaseProcess.class);

    protected MessagingName mProcessId;
    protected MsgBrokerClient mBrokerClient;
    protected String mInQueue;

    public BaseProcess(String host, int portNumber, String exchangeTopic) {
        this(new MsgBrokerClient(host, portNumber, exchangeTopic));
    }

    public BaseProcess(MsgBrokerClient brokercl) {
        mBrokerClient = brokercl;
        mProcessId = MessagingName.generateUniqueName();
    }

    public void spawn() {
        mInQueue = mBrokerClient.declareQueue(null);
        mBrokerClient.bindQueue(mInQueue, mProcessId, null);
        mBrokerClient.attachConsumer(mInQueue);
        if (log.isDebugEnabled()) { 
            log.debug("Spawned process " + mProcessId);
        }
    }

    public void dispose() {
        mBrokerClient.detach();
    }

    public String getInQueue() {
        return mInQueue;
    }

    public MessagingName getMessagingName() {
        return mProcessId;
    }

    public void init() {
    }

    public void send(MessagingName to, String op, Object content) {
    	send(to, op, content, "ANONYMOUS", "0");
    }

    public void send(MessagingName to, String op, Object content, String userId, String expiry) {
        IonMessage msgout = mBrokerClient.createMessage(mProcessId, to, op, content);

        // Adjust the message headers and send
        msgout.getIonHeaders().put("user-id", userId);
        msgout.getIonHeaders().put("expiry", expiry);
        mBrokerClient.sendMessage(msgout);
    }

    public IonMessage rpcSend(MessagingName to, String op, Object content) {
    	return rpcSend(to, op, content, "ANONYMOUS", "0");
    }

    public IonMessage rpcSend(MessagingName to, String op, Object content, String userId, String expiry) {
        IonMessage msgout = mBrokerClient.createMessage(mProcessId, to, op, content);

        // Adjust the message headers and send
        msgout.getIonHeaders().put("user-id", userId);
        msgout.getIonHeaders().put("expiry", expiry);
        mBrokerClient.sendMessage(msgout);
        IonMessage msgin = mBrokerClient.consumeMessage(mInQueue);
        return msgin;
    }

    public IonMessage rpcSend(IonMessage msg) {
    	return rpcSend(msg, "ANONYMOUS", "0");
    }

    public IonMessage rpcSend(IonMessage msg, String userId, String expiry) {
    	msg.getIonHeaders().put("user-id", userId);
    	msg.getIonHeaders().put("expiry", expiry);
        mBrokerClient.sendMessage(msg);
        IonMessage msgin = mBrokerClient.consumeMessage(mInQueue);
        return msgin;
    }

    /**
     * Method that performs message send and consume message on behalf of the caller.
     * Content is a Protocol Buffer Container.Structure object.  Default anonymous
     * user id is passed in the message header.
     * 
     * @param to target service
     * @param op target service's operation
     * @param structure message payload
     * @return response message
     */
    public IonMessage rpcSendContainerContent(MessagingName to, String op, Container.Structure structure) {
    	return rpcSendContainerContent(to, op, structure, "ANONYMOUS", "0");
    }

    /**
     * Method that performs message send and consume message on behalf of the caller.
     * Content is a Protocol Buffer Container.Structure object.
     * 
     * @param to target service
     * @param op target service's operation
     * @param structure message payload
     * @param userId OOID user id
     * @return response message
     */
    public IonMessage rpcSendContainerContent(MessagingName to, String op, Container.Structure structure, String userId, String expiry) {
    	
        IonMessage msgout = mBrokerClient.createMessage(mProcessId, to, op, structure.toByteArray());

        // Adjust the message headers and send
        msgout.getIonHeaders().put("encoding", "ION R1 GPB");
        msgout.getIonHeaders().put("user-id", userId);
        msgout.getIonHeaders().put("expiry", expiry);
        mBrokerClient.sendMessage(msgout);
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
