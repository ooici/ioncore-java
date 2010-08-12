package ion.resource;

import ion.core.data.DataObject;

import java.util.HashMap;
import java.util.UUID;

public class ResourceDO extends DataObject {

	public ResourceDO(DataObject dobj) {
		super(dobj);
	}

	private ResourceDO() {
		this("Resource");
	}

	public ResourceDO(String rclass) {
		super();
		UUID id = UUID.randomUUID();
		mRegIdentity = id.toString();
		mRegCommit = "";
		mRegBranch = "master";
		mDOClass = rclass;
	}	
}
