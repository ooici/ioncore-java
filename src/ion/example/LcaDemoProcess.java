package ion.example;

import ion.core.BaseProcess;
import ion.core.data.DataObject;
import ion.core.data.DataObjectManager;
import ion.core.messaging.IonMessage;
import ion.core.messaging.MessagingName;
import ion.core.messaging.MsgBrokerClient;
import ion.resource.DataProductRDO;
import ion.resource.InstrumentRDO;
import ion.resource.ListAllQueryDO;
import ion.resource.ResourceDO;
import ion.resource.ServiceRDO;

import java.util.Iterator;
import java.util.List;

import com.rabbitmq.client.AMQP;

public class LcaDemoProcess extends BaseProcess {

	public LcaDemoProcess(MsgBrokerClient brokeratt) {
		super(brokeratt);
	}
	
	String SYSNAME = System.getProperty("ioncore.sysname","mysys");
    MessagingName instMgmtRegSvc = new MessagingName(SYSNAME, "instrument_management");
    MessagingName javaintvc = new MessagingName(SYSNAME, "javaint");

    public void callSequenceInstrumentRegistry() {
        MessagingName instRegSvc = new MessagingName(SYSNAME, "instrument_registry");

        System.out.println("\nSTEP: Register a new resource");
        
        InstrumentRDO res1 = new InstrumentRDO();
        res1.create_identity();
        res1.addAttribute("serial_num", "1234");
        res1.addAttribute("model", "SBE49");
        
        // Create and send request message
        IonMessage msgin1 = this.rpcSend(instRegSvc, "register_instrument_instance", res1);
        this.ackMessage(msgin1);
        
        // Create and send message
    	System.out.println("\nSTEP: Get previously registered resource attributes");
    	IonMessage msgin2 = this.rpcSend(instRegSvc, "get_instrument_by_id", res1.getIdentity());
        if (msgin2.hasDataObject()) {
        	DataObject dobj = msgin2.extractDataObject();
        	System.out.println("Message DO: "+dobj);
        }
        this.ackMessage(msgin2);

        // Create and send message
    	System.out.println("\nSTEP: Find all resources of a type");
        ListAllQueryDO listall = new ListAllQueryDO(new InstrumentRDO());
        IonMessage msgin3 = this.rpcSend(instRegSvc, "find_instrument_instance", listall);
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

    public void callSequenceDataProductRegistry() {
        MessagingName dataprodRegSvc = new MessagingName(SYSNAME, "data_product_registry");

        System.out.println("\nSTEP: Register a new resource");
        
        DataProductRDO res1 = new DataProductRDO();
        res1.create_identity();
//       res1.addAttribute("serial_num", "abc1236215-33");
        
        // Create and send request message
        IonMessage msgin1 = this.rpcSend(dataprodRegSvc, "register_data_product", res1);
        this.ackMessage(msgin1);
        

        // Create and send message
    	System.out.println("\nSTEP: Find all resources of a type");
        ListAllQueryDO listall = new ListAllQueryDO(new DataProductRDO());
        IonMessage msgin3 = this.rpcSend(dataprodRegSvc, "find_data_product", listall);
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

    public void callSequenceServiceRegistry() {
        MessagingName serviceRegSvc = new MessagingName(SYSNAME, "service_registry");

        // Create and send message
    	System.out.println("\nSTEP: Find all resources of a type");
        ListAllQueryDO listall = new ListAllQueryDO(new ServiceRDO());
        IonMessage msgin3 = this.rpcSend(serviceRegSvc, "find_registered_service_instance_from_description", listall);
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
        DataObjectManager.registerDOType(DataProductRDO.class);
        DataObjectManager.registerDOType(ServiceRDO.class);
        
        // Messaging environment
        MsgBrokerClient ionClient = new MsgBrokerClient(hostName, portNumber, exchange);
        ionClient.attach();

        LcaDemoProcess proc = new LcaDemoProcess(ionClient);
        proc.spawn();
        
        proc.callSequenceInstrumentRegistry(); 
        proc.callSequenceDataProductRegistry();
        proc.callSequenceServiceRegistry();

        // Close connection
        ionClient.detach();
    }
}
