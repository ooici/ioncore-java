package ion.resource;

import ion.core.data.DataObject;

import java.util.UUID;

public class ResourceDO extends DataObject {

	private ResourceDO() {
		this("Resource");
	}

	public ResourceDO(String rclass) {
		super();
		mRegIdentity = "";
		mRegCommit = "";
		mRegBranch = "master";
		mDOClass = rclass;
	}
	
	public void create_identity() {
		UUID id = UUID.randomUUID();
		mRegIdentity = id.toString();
	}
}
