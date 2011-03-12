package ion.integration.ais;

import ion.core.BaseProcess;
import ion.core.IonBootstrap;
import ion.core.messaging.IonMessage;
import ion.core.messaging.MessagingName;
import ion.core.messaging.MsgBrokerClient;
import ion.core.utils.GPBWrapper;
import ion.core.utils.ProtoUtils;
import ion.core.utils.StructureManager;

import java.lang.reflect.Method;
import java.util.HashMap;

import net.ooici.core.container.Container;
import net.ooici.core.container.Container.Structure;
import net.ooici.core.message.IonMessage.IonMsg;
import net.ooici.integration.ais.AisRequestResponse.ApplicationIntegrationServiceError;
import net.ooici.integration.ais.AisRequestResponse.ApplicationIntegrationServiceRequestMsg;
import net.ooici.integration.ais.AisRequestResponse.ApplicationIntegrationServiceResponseMsg;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.JsonFormat;
import com.google.protobuf.Message;
import com.rabbitmq.client.AMQP;

public class App_Integration_Service extends BaseProcess {
	
	private static final String SYSNAME = System.getProperty("ioncore.sysname","Tom");
	// TODO point to real AIS messaging name
    private static final MessagingName applicationIntegrationSvc = new MessagingName(SYSNAME, "echo_service");

    // TODO fill out enum
    public enum RequestType {
    	REGISTER_USER
    }

    // Map of RequestType enum values to payload GPB id values
    private static final HashMap<RequestType,Integer> typeEnumToTypeIntMap = new HashMap<RequestType,Integer>();

    // Map of RequestType enum values to AIS service op name values
    private static final HashMap<RequestType,String> typeEnumToServiceOpMap = new HashMap<RequestType,String>();

    // Map enum values to int GPB type ids
    // TODO define mappings.  Currently using echo service to validate functionality.
    static {
    	typeEnumToTypeIntMap.put(RequestType.REGISTER_USER, 20011);
    	typeEnumToServiceOpMap.put(RequestType.REGISTER_USER, "echo_msg");
    }
    
	public App_Integration_Service(MsgBrokerClient brokeratt) {
		super(brokeratt);
	}

	/**
	 * Method to be called from UI to send requests to ION Core Application Integration Service.  Input
	 * is in Json string format.  Results is returned in Json string format.
	 * 
	 * @param jsonRequest input to be send in GPB to AIS
	 * @param reqType enum value indicating specific operation being requested
	 * @param userId for enforcing policy.  OOID or 'ANONYMOUS'
	 * @param expiry for enforcing policy.  Expiry of certificate represented (time since epoc in seconds) as a string or '0'
	 * @return Json formatted string containing good results or error information.
	 */
	public String sendReceiveUIRequest(String jsonRequest, RequestType reqType, String userId, String expiry) {

    	Integer gpbId = typeEnumToTypeIntMap.get(reqType);
    	assert(gpbId != null);

    	String serviceOperation = typeEnumToServiceOpMap.get(reqType);
    	assert(serviceOperation != null);
    	
    	return sendReceive(jsonRequest, gpbId, serviceOperation, userId, expiry);
	}

	public String sendReceive(String jsonRequest, int gpbId, String serviceOperation, String userId, String expiry) {

		// Convert Json "payload" data to appropriate GPB format
		GeneratedMessage payload = convertJsonToGPB(jsonRequest, gpbId);
		assert(payload != null);

		Structure requestMessage = packageRequestMessage(payload, gpbId);
		
		IonMessage replyMessage = rpcSendContainerContent(applicationIntegrationSvc, serviceOperation, requestMessage);
		
		return unpackageResponseMessage(replyMessage);
	}

    public GeneratedMessage convertJsonToGPB(String jsonRequest, int typeInt) {
    	assert(jsonRequest != null && jsonRequest.length() > 0);

    	try {
    		// Get GeneratedMessage class for type id
        	Class clazz = IonBootstrap.getMappedClassForKeyValue(typeInt);
        	assert(clazz != null);

    		// Get builder instance by invoking static newBuilder() via reflection
    		Method method = clazz.getMethod("newBuilder", null);
    		Message.Builder builder = (Message.Builder)method.invoke(null, (Object[])null);

    		// Copy Json into GPB
    		JsonFormat.merge(jsonRequest, builder);
    		
    		return (GeneratedMessage)builder.build();
    	} catch(Exception e) {
    		System.out.println("Exception: " + e);
    	}

    	return null;
    }
    
