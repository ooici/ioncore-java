package ion.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import net.Init;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.GeneratedMessage;
import java.util.Iterator;

public class IonBootstrap {

	private static final String GET_DESCRIPTOR_METHOD_NAME = "getDescriptor";
	private static final String MESSAGE_TYPE_ID_ENUM_NAME = "_MessageTypeIdentifier";
	private static final String MESSAGE_TYPE_ID_ENUM_ID = "_ID";
	private static final String MESSAGE_TYPE_VERSION_ENUM_ID = "_VERSION";
	
	private static IonBootstrap instance = null;

	static {
		instance = new IonBootstrap();
	}

	// Map that contains the collection of enum value ==> Class mappings
	private static HashMap<Integer, Class> intToClassMap;
	
	// Backward map from Class to enum value
	private static HashMap<Class, Integer> classToIntMap;
	
	// Map from enum value to version value
	private static HashMap<Integer, Integer> intToVersionMap;

	private IonBootstrap() {
        intToClassMap = new HashMap<Integer, Class>();
		classToIntMap = new HashMap<Class, Integer>();
        intToVersionMap = new HashMap<Integer, Integer>();

		try {
			createProtoMap();
		}
		catch (Exception e) {
			// TODO log exception
			// Exit with non-zero code to indicate error
			System.exit(1);
		}
	}
	
	private static void checkInitialized() {
		if (instance == null) {
			// TODO log exception
			// Exit with non-zero code to indate error
			System.exit(1);
		}
	}

	public static void bootstrap() {
		checkInitialized();
	}

	/**
	 * Method which traverses the list of GeneratedMessage classes listed in
	 * the net.Init class and adds them to the enum value => Class hash map.
	 */
	private static void createProtoMap() throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		// net.Init class has public static String[] containing all the known
		// proto buffer class names
		for (String containerClass : Init.protos) {
            /* class names from Init.protos are separated with '/' - replace with '.' */
            containerClass = containerClass.replace("/", ".");
			// Can't instantiate class because default constructor is private.
			// But can call static method to get the Descriptor directly using reflection.
			Class containerClazz = Class.forName(containerClass);

			Method getDescriptorMethod = containerClazz.getMethod(GET_DESCRIPTOR_METHOD_NAME);
			Object fileDescriptorRetObject = getDescriptorMethod.invoke(null);

			// Cast return to correct Descriptor type
			FileDescriptor fileDescriptor = (FileDescriptor)fileDescriptorRetObject;

			// Iterate through contained message types
			for (Descriptor msgTypeDesc : fileDescriptor.getMessageTypes()) {

				// Derive a message class name we can use in the Class.forName() method
				String msgClassName = containerClass + "$" + msgTypeDesc.getName();

				// Get instance of Class.  This will be mapped to int value
				Class messageClazz = Class.forName(msgClassName);
				
				// Make sure the class is of the appropriate type
				assert (GeneratedMessage.class.isAssignableFrom(messageClazz));

				// Search the EnumDescriptors within the message type Descriptor
				for (EnumDescriptor enumDesc : msgTypeDesc.getEnumTypes()) {
					
					// If the enum type name matches the one of interest do
					if (enumDesc.getName().equals(MESSAGE_TYPE_ID_ENUM_NAME)) {
						
						// Make sure something odd didn't happen
						assert(enumDesc.getValues().size() == 2);
						int id = -1;
						for (EnumValueDescriptor enumValueDesc : enumDesc.getValues()) {
							
							// Get the _ID EnumValueDescriptor which will contain the value after
							// the = sign from the proto file
							if (enumValueDesc.getName().equals(MESSAGE_TYPE_ID_ENUM_ID)) {
								
								id = enumValueDesc.getNumber();
								// Make sure there isn't another class mapped to this value
								assert(!intToClassMap.containsKey(id));

								// Map the class
								intToClassMap.put(id, messageClazz);
								classToIntMap.put(messageClazz, id);
							}
						}

						assert(id != -1);
						for (EnumValueDescriptor enumValueDesc : enumDesc.getValues()) {
							// Get the _VERSION EnumValueDescriptor which will contain the value after
							// the = sign from the proto file
							if (enumValueDesc.getName().equals(MESSAGE_TYPE_VERSION_ENUM_ID)) {
								
								int version = enumValueDesc.getNumber();
								// Make sure there isn't another class mapped to this value
								assert(!intToVersionMap.containsKey(id));

								// Map the class
								intToVersionMap.put(id, version);
							}
						}
					}
				}
			}
		}
	}

	public static Class getMappedClassForKeyValue(int key) {
		checkInitialized();
		return intToClassMap.get(key);
	}
	
	public static int getKeyValueForMappedClass(Class key) {
		checkInitialized();
		return classToIntMap.get(key);
	}

	public static int getMappedClassVersion(int key) {
		checkInitialized();
		return intToVersionMap.get(key);
	}

	public static Set<Integer> getKeySet() {
		checkInitialized();
		return intToClassMap.keySet();
	}

	public static Collection<Class> getValueSet() {
		checkInitialized();
		return intToClassMap.values();
	}

	// Simple main routine to exercise the mapping logic
	public static void main(String[] args) {
			IonBootstrap.bootstrap();
            StringBuilder sb = new StringBuilder("Key = Value").append("\n");
            Integer[] ints = IonBootstrap.getKeySet().toArray(new Integer[0]);
            java.util.Arrays.sort(ints);
            for(Integer i : ints){
                sb.append(i).append(" = ").append(IonBootstrap.getMappedClassForKeyValue(i).getName()).append("\n");
            }
            System.out.println(sb.toString());
	}
}
