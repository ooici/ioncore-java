package ion.core.messaging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.msgpack.Packer;

public class IonSendMessage extends IonMessage {

	private static int sConvCnt = 0;
	
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
		mHeaders.put("accept-encoding", "application/ion-jsond");
		mHeaders.put("encoding", "application/ion-jsond");
		mHeaders.put("content", content);
		mContent = content;
	}
	
	public byte[] getBody() {
		if (mBody == null) {
			Map bodymap = new HashMap(mHeaders);
			bodymap.put("content", mContent);
			byte[] bodyenc = encodeMessage(bodymap);
			mBody = bodyenc;
		}
		return mBody;
	}

	byte[] encodeMessage(Map msg) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Packer pack = new Packer(out);
		try {
			pack.pack(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] res = out.toByteArray();
		
		return res;
	}
}
