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

/**
 * MsgBrokerClient serves as the messaging client to a RabbitMQ broker.
 *
 * @author Michael Meisinger
 * @author Stephen Pasco
 */
public class MsgBrokerClient {

	private String mBrokerHost;
	private int mBrokerPort;
	private String mBaseExchange;

	private Connection mBrokerConnection = null;
	private Channel mDefaultChannel = null;
	private Map mConsumerMap = null;

	/**
	 * Base class constructor
	 *
	 * Establishes the broker host name, port and exchange.
	 *
	 * @param hostName    The RabbitMQ broker host name
	 * @param portNumber  The RabbitMQ broker port number
	 * @param ionExchange The RabbitMQ exchange name
	 */
	public MsgBrokerClient(String hostName, int portNumber, String ionExchange) {
		mBrokerHost = hostName;
		mBrokerPort = portNumber;
		mBaseExchange = ionExchange;
		mConsumerMap = new HashMap();
	}

	/**
	 * Establish a connection to a RabbitMQ broker and attach a channel to that connection.
	 */
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
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Declares a queue on the channel.
	 *
	 * @param queueName
	 * @return String queue name
	 */
	public String declareQueue(String queueName) {

		try {
			if (queueName == null) {
				queueName = mDefaultChannel.queueDeclare().getQueue();
			}
			else {
				mDefaultChannel.queueDeclare(queueName, false, false, false, null);
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Declared queue " + queueName);
		
		return queueName;
	}

	/**
	 *  Binds to a queue using the queue name, binding key and exchange to a channel.
	 *
	 * @param queueName     The queue name
	 * @param bindingKey    The binding key
	 * @param exchange      The exchange name
	 */
	public void bindQueue(String queueName, MessagingName bindingKey, String exchange) {

		if (exchange == null) {
			exchange = mBaseExchange;
		}
		try {
			mDefaultChannel.queueBind(queueName, exchange, bindingKey.getName());
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Bound queue " + queueName + " to exchange " + exchange +
				" with binding key " + bindingKey);
	}

	/**
	 * Establishes a Queuing consumer on the default channel to allow for the consumption of
	 * messages on a queue.
	 *
	 * @param queueName The queue name
	 */
	public void attachConsumer(String queueName) {

		QueueingConsumer consumer = new QueueingConsumer(mDefaultChannel);

		try {
			mDefaultChannel.basicConsume(queueName, consumer);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mConsumerMap.put(queueName, consumer);
	}

	/**
	 * Returns a Queuing Consumer
	 *
	 * @param queueName The queue name
	 * @return QueueingConsumer Returns the QueueingConsumer
	 */
	QueueingConsumer getQueueConsumer(String queueName) {
		return (QueueingConsumer) mConsumerMap.get(queueName);
	}

	/**
	 *  Allows messages (an IonMessage) to be consumed from a queue.
	 *
	 * @param queueName     The queue name
	 * @return IonMessage   Returns an IonMessage
	 */
	public IonMessage consumeMessage(String queueName) {

		QueueingConsumer consumer = getQueueConsumer(queueName);
		QueueingConsumer.Delivery delivery = null;

		try {
			delivery = consumer.nextDelivery();
		}
		catch (ShutdownSignalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InterruptedException e) {
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

	/**
	 * Allows a message to be consumed from a QueuingConsumer.Delivery
	 *
	 * @param delivery      The delivery
	 * @return IonMessage   Returns an IonMessage
	 */
	public IonMessage messageFromDelivery(QueueingConsumer.Delivery delivery) {

		Envelope envelope = delivery.getEnvelope();
		byte[] body = delivery.getBody();

		IonAmqpMessage msg = new IonAmqpMessage(envelope, body);
		return msg;
	}

	/**
	 * Creates an IonMessage
	 *
	 * @param from      Where this message is sourced from.
	 * @param to        Where this message is going.
	 * @param op        The operation to be performed by the target.
	 * @param content   The content of this message
	 * @return Returns the IonMessage
	 */
	public IonMessage createMessage(MessagingName from, MessagingName to, String op, Object content) {

		if (content instanceof DataObject) {
			content = DataObjectManager.toValueString((DataObject) content);
		}

		IonSendMessage newmsg = new IonSendMessage(from.getName(), to.getName(), op, content);
		
		return newmsg;
	}

	/**
	 * Sends a IonMessage to the "receiver" specified in the header of the message.
	 *
	 * @param msg The IonMessage being sent
	 */
	public void sendMessage(IonMessage msg) {

		byte[] msgbytes = msg.getBody();

		String toName = (String) msg.getIonHeaders().get("receiver");
		BasicProperties props = new BasicProperties("application/msgpack", "binary", null, null,
				null, null, null, null,
				null, null, null, null,
				null, null);
		try {
			mDefaultChannel.basicPublish(mBaseExchange, toName, props, msgbytes);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Sent message to exchange " + mBaseExchange + " with routing key " + toName +
				", msglen " + msgbytes.length);
	}

	/**
	 * Creates and sends an IonMessage
	 *
	 * @param from      Message source
	 * @param to        Message destination
	 * @param op        Message operation
	 * @param content   Message content
	 */
	public void createSendMessage(MessagingName from, MessagingName to, String op, Object content) {
		IonMessage msg = createMessage(from, to, op, content);
		sendMessage(msg);
	}

	/**
	 * Acknowledges receipt of a delivered message
	 *
	 * @param msg IonMessage to acknowledge receipt of
	 */
	public void ackMessage(IonMessage msg) {
		try {
			mDefaultChannel.basicAck(msg.getEnvelope().getDeliveryTag(), false);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Detaches the channel and connection from a RabbitMQ broker.
	 */
	public void detach() {
		try {
			mDefaultChannel.close();
			mBrokerConnection.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mDefaultChannel = null;
		mBrokerConnection = null;
	}
}
