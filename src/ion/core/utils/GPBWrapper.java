/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ion.core.utils;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage;
import ion.core.IonBootstrap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.ooici.core.container.Container;
import net.ooici.core.container.Container.StructureElement;
import net.ooici.core.link.Link.CASRef;
import net.ooici.core.type.Type;

/**
 *
 * Wrapper class that marries the GPBType and Proto Buff payload info
 *
 */
public class GPBWrapper<V> {

    private ByteString key;
    private String keyString;
    private String prettyKeyString;
    private Type.GPBType type;
    private ByteString value;
    private boolean isLeaf = true;
    private V _v;
    private CASRef ref;
    private Container.StructureElement se;

    /**
     * Factory method to return a GPBWrapper for a GeneratedMessage
     * <p>
     * This method iterates the list of fields in the generated message to determine the isLeaf parameter.
     * It assumes that the existence of a field that is of Type == MESSAGE net.ooici.core.link.CASRef results in isLeaf == false
     * @param content the StructureElement containing the object to wrap
     * @return the GPBWrapper
     */
    public static GPBWrapper Factory(GeneratedMessage content) {
        GPBWrapper ret = new GPBWrapper();
        ret.value = content.toByteString();
        ret.type = _getGPBType(content);
        ret.key = ret.getObjectKey();
        Map<FieldDescriptor, Object> fields = content.getAllFields();
        Iterator<Entry<FieldDescriptor, Object>> iter = fields.entrySet().iterator();
        while(iter.hasNext()) {
            FieldDescriptor desc = iter.next().getKey();
//            System.out.println(desc.getFullName());
//            System.out.println("\t" + desc.getClass().getName());
//            System.out.println("\t" + desc.getType().name());
            if(desc.getType().equals(FieldDescriptor.Type.MESSAGE)) {
//                System.out.println("\t" + desc.getMessageType().getFullName());
                /*TODO: I don't really like this much, but I couldn't find much else useful to use to determine isLeaf...? */
                if(desc.getMessageType().getFullName().equals("net.ooici.core.link.CASRef")) {
                    ret.isLeaf = false;
                    break;
                }
            }
        }

        return ret;
    }

    /**
     * Factory method to return a GPBWrapper for the object contained in a StructureElement
     * @param element the StructureElement containing the object to wrap
     * @return the GPBWrapper
     */
    public static GPBWrapper Factory(StructureElement element) {
        GPBWrapper ret = new GPBWrapper();
        ret.value = element.getValue();
        ret.type = element.getType();
        ret.key = element.getKey();
        ret.isLeaf = element.getIsleaf();

        return ret;
    }

    private GPBWrapper() {
    }

    /**
     * Packs the object of this GPBWrapper into a Container.StructureElement
     * @return StructureElement
     */
    public StructureElement getStructureElement() {
        return (se == null) ? se = Container.StructureElement.newBuilder().setKey(getObjectKey()).setType(type).setIsleaf(isLeaf).setValue(value).build() : se;
    }

    public CASRef getCASRef() {
        return (ref == null) ? ref = CASRef.newBuilder().setIsleaf(isLeaf).setKey(getObjectKey()).setType(type).build() : ref;
    }

    /**
     * Gets the String representation of the key
     * @return String key
     */
    public String getPrettyKeyString() {
        return (prettyKeyString == null) ? prettyKeyString = SHA1.bytesToHex(getObjectKey().toByteArray()) : prettyKeyString;
    }

    /**
     * Gets the String representation of the key
     * @return String key
     */
    public String getKeyString() {
        return (keyString == null) ? keyString = escapeBytes(getObjectKey()) : keyString;
    }
    /**
   * Escapes bytes in the format used in protocol buffer text format, which
   * is the same as the format used for C string literals.  All bytes
   * that are not printable 7-bit ASCII characters are escaped, as well as
   * backslash, single-quote, and double-quote characters.  Characters for
   * which no defined short-hand escape sequence is defined will be escaped
   * using 3-digit octal sequences.
   * <p>
   * This method was taken from com.google.protobuf.TextFormat.java - it could
   * not be called directly because it is package-private
   */
  private static String escapeBytes(final ByteString input) {
    final StringBuilder builder = new StringBuilder(input.size());
    for (int i = 0; i < input.size(); i++) {
      final byte b = input.byteAt(i);
      switch (b) {
        // Java does not recognize \a or \v, apparently.
        case 0x07: builder.append("\\a" ); break;
        case '\b': builder.append("\\b" ); break;
        case '\f': builder.append("\\f" ); break;
        case '\n': builder.append("\\n" ); break;
        case '\r': builder.append("\\r" ); break;
        case '\t': builder.append("\\t" ); break;
        case 0x0b: builder.append("\\v" ); break;
        case '\\': builder.append("\\\\"); break;
        case '\'': builder.append("\\\'"); break;
        case '"' : builder.append("\\\""); break;
        default:
          if (b >= 0x20) {
            builder.append((char) b);
          } else {
            builder.append('\\');
            builder.append((char) ('0' + ((b >>> 6) & 3)));
            builder.append((char) ('0' + ((b >>> 3) & 7)));
            builder.append((char) ('0' + (b & 7)));
          }
          break;
      }
    }
    return builder.toString();
  }

