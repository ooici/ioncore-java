package ion.integration.ais.test;

import java.util.HashMap;
import java.util.Map;

import net.ooici.integration.ais.findDataResources.FindDataResources.FindDataResourcesMsg;
import net.ooici.integration.ais.findDataResources.FindDataResources.Spatial;
import net.ooici.integration.ais.findDataResources.FindDataResources.Temporal;

import ion.core.BaseProcess;
import ion.core.messaging.IonMessage;
import ion.core.messaging.MessagingName;
import ion.core.messaging.MsgBrokerClient;
import ion.integration.ais.AppIntegrationService;
import ion.integration.ais.AppIntegrationService.RequestType;

import com.google.protobuf.JsonFormat;
import com.rabbitmq.client.AMQP;

import junit.framework.TestCase;

public class TestAppIntegrationService extends TestCase {

	// Alter these values as necessary for your system configuration
	private String sysName = "Tom";
    private String hostName = "localhost";
    private int portNumber = AMQP.PROTOCOL.PORT;
    private String exchange = "magnet.topic";
    
    private AppIntegrationService ais;

	private String certificate = "-----BEGIN CERTIFICATE-----\\n" +
		"MIIEMzCCAxugAwIBAgICBQAwDQYJKoZIhvcNAQEFBQAwajETMBEGCgmSJomT8ixkARkWA29yZzEX\\n" +
		"MBUGCgmSJomT8ixkARkWB2NpbG9nb24xCzAJBgNVBAYTAlVTMRAwDgYDVQQKEwdDSUxvZ29uMRsw\\n" +
		"GQYDVQQDExJDSUxvZ29uIEJhc2ljIENBIDEwHhcNMTAxMTE4MjIyNTA2WhcNMTAxMTE5MTAzMDA2\\n" +
		"WjBvMRMwEQYKCZImiZPyLGQBGRMDb3JnMRcwFQYKCZImiZPyLGQBGRMHY2lsb2dvbjELMAkGA1UE\\n" +
		"BhMCVVMxFzAVBgNVBAoTDlByb3RlY3ROZXR3b3JrMRkwFwYDVQQDExBSb2dlciBVbndpbiBBMjU0\\n" +
		"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6QhsWxhUXbIxg+1ZyEc7d+hIGvchVmtb\\n" +
		"g0kKLmivgoVsA4U7swNDRH6svW242THta0oTf6crkRx7kOKg6jma2lcAC1sjOSddqX7/92ChoUPq\\n" +
		"7LWt2T6GVVA10ex5WAeB/o7br/Z4U8/75uCBis+ru7xEDl09PToK20mrkcz9M4HqIv1eSoPkrs3b\\n" +
		"2lUtQc6cjuHRDU4NknXaVMXTBHKPM40UxEDHJueFyCiZJFg3lvQuSsAl4JL5Z8pC02T8/bODBuf4\\n" +
		"dszsqn2SC8YDw1xrujvW2Bd7Q7BwMQ/gO+dZKM1mLJFpfEsR9WrjMeg6vkD2TMWLMr0/WIkGC8u+\\n" +
		"6M6SMQIDAQABo4HdMIHaMAwGA1UdEwEB/wQCMAAwDgYDVR0PAQH/BAQDAgSwMBMGA1UdJQQMMAoG\\n" +
		"CCsGAQUFBwMCMBgGA1UdIAQRMA8wDQYLKwYBBAGCkTYBAgEwagYDVR0fBGMwYTAuoCygKoYoaHR0\\n" +
		"cDovL2NybC5jaWxvZ29uLm9yZy9jaWxvZ29uLWJhc2ljLmNybDAvoC2gK4YpaHR0cDovL2NybC5k\\n" +
		"b2Vncmlkcy5vcmcvY2lsb2dvbi1iYXNpYy5jcmwwHwYDVR0RBBgwFoEUaXRzYWdyZWVuMUB5YWhv\\n" +
		"by5jb20wDQYJKoZIhvcNAQEFBQADggEBAEYHQPMY9Grs19MHxUzMwXp1GzCKhGpgyVKJKW86PJlr\\n" +
		"HGruoWvx+DLNX75Oj5FC4t8bOUQVQusZGeGSEGegzzfIeOI/jWP1UtIjzvTFDq3tQMNvsgROSCx5\\n" +
		"CkpK4nS0kbwLux+zI7BWON97UpMIzEeE05pd7SmNAETuWRsHMP+x6i7hoUp/uad4DwbzNUGIotdK\\n" +
		"f8b270icOVgkOKRdLP/Q4r/x8skKSCRz1ZsRdR+7+B/EgksAJj7Ut3yiWoUekEMxCaTdAHPTMD/g\\n" +
		"Mh9xL90hfMJyoGemjJswG5g3fAdTP/Lv0I6/nWeH/cLjwwpQgIEjEAVXl7KHuzX5vPD/wqQ=\\n" +
		"-----END CERTIFICATE-----";
	private String privateKey = "-----BEGIN RSA PRIVATE KEY-----\\n" +
		"MIIEowIBAAKCAQEA6QhsWxhUXbIxg+1ZyEc7d+hIGvchVmtbg0kKLmivgoVsA4U7swNDRH6svW24\\n" +
		"2THta0oTf6crkRx7kOKg6jma2lcAC1sjOSddqX7/92ChoUPq7LWt2T6GVVA10ex5WAeB/o7br/Z4\\n" +
		"U8/75uCBis+ru7xEDl09PToK20mrkcz9M4HqIv1eSoPkrs3b2lUtQc6cjuHRDU4NknXaVMXTBHKP\\n" +
		"M40UxEDHJueFyCiZJFg3lvQuSsAl4JL5Z8pC02T8/bODBuf4dszsqn2SC8YDw1xrujvW2Bd7Q7Bw\\n" +
		"MQ/gO+dZKM1mLJFpfEsR9WrjMeg6vkD2TMWLMr0/WIkGC8u+6M6SMQIDAQABAoIBAAc/Ic97ZDQ9\\n" +
		"tFh76wzVWj4SVRuxj7HWSNQ+Uzi6PKr8Zy182Sxp74+TuN9zKAppCQ8LEKwpkKtEjXsl8QcXn38m\\n" +
		"sXOo8+F1He6FaoRQ1vXi3M1boPpefWLtyZ6rkeJw6VP3MVG5gmho0VaOqLieWKLP6fXgZGUhBvFm\\n" +
		"yxUPoNgXJPLjJ9pNGy4IBuQDudqfJeqnbIe0GOXdB1oLCjAgZlTR4lFA92OrkMEldyVp72iYbffN\\n" +
		"4GqoCEiHi8lX9m2kvwiQKRnfH1dLnnPBrrwatu7TxOs02HpJ99wfzKRy4B1SKcB0Gs22761r+N/M\\n" +
		"oO966VxlkKYTN+soN5ID9mQmXJkCgYEA/h2bqH9mNzHhzS21x8mC6n+MTyYYKVlEW4VSJ3TyMKlR\\n" +
		"gAjhxY/LUNeVpfxm2fY8tvQecWaW3mYQLfnvM7f1FeNJwEwIkS/yaeNmcRC6HK/hHeE87+fNVW/U\\n" +
		"ftU4FW5Krg3QIYxcTL2vL3JU4Auu3E/XVcx0iqYMGZMEEDOcQPcCgYEA6sLLIeOdngUvxdA4KKEe\\n" +
		"qInDpa/coWbtAlGJv8NueYTuD3BYJG5KoWFY4TVfjQsBgdxNxHzxb5l9PrFLm9mRn3iiR/2EpQke\\n" +
		"qJzs87K0A/sxTVES29w1PKinkBkdu8pNk10TxtRUl/Ox3fuuZPvyt9hi5c5O/MCKJbjmyJHuJBcC\\n" +
		"gYBiAJM2oaOPJ9q4oadYnLuzqms3Xy60S6wUS8+KTgzVfYdkBIjmA3XbALnDIRudddymhnFzNKh8\\n" +
		"rwoQYTLCVHDd9yFLW0d2jvJDqiKo+lV8mMwOFP7GWzSSfaWLILoXcci1ZbheJ9607faxKrvXCEpw\\n" +
		"xw36FfbgPfeuqUdI5E6fswKBgFIxCu99gnSNulEWemL3LgWx3fbHYIZ9w6MZKxIheS9AdByhp6px\\n" +
		"lt1zeKu4hRCbdtaha/TMDbeV1Hy7lA4nmU1s7dwojWU+kSZVcrxLp6zxKCy6otCpA1aOccQIlxll\\n" +
		"Vc2vO7pUIp3kqzRd5ovijfMB5nYwygTB4FwepWY5eVfXAoGBAIqrLKhRzdpGL0Vp2jwtJJiMShKm\\n" +
		"WJ1c7fBskgAVk8jJzbEgMxuVeurioYqj0Cn7hFQoLc+npdU5byRti+4xjZBXSmmjo4Y7ttXGvBrf\\n" +
		"c2bPOQRAYZyD2o+/MHBDsz7RWZJoZiI+SJJuE4wphGUsEbI2Ger1QW9135jKp6BsY2qZ\\n" +
		"-----END RSA PRIVATE KEY-----";

