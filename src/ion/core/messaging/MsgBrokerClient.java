package ion.core.messaging;

import ion.core.IonException;
import ion.core.data.DataObject;
import ion.core.data.DataObjectManager;
import ion.core.utils.IonConstants;
import ion.core.utils.IonUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author mmeisinger
 * @author Stephen Pasco
 * @author Chris Mueller
 */
public class MsgBrokerClient {
    
    private static final Logger log = LoggerFactory.getLogger(MsgBrokerClient.class);

    private String mBrokerHost;
    private int mBrokerPort;
    private String mUsername;
    private String mPassword;
    private String mBaseExchange;
    private Connection mBrokerConnection = null;
    protected Channel mDefaultChannel = null;
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
    public MsgBrokerClient() {
    	HashMap<String, String> propertyMap = null;
    	try {
    		propertyMap = IonUtils.parseProperties();
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}

    	String hostName = System.getProperty(IonConstants.HOSTNAME_KEY, IonConstants.HOSTNAME_DEFAULT);
    	int portNumber = IonConstants.PORTNUMBER_DEFAULT;
    	String portNumberStr = System.getProperty(IonConstants.PORTNUMBER_KEY);
    	if (portNumberStr != null) {
    		portNumber = Integer.valueOf(portNumberStr);
    	}
    	String username = System.getProperty(IonConstants.USERNAME_KEY, IonConstants.USERNAME_DEFAULT);
    	String password = System.getProperty(IonConstants.PASSWORD_KEY, IonConstants.PASSWORD_DEFAULT);
    	String ionExchange = System.getProperty(IonConstants.EXCHANGE_KEY, IonConstants.EXCHANGE_DEFAULT);

    	mBrokerHost = hostName;
    	mBrokerPort = portNumber;
    	mUsername = username;
    	mPassword = password;
    	mBaseExchange = ionExchange;
    	mConsumerMap = new HashMap();
    }

    public MsgBrokerClient(String hostName, int portNumber, String ionExchange) {
    	HashMap<String, String> propertyMap = null;
    	try {
    		propertyMap = IonUtils.parseProperties();
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}

    	String username = System.getProperty(IonConstants.USERNAME_KEY, IonConstants.USERNAME_DEFAULT);
    	String password = System.getProperty(IonConstants.PASSWORD_KEY, IonConstants.PASSWORD_DEFAULT);

    	mBrokerHost = hostName;
    	mBrokerPort = portNumber;
    	mUsername = username;
    	mPassword = password;
    	mBaseExchange = ionExchange;
    	mConsumerMap = new HashMap();
    }

    public MsgBrokerClient(String hostName, int portNumber, String username, String password, String ionExchange) {
    	mBrokerHost = hostName;
    	mBrokerPort = portNumber;
    	mUsername = username;
    	mPassword = password;
    	mBaseExchange = ionExchange;
    	mConsumerMap = new HashMap();
    }

