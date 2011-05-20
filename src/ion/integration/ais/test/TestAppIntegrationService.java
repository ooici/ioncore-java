package ion.integration.ais.test;

import ion.core.utils.IonUtils;
import ion.integration.ais.AppIntegrationService;
import ion.integration.ais.AppIntegrationService.RequestType;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

import junit.framework.TestCase;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.rabbitmq.client.AMQP;

public class TestAppIntegrationService extends TestCase {

	private String SYSNAME_KEY = "ion.sysname";
	private String HOSTNAME_KEY = "ion.host";
	private String PORTNUMBER_KEY = "ion.port";
	private String EXCHANGE_KEY = "ion.exchange";

	private String SYSNAME_DEFAULT = "sysName-UNIQUE";
	private String HOSTNAME_DEFAULT = "localhost";
	private int PORTNUMBER_DEFAULT = AMQP.PROTOCOL.PORT;
	private String EXCHANGE_DEFAULT = "magnet.topic";

	private AppIntegrationService ais;

	private String certificate = "-----BEGIN CERTIFICATE-----\\n" +
	"MIIEUzCCAzugAwIBAgICBgIwDQYJKoZIhvcNAQELBQAwazETMBEGCgmSJomT8ixkARkWA29yZzEX\\n" +
	"MBUGCgmSJomT8ixkARkWB2NpbG9nb24xCzAJBgNVBAYTAlVTMRAwDgYDVQQKEwdDSUxvZ29uMRww\\n" +
	"GgYDVQQDExNDSUxvZ29uIE9wZW5JRCBDQSAxMB4XDTExMDQyMTE5MzMyMVoXDTExMDQyMjA3Mzgy\\n" +
	"MVowZTETMBEGCgmSJomT8ixkARkTA29yZzEXMBUGCgmSJomT8ixkARkTB2NpbG9nb24xCzAJBgNV\\n" +
	"BAYTAlVTMQ8wDQYDVQQKEwZHb29nbGUxFzAVBgNVBAMTDnRlc3QgdXNlciBBNTAxMIIBIjANBgkq\\n" +
	"hkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAu+SQwAWMAY/+6eZjcirp0YfhKdgM06uZmTU9DPJqcNXF\\n" +
	"ROFCeGEkg2jzgfcK5NiT662YbQkxETWDl4XZazmbPv787XJjYnbF8XErztauE3+caWNOpob2yPDt\\n" +
	"mk3F0I0ullSbqsxPvsYAZNEveDBFzxCeeO+GKFQnw12ZYo968RcyZW2Fep9OQ4VfpWQExSA37FA+\\n" +
	"4KL0RfZnd8Vc1ru9tFPw86hEstzC0Lt5HuXUHhuR9xsW3E5xY7mggHOrZWMQFiUN8WPnrHSCarwI\\n" +
	"PQDKv8pMQ2LIacU8QYzVow74WUjs7hMd3naQ2+QgRd7eRc3fRYXPPNCYlomtnt4OcXcQSwIDAQAB\\n" +
	"o4IBBTCCAQEwDAYDVR0TAQH/BAIwADAOBgNVHQ8BAf8EBAMCBLAwEwYDVR0lBAwwCgYIKwYBBQUH\\n" +
	"AwIwGAYDVR0gBBEwDzANBgsrBgEEAYKRNgEDAzBsBgNVHR8EZTBjMC+gLaArhilodHRwOi8vY3Js\\n" +
	"LmNpbG9nb24ub3JnL2NpbG9nb24tb3BlbmlkLmNybDAwoC6gLIYqaHR0cDovL2NybC5kb2Vncmlk\\n" +
	"cy5vcmcvY2lsb2dvbi1vcGVuaWQuY3JsMEQGA1UdEQQ9MDuBEW15b29pY2lAZ21haWwuY29thiZ1\\n" +
	"cm46cHVibGljaWQ6SUROK2NpbG9nb24ub3JnK3VzZXIrQTUwMTANBgkqhkiG9w0BAQsFAAOCAQEA\\n" +
	"Omon3wMV3RFzs28iqs+r1j9WxLSvQXRXtk3BMNNmrobDspb2rodiNGMeVxGD2oGSAfh1Mn/l+vDE\\n" +
	"1333XzQ3BGkucaSSBOTll5ZBqf52w/ru/dyrJ2GvHbIrKv+QkpKuP9uB0eJYi1n7+q/23rBR5V+E\\n" +
	"+LsnTG8BcuzpFxtlY4SKIsijHNV+5y2+hfGHiNGfAr3X8FfwjIfmqBroCRc01ix8+jMnvplLr5rp\\n" +
	"Wkkk8zr1nuzaUjNA/8G+24UBNSgLYOUP/xH2GlPUiAP4tZX+zGsOVkYkbyc67M4TLyD3hxuLbDCU\\n" +
	"Aw3E0TjYpPxuQ8OsJ1LdECRfHgHFfd5KtG8BgQ==\\n" +
	"-----END CERTIFICATE-----";
	private String privateKey = "-----BEGIN RSA PRIVATE KEY-----\\n" +
	"MIIEowIBAAKCAQEAu+SQwAWMAY/+6eZjcirp0YfhKdgM06uZmTU9DPJqcNXFROFCeGEkg2jzgfcK\\n" +
	"5NiT662YbQkxETWDl4XZazmbPv787XJjYnbF8XErztauE3+caWNOpob2yPDtmk3F0I0ullSbqsxP\\n" +
	"vsYAZNEveDBFzxCeeO+GKFQnw12ZYo968RcyZW2Fep9OQ4VfpWQExSA37FA+4KL0RfZnd8Vc1ru9\\n" +
	"tFPw86hEstzC0Lt5HuXUHhuR9xsW3E5xY7mggHOrZWMQFiUN8WPnrHSCarwIPQDKv8pMQ2LIacU8\\n" +
	"QYzVow74WUjs7hMd3naQ2+QgRd7eRc3fRYXPPNCYlomtnt4OcXcQSwIDAQABAoIBAE7JjC0I5mlt\\n" +
	"US4RbpfcCMnU2YTrVI2ZwkGtQllgeWOxMBQvBOlniqET7DAOQGIvsu87jtQB67JUp0ZtWPsOX9vt\\n" +
	"nm+O7L/IID6a/wyvlrUUaKkEfGF17Jvb8zYl8JH/8Y4WEmRvYe0UJ+wej3Itg8hNJrZ9cdsNVtMk\\n" +
	"N4JNufbH0+s2t+nZPm7jLNbXfdP6CIiyTB6OIB9M3JRKed5lpFOOsTB0HNgBFGaZvmmzWpGQJ6wQ\\n" +
	"YsEWbMiFrB4e8qutfF+itzq5cyMrMVsAJiecMfc/j1gv+77wSi3x6tqYWgLsk5jZBNm99UM/nxWp\\n" +
	"Xl+091gN7aha9DQ1WmCpG+D6h4kCgYEA7AuKIn/m4riQ7PsuGKNIU/h8flsO+op5FUP0NBRBY8Mc\\n" +
	"LTon/QBcZTqpkWYblkz/ME8AEuPWKsPZQrCO9sCFRBMk0L5IZQ43kr2leB43iHDhc+OsjDB0sV8M\\n" +
	"oEWCI4BFu7wrtbmYTqJhQaHBh0lu3jWmKnaMkWIXsF2nvqDt7VcCgYEAy8brqFssASiDFJsZB1kK\\n" +
	"AzVkM0f43/+51fzdPW6YnrxOMt3nQqzUOF1FlmvMog/fRPjcfcttdjVu12s9DljB0AaMoBRxmKcj\\n" +
	"/mIvxPNrTBhAHeqowZ0XyCtgEl8c+8sZUi1hUmnCIDFvi9LKXbX/mnXp0aKqWD03Hnbm/o3vaC0C\\n" +
	"gYEAmrcFl49V+o0XEP2iPSvpIIDiuL9elgFlU/byfaA5K/aa5VoVE9PEu+Uzd8YBlwZozXU6iycj\\n" +
	"HWy5XujzC/EsaG5T1y6hrPsgmeIMLys/IwM6Awfb9RddpVSzpelpX3OYQXEZBUfc+M2eCbLIcrBD\\n" +
	"JwrrGzIQ+Mne1Q7OADjjOokCgYABgHbOJ9XcMFM+/KGjlzlmqqcRZa9k3zqcZB+xSzZevR6Ka24/\\n" +
	"5Iwv2iggIq1AaIOJu5fMaYpl+6DUf5rUlzzebp3stBneOSUfw9N8TRr2VZtrXQZfXuwE8qTjncXV\\n" +
	"6TpHi8QS2mqu2A5tZmFNbYDzv3i4rc05l0HnvJKZP6yLBQKBgERpUxpX4r5Obi8PNIECZ4ucTlhT\\n" +
	"KJpn8B+9GrIjTqs+ae0oRfbSo1Jt/SDts/c6DYaT2RZma7JVosWd2aOAw9k69zMObHlJrcHGmb3l\\n" +
	"eCc/SSPAJvor9B8dBoTQZbaAF4js/wffMl2Qg1WuFfyRQIAhHYO1I9aibqcJmSwDKmsL\\n" +
	"-----END RSA PRIVATE KEY-----";

