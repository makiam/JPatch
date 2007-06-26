package jpatch.entity;

public class Camera extends AbstractNamedObject {
	
	public ScalarAttribute.BoundedDouble focalLength = new ScalarAttribute.BoundedDouble("Focal Length", 50);
	
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
