package ion.resource;

import ion.core.data.DataObject;

import java.util.List;

/**
 * @author mmeisinger
 */
public class FindResourceDO extends DataObject {

	/**
	 * Class Constructor.
	 *
	 * @param desc           The ResourceDO
	 * @param ignoreDefaults Boolean value to ignore defaults
	 * @param regex          Boolean value to set regex
	 * @param attNames       List containing attribute names
	 */
	public FindResourceDO(ResourceDO desc, boolean ignoreDefaults, boolean regex, List attNames) {

		super();
		this.mDOClass = "FindResourceContainer";
		this.addAttribute("description", desc);
		this.addAttribute("ignore_defaults", ignoreDefaults);
		this.addAttribute("regex", regex);
		this.addAttribute("attnames", attNames);
	}
}
