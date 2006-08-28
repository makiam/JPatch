package jpatch.entity;

public abstract class AbstractNamedObject extends AbstractJPatchXObject {
	public Attribute.String name = new Attribute.String();
	
	public String getName() {
		return name.get();
	}
}
