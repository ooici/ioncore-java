/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ion.core.utils;

import com.google.protobuf.InvalidProtocolBufferException;
import ion.core.BaseProcess;
import ion.core.IonBootstrap;
import ion.core.messaging.IonMessage;
import ion.core.messaging.MessagingName;
import ion.core.messaging.MsgBrokerClient;

import java.util.HashMap;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;
import com.rabbitmq.client.AMQP;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.ooici.core.container.Container;
import net.ooici.core.link.Link.CASRef;
import net.ooici.core.message.IonMessage.IonMsg;
import net.ooici.core.type.Type;
import net.ooici.core.type.Type.GPBType;
import net.ooici.services.dm.DatasetRegistry.DatasetEntryMessage;

/**
 *
 * @author cmueller
 */
public class ProtoUtils {

	static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProtoUtils.class);


    private ProtoUtils() {
    }

    /**
     * 
     * @param isLeaf
     * @param key
     * @param gpbType
     * @param value
     * @return
     * @deprecated Use GPBWrapper.getStructureElement() instead
     */
    @Deprecated
    public static Container.StructureElement getStructureElement(boolean isLeaf, byte[] key, Type.GPBType gpbType, ByteString value) {
        com.google.protobuf.ByteString keyS = com.google.protobuf.ByteString.copyFrom(key);
        return getStructureElement(isLeaf, keyS, gpbType, value);
    }

    /**
     *
     * @param isLeaf
     * @param bsKey
     * @param gpbType
     * @param value
     * @return
     * @deprecated Use GPBWrapper.getStructureElement() instead
     */
    @Deprecated
    public static Container.StructureElement getStructureElement(boolean isLeaf, ByteString bsKey, Type.GPBType gpbType, ByteString value) {
        return Container.StructureElement.newBuilder().setIsleaf(isLeaf).setKey(bsKey).setType(gpbType).setValue(value).build();
    }

    /**
     *
     * @param clazz
     * @return
     * @deprecated Use GPBWrapper.getGPBType() instead
     */
    @Deprecated
    public static Type.GPBType getGPBType(Class clazz) {
        int _ID = ion.core.IonBootstrap.getKeyValueForMappedClass(clazz);
        return Type.GPBType.newBuilder().setObjectId(_ID).setVersion(1).build();
    }

    /**
     * 
     * @param isLeaf
     * @param key
     * @param gpbType
     * @return
     * @deprecated Use GPBWrapper.getCASRef() instead
     */
    @Deprecated
    public static CASRef getLink(boolean isLeaf, byte[] key, Type.GPBType gpbType) {
        com.google.protobuf.ByteString keyS = com.google.protobuf.ByteString.copyFrom(key);
        return CASRef.newBuilder().setKey(keyS).setType(gpbType).setIsleaf(isLeaf).build();
    }

    /**
     * 
     * @param bsContent
     * @param type
     * @return
     * @deprecated Use GPBWrapper.getObjectKey() instead
     */
    @Deprecated
    public static byte[] getObjectKey(ByteString bsContent, Type.GPBType type) {
        return getObjectKey(bsContent.toByteArray(), type);
    }

    /**
     * 
     * @param content
     * @param type
     * @return
     * @deprecated Use GPBWrapper.getObjectKey() instead
     */
    @Deprecated
    public static byte[] getObjectKey(byte[] content, Type.GPBType type) {
        byte[] key = SHA1.getSHA1Hash(content);
        byte[] type_ba = type.toByteArray();
        byte[] comb = new byte[key.length + type_ba.length];
        System.arraycopy(key, 0, comb, 0, key.length);
        System.arraycopy(type_ba, 0, comb, key.length, type_ba.length);
        return SHA1.getSHA1Hash(comb);
    }

    /**
     * 
     * @param content
     * @param type
     * @return
     * @deprecated Use GPBWrapper.getObjectKey() instead
     */
    @Deprecated
    public static ByteString getObjectKeyByteString(ByteString content, Type.GPBType type) {
        return com.google.protobuf.ByteString.copyFrom(getObjectKey(content.toByteArray(), type));
    }

