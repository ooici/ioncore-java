package ion.core;

import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DataObject {
	protected String mRegIdentity = null;
	protected String mRegBranch = null;
	protected String mRegCommit = null;
	
	public static Map decodeDataObject(String dostr) {
		  JSONParser parser=new JSONParser();

		  System.out.println("=======decode JSON=======");
		                
		  Object obj = null;
		  try {
			  obj = parser.parse(dostr);
		  } catch (ParseException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }
		  System.out.println("decode result: "+obj);
		  return (Map) obj;
	}
}
