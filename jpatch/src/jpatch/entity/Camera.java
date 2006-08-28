package jpatch.entity;

public class Camera extends AbstractNamedObject {

	public Attribute.BoundedDouble focalLength = new Attribute.BoundedDouble("Focal Length", 50);
	
	public void setParent(JPatchObject parent) {
		// TODO Auto-generated method stub
		
	}

}
