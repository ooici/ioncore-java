package ion.core.messaging;

import ion.core.data.DataObject;
import ion.core.data.DataObjectManager;

import java.util.Map;

import com.rabbitmq.client.Envelope;

/**
 * The IonMessage serves as the base class for all Ion messages. It contains the core constructs
 * for all messages within ION. Such core message features include the message envelope, the message
 * body, the message content and more. 
 *
 * @author mmeisinger
 * @author Stephen Pasco
 *
 */
public class IonMessage {

	// Message envelope
	Envelope mEnvelope;
	// Message body
	byte[] mBody;
	// Message headers
	Map mHeaders;
	// Message content
	Object mContent;
	// Was the message received
	boolean mReceived;

	/**
	 * Base class constructor
	 *
	 * @param received
	 */
	public IonMessage(boolean received) {
		mReceived = received;
	}

	/**
	 * Returns the message envelope
	 *
	 * @return  Returns the message envelope
	 */
	public Envelope getEnvelope() {
		return mEnvelope;
	}

	/**
	 * Message body
	 *
	 * @return  Returns the message body
	 */
	public byte[] getBody() {
		return mBody;
	}

	/**
	 * Returns the message headers
	 *
	 * @return  Returns the message headers
	 */
	public Map getIonHeaders() {
		return mHeaders;
	}

	/**
	 * Returns the message content
	 *
	 * @return Returns the message content
	 */
	public Object getContent() {
		return mContent;
	}

	/**
	 * Determines if the message property "value" has content
	 *
	 * @return Returns a boolean value of true if the message contains a data value
	 */
	public boolean hasDataObject() {
		if (mContent instanceof Map) {
			Map cont = (Map) mContent;
			Object value = cont.get("value");
			if (value instanceof String) {
				String valstr = (String) value;
				return valstr.startsWith("{");
			}
		}
		return  false;
	}

	/**
	 * Determines if the message content contains an "ERROR" under the "status property
	 * @return Returns a boolean value of true if the message contains an "ERROR" value within
	 * the "status" property
	 *
	 */
	public boolean isErrorMessage() {
        return (mContent instanceof Map && "ERROR".equals(((Map) mContent).get("status")));
	}

	/**
	 * Extracts and returns the DataObject of a message if it exists within the "value" property
	 * of the IonMessage.
	 *
	 * @return Returns the DataObject contained in the "value" property of the IonMessage.
	 */
	public DataObject extractDataObject() {
		
		if (! hasDataObject()) {
			return null;
		}

		String dovalstr = (String) ((Map) mContent).get("value");
		DataObject dataObject = DataObjectManager.fromValueString(dovalstr);
		
	    return dataObject;
	}
}
