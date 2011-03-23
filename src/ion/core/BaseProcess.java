package ion.core;

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
        System.out.println("Spawned process " + mProcessId);
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
        mBrokerClient.createSendMessage(mProcessId, to, op, content);
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
