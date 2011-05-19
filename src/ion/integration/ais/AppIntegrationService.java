package ion.integration.ais;

import ion.core.BaseProcess;
import ion.core.IonBootstrap;
import ion.core.messaging.IonMessage;
import ion.core.messaging.MessagingName;
import ion.core.messaging.MsgBrokerClient;
import ion.core.utils.GPBWrapper;
import ion.core.utils.ProtoUtils;
import ion.core.utils.StructureManager;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.ooici.core.container.Container;
import net.ooici.core.container.Container.Structure;
import net.ooici.core.message.IonMessage.IonMsg;
import net.ooici.core.message.IonMessage.ResponseCodes;
import net.ooici.integration.ais.AisRequestResponse.ApplicationIntegrationServiceError;
import net.ooici.integration.ais.AisRequestResponse.ApplicationIntegrationServiceRequestMsg;
import net.ooici.integration.ais.AisRequestResponse.ApplicationIntegrationServiceResponseMsg;
import net.ooici.integration.ais.findDataResources.FindDataResources.FindDataResourcesRspMsg;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.JsonFormat;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;
import com.rabbitmq.client.AMQP;

public class AppIntegrationService {
	
	private static final String AIS_SERVICE_NAME = "app_integration";
    private MessagingName applicationIntegrationSvc;

    private MsgBrokerClient msgBrokerClient;
    private BaseProcess baseProcess;
    
    private String errorMessage;
    private int status;

    // TODO fill out enum
    // Enum used by UI to indicate to this library what operation is
    // associated with the passed Json data.
    public enum RequestType {
    	CREATE_DOWNLOAD_URL,
    	REGISTER_USER, GET_USER_PROFILE, UPDATE_USER_PROFILE,
    	FIND_DATA_RESOURCE_SUBSCRIPTION, CREATE_DATA_RESOURCE_SUBSCRIPTION,
    	UPDATE_DATA_RESOURCE_SUBSCRIPTION, DELETE_DATA_RESOURCE_SUBSCRIPTION,
    	FIND_DATA_RESOURCES, FIND_DATA_RESOURCES_BY_USER, GET_DATA_RESOURCE_DETAIL,
    	CREATE_DATA_RESOURCE, VALIDATE_DATA_RESOURCE, UPDATE_DATA_RESOURCE, DELETE_DATA_RESOURCE,
    	GET_RESOURCE_TYPES, GET_RESOURCES_OF_TYPE, GET_RESOURCE
    }

    // Enum identifying expected response "type".
    public enum ResponseType {
    	STATUS_ONLY, SINGLE_OBJECT, LIST_OF_OBJECTS
    }

    // Map of RequestType enum values to request payload GPB id values
    private static final HashMap<RequestType,Integer> typeEnumToRequestTypeIntMap = new HashMap<RequestType,Integer>();

    // Map of RequestType enum values to response payload GPB id values
    private static final HashMap<RequestType,Integer> typeEnumToResponseTypeIntMap = new HashMap<RequestType,Integer>();

    // Map of RequestType enum values to AIS service op name values
    private static final HashMap<RequestType,String> typeEnumToServiceOpMap = new HashMap<RequestType,String>();

    // Map of RequestType enum values to expected response "type"
    private static final HashMap<RequestType,ResponseType> typeEnumToResponseTypeMap = new HashMap<RequestType,ResponseType>();

