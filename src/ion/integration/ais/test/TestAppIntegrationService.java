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
	"MIIEVDCCAzygAwIBAgICCQ4wDQYJKoZIhvcNAQELBQAwazETMBEGCgmSJomT8ixkARkWA29yZzEX\\n" +
	"MBUGCgmSJomT8ixkARkWB2NpbG9nb24xCzAJBgNVBAYTAlVTMRAwDgYDVQQKEwdDSUxvZ29uMRww\\n" +
	"GgYDVQQDExNDSUxvZ29uIE9wZW5JRCBDQSAxMB4XDTExMDYwMzIxMDIxOFoXDTExMDYwNDA5MDcx\\n" +
	"OFowZjETMBEGCgmSJomT8ixkARkTA29yZzEXMBUGCgmSJomT8ixkARkTB2NpbG9nb24xCzAJBgNV\\n" +
	"BAYTAlVTMQ8wDQYDVQQKEwZHb29nbGUxGDAWBgNVBAMTD09PSS1DSSBPT0kgQTU1MjCCASIwDQYJ\\n" +
	"KoZIhvcNAQEBBQADggEPADCCAQoCggEBAMIbdvzufLyoedYoWaKW8OISLcC8GfvpvhnUmrM9prEI\\n" +
	"NHYwSfXuVlqVGHXtRUfPJj0Its+TQf7myOH5gsApqwX2MqP5QcJyO2aNWRNkTmK3XPC7gWI0Hcd5\\n" +
	"qgwzzK3Sn6UKRjmoEcjL2vm9NaNIg8TMkj04lAG3Re59+v5uLq+cltced2QKKpxdU8EWtGMQozAu\\n" +
	"AYaJM1avcX51ea122z49LrNCJ+2dFcpklYF61C6/A9guKkXGhk0KM+n8JU1pyKlpvmI/p8wVbgs5\\n" +
	"GnmCaUdsyUnblXAKP3pioC1LJMRzm15YP6GLGyo8lRQviIR9efKLXoVS3PisC7eoCcyUOTcCAwEA\\n" +
	"AaOCAQUwggEBMAwGA1UdEwEB/wQCMAAwDgYDVR0PAQH/BAQDAgSwMBMGA1UdJQQMMAoGCCsGAQUF\\n" +
	"BwMCMBgGA1UdIAQRMA8wDQYLKwYBBAGCkTYBAwMwbAYDVR0fBGUwYzAvoC2gK4YpaHR0cDovL2Ny\\n" +
	"bC5jaWxvZ29uLm9yZy9jaWxvZ29uLW9wZW5pZC5jcmwwMKAuoCyGKmh0dHA6Ly9jcmwuZG9lZ3Jp\\n" +
	"ZHMub3JnL2NpbG9nb24tb3BlbmlkLmNybDBEBgNVHREEPTA7gRFteW9vaWNpQGdtYWlsLmNvbYYm\\n" +
	"dXJuOnB1YmxpY2lkOklETitjaWxvZ29uLm9yZyt1c2VyK0E1NTIwDQYJKoZIhvcNAQELBQADggEB\\n" +
	"AAW2n6oHSRBK3hoO/7628SLh0WCesmISKzqZRm1K6EuYiLpLsgfLOZWqu27UmuxlrBNDYNs3lgL/\\n" +
	"8VaDVo9sJMowrdWhBawALuEHrIYkX6S1HsgvcRW9n23zb1AyjwbCZlKK8QH4Moh6uByO+pOSZdbV\\n" +
	"Lz2dw6nIoKz702VMiElLXeE1pDJIeCr5W1FJAZpi9SEWIzdjtHGojSpUx7CNupCOOTIH8R1cHbO0\\n" +
	"mBDnP20LUI+JjtN1Va0bAHc2W8UZSsW8g4QvTBJ7XvsOGV+7XhFmxZmEhGaFDtPyCMW6E34EuRJS\\n" +
	"9l8al9sP+u2brS6fQ5qoc5xyZVVcffYPdBFT8gY=\\n" +
	"-----END CERTIFICATE-----";
	private String privateKey = "-----BEGIN RSA PRIVATE KEY-----\\n" +
	"MIIEowIBAAKCAQEAwht2/O58vKh51ihZopbw4hItwLwZ++m+GdSasz2msQg0djBJ9e5WWpUYde1F\\n" +
	"R88mPQi2z5NB/ubI4fmCwCmrBfYyo/lBwnI7Zo1ZE2ROYrdc8LuBYjQdx3mqDDPMrdKfpQpGOagR\\n" +
	"yMva+b01o0iDxMySPTiUAbdF7n36/m4ur5yW1x53ZAoqnF1TwRa0YxCjMC4BhokzVq9xfnV5rXbb\\n" +
	"Pj0us0In7Z0VymSVgXrULr8D2C4qRcaGTQoz6fwlTWnIqWm+Yj+nzBVuCzkaeYJpR2zJSduVcAo/\\n" +
	"emKgLUskxHObXlg/oYsbKjyVFC+IhH158otehVLc+KwLt6gJzJQ5NwIDAQABAoIBAQCoDild4YmD\\n" +
	"uYYK4dKBT5fs03pjZThF/+DD8muiBh2dJpJtRW+zio+fS3jrGOujuXjM3Q+R9lfsPpnr9B+9ChZ1\\n" +
	"SewcRcEmfcpqBrT5ch3foAvKrTze7mpd+zs751ktoa7wsE2Ou7HyHHVRRfz7itvy9n8inCqgtbHJ\\n" +
	"Q6+cu36WMUXhDlfa9hq73DN2nmKZjqaRg0rIIfyLa4fvMFWz5AtHR8FOwk79YvzOAE70MXuca0en\\n" +
	"NmqXD/OaZ4MNXTMdPt0f2hlYOO+/rPv8DZpfi+joB9NQ+ZZqcb7nQ56yOcJx+yPjQ+8yRmheil4g\\n" +
	"BVRr83Z41ZsCpuHpnP6FdwZNqobpAoGBAONEBtYpyktY6bbc1Z9pAy00kiGyoNFCzvtvhL3JvPZo\\n" +
	"nC+N0aRMnlWXxxPtvyqyJIJvqK1KbWcd4yD200xhLpC5r/y3HpXJ19V+mAQ0cEsuZamw9K1fB+EY\\n" +
	"aSlp9Foz/5cZCiX31F8yi0js+IuP1xzAv7oqup9CFry/6wp7jBm7AoGBANqmMH+0OpFVIiSjBjre\\n" +
	"/E+sSxcqrajv4JvDns96fJjGOJ/LBY0eUUhwPY4wim0rfNcu3Hmotp/X8w0+OX0svXhu0MP0WCCJ\\n" +
	"y/S8wNIQuXN25mqVRmU+hLFii4t7SgdxM8r1/oQKH0lAqE123zAYR417cdBurCBQA5aKKyEyTZi1\\n" +
	"AoGAWesucUnzmkBBqHJTq1DXSumD8AVHD8TJND55XMYXF79oHICWM9WEyATXZZEpk/EL9PfM21OZ\\n" +
	"WbU/imleTNgennB5qxmg5k8IMJZ3+yHsVDK1UqCLDpWM/oi0AwjC/3WXaOclVsRpqIjNBzuLU1zE\\n" +
	"FcJFmZkSYbS6Xk/o5Srg0cUCgYBVRLZpNwoYH1E/ZGxLjSZsk8587GHpHhND65gFZcktczAl8PDr\\n" +
	"RcWBMHRw/TEevfTjnhzRPSBrWbYplfipfkctrlmv8ZxkpBhsCyhPQ8Ju6xGUwz4+wZDR9JJjBOOr\\n" +
	"31PJdQGa0K++y35XJ2KGyREuddO+60opF8subBfBzHJCeQKBgA0SkCgGHFILi80EG4FHZCdb3+CR\\n" +
	"w/0z56l5aPSP52xpWjzPyywv+4ku+LXEyWF3qj4xJww8SVBP5nmTsYEJwu26g97ZWprehJzOOhWu\\n" +
	"11HQQLNLNPYu68sggMAjjdguSl7W2cEJskqTWs8Gsjug0HQw/I3I9MTJKa71rsYBNdhL\\n" +
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

		// Grab stuff for use in later calls
		JSONObject createDataResourceMsg = (JSONObject)JSONValue.parse(replyJsonString);
		String dataSetID = (String)createDataResourceMsg.get("data_set_id");

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
		requestJsonString = "{\"user_id\": \"" + ooi_id + "\",\"data_set_resource_id\":\"" + dataSetID + "\",\"isPublic\":true,\"max_ingest_millis\": 1,\"update_start_datetime_millis\": 1,\"update_interval_seconds\": 1,\"ion_title\": \"ion_title\",\"ion_description\": \"ion_description\"}";
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

		// Delete data resource
		requestJsonString = "{\"data_set_resource_id\":\"" + dataSetID + "\"}";
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
