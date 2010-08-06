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
        
        ResourceDO res1 = new ResourceDO();
        res1.addAttribute("name", "thing");
        
        // Create and send request message
        IonMessage msgout1 = ionAttach.createMessage(fromName, toName, "register_resource", res1);
        ionAttach.sendMessage(msgout1);
        
        // Receive response message
        IonMessage msgin1 = ionAttach.consumeMessage(queue);
        ionAttach.ackMessage(msgin1);
        
        // Create and send message
        IonMessage msgout2 = ionAttach.createMessage(fromName, toName, "get_resource_by_id", res1.getIdentity());
        ionAttach.sendMessage(msgout2);
               
        // Receive response message
        IonMessage msgin2 = ionAttach.consumeMessage(queue);
        if (msgin2.hasDataObject()) {
        	DataObject dobj = msgin2.extractDataObject();
        	ResourceDO res = new ResourceDO(dobj);
        	System.out.println("Message: "+res);
        }
        ionAttach.ackMessage(msgin2);
            
        // Close connection
        ionAttach.detach();
    }
    
}

