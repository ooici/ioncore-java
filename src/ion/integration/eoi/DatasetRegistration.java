/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ion.integration.eoi;

import ion.core.IonException;
import ion.core.messaging.IonMessage;
import ion.core.messaging.MessagingName;
import ion.core.messaging.MsgBrokerClient;
import ion.core.utils.GPBWrapper;
import ion.core.utils.IonConstants;
import ion.core.utils.StructureManager;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.ooici.core.container.Container;
import net.ooici.core.link.Link.CASRef;
import net.ooici.integration.ais.AisRequestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cmueller
 */
public class DatasetRegistration {

    private static final Logger log = LoggerFactory.getLogger(DatasetRegistration.class);

    public static void main(String[] args) {
        if (args.length < 1) {
            log.error("Must provide an argument specifying a \".dsreg\" file or a directory containing \".dsreg\" files; cannot continue");
            System.exit(1);
        }
        try {
            DatasetRegistration dataReg = new DatasetRegistration(args[0]);
            System.exit(0);
        } catch (Exception ex) {
            log.error("Error creating resource", ex);
            System.exit(1);
        }
    }
    private String ooiciSysname = "";
    private String ooiciTopic = "";
    private String mainQueue;
    private MessagingName ooiRegistrationName;
    private MessagingName ooiMyName;