// NEW CODE STARTS HERE
    /**
     * Helper method to simplify construction of GPBType objects
     * @param proto buff object for which
     * @deprecated Use GPBWrapper.getObjectType() instead
     */
    @Deprecated
    public static GPBType getGPBType(Object protoBuffObj, int version) {
        GPBType.Builder gpbType = GPBType.newBuilder();
        gpbType.setObjectId(IonBootstrap.getKeyValueForMappedClass(protoBuffObj.getClass()));
        gpbType.setVersion(version);
        return gpbType.build();
    }

    /**
     * Method to encapsulate the effort to put a proto buffer payload into a StructureElement.
     * The key assigned to the StructureElement is the SHA1 of the content.
     * 
     * @param content built proto buffer
     * @param gpbType type of built proto buffer
     * @param isLeaf
     * @return
     * @deprecated Use GPBWrapper.getStructureElement() instead
     */
    @Deprecated
    public static Container.StructureElement packStructureElement(GeneratedMessage content, Type.GPBType gpbType, boolean isLeaf) {
        ByteString byteString = content.toByteString();
        Container.StructureElement.Builder structureElement = Container.StructureElement.newBuilder();
        structureElement.setValue(byteString);
        structureElement.setType(gpbType);
        structureElement.setIsleaf(isLeaf);
        structureElement.setKey(getObjectKeyByteString(byteString, gpbType));

        return structureElement.build();
    }

    /**
     * Method to encapsulate the effort to put a StructureElement into a Structure.  The structBldr parameter can be passes a null.
     * This results in the method constructing a new Structure.Builder, which is returned from the method.  This Structure.Builder
     * should be passed in on subsequent calls to this method.
     *
     * @param structBldr Structure.Builder into which the StructureElement should be placed.  Will be initialized if == null
     * @param element built StructureElement to be added to the StructureBuilder
     * @param isHead indicates if this should be added to the head list or the items list
     * @return Structure.Builder for convenience, returns the {@code structBldr} that was passed in
     */
    public static Container.Structure.Builder addStructureElementToStructureBuilder(Container.Structure.Builder structBldr, Container.StructureElement element, boolean isHead) {
//        Container.Structure.Builder struct = structBldr;
        if (structBldr == null) {
            structBldr = Container.Structure.newBuilder();
        }

        if (isHead) {
            structBldr.setHead(element);
        } else {
            structBldr.addItems(element);
        }

        return structBldr;
    }

    /**
     * Overload method for {@link ProtoUtils.addStructureElementToStructureBuilder(Container.Structure.Builder structBldr, Container.StructureElement element, boolean isHead) }
     * that sets the {@code isHead } parameter to false.
     * </p>
     * Use this method when adding "items" to the Structure.Builder
     * @param structBldr Structure.Builder into which the StructureElement should be placed.  Will be initialized if == null
     * @param element built StructureElement to be added to the StructureBuilder
     * @return Structure.Builder for convenience, returns the {@code structBldr} that was passed in
     */
    public static Container.Structure.Builder addStructureElementToStructureBuilder(Container.Structure.Builder structBldr, Container.StructureElement element) {
        return addStructureElementToStructureBuilder(structBldr, element, false);
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
//        ionMsgBldr.setType(contentWrapper.getObjectType());
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
        System.out.println("Head " + (structure.getHead() != null ? "exists" : "not defined"));
        if (structure.getHead() != null) {
        	Container.StructureElement element = structure.getHead();
        	GPBWrapper head = GPBWrapper.Factory(element);
       		map.put(head.getKeyString(), head);
        }

        // Iterate through items list.
        System.out.println("# Items: " + structure.getItemsCount());
        for (Container.StructureElement element : structure.getItemsList()) {
            GPBWrapper item = GPBWrapper.Factory(element);
            map.put(item.getKeyString(), item);
        }

        return map;
    }

