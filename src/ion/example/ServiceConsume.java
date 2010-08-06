package ion.example;

import ion.core.data.DataObject;
import ion.core.messaging.IonMessage;
import ion.core.messaging.MsgBrokerAttachment;
import ion.resource.ResourceDO;

import com.rabbitmq.client.AMQP;

public class ServiceConsume {
    public static void main(String[] args) {
        String hostName = "localhost";
        int portNumber = AMQP.PROTOCOL.PORT;
        String exchange = "magnet.topic";
        String toName = "mysys.registry";
        String fromName = "mysys.return";
        
        // Messaging environment
        MsgBrokerAttachment ionAttach = new MsgBrokerAttachment(hostName, portNumber, exchange);
        ionAttach.attach();
        
        // Create return queue
        String queue = ionAttach.declareQueue(null);
        ionAttach.bindQueue(queue, fromName, null);                                
        ionAttach.attachConsumer(queue);
        
        // Create and send message
        IonMessage msgout = ionAttach.createMessage(fromName, toName, "get_resource_by_id", "06a5672b-4a8e-4b72-8636-a64e0574fcad");
        ionAttach.sendMessage(msgout);
        
       
        // Receive response message
        IonMessage msgin = ionAttach.consumeMessage(queue);
        if (msgin.hasDataObject()) {
        	DataObject dobj = msgin.extractDataObject();
        	ResourceDO res = new ResourceDO(dobj);
        	System.out.println("Message: "+dobj);
        }
        ionAttach.ackMessage(msgin);
            
        // Close connection
        ionAttach.detach();
    }
    
}

