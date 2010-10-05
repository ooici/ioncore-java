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

/**
 * @author mmeisinger
 * @author Stephen Pasco
 */
public class IonAmqpMessage extends IonMessage {

	/**
	 * Class Constructor.
	 * 
	 * Enables the class establishment of the message envelope and message body.
	 *
	 * @param envelope The message envelope
	 * @param body     The message body
	 */
	public IonAmqpMessage(Envelope envelope, byte[] body) {

		super(true);
		mEnvelope = envelope;
		mBody = body;

		mHeaders = (Map) decodeMessage(body);
		mContent = mHeaders.remove("content");
	}

	/**
	 * Allows a MessagePack encoded message to be decoded
	 *
	 * @param msgbytes The encoded message as a byte array
	 * @return The message decoded
	 * @see <a href="http://http://msgpack.org/">MessagePack</a>
	 */
	public static Object decodeMessage(byte[] msgbytes) {

		ByteArrayInputStream bin = new ByteArrayInputStream(msgbytes);
		Unpacker unpack = new Unpacker(bin);
		UnpackResult result = null;

		try {
			result = unpack.next();
		}
		catch (UnpackException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Object msgData = decodeValue(result.getData());

		return msgData;
	}

	/**
	 * This is a helper method useful for taking a formerly decoded, MessagePacked, object and
	 * turning it into something (String, List or Map) that's human readable.
	 *
	 * @param obj The decoded raw object
	 * @return Returns an Object either as a String or a Collection (List or Map). String objects
	 * should be human readable. Objects of type Collection can be read via toString() functions.
	 */
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
				newm.put(key, val);
			}
			return newm;

		}
		return obj;
	}
}