    /**
     * Gets the key of the wrapped object
     * @return ByteString the key
     */
    public ByteString getObjectKey() {
        return (key == null) ? key = _getObjectKey() : key;
    }
    private ByteString _getObjectKey() {
        byte[] key_ba = SHA1.getSHA1Hash(value.toByteArray());
        byte[] type_ba = type.toByteArray();
        byte[] comb = new byte[key_ba.length + type_ba.length];
        System.arraycopy(key_ba, 0, comb, 0, key_ba.length);
        System.arraycopy(type_ba, 0, comb, key_ba.length, type_ba.length);
        return ByteString.copyFrom(SHA1.getSHA1Hash(comb));
    }

    /**
     * Gets the type of the wrapped object
     * @return Type.GPBTye
     */
    public Type.GPBType getObjectType() {
        return type;
    }
    /**
     * Create a Type.GPBType for the object
     * @param proto buff object for which to generate the GPBType
     * @return Type.GPBType for the wrapped object
     */
    private static Type.GPBType _getGPBType(Object protoBuffObj) {
        Type.GPBType.Builder gpbType = Type.GPBType.newBuilder();
        int key = IonBootstrap.getKeyValueForMappedClass(protoBuffObj.getClass());
        gpbType.setObjectId(key);
        gpbType.setVersion(IonBootstrap.getMappedClassVersion(key));
        return gpbType.build();
    }

    /**
     * Convenience method for getting integer id of proto buff type
     * @return integer id for type of contained proto buff
     */
    public int getObjectID() {
        return type.getObjectId();
    }

    /**
     * Convenience method for getting integer version of proto buff type
     * @return integer version for type of contained proto buff
     */
    public int getObjectVersion() {
        return type.getVersion();
    }

    /**
     * Convenience method for getting proto buff type Class
     * @return Class for type of contained proto buff
     */
    //TODO: privatize
    public Class getTypeClass() {
        return IonBootstrap.getMappedClassForKeyValue(getObjectID());
    }

    //TODO: privatize
    public ByteString getValue() {
        return value;
    }

    /**
     * Parses the value of this GPBWrapper into the appropriate Java object
     * <p>
     * If the <V> has been specified, this will return the correct type.
     * @return the Java object
     */
    public V getObjectValue() {
        return (_v == null) ? _v = (V) _getObjectValue() : _v;
    }

    /**
     * Parses the value of this GPBWrapper into the appropriate Java object
     * <p>
     * If the <V> has been specified, this will return the correct type.
     * @return the Java object
     */
    private V _getObjectValue() {
        V ret = null;
        try {
            java.lang.reflect.Method parseFromMethod = getTypeClass().getDeclaredMethod("parseFrom", ByteString.class);
//			System.out.println(">>> Found Method: " + buildMethod);
            ret = (V) parseFromMethod.invoke(null, value);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace(System.out);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace(System.out);
        } catch (java.lang.reflect.InvocationTargetException ex) {
            ex.printStackTrace(System.out);
        }
        return ret;
    }

    @Override
    public String toString() {
        String nl = "\n";
        return new StringBuilder("message_object-J {\n")
                .append("  prettyKey: ").append(getPrettyKeyString()).append(nl)
                .append("  key: ").append(getKeyString()).append(nl)
                .append("  type {").append(nl)
                .append("    object_id: ").append(getObjectID()).append(nl)
                .append("    version: ").append(getObjectVersion()).append(nl)
                .append("  }").append(nl)
                .append("  isLeaf: ").append(isLeaf).append(nl)
                .append("  class: ").append(getTypeClass().getName()).append(nl)
                .append("}").append(nl).append(nl)
                .append("============== Object ==============\n")
                .append(getObjectValue())
                .append("====================================\n")
                .toString();
    }


////    public B b;
////    public <B extends Message.Builder> B getBuilderObject() {
////        return (b == null) ? b = (B) _getBuilderObject() : b;
////    }
//    public <B extends Message.Builder> B getBuilderObject() {
//        B ret = null;
//        try {
//            java.lang.reflect.Method buildMethod = getTypeClass().getDeclaredMethod("newBuilder");
////			System.out.println(">>> Found Method: " + buildMethod);
//            ret = (B) buildMethod.invoke(null, value);
//        } catch (java.lang.NoSuchMethodException ex) {
//            ex.printStackTrace(System.out);
//        } catch (java.lang.IllegalAccessException ex) {
//            ex.printStackTrace(System.out);
//        } catch (java.lang.reflect.InvocationTargetException ex) {
//            ex.printStackTrace(System.out);
//        }
//        return ret;
//    }
}
