package ion.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.msgpack.Packer;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;

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
		msg.put("content", content);
		return msg;
	}

	public static Channel openBrokerChannel(String hostName, int portNumber, String exchange) {
        try {
			ConnectionFactory cfconn = new ConnectionFactory(); 
			cfconn.setHost(hostName); 
			cfconn.setPort(portNumber);
			Connection conn = cfconn.newConnection();

			final Channel channel = conn.createChannel();

			if (exchange == null) {
			    exchange = "amq.topic";
			} else {
			    channel.exchangeDeclare(exchange, "topic");
			}
			
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
