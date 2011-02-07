package ion.core.messaging;

import java.util.UUID;

public class MessagingName {
	private String mName;
//    private static String localHostName;
//    static {
//        try {
//            localHostName = java.net.InetAddress.getLocalHost().getHostName();
//            System.out.println("localHostName: " + localHostName);
//        } catch (UnknownHostException ex) {
//            Logger.getLogger(MessagingName.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
	
	public MessagingName(String qualifier, String name) {
		this(qualifier + "." + name);
	}

	public MessagingName(String name) {
//		mName = localHostName + "." + name;
        mName = name;
	}

	public String getName() {
		return mName;
	}

	public String toString() {
		return mName;
	}

	public static MessagingName generateUniqueName() {
		UUID id = UUID.randomUUID();
		return new MessagingName(id.toString() + ".1");
	}
}