    public DatasetRegistration(String dirOrFile) {
        MsgBrokerClient mainBroker = null;
        try {
            boolean isDir;
            File[] regFiles = null;
            File dir = new File(dirOrFile);
            if ((isDir = dir.isDirectory())) {
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
                String errorMsg = "The passed argument is not a valid \".dsreg\" file or a directory containing \".dsreg\" files.  Cannot continue.";
                log.error(errorMsg);
                throw new IonException(errorMsg);
            }

            try {

                mainBroker = new MsgBrokerClient();
                ooiciSysname = System.getProperty(IonConstants.SYSNAME_KEY, IonConstants.SYSNAME_DEFAULT);
                ooiciTopic = System.getProperty(IonConstants.DATASET_REGISTRATION_TOPIC_KEY, IonConstants.DATASET_REGISTRATION_TOPIC_DEFAULT);

                if (log.isInfoEnabled()) {
                    log.info("OOICI Connection Parameters:: host={} : exchange={} : topic={} : sysname={}", new Object[]{System.getProperty(IonConstants.HOSTNAME_KEY, IonConstants.HOSTNAME_DEFAULT),
                                System.getProperty(IonConstants.EXCHANGE_KEY, IonConstants.EXCHANGE_DEFAULT), ooiciTopic, ooiciSysname});
                }


                ooiRegistrationName = new MessagingName(ooiciSysname, ooiciTopic);
                ooiMyName = MessagingName.generateUniqueName();
                try {
                    mainBroker.attach();
                    if (log.isDebugEnabled()) {
                        log.debug("MainBroker:: binding_key={} : to_name={} : queue_name={}", new Object[]{ooiMyName, ooiRegistrationName, mainQueue});
                    }
                } catch (IonException ex) {
                    throw new IOException("Could not connect to broker", ex);
                }
                mainQueue = mainBroker.declareQueue(null);
                mainBroker.bindQueue(mainQueue, ooiMyName, null);
                mainBroker.attachConsumer(mainQueue);
            } catch (IOException ex) {
                log.error("There was a problem connecting to ooici", ex);
                System.exit(1);
            }

            String nl = System.getProperty("line.separator");
            StringBuilder sb;
            for (File regFile : regFiles) {
                String filepath = "";
                try {
                    filepath = regFile.getCanonicalPath();
                } catch (IOException ex) {
                    throw new IonException("Could not resolve file path", ex);
                }
                sb = new StringBuilder(nl).append(">>>>>>>>>>").append("Registering .dsreg file \"").append(filepath).append("\"").append(nl);
                sb.append("Data resource file \"").append(filepath).append("\" registered successfully!").append(nl);
                try {
                    String user_id = null;
                    Container.Structure struct = DataResourceBuilder.getDataResourceCreateRequestStructure(filepath, sb);
                    if (log.isDebugEnabled()) {
                        log.debug(GPBWrapper.Factory(struct).toString());
                    }
                    Pattern p = Pattern.compile("user_id: \"([0-9A-Za-z-]+)\"");
                    Matcher m = p.matcher(sb.toString());
                    m.find();
                    user_id = sb.substring(m.start(1), m.end(1));

                    IonMessage sendMessage = mainBroker.createMessage(ooiMyName, ooiRegistrationName, "createDataResource", struct.toByteArray());
                    sendMessage.getIonHeaders().put("encoding", "ION R1 GPB");
                    sendMessage.getIonHeaders().put("performative", "request");
                    sendMessage.getIonHeaders().put("user-id", user_id);
                    sendMessage.getIonHeaders().put("expiry", "0");
                    if (log.isDebugEnabled()) {
                        log.debug(sendMessage.toString());
                    }

                    /* Send the message to the datastore*/
                    mainBroker.sendMessage(sendMessage);
                    IonMessage repMessage = mainBroker.consumeMessage(mainQueue);//default timeout is 30 seconds
                    mainBroker.ackMessage(repMessage);
                    if (log.isDebugEnabled()) {
                        log.debug(repMessage.toString());
                    }
                    if (!repMessage.isErrorMessage()) {
                        StructureManager sm = StructureManager.Factory(repMessage);
                        GPBWrapper<net.ooici.core.message.IonMessage.IonMsg> ionMsgWrap = sm.getObjectWrapper(sm.getHeadId());
                        net.ooici.core.message.IonMessage.IonMsg ionMsg = ionMsgWrap.getObjectValue();

                        GPBWrapper<?> aisRespWrap = sm.getObjectWrapper(ionMsg.getMessageObject());
                        switch (aisRespWrap.getObjectID()) {
                            case 9002:
                                AisRequestResponse.ApplicationIntegrationServiceResponseMsg resp = (AisRequestResponse.ApplicationIntegrationServiceResponseMsg) aisRespWrap.getObjectValue();
                                for (CASRef ref : resp.getMessageParametersReferenceList()) {
                                    GPBWrapper wrap = sm.getObjectWrapper(ref);
                                    if (log.isDebugEnabled()) {
                                        log.debug(wrap.toString());
                                    }
                                    if (log.isInfoEnabled()) {
                                        sb.append(nl).append("Data resource details:").append(nl);
                                        sb.append(wrap.getObjectValue());
                                        sb.append("<<<<<<<<<<").append(nl);
                                        log.info(sb.toString());
                                    }
                                }
                                break;
                            case 9003:
                                AisRequestResponse.ApplicationIntegrationServiceError err = (AisRequestResponse.ApplicationIntegrationServiceError) aisRespWrap.getObjectValue();
                                if (log.isInfoEnabled()) {
                                    sb.append(nl).append("Error registering data resource:").append(nl);
                                    sb.append("Error #").append(err.getErrorNum()).append(": {").append(err.getErrorStr()).append("}").append(nl);
                                    log.info(sb.toString());
                                }
                        }
                    } else {
                        StructureManager sm = StructureManager.Factory(repMessage);
                        GPBWrapper headWrap = sm.getObjectWrapper(sm.getHeadId());
                        throw new IonException("Error returned:" + headWrap.toString());
                    }

                } catch (Exception ex) {
                    throw new IonException("Error creating resource for dsreg file: " + filepath, ex);
                }
                if (isDir) {
                    try {
                        log.info("Waiting 10 seconds before registering next \".dsreg\" file...");
                        Thread.sleep(10000);
                    } catch (InterruptedException ex) {
                        log.error("Thread wait failed - proceeding to next \".dsreg\" file...", ex);
                    }
                }
            }
        } finally {
            if (mainBroker != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Broker Detached");
                }
                mainBroker.detach();
                mainBroker = null;
            }
        }
    }
}
