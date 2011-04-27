package ion.integration.ais.test;

import ion.integration.ais.AppIntegrationService;
import ion.integration.ais.AppIntegrationService.RequestType;
import junit.framework.TestCase;

import com.rabbitmq.client.AMQP;

public class TestAppIntegrationService extends TestCase {

    // Alter these values as necessary for your system configuration
    private String sysName = "sysName-UNIQUE";
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

	// Sample input string: "{\"user_ooi_id\": \"3f27a744-2c3e-4d2a-a98c-050b246334a3\",\"minLatitude\": 32.87521,\"maxLatitude\": 32.97521,\"minLongitude\": -117.274609,\"maxLongitude\": -117.174609,\"minDepth\": 5.5,\"maxDepth\": 6.6,\"minTime\": 7.7,\"maxTime\": 8.8,\"identity\": \"ad0adf8b-4a7e-43e1-8cc0-371fb9057664\"}"
	// Sample output string: "{\"data_resource_id": [\"ad0adf8b-4a7e-43e1-8cc0-371fb9057664\"]}"
	public void testFindDataResources() {
		String requestJsonString = "{\"user_ooi_id\": \"3f27a744-2c3e-4d2a-a98c-050b246334a3\",\"minLatitude\": 32.87521,\"maxLatitude\": 32.97521,\"minLongitude\": -117.274609,\"maxLongitude\": -117.174609,\"minDepth\": 5.5,\"maxDepth\": 6.6,\"minTime\": 7.7,\"maxTime\": 8.8,\"identity\": \"ad0adf8b-4a7e-43e1-8cc0-371fb9057664\"}";
        String replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.FIND_DATA_RESOURCES, "3f27a744-2c3e-4d2a-a98c-050b246334a3", "0");
        assertTrue(replyJsonString.startsWith("{\"data_resource_id\":"));
    }

	public void testGetDataResourceDetail() {
		// TODO
//		String requestJsonString = "{\"certificate\": \"" + certificate + "\",";
//		requestJsonString += "\"rsa_private_key\": \"" + privateKey + "\"}";
//        String replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.REGISTER_USER, "ANONYMOUS", "0");
//        assertTrue(replyJsonString.startsWith("{\"ooi_id\":"));
    }

	public void testCreateDataResource() {
//		DataResourceCreateUpdateRequest.Builder bldr = DataResourceCreateUpdateRequest.newBuilder();
//		bldr.setDataResourceId("123");
//		bldr.setProvider("provider");
//		bldr.setFormat("format");
//		bldr.setProtocol("protocol");
//		bldr.setType("type");
//		bldr.setTitle("Title");
//		bldr.setDataFormat("dataformat");
//		bldr.setDataType("datatype");
//		bldr.setNamingAuthority("namingauthority");
//		bldr.setSummary("summary");
//		bldr.setCreatorUserId("creatoruserid");
//		bldr.setPublisherId("publisherid");
		
		
		
//		String requestJsonString = "{\"user_ooi_id\": \"3f27a744-2c3e-4d2a-a98c-050b246334a3\",\"minLatitude\": 1.1,\"maxLatitude\": 2.2,\"minLongitude\": 3.3,\"maxLongitude\": 4.4,\"minDepth\": 5.5,\"maxDepth\": 6.6,\"minTime\": 7.7,\"maxTime\": 8.8}";
//        String replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.CREATE_DATA_RESOURCE, "ANONYMOUS", "0");
//        assertTrue(replyJsonString.startsWith("{\"data_resource_id\":"));
    }

	public void testUpdateDataResource() {
//		String requestJsonString = "{\"user_ooi_id\": \"3f27a744-2c3e-4d2a-a98c-050b246334a3\",\"minLatitude\": 1.1,\"maxLatitude\": 2.2,\"minLongitude\": 3.3,\"maxLongitude\": 4.4,\"minDepth\": 5.5,\"maxDepth\": 6.6,\"minTime\": 7.7,\"maxTime\": 8.8}";
//        String replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.UPDATE_DATA_RESOURCE, "ANONYMOUS", "0");
//        assertTrue(replyJsonString.startsWith("{\"data_resource_id\":"));
    }

	public void testDeleteDataResource() {
//		String requestJsonString = "{\"user_ooi_id\": \"3f27a744-2c3e-4d2a-a98c-050b246334a3\",\"minLatitude\": 1.1,\"maxLatitude\": 2.2,\"minLongitude\": 3.3,\"maxLongitude\": 4.4,\"minDepth\": 5.5,\"maxDepth\": 6.6,\"minTime\": 7.7,\"maxTime\": 8.8}";
//        String replyJsonString = ais.sendReceiveUIRequest(requestJsonString, RequestType.DELETE_DATA_RESOURCE, "ANONYMOUS", "0");
//        assertTrue(replyJsonString.startsWith("{\"success\":"));
    }

	// Sample input string: "{\"certificate\": \"-----BEGIN CERTIFICATE-----\nMIIEMzCCAxugAwIBAgICBQAwDQYJKoZIhvcNAQEFBQAwajETMBEGCgmSJomT8ixkARkWA29yZzEX\nMBUGCgmSJomT8ixkARkWB2NpbG9nb24xCzAJBgNVBAYTAlVTMRAwDgYDVQQKEwdDSUxvZ29uMRsw\nGQYDVQQDExJDSUxvZ29uIEJhc2ljIENBIDEwHhcNMTAxMTE4MjIyNTA2WhcNMTAxMTE5MTAzMDA2\nWjBvMRMwEQYKCZImiZPyLGQBGRMDb3JnMRcwFQYKCZImiZPyLGQBGRMHY2lsb2dvbjELMAkGA1UE\nBhMCVVMxFzAVBgNVBAoTDlByb3RlY3ROZXR3b3JrMRkwFwYDVQQDExBSb2dlciBVbndpbiBBMjU0\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6QhsWxhUXbIxg+1ZyEc7d+hIGvchVmtb\ng0kKLmivgoVsA4U7swNDRH6svW242THta0oTf6crkRx7kOKg6jma2lcAC1sjOSddqX7/92ChoUPq\n7LWt2T6GVVA10ex5WAeB/o7br/Z4U8/75uCBis+ru7xEDl09PToK20mrkcz9M4HqIv1eSoPkrs3b\n2lUtQc6cjuHRDU4NknXaVMXTBHKPM40UxEDHJueFyCiZJFg3lvQuSsAl4JL5Z8pC02T8/bODBuf4\ndszsqn2SC8YDw1xrujvW2Bd7Q7BwMQ/gO+dZKM1mLJFpfEsR9WrjMeg6vkD2TMWLMr0/WIkGC8u+\n6M6SMQIDAQABo4HdMIHaMAwGA1UdEwEB/wQCMAAwDgYDVR0PAQH/BAQDAgSwMBMGA1UdJQQMMAoG\nCCsGAQUFBwMCMBgGA1UdIAQRMA8wDQYLKwYBBAGCkTYBAgEwagYDVR0fBGMwYTAuoCygKoYoaHR0\ncDovL2NybC5jaWxvZ29uLm9yZy9jaWxvZ29uLWJhc2ljLmNybDAvoC2gK4YpaHR0cDovL2NybC5k\nb2Vncmlkcy5vcmcvY2lsb2dvbi1iYXNpYy5jcmwwHwYDVR0RBBgwFoEUaXRzYWdyZWVuMUB5YWhv\nby5jb20wDQYJKoZIhvcNAQEFBQADggEBAEYHQPMY9Grs19MHxUzMwXp1GzCKhGpgyVKJKW86PJlr\nHGruoWvx+DLNX75Oj5FC4t8bOUQVQusZGeGSEGegzzfIeOI/jWP1UtIjzvTFDq3tQMNvsgROSCx5\nCkpK4nS0kbwLux+zI7BWON97UpMIzEeE05pd7SmNAETuWRsHMP+x6i7hoUp/uad4DwbzNUGIotdK\nf8b270icOVgkOKRdLP/Q4r/x8skKSCRz1ZsRdR+7+B/EgksAJj7Ut3yiWoUekEMxCaTdAHPTMD/g\nMh9xL90hfMJyoGemjJswG5g3fAdTP/Lv0I6/nWeH/cLjwwpQgIEjEAVXl7KHuzX5vPD/wqQ=\n-----END CERTIFICATE-----\",\"rsa_private_key\": \"-----BEGIN RSA PRIVATE KEY-----\nMIIEowIBAAKCAQEA6QhsWxhUXbIxg+1ZyEc7d+hIGvchVmtbg0kKLmivgoVsA4U7swNDRH6svW24\n2THta0oTf6crkRx7kOKg6jma2lcAC1sjOSddqX7/92ChoUPq7LWt2T6GVVA10ex5WAeB/o7br/Z4\nU8/75uCBis+ru7xEDl09PToK20mrkcz9M4HqIv1eSoPkrs3b2lUtQc6cjuHRDU4NknXaVMXTBHKP\nM40UxEDHJueFyCiZJFg3lvQuSsAl4JL5Z8pC02T8/bODBuf4dszsqn2SC8YDw1xrujvW2Bd7Q7Bw\nMQ/gO+dZKM1mLJFpfEsR9WrjMeg6vkD2TMWLMr0/WIkGC8u+6M6SMQIDAQABAoIBAAc/Ic97ZDQ9\ntFh76wzVWj4SVRuxj7HWSNQ+Uzi6PKr8Zy182Sxp74+TuN9zKAppCQ8LEKwpkKtEjXsl8QcXn38m\nsXOo8+F1He6FaoRQ1vXi3M1boPpefWLtyZ6rkeJw6VP3MVG5gmho0VaOqLieWKLP6fXgZGUhBvFm\nyxUPoNgXJPLjJ9pNGy4IBuQDudqfJeqnbIe0GOXdB1oLCjAgZlTR4lFA92OrkMEldyVp72iYbffN\n4GqoCEiHi8lX9m2kvwiQKRnfH1dLnnPBrrwatu7TxOs02HpJ99wfzKRy4B1SKcB0Gs22761r+N/M\noO966VxlkKYTN+soN5ID9mQmXJkCgYEA/h2bqH9mNzHhzS21x8mC6n+MTyYYKVlEW4VSJ3TyMKlR\ngAjhxY/LUNeVpfxm2fY8tvQecWaW3mYQLfnvM7f1FeNJwEwIkS/yaeNmcRC6HK/hHeE87+fNVW/U\nftU4FW5Krg3QIYxcTL2vL3JU4Auu3E/XVcx0iqYMGZMEEDOcQPcCgYEA6sLLIeOdngUvxdA4KKEe\nqInDpa/coWbtAlGJv8NueYTuD3BYJG5KoWFY4TVfjQsBgdxNxHzxb5l9PrFLm9mRn3iiR/2EpQke\nqJzs87K0A/sxTVES29w1PKinkBkdu8pNk10TxtRUl/Ox3fuuZPvyt9hi5c5O/MCKJbjmyJHuJBcC\ngYBiAJM2oaOPJ9q4oadYnLuzqms3Xy60S6wUS8+KTgzVfYdkBIjmA3XbALnDIRudddymhnFzNKh8\nrwoQYTLCVHDd9yFLW0d2jvJDqiKo+lV8mMwOFP7GWzSSfaWLILoXcci1ZbheJ9607faxKrvXCEpw\nxw36FfbgPfeuqUdI5E6fswKBgFIxCu99gnSNulEWemL3LgWx3fbHYIZ9w6MZKxIheS9AdByhp6px\nlt1zeKu4hRCbdtaha/TMDbeV1Hy7lA4nmU1s7dwojWU+kSZVcrxLp6zxKCy6otCpA1aOccQIlxll\nVc2vO7pUIp3kqzRd5ovijfMB5nYwygTB4FwepWY5eVfXAoGBAIqrLKhRzdpGL0Vp2jwtJJiMShKm\nWJ1c7fBskgAVk8jJzbEgMxuVeurioYqj0Cn7hFQoLc+npdU5byRti+4xjZBXSmmjo4Y7ttXGvBrf\nc2bPOQRAYZyD2o+/MHBDsz7RWZJoZiI+SJJuE4wphGUsEbI2Ger1QW9135jKp6BsY2qZ\n-----END RSA PRIVATE KEY-----\"}"
	// Sample output string: "{\"ooi_id\": \"3f27a744-2c3e-4d2a-a98c-050b246334a3\"}"
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
