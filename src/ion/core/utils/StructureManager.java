/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ion.core.utils;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import ion.core.messaging.IonMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.ooici.core.container.Container.*;
import net.ooici.core.link.Link.CASRef;

/**
 * Management class for packing, unpacking, and accessing members of {@link Container.Structure} objects
 * @author cmueller
 */
public class StructureManager {

    private HashMap<String, GPBWrapper> _map;
    private List<String> _headIds;
    private List<String> _itemIds;

    private StructureManager(Structure structure) {
        _map = new HashMap<String, GPBWrapper>();
        _headIds = new ArrayList<String>();
        _itemIds = new ArrayList<String>();
        addStructure(structure);
    }

    public static StructureManager Factory(IonMessage msg) {
        try {
            return new StructureManager(Structure.parseFrom((byte[]) msg.getContent()));
        } catch (InvalidProtocolBufferException ex) {
            return null;
        }
    }

    public static StructureManager Factory(Structure structure) {
        return new StructureManager(structure);
    }

    public List<String> addStructure(Structure structure) {
        if(structure.getHeadsCount() >= 1) {
            for(StructureElement se : structure.getHeadsList()) {
                GPBWrapper head = GPBWrapper.Factory(se);
                String key = head.getKeyString();
                _map.put(key, head);
                _headIds.add(key);
            }
        }
        if(structure.getItemsCount() >= 1) {
            for(StructureElement se : structure.getItemsList()) {
                GPBWrapper item = GPBWrapper.Factory(se);
                String key = item.getKeyString();
                _map.put(key, item);
                _itemIds.add(key);
            }
        }

        return _headIds;
    }

    public List<String> removeStructure(Structure structure) {
        if(structure.getHeadsCount() >= 1) {
            for(StructureElement se : structure.getHeadsList()) {
                GPBWrapper head = GPBWrapper.Factory(se);
                String key = head.getKeyString();
                _map.remove(key);
                _headIds.remove(key);
            }
        }
        if(structure.getItemsCount() >= 1) {
            for(StructureElement se : structure.getItemsList()) {
                GPBWrapper item = GPBWrapper.Factory(se);
                String key = item.getKeyString();
                _map.remove(key);
                _itemIds.remove(key);
            }
        }

        return _headIds;
    }

    public GPBWrapper getObjectWrapper(CASRef key) {
        if(key == null) {
            return null;
        }
        return getObjectWrapper(key.getKey());
    }

    public GPBWrapper getObjectWrapper(ByteString key) {
        return getObjectWrapper(GPBWrapper.escapeBytes(key));
    }

    public GPBWrapper getObjectWrapper(String key) {
        return _map.get(key);
    }

    public List<String> getHeadIds() {
        return _headIds;
    }
    public List<String> getItemIds() {
        return _itemIds;
    }

    public void clear() {
        _map.clear();
        _headIds.clear();
        _itemIds.clear();
    }

    @Override
    public String toString() {
        String nl = "\n";
        StringBuilder sb = new StringBuilder();
        sb.append("Head IDs:").append(nl);
        for(String s : getHeadIds()) {
            sb.append("\t").append(s).append(nl);
        }
        sb.append(nl).append("Item IDs:").append(nl);
        for(String s : getItemIds()) {
            sb.append("\t").append(s).append(nl);
        }
        
        return sb.toString();
    }
}
