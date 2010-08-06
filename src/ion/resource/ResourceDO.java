package ion.resource;

import ion.core.data.DataObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ResourceDO extends DataObject {

	public ResourceDO(DataObject dobj) {
		super(dobj);
	}
	
	public ResourceDO() {
		super();
		UUID id = UUID.randomUUID();
		mRegIdentity = id.toString();
		mRegCommit = "";
		mRegBranch = "master";
		mDOClass = "Resource";
		mDOType = "DataObject";
		mAttributes = new HashMap();
	}
	
}
