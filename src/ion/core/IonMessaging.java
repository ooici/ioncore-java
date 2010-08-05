package ion.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.msgpack.Packer;
import org.msgpack.UnpackException;
import org.msgpack.UnpackResult;
import org.msgpack.Unpacker;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class IonMessaging {
	
	public static byte[] encodeMessage(Map msg) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Packer pack = new Packer(out);
		try {
			pack.pack(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] res = out.toByteArray();
		
		return res;
	}
	
	@SuppressWarnings("unchecked")
	public static Map createMessage(String from, String to, String op, Object content) {
		Map msg = new HashMap();
		msg.put("sender", from);
		msg.put("receiver", to);
		msg.put("reply-to", from);
		msg.put("op", op);
		msg.put("conv-id", "#1");
		msg.put("conv-seq", 1);
		msg.put("accept-encoding", "application/ion-jsond");
		msg.put("encoding", "application/ion-jsond");
		msg.put("content", content);
		return msg;
	}
	
	public static Object decodeMessage(byte[] msgbytes) {
		ByteArrayInputStream bin = new ByteArrayInputStream(msgbytes);
		Unpacker unpack = new Unpacker(bin);
		UnpackResult result = null;
		try {
			result = unpack.next();
		} catch (UnpackException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Object msgData = decodeValue(result.getData());
		
        System.out.println("Message obj " + msgData);

		return msgData;
	}
	
	public static Object decodeValue(Object obj) {
		if (obj instanceof byte[]) {
			return new String((byte[]) obj);
		}
		else if (obj instanceof List) {
			List newl = new ArrayList();
			for (Object v : ((List) obj)) {
				newl.add(decodeValue(v));
			}
			return newl;
		}
		else if (obj instanceof Map) {
			Map newm = new HashMap();
			for (Object me : ((Map) obj).entrySet()) {
				String key = (String) decodeValue(((Map.Entry) me).getKey());
				Object val = decodeValue(((Map.Entry) me).getValue());
				newm.put(key,val);
			}
			return newm;

		}
		return obj;
	}

	public static Map decodeDataObject(String dostr) {
		  JSONParser parser=new JSONParser();

		  System.out.println("=======decode JSON=======");
		                
		  Object obj = null;
		  try {
			  obj = parser.parse(dostr);
		  } catch (ParseException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }
		  System.out.println("decode result: "+obj);
		  return (Map) obj;
	}
	
	public static Channel openBrokerChannel(String hostName, int portNumber, String exchange) {
        try {
			ConnectionFactory cfconn = new ConnectionFactory(); 
			cfconn.setHost(hostName); 
			cfconn.setPort(portNumber);
			Connection conn = cfconn.newConnection();

			final Channel channel = conn.createChannel();

//			if (exchange == null) {
//			    exchange = "amq.topic";
//			} else {
//			    channel.exchangeDeclare(exchange, "topic");
//			}
			
	        System.out.println("Opened channel on host " + hostName + ", port " + portNumber +
	                " with exchange " + exchange);

	        return channel;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}
	
	public static void declareBindQueue(Channel channel, String topicPattern, String exchange) {
		String queue = topicPattern;
        try {
			if (queue == null) {
			    queue = channel.queueDeclare().getQueue();
			} else {
			  channel.queueDeclare(queue, false, false, false, null);
			}

			channel.queueBind(queue, exchange, topicPattern);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        System.out.println("Listening to exchange " + exchange + ", pattern " + topicPattern +
               " from queue " + queue);
	}
}