	public TestAppIntegrationService(String name) {
		super(name);
	}
	
	protected void setUp() {
		ais = new AppIntegrationService(sysName, hostName, portNumber, exchange);
	}
	
	protected void tearDown() {
		ais.dispose();
		ais = null;
	}

	public void testCreateDownloadURL() {
		// TODO
//		String requestJsonString = "{\"certificate\": \"" + certificate + "\",";
//		requestJsonString += "\"rsa_private_key\": \"" + privateKey + "\"}";
//        String replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.REGISTER_USER, "ANONYMOUS", "0");
//        assertTrue(replyJsonString.startsWith("{\"ooi_id\":"));
    }

	public void testFindDataResources() {
		// TODO
//		FindDataResourcesMsg.Builder find = FindDataResourcesMsg.newBuilder();
//		Spatial.Builder spatial = Spatial.newBuilder();
//		Temporal.Builder temporal = Temporal.newBuilder();
//		
//		spatial.setMinLatitude(1.1);
//		spatial.setMaxLatitude(2.2);
//		spatial.setMinLongitude(3.3);
//		spatial.setMaxLongitude(4.4);
//		spatial.setMinAltitude(5.5);
//		spatial.setMaxAltitude(6.6);
//		
//		temporal.setMinTime(7.7);
//		temporal.setMaxTime(8.8);
//		
//		find.setSpatial(spatial);
//		find.setTemporal(temporal);
//		
//		String requestJsonString = JsonFormat.printToString(find.build());
//		
//        String replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.REGISTER_USER, "ANONYMOUS", "0");
//        assertTrue(replyJsonString.startsWith("{\"ooi_id\":"));
    }