	public TestAppIntegrationService(String name) {
		super(name);
	}

	protected void setUp() {
		HashMap<String, String> propertyMap = null;
		try {
			propertyMap = IonUtils.parseProperties();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String sysName = (propertyMap != null && propertyMap.get(SYSNAME_KEY) != null) ? propertyMap.get(SYSNAME_KEY) : SYSNAME_DEFAULT;
		String hostName = (propertyMap != null && propertyMap.get(HOSTNAME_KEY) != null) ? propertyMap.get(HOSTNAME_KEY) : HOSTNAME_DEFAULT;
		int portNumber = (propertyMap != null && propertyMap.get(PORTNUMBER_KEY) != null) ? new Integer(propertyMap.get(PORTNUMBER_KEY)) : PORTNUMBER_DEFAULT;
		String exchange = (propertyMap != null && propertyMap.get(EXCHANGE_KEY) != null) ? propertyMap.get(EXCHANGE_KEY) : EXCHANGE_DEFAULT;
		
		System.out.println("Connecting to " + hostName + ":" + portNumber + ":" + exchange + ":" + sysName);

		ais = new AppIntegrationService(sysName, hostName, portNumber, exchange);
	}

	protected void tearDown() {
		ais.dispose();
		ais = null;
	}

	public void testBadRequests() {
		// No request data provided
		String replyJsonString = ais.sendReceiveUIRequest(null, RequestType.UPDATE_USER_PROFILE, "3f27a744-2c3e-4d2a-a98c-050b246334a3", "0");
		assertNull(replyJsonString);
		assertTrue(ais.getStatus() == 400);
		assertTrue(ais.getErrorMessage().equals("Request data not provided"));

		// Poorly formed JSON data provided
		String requestJsonString = "{\"user_ooi_id\": \"3f27a744-2c3e-4d2a-a98c-050b246334a3\";\"email_address\": \"my.email@gmail.com\"}";
		replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.UPDATE_USER_PROFILE, "3f27a744-2c3e-4d2a-a98c-050b246334a3", "0");
		assertNull(replyJsonString);
		assertTrue(ais.getStatus() == 400);
		assertTrue(ais.getErrorMessage().equals("Exception encoding JSON to GPB: com.google.protobuf.JsonFormat$ParseException: 1:55: Expected identifier. -;"));
	}

