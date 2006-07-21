package jpatch.entity;

public interface JPatchObject {
	String getName();
	Iterable<Attribute> getAttributes();
	Iterable<Attribute> getChannels();
	Attribute getAttribute(String name);
	Attribute getAttribute(int index);
	void setParent(JPatchObject parent);
}
