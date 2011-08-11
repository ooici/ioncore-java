package ion.integration.ais.test;

import ion.core.utils.IonConstants;
import ion.core.utils.IonUtils;
import ion.integration.ais.AppIntegrationService;
import ion.integration.ais.AppIntegrationService.RequestType;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import junit.framework.TestCase;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class TestAppIntegrationService extends TestCase {

	private static AppIntegrationService ais;

	private static FileOutputStream out;
	private static PrintStream printStream;

	private static boolean runValidateDataResourceTests = false;
	private static boolean runInstrumentTests = false;
	private static String ooi_id = null;


	private String certificate = "-----BEGIN CERTIFICATE-----\\n" +
	"MIIEVDCCAzygAwIBAgICBZ0wDQYJKoZIhvcNAQELBQAwazETMBEGCgmSJomT8ixkARkWA29yZzEX\\n" +
	"MBUGCgmSJomT8ixkARkWB2NpbG9nb24xCzAJBgNVBAYTAlVTMRAwDgYDVQQKEwdDSUxvZ29uMRww\\n" +
	"GgYDVQQDExNDSUxvZ29uIE9wZW5JRCBDQSAxMB4XDTExMDgxMDIzMzQxOFoXDTExMDgxMTExMzkx\\n" +
	"OFowZjETMBEGCgmSJomT8ixkARkTA29yZzEXMBUGCgmSJomT8ixkARkTB2NpbG9nb24xCzAJBgNV\\n" +
	"BAYTAlVTMQ8wDQYDVQQKEwZHb29nbGUxGDAWBgNVBAMTD09PSS1DSSBPT0kgQTUwMTCCASIwDQYJ\\n" +
	"KoZIhvcNAQEBBQADggEPADCCAQoCggEBAIDLZVG6oyn3sHlb9Xg/s0+09guSQRiIngNJh8Fxd02G\\n" +
	"DKye0et/sfjO358Evq8NSeRx9lgbWFeBYtRqg4enxz913FySUXh7WxYjgm72No9aeMtY3DhwihrI\\n" +
	"hvvpIrnZH5upAr+v8N/NgrSXmSZfEsO/VhW8WzjtnbCPhrgeP+3s8u6k/jZrJly03T76Lh7OfY+D\\n" +
	"oiio1aEJ7zp077JN3FRcKXH/9WbM5dnT0sWj8gtsRfA0oUpTLr9Pi7ukwN/3bb1aGby6m5FzJzZZ\\n" +
	"TD/Bql7BZs1dnIoKK3C0rXWQ/1w7XUxKMfUEAcAdLemMc+fJlmLlkj7ceu3qIiqacxD8gskCAwEA\\n" +
	"AaOCAQUwggEBMAwGA1UdEwEB/wQCMAAwDgYDVR0PAQH/BAQDAgSwMBMGA1UdJQQMMAoGCCsGAQUF\\n" +
	"BwMCMBgGA1UdIAQRMA8wDQYLKwYBBAGCkTYBAwMwbAYDVR0fBGUwYzAvoC2gK4YpaHR0cDovL2Ny\\n" +
	"bC5jaWxvZ29uLm9yZy9jaWxvZ29uLW9wZW5pZC5jcmwwMKAuoCyGKmh0dHA6Ly9jcmwuZG9lZ3Jp\\n" +
	"ZHMub3JnL2NpbG9nb24tb3BlbmlkLmNybDBEBgNVHREEPTA7gRFteW9vaWNpQGdtYWlsLmNvbYYm\\n" +
	"dXJuOnB1YmxpY2lkOklETitjaWxvZ29uLm9yZyt1c2VyK0E1MDEwDQYJKoZIhvcNAQELBQADggEB\\n" +
	"AJVbyOSd5TI/ZJFTWjKzyzoRWCsVsa0Kc+8mZb48fqIh+aWIeQb232uzcb6E3d6c7EHy3sYx/mGX\\n" +
	"p1yNw0tYl4bMmj3DCZNvOpl3fiI29a351nmMOmbCgWETSTOUr+Peoc90fwa77MJ+u4P/8m95vJpf\\n" +
	"IkUze92bJ78k8ztmVCw69R7DTooNMLc2GH3zcdp3ul+4We/uIV0VvQPQnqKAibUb2spHjU1/u6Kw\\n" +
	"aIJVedgVu050DzA/gyv019p1tzJAHsaz4fwpd5iSelmOHU2ZCIIRPz9uRHQLQfVq1C4lzVdhogby\\n" +
	"wyHT0uL94u2u3IELKAcY8Zz78hdHv2AWwpwenMk=\\n" +
	"-----END CERTIFICATE-----";
	private String privateKey = "-----BEGIN RSA PRIVATE KEY-----\\n" +
	"MIIEowIBAAKCAQEAgMtlUbqjKfeweVv1eD+zT7T2C5JBGIieA0mHwXF3TYYMrJ7R63+x+M7fnwS+\\n" +
	"rw1J5HH2WBtYV4Fi1GqDh6fHP3XcXJJReHtbFiOCbvY2j1p4y1jcOHCKGsiG++kiudkfm6kCv6/w\\n" +
	"382CtJeZJl8Sw79WFbxbOO2dsI+GuB4/7ezy7qT+NmsmXLTdPvouHs59j4OiKKjVoQnvOnTvsk3c\\n" +
	"VFwpcf/1Zszl2dPSxaPyC2xF8DShSlMuv0+Lu6TA3/dtvVoZvLqbkXMnNllMP8GqXsFmzV2cigor\\n" +
	"cLStdZD/XDtdTEox9QQBwB0t6Yxz58mWYuWSPtx67eoiKppzEPyCyQIDAQABAoIBABdlW01iavtX\\n" +
	"rC4Pf2LNp4QGKl/lvH95acLNG6UPOI3TmP/OhfGSq8C3y7V2RjFEZ7Tg4tAUf5K9xTcy9huxZado\\n" +
	"gJQsXDJXri8yWiJQBY867xB5Xt+9ycidvq+KJS2/fFdpdz9c9ZOiIGkv1Lk8sgru+fNO2P9ZYrjN\\n" +
	"Cbrue8x7aZERyxuewU7opU6BihX2Ckw+YLGQwTYmqp6nNDKXV3dZY9tKE4a9uuhOelPggbi1Zy98\\n" +
	"/HrN6qMqH6ICQq3Zm82NZQlb8PL0u6v18Ojf+4BUDKyD9QHWtuB7IsUnDU3Wr1Y0Rpi0hx5gXM4/\\n" +
	"2QwzOqVS3n6BSCJCOimwqkKOh4UCgYEAu2i4+Q1VGazIW98/KKEEL87gPDxXIhq1IcbzfzMAqsNp\\n" +
	"bxGdypMmD7fnHN8FEFqTw8UND9DOsmh90fSPmprrXoLlf4Ymr3DNKhrZVKzrOM/LB0L6Yry3NXWN\\n" +
	"2cZE5bj+0bxLqqQ2rFxoEfWjilO0lLUOW7XFr/PLTNCd9xmK4LMCgYEAr+7JgMwQi9LTO+oF+F0J\\n" +
	"oExwoSqre8SbtGoGvke0Bb+ir/RD1Ghoa01PgX0xM1XRhaeMjLoVj4/EDcSMKC5gS+Lug9VRUbZt\\n" +
	"drz6f0lcvy39rzL7yEoB/Ap2QXRLmILUs14zR/duOiAA/cqm49uCzp4hypRcmepvw4beL9FSlJMC\\n" +
	"gYEAshb0IAehZQKia1ucozlPxzaqM9OLYadLlUuAPNH0wlFsMdXlwolO1AUIpJDyOPY6EQGCRhNB\\n" +
	"OJy/Y/MpO9wX6vosqKCMxo9FB8v31tVzucsMvlvRoF6BI1YQdHBLLJo93IU4ynG+WtB9PQPWYy7k\\n" +
	"HaRofpIfx/K+sMJWOmiVZq0CgYBqoKav0P4WQGiV33hO1tSGus1oYJweH0LfTYNYv8xzz3miesDB\\n" +
	"c6YVon2VVXMEUfbysmGUyRNYNyHz1jO8Bp+GXruAW0E17QLa/B42FxiHJjCihpvjADfDsfOKKBnJ\\n" +
	"DUIsk+Mwst2zjMIND02mu9vDrkN8q/6TqmqibpMrGAqc0QKBgAt4sl2UD6ZthrElIe6+R7zGvJtq\\n" +
	"dygQsBAF2wb2sluIRu6zdm9EDp6XzlzouN3PhCr6Vh/CO/zf43+dKC6yrRDAFiEttoOgeMTBIj2B\\n" +
	"xN/XW/CKTL9f05RpuHULaoNHcOqi56m+Cs6jUaOuVwPxn5T0YoN/Tp24hbRfxFvMG34V\\n" +
	"-----END RSA PRIVATE KEY-----";

	public TestAppIntegrationService(String name) throws FileNotFoundException {
		super(name);

		try {
			IonUtils.parseProperties();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String sysName = System.getProperty(IonConstants.SYSNAME_KEY, IonConstants.SYSNAME_DEFAULT);

		ais = new AppIntegrationService(sysName);

		out = new FileOutputStream("JSONFormatExamples.txt");
		printStream = new PrintStream(out);
	}

	protected void finalize() throws Throwable {
		try {
			ais.dispose();
			ais = null;

			out.close();
			out = null;
		}
		finally {
			super.finalize();
		}
	}

	public void testBadRequestDataNotProvided() {
		// No request data provided
		String replyJsonString = ais.sendReceiveUIRequest(null, RequestType.UPDATE_USER_PROFILE, "3f27a744-2c3e-4d2a-a98c-050b246334a3", "0");
		assertNull(replyJsonString);
		assertTrue(ais.getStatus() == 400);
		assertTrue(ais.getErrorMessage().equals("Request data not provided"));
	}

	public void testBadRequestMalformedJSON() {
		// Poorly formed JSON data provided
		String requestJsonString = "{\"user_ooi_id\": \"3f27a744-2c3e-4d2a-a98c-050b246334a3\";\"email_address\": \"my.email@gmail.com\"}";
		String replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.UPDATE_USER_PROFILE, "3f27a744-2c3e-4d2a-a98c-050b246334a3", "0");
		assertNull(replyJsonString);
		assertTrue(ais.getStatus() == 400);
		assertTrue(ais.getErrorMessage().equals("Exception encoding JSON to GPB: com.google.protobuf.JsonFormat$ParseException: 1:55: Expected identifier. -;"));
	}

	public void registerUser() {
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
		ooi_id = registerUserResp.get("ooi_id").toString();
	}

	public void testUpdateUserProfile() {
		if (ooi_id == null) {
			registerUser();
		}

		// Update user profile
		String requestJsonString = "{\"user_ooi_id\": \"" + ooi_id + "\",\"name\": \"MyOOICI\",\"institution\": \"OOICI\",\"email_address\": \"myooici@gmail.com\", \"profile\": [{\"name\": \"mobile\",\"value\": \"555-555-5555\"}]}";
		String replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.UPDATE_USER_PROFILE, ooi_id, "0");
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

		// Set user role
//		requestJsonString = "{\"user_ooi_id\": \"" + ooi_id + "\",\"role\": \"ADMIN\"}";
//		replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.SET_USER_ROLE, ooi_id, "0");
//		printStream.println("SET_USER_ROLE");
//		printStream.println("  request: " + requestJsonString);
//		printStream.println("  reply: " + replyJsonString);
//		if (ais.getStatus() != 200) {
//			printStream.println("  error string: " + ais.getErrorMessage());
//		}
//		assertTrue(ais.getStatus() == 200);
//		assertTrue(replyJsonString == null);
	}

	public void testShowDataResourcesAnonymous() {
		// Get data resources
		String requestJsonString = "{\"user_ooi_id\": \"ANONYMOUS\"}";
		String replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.FIND_DATA_RESOURCES, "ANONYMOUS", "0");
		printStream.println("FIND_DATA_RESOURCES");
		printStream.println("  request: " + requestJsonString);
		printStream.println("  reply: " + replyJsonString);
		if (ais.getStatus() != 200) {
			printStream.println("  error string: " + ais.getErrorMessage());
		}
		assertTrue(ais.getStatus() == 200);
		assertTrue(replyJsonString.startsWith("{\"dataResourceSummary\":"));

		// Grab stuff for use in later calls
		// Convert NaN to "NaN" just to get the parse to work
		replyJsonString = replyJsonString.replaceAll("NaN", "\"NaN\"");
		JSONObject dataResourceSummary = (JSONObject)JSONValue.parse(replyJsonString);
		JSONArray array = (JSONArray)dataResourceSummary.get("dataResourceSummary");
		JSONObject datasetMetadata = (JSONObject)array.get(0);
		JSONObject dataResourceMetadataObj = (JSONObject)datasetMetadata.get("datasetMetadata");
		String dataResourceMetadata = dataResourceMetadataObj.toString();
		dataResourceMetadata = dataResourceMetadata.replaceAll("\\\\/", "/");
		String dataResourceId = dataResourceMetadataObj.get("data_resource_id").toString();

		// Get data resource details
		requestJsonString = "{\"data_resource_id\": \"" + dataResourceId + "\"}";
		replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.GET_DATA_RESOURCE_DETAIL, "ANONYMOUS", "0");
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
	}

	public void testShowManageDataResourcesUser() {
		if (ooi_id == null) {
			registerUser();
		}

		String requestJsonString;
		String replyJsonString;
		if (runValidateDataResourceTests) {
			// Validate data resource good case
			requestJsonString = "{\"data_resource_url\": \"http://geoport.whoi.edu/thredds/dodsC/usgs/data0/rsignell/data/oceansites/OS_WHOTS_2010_R_M-1.nc\"}";
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
		}

		// Create data resource 1
		requestJsonString = "{\"user_id\": \"" + ooi_id + "\",\"source_type\": 4,\"request_type\": 4, \"ion_title\": \"ion_title\",\"ion_description\": \"ion_description\", \"ion_institution_id\": \"ion_institution_id\",\"dataset_url\": \"http://geoport.whoi.edu/thredds/dodsC/oceansites/OS_NTAS_2010_R_M-1.nc\",\"is_public\":true}";
		replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.CREATE_DATA_RESOURCE, ooi_id, "0");
		printStream.println("CREATE_DATA_RESOURCE");
		printStream.println("  request: " + requestJsonString);
		printStream.println("  reply: " + replyJsonString);
		if (ais.getStatus() != 200) {
			printStream.println("  error string: " + ais.getErrorMessage());
		}
		assertTrue(ais.getStatus() == 200);
		assertTrue(replyJsonString.startsWith("{\"data_source_id\": "));

		// Grab stuff for use in later calls
		JSONObject createDataResourceMsg1 = (JSONObject)JSONValue.parse(replyJsonString);
		String dataSetID1 = (String)createDataResourceMsg1.get("data_set_id");

		// Create data resource 2
		requestJsonString = "{\"user_id\": \"" + ooi_id + "\",\"source_type\": 4,\"request_type\": 4, \"ion_title\": \"ion_title\",\"ion_description\": \"ion_description\", \"ion_institution_id\": \"ion_institution_id\",\"dataset_url\": \"http://geoport.whoi.edu/thredds/dodsC/oceansites/OS_NTAS_2010_R_M-1.nc\",\"is_public\":true}";
		replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.CREATE_DATA_RESOURCE, ooi_id, "0");
		printStream.println("CREATE_DATA_RESOURCE");
		printStream.println("  request: " + requestJsonString);
		printStream.println("  reply: " + replyJsonString);
		if (ais.getStatus() != 200) {
			printStream.println("  error string: " + ais.getErrorMessage());
		}
		assertTrue(ais.getStatus() == 200);
		assertTrue(replyJsonString.startsWith("{\"data_source_id\": "));

		// Grab stuff for use in later calls
		JSONObject createDataResourceMsg2 = (JSONObject)JSONValue.parse(replyJsonString);
		String dataSetID2 = (String)createDataResourceMsg2.get("data_set_id");

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

		// Grab stuff for use in later calls
		replyJsonString = replyJsonString.replaceAll("NaN", "\"NaN\"");
		JSONObject dataResourceSummary = (JSONObject)JSONValue.parse(replyJsonString);
		JSONArray array = (JSONArray)dataResourceSummary.get("datasetByOwnerMetadata");
		JSONObject datasetMetadata = (JSONObject)array.get(0);
		String dataSetMetadata = datasetMetadata.toString();
		dataSetMetadata = dataSetMetadata.replaceAll("\\\\/", "/");
		String dataResourceId = datasetMetadata.get("data_resource_id").toString();

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

		// Update data resource
		requestJsonString = "{\"user_id\": \"" + ooi_id + "\",\"data_set_resource_id\":\"" + dataSetID1 + "\",\"isPublic\":true,\"max_ingest_millis\": 1,\"update_start_datetime_millis\": 1,\"update_interval_seconds\": 1,\"ion_title\": \"ion_title\",\"ion_description\": \"ion_description\", \"visualization_url\":\"http://foo.org/bar\"}";
		replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.UPDATE_DATA_RESOURCE, ooi_id, "0");
		printStream.println("UPDATE_DATA_RESOURCE");
		printStream.println("  request: " + requestJsonString);
		printStream.println("  reply: " + replyJsonString);
		if (ais.getStatus() != 200) {
			printStream.println("  error string: " + ais.getErrorMessage());
		}
		assertTrue(ais.getStatus() == 200);
		assertTrue(replyJsonString.equals("{\"success\": true}"));

		// Create subscription
		requestJsonString = "{\"subscriptionInfo\": {\"user_ooi_id\": \"" + ooi_id + "\",\"data_src_id\": \"" + dataResourceId + "\",\"subscription_type\": \"EMAIL\", \"email_alerts_filter\": \"UPDATES\", \"dispatcher_alerts_filter\": \"UPDATES\",\"dispatcher_script_path\": \"path\"}, \"datasetMetadata\": " + dataSetMetadata + "}";
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
		requestJsonString = "{\"subscriptionInfo\": {\"user_ooi_id\": \"" + ooi_id + "\",\"data_src_id\": \"" + dataResourceId + "\",\"subscription_type\": \"EMAIL\", \"email_alerts_filter\": \"UPDATES\", \"dispatcher_alerts_filter\": \"UPDATES\",\"dispatcher_script_path\": \"newpath\"}, \"datasetMetadata\": " + dataSetMetadata + "}";
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
		requestJsonString = "{\"subscriptions\": [{\"user_ooi_id\": \"" + ooi_id + "\",\"data_src_id\": \"" + dataResourceId + "\",\"subscription_type\": \"EMAIL\", \"email_alerts_filter\": \"UPDATES\", \"dispatcher_alerts_filter\": \"UPDATES\",\"dispatcher_script_path\": \"newpath\"}]}";
		replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.DELETE_DATA_RESOURCE_SUBSCRIPTION, ooi_id, "0");
		printStream.println("DELETE_DATA_RESOURCE_SUBSCRIPTION");
		printStream.println("  request: " + requestJsonString);
		printStream.println("  reply: " + replyJsonString);
		if (ais.getStatus() != 200) {
			printStream.println("  error string: " + ais.getErrorMessage());
		}
		assertTrue(ais.getStatus() == 200);
		assertTrue(replyJsonString.equals("{\"success\": true}"));

		// Delete data resources
		requestJsonString = "{\"data_set_resource_id\": [\"" + dataSetID1 + "\",\"" + dataSetID2 + "\"]}";
		replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.DELETE_DATA_RESOURCE, ooi_id, "0");
		printStream.println("DELETE_DATA_RESOURCE");
		printStream.println("  request: " + requestJsonString);
		printStream.println("  reply: " + replyJsonString);
		if (ais.getStatus() != 200) {
			printStream.println("  error string: " + ais.getErrorMessage());
		}
		assertTrue(ais.getStatus() == 200);
		assertTrue(replyJsonString.startsWith("{\"successfully_deleted_id\":"));
	}

	public void testAdmin() {
		if (ooi_id == null) {
			registerUser();
		}

		// Get resource types
		String requestJsonString = null;
		String replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.GET_RESOURCE_TYPES, ooi_id, "0");
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

		// Grab stuff for use in later calls
		JSONObject getResourceTypeMsg = (JSONObject)JSONValue.parse(replyJsonString);
		JSONArray resourcesArray = (JSONArray)getResourceTypeMsg.get("resources");
		JSONObject jsonObj = (JSONObject)resourcesArray.get(3);
		JSONArray attributeArray = (JSONArray)jsonObj.get("attribute");
		String id = (String)attributeArray.get(0);

		requestJsonString = "{\"ooi_id\": \"" + id + "\"}";
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

		// Grab stuff for use in later calls
		getResourceTypeMsg = (JSONObject)JSONValue.parse(replyJsonString);
		resourcesArray = (JSONArray)getResourceTypeMsg.get("resources");
		jsonObj = (JSONObject)resourcesArray.get(1);
		attributeArray = (JSONArray)jsonObj.get("attribute");
		id = (String)attributeArray.get(0);

		requestJsonString = "{\"ooi_id\": \"" + id + "\"}";
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
		if (ais.getStatus() != 200) {
			printStream.println("  error string: " + ais.getErrorMessage());
		}
		assertTrue(ais.getStatus() == 200);
		printStream.println("GET_RESOURCE: epucontrollers");
		printStream.println("  request: " + requestJsonString);
		printStream.println("  reply: " + replyJsonString);
	}

	public void testInstrumentManagement() {
		if (runInstrumentTests) {
			// create instrument
			String requestJsonString = "{\"name\": \"SeaBird SBE37\",\"description\": \"SeaBird Sensor\", \"manufacturer\": \"SeaBird Electronics\", \"model\": \"SBE37\", \"serial_num\": \"123ABC\",\"fw_version\": \"1.0\"}";
			String replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.CREATE_INSTRUMENT, ooi_id, "0");
			printStream.println("CREATE_INSTRUMENT");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.startsWith("{\"instrument_resource_id\": "));

			JSONObject createInstrumentResp = (JSONObject)JSONValue.parse(replyJsonString);
			String instrument_resource_id = createInstrumentResp.get("instrument_resource_id").toString();

			// start instrument agent
			requestJsonString = "{\"name\": \"SeaBird Electronics\", \"model\": \"SBE37\", \"instrument_resource_id\": \"" + instrument_resource_id + "\"}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.START_INSTRUMENT_AGENT, ooi_id, "0");
			printStream.println("START_INSTRUMENT_AGENT");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.startsWith("{\"instrument_agent_resource_id\": "));

			// get instrument list
			requestJsonString = "{}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.GET_INSTRUMENT_LIST, ooi_id, "0");
			printStream.println("GET_INSTRUMENT_LIST");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.startsWith("{\"instrument_metadata\": "));

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// start instrument sampling
			requestJsonString = "{\"instrument_resource_id\": \"" + instrument_resource_id + "\"}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.START_INSTRUMENT_SAMPLING, ooi_id, "0");
			printStream.println("START_INSTRUMENT_SAMPLING");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.equals("{\"status\": \"OK\"}"));

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// stop instrument sampling
			requestJsonString = "{\"instrument_resource_id\": \"" + instrument_resource_id + "\"}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.STOP_INSTRUMENT_SAMPLING, ooi_id, "0");
			printStream.println("STOP_INSTRUMENT_SAMPLING");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.equals("{\"status\": \"OK\"}"));

			// set instrument state
			requestJsonString = "{\"instrument_resource_id\": \"" + instrument_resource_id + "\", \"properties\": {\"navg\": 1,\"interval\": 5,\"outputsv\": true,\"outputsal\": true,\"txrealtime\": true,\"storetime\": true}}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.SET_INSTRUMENT_STATE, ooi_id, "0");
			printStream.println("SET_INSTRUMENT_STATE");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.equals("{\"status\": \"OK\"}"));

			// get instrument state
			requestJsonString = "{\"instrument_resource_id\": \"" + instrument_resource_id + "\"}";
			replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.GET_INSTRUMENT_STATE, ooi_id, "0");
			printStream.println("GET_INSTRUMENT_STATE");
			printStream.println("  request: " + requestJsonString);
			printStream.println("  reply: " + replyJsonString);
			if (ais.getStatus() != 200) {
				printStream.println("  error string: " + ais.getErrorMessage());
			}
			assertTrue(ais.getStatus() == 200);
			assertTrue(replyJsonString.startsWith("{\"properties\": "));
		}

	}
}
