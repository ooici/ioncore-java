package ion.core.utils;

import com.rabbitmq.client.AMQP;

public class IonConstants {

	// Message receive timeout
	public static final int DEFAULT_TIMEOUT_MS = 300000;
    
    // AMQP Heartbeat seconds
    public static final int DEFAULT_AMQP_HEARTBEAT = 30;

	// Properties file constants pertaining to configuring
	// the MsgBrokerClient
	public static String HOSTNAME_KEY = "ion.host";
	public static String PORTNUMBER_KEY = "ion.port";
	public static String USERNAME_KEY = "ion.username";
	public static String PASSWORD_KEY = "ion.password";
	public static String EXCHANGE_KEY = "ion.exchange";

	public static String OOICI_CONN_FILENAME = "ooici-conn.properties";

	public static String HOSTNAME_DEFAULT = "rabbitmq.oceanobservatories.org";
	public static int PORTNUMBER_DEFAULT = AMQP.PROTOCOL.PORT;
	public static String USERNAME_DEFAULT = null;
	public static String PASSWORD_DEFAULT = null;
	public static String EXCHANGE_DEFAULT = "magnet.topic";

	// Properties file and default constants pertaining to sysname
	public static String SYSNAME_KEY = "ion.sysname";
	public static String SYSNAME_DEFAULT = "sysName-UNIQUE";
    
    // Properties pertaining to data retrieval
    public static String DATASTORE_TOPIC_KEY = "ion.datastore_topic";
    public static String DATASTORE_TOPIC_DEFAULT = "datastore";
    
    // Properties pertaining to dataset_registration
    public static String DATASET_REGISTRATION_TOPIC_KEY = "ion.dataset_registration_topic";
    public static String DATASET_REGISTRATION_TOPIC_DEFAULT = "app_integration";
    
    // Properties pertaining to events
    public static String EVENT_EXCHANGE_KEY = "ion.event_exchange";
    public static String EVENT_EXCHANGE_DEFAULT = "events.topic";
    
    // Properties pertaining to update events
    public static String UPDATE_EVENT_TOPIC_KEY = "ion.update_event_topic";
    public static String UPDATE_EVENT_TOPIC_DEFAULT = "2001.1001";
    
	
}
