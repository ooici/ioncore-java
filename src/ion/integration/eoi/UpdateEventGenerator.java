/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ion.integration.eoi;

import com.rabbitmq.client.AMQP;
import ion.core.IonException;
import ion.core.messaging.IonMessage;
import ion.core.messaging.MessagingName;
import ion.core.messaging.MsgBrokerClient;
import ion.core.utils.GPBWrapper;
import ion.core.utils.IonTime;
import ion.core.utils.IonUtils;
import ion.core.utils.ProtoUtils;
import java.io.IOException;
import java.util.UUID;
import net.ooici.core.container.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cmueller
 */
public class UpdateEventGenerator {

    private static final Logger log = LoggerFactory.getLogger(UpdateEventGenerator.class);

    public static void main(String[] args) throws IOException {
        args = new String[]{"3319A67F-81F3-424F-8E69-4F28C4E04808"};

        generateUpdateEvent(args);

    }

    private static void generateUpdateEvent(String[] args) throws IOException {
        if (args.length < 1) {
            log.error("Must provide at least 1 argument");
            System.out.println(usageString());
            System.exit(1);
        }
        String connInfoPath = null;
        String dsResourceId = null;
        switch (args.length) {
            case 2:
                connInfoPath = args[1];
            case 1:
                dsResourceId = args[0];
                break;
        }

        java.util.HashMap<String, String> connInfo = null;
        try {
            java.io.File propsFile = new java.io.File(connInfoPath);
            if (connInfoPath != null && propsFile.exists()) {
                connInfo = IonUtils.parseProperties(propsFile);
            } else {
                connInfo = IonUtils.parseProperties();
            }
        } catch (IOException ex) {
            log.error("Error reading connection info:", ex);
            System.exit(1);
        }
        if (connInfo == null || connInfo.isEmpty()) {
            log.error("Connection info is empty or doesn't exist");
            System.exit(1);
        }

        String ooiciHost = connInfo.get("host");
        String ooiciExchange = connInfo.get("event_xp_name");
        String ooiciSysname = connInfo.get("sysname");
        String ooiciTopic = connInfo.get("update_event_topic");

        log.error("Connection Parameters:: host={} : xp={} : topic={} : sysname={}", new Object[]{ooiciHost, ooiciExchange, ooiciTopic, ooiciSysname});

        MsgBrokerClient mainBroker = null;
        try {
            MessagingName ooiToName = new MessagingName(ooiciTopic);
            MessagingName ooiMyName = MessagingName.generateUniqueName();
            mainBroker = new MsgBrokerClient(ooiciHost, AMQP.PROTOCOL.PORT, ooiciSysname + "." + ooiciExchange);
            try {
                mainBroker.attach();
            } catch (IonException ex) {
                throw new IOException("Error opening file: Could not connect to broker", ex);
            }
            String mainQueue = mainBroker.declareQueue(null);
            mainBroker.bindQueue(mainQueue, ooiMyName, null);
            mainBroker.attachConsumer(mainQueue);

            log.error("MainBroker || binding_key={} : to_name={} : queue_name = {}", new Object[]{ooiMyName, ooiToName, mainQueue});

//            net.ooici.services.dm.Event.ResourceModificationEventMessage.Builder resModBldr = net.ooici.services.dm.Event.ResourceModificationEventMessage.newBuilder();
//            net.ooici.core.link.Link.IDRef idRef = net.ooici.core.link.Link.IDRef.newBuilder().setKey(dsResourceId).build();
//            GPBWrapper idWrap = GPBWrapper.Factory(idRef);
//            resModBldr.setResource(idWrap.getCASRef());
//
//            GPBWrapper resModWrap = GPBWrapper.Factory(resModBldr.build());


            /* Make the SchedulerPerformIngestionUpdateMessage */
            net.ooici.services.dm.Scheduler.SchedulerPerformIngestionUpdateMessage updMsg = net.ooici.services.dm.Scheduler.SchedulerPerformIngestionUpdateMessage.newBuilder().setDatasetId(dsResourceId).build();
            GPBWrapper updMsgWrap = GPBWrapper.Factory(updMsg);

            /* Make the TriggerEventMessage and set the updateMessage as the payload */
            net.ooici.services.dm.Event.TriggerEventMessage trigMsg = net.ooici.services.dm.Event.TriggerEventMessage.newBuilder().setPayload(updMsgWrap.getCASRef()).build();
            GPBWrapper trigMsgWrap = GPBWrapper.Factory(trigMsg);



            /* Build the EventMessage that "holds" the ResourceModificationEventMessage */
            net.ooici.services.dm.Event.EventMessage.Builder evtBldr = net.ooici.services.dm.Event.EventMessage.newBuilder();
//            evtBldr.setName("External ResourceModifiedEvent Trigger");
//            evtBldr.setDataResourceId(dsResourceId);// ??TODO: Find out what this is supposed to be!!
//            evtBldr.setDescription("Contains a \"ResourceModificationEventMessage\" which will result in an update cycle for the indicated dataset");
//            evtBldr.setStatus(Status.IN_PROGRESS);// ??
            evtBldr.setOrigin("1001");
            evtBldr.setDatetime(IonTime.now().getMillis());
            /* Add the TriggerEventMessage */
            evtBldr.setAdditionalData(trigMsgWrap.getCASRef());
            GPBWrapper evtWrap = GPBWrapper.Factory(evtBldr.build());

            net.ooici.core.message.IonMessage.IonMsg.Builder ionMsgBldr = net.ooici.core.message.IonMessage.IonMsg.newBuilder();
//        ionMsgBldr.setName("Dataset Update Event");//deprecated
            ionMsgBldr.setIdentity(UUID.randomUUID().toString());
            ionMsgBldr.setMessageObject(evtWrap.getCASRef());
            GPBWrapper ionMsgWrap = GPBWrapper.Factory(ionMsgBldr.build());

            /* Add everything to a Container.Structure */
            net.ooici.core.container.Container.Structure.Builder structBldr = net.ooici.core.container.Container.Structure.newBuilder();
//            ProtoUtils.addStructureElementToStructureBuilder(structBldr, idWrap.getStructureElement());
//            ProtoUtils.addStructureElementToStructureBuilder(structBldr, resModWrap.getStructureElement());
            ProtoUtils.addStructureElementToStructureBuilder(structBldr, updMsgWrap.getStructureElement());
            ProtoUtils.addStructureElementToStructureBuilder(structBldr, trigMsgWrap.getStructureElement());
            ProtoUtils.addStructureElementToStructureBuilder(structBldr, evtWrap.getStructureElement());
            ProtoUtils.addStructureElementToStructureBuilder(structBldr, ionMsgWrap.getStructureElement(), true);// add as head element

            Container.Structure struct = structBldr.build();
            GPBWrapper structWrap = GPBWrapper.Factory(struct);
            System.out.println(structWrap);

            IonMessage sendMessage = mainBroker.createMessage(ooiMyName, ooiToName, "", struct.toByteArray());
            sendMessage.getIonHeaders().put("encoding", "ION R1 GPB");
            sendMessage.getIonHeaders().put("user-id", "ANONYMOUS");
            sendMessage.getIonHeaders().put("expiry", "0");
            sendMessage.getIonHeaders().put("performative", "request");
            mainBroker.sendMessage(sendMessage);

            System.exit(0);
        } finally {
            if (mainBroker != null) {
                mainBroker.detach();
                mainBroker = null;
            }
        }
    }

    private static String usageString() {
        String nl = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder();
        sb.append("genUpdate <dataset_id> [connection_info_path]").append(nl);
        sb.append(nl);
        sb.append("\tdataset_id (required) ==> GUID of OOICI dataset resource that should be updated").append(nl);
        sb.append("\tconnection_info_path (optional) ==> Path to the \"ooici_conn.properties\" file to use for connecting to OOICI.  Defaults to the file located in the current user's home directory.").append(nl);

        return sb.toString();
    }
}