	public void testGoodRequests() {
		try {
			FileOutputStream out = new FileOutputStream("JSONFormatExamples.txt");
			PrintStream printStream = new PrintStream(out);

			// Register user
			String requestJsonString = "{\"certificate\": \"" + certificate + "\",";
			requestJsonString += "\"rsa_private_key\": \"" + privateKey + "\"}";
			String replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.REGISTER_USER, "ANONYMOUS", "0");
			printStream.println("REGISTER_USER");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.startsWith("{\"ooi_id\":"));

			JSONObject registerUserResp = (JSONObject)JSONValue.parse(replyJsonString);
			String ooi_id = registerUserResp.get("ooi_id").toString();

//			// create instrument
//			requestJsonString = "{\"name\": \"SeaBird SBE37\",\"description\": \"SeaBird Sensor\", \"manufacturer\": \"SeaBird Electronics\", \"model\": \"SBE37\", \"serial_num\": \"123ABC\",\"fw_version\": \"1.0\"}";
//			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.CREATE_INSTRUMENT, ooi_id, "0");
//			printStream.println("CREATE_INSTRUMENT");
//			printStream.println("  request: " + requestJsonString);
//			printStream.println("  reply: " + replyJsonString);
//			if (ais.getStatus() != 200) {
//				printStream.println("  error string: " + ais.getErrorMessage());
//			}
//			assertTrue(ais.getStatus() == 200);
//			assertTrue(replyJsonString.startsWith("{\"instrument_resource_id\": "));
//
//			JSONObject createInstrumentResp = (JSONObject)JSONValue.parse(replyJsonString);
//			String instrument_resource_id = createInstrumentResp.get("instrument_resource_id").toString();
//
//			// start instrument agent
//			requestJsonString = "{\"name\": \"SeaBird SBE37\", \"model\": \"SBE37\", \"instrument_resource_id\": \"" + instrument_resource_id + "\"}";
//			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.START_INSTRUMENT_AGENT, ooi_id, "0");
//			printStream.println("START_INSTRUMENT_AGENT");
//			printStream.println("  request: " + requestJsonString);
//			printStream.println("  reply: " + replyJsonString);
//			if (ais.getStatus() != 200) {
//				printStream.println("  error string: " + ais.getErrorMessage());
//			}
//			assertTrue(ais.getStatus() == 200);
//			assertTrue(replyJsonString.startsWith("{\"instrument_resource_id\": "));
//
//			// get instrument list
//			requestJsonString = "{}";
//			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.GET_INSTRUMENT_LIST, ooi_id, "0");
//			printStream.println("GET_INSTRUMENT_LIST");
//			printStream.println("  request: " + requestJsonString);
//			printStream.println("  reply: " + replyJsonString);
//			if (ais.getStatus() != 200) {
//				printStream.println("  error string: " + ais.getErrorMessage());
//			}
//			assertTrue(ais.getStatus() == 200);
//			assertTrue(replyJsonString.startsWith("{\"result\": "));
//
//			// start instrument sampling
//			requestJsonString = "{\"instrument_resource_id\": \"" + instrument_resource_id + "\"}";
//			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.START_INSTRUMENT_SAMPLING, ooi_id, "0");
//			printStream.println("START_INSTRUMENT_SAMPLING");
//			printStream.println("  request: " + requestJsonString);
//			printStream.println("  reply: " + replyJsonString);
//			if (ais.getStatus() != 200) {
//				printStream.println("  error string: " + ais.getErrorMessage());
//			}
//			assertTrue(ais.getStatus() == 200);
//			assertTrue(replyJsonString.startsWith("{\"result\": "));
//
//			// stop instrument sampling
//			requestJsonString = "{\"instrument_resource_id\": \"" + instrument_resource_id + "\"}";
//			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.STOP_INSTRUMENT_SAMPLING, ooi_id, "0");
//			printStream.println("STOP_INSTRUMENT_SAMPLING");
//			printStream.println("  request: " + requestJsonString);
//			printStream.println("  reply: " + replyJsonString);
//			if (ais.getStatus() != 200) {
//				printStream.println("  error string: " + ais.getErrorMessage());
//			}
//			assertTrue(ais.getStatus() == 200);
//			assertTrue(replyJsonString.startsWith("{\"result\": "));
//
//			// get instrument state
//			requestJsonString = "{\"instrument_resource_id\": \"" + instrument_resource_id + "\"}";
//			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.GET_INSTRUMENT_STATE, ooi_id, "0");
//			printStream.println("GET_INSTRUMENT_STATE");
//			printStream.println("  request: " + requestJsonString);
//			printStream.println("  reply: " + replyJsonString);
//			if (ais.getStatus() != 200) {
//				printStream.println("  error string: " + ais.getErrorMessage());
//			}
//			assertTrue(ais.getStatus() == 200);
//			assertTrue(replyJsonString.startsWith("{\"result\": "));
//
//			// set instrument state
//			requestJsonString = "{\"instrument_resource_id\": \"" + instrument_resource_id + "\"}";
//			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.SET_INSTRUMENT_STATE, ooi_id, "0");
//			printStream.println("SET_INSTRUMENT_STATE");
//			printStream.println("  request: " + requestJsonString);
//			printStream.println("  reply: " + replyJsonString);
//			if (ais.getStatus() != 200) {
//				printStream.println("  error string: " + ais.getErrorMessage());
//			}
//			assertTrue(ais.getStatus() == 200);
//			assertTrue(replyJsonString.startsWith("{\"result\": "));

