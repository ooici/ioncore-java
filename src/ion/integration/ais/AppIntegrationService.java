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
import net.ooici.integration.ais.findDataResources.FindDataResources.FindDataResourcesRspMsg;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.JsonFormat;
import com.google.protobuf.Message;
import com.rabbitmq.client.AMQP;

public class AppIntegrationService {
	
	private static final String AIS_SERVICE_NAME = "app_integration";
    private MessagingName applicationIntegrationSvc;

    private MsgBrokerClient msgBrokerClient;
    private BaseProcess baseProcess;
    
    private String errorMessage = "";

    // TODO fill out enum
    // Enum used by UI to indicate to this library what operation is
    // associated with the passed Json data.
    public enum RequestType {
    	CREATE_DOWNLOAD_URL, FIND_DATA_RESOURCES, GET_DATA_RESOURCE_DETAIL,
    	CREATE_DATA_RESOURCE, UPDATE_DATA_RESOURCE, DELETE_DATA_RESOURCE,
    	REGISTER_USER, UPDATE_USER_EMAIL, UPDATE_USER_DISPATCHER_QUEUE
    }

    // Map of RequestType enum values to payload GPB id values
    private static final HashMap<RequestType,Integer> typeEnumToTypeIntMap = new HashMap<RequestType,Integer>();

    // Map of RequestType enum values to AIS service op name values
    private static final HashMap<RequestType,String> typeEnumToServiceOpMap = new HashMap<RequestType,String>();

    // Map enum values to int GPB type ids
    // TODO define all mappings.  Currently using echo service to validate functionality.
    static {
    	typeEnumToTypeIntMap.put(RequestType.CREATE_DOWNLOAD_URL, -1);
    	typeEnumToServiceOpMap.put(RequestType.CREATE_DOWNLOAD_URL, "createDownloadURL");
    	
    	typeEnumToTypeIntMap.put(RequestType.FIND_DATA_RESOURCES, 9031);
    	typeEnumToServiceOpMap.put(RequestType.FIND_DATA_RESOURCES, "findDataResources");
    	
    	typeEnumToTypeIntMap.put(RequestType.GET_DATA_RESOURCE_DETAIL, -1);
    	typeEnumToServiceOpMap.put(RequestType.GET_DATA_RESOURCE_DETAIL, "getDataResourceDetail");
    	
    	typeEnumToTypeIntMap.put(RequestType.CREATE_DATA_RESOURCE, 9211);
    	typeEnumToServiceOpMap.put(RequestType.CREATE_DATA_RESOURCE, "createDataResource");
    	
    	typeEnumToTypeIntMap.put(RequestType.UPDATE_DATA_RESOURCE, 9211);
    	typeEnumToServiceOpMap.put(RequestType.UPDATE_DATA_RESOURCE, "updateDataResource");
    	
    	typeEnumToTypeIntMap.put(RequestType.DELETE_DATA_RESOURCE, 9213);
    	typeEnumToServiceOpMap.put(RequestType.DELETE_DATA_RESOURCE, "deleteDataResource");
    	
    	typeEnumToTypeIntMap.put(RequestType.REGISTER_USER, 9101);
    	typeEnumToServiceOpMap.put(RequestType.REGISTER_USER, "registerUser");
    	
    	typeEnumToTypeIntMap.put(RequestType.UPDATE_USER_EMAIL, -1);
    	typeEnumToServiceOpMap.put(RequestType.UPDATE_USER_EMAIL, "updateUserEmail");
    	
    	typeEnumToTypeIntMap.put(RequestType.UPDATE_USER_DISPATCHER_QUEUE, -1);
    	typeEnumToServiceOpMap.put(RequestType.UPDATE_USER_DISPATCHER_QUEUE, "updateUserDispatcherQueue");
    }

