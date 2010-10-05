package ion.core.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains a structured object made of attributes and can encode
 * decode itself for persistence and transport.
 * 
 * Attributes are typed. DataObjects can be encoded and decoded to an intermediate 
 * format made only of basic data types, which can be wire encoded.
 * 
 * @author mmeisinger
 */
public class DataObject {
	protected String mDOClass = null;
	protected String mDOType = null;
	
	public String mRegIdentity = null;
	protected String mRegBranch = null;
	protected String mRegCommit = null;
	
	protected Map mAttributes = null;

	public DataObject() {
		mDOType = "DataObject";
		mAttributes = new HashMap();
	}

	public DataObject(DataObject dobj) {
		mDOClass = dobj.mDOClass;
		mDOType = dobj.mDOType;
		mRegIdentity = dobj.mRegIdentity;
		mRegBranch = dobj.mRegBranch;
		mRegCommit = dobj.mRegCommit;
		mAttributes = dobj.mAttributes;
	}

	public String getDOClass() {
		return mDOClass;
	}
	
	public String getIdentity() {
		return mRegIdentity;
	}

	public Map getAttributes() {
		return mAttributes;
	}
	
	public void addAttribute(String attrName, Object value) {
		mAttributes.put(attrName, value);
	}

	public Object getAttribute(String attrName) {
		return mAttributes.get(attrName);
	}

	public Object getAttributeStr(String attrName) {
		Object atto = mAttributes.get(attrName);
		if (atto == null) {
			return null;
		}
		return atto.toString();
	}

	public String toString() {
		
    	StringBuffer sb = new StringBuffer();
    	sb.append("DataObject[");
    	sb.append("*class="+mDOClass);
    	sb.append(",*type="+mDOType);
    	
    	if (mRegIdentity != null || mRegBranch != null || mRegCommit != null) {
	    	sb.append(",*id="+mRegIdentity);
	    	sb.append(",*branch="+mRegBranch);
	    	sb.append(",*commit="+mRegCommit);
    	}
    	sb.append(",");
    	for (Object me : mAttributes.entrySet()) {
    		String key = (String) ((Map.Entry) me).getKey();
    		Object value = ((Map.Entry) me).getValue();
    		String vtype = "UNKNOWN";
    		if (value instanceof String) {
    			vtype = "str";
    		} else if (value instanceof Integer) {
    			vtype = "int";
    		} else if (value instanceof Boolean) {
    			vtype = "bool";
    		} else if (value instanceof List) {
    			vtype = "list";
    		} else if (value instanceof DataObject) {
    			vtype = "DataObject";
    		}
    		sb.append(key);
    		sb.append('<');
    		sb.append(vtype);
    		sb.append(">=");
    		sb.append(value);
    		sb.append(',');
    	}
    	sb.append("]");
    	return sb.toString();
    }
}