	public void testGetDataResourceDetail() {
		// TODO
//		String requestJsonString = "{\"certificate\": \"" + certificate + "\",";
//		requestJsonString += "\"rsa_private_key\": \"" + privateKey + "\"}";
//        String replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.REGISTER_USER, "ANONYMOUS", "0");
//        assertTrue(replyJsonString.startsWith("{\"ooi_id\":"));
    }

	public void testRegisterUser() {
		String requestJsonString = "{\"certificate\": \"" + certificate + "\",";
		requestJsonString += "\"rsa_private_key\": \"" + privateKey + "\"}";
        String replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.REGISTER_USER, "ANONYMOUS", "0");
        assertTrue(replyJsonString.startsWith("{\"ooi_id\":"));
    }

	public void testUpdateUserEmail() {
		// TODO
//		String requestJsonString = "{\"certificate\": \"" + certificate + "\",";
//		requestJsonString += "\"rsa_private_key\": \"" + privateKey + "\"}";
//        String replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.REGISTER_USER, "ANONYMOUS", "0");
//        assertTrue(replyJsonString.startsWith("{\"ooi_id\":"));
    }

	public void testUpdateUserDispatcherQueue() {
		// TODO
//		String requestJsonString = "{\"certificate\": \"" + certificate + "\",";
//		requestJsonString += "\"rsa_private_key\": \"" + privateKey + "\"}";
//        String replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.REGISTER_USER, "ANONYMOUS", "0");
//        assertTrue(replyJsonString.startsWith("{\"ooi_id\":"));
    }

}