			// Update user profile
			requestJsonString = "{\"user_ooi_id\": \"" + ooi_id + "\",\"name\": \"MyOOICI\",\"institution\": \"OOICI\",\"email_address\": \"myooici@gmail.com\", \"profile\": [{\"name\": \"mobile\",\"value\": \"555-555-5555\"}]}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.UPDATE_USER_PROFILE, ooi_id, "0");
			printStream.println("UPDATE_USER_PROFILE");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString == null);

			// get user profile
			requestJsonString = "{\"user_ooi_id\": \"" + ooi_id + "\"}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.GET_USER_PROFILE, ooi_id, "0");
			printStream.println("GET_USER_PROFILE");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.startsWith("{\"name\": \"MyOOICI\",\"institution\": \"OOICI\",\"email_address\": \"myooici@gmail.com\",\"authenticating_organization\": \"Google\",\"profile\": [{\"name\": \"mobile\",\"value\": \"555-555-5555\"}"));

			// Get data resources
			requestJsonString = "{}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.FIND_DATA_RESOURCES, ooi_id, "0");
			printStream.println("FIND_DATA_RESOURCES");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.startsWith("{\"dataResourceSummary\":"));

			// Grab stuff for use in later calls
			JSONObject dataResourceSummary = (JSONObject)JSONValue.parse(replyJsonString);
			JSONArray array = (JSONArray)dataResourceSummary.get("dataResourceSummary");
			JSONObject datasetMetadata = (JSONObject)array.get(0);
			JSONObject dataResourceMetadataObj = (JSONObject)datasetMetadata.get("datasetMetadata");
			String dataResourceMetadata = dataResourceMetadataObj.toString();
			dataResourceMetadata = dataResourceMetadata.replaceAll("\\\\/", "/");
			String dataResourceId = dataResourceMetadataObj.get("data_resource_id").toString();

			requestJsonString = "{\"user_ooi_id\": \"" + ooi_id + "\"}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.FIND_DATA_RESOURCES_BY_USER, ooi_id, "0");
			printStream.println("FIND_DATA_RESOURCES_BY_USER");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.startsWith("{\"datasetByOwnerMetadata\":"));

			// Get data resource details
			requestJsonString = "{\"data_resource_id\": \"" + dataResourceId + "\"}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.GET_DATA_RESOURCE_DETAIL, ooi_id, "0");
			printStream.println("GET_DATA_RESOURCE_DETAIL");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.startsWith("{\"data_resource_id\": "));

			// Create download URL
			requestJsonString = "{\"user_ooi_id\": \"" + ooi_id + "\",\"data_resource_id\": \"" + dataResourceId + "\"}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.CREATE_DOWNLOAD_URL, ooi_id, "0");
			printStream.println("CREATE_DOWNLOAD_URL");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.startsWith("{\"download_url\":"));

			// Create subscription
			requestJsonString = "{\"subscriptionInfo\": {\"user_ooi_id\": \"" + ooi_id + "\",\"data_src_id\": \"" + dataResourceId + "\",\"subscription_type\": \"EMAIL\", \"email_alerts_filter\": \"UPDATES\", \"dispatcher_alerts_filter\": \"UPDATES\",\"dispatcher_script_path\": \"path\"}, \"datasetMetadata\": " + dataResourceMetadata + "}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.CREATE_DATA_RESOURCE_SUBSCRIPTION, ooi_id, "0");
			printStream.println("CREATE_DATA_RESOURCE_SUBSCRIPTION");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.equals("{\"success\": true}"));

			// Find subscription
			requestJsonString = "{\"user_ooi_id\": \"" + ooi_id + "\"}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.FIND_DATA_RESOURCE_SUBSCRIPTION, ooi_id, "0");
			printStream.println("FIND_DATA_RESOURCE_SUBSCRIPTION");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.startsWith("{\"subscriptionListResults\":"));

			// Update subscription
			requestJsonString = "{\"subscriptionInfo\": {\"user_ooi_id\": \"" + ooi_id + "\",\"data_src_id\": \"" + dataResourceId + "\",\"subscription_type\": \"EMAIL\", \"email_alerts_filter\": \"UPDATES\", \"dispatcher_alerts_filter\": \"UPDATES\",\"dispatcher_script_path\": \"newpath\"}, \"datasetMetadata\": " + dataResourceMetadata + "}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.UPDATE_DATA_RESOURCE_SUBSCRIPTION, ooi_id, "0");
			printStream.println("UPDATE_DATA_RESOURCE_SUBSCRIPTION");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.equals("{\"success\": true}"));

			// Delete subscription
			requestJsonString = "{\"subscriptionInfo\": {\"user_ooi_id\": \"" + ooi_id + "\",\"data_src_id\": \"" + dataResourceId + "\",\"subscription_type\": \"EMAIL\", \"email_alerts_filter\": \"UPDATES\", \"dispatcher_alerts_filter\": \"UPDATES\",\"dispatcher_script_path\": \"newpath\"}}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.DELETE_DATA_RESOURCE_SUBSCRIPTION, ooi_id, "0");
			printStream.println("DELETE_DATA_RESOURCE_SUBSCRIPTION");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.equals("{\"success\": true}"));

			// Validate data resource good case
			requestJsonString = "{\"data_resource_url\": \"http://geoport.whoi.edu/thredds/dodsC/usgs/data0/rsignell/data/oceansites/OS_WHOTS_2010_R_M-1.nc\"}";
