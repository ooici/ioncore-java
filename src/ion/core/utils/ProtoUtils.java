/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ion.core.utils;

import ion.core.BaseProcess;
import ion.core.messaging.IonMessage;
import ion.core.messaging.MessagingName;
import ion.core.messaging.MsgBrokerClient;

import java.util.HashMap;

import net.ooici.core.container.Container;
import net.ooici.core.message.IonMessage.IonMsg;
import net.ooici.services.dm.DatasetRegistry.DatasetEntryMessage;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rabbitmq.client.AMQP;

/**
 *
 * @author cmueller
 */
public class ProtoUtils {

	static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProtoUtils.class);

    private ProtoUtils() {
    }

    /**
     * Method to encapsulate the effort to put a StructureElement into a Structure.  The structure parameter can be passes a null.
     * This results in the method constructing a new Structure.Builder, which is returned from the method.  This Structure.Builder
     * should be passed in on subsequent calls to this method.
     * 
     * @param structure Structure.Builder into which the StructureElement should be placed.
     * @param element built StructureElement to be added to the StructureBuilder
     * @param isHead indicates if this should be added to the head list or the items list
     * @return StructureBuilder in which the StructureElement was added
     */
    public static Container.Structure.Builder addStructureElementToStructureBuilder(Container.Structure.Builder structure, Container.StructureElement element, boolean isHead) {
        Container.Structure.Builder struct = structure;
        if (struct == null) {
            struct = Container.Structure.newBuilder();
        }

        if (isHead) {
            struct.addHeads(element);
        } else {
            struct.addItems(element);
        }

        return struct;
    }

    /**
     * Method that encapsulates the inclusion of a StructureElement into a Structure.  A corresponding IonMessage StructureElement
     * that references the passed StructureElement is added to the Structure heads.  The structure parameter can be passes a null.
     * This results in the method constructing a new Structure.Builder, which is returned from the method.  This Structure.Builder
     * should be passed in on subsequent calls to this method.
     * 
     * @param structure Structure.Builder into which the StructureElement should be placed.
     * @param element built StructureElement to be added to the StructureBuilder
     * @return StructureBuilder in which the StructureElement was added
     */
    public static Container.Structure.Builder addIonMessageContent(Container.Structure.Builder structure, String name, String identity, GeneratedMessage content) {
    	// Construct wrapper for content
        GPBWrapper contentWrapper = GPBWrapper.Factory(content);
    	// Add content to items list
    	Container.Structure.Builder struct = addStructureElementToStructureBuilder(structure, contentWrapper.getStructureElement(), false);

    	// Construct IonMsg to be put in heads list
    	IonMsg.Builder ionMsgBldr = IonMsg.newBuilder().setName(name).setIdentity(identity);
        ionMsgBldr.setType(contentWrapper.getObjectType());
        // Reference the content object via a CASRef
        ionMsgBldr.setMessageObject(contentWrapper.getCASRef());
        // Construct wrapper for IonMsg
        GPBWrapper msgWrapper = GPBWrapper.Factory(ionMsgBldr.build());
        
        addStructureElementToStructureBuilder(struct, msgWrapper.getStructureElement(), true);
    	return struct;
    }

    public static HashMap<String, GPBWrapper> unpackStructure(byte[] structure) {
        return unpackStructure(ByteString.copyFrom(structure));
    }

    public static HashMap<String, GPBWrapper> unpackStructure(ByteString structure) {
        try {
            return unpackStructure(Container.Structure.parseFrom(structure));
        } catch (InvalidProtocolBufferException ex) {
            log.error(ex.toString());
        }
        return null;
    }

    /**
     * Method that extracts n-layer nested proto buff content hanging off Structure into hash map.
     * 
     * @param structure container object
     * @return
     */
    public static HashMap<String, GPBWrapper> unpackStructure(Container.Structure structure) {
        HashMap<String, GPBWrapper> map = new HashMap<String, GPBWrapper>();

        // Traverse the head elements
        System.out.println("# Heads: " + structure.getHeadsCount());
        if (structure.getHeadsCount() > 0) {
        	for (Container.StructureElement element : structure.getHeadsList()) {
        		GPBWrapper head = GPBWrapper.Factory(element);
        		map.put(head.getKeyString(), head);
        	}
        }

        // Iterate through items list.
        System.out.println("# Items: " + structure.getItemsCount());
        for (Container.StructureElement element : structure.getItemsList()) {
            GPBWrapper item = GPBWrapper.Factory(element);
            map.put(item.getKeyString(), item);
        }

        return map;
    }
    
    public static void main(String[] args) {
    	String hostName = "localhost";
        int portNumber = AMQP.PROTOCOL.PORT;
        String exchange = "magnet.topic";

    	// Messaging environment
        MsgBrokerClient ionClient = new MsgBrokerClient(hostName, portNumber, exchange);
        ionClient.attach();

        BaseProcess baseProcess = new BaseProcess(ionClient);
    	baseProcess.spawn();
    	
		DatasetEntryMessage.Builder dataResource = DatasetEntryMessage.newBuilder();
		
		dataResource.setProvider("provider1");
		dataResource.setFormat("format1");
		dataResource.setProtocol("protocol1");
		dataResource.setType("type1");
		dataResource.setTitle("title1");
		dataResource.setDataType("data_type1");
		dataResource.setNamingAuthority("naming_authority1");
		
		DatasetEntryMessage.Person.Builder publisher = DatasetEntryMessage.Person.newBuilder();
		publisher.setName("publisher_name1");
		publisher.setEmail("publisher_email1");
		publisher.setWebsite("publisher_website1");
		publisher.setInstitution("publisher_institution1");
		dataResource.setPublisher(publisher);
		
		DatasetEntryMessage.Person.Builder creator = DatasetEntryMessage.Person.newBuilder();
		creator.setName("creator_name1");
		creator.setEmail("creator_email1");
		creator.setWebsite("creator_website1");
		creator.setInstitution("creator_institution1");
		dataResource.setCreator(creator);

		Container.Structure.Builder structureBuilder = ProtoUtils.addIonMessageContent(null, "CreateDataResource", "Identity", dataResource.build());

		MessagingName r1intSvc = new MessagingName("Tom", "r1integration");

        IonMessage msgin = baseProcess.rpcSendContainerContent(r1intSvc, "createDataResource", structureBuilder.build());
        
        System.out.println("UUID: " + msgin.getContent());
    }
}