//    /**
//     * Method that extracts n-layer nested proto buff content hanging off StructureElement into hash map.
//     *
//     * @param element structure element parent object
//     * @return
//     */
//    public static void recurseStructureElement(HashMap<ByteString, GPBWrapper> map, Container.StructureElement element) {
//        GPBWrapper protoBuffWrapper = new GPBWrapper(element.getType(), element.getValue());
//        // TODO how to get back from byte[] to SHA1 class?  Can't just cast here.
//        map.put(element.getObjectKey(), protoBuffWrapper);
//
//        // TODO how  do I drill down the hierarchy?
//        if (!element.getIsleaf()) {
//            recurseStructureElement(map, element);
//        }
//    }
    public static void main(String[] args) {
//        testSendReceive();
        testStructureManager();
//        test();
    }

    private static void testStructureManager() {
        System.out.println("\n>>>>>>>>>>>>>>>>>Test StructureManager<<<<<<<<<<<<<<<<<");
        
        System.out.println("\n******Make struct1******");
        Container.Structure struct1 = makeStruct1();
        System.out.println("*******************************\n\n");

        System.out.println(">>> Add struct1 to StructureManager");
        StructureManager sm = StructureManager.Factory(struct1);
        System.out.println(sm);

        System.out.println("\n******Make struct2******");
        Container.Structure struct2 = makeStruct2();
        System.out.println("*******************************\n\n");

        System.out.println(">>> Add struct2 to StructureManager");
        sm.addStructure(struct2);
        System.out.println(sm);

        System.out.println(">>> Remove the struct1 from StructureManager");
        sm.removeStructure(struct1);
        System.out.println(sm);

        System.out.println(">>> Get the first head");
        GPBWrapper<IonMsg> msgWrap = sm.getObjectWrapper(sm.getHeadIds().get(0));
        IonMsg msg = msgWrap.getObjectValue();
        System.out.println(msg);

        System.out.println(">>> Get the item referenced by that head");
        GPBWrapper<DatasetEntryMessage> demWrap = sm.getObjectWrapper(msg.getMessageObject());
        DatasetEntryMessage dem = demWrap.getObjectValue();
        System.out.println(dem);
    }

    private static void testSendReceive() {
        System.out.println("\n>>>>>>>>>>>>>>>>>Test Send/Receive<<<<<<<<<<<<<<<<<");
        
        System.out.println("\n******Make Test Structure******");
        /* Generate the test struct1 */
        Container.Structure structure = makeStruct1();
        System.out.println("\n*******************************\n\n");

        System.out.println("\n******Prepare MsgBrokerClient******");
        /* Send the message to the simple_responder service which just replys with the content of the sent message */
        MsgBrokerClient ionClient = new MsgBrokerClient("localhost", com.rabbitmq.client.AMQP.PROTOCOL.PORT, "magnet.topic");
        ionClient.attach();
        BaseProcess baseProcess = new BaseProcess(ionClient);
        baseProcess.spawn();


        System.out.println("\n******RPC Send******");
        MessagingName simpleResponder = new MessagingName("testing", "responder");
        IonMessage reply = baseProcess.rpcSendContainerContent(simpleResponder, "respond", structure);

        System.out.println("\n******Unpack Message******");
        StructureManager sm = StructureManager.Factory(reply);
        System.out.println(">>>> Heads:");
        for(String key : sm.getHeadIds()) {
            System.out.println(key);
            GPBWrapper<IonMsg> msgWrap = sm.getObjectWrapper(key);
            System.out.println(msgWrap);
            IonMsg msg = msgWrap.getObjectValue();
        }
        System.out.println("\n>>>> Items:");
        for(String key : sm.getItemIds()) {
            System.out.println(key);
            GPBWrapper<DatasetEntryMessage> demWrap = sm.getObjectWrapper(key);
            System.out.println(demWrap);
            DatasetEntryMessage dem = demWrap.getObjectValue();
        }

        baseProcess.dispose();
    }
    private static Container.Structure makeStruct1() {
        /* GPBWrapper Factory Example */

        // Item object is a DatasetEntryMessage
        DatasetEntryMessage.Builder dataResourceBuilder = DatasetEntryMessage.newBuilder();
        dataResourceBuilder.setProvider("provider1").setDataType("cool_type").setFormat("json").setTitle("great title");
        DatasetEntryMessage dataResource = dataResourceBuilder.build();

        System.out.println("****** Generate message_objects******");
        GPBWrapper<DatasetEntryMessage> demWrap = GPBWrapper.Factory(dataResource);
        System.out.println("DatasetEntryMessage:\n" + demWrap);

        // Head is an IonMsg
        IonMsg.Builder ionMsgBldr = IonMsg.newBuilder().setName("Test Message").setIdentity("1");
//        ionMsgBldr.setType(demWrap.getObjectType());
        /* This object references the dem object via a CASRef */
        ionMsgBldr.setMessageObject(demWrap.getCASRef());
        GPBWrapper msgWrap = GPBWrapper.Factory(ionMsgBldr.build());
        System.out.println("IonMsg:\n" + msgWrap);

        /* Add the elements to the Container.Structure.Builder */
        Container.Structure.Builder structBldr = ProtoUtils.addStructureElementToStructureBuilder(null, msgWrap.getStructureElement(), true);
        ProtoUtils.addStructureElementToStructureBuilder(structBldr, demWrap.getStructureElement(), false);

        return structBldr.build();
    }
    private static Container.Structure makeStruct2() {
        /* GPBWrapper Factory Example */

        // Item object is a DatasetEntryMessage
        DatasetEntryMessage.Builder dataResourceBuilder = DatasetEntryMessage.newBuilder();
        dataResourceBuilder.setProvider("provider2").setDataType("awesome_type").setFormat("json").setTitle("Best Title Ever");
        DatasetEntryMessage dataResource = dataResourceBuilder.build();

        System.out.println("****** Generate message_objects******");
        GPBWrapper<DatasetEntryMessage> demWrap = GPBWrapper.Factory(dataResource);
        System.out.println("DatasetEntryMessage:\n" + demWrap);

        // Head is an IonMsg
        IonMsg.Builder ionMsgBldr = IonMsg.newBuilder().setName("Another Test Message").setIdentity("22");
//        ionMsgBldr.setType(demWrap.getObjectType());
        /* This object references the dem object via a CASRef */
        ionMsgBldr.setMessageObject(demWrap.getCASRef());
        GPBWrapper msgWrap = GPBWrapper.Factory(ionMsgBldr.build());
        System.out.println("IonMsg:\n" + msgWrap);

        /* Add the elements to the Container.Structure.Builder */
        Container.Structure.Builder structBldr = ProtoUtils.addStructureElementToStructureBuilder(null, msgWrap.getStructureElement(), true);
        ProtoUtils.addStructureElementToStructureBuilder(structBldr, demWrap.getStructureElement(), false);

        return structBldr.build();
    }

    private static void test() {
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

        IonMessage msgin = baseProcess.rpcSendContainerContent(r1intSvc, "createDataResource", structureBuilder.build(), "83e08e23-1666-47c2-9e8a-f5dfcbde6690", "1234");

        System.out.println("UUID: " + msgin.getContent());
    }
}
