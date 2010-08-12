package ion.resource;

import ion.core.data.DataObject;

import java.util.List;

public class FindResourceDO extends DataObject {

	public FindResourceDO(ResourceDO desc, boolean ignoreDefaults, boolean regex, List attNames) {
		super();
		this.mDOClass = "FindResourceContainer";
		this.addAttribute("description", desc);
		this.addAttribute("ignore_defaults", ignoreDefaults);
		this.addAttribute("regex", regex);
		this.addAttribute("attnames", attNames);
	}
}
