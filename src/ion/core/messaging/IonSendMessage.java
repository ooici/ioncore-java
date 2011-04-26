package ion.core.messaging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.msgpack.Packer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The IonSendMessage allows a message's headers to be set. The message headers define
 * such things as where a message is sent, where the message originates from, the message
 * operations and more.
 *
 * @author mmeisinger
 * @author Stephen Pasco
 */
public class IonSendMessage extends IonMessage {
    
    private static final Logger log = LoggerFactory.getLogger(IonSendMessage.class);

	// Message conversation id counter
	private static int sConvCnt = 0;

	/**
	 * Class constructor
	 * 
	 * Establishes where a message is from, its destination, its operations and its content.
	 *
	 * @param from    Where the message is from
	 * @param to      The message destination
	 * @param op      The message operation
	 * @param content The message content
	 */
	public IonSendMessage(String from, String to, String op, Object content) {

		super(false);
		mHeaders = new HashMap();
		mHeaders.put("sender", from);
		mHeaders.put("receiver", to);
		mHeaders.put("reply-to", from);
		mHeaders.put("op", op);
		String convid = "#" + (sConvCnt++);
		mHeaders.put("conv-id", convid);
		mHeaders.put("conv-seq", 1);
		mHeaders.put("performative", "request");
		mHeaders.put("protocol", "rpc");
		mHeaders.put("accept-encoding", "application/ion-jsond");
		mHeaders.put("encoding", "application/ion-jsond");
		mHeaders.put("user-id", "ANONYMOUS");
		mHeaders.put("expiry", "0");
		mHeaders.put("content", content);
		mContent = content;
	}

	/**
	 * Returns the content of a message body
	 *
	 * @return Returns the byte array of a message body
	 */
	public byte[] getBody() {

		if (mBody == null) {
			Map bodymap = new HashMap(mHeaders);
			bodymap.put("content", mContent);
			byte[] bodyenc = encodeMessage(bodymap);
			mBody = bodyenc;
		}
		
		return mBody;
	}

	/**
	 * Encodes a message using MessagePack
	 *
	 * @param msg A java.util.Map of messages
	 * @return Returns a byte array of message pack encoded messages
	 * @see <a href="http://msgpack.org>Msgpack</a>
	 */
	byte[] encodeMessage(Map msg) {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Packer pack = new Packer(out);

		try {
			pack.pack(msg);
		}
		catch (IOException e) {
			// TODO the exception should propbably be handled by the caller
		    log.error("Error in pack.pack call", e);
		}
		byte[] res = out.toByteArray();

		return res;
	}
}
