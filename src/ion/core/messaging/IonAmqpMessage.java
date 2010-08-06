package ion.core.messaging;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.msgpack.UnpackException;
import org.msgpack.UnpackResult;
import org.msgpack.Unpacker;

import com.rabbitmq.client.Envelope;

public class IonAmqpMessage extends IonMessage {

	public IonAmqpMessage(Envelope envelope, byte[] body) {
		super(true);
		mEnvelope = envelope;
		mBody = body;

		mHeaders = (Map) decodeMessage(body);
		mContent = mHeaders.remove("content");
	}

	public static Object decodeMessage(byte[] msgbytes) {
		ByteArrayInputStream bin = new ByteArrayInputStream(msgbytes);
		Unpacker unpack = new Unpacker(bin);
		UnpackResult result = null;
		try {
			result = unpack.next();
		} catch (UnpackException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Object msgData = decodeValue(result.getData());
		
		return msgData;
	}
	
	public static Object decodeValue(Object obj) {
		if (obj instanceof byte[]) {
			return new String((byte[]) obj);
		}
		else if (obj instanceof List) {
			List newl = new ArrayList();
			for (Object v : ((List) obj)) {
				newl.add(decodeValue(v));
			}
			return newl;
		}
		else if (obj instanceof Map) {
			Map newm = new HashMap();
			for (Object me : ((Map) obj).entrySet()) {
				String key = (String) decodeValue(((Map.Entry) me).getKey());
				Object val = decodeValue(((Map.Entry) me).getValue());
				newm.put(key,val);
			}
			return newm;

		}
		return obj;
	}
}