//			requestJsonString = "{\"data_resource_url\": \"http://geoport.whoi.edu/thredds/dodsC/usgs/data0/rsignell/data/oceansites/OS_NTAS_2010_R_M-1.nc\"}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.VALIDATE_DATA_RESOURCE, ooi_id, "0");
			printStream.println("VALIDATE_DATA_RESOURCE");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.startsWith("{\"dataResourceSummary\": "));
			
			// Validate data resource fail case
			requestJsonString = "{\"data_resource_url\": \"http://hfrnet.ucsd.edu:8080/thredds/dodsC/HFRNet/USEGC/6km/hourly/RTV\"}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.VALIDATE_DATA_RESOURCE, ooi_id, "0");
			printStream.println("VALIDATE_DATA_RESOURCE");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + ais.getErrorMessage());
			assertTrue(ais.getStatus() == 500);
			assertTrue(ais.getErrorMessage().startsWith("{\"error_num\": 500,\"error_str\":"));

			// Create data resource
			requestJsonString = "{\"user_id\": \"" + ooi_id + "\",\"source_type\": 1,\"request_type\": 1, \"ion_title\": \"ion_title\",\"ion_description\": \"ion_description\", \"ion_institution_id\": \"ion_institution_id\",\"base_url\": \"http://foo\",\"is_public\":true}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.CREATE_DATA_RESOURCE, ooi_id, "0");
			printStream.println("CREATE_DATA_RESOURCE");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.startsWith("{\"data_source_id\": "));
			
			String newDataSourceId = replyJsonString.substring(20,56);

			// Update data resource
			requestJsonString = "{\"user_id\": \"" + ooi_id + "\",\"data_source_resource_id\":\"" + newDataSourceId + "\",\"isPublic\":true,\"max_ingest_millis\": 1,\"update_start_datetime_millis\": 1,\"update_interval_seconds\": 1,\"ion_title\": \"ion_title\",\"ion_description\": \"ion_description\"}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.UPDATE_DATA_RESOURCE, ooi_id, "0");
			printStream.println("UPDATE_DATA_RESOURCE");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.equals("{\"success\": true}"));

			// Delete data resource
			requestJsonString = "{\"data_source_resource_id\":\"" + newDataSourceId + "\"}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.DELETE_DATA_RESOURCE, ooi_id, "0");
			printStream.println("DELETE_DATA_RESOURCE");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.startsWith("{\"successfully_deleted_id\":"));

			// Get resource types
			requestJsonString = null;
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.GET_RESOURCE_TYPES, ooi_id, "0");
			printStream.println("GET_RESOURCE_TYPES");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.startsWith("{\"resource_types_list\": ["));

			// Get resources of type
			requestJsonString = "{\"resource_type\": \"datasets\"}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.GET_RESOURCES_OF_TYPE, ooi_id, "0");
			printStream.println("GET_RESOURCES_OF_TYPE: datasets");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.startsWith("{\"column_names\": ["));

			requestJsonString = "{\"ooi_id\": \"" + replyJsonString.substring(65, 101) + "\"}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.GET_RESOURCE, ooi_id, "0");
			printStream.println("GET_RESOURCE: dataset");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.startsWith("{\"resource\": ["));

			requestJsonString = "{\"resource_type\": \"identities\"}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.GET_RESOURCES_OF_TYPE, ooi_id, "0");
			printStream.println("GET_RESOURCES_OF_TYPE: identities");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.startsWith("{\"column_names\": ["));

			requestJsonString = "{\"ooi_id\": \"" + ooi_id + "\"}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.GET_RESOURCE, ooi_id, "0");
			printStream.println("GET_RESOURCE: identities");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.startsWith("{\"resource\": ["));

			requestJsonString = "{\"resource_type\": \"datasources\"}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.GET_RESOURCES_OF_TYPE, ooi_id, "0");
			printStream.println("GET_RESOURCES_OF_TYPE: datasources");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.startsWith("{\"column_names\": ["));

			requestJsonString = "{\"ooi_id\": \"" + replyJsonString.substring(70, 106) + "\"}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.GET_RESOURCE, ooi_id, "0");
			printStream.println("GET_RESOURCE: datasources");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.startsWith("{\"resource\": ["));

			requestJsonString = "{\"resource_type\": \"epucontrollers\"}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.GET_RESOURCES_OF_TYPE, ooi_id, "0");
			printStream.println("GET_RESOURCES_OF_TYPE: epucontrollers");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.startsWith("{\"column_names\": ["));

			requestJsonString = "{\"ooi_id\": \"" + ooi_id + "\"}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.GET_RESOURCE, ooi_id, "0");
			assertTrue(replyJsonString.startsWith("{\"resource\": ["));
			assertTrue(ais.getStatus() == 200);
			printStream.println("GET_RESOURCE: epucontrollers");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);

			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
