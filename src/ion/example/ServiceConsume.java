package ion.example;

import ion.core.BaseProcess;
import ion.core.data.DataObject;
import ion.core.data.DataObjectManager;
import ion.core.messaging.IonMessage;
import ion.core.messaging.MessagingName;
import ion.core.messaging.MsgBrokerClient;
import ion.resource.InstrumentRDO;
import ion.resource.ListAllQueryDO;
import ion.resource.ResourceDO;

import java.util.Iterator;
import java.util.List;

import com.rabbitmq.client.AMQP;

public class ServiceConsume extends BaseProcess {
	
	String SYSNAME = System.getProperty("ioncore.sysname","mysys");

	public ServiceConsume(MsgBrokerClient brokercl) {
		super(brokercl);
	}
    
    public void callSequence() {
        MessagingName ionServiceName = new MessagingName(SYSNAME, "registry");
    	System.out.println("\nSTEP: Register a new resource");
        
        InstrumentRDO res1 = new InstrumentRDO();
        res1.create_identity();
        res1.addAttribute("serial_num", "1234");
        res1.addAttribute("model", "SBE49");
        
        // Create and send request message
        IonMessage msgin1 = this.rpcSend(ionServiceName, "register_resource", res1);
        this.ackMessage(msgin1);
                
        // Create and send message
    	System.out.println("\nSTEP: Get previously registered resource attributes");
    	IonMessage msgin2 = this.rpcSend(ionServiceName, "get_resource_by_id", res1.getIdentity());
        if (msgin2.hasDataObject()) {
        	DataObject dobj = msgin2.extractDataObject();
        	System.out.println("Message DO: "+dobj);
        }
        this.ackMessage(msgin2);

        // Create and send message
    	System.out.println("\nSTEP: Find all resources of a type");
        ListAllQueryDO listall = new ListAllQueryDO(new InstrumentRDO());
        IonMessage msgin3 = this.rpcSend(ionServiceName, "find_resource", listall);
        if (msgin3.hasDataObject()) {
        	DataObject dobj = msgin3.extractDataObject();
        	List reslist = (List) dobj.getAttribute("resources");
        	for (Iterator it = reslist.iterator(); it.hasNext();) {
				ResourceDO resobj = (ResourceDO) it.next();
	        	System.out.println("Resource found: "+resobj);
			}
        }
        this.ackMessage(msgin3);
    }
    
	public static void main(String[] args) {
        String hostName = "localhost";
        int portNumber = AMQP.PROTOCOL.PORT;
        String exchange = "magnet.topic";
        
    	System.out.println("\nSTEP: Process and Message Broker Client Setup");

    	// DataObject handling
        DataObjectManager.registerDOType(InstrumentRDO.class);
        
        // Messaging environment
        MsgBrokerClient ionClient = new MsgBrokerClient(hostName, portNumber, exchange);
        ionClient.attach();

        ServiceConsume scex = new ServiceConsume(ionClient);
        scex.spawn();
        scex.callSequence(); 

        // Close connection
        ionClient.detach();
    }
}

