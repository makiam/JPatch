package jpatch.entity;

public interface JPatchObject {
	String getName();
	Iterable<Attribute> getAttributes();
	Iterable<Attribute> getChannels();
	Attribute getAttribute(int index);
	void setParent(JPatchObject parent);
//	void setObjectRegistry(ObjectRegistry objectRegistry);
//	ObjectRegistry getObjectRegistry();
}