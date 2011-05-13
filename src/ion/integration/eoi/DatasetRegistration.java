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
import ion.core.utils.IonUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cmueller
 */
public class DatasetRegistration {

    private static final Logger log = LoggerFactory.getLogger(DataResourceBuilder.class);
    
    private static String ooiciHost = "";
    private static String ooiciExchange = "";
    private static String ooiciSysname = "";
    private static String ooiciTopic = "";
    private static String mainQueue;
    private static MessagingName ooiRegistrationName;
    private static MessagingName ooiMyName;

    public static void main(String[] args) {
        MsgBrokerClient mainBroker = null;
        try {
            File[] regFiles = null;
            File dir = new File("json");
            if (dir.isDirectory()) {
                regFiles = dir.listFiles(new FilenameFilter() {

                    public boolean accept(File dir, String name) {
                        return name.endsWith(".dsreg");
                    }
                });
            } else {
                if (dir.getName().endsWith(".dsreg")) {
                    regFiles = new File[]{dir};
                }
            }

            if (regFiles == null) {
                log.error("The passed argument is not a valid \".dsreg\" file or a directory containing \".dsreg\" files.  Cannot continue.");
                System.exit(1);
            }

            try {
                java.util.HashMap<String, String> connInfo = IonUtils.parseProperties();
                if (connInfo != null && !connInfo.isEmpty()) {
                    ooiciHost = connInfo.get("host");
                    ooiciExchange = connInfo.get("xp_name");
                    ooiciSysname = connInfo.get("sysname");
                    ooiciTopic = connInfo.get("dataset_registration_topic");
                    log.debug("OOICI IOSP Connection Parameters:: host={} : exchange={} : topic={} : sysname={}", new Object[]{ooiciHost, ooiciExchange, ooiciTopic, ooiciSysname});
                } else {
                    throw new IOException("Error parsing OOICI connection information cannot continue.");
                }


                ooiRegistrationName = new MessagingName(ooiciSysname, ooiciTopic);
                ooiMyName = MessagingName.generateUniqueName();
                mainBroker = new MsgBrokerClient(ooiciHost, AMQP.PROTOCOL.PORT, ooiciExchange);
                try {
                    mainBroker.attach();
                    log.debug("Main Broker Attached:: myBindingKey={}", ooiMyName.toString());
                } catch (IonException ex) {
                    throw new IOException("Error opening file: Could not connect to broker", ex);
                }
                mainQueue = mainBroker.declareQueue(null);
                mainBroker.bindQueue(mainQueue, ooiMyName, null);
                mainBroker.attachConsumer(mainQueue);
            } catch (IOException ex) {
                log.error("Error connecting to ooici", ex);
                System.exit(1);
            }

            for (File regFile : regFiles) {

                try {
                    net.ooici.core.container.Container.Structure struct = DataResourceBuilder.getDataSourceResourceStructure(regFile.getCanonicalPath());
                    log.debug(GPBWrapper.Factory(struct).toString());
//            log.debug("\n\n\nOutput:");
//            log.debug(JsonFormat.printToString(struct));

                    IonMessage sendMessage = mainBroker.createMessage(ooiMyName, ooiRegistrationName, "get_object", struct.toByteArray());
                    sendMessage.getIonHeaders().put("encoding", "ION R1 GPB");
                    sendMessage.getIonHeaders().put("user-id", "ANONYMOUS");
                    sendMessage.getIonHeaders().put("expiry", "0");
                    sendMessage.getIonHeaders().put("performative", "request");

                    /* Send the message to the datastore*/
                    mainBroker.sendMessage(sendMessage);
                    IonMessage repMessage = mainBroker.consumeMessage(mainQueue);//default timeout is 30 seconds
                    log.debug(repMessage.toString());

                } catch (FileNotFoundException ex) {
                    log.error("Error", ex);
                } catch (IOException ex) {
                    log.error("Error", ex);
                } catch (Exception ex) {
                    log.error("Error", ex);
                }
            }
        } finally {
            if (mainBroker != null) {
                log.debug("Data Broker Detached");
                mainBroker.detach();
                mainBroker = null;
            }
        }

    }
}
