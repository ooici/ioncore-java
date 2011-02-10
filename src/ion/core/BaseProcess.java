package ion.core;

import net.ooici.core.container.Container;
import ion.core.messaging.IonMessage;
import ion.core.messaging.MessagingName;
import ion.core.messaging.MsgBrokerClient;

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
        mBrokerClient.createSendMessage(mProcessId, to, op, content);
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
     * Content is a Protocol Buffer Container.Structure object.
     * 
     * @param to target service
     * @param op target service's operation
     * @param structure message payload
     * @return response message
     */
    public IonMessage rpcSendContainerContent(MessagingName to, String op, Container.Structure structure) {
    	
        IonMessage msgout = mBrokerClient.createMessage(mProcessId, to, op, structure.toByteArray());

        // Adjust the message headers and send
        msgout.getIonHeaders().put("encoding", "ION R1 GPB");
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
