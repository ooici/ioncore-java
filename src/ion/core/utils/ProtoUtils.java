/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ion.core.utils;

import com.google.protobuf.ByteString;
import net.ooici.core.container.Container;
import net.ooici.core.link.Link.CASRef;
import net.ooici.core.type.Type;

/**
 *
 * @author cmueller
 */
public class ProtoUtils {

    private ProtoUtils() {
    }

    public static Container.StructureElement getStructureElement(boolean isLeaf, byte[] key, Type.GPBType gpbType, ByteString value) {
        com.google.protobuf.ByteString keyS = com.google.protobuf.ByteString.copyFrom(key);
        return getStructureElement(isLeaf, keyS, gpbType, value);
    }
    public static Container.StructureElement getStructureElement(boolean isLeaf, ByteString bsKey, Type.GPBType gpbType, ByteString value) {
        return Container.StructureElement.newBuilder().setIsleaf(isLeaf).setKey(bsKey).setType(gpbType).setValue(value).build();
    }

    public static Type.GPBType getGPBType(Class clazz) {
        int _ID = ion.core.IonBootstrap.getKeyValueForMappedClass(clazz);
        return Type.GPBType.newBuilder().setObjectId(_ID).setVersion(1).build();
    }

    public static CASRef getLink(boolean isLeaf, byte[] key, Type.GPBType gpbType) {
        com.google.protobuf.ByteString keyS = com.google.protobuf.ByteString.copyFrom(key);
        return CASRef.newBuilder().setKey(keyS).setType(gpbType).setIsleaf(isLeaf).build();
    }

    public static byte[] getObjectKey(ByteString bsContent, Type.GPBType type) {
        return getObjectKey(bsContent.toByteArray(), type);
    }

    public static byte[] getObjectKey(byte[] content, Type.GPBType type) {
        byte[] key = SHA1.getSHA1Hash(content);
        byte[] type_ba = type.toByteArray();
        byte[] comb = new byte[key.length + type_ba.length];
        System.arraycopy(key, 0, comb, 0, key.length);
        System.arraycopy(type_ba, 0, comb, key.length, type_ba.length);
        return SHA1.getSHA1Hash(comb);
    }
}