    // Map enum values to int GPB type ids
    // TODO define all mappings.  Currently using echo service to validate functionality.
    static {
    	typeEnumToRequestTypeIntMap.put(RequestType.REGISTER_USER, 9101);
    	typeEnumToResponseTypeIntMap.put(RequestType.REGISTER_USER, 9103);
    	typeEnumToServiceOpMap.put(RequestType.REGISTER_USER, "registerUser");
    	typeEnumToResponseTypeMap.put(RequestType.REGISTER_USER, ResponseType.SINGLE_OBJECT);

    	typeEnumToRequestTypeIntMap.put(RequestType.GET_USER_PROFILE, 9104);
    	typeEnumToResponseTypeIntMap.put(RequestType.GET_USER_PROFILE, 9105);
    	typeEnumToServiceOpMap.put(RequestType.GET_USER_PROFILE, "getUser");
    	typeEnumToResponseTypeMap.put(RequestType.GET_USER_PROFILE, ResponseType.SINGLE_OBJECT);

    	typeEnumToRequestTypeIntMap.put(RequestType.UPDATE_USER_PROFILE, 9102);
    	typeEnumToResponseTypeIntMap.put(RequestType.UPDATE_USER_PROFILE, 9002);
    	typeEnumToServiceOpMap.put(RequestType.UPDATE_USER_PROFILE, "updateUserProfile");
    	typeEnumToResponseTypeMap.put(RequestType.UPDATE_USER_PROFILE, ResponseType.STATUS_ONLY);

    	typeEnumToRequestTypeIntMap.put(RequestType.FIND_DATA_RESOURCE_SUBSCRIPTION, 9218);
    	typeEnumToResponseTypeIntMap.put(RequestType.FIND_DATA_RESOURCE_SUBSCRIPTION, 9208);
    	typeEnumToServiceOpMap.put(RequestType.FIND_DATA_RESOURCE_SUBSCRIPTION, "findDataResourceSubscriptions");
    	typeEnumToResponseTypeMap.put(RequestType.FIND_DATA_RESOURCE_SUBSCRIPTION, ResponseType.SINGLE_OBJECT);

    	typeEnumToRequestTypeIntMap.put(RequestType.CREATE_DATA_RESOURCE_SUBSCRIPTION, 9203);
    	typeEnumToResponseTypeIntMap.put(RequestType.CREATE_DATA_RESOURCE_SUBSCRIPTION, 9204);
    	typeEnumToServiceOpMap.put(RequestType.CREATE_DATA_RESOURCE_SUBSCRIPTION, "createDataResourceSubscription");
    	typeEnumToResponseTypeMap.put(RequestType.CREATE_DATA_RESOURCE_SUBSCRIPTION, ResponseType.SINGLE_OBJECT);

    	typeEnumToRequestTypeIntMap.put(RequestType.UPDATE_DATA_RESOURCE_SUBSCRIPTION, 9209);
    	typeEnumToResponseTypeIntMap.put(RequestType.UPDATE_DATA_RESOURCE_SUBSCRIPTION, 9204);
    	typeEnumToServiceOpMap.put(RequestType.UPDATE_DATA_RESOURCE_SUBSCRIPTION, "updateDataResourceSubscription");
    	typeEnumToResponseTypeMap.put(RequestType.UPDATE_DATA_RESOURCE_SUBSCRIPTION, ResponseType.SINGLE_OBJECT);

    	typeEnumToRequestTypeIntMap.put(RequestType.DELETE_DATA_RESOURCE_SUBSCRIPTION, 9205);
    	typeEnumToResponseTypeIntMap.put(RequestType.DELETE_DATA_RESOURCE_SUBSCRIPTION, 9206);
    	typeEnumToServiceOpMap.put(RequestType.DELETE_DATA_RESOURCE_SUBSCRIPTION, "deleteDataResourceSubscription");
    	typeEnumToResponseTypeMap.put(RequestType.DELETE_DATA_RESOURCE_SUBSCRIPTION, ResponseType.SINGLE_OBJECT);
    	
    	typeEnumToRequestTypeIntMap.put(RequestType.CREATE_DOWNLOAD_URL, 9035);
    	typeEnumToResponseTypeIntMap.put(RequestType.CREATE_DOWNLOAD_URL, 9036);
    	typeEnumToServiceOpMap.put(RequestType.CREATE_DOWNLOAD_URL, "createDownloadURL");
    	typeEnumToResponseTypeMap.put(RequestType.CREATE_DOWNLOAD_URL, ResponseType.SINGLE_OBJECT);
    	
    	typeEnumToRequestTypeIntMap.put(RequestType.FIND_DATA_RESOURCES, 9031);
    	typeEnumToResponseTypeIntMap.put(RequestType.FIND_DATA_RESOURCES, 9032);
    	typeEnumToServiceOpMap.put(RequestType.FIND_DATA_RESOURCES, "findDataResources");
    	typeEnumToResponseTypeMap.put(RequestType.FIND_DATA_RESOURCES, ResponseType.SINGLE_OBJECT);
    	
    	typeEnumToRequestTypeIntMap.put(RequestType.FIND_DATA_RESOURCES_BY_USER, 9031);
    	typeEnumToResponseTypeIntMap.put(RequestType.FIND_DATA_RESOURCES_BY_USER, 9038);
    	typeEnumToServiceOpMap.put(RequestType.FIND_DATA_RESOURCES_BY_USER, "findDataResourcesByUser");
    	typeEnumToResponseTypeMap.put(RequestType.FIND_DATA_RESOURCES_BY_USER, ResponseType.SINGLE_OBJECT);
    	
    	typeEnumToRequestTypeIntMap.put(RequestType.GET_DATA_RESOURCE_DETAIL, 9033);
    	typeEnumToResponseTypeIntMap.put(RequestType.GET_DATA_RESOURCE_DETAIL, 9034);
    	typeEnumToServiceOpMap.put(RequestType.GET_DATA_RESOURCE_DETAIL, "getDataResourceDetail");
    	typeEnumToResponseTypeMap.put(RequestType.GET_DATA_RESOURCE_DETAIL, ResponseType.SINGLE_OBJECT);
    	
    	typeEnumToRequestTypeIntMap.put(RequestType.CREATE_DATA_RESOURCE, 9211);
    	typeEnumToResponseTypeIntMap.put(RequestType.CREATE_DATA_RESOURCE, 9212);
    	typeEnumToServiceOpMap.put(RequestType.CREATE_DATA_RESOURCE, "createDataResource");
    	typeEnumToResponseTypeMap.put(RequestType.CREATE_DATA_RESOURCE, ResponseType.SINGLE_OBJECT);
    	
    	typeEnumToRequestTypeIntMap.put(RequestType.VALIDATE_DATA_RESOURCE, 9010);
    	typeEnumToResponseTypeIntMap.put(RequestType.VALIDATE_DATA_RESOURCE, 9011);
    	typeEnumToServiceOpMap.put(RequestType.VALIDATE_DATA_RESOURCE, "validateDataResource");
    	typeEnumToResponseTypeMap.put(RequestType.VALIDATE_DATA_RESOURCE, ResponseType.SINGLE_OBJECT);
    	
    	typeEnumToRequestTypeIntMap.put(RequestType.UPDATE_DATA_RESOURCE, 9215);
    	typeEnumToResponseTypeIntMap.put(RequestType.UPDATE_DATA_RESOURCE, 9216);
    	typeEnumToServiceOpMap.put(RequestType.UPDATE_DATA_RESOURCE, "updateDataResource");
    	typeEnumToResponseTypeMap.put(RequestType.UPDATE_DATA_RESOURCE, ResponseType.SINGLE_OBJECT);
    	
    	typeEnumToRequestTypeIntMap.put(RequestType.DELETE_DATA_RESOURCE, 9213);
    	typeEnumToResponseTypeIntMap.put(RequestType.DELETE_DATA_RESOURCE, 9214);
    	typeEnumToServiceOpMap.put(RequestType.DELETE_DATA_RESOURCE, "deleteDataResource");
    	typeEnumToResponseTypeMap.put(RequestType.DELETE_DATA_RESOURCE, ResponseType.SINGLE_OBJECT);

    	typeEnumToRequestTypeIntMap.put(RequestType.GET_RESOURCE_TYPES, -1);
    	typeEnumToResponseTypeIntMap.put(RequestType.GET_RESOURCE_TYPES, 9120);
    	typeEnumToServiceOpMap.put(RequestType.GET_RESOURCE_TYPES, "getResourceTypes");
    	typeEnumToResponseTypeMap.put(RequestType.GET_RESOURCE_TYPES, ResponseType.SINGLE_OBJECT);

    	typeEnumToRequestTypeIntMap.put(RequestType.GET_RESOURCES_OF_TYPE, 9121);
    	typeEnumToResponseTypeIntMap.put(RequestType.GET_RESOURCES_OF_TYPE, 9123);
    	typeEnumToServiceOpMap.put(RequestType.GET_RESOURCES_OF_TYPE, "getResourcesOfType");
    	typeEnumToResponseTypeMap.put(RequestType.GET_RESOURCES_OF_TYPE, ResponseType.SINGLE_OBJECT);

    	typeEnumToRequestTypeIntMap.put(RequestType.GET_RESOURCE, 9124);
    	typeEnumToResponseTypeIntMap.put(RequestType.GET_RESOURCE, 9126);
    	typeEnumToServiceOpMap.put(RequestType.GET_RESOURCE, "getResource");
    	typeEnumToResponseTypeMap.put(RequestType.GET_RESOURCE, ResponseType.SINGLE_OBJECT);
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
	public String sendReceiveUIRequest(String jsonRequest, RequestType requestType, String userId, String expiry) {
		
		errorMessage = "Internal error";
		status = 500;

    	Integer gpbId = typeEnumToRequestTypeIntMap.get(requestType);
    	if (gpbId == null) {
    		errorMessage = "GPB mapping not defined for request type";
    		status = 400;
    		return null;
    	}

    	String serviceOperation = typeEnumToServiceOpMap.get(requestType);
    	if (serviceOperation == null) {
    		errorMessage = "Service operation mapping not defined for request type";
    		status = 400;
    		return null;
    	}
    	
    	return sendReceive(jsonRequest, requestType, gpbId, serviceOperation, userId, expiry);
	}

	public String sendReceive(String jsonRequest, RequestType requestType, int gpbId, String serviceOperation, String userId, String expiry) {
		Structure requestMessage = packageRequestMessage(jsonRequest, gpbId);
		if (requestMessage == null) {
			return null;
		}
		
		IonMessage replyMessage = baseProcess.rpcSendContainerContent(applicationIntegrationSvc, serviceOperation, requestMessage, userId, expiry);
		
		if (replyMessage == null) {
    		errorMessage = "Request timeout";
    		status = 408;
    		return null;
		}
		
		return unpackageResponseMessage(replyMessage, requestType);
	}

    public Structure packageRequestMessage(String jsonRequest, int gpbId) {
    	GPBWrapper aisrmWrapper;
    	Container.Structure.Builder structBldr;
    	
    	// Handle case where there is no "payload"
    	if (gpbId == -1) {
        	// Construct ApplicationIntegrationServiceRequestMsg GPB
        	ApplicationIntegrationServiceRequestMsg.Builder aisrmBuilder = ApplicationIntegrationServiceRequestMsg.newBuilder();
        	
        	// Wrap request message
        	aisrmWrapper = GPBWrapper.Factory(aisrmBuilder.build());

        	// Add ApplicationIntegrationServieRequestMsg to items list
            structBldr = ProtoUtils.addStructureElementToStructureBuilder(null, aisrmWrapper.getStructureElement(), false);
    	}
    	else {
    		// Convert Json "payload" data to appropriate GPB format
    		GeneratedMessage payload = convertJsonToGPB(jsonRequest, gpbId);
    		if (payload == null) {
    			return null;
    		}

    		// Wrap payload
    		GPBWrapper payloadWrapper = GPBWrapper.Factory(payload);

    		// Construct ApplicationIntegrationServiceRequestMsg GPB
    		ApplicationIntegrationServiceRequestMsg.Builder aisrmBuilder = ApplicationIntegrationServiceRequestMsg.newBuilder();
    		aisrmBuilder.setMessageParametersReference(payloadWrapper.getCASRef());

    		// Wrap request message
    		aisrmWrapper = GPBWrapper.Factory(aisrmBuilder.build());

    		// Add ApplicationIntegrationServieRequestMsg and payload to items list
    		structBldr = ProtoUtils.addStructureElementToStructureBuilder(null, aisrmWrapper.getStructureElement(), false);
    		ProtoUtils.addStructureElementToStructureBuilder(structBldr, payloadWrapper.getStructureElement(), false);
    	}

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

    public GeneratedMessage convertJsonToGPB(String jsonRequest, int typeInt) {
    	if (jsonRequest == null || jsonRequest.length() == 0) {
    		errorMessage = "Request data not provided";
    		status = 400;
    		return null;
    	}

    	try {
    		Message.Builder builder = getMessageBuilder(typeInt);
    		if (builder == null) {
    			return null;
    		}

    		// Copy Json into GPB
    		JsonFormat.merge(jsonRequest, builder);
    		System.out.println("builder: " + builder);
    		
    		return (GeneratedMessage)builder.build();
    	} catch(Exception e) {
    		errorMessage = "Exception encoding JSON to GPB: " + e;
    		status = 400;
    	}

    	return null;
    }

    public Builder getMessageBuilder(int typeInt) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		// Get GeneratedMessage class for type id
    	Class clazz = IonBootstrap.getMappedClassForKeyValue(typeInt);
    	if (clazz == null) {
    		errorMessage = "Could not find GPB class";
    		status = 500;
    		return null;
    	}

		// Get builder instance by invoking static newBuilder() via reflection
		Method method = clazz.getMethod("newBuilder", (Class[])null);
		Message.Builder builder = (Message.Builder)method.invoke(null, (Object[])null);

		return builder;
    }

    public Class getMessageClass(int typeInt) {
		// Get GeneratedMessage class for type id
    	Class clazz = IonBootstrap.getMappedClassForKeyValue(typeInt);
    	if (clazz == null) {
    		errorMessage = "Could not find GPB class";
    		status = 500;
    		return null;
    	}

		return clazz;
    }

    public String unpackageResponseMessage(IonMessage replyMessage, RequestType requestType) {
    	Class responseMsgClazz = getMessageClass(typeEnumToResponseTypeIntMap.get(requestType));
    	if (responseMsgClazz == null) {
    		return null;
    	}
    	
        StructureManager sm = StructureManager.Factory(replyMessage);
        
        // Extract the IonMsg from the head
        if (sm.getHeadId() == null || sm.getHeadId().isEmpty()) {
        	errorMessage = "Expected one head item on response";
        	return null;
        }

        String ionMsgKey = sm.getHeadId();
        GPBWrapper msgWrapper = sm.getObjectWrapper(ionMsgKey);
        if (!(msgWrapper.getObjectValue() instanceof IonMsg)) {
        	errorMessage = "Expected IonMsg head item";
        	return null;
        }
        IonMsg ionMsg = (IonMsg)msgWrapper.getObjectValue();

        // Extract the items
        // First find the ApplicationIntegrationServiceResponseMsg
        // or ApplicationIntegrationServiceError proto buff
        if (sm.getItemIds().size() == 0) {
        	// Check for error in IonMsg header
        	ResponseCodes responseCode = ionMsg.getResponseCode();
        	if (responseCode == ResponseCodes.OK) {
        		errorMessage = "Expected at least one item";
        		return null;
        	}
        	else {
        		status = responseCode.getNumber();
        		errorMessage = responseCode.name() + ":" + ionMsg.getResponseBody();
        		return null;
        	}
        }
        for (String itemKey : sm.getItemIds()) {
            msgWrapper = sm.getObjectWrapper(itemKey);

            // If error response, convert info and return
            if (msgWrapper.getObjectValue() instanceof ApplicationIntegrationServiceError) {
            	ApplicationIntegrationServiceError responseMsg = (ApplicationIntegrationServiceError)msgWrapper.getObjectValue();
            	status = responseMsg.getErrorNum();
            	errorMessage = JsonFormat.printToString(responseMsg);
            	return null;
            }
        }
        
        // Simple response payload
        if (sm.getItemIds().size() == 1) {
        	String itemKey = sm.getItemIds().get(0);
    		msgWrapper = sm.getObjectWrapper(itemKey);

    		// If error response, convert info and return
    		if (!(msgWrapper.getObjectValue() instanceof ApplicationIntegrationServiceResponseMsg)) {
    			errorMessage = "Expected ApplicationIntegrationServiceResponseMsg in items";
    			return null;
    		}
    		if (typeEnumToResponseTypeMap.get(requestType) != ResponseType.STATUS_ONLY) {
    			errorMessage = "Expected one or more objects in payload";
    			return null;
    		}
    		GPBWrapper payloadWrapper = sm.getObjectWrapper(itemKey);
    		ApplicationIntegrationServiceResponseMsg responseMsg = (ApplicationIntegrationServiceResponseMsg)payloadWrapper.getObjectValue();
        	errorMessage = "";
    		status = responseMsg.getResult();
        	return null;
        }
        else {
        	boolean createList = sm.getItemIds().size() > 2 ? true : false;
    		if (createList && typeEnumToResponseTypeMap.get(requestType) != ResponseType.LIST_OF_OBJECTS) {
    			errorMessage = "Expected only one object of type " + responseMsgClazz.getSimpleName() + " in response payload, but multiple objects returned.";
    			for (int i = 0; i < sm.getItemIds().size(); i++) {
    	        	String itemKey = sm.getItemIds().get(i);
    	    		msgWrapper = sm.getObjectWrapper(itemKey);
    				errorMessage += "\nObject type [" + i + "]" + msgWrapper.getObjectValue().getClass().getSimpleName();
    			}
    			return null;
    		}
    		if (!createList && typeEnumToResponseTypeMap.get(requestType) != ResponseType.SINGLE_OBJECT) {
    			errorMessage = "Expected list of objects of type " + responseMsgClazz.getSimpleName() + " in response payload, but only one object returned.";
    			for (int i = 0; i < sm.getItemIds().size(); i++) {
    	        	String itemKey = sm.getItemIds().get(i);
    	    		msgWrapper = sm.getObjectWrapper(itemKey);
    				errorMessage += "\nObject type [" + i + "]" + msgWrapper.getObjectValue().getClass().getSimpleName();
    			}
    			return null;
    		}

    		String payloadInfo = createList ? "[" : "";
        	boolean firstTime = true;
        	for (String itemKey : sm.getItemIds()) {
        		msgWrapper = sm.getObjectWrapper(itemKey);

        		// If error response, convert info and return
        		if (msgWrapper.getObjectValue() instanceof ApplicationIntegrationServiceResponseMsg
        				|| msgWrapper.getObjectValue() instanceof ApplicationIntegrationServiceError) {
        			continue;
        		}
        		if (!msgWrapper.getObjectValue().getClass().getName().equals(responseMsgClazz.getName())) {
        			errorMessage = "Expected object(s) of type " + responseMsgClazz.getSimpleName() + " in response payload.";
        			for (int i = 0; i < sm.getItemIds().size(); i++) {
        	        	String key = sm.getItemIds().get(i);
        	    		msgWrapper = sm.getObjectWrapper(key);
        				errorMessage += "\nObject type [" + i + "]" + msgWrapper.getObjectValue().getClass().getSimpleName();
        			}
        			return null;
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
        	// TODO get status from reply message
        	errorMessage = "";
    		status = 200;
        	return payloadInfo;
        }
    }
    
    public String getErrorMessage() {
    	return errorMessage;
    }
    
    public int getStatus() {
    	return status;
    }

    // For testing purposes
    public void populateGPB(Builder builder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {
		Class clazz = builder.getClass();
		Method[] methods = clazz.getMethods();
		for (int j = 0; j < methods.length; j++) {
			if (methods[j].getName().startsWith("set") && (!methods[j].getName().equals("setUnknownFields"))) {
				Class[] parameters = methods[j].getParameterTypes();
				if (parameters.length == 1) {
					Object[] inputParam = new Object[1];
					if (parameters[0] == String.class) {
						inputParam[0] = methods[j].getName().substring(3, methods[j].getName().length());
					}
					else if (parameters[0] == int.class) {
						inputParam[0] = new Integer(1);
					}
					else if (parameters[0] == double.class) {
						inputParam[0] = new Double(1.1);
					}
					else if (parameters[0] == long.class) {
						inputParam[0] = new Long(1);
					}
					else if (parameters[0] == boolean.class) {
						inputParam[0] = new Boolean(true);
					}
					else if (parameters[0].getSuperclass() == GeneratedMessage.class) {
						continue;
					}
					else if (parameters[0].getSuperclass() == GeneratedMessage.Builder.class) {
						Class parentClazz = parameters[0].getEnclosingClass();
						Method method = parentClazz.getMethod("newBuilder", (Class[])null);
						Message.Builder subBuilder = (Message.Builder)method.invoke(null, (Object[])null);
						populateGPB(subBuilder);
						inputParam[0] = subBuilder;
					}
					else {
						Class inputParamClazz = (Class)parameters[0];
						Method[] inputParamClazzMethods = inputParamClazz.getMethods();
						boolean found = false;
						for (int k = 0; k < inputParamClazzMethods.length; k++) {
							if (inputParamClazzMethods[k].getName().equals("values")) {
								Object[] values = (Object[])inputParamClazzMethods[k].invoke(null, (Object[])null);
								inputParam[0] = values[0];
								found = true;
								break;
							}
						}
						assert (found);
					}
					if (inputParam[0] == null) {
						System.out.println("HERE");
					}
					methods[j].invoke(builder, inputParam);
				}
			}
			else if (methods[j].getName().startsWith("add") && (!methods[j].getName().startsWith("addAll"))) {
				Class[] parameters = methods[j].getParameterTypes();
				if (parameters.length == 1) {
					System.out.println("Simple name: " + parameters[0].getSimpleName());
					if (parameters[0].getSimpleName().equals("Builder")) {
						Object[] inputParam = new Object[1];
						Class inputParamClazz = (Class)parameters[0];
						Class parentClazz = inputParamClazz.getEnclosingClass();
						Method method = parentClazz.getMethod("newBuilder", (Class[])null);
						Message.Builder subBuilder = (Message.Builder)method.invoke(null, (Object[])null);
						populateGPB(subBuilder);
						inputParam[0] = subBuilder;
						methods[j].invoke(builder, inputParam);
					}
				}
			}
		}
    }

    /**
     * Main method will produce a text file containing sample JSON input/output strings
     * for each of the protocol buffer types mapped to request types.
     * @param args
     */
	public static void main(String[] args) {
		AppIntegrationService ais = new AppIntegrationService("Tom", "localhost", AMQP.PROTOCOL.PORT, "magnet.topic");

		ArrayList<String> requestMessages = new ArrayList<String>();
		ArrayList<String> responseMessages = new ArrayList<String>();

		try {
			RequestType[] requestTypeArray = RequestType.values();
			for (int i = 0; i < requestTypeArray.length; i++) {
				String requestMsgString = "undefined";
				if (typeEnumToRequestTypeIntMap.get(requestTypeArray[i]) != -1) {
					Builder builder = ais.getMessageBuilder(typeEnumToRequestTypeIntMap.get(requestTypeArray[i]));
					ais.populateGPB(builder);
					requestMsgString = JsonFormat.printToString(builder.build());
				}
				requestMessages.add(requestMsgString);
				System.out.println("JSON Request for " + requestTypeArray[i] + ": <" + requestMsgString + ">");
			}

			for (int i = 0; i < requestTypeArray.length; i++) {
				String responseMsgString = "undefined";
				if (typeEnumToResponseTypeMap.get(requestTypeArray[i]) == ResponseType.STATUS_ONLY) {
					responseMsgString = "Status Code";
				}
				else {
					if (typeEnumToResponseTypeIntMap.get(requestTypeArray[i]) != -1) {
						Builder builder = ais.getMessageBuilder(typeEnumToResponseTypeIntMap.get(requestTypeArray[i]));
						ais.populateGPB(builder);
						if (typeEnumToResponseTypeMap.get(requestTypeArray[i]) == ResponseType.LIST_OF_OBJECTS) {
							responseMsgString = "[" + JsonFormat.printToString(builder.build()) + "]";
						}
						else {
							responseMsgString = JsonFormat.printToString(builder.build());
						}
					}
				}
				responseMessages.add(responseMsgString);
				System.out.println("JSON Response for " + requestTypeArray[i] + ": <" + responseMsgString + ">");
			}

			FileOutputStream out;
			PrintStream p;

			out = new FileOutputStream("JSONFormatExamples.txt");
			
			p = new PrintStream(out);

			for (int i = 0; i < requestTypeArray.length; i++) {
				p.println ("JSON for " + requestTypeArray[i]);
				p.println("  Request:  " + requestMessages.get(i));
				p.println("  Response: " + responseMessages.get(i));
			}

			p.close();
		}
		catch (Exception e) {
			System.out.println("Exception: " + e);
		}

		ais.dispose();
	}
	
}