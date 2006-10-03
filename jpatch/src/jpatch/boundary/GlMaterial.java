package jpatch.boundary;

import javax.vecmath.*;

public class GlMaterial {
	public static final int AMBIENT = 0;
	public static final int DIFFUSE = 4;
	public static final int SPECULAR = 8;
	public static final int SHININESS = 12;
	public final float[] array = new float[13];
	public final Color4f ka;
	public final Color4f kd;
	public final Color4f ks;
	public final float shininess;
	
	public GlMaterial(final Color3f ka, final Color3f kd, final Color3f ks, final float shininess) {
		this(
				new Color4f(ka.x, ka.y, ka.z, 1.0f),
				new Color4f(kd.x, kd.y, kd.z, 1.0f),
				new Color4f(ks.x, ks.y, ks.z, 1.0f),
				shininess
		);
	}
	
	public GlMaterial(final Color4f ka, final Color4f kd, final Color4f ks, final float shininess) {
		array[AMBIENT] = ka.x;
		array[AMBIENT + 1] = ka.y;
		array[AMBIENT + 2] = ka.z;
		array[DIFFUSE] = kd.x;
		array[DIFFUSE + 1] = kd.y;
		array[DIFFUSE + 2] = kd.z;
		array[SPECULAR] = ks.x;
		array[SPECULAR + 1] = ks.y;
		array[SPECULAR + 2] = ks.z;
		array[SHININESS] = shininess;
		this.ka = new Color4f(ka);
		this.kd = new Color4f(kd);
		this.ks = new Color4f(ks);
		this.shininess = shininess;
	}
}
