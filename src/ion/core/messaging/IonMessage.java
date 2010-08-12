package ion.core.messaging;

import ion.core.data.DataObject;
import ion.core.data.DataObjectManager;

import java.util.Map;

import com.rabbitmq.client.Envelope;

public class IonMessage {
	
	Envelope mEnvelope;
	byte[] mBody;
	Map mHeaders;
	Object mContent;
	boolean mReceived;
	
	public IonMessage(boolean received) {
		mReceived = received;
	}
	
	public Envelope getEnvelope() {
		return mEnvelope;
	}

	public byte[] getBody() {
		return mBody;
	}
	
	public Map getIonHeaders() {
		return mHeaders;
	}

	public Object getContent() {
		return mContent;
	}

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
	
	public boolean isErrorMessage() {
        return (mContent instanceof Map && "ERROR".equals(((Map) mContent).get("status")));
	}
	
	public DataObject extractDataObject() {
		if (! hasDataObject()) {
			return null;
		}

		String dovalstr = (String) ((Map) mContent).get("value");
		DataObject dobj = DataObjectManager.fromValueString(dovalstr);
	    return dobj;
	}
}
