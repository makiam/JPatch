package jpatch.boundary.tree;

import java.util.*;
import jpatch.boundary.*;
import jpatch.entity.*;

public class EntityRegistry {
	private static final EntityRegistry INSTANCE = new EntityRegistry();
	private final Map<SelectionTreeNode, Selection> selectionMap = new HashMap<SelectionTreeNode, Selection>();
	
	private EntityRegistry() { }
	
	public static EntityRegistry getInstance() {
		return INSTANCE;
	}
	
	public void put(Object key, Object value) {
		if (key instanceof SelectionTreeNode)
			selectionMap.put((SelectionTreeNode) key, (Selection) value);
	}
	
	public void remove(Object key) {
		if (key instanceof SelectionTreeNode)
			selectionMap.remove(key);
	}
	
	public Selection getSelection(SelectionTreeNode key) {
		return selectionMap.get(key);
	}
}
