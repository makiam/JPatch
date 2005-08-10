package jpatch.boundary;

import javax.vecmath.*;
import jpatch.auxilary.*;
import jpatch.entity.*;

public class BoneShape extends SimpleShape
implements Comparable {
	static final float R = 0.05f;
	static final float A = 0.2f;
	static final float B = A - 1;
	
	static final MaterialProperties[] MATERIAL = new MaterialProperties[] {
		new MaterialProperties(1,0,0),
		new MaterialProperties(1,1,0),
		new MaterialProperties(0,1,0),
		new MaterialProperties(0,1,1),
		new MaterialProperties(0,0,1),
		new MaterialProperties(1,0,1),
	};
	
	private final Bone bone;
	
	public BoneShape(Bone bone) {
		this.bone = bone;
		ap3Points = new Point3f[6];
		av3Normals = new Vector3f[8];
		for (int i = 0; i < 6; ap3Points[i++] = new Point3f());
		for (int i = 0; i < 8; av3Normals[i++] = new Vector3f());
		aiTriangles = new int[] {
			0,1,2,
			0,2,3,
			0,3,4,
			0,4,1,
			5,2,1,
			5,3,2,
			5,4,3,
			5,1,4
		};
		aiNormalIndices = new int[] {
			0,
			1,
			2,
			3,
			4,
			5,
			6,
			7
		};
		mp = new MaterialProperties();
	}
	
	public void reset() {
		ap3Points[0].set( 0, 0, 0);
		ap3Points[1].set(-R, A,-R);
		ap3Points[2].set( R, A,-R);
		ap3Points[3].set( R, A, R);
		ap3Points[4].set(-R, A, R);
		ap3Points[5].set( 0, 1, 0);
		av3Normals[0].set( 0,-R,-A);
		av3Normals[1].set( A,-R, 0);
		av3Normals[2].set( 0,-R, A);
		av3Normals[3].set(-A,-R, 0);
		av3Normals[4].set( 0, R, B);
		av3Normals[5].set(-B, R, 0);
		av3Normals[6].set( 0, R,-B);
		av3Normals[7].set( B, R, 0);
	}
	
	public void set(Point3f p3A, Point3f p3B) {
		reset();
		Vector3f v3Y = new Vector3f(p3B);
		v3Y.sub(p3A);
		float length = v3Y.length();
		v3Y.scale(1f / length);
		Vector3f v3X = Utils3D.perpendicularVector(v3Y);
		Vector3f v3Z = new Vector3f();
		v3Z.cross(v3X, v3Y);
		Matrix4f matrix = new Matrix4f(
			v3X.x * length, v3Y.x * length, v3Z.x * length,p3A.x,
			v3X.y * length, v3Y.y * length, v3Z.y * length,p3A.y,
			v3X.z * length, v3Y.z * length, v3Z.z * length,p3A.z,
			   0f,    0f,    0f,   1f);
			//v3X.x, v3X.y, v3X.z,p3A.x,
			//v3Y.x, v3Y.y, v3Y.z,p3A.y,
			//v3Z.x, v3Z.y, v3Z.z,p3A.z,
			//0f,    0f,    0f,   1f);
		transform(matrix);
	}
	
	public void set() {
		set(bone.getStart(), bone.getEnd());
	}
	
	public Bone getBone() {
		return bone;
	}
	
	public MaterialProperties getMaterialProperties() {
		return MATERIAL[bone.getNumber() % MATERIAL.length];
	}
	
	public int compareTo(Object o) {
		//System.out.println("compare" + this + " " + o);
		BoneShape boneShape = (BoneShape) o;
		Point3f p3Center = new Point3f();
		p3Center.interpolate(ap3Points[0],ap3Points[5],0.5f);
		return -Float.compare(p3Center.z,Utils3D.closestPointOnLine(boneShape.ap3Points[0],boneShape.ap3Points[5],p3Center).z);
		//return Float.compare(Utils3D.closestPointOnLine(boneShape.ap3Points[0], boneShape.ap3Points[5], ap3Points[0]).z, ap3Points[0].z);
	}
}
