package ion.resource;

import ion.core.data.DataObject;

import java.util.UUID;

/**
 * @author Michael Meisinger
 */
public class ResourceDO extends DataObject {

	private ResourceDO() {
		this("Resource");
	}

	/**
	 * Class Constructor.
	 *
	 * @param rclass String representing the name of the DataObject class.
	 */
	public ResourceDO(String rclass) {
		super();
		mRegIdentity = "";
		mRegCommit = "";
		mRegBranch = "master";
		mDOClass = rclass;
	}

	/**
	 * Creates a unique (String) identity value using UUID.randomUUID().
	 */
	public void create_identity() {
		UUID id = UUID.randomUUID();
		mRegIdentity = id.toString();
	}
}
