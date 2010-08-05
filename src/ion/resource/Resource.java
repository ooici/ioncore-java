package ion.resource;

import ion.core.DataObject;
import ion.core.IonMessaging;

import java.util.HashMap;
import java.util.Map;

public class Resource extends DataObject {
	Map mAttributes;
	
	public Resource() {
		mAttributes = new HashMap();
	}
	
	public static Resource fromDataObject(String dostr) {
		Resource res = new Resource();
		Map domap = IonMessaging.decodeDataObject(dostr);
		res.mRegIdentity = (String) domap.remove("RegistryIdentity");
		res.mRegBranch = (String) domap.remove("RegistryBranch");
		res.mRegCommit = (String) domap.remove("RegistryCommit");
		
		return res;
	}
}
