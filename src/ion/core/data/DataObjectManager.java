package ion.core.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * @author Michael Meisinger
 * @author Stephen Pasco
 */
public class DataObjectManager {

	private static Map sDOTypes = new HashMap();

	/**
	 * Registers a DataObject subclass for automatic factory instantiation on decode.
	 *
	 * @param cls A subclass of DataObject
	 */
	public static void registerDOType(Class cls) {
		
		if (!DataObject.class.isAssignableFrom(cls)) {
			System.out.println("Not a DataObject subclass: " + cls);
		}
		try {
			DataObject dobj = (DataObject) cls.newInstance();
			String doclassname = dobj.getDOClass();
			sDOTypes.put(doclassname, cls);
			System.out.println("Registered DataObject type: " + doclassname);
		}
		catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Returns a DataObject instance of a particular type
	 *
	 * @param doclass String name of DataObject instance
	 * @return Returns DataObject instance of type requested
	 */
	public static DataObject getDOInstance(String doclass) {
		
		Class docls = (Class) sDOTypes.get(doclass);
		
		if (docls == null) {
			docls = DataObject.class;
		}

		DataObject dobj = null;
		
		try {
			dobj = (DataObject) docls.newInstance();
		}
		catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return dobj;
	}

	/**
	 * MsgPack decoding of a JSON encoded String into a pre-populated DataObject. 
	 *
	 * @param valstr String value to be decoded
	 * @return Returns decoded String as a DataObject
	 */
	public static DataObject fromValueString(String valstr) {
		
		Map valmap = decodeDataObject(valstr);
		DataObject dobj = fromValueMap(valmap);
		return dobj;
	}

	/**
	 * Decodes the intermediate format contents of a DataObject from a transport
	 * format encoded String representation
	 *
	 * @param dostr DataObject String
	 * @return Returns a Map with the intermediate format contents
	 */
	static Map decodeDataObject(String dostr) {
		
		JSONParser parser = new JSONParser();
		Object obj = null;
		
		try {
			obj = parser.parse(dostr);
		}
	catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (Map) obj;
	}

	/**
	 * Takes a Map containing class and field information and repurposes it into a DataObject. 
	 *
	 * @param valmap a Map containing "class" and "field" keys
	 * @return Returns a DataObject
	 */
	private static DataObject fromValueMap(Map valmap) {
		
		String doclass = valmap.get("class").toString();
		DataObject newDO = getDOInstance(doclass);
		newDO.mDOClass = doclass;

		Map fields = (Map) valmap.get("fields");
		assembleDataObject(newDO, fields);

		return newDO;
	}

	/**
	 * Given a Map containing registry fields, a DataObject is constructed similarly. 
	 *
	 * @param dobj DataObject to be populated with Map fields
	 * @param fields Map containing fields to be copied into DataObject
	 */
	static void assembleDataObject(DataObject dobj, Map fields) {

		// Extract the data store commit identifier values first
		dobj.mRegIdentity = getAttributeStr(fields, "RegistryIdentity");
		dobj.mRegBranch = getAttributeStr(fields, "RegistryBranch");
		dobj.mRegCommit = getAttributeStr(fields, "RegistryCommit");
		
		fields.remove("RegistryIdentity");
		fields.remove("RegistryBranch");
		fields.remove("RegistryCommit");

		Map doattrs = new HashMap();
		
		for (Object meo : ((Map) fields).entrySet()) {
			Map.Entry me = (Map.Entry) meo;
			String attname = (String) me.getKey();
			Map attmap = (Map) me.getValue();
			Object attvalue = getValueFromValueMap(attmap);
			doattrs.put(attname, attvalue);
		}

		dobj.mAttributes = doattrs;
	}

	/**
	 * Gets values from a value map and returns them as an Object
	 *
	 * @param attmap Map containing "list" or "DataObject" attributes
	 * @return Returns a List or DataObject
	 */
	static Object getValueFromValueMap(Map attmap) {
		
		String atttype = (String) attmap.get("type");
		Object attvalue = attmap.get("value");
		
		if (atttype.equals("list")) {
			Map limap = (Map) attvalue;
			List li = new ArrayList();
			Object lio = null;
			for (int idx = 0; (lio = limap.get("" + idx)) != null; idx++) {
				Object lival = getValueFromValueMap((Map) lio);
				li.add(lival);
			}
			attvalue = li;
		}
		else if (atttype.equals("DataObject")) {
			DataObject valdo = fromValueMap((Map) attmap);
			attvalue = valdo;
		}

		return attvalue;
	}

	/**
	 * Gets the specified attribute from a Map
	 *
	 * @param doattrs Map containing an attribute of interest
	 * @param attrName String value representing attribute of interest
	 * @return Returns a String value of the specified attribute
	 */
	public static String getAttributeStr(Map doattrs, String attrName) {
		Object attr = doattrs.get(attrName);
		if (attr != null) {
			Object attrv = ((Map) attr).get("value");
			if (attrv != null) {
				return attrv.toString();
			}
		}
		return null;
	}

	/**
	 * Takes a DataObject and converts it to a Map with MsgPack encoded content
	 *
	 * @param dobj DataObject to convert to a Map
	 * @return Returned Map object containing MsgPack encoded DataObject values.
	 */
	public static Map toValueMap(DataObject dobj) {

		Map domap = new HashMap();
		
		domap.put("class", dobj.mDOClass);
		domap.put("type", dobj.mDOType);

		Map fieldsmap = new HashMap();

		encodeValue("RegistryIdentity", dobj.mRegIdentity, "str", fieldsmap);
		encodeValue("RegistryBranch", dobj.mRegBranch, "str", fieldsmap);
		encodeValue("RegistryCommit", dobj.mRegCommit, "str", fieldsmap);

		for (Object meo : ((Map) dobj.mAttributes).entrySet()) {
			
			Map.Entry me = (Map.Entry) meo;
			String attname = (String) me.getKey();
			Object attvalue = me.getValue();
			
			if (attvalue instanceof String) {
				encodeValue(attname, attvalue, "str", fieldsmap);
			}
			else if (attvalue instanceof Integer) {
				encodeValue(attname, attvalue, "int", fieldsmap);
			}
			else if (attvalue instanceof Boolean) {
				encodeValue(attname, attvalue, "bool", fieldsmap);
			}
			else if (attvalue instanceof List) {
				Map listmap = new HashMap();
				int lidx = 0;
				for (Object li : (List) attvalue) {
					listmap.put("" + (lidx++), li);
				}
				encodeValue(attname, listmap, "list", fieldsmap);
			}
			else if (attvalue instanceof DataObject) {
				fieldsmap.put(attname, toValueMap((DataObject) attvalue));
			}
		}

		domap.put("fields", fieldsmap);
		
		return domap;
	}

	/**
	 * Takes a Map and adds to it new values based on the passed in parameters
	 *
	 * @param attrName String representing a Map
	 * @param value The "value" value gets added to valmap
	 * @param valtype The "type" value gets added to valmap
	 * @param valmap Map upon which the attrName (key) gets a new value
	 */
	static void encodeValue(String attrName, Object value, String valtype, Map valmap) {

		Map attrValMap = new HashMap();

		attrValMap.put("type", valtype);
		attrValMap.put("value", value);
		
		valmap.put(attrName, attrValMap);
	}

	/**
	 * Extracts a Map from the passed in DataObject then JSON decodes the map to a String. The String value is returned.
	 *
	 * @param dobj DataObject containing internal Map which get extracted and converted to a JSON String.
	 * @return Returned JSON string
	 */
	public static String toValueString(DataObject dobj) {

		Map valmap = toValueMap(dobj);
		String dovalstr = JSONValue.toJSONString(valmap);
		
		return dovalstr;
	}
}
