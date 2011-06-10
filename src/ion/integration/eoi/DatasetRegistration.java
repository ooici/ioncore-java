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
import ion.core.utils.StructureManager;
import ion.integration.ais.AppIntegrationService;
import ion.integration.ais.AppIntegrationService.RequestType;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import net.ooici.core.container.Container;
import net.ooici.core.link.Link.CASRef;
import net.ooici.integration.ais.AisRequestResponse;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
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
    private String ooiciHost = "";
    private String ooiciExchange = "";
    private String ooiciSysname = "";
    private String ooiciTopic = "";
    private String mainQueue;
    private MessagingName ooiRegistrationName;
    private MessagingName ooiMyName;

    public DatasetRegistration(String dirOrFile) {
        MsgBrokerClient mainBroker = null;
        try {
            File[] regFiles = null;
            File dir = new File(dirOrFile);
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
                String errorMsg = "The passed argument is not a valid \".dsreg\" file or a directory containing \".dsreg\" files.  Cannot continue.";
                log.error(errorMsg);
                throw new IonException(errorMsg);
            }

            try {
                java.util.HashMap<String, String> connInfo = IonUtils.parseProperties();
                if (connInfo != null && !connInfo.isEmpty()) {
                    ooiciHost = connInfo.get("host");
                    ooiciExchange = connInfo.get("xp_name");
                    ooiciSysname = connInfo.get("sysname");
                    ooiciTopic = connInfo.get("dataset_registration_topic");
                    if (log.isDebugEnabled()) {
                        log.debug("OOICI IOSP Connection Parameters:: host={} : exchange={} : topic={} : sysname={}", new Object[]{ooiciHost, ooiciExchange, ooiciTopic, ooiciSysname});
                    }
                } else {
                    throw new IOException("Error parsing OOICI connection information cannot continue.");
                }


                ooiRegistrationName = new MessagingName(ooiciSysname, ooiciTopic);
                ooiMyName = MessagingName.generateUniqueName();
                mainBroker = new MsgBrokerClient(ooiciHost, AMQP.PROTOCOL.PORT, ooiciExchange);
                try {
                    mainBroker.attach();
                    if (log.isDebugEnabled()) {
                        log.debug("Broker Attached:: myBindingKey={}", ooiMyName.toString());
                    }
                } catch (IonException ex) {
                    throw new IOException("Error opening file: Could not connect to broker", ex);
                }
                mainQueue = mainBroker.declareQueue(null);
                mainBroker.bindQueue(mainQueue, ooiMyName, null);
                mainBroker.attachConsumer(mainQueue);

                /* register the temp user... TODO: Remove this! */
                String user_id = registerTestUser();
                if (log.isDebugEnabled()) {
                    log.debug("*********** USER_ID == {} ***********", user_id);
                }
            } catch (IOException ex) {
                log.error("Error connecting to ooici", ex);
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
                sb = new StringBuilder(nl).append(">>>>>>>>>>").append(nl);
                sb.append("Data resource file \"").append(filepath).append("\" registered successfully!").append(nl);
                try {
                    Container.Structure struct = DataResourceBuilder.getDataResourceCreateRequestStructure(filepath, sb);
                    if (log.isDebugEnabled()) {
                        log.debug(GPBWrapper.Factory(struct).toString());
                    }

                    IonMessage sendMessage = mainBroker.createMessage(ooiMyName, ooiRegistrationName, "createDataResource", struct.toByteArray());
                    sendMessage.getIonHeaders().put("encoding", "ION R1 GPB");
                    sendMessage.getIonHeaders().put("performative", "request");
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
                                if(log.isInfoEnabled()) {
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

// <editor-fold defaultstate="collapsed" desc="Registration of User for testing">
    private String registerTestUser() {
        AppIntegrationService ais = null;
        String user_id = null;
        try {

            ais = new AppIntegrationService(ooiciSysname, ooiciHost, AMQP.PROTOCOL.PORT, ooiciExchange);

            // Register user
            String requestJsonString = "{\"certificate\": \"" + certificate + "\",";
            requestJsonString += "\"rsa_private_key\": \"" + privateKey + "\"}";
            String replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.REGISTER_USER, "ANONYMOUS", "0");
            if (log.isDebugEnabled()) {
                log.debug("REGISTER_USER");
                log.debug("  request: " + requestJsonString);
                log.debug("  reply: " + replyJsonString);
            }
            if (ais.getStatus() != 200) {
                log.error("  error string: " + ais.getErrorMessage());
            }

            JSONObject registerUserResp = (JSONObject) JSONValue.parse(replyJsonString);
            user_id = registerUserResp.get("ooi_id").toString();
        } finally {
            if (ais != null) {
                ais.dispose();
                ais = null;
            }
        }
        return user_id;

    }
    private static String certificate = "-----BEGIN CERTIFICATE-----\\n"
            + "MIIEUzCCAzugAwIBAgICBgIwDQYJKoZIhvcNAQELBQAwazETMBEGCgmSJomT8ixkARkWA29yZzEX\\n"
            + "MBUGCgmSJomT8ixkARkWB2NpbG9nb24xCzAJBgNVBAYTAlVTMRAwDgYDVQQKEwdDSUxvZ29uMRww\\n"
            + "GgYDVQQDExNDSUxvZ29uIE9wZW5JRCBDQSAxMB4XDTExMDQyMTE5MzMyMVoXDTExMDQyMjA3Mzgy\\n"
            + "MVowZTETMBEGCgmSJomT8ixkARkTA29yZzEXMBUGCgmSJomT8ixkARkTB2NpbG9nb24xCzAJBgNV\\n"
            + "BAYTAlVTMQ8wDQYDVQQKEwZHb29nbGUxFzAVBgNVBAMTDnRlc3QgdXNlciBBNTAxMIIBIjANBgkq\\n"
            + "hkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAu+SQwAWMAY/+6eZjcirp0YfhKdgM06uZmTU9DPJqcNXF\\n"
            + "ROFCeGEkg2jzgfcK5NiT662YbQkxETWDl4XZazmbPv787XJjYnbF8XErztauE3+caWNOpob2yPDt\\n"
            + "mk3F0I0ullSbqsxPvsYAZNEveDBFzxCeeO+GKFQnw12ZYo968RcyZW2Fep9OQ4VfpWQExSA37FA+\\n"
            + "4KL0RfZnd8Vc1ru9tFPw86hEstzC0Lt5HuXUHhuR9xsW3E5xY7mggHOrZWMQFiUN8WPnrHSCarwI\\n"
            + "PQDKv8pMQ2LIacU8QYzVow74WUjs7hMd3naQ2+QgRd7eRc3fRYXPPNCYlomtnt4OcXcQSwIDAQAB\\n"
            + "o4IBBTCCAQEwDAYDVR0TAQH/BAIwADAOBgNVHQ8BAf8EBAMCBLAwEwYDVR0lBAwwCgYIKwYBBQUH\\n"
            + "AwIwGAYDVR0gBBEwDzANBgsrBgEEAYKRNgEDAzBsBgNVHR8EZTBjMC+gLaArhilodHRwOi8vY3Js\\n"
            + "LmNpbG9nb24ub3JnL2NpbG9nb24tb3BlbmlkLmNybDAwoC6gLIYqaHR0cDovL2NybC5kb2Vncmlk\\n"
            + "cy5vcmcvY2lsb2dvbi1vcGVuaWQuY3JsMEQGA1UdEQQ9MDuBEW15b29pY2lAZ21haWwuY29thiZ1\\n"
            + "cm46cHVibGljaWQ6SUROK2NpbG9nb24ub3JnK3VzZXIrQTUwMTANBgkqhkiG9w0BAQsFAAOCAQEA\\n"
            + "Omon3wMV3RFzs28iqs+r1j9WxLSvQXRXtk3BMNNmrobDspb2rodiNGMeVxGD2oGSAfh1Mn/l+vDE\\n"
            + "1333XzQ3BGkucaSSBOTll5ZBqf52w/ru/dyrJ2GvHbIrKv+QkpKuP9uB0eJYi1n7+q/23rBR5V+E\\n"
            + "+LsnTG8BcuzpFxtlY4SKIsijHNV+5y2+hfGHiNGfAr3X8FfwjIfmqBroCRc01ix8+jMnvplLr5rp\\n"
            + "Wkkk8zr1nuzaUjNA/8G+24UBNSgLYOUP/xH2GlPUiAP4tZX+zGsOVkYkbyc67M4TLyD3hxuLbDCU\\n"
            + "Aw3E0TjYpPxuQ8OsJ1LdECRfHgHFfd5KtG8BgQ==\\n"
            + "-----END CERTIFICATE-----";
    private static String privateKey = "-----BEGIN RSA PRIVATE KEY-----\\n"
            + "MIIEowIBAAKCAQEAu+SQwAWMAY/+6eZjcirp0YfhKdgM06uZmTU9DPJqcNXFROFCeGEkg2jzgfcK\\n"
            + "5NiT662YbQkxETWDl4XZazmbPv787XJjYnbF8XErztauE3+caWNOpob2yPDtmk3F0I0ullSbqsxP\\n"
            + "vsYAZNEveDBFzxCeeO+GKFQnw12ZYo968RcyZW2Fep9OQ4VfpWQExSA37FA+4KL0RfZnd8Vc1ru9\\n"
            + "tFPw86hEstzC0Lt5HuXUHhuR9xsW3E5xY7mggHOrZWMQFiUN8WPnrHSCarwIPQDKv8pMQ2LIacU8\\n"
            + "QYzVow74WUjs7hMd3naQ2+QgRd7eRc3fRYXPPNCYlomtnt4OcXcQSwIDAQABAoIBAE7JjC0I5mlt\\n"
            + "US4RbpfcCMnU2YTrVI2ZwkGtQllgeWOxMBQvBOlniqET7DAOQGIvsu87jtQB67JUp0ZtWPsOX9vt\\n"
            + "nm+O7L/IID6a/wyvlrUUaKkEfGF17Jvb8zYl8JH/8Y4WEmRvYe0UJ+wej3Itg8hNJrZ9cdsNVtMk\\n"
            + "N4JNufbH0+s2t+nZPm7jLNbXfdP6CIiyTB6OIB9M3JRKed5lpFOOsTB0HNgBFGaZvmmzWpGQJ6wQ\\n"
            + "YsEWbMiFrB4e8qutfF+itzq5cyMrMVsAJiecMfc/j1gv+77wSi3x6tqYWgLsk5jZBNm99UM/nxWp\\n"
            + "Xl+091gN7aha9DQ1WmCpG+D6h4kCgYEA7AuKIn/m4riQ7PsuGKNIU/h8flsO+op5FUP0NBRBY8Mc\\n"
            + "LTon/QBcZTqpkWYblkz/ME8AEuPWKsPZQrCO9sCFRBMk0L5IZQ43kr2leB43iHDhc+OsjDB0sV8M\\n"
            + "oEWCI4BFu7wrtbmYTqJhQaHBh0lu3jWmKnaMkWIXsF2nvqDt7VcCgYEAy8brqFssASiDFJsZB1kK\\n"
            + "AzVkM0f43/+51fzdPW6YnrxOMt3nQqzUOF1FlmvMog/fRPjcfcttdjVu12s9DljB0AaMoBRxmKcj\\n"
            + "/mIvxPNrTBhAHeqowZ0XyCtgEl8c+8sZUi1hUmnCIDFvi9LKXbX/mnXp0aKqWD03Hnbm/o3vaC0C\\n"
            + "gYEAmrcFl49V+o0XEP2iPSvpIIDiuL9elgFlU/byfaA5K/aa5VoVE9PEu+Uzd8YBlwZozXU6iycj\\n"
            + "HWy5XujzC/EsaG5T1y6hrPsgmeIMLys/IwM6Awfb9RddpVSzpelpX3OYQXEZBUfc+M2eCbLIcrBD\\n"
            + "JwrrGzIQ+Mne1Q7OADjjOokCgYABgHbOJ9XcMFM+/KGjlzlmqqcRZa9k3zqcZB+xSzZevR6Ka24/\\n"
            + "5Iwv2iggIq1AaIOJu5fMaYpl+6DUf5rUlzzebp3stBneOSUfw9N8TRr2VZtrXQZfXuwE8qTjncXV\\n"
            + "6TpHi8QS2mqu2A5tZmFNbYDzv3i4rc05l0HnvJKZP6yLBQKBgERpUxpX4r5Obi8PNIECZ4ucTlhT\\n"
            + "KJpn8B+9GrIjTqs+ae0oRfbSo1Jt/SDts/c6DYaT2RZma7JVosWd2aOAw9k69zMObHlJrcHGmb3l\\n"
            + "eCc/SSPAJvor9B8dBoTQZbaAF4js/wffMl2Qg1WuFfyRQIAhHYO1I9aibqcJmSwDKmsL\\n"
            + "-----END RSA PRIVATE KEY-----";
// </editor-fold>
}