    public Structure packageRequestMessage(GeneratedMessage payload, int typeInt) {
    	// Wrap payload
    	GPBWrapper payloadWrapper = GPBWrapper.Factory(payload);
    	
    	// Construct ApplicationIntegrationServiceRequestMsg GPB
    	ApplicationIntegrationServiceRequestMsg.Builder aisrmBuilder = ApplicationIntegrationServiceRequestMsg.newBuilder();
    	aisrmBuilder.setMessageParametersReference(payloadWrapper.getCASRef());
    	
    	// Wrap request message
    	GPBWrapper aisrmWrapper = GPBWrapper.Factory(aisrmBuilder.build());

    	// Add ApplicationIntegrationServieRequestMsg and payload to items list
        Container.Structure.Builder structBldr = ProtoUtils.addStructureElementToStructureBuilder(null, aisrmWrapper.getStructureElement(), false);
        ProtoUtils.addStructureElementToStructureBuilder(structBldr, payloadWrapper.getStructureElement(), false);

        // Construct IonMsg
        IonMsg.Builder ionMsgBuilder = IonMsg.newBuilder();
        ionMsgBuilder.setName("UI Request");
        ionMsgBuilder.setIdentity("1");
        ionMsgBuilder.setMessageObject(aisrmWrapper.getCASRef());

        // Wrap IonMsg
        GPBWrapper ionMsgWrapper = GPBWrapper.Factory(ionMsgBuilder.build());
        
        // Add IonMsg to head
        ProtoUtils.addStructureElementToStructureBuilder(structBldr, ionMsgWrapper.getStructureElement(), true);
        
        return structBldr.build();
    }

    public String unpackageResponseMessage(IonMessage replyMessage) {
        StructureManager sm = StructureManager.Factory(replyMessage);
        
        // Extract the IonMsg from the head
        assert(sm.getHeadIds().size() == 1);
        String ionMsgKey = sm.getHeadIds().get(0);
        GPBWrapper msgWrapper = sm.getObjectWrapper(ionMsgKey);
        assert(msgWrapper.getObjectValue() instanceof IonMsg);
        IonMsg ionMsg = (IonMsg)msgWrapper.getObjectValue();

        // Extract the items
        // First item should be ApplicationIntegrationServiceResponseMsg
        // or ApplicationIntegrationServiceError
        assert(sm.getItemIds().size() != 0);
        String responseMsgKey = sm.getItemIds().get(0);
        GPBWrapper responseMsgWrapper = sm.getObjectWrapper(responseMsgKey);
        assert(responseMsgWrapper.getObjectValue() instanceof ApplicationIntegrationServiceResponseMsg
        		|| responseMsgWrapper.getObjectValue() instanceof ApplicationIntegrationServiceError);
        
        // If error response, convert info and return
        if (responseMsgWrapper.getObjectValue() instanceof ApplicationIntegrationServiceError) {
        	ApplicationIntegrationServiceError responseMsg = (ApplicationIntegrationServiceError)responseMsgWrapper.getObjectValue();
        	String errorInfo = JsonFormat.printToString(responseMsg);
        	return errorInfo;
        }

        // Else, good response. Get payload object.
        assert(sm.getItemIds().size() > 1);
        String payloadKey = sm.getItemIds().get(1);
        GPBWrapper payloadWrapper = sm.getObjectWrapper(payloadKey);
        GeneratedMessage payloadMessage = (GeneratedMessage)payloadWrapper.getObjectValue();
    	String payloadInfo = JsonFormat.printToString(payloadMessage);
    	return payloadInfo;
    }

	public static void main(String[] args) {
        String hostName = "localhost";
        int portNumber = AMQP.PROTOCOL.PORT;
        String exchange = "magnet.topic";
        
        MsgBrokerClient ionClient = new MsgBrokerClient(hostName, portNumber, exchange);
        ionClient.attach();

        App_Integration_Service proc = new App_Integration_Service(ionClient);
        proc.spawn();
        
        String requestJsonString = "{\"lon\":2.2,\"time\":4.4,\"depth\":3.3,\"lat\":1.1}";
        System.out.println("requestJsonString: <" + requestJsonString + ">");
        String replyJsonString = proc.sendReceiveUIRequest(requestJsonString, RequestType.REGISTER_USER, "ANONYMOUS", "0");
        System.out.println("replyJsonString: " + replyJsonString);
    }
	
	
	
	
	
}