package ion.core.data;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DataObject {
	protected String mDOClass = null;
	protected String mDOType = null;
	
	protected String mRegIdentity = null;
	protected String mRegBranch = null;
	protected String mRegCommit = null;
	
	protected Map mAttributes = null;

	public DataObject() {
	}

	public DataObject(DataObject dobj) {
		mDOClass = dobj.mDOClass;
		mDOType = dobj.mDOType;
		mAttributes = dobj.mAttributes;
		mRegIdentity = dobj.mRegIdentity;
		mRegBranch = dobj.mRegBranch;
		mRegCommit = dobj.mRegCommit;
	}

	public static DataObject fromValueString(String valstr) {
		DataObject newDO = new DataObject();
		Map valmap = decodeDataObject(valstr);
		newDO.mDOClass = valmap.get("class").toString();
		newDO.mDOType = valmap.get("type").toString();
		newDO.mAttributes = (Map) valmap.get("fields");
		newDO.mRegIdentity = newDO.getAttributeStr("RegistryIdentity");
		newDO.mRegBranch = newDO.getAttributeStr("RegistryBranch");
		newDO.mRegCommit = newDO.getAttributeStr("RegistryCommit");
		newDO.mAttributes.remove("RegistryIdentity");
		newDO.mAttributes.remove("RegistryBranch");
		newDO.mAttributes.remove("RegistryCommit");

		return newDO;
	}
	
	static Map decodeDataObject(String dostr) {
		  JSONParser parser = new JSONParser();		                
		  Object obj = null;
		  try {
			  obj = parser.parse(dostr);
		  } catch (ParseException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }
		  return (Map) obj;
	}

	public void addAttribute(String attrName, String value) {
		addAttributeValue(attrName, value, "str", mAttributes);
	}

	public void addAttributeValue(String attrName, Object value, String valtype, Map valmap) {
		Map attrValMap = new HashMap();
		attrValMap.put("type", valtype);
		attrValMap.put("value", value);
		valmap.put(attrName, attrValMap);
	}

	public String encodeDataObject() {
		Map domap = new HashMap();
		domap.put("class", mDOClass);
		domap.put("type", mDOType);
		Map fieldsmap = new HashMap(mAttributes);
		addAttributeValue("RegistryIdentity", mRegIdentity, "str", fieldsmap);
		addAttributeValue("RegistryBranch", mRegBranch, "str", fieldsmap);
		addAttributeValue("RegistryCommit", mRegCommit, "str", fieldsmap);
		domap.put("fields", fieldsmap);
		String dovalstr = JSONValue.toJSONString(domap);
		return dovalstr;
	}
	
	public String getIdentity() {
		return mRegIdentity;
	}

	public Map getAttributes() {
		return mAttributes;
	}
	
	public String getAttributeStr(String attrName) {
		Object attr = mAttributes.get(attrName);
		if (attr != null) {
			Object attrv = ((Map) attr).get("value");
			if (attrv != null) {
				return attrv.toString();
			}
		}
		return null;
	}
	
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("DataObject[");
    	sb.append("class="+mDOClass);
    	sb.append(",type="+mDOType);
    	sb.append(",id="+mRegIdentity);
    	sb.append(",branch="+mRegBranch);
    	sb.append(",commit="+mRegCommit);
    	sb.append(",fields={");
    	for (Object me : mAttributes.entrySet()) {
    		String key = (String) ((Map.Entry) me).getKey();
    		Object value = ((Map.Entry) me).getValue();
    		Object value1 = ((Map) value).get("value");
    		String vtype = (String) ((Map) value).get("type");
    		sb.append(key);
    		sb.append('<');
    		sb.append(vtype);
    		sb.append(">=");
    		sb.append(value1);
    		sb.append(',');
    	}
    	sb.append("}]");
    	return sb.toString();
    }
}
