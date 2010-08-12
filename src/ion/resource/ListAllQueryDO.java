package ion.resource;

import java.util.ArrayList;

public class ListAllQueryDO extends FindResourceDO {

	public ListAllQueryDO(ResourceDO resdo) {
		super(resdo, true, false, new ArrayList());
	}
}
