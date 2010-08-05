package ion.example;

import ion.core.IonMessaging;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;

public class ServiceConsume {
    public static void main(String[] args) {
        String hostName = "localhost";
        int portNumber = AMQP.PROTOCOL.PORT;
        String exchange = "magnet.topic";
        String toName = "mysys.registry";
        String fromName = "mmeisinger.return";

        Channel channel = null;
        try {
            channel = IonMessaging.openBrokerChannel(hostName, portNumber, exchange);
            IonMessaging.declareBindQueue(channel, fromName, exchange);
            
            Map message = IonMessaging.createMessage(fromName, toName, "get_resource_by_id", "4d60a4ad-0c20-4192-a4ef-cb237bf8d41e");
            byte[] msgbytes = IonMessaging.encodeMessage(message);
                        
            System.out.println("Sending to exchange " + exchange + ", topic " + toName);
            
            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(fromName, consumer);

            BasicProperties props = new BasicProperties("application/msgpack", "binary", null, null,
    							    null, null, null, null,
    							    null, null, null, null,
    							    null, null);
            channel.basicPublish(exchange, toName, props, msgbytes);
            
            //while (true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                Envelope envelope = delivery.getEnvelope();
                Object obj = IonMessaging.decodeMessage(delivery.getBody());
                Map msg = (Map) obj;
                Map msgcont = (Map) msg.get("content");
                String dovalue = (String) msgcont.get("value");
                
                Map domap = IonMessaging.decodeDataObject(dovalue);
                printResAttributes((Map) domap.get("fields"));
                
//                System.out.println(envelope.getRoutingKey() + ": " + new String(delivery.getBody()));
                channel.basicAck(envelope.getDeliveryTag(), false);
            //}
            
        } catch (Exception ex) {
            System.err.println("Main thread caught exception: " + ex);
            ex.printStackTrace();
            System.exit(1);
        } finally {
        	try {
        		channel.close();
        		channel.getConnection().close();
        	} catch (IOException e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();
        	} 
        }
    }
    
    static void printResAttributes(Map resourceDO) {
    	for (Object me : resourceDO.entrySet()) {
    		String key = (String) ((Map.Entry) me).getKey();
    		Object value = ((Map.Entry) me).getValue();
    		Object value1 = ((Map) value).get("value");
    		System.out.println("Attribute "+key+"="+value1);
    	}
    }
}