    /**
     * Establish a connection to a RabbitMQ broker and attaches a channel to that connection.
     */
    public void attach() {

        if (mBrokerConnection != null) {
            throw new IonException("Connection already existing");
        }
        try {
            ConnectionFactory cfconn = new ConnectionFactory();
            cfconn.setHost(mBrokerHost);
            cfconn.setPort(mBrokerPort);
            if (mUsername != null) {
            	cfconn.setUsername(mUsername);
            }
            if (mPassword != null) {
            	cfconn.setPassword(mPassword);
            }
            mBrokerConnection = cfconn.newConnection();

            mDefaultChannel = mBrokerConnection.createChannel();
            mDefaultChannel.exchangeDeclare(mBaseExchange, "topic", false, true, null);

            if (log.isDebugEnabled()) {
                log.debug("Opened channel on host " + mBrokerHost + ", port " + mBrokerPort);
            }
        } catch (IOException e) {
            // TODO This exception should be handled by the caller
            log.error("some error thrown in attach() call", e);
            throw new IonException("Error attaching to broker", e);
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
            } else {
                mDefaultChannel.queueDeclare(queueName, false, false, false, null);
            }
        } catch (IOException e) {
            // TODO This exception should be handled by the caller
            log.error("Error calling queueDeclare", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Declared queue " + queueName);
        }

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
            if (log.isDebugEnabled()) {
                log.debug("Bound queue " + queueName + " to exchange " + exchange
                    + " with binding key " + bindingKey);
            }
        } catch (IOException e) {
            // TODO This exception should be handled by the caller
            log.error("Error calling queueBind", e);
        }

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
        } catch (IOException e) {
            // TODO This exception should be handled by the caller
            log.error("Error calling basicConsume", e);
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

    public IonMessage consumeMessage(String queueName) {
        return consumeMessage(queueName, IonConstants.DEFAULT_TIMEOUT_MS);
    }

    /**
     *  Allows messages (an IonMessage) to be consumed from a queue.
     *
     * @param queueName     The queue name
     * @return IonMessage   Returns an IonMessage
     */
    public IonMessage consumeMessage(String queueName, long timeout) {

        QueueingConsumer consumer = getQueueConsumer(queueName);
        QueueingConsumer.Delivery delivery = null;

        IonMessage msgin = null;
        try {
            if(timeout <= 0) {
                delivery = consumer.nextDelivery();
            } else {
                delivery = consumer.nextDelivery(timeout);
            }
            if(delivery == null) {
                if (log.isDebugEnabled()) {
                    log.debug(consumer.getConsumerTag());
                }
                return null;
            }
            msgin = messageFromDelivery(delivery);
            if (log.isDebugEnabled()) {
                log.debug("Message received on queue " + queueName + ", msglen " + msgin.getBody().length);
            }

            /* Commented this out because the content is NOT a map, so this log.debug call throws a ClassCastException */
//            if (msgin.isErrorMessage()) {
//                if (log.isDebugEnabled()) {
//                    log.debug("Received message is an ERROR message: " + ((Map) msgin.getContent()).get("value"));
//                }
//            }
        } catch (ShutdownSignalException e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
            consumer.handleShutdownSignal(consumer.getConsumerTag(), e);

            /* Attempt at replying with an error of some sort */
//            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
//            e.printStackTrace(new java.io.PrintStream(baos));
//            msgin = new IonAmqpMessage(null, baos.toByteArray());
        } catch (InterruptedException e) {
            log.error("interrupted", e);

            /* Attempt at replying with an error of some sort */
//            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
//            e.printStackTrace(new java.io.PrintStream(baos));
//            msgin = new IonAmqpMessage(null, baos.toByteArray());
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

    	assert(msg.getIonHeaders().get("user-id") != null);
    	assert(msg.getIonHeaders().get("expiry") != null);

    	String toName = (String) msg.getIonHeaders().get("receiver");
    	BasicProperties props = new BasicProperties("application/msgpack", "binary", null, null,
    			null, null, null, null,
    			null, null, null, null,
    			null, null);
    	try {
    		mDefaultChannel.basicPublish(mBaseExchange, toName, props, msgbytes);
    		if (log.isDebugEnabled()) {
    		    log.debug("Sent message to exchange " + mBaseExchange + " with routing key " + toName
    		            + ", msglen " + msgbytes.length);
    		}
    	} catch (IOException e) {
            // TODO This exception should be handled by the caller
            log.error("Error calling basicPublish", e);
    	}

    }


    /**
     * Sends a IonMessage to the "receiver" specified in the header of the message.
     * 
     * For the mandatory and immediate flags, see 
     * <a href="http://www.rabbitmq.com/releases/rabbitmq-java-client/v2.4.1/rabbitmq-java-client-javadoc-2.4.1/com/rabbitmq/client/Channel.html#basicPublish%28java.lang.String,%20java.lang.String,%20boolean,%20boolean,%20com.rabbitmq.client.AMQP.BasicProperties,%20byte[]%29"
     * >here</a>
     * 
     * <p>NOTE: This method was added in the context of the SIAM-CI integration 
     * prototype where the "mandatory"/"immediate" flags are
     * being used for enabling functionality related with detecting
     * undelivered/unroutable messages. All of this may change as ioncore-java
     * is further developed as a whole.
     *
     * @param msg 
     *                    The IonMessage being sent
     * @param mandatory true 
     *                    if requesting a mandatory publish
     * @param immediate true 
     *                    if requesting an immediate publish
     *                    
     * @throws IOException if the message cannot be published
     */
    public void sendMessage(IonMessage msg, boolean mandatory, boolean immediate) 
    throws IOException {
        
        // NOTE: Adapted from sendMessage(IonMessage msg) with the following changes:
        //  - of course, use the new parameters for the corresponding basicPublish call
        //  - expose the possible IOException (instead of hiding it within the method)
        //  - use a logger (instead of writing to stdout)

        byte[] msgbytes = msg.getBody();

        assert(msg.getIonHeaders().get("user-id") != null);
        assert(msg.getIonHeaders().get("expiry") != null);

        String toName = (String) msg.getIonHeaders().get("receiver");
        BasicProperties props = new BasicProperties("application/msgpack", "binary", null, null,
                null, null, null, null,
                null, null, null, null,
                null, null);
        
        mDefaultChannel.basicPublish(mBaseExchange, toName, mandatory, immediate, props, msgbytes);

        if ( log.isDebugEnabled() ) {
            log.debug("Sent message to exchange " + mBaseExchange + " with routing key " + toName
                + ", msglen " + msgbytes.length + ", mandatory=" +mandatory+ " immediate=" +immediate);
        }
    }
    
    /**
     * Acknowledges receipt of a delivered message
     *
     * @param msg IonMessage to acknowledge receipt of
     */
    public void ackMessage(IonMessage msg) {
        try {
            mDefaultChannel.basicAck(msg.getEnvelope().getDeliveryTag(), false);
        } catch (IOException e) {
            // TODO This exception should probably be handled by the caller
            log.error("Error calling basicAck", e);
        }
    }

    /**
     * Detaches the channel and connection from a RabbitMQ broker.
     */
    public void detach() {
        try {
            mDefaultChannel.close();
            mBrokerConnection.close();
        } catch (IOException e) {
            // TODO This exception should probably be handled by the caller
            log.error("Error closing channel or connection", e);
        }
        mDefaultChannel = null;
        mBrokerConnection = null;
    }
    
    public String toString() {
    	return "Broker host: <" + mBrokerHost + "> port: <" + mBrokerPort + "> username: <" + mUsername + "> exchange: <" + mBaseExchange + ">";
    }
}
