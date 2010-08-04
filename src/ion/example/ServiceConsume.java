package ion.example;

import ion.core.IonMessaging;

import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;

public class ServiceConsume {
    public static void main(String[] args) {
        try {
            String hostName = "amoeba.ucsd.edu";
            int portNumber = AMQP.PROTOCOL.PORT;
            String exchange = "magnet.topic";
            String toName = "mmeisinger.javaint";
            String fromName = "mmeisinger.return";

            Channel channel = IonMessaging.openBrokerChannel(hostName, portNumber, exchange);
            IonMessaging.declareBindQueue(channel, fromName, exchange);
            
            Map message = IonMessaging.createMessage(fromName, toName, "list_all_instruments", "");
            byte[] msgbytes = IonMessaging.encodeMessage(message);
                        
            System.out.println("Sending to exchange " + exchange + ", topic " + toName);
            
    		BasicProperties props = new BasicProperties("application/msgpack", "binary", null, null,
    							    null, null, null, null,
    							    null, null, null, null,
    							    null, null);
            channel.basicPublish(exchange, toName, props, msgbytes);
            
            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(fromName, consumer);
            while (true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                Envelope envelope = delivery.getEnvelope();
                System.out.println(envelope.getRoutingKey() + ": " + new String(delivery.getBody()));
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
            
//            channel.close();
//            conn.close(); 
        } catch (Exception ex) {
            System.err.println("Main thread caught exception: " + ex);
            ex.printStackTrace();
            System.exit(1);
        }
    }
}

