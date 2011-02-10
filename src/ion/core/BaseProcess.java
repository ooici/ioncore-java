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
        mBrokerClient.createSendMessage(mProcessId, to, op, content);
        IonMessage msgin = mBrokerClient.consumeMessage(mInQueue);
        return msgin;
    }

 // NEW CODE STARTS HERE

    /**
     * Method that performs message send and consume message on behalf of the caller.
     * Content is a Protocol Buffer Container.Structure object.
     * 
     * @param to target service
     * @param op target service's operation
     * @param structure message payload
     * @return response message
     */
    public IonMessage rpcSendContainerContent(MessagingName to, String op, Container.Structure structure, Class clazz) {
    	
        IonMessage msgout = mBrokerClient.createMessage(mProcessId, to, op, structure.toByteArray());

        // Adjust the message headers and send
        msgout.getIonHeaders().put("encoding", "ION R1 GPB");
        mBrokerClient.sendMessage(msgout);
        IonMessage msgin = mBrokerClient.consumeMessage(mInQueue);
        return msgin;
    }

 // NEW CODE ENDS HERE

    /**
     * Method that performs message send and consume message on behalf of the caller.
     * Ref and conf parameters are treated as already encoded ByteStrings.
     * 
     * @param to target service
     * @param op target service's operation
     * @param reference link or id of object of interest
     * @param configuration already ByteString encoded
     * @param lifecycle state
     * @return response message
     */
//    public IonMessage rpcSendEncodedContent(MessagingName to, String op, ByteString reference, ByteString configuration, LifeCycleOperation cycle) {
//    	// Fill in ResourceConfigurationRequest
//    	ResourceRequest.ResourceConfigurationRequest.Builder resourceRequest = ResourceRequest.ResourceConfigurationRequest.newBuilder();
//    	
//    	// Wrap the data to be sent
//    	Container.Structure.Builder structure = Container.Structure.newBuilder();
//
//    	Container.StructureElement.Builder structureElement = Container.StructureElement.newBuilder();
//    	structureElement.setKey(com.google.protobuf.ByteString.copyFrom(ProtoUtils.getObjectKey(content.toByteArray(),ProtoUtils.getGPBType(clazz))));
//    	structureElement.setType(ProtoUtils.getGPBType(clazz));
//    	structureElement.setIsleaf(true);
//    	structureElement.setValue(content);
//    	
//    	structure.setHead(structureElement.build());
//    	Container.Structure builtStructure = structure.build();
//    	
//        IonMessage msgout = mBrokerClient.createMessage(mProcessId, to, op, builtStructure.toByteArray());
//
//        // Adjust the message headers and send
//        msgout.getIonHeaders().put("encoding", "ION R1 GPB");
//        mBrokerClient.sendMessage(msgout);
//        IonMessage msgin = mBrokerClient.consumeMessage(mInQueue);
//        return msgin;
//    }

    public IonMessage rpcSend(IonMessage msg) {
        mBrokerClient.sendMessage(msg);
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
//    
//    public static void main(String[] argv) {
//    	String hostName = "localhost";
//        int portNumber = 5672;
//        String exchange = "magnet.topic";
//
// 		// Messaging environment
//        MsgBrokerClient ionClient = new MsgBrokerClient(hostName, portNumber, exchange);
//        ionClient.attach();
//
//        BaseProcess baseProcess = new BaseProcess(ionClient);
//  		baseProcess.spawn();
//
//		DatasetEntryMessage.Builder dataResource = DatasetEntryMessage.newBuilder();
//		
//		dataResource.setProvider("provider1");
//		dataResource.setFormat("format1");
//		dataResource.setProtocol("protocol1");
//		dataResource.setType("type1");
//		dataResource.setTitle("title1");
//		dataResource.setDataType("data_type1");
//		dataResource.setNamingAuthority("naming_authority1");
//		
//		DatasetEntryMessage.Person.Builder publisher = DatasetEntryMessage.Person.newBuilder();
//		publisher.setName("publisher_name1");
//		publisher.setEmail("publisher_email1");
//		publisher.setWebsite("publisher_website1");
//		publisher.setInstitution("publisher_institution1");
//		dataResource.setPublisher(publisher);
//		
//		DatasetEntryMessage.Person.Builder creator = DatasetEntryMessage.Person.newBuilder();
//		creator.setName("creator_name1");
//		creator.setEmail("creator_email1");
//		creator.setWebsite("creator_website1");
//		creator.setInstitution("creator_institution1");
//		dataResource.setCreator(creator);
//		
//		MessagingName r1intSvc = new MessagingName("Tom", "r1integration");
//        
//        IonMessage msgin = baseProcess.rpcSendContainerContent(r1intSvc, "createDataResource", dataResource.build().toByteString(), DatasetEntryMessage.class);
//
//        if (msgin.isErrorMessage()) {
//        	System.out.println("Error text: " + msgin.getContent());
//        }
//        else {
//        	System.out.println("UUID text: " + msgin.getContent());
//        }
//    }
}
