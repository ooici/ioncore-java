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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Envelope;

/**
 * @author mmeisinger
 * @author Stephen Pasco
 */
public class IonAmqpMessage extends IonMessage {
    
    private static final Logger log = LoggerFactory.getLogger(IonAmqpMessage.class);

        private String encoding = "";
        private String contentKey = "content";
        private Object content = null;

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
	private Object decodeMessage(byte[] msgbytes) {

		ByteArrayInputStream bin = new ByteArrayInputStream(msgbytes);
		Unpacker unpack = new Unpacker(bin);
		UnpackResult result = null;

		try {
			result = unpack.next();
		}
		// TODO these possible exceptions should be handled by the caller OR
		// perhaps return null as an indication that there was an error...
		catch (UnpackException e) {
			log.error("Error in unpack.next call", e);
		}
		catch (IOException e) {
		    log.error("Error in unpack.next call", e);
		}

		// TODO this will throw a NPE in case of any of the exceptions above
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
	private Object decodeValue(Object obj) {
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

                                if(key.equalsIgnoreCase("content")) {
                                    if(!encoding.isEmpty()) {
                                        /* If the encoding indicates that this is a GPB data message, return the raw byte[] object */
                                        if(encoding.equalsIgnoreCase("ION R1 GPB")) {
                                            val = ((Map.Entry)me).getValue();
                                        }
                                    } else {
                                        /* Stash the content and decode it later */
                                        contentKey = key;
                                        content = ((Map.Entry) me).getValue();
                                        /* Don't add anything to the map! */
                                        continue;
                                    }
                                } else if (key.equalsIgnoreCase("encoding")) {
                                    /* Capture the encoding of the message so that the content of ION R1 GPB messages can be left as byte[] objects */
                                    encoding = val.toString();
                                    if (log.isDebugEnabled()) {
                                        log.debug("set encoding to: " + encoding);
                                    }
                                    /* Deal with the content now if it has been stashed */
                                    if(content != null) {
                                        if(encoding.equalsIgnoreCase("ION R1 GPB")) {
                                            newm.put(contentKey, content);
                                        } else {
                                            newm.put(contentKey, decodeValue(content));
                                        }
                                    }
                                }
				newm.put(key, val);
			}
			return newm;

		}
		return obj;
	}
}
