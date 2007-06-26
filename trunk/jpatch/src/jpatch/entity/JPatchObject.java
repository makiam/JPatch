package jpatch.entity;

public interface JPatchObject {
	String getName();
	Iterable<ScalarAttribute> getAttributes();
	Iterable<ScalarAttribute> getChannels();
	ScalarAttribute getAttribute(int index);
	void setParent(JPatchObject parent);
//	void setObjectRegistry(ObjectRegistry objectRegistry);
	ObjectRegistry getObjectRegistry();
}