    /**
     * Constructor taking the full set of parameters required to target
     * requests to the Application Integration Service running in a specific
     * capability container with topic being hosted on a specific node.
     * 
     * @param sysName system name under which the ION Core Python capability container is
     * running
     * @param hostName machine hosting the broker
     * @param portNumber port number of the broker
     * @param exchange name of the exchange being hosted by the broker
     */
	public AppIntegrationService(String sysName, String hostName, int portNumber, String exchange) {
		msgBrokerClient = new MsgBrokerClient(hostName, portNumber, exchange);
		msgBrokerClient.attach();
		baseProcess = new BaseProcess(msgBrokerClient);
		baseProcess.spawn();
		createMessagingName(sysName);
	}

	public AppIntegrationService(String sysName, MsgBrokerClient brokerClient) {
		baseProcess = new BaseProcess(brokerClient);
		baseProcess.spawn();
		createMessagingName(sysName);
	}

	public AppIntegrationService(String sysName, BaseProcess baseProcess) {
		this.baseProcess = baseProcess;
		createMessagingName(sysName);
	}

	public void dispose() {
		baseProcess.dispose();
	}

	private void createMessagingName(String sysName) {
		applicationIntegrationSvc = new MessagingName(sysName, AIS_SERVICE_NAME);
	}

	/**
	 * Method to be called from UI to send requests to ION Core Application Integration
	 * Service.  Input is in Json string format.  Results is returned to UI in Json
	 * string format.
	 * 
	 * @param jsonRequest input to be send in GPB to Application Integration Service
	 * @param reqType enum value indicating specific operation being requested
	 * @param userId for enforcing policy.  OOID or 'ANONYMOUS'
	 * @param expiry for enforcing policy.  Expiry of certificate represented (time since epoc in seconds) as a string or '0'
	 * @return Json formatted string containing good results or error information.
	 */
	public String sendReceiveUIRequest(String jsonRequest, RequestType reqType, String userId, String expiry) {

    	Integer gpbId = typeEnumToTypeIntMap.get(reqType);
    	assert(gpbId != null && gpbId != -1);

    	String serviceOperation = typeEnumToServiceOpMap.get(reqType);
    	assert(serviceOperation != null);
    	
    	return sendReceive(jsonRequest, gpbId, serviceOperation, userId, expiry);
	}

	public String sendReceiveUIRequestCanned() {
		String requestJsonString = "{\"user_ooi_id\": \"3f27a744-2c3e-4d2a-a98c-050b246334a3\",\"minLatitude\": 32.87521,\"maxLatitude\": 32.97521,\"minLongitude\": -117.274609,\"maxLongitude\": -117.174609,\"minDepth\": 5.5,\"maxDepth\": 6.6,\"minTime\": 7.7,\"maxTime\": 8.8,\"identity\": \"ad0adf8b-4a7e-43e1-8cc0-371fb9057664\"}";
        String replyJsonString = sendReceiveUIRequest(requestJsonString, RequestType.FIND_DATA_RESOURCES, "3f27a744-2c3e-4d2a-a98c-050b246334a3", "0");
        return replyJsonString;
	}

	public String sendReceive(String jsonRequest, int gpbId, String serviceOperation, String userId, String expiry) {

		// Convert Json "payload" data to appropriate GPB format
		GeneratedMessage payload = convertJsonToGPB(jsonRequest, gpbId);
		if (payload == null) {
			return errorMessage;
		}

		Structure requestMessage = packageRequestMessage(payload, gpbId);
		
		IonMessage replyMessage = baseProcess.rpcSendContainerContent(applicationIntegrationSvc, serviceOperation, requestMessage);
		
		if (replyMessage == null) {
    		return "Request timeout";
		}
		
		return unpackageResponseMessage(replyMessage);
	}

