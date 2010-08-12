package ion.core.messaging;

import ion.core.IonException;
import ion.core.data.DataObject;
import ion.core.data.DataObjectManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class MsgBrokerClient {
	String mBrokerHost;
	int mBrokerPort;
	String mBaseExchange;

	Connection mBrokerConnection = null;
	Channel mDefaultChannel = null;
	Map mConsumerMap = null;

	public MsgBrokerClient(String hostName, int portNumber, String ionExchange) {
		mBrokerHost = hostName;
		mBrokerPort = portNumber;
		mBaseExchange = ionExchange;
		mConsumerMap = new HashMap();
	}
	
	public void attach() {
		if (mBrokerConnection != null) {
			throw new IonException("Connection already existing");
		}
        try {
			ConnectionFactory cfconn = new ConnectionFactory(); 
			cfconn.setHost(mBrokerHost); 
			cfconn.setPort(mBrokerPort);
			mBrokerConnection = cfconn.newConnection();

			mDefaultChannel = mBrokerConnection.createChannel();

	        System.out.println("Opened channel on host " + mBrokerHost + ", port " + mBrokerPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public String declareQueue(String queueName) {
        try {
			if (queueName == null) {
				queueName = mDefaultChannel.queueDeclare().getQueue();
			} else {
				mDefaultChannel.queueDeclare(queueName, false, false, false, null);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        System.out.println("Declared queue " + queueName);
        return queueName;
	}

	public void bindQueue(String queueName, MessagingName bindingKey, String exchange) {
		if (exchange == null) {
			exchange = mBaseExchange;
		}
		try {
			mDefaultChannel.queueBind(queueName, exchange, bindingKey.getName());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        System.out.println("Bound queue " + queueName + " to exchange " + exchange +
               " with binding key " + bindingKey);
	}
	
	public void attachConsumer(String queueName) {
        QueueingConsumer consumer = new QueueingConsumer(mDefaultChannel);
        try {
			mDefaultChannel.basicConsume(queueName, consumer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mConsumerMap.put(queueName, consumer);
	}
	
	QueueingConsumer getQueueConsumer(String queueName) {
		return (QueueingConsumer) mConsumerMap.get(queueName);
	}

	public IonMessage consumeMessage(String queueName) {
        QueueingConsumer consumer = getQueueConsumer(queueName);
        QueueingConsumer.Delivery delivery = null;
		try {
			delivery = consumer.nextDelivery();
		} catch (ShutdownSignalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        IonMessage msgin = messageFromDelivery(delivery);

        System.out.println("Message received on queue " + queueName + ", msglen " + msgin.getBody().length);
        
        if (msgin.isErrorMessage()) {
            System.out.println("Received message is an ERROR message: " + ((Map) msgin.getContent()).get("value"));
        }

        return msgin;
	}

	public IonMessage messageFromDelivery(QueueingConsumer.Delivery delivery) {
		
        Envelope envelope = delivery.getEnvelope();
        byte[] body = delivery.getBody();
        
        IonAmqpMessage msg = new IonAmqpMessage(envelope, body);
        return msg;
	}
	
	public IonMessage createMessage(MessagingName from, MessagingName to, String op, Object content) {
		if (content instanceof DataObject) {
			content = DataObjectManager.toValueString((DataObject) content);
		}
		IonSendMessage newmsg = new IonSendMessage(from.getName(), to.getName(), op, content);
		return newmsg; 
	}

	public void sendMessage(IonMessage msg) {
		byte[] msgbytes = msg.getBody();
		
		String toName = (String) msg.getIonHeaders().get("receiver");
        BasicProperties props = new BasicProperties("application/msgpack", "binary", null, null,
			    null, null, null, null,
			    null, null, null, null,
			    null, null);
        try {
			mDefaultChannel.basicPublish(mBaseExchange, toName, props, msgbytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        System.out.println("Sent message to exchange " + mBaseExchange + " with routing key " + toName + 
        		", msglen "+msgbytes.length);
	}
	
	public void createSendMessage(MessagingName from, MessagingName to, String op, Object content) {
		IonMessage msg = createMessage(from, to, op, content);
		sendMessage(msg);
	}
	
	public void ackMessage(IonMessage msg) {
		try {
			mDefaultChannel.basicAck(msg.getEnvelope().getDeliveryTag(), false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void detach() {
    	try {
    		mDefaultChannel.close();
    		mBrokerConnection.close();
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	} 
    	mDefaultChannel = null;
    	mBrokerConnection = null;
	}
}
