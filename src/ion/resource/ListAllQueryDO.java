package ion.resource;

import java.util.ArrayList;

/**
 * @author mmeisinger
 */
public class ListAllQueryDO extends FindResourceDO {

	/**
	 * Class Constructor.
	 *
	 * @param resdo The ResourceDO
	 */
	public ListAllQueryDO(ResourceDO resdo) {
		super(resdo, true, false, new ArrayList());
	}
}