    public GeneratedMessage convertJsonToGPB(String jsonRequest, int typeInt) {
    	assert(jsonRequest != null && jsonRequest.length() > 0);

    	try {
    		// Get GeneratedMessage class for type id
        	Class clazz = IonBootstrap.getMappedClassForKeyValue(typeInt);
        	assert(clazz != null);

    		// Get builder instance by invoking static newBuilder() via reflection
    		Method method = clazz.getMethod("newBuilder", (Class[])null);
    		Message.Builder builder = (Message.Builder)method.invoke(null, (Object[])null);

    		// Copy Json into GPB
    		JsonFormat.merge(jsonRequest, builder);
    		
    		return (GeneratedMessage)builder.build();
    	} catch(Exception e) {
    		errorMessage = "Exception encoding JSON to GPB: " + e;
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
        assert(sm.getHeadId() != null);
        String ionMsgKey = sm.getHeadId();
        GPBWrapper msgWrapper = sm.getObjectWrapper(ionMsgKey);
        assert(msgWrapper.getObjectValue() instanceof IonMsg);
        IonMsg ionMsg = (IonMsg)msgWrapper.getObjectValue();

        // Extract the items
        // First find the ApplicationIntegrationServiceResponseMsg
        // or ApplicationIntegrationServiceError proto buff
        assert(sm.getItemIds().size() != 0);
        for (String itemKey : sm.getItemIds()) {
            msgWrapper = sm.getObjectWrapper(itemKey);

            // If error response, convert info and return
            if (msgWrapper.getObjectValue() instanceof ApplicationIntegrationServiceError) {
            	ApplicationIntegrationServiceError responseMsg = (ApplicationIntegrationServiceError)msgWrapper.getObjectValue();
            	String errorInfo = JsonFormat.printToString(responseMsg);
            	return errorInfo;
            }
        }
        
        // Now get payload
        assert(sm.getItemIds().size() > 1);
        boolean createList = sm.getItemIds().size() > 2 ? true : false;
        String payloadInfo = createList ? "[" : "";
        boolean firstTime = true;
        for (String itemKey : sm.getItemIds()) {
            msgWrapper = sm.getObjectWrapper(itemKey);

            // If error response, convert info and return
            if (msgWrapper.getObjectValue() instanceof ApplicationIntegrationServiceResponseMsg
            	|| msgWrapper.getObjectValue() instanceof ApplicationIntegrationServiceError
            	|| msgWrapper.getObjectValue() instanceof FindDataResourcesRspMsg) {
            	continue;
            }
            GPBWrapper payloadWrapper = sm.getObjectWrapper(itemKey);
            GeneratedMessage payloadMessage = (GeneratedMessage)payloadWrapper.getObjectValue();
        	if (createList && !firstTime) {
        		payloadInfo += ", ";
        	}
            payloadInfo += JsonFormat.printToString(payloadMessage);
            if (firstTime) {
            	firstTime = false;
            }
        }
        if (createList) {
        	payloadInfo += "]";
        }
        return payloadInfo;
    }

	public static void main(String[] args) {
		String sysName = "Tom";
        String hostName = "localhost";
        int portNumber = AMQP.PROTOCOL.PORT;
        String exchange = "magnet.topic";

    	String certificate = "-----BEGIN CERTIFICATE-----\\n" +
    		"MIIEMzCCAxugAwIBAgICBQAwDQYJKoZIhvcNAQEFBQAwajETMBEGCgmSJomT8ixkARkWA29yZzEX\\n" +
    		"MBUGCgmSJomT8ixkARkWB2NpbG9nb24xCzAJBgNVBAYTAlVTMRAwDgYDVQQKEwdDSUxvZ29uMRsw\\n" +
    		"GQYDVQQDExJDSUxvZ29uIEJhc2ljIENBIDEwHhcNMTAxMTE4MjIyNTA2WhcNMTAxMTE5MTAzMDA2\\n" +
    		"WjBvMRMwEQYKCZImiZPyLGQBGRMDb3JnMRcwFQYKCZImiZPyLGQBGRMHY2lsb2dvbjELMAkGA1UE\\n" +
    		"BhMCVVMxFzAVBgNVBAoTDlByb3RlY3ROZXR3b3JrMRkwFwYDVQQDExBSb2dlciBVbndpbiBBMjU0\\n" +
    		"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6QhsWxhUXbIxg+1ZyEc7d+hIGvchVmtb\\n" +
    		"g0kKLmivgoVsA4U7swNDRH6svW242THta0oTf6crkRx7kOKg6jma2lcAC1sjOSddqX7/92ChoUPq\\n" +
    		"7LWt2T6GVVA10ex5WAeB/o7br/Z4U8/75uCBis+ru7xEDl09PToK20mrkcz9M4HqIv1eSoPkrs3b\\n" +
    		"2lUtQc6cjuHRDU4NknXaVMXTBHKPM40UxEDHJueFyCiZJFg3lvQuSsAl4JL5Z8pC02T8/bODBuf4\\n" +
    		"dszsqn2SC8YDw1xrujvW2Bd7Q7BwMQ/gO+dZKM1mLJFpfEsR9WrjMeg6vkD2TMWLMr0/WIkGC8u+\\n" +
    		"6M6SMQIDAQABo4HdMIHaMAwGA1UdEwEB/wQCMAAwDgYDVR0PAQH/BAQDAgSwMBMGA1UdJQQMMAoG\\n" +
    		"CCsGAQUFBwMCMBgGA1UdIAQRMA8wDQYLKwYBBAGCkTYBAgEwagYDVR0fBGMwYTAuoCygKoYoaHR0\\n" +
    		"cDovL2NybC5jaWxvZ29uLm9yZy9jaWxvZ29uLWJhc2ljLmNybDAvoC2gK4YpaHR0cDovL2NybC5k\\n" +
    		"b2Vncmlkcy5vcmcvY2lsb2dvbi1iYXNpYy5jcmwwHwYDVR0RBBgwFoEUaXRzYWdyZWVuMUB5YWhv\\n" +
    		"by5jb20wDQYJKoZIhvcNAQEFBQADggEBAEYHQPMY9Grs19MHxUzMwXp1GzCKhGpgyVKJKW86PJlr\\n" +
    		"HGruoWvx+DLNX75Oj5FC4t8bOUQVQusZGeGSEGegzzfIeOI/jWP1UtIjzvTFDq3tQMNvsgROSCx5\\n" +
    		"CkpK4nS0kbwLux+zI7BWON97UpMIzEeE05pd7SmNAETuWRsHMP+x6i7hoUp/uad4DwbzNUGIotdK\\n" +
    		"f8b270icOVgkOKRdLP/Q4r/x8skKSCRz1ZsRdR+7+B/EgksAJj7Ut3yiWoUekEMxCaTdAHPTMD/g\\n" +
    		"Mh9xL90hfMJyoGemjJswG5g3fAdTP/Lv0I6/nWeH/cLjwwpQgIEjEAVXl7KHuzX5vPD/wqQ=\\n" +
    		"-----END CERTIFICATE-----";
    	String privateKey = "-----BEGIN RSA PRIVATE KEY-----\\n" +
    		"MIIEowIBAAKCAQEA6QhsWxhUXbIxg+1ZyEc7d+hIGvchVmtbg0kKLmivgoVsA4U7swNDRH6svW24\\n" +
    		"2THta0oTf6crkRx7kOKg6jma2lcAC1sjOSddqX7/92ChoUPq7LWt2T6GVVA10ex5WAeB/o7br/Z4\\n" +
    		"U8/75uCBis+ru7xEDl09PToK20mrkcz9M4HqIv1eSoPkrs3b2lUtQc6cjuHRDU4NknXaVMXTBHKP\\n" +
    		"M40UxEDHJueFyCiZJFg3lvQuSsAl4JL5Z8pC02T8/bODBuf4dszsqn2SC8YDw1xrujvW2Bd7Q7Bw\\n" +
    		"MQ/gO+dZKM1mLJFpfEsR9WrjMeg6vkD2TMWLMr0/WIkGC8u+6M6SMQIDAQABAoIBAAc/Ic97ZDQ9\\n" +
    		"tFh76wzVWj4SVRuxj7HWSNQ+Uzi6PKr8Zy182Sxp74+TuN9zKAppCQ8LEKwpkKtEjXsl8QcXn38m\\n" +
    		"sXOo8+F1He6FaoRQ1vXi3M1boPpefWLtyZ6rkeJw6VP3MVG5gmho0VaOqLieWKLP6fXgZGUhBvFm\\n" +
    		"yxUPoNgXJPLjJ9pNGy4IBuQDudqfJeqnbIe0GOXdB1oLCjAgZlTR4lFA92OrkMEldyVp72iYbffN\\n" +
    		"4GqoCEiHi8lX9m2kvwiQKRnfH1dLnnPBrrwatu7TxOs02HpJ99wfzKRy4B1SKcB0Gs22761r+N/M\\n" +
    		"oO966VxlkKYTN+soN5ID9mQmXJkCgYEA/h2bqH9mNzHhzS21x8mC6n+MTyYYKVlEW4VSJ3TyMKlR\\n" +
    		"gAjhxY/LUNeVpfxm2fY8tvQecWaW3mYQLfnvM7f1FeNJwEwIkS/yaeNmcRC6HK/hHeE87+fNVW/U\\n" +
    		"ftU4FW5Krg3QIYxcTL2vL3JU4Auu3E/XVcx0iqYMGZMEEDOcQPcCgYEA6sLLIeOdngUvxdA4KKEe\\n" +
    		"qInDpa/coWbtAlGJv8NueYTuD3BYJG5KoWFY4TVfjQsBgdxNxHzxb5l9PrFLm9mRn3iiR/2EpQke\\n" +
    		"qJzs87K0A/sxTVES29w1PKinkBkdu8pNk10TxtRUl/Ox3fuuZPvyt9hi5c5O/MCKJbjmyJHuJBcC\\n" +
    		"gYBiAJM2oaOPJ9q4oadYnLuzqms3Xy60S6wUS8+KTgzVfYdkBIjmA3XbALnDIRudddymhnFzNKh8\\n" +
    		"rwoQYTLCVHDd9yFLW0d2jvJDqiKo+lV8mMwOFP7GWzSSfaWLILoXcci1ZbheJ9607faxKrvXCEpw\\n" +
    		"xw36FfbgPfeuqUdI5E6fswKBgFIxCu99gnSNulEWemL3LgWx3fbHYIZ9w6MZKxIheS9AdByhp6px\\n" +
    		"lt1zeKu4hRCbdtaha/TMDbeV1Hy7lA4nmU1s7dwojWU+kSZVcrxLp6zxKCy6otCpA1aOccQIlxll\\n" +
    		"Vc2vO7pUIp3kqzRd5ovijfMB5nYwygTB4FwepWY5eVfXAoGBAIqrLKhRzdpGL0Vp2jwtJJiMShKm\\n" +
    		"WJ1c7fBskgAVk8jJzbEgMxuVeurioYqj0Cn7hFQoLc+npdU5byRti+4xjZBXSmmjo4Y7ttXGvBrf\\n" +
    		"c2bPOQRAYZyD2o+/MHBDsz7RWZJoZiI+SJJuE4wphGUsEbI2Ger1QW9135jKp6BsY2qZ\\n" +
    		"-----END RSA PRIVATE KEY-----";

    	AppIntegrationService ais = new AppIntegrationService(sysName, hostName, portNumber, exchange);

		String requestJsonString = "{\"certificate\": \"" + certificate + "\",";
		requestJsonString += "\"rsa_private_key\": \"" + privateKey + "\"}";
        System.out.println("requestJsonString: <" + requestJsonString + ">");
        String replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.REGISTER_USER, "ANONYMOUS", "0");
        System.out.println("replyJsonString:   <" + replyJsonString + ">");
        
        ais.dispose();
    }
	
}