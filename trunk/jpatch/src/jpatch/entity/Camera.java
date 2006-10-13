package jpatch.entity;

public class Camera extends AbstractNamedObject {
	
	public Attribute.BoundedDouble focalLength = new Attribute.BoundedDouble("Focal Length", 50);
	
	private ObjectRegistry objectRegistry;
	
	public Camera(ObjectRegistry objectRegistry) {
		this.objectRegistry = objectRegistry;
	}
	
	public void setParent(JPatchObject parent) {
		// TODO Auto-generated method stub
		
	}

	public ObjectRegistry getObjectRegistry() {
		return objectRegistry;
	}

}
