package patterns;

public class Texture {
	private final Pigment3D pigment;
	private final Vector3D normal;
	
	public Texture(Pigment3D pigment, Vector3D normal) {
		this.pigment = pigment;
		this.normal = normal;
	}
	
	public Pigment3D getPigment() {
		return pigment;
	}
	
	public Vector3D getNormal() {
		return normal;
	}
}
