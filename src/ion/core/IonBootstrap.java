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

public class IonBootstrap {

	private static final String GET_DESCRIPTOR_METHOD_NAME = "getDescriptor";
	private static final String MESSAGE_TYPE_ID_ENUM_NAME = "_MessageTypeIdentifier";
	private static final String MESSAGE_TYPE_ID_ENUM_ID = "_ID";

	// Map that will contain the collection of enum value ==> Class mappings
	private HashMap<Integer, Class> intToClassMap;
	
	// Backward map from Class to enum value
	private HashMap<Class, Integer> classToIntMap;

	private IonBootstrap() throws Exception {
		intToClassMap = new HashMap<Integer, Class>();
		classToIntMap = new HashMap<Class, Integer>();

		createProtoMap();
	}

	public static void bootstrap() {

	}

	/**
	 * Method which traverses the list of GeneratedMessage classes listed in
	 * the net.Init class and adds them to the enum value => Class hash map.
	 */
	private void createProtoMap() throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		// net.Init class has public static String[] containing all the known
		// proto buffer class names
		for (String containerClass : Init.protos) {
			// Can't instantiate class because default constructor is private.
			// But can call static method to get the Descriptor directly using reflection.
			Class containerClazz = Class.forName(containerClass);

			Method getDescriptorMethod = containerClazz.getMethod(GET_DESCRIPTOR_METHOD_NAME, null);
			Object fileDescriptorRetObject = getDescriptorMethod.invoke(null, null);

			// Cast return to correct Descriptor type
			FileDescriptor fileDescriptor = (FileDescriptor)fileDescriptorRetObject;

			// Iterate through contained message types
			for (Descriptor msgTypeDesc : fileDescriptor.getMessageTypes()) {

				// Derive a message class name we can use in the Class.forName() method
				String msgClassName = containerClass + "$" + msgTypeDesc.getName();

				// Get instance of Class.  This will be mapped to int value
				Class messageClazz = Class.forName(msgClassName);
				
				// Make sure the class is of the appropriate type
				assert (messageClazz.isInstance(GeneratedMessage.class));

				// Search the EnumDescriptors within the message type Descriptor
				for (EnumDescriptor enumDesc : msgTypeDesc.getEnumTypes()) {
					
					// If the enum type name matches the one of interest do
					if (enumDesc.getName().equals(MESSAGE_TYPE_ID_ENUM_NAME)) {
						
						// Make sure something odd didn't happen
						assert(enumDesc.getValues().size() == 2);
						for (EnumValueDescriptor enumValueDesc : enumDesc.getValues()) {
							
							// Get the _ID EnumValueDescriptor which will contain the value after
							// the = sign from the proto file
							if (enumValueDesc.getName().equals(MESSAGE_TYPE_ID_ENUM_ID)) {
								
								int number = enumValueDesc.getNumber();
								// Make sure there isn't another class mapped to this value
								assert(!intToClassMap.containsKey(number));

								// Map the class
								intToClassMap.put(number, messageClazz);
								classToIntMap.put(messageClazz, number);
							}
						}
					}
				}
			}
		}
	}

	public Class getMappedClassForKeyValue(int key) {
		return intToClassMap.get(key);
	}
	
	public int getKeyValueForMappedClass(Class key) {
		return classToIntMap.get(key);
	}

	public Set<Integer> getKeySet() {
		return intToClassMap.keySet();
	}

	public Collection<Class> getValueSet() {
		return intToClassMap.values();
	}

	// Simple main routine to exercise the mapping logic
	public static void main(String[] args) {
		try {
			IonBootstrap bootstrap = new IonBootstrap();
			System.out.println("Keys: " + bootstrap.getKeySet());
			System.out.println("Keys: " + bootstrap.getValueSet());
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}

	}
}
