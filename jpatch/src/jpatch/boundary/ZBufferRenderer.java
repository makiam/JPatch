package jpatch.boundary;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import javax.vecmath.*;
import jpatch.entity.*;

public abstract class ZBufferRenderer {
	private static int iCurveSubdivisions;
	private static int iPatchSubdivisions;
	private static float[] B0;
	private static float[] B1;
	private static float[] B2;
	private static float[] B3;
	private static float[] dB0;
	private static float[] dB1;
	private static float[] dB2;
	private static float[] dB3;
	private static float[] cB0;
	private static float[] cB1;
	private static float[] cB2;
	private static float[] cB3;
	
	private static JPatchSettings settings = JPatchSettings.getInstance();
	
	private static final byte SUB_PIXEL_BITS = 16;
	private static final int SUB_PIXEL_MULTIPLIER = 65536;
	private static final int SUB_PIXEL_MASK_1 = 0xffff;
		
	protected int iWidth;
	protected int iHeight;
	private int[] aiActiveFrameBuffer;
	private int[] aiActiveZBuffer;
	private int[] aiImageZBuffer;
	private BufferedImage bufferedImage;
	
	//private ColorModel cm;
	//private ImageConsumer ic;
	private int iMaxZ = Integer.MAX_VALUE;

	private float fPolygonOffset = 2f;
	
	private int color;
	
	private static Point3f[] ap3;
	private static Vector3f[] av3N;
	private static int[] aiColor;
	
	private Vector3f v3dt = new Vector3f();
	private Vector3f v3ds = new Vector3f();
			
	/*
	protected Vector3f[] av3LightDirection;
	protected float[] afLightIntensity;
	protected boolean[] abLightSpecular;
	protected float fAmbientLight;
	
	protected Vector3f v3View = new Vector3f(0,0,1);
	*/
	
	protected Lighting lighting;
	protected boolean bBackfaceNormalFlip = false;
	private boolean bCulling = false;
	private boolean bMark = false;
	
	public ZBufferRenderer(Lighting lighting) {
		init();
		this.lighting = lighting;
		//initLight();
		//lighting = Lighting.createDefaultLight();
	}

	public static void init() {
		iPatchSubdivisions = settings.iPatchSubdivisions;
		iCurveSubdivisions = settings.iCurveSubdivisions;
		ap3 = new Point3f[iPatchSubdivisions * iPatchSubdivisions];
		av3N = new Vector3f[iPatchSubdivisions * iPatchSubdivisions];
		aiColor = new int[iPatchSubdivisions * iPatchSubdivisions];
		B0 = new float[iPatchSubdivisions];
		B1 = new float[iPatchSubdivisions];
		B2 = new float[iPatchSubdivisions];
		B3 = new float[iPatchSubdivisions];
		dB0 = new float[iPatchSubdivisions];
		dB1 = new float[iPatchSubdivisions];
		dB2 = new float[iPatchSubdivisions];
		dB3 = new float[iPatchSubdivisions];
		cB0 = new float[iCurveSubdivisions];
		cB1 = new float[iCurveSubdivisions];
		cB2 = new float[iCurveSubdivisions];
		cB3 = new float[iCurveSubdivisions];
		for (int i = 0; i < iPatchSubdivisions * iPatchSubdivisions; i++) {
			ap3[i] = new Point3f();
			av3N[i] = new Vector3f();
		}
		for (int i = 0; i < iPatchSubdivisions; i++) {
			float s = (float)i / (iPatchSubdivisions - 1);
			B0[i] = (1 - s) * (1 - s) * (1 - s);
			B1[i] = 3 * s * (1 - s) * (1 - s);
			B2[i] = 3 * s * s * (1 - s);
			B3[i] = s * s * s;
			dB0[i] = - 3 * (1 - s) * (1 - s);
			dB1[i] = -6 * s * (1 - s) + 3 * (1 - s) * (1 - s);
			dB2[i] = -3 * s * s + 6 * s * (1 - s);
			dB3[i] = 3 * s * s;
		}
		for (int i = 0; i < iCurveSubdivisions; i++) {
			float s = (float)i / (iCurveSubdivisions - 1);
			cB0[i] = (1 - s) * (1 - s) * (1 - s);
			cB1[i] = 3 * s * (1 - s) * (1 - s);
			cB2[i] = 3 * s * s * (1 - s);
			cB3[i] = s * s * s;
		}
	}

	//public void setBackfaceNormalFlip(boolean enable) {
	//	bBackfaceNormalFlip = enable;
	//}
	
	public void setCulling(int mode) {
		bCulling = (mode == 1);
		bMark = (mode == 2);
	}
	
	public void setLighting(Lighting lighting) {
		this.lighting = lighting;
	}
	
	public Lighting getLighting() {
		return lighting;
	}
	
	public Graphics getGraphics() {
		return bufferedImage.getGraphics();
	}
	
	public Image getImage() {
		return bufferedImage;
	}
	
	public void setColor(Color color) {
		this.color = color.getRGB();
	}
	
	public void setColor(int rgb) {
		this.color = rgb;
	}
	
	public void setImage(BufferedImage bufferedImage) {
		this.bufferedImage = bufferedImage;
		iWidth = bufferedImage.getWidth();
		iHeight = bufferedImage.getHeight();
		aiImageZBuffer = new int[iWidth * iHeight];
	}

	public void renderToImage() {
		try {
			aiActiveFrameBuffer = ((DataBufferInt)bufferedImage.getRaster().getDataBuffer()).getData();
		} catch (ClassCastException e) {
			//System.err.println("can not render directly!");
			bufferedImage = new BufferedImage(iWidth, iHeight, BufferedImage.TYPE_INT_RGB);
			aiActiveFrameBuffer = ((DataBufferInt)bufferedImage.getRaster().getDataBuffer()).getData();
		}
		aiActiveZBuffer = aiImageZBuffer;
	}
	
	public void setActiveBuffers(int[] frameBuffer, int[] zBuffer) {
		aiActiveFrameBuffer = frameBuffer;
		aiActiveZBuffer = zBuffer;
	}
	
	public int getSubdivisions() {
		return iPatchSubdivisions - 1;
	}
	
	public void setSubdivisions(int subdiv) {
		iPatchSubdivisions = ++subdiv;
		init();
	}
	
	public void clear() {
		Arrays.fill(aiActiveFrameBuffer, settings.iBackground);
		//Arrays.fill(aiActiveFrameBuffer, 0xff000000);
		Arrays.fill(aiActiveZBuffer, iMaxZ);
	}
	
	public void clearZBuffer() {
		Arrays.fill(aiActiveZBuffer, iMaxZ);
	}
	
	public void reset(int[] frameBuffer, int[] zBuffer) {
		int l = aiActiveFrameBuffer.length;
		for (int i = 0; i < l; i++) {
			aiActiveFrameBuffer[i] = frameBuffer[i];
			aiActiveZBuffer[i] = zBuffer[i];
		}
	}
	
	protected final void drawCurveSegment3D(Point3f p0, Point3f p1, Point3f p2, Point3f p3) {
		int t,x,y,z,xx,yy,zz;
		x = (int)p0.x;
		y = (int)p0.y;
		z = (int)((p0.z) * 65536);
		for (t = 1; t < iCurveSubdivisions - 1; t++) {
			xx = (int)(cB0[t] * p0.x + cB1[t] * p1.x + cB2[t] * p2.x + cB3[t] * p3.x);
			yy = (int)(cB0[t] * p0.y + cB1[t] * p1.y + cB2[t] * p2.y + cB3[t] * p3.y);
			zz = (int)(cB0[t] * p0.z + cB1[t] * p1.z + cB2[t] * p2.z + cB3[t] * p3.z) * 65536;
			drawLine3D(x,y,z,xx,yy,zz);
			x = xx;
			y = yy;
			z = zz;  
		}
		xx = (int)p3.x;
		yy = (int)p3.y;
		zz = (int)((p3.z) * 65536);
		drawLine3D(x,y,z,xx,yy,zz);	
	}

	public final void drawCurveSegment(Point3f p0, Point3f p1, Point3f p2, Point3f p3) {
		int t,x,y,z,xx,yy,zz;
		x = (int)p0.x;
		y = (int)p0.y;
		z = (int)((p0.z) * 65536);
		for (t = 1; t < iCurveSubdivisions - 1; t++) {
			xx = (int)(cB0[t] * p0.x + cB1[t] * p1.x + cB2[t] * p2.x + cB3[t] * p3.x);
			yy = (int)(cB0[t] * p0.y + cB1[t] * p1.y + cB2[t] * p2.y + cB3[t] * p3.y);
			zz = (int)(cB0[t] * p0.z + cB1[t] * p1.z + cB2[t] * p2.z + cB3[t] * p3.z) * 65536;
			drawLine3D(x,y,z,xx,yy,zz);
			x = xx;
			y = yy;
			z = zz;  
		}
		xx = (int)p3.x;
		yy = (int)p3.y;
		zz = (int)((p3.z) * 65536);
		drawLine3D(x,y,z,xx,yy,zz);
	}
	
	public final void drawBicubicPatchGourad(Point3f[] p, MaterialProperties mp) {
		float x0, x1, x2, x3, y0, y1, y2, y3, z0, z1, z2, z3;
		int index, s, t;
		boolean left, right, up, down;
		left = right = up = down = false;
		for (int i = 0; i < 16; i++) {
			left |= (p[i].x > 0);
			right |= (p[i].x < iWidth);
			up |= (p[i].y > 0);
			down |= (p[i].y < iHeight);
		}
		if (left && right && up && down) {
			//v3ds.set(0,0,0);
			//v3dt.set(0,0,0);
			switch (iPatchSubdivisions) {
				case 2:
					ap3[0].set(p[0]);
					ap3[1].set(p[3]);
					ap3[2].set(p[12]);
					ap3[3].set(p[15]);
					
					v3ds.sub(p[1],p[0]);
					v3dt.sub(p[4],p[0]);
					av3N[0].cross(v3ds,v3dt);
					av3N[0].normalize();
					
					v3ds.sub(p[2],p[3]);
					v3dt.sub(p[7],p[3]);
					av3N[1].cross(v3ds,v3dt);
					av3N[1].normalize();
					
					v3ds.sub(p[13],p[12]);
					v3dt.sub(p[8],p[12]);
					av3N[2].cross(v3ds,v3dt);
					av3N[2].normalize();
					
					v3ds.sub(p[14],p[15]);
					v3dt.sub(p[11],p[15]);
					av3N[3].cross(v3ds,v3dt);
					av3N[3].normalize();
					break;
			
			//	case 3:
			//		float px0 = p[0].x * 0.125f;
			//		float py0 = p[0].y * 0.125f;
			//		float pz0 = p[0].z * 0.125f;
			//		float px3 = p[3].x * 0.125f;
			//		float py3 = p[3].y * 0.125f;
			//		float pz3 = p[3].z * 0.125f;
			//		float px12 = p[12].x * 0.125f;
			//		float py12 = p[12].y * 0.125f;
			//		float pz12 = p[12].z * 0.125f;
			//		float px15 = p[15].x * 0.125f;
			//		float py15 = p[15].y * 0.125f;
			//		float pz15 = p[15].z * 0.125f;
			//		
			//		x0 = px0 + p[1].x * 0.375f + p[2].x * 0.375f + px3;
			//		y0 = py0 + p[1].y * 0.375f + p[2].y * 0.375f + py3;
			//		z0 = pz0 + p[1].z * 0.375f + p[2].z * 0.375f + pz3;
			//		
			//		x3 = px12 + p[13].x * 0.375f + p[14].x * 0.375f + px15;
			//		y3 = py12 + p[13].y * 0.375f + p[14].y * 0.375f + py15;
			//		z3 = pz12 + p[13].z * 0.375f + p[14].z * 0.375f + pz15;
			//		
			//		ap3[0].set(p[0]);
			//		ap3[1].set(x0,y0,z0);
			//		ap3[2].set(p[3]);
			//		ap3[3].x = px0 + p[4].x * 0.375f + p[8].x * 0.375f + px12;
			//		ap3[3].y = py0 + p[4].y * 0.375f + p[8].y * 0.375f + py12;
			//		ap3[3].z = pz0 + p[4].z * 0.375f + p[8].z * 0.375f + pz12;
			//		ap3[5].x = px3 + p[7].x * 0.375f + p[11].x * 0.375f + px15;
			//		ap3[5].y = py3 + p[7].y * 0.375f + p[11].y * 0.375f + py15;
			//		ap3[5].z = pz3 + p[7].z * 0.375f + p[11].z * 0.375f + pz15;
			//		ap3[6].set(p[12]);
			//		ap3[7].set(x3,y3,z3);
			//		ap3[8].set(p[15]);
			//		
			//		x1 = p[4].x * 0.125f + p[5].x * 0.375f + p[6].x * 0.375f+ p[7].x * 0.125f;
			//		x2 = p[8].x * 0.125f + p[9].x * 0.375f + p[10].x * 0.375f + p[11].x * 0.125f;
			//		y1 = p[4].y * 0.125f + p[5].y * 0.375f + p[6].y * 0.375f+ p[7].y * 0.125f;
			//		y2 = p[8].y * 0.125f + p[9].y * 0.375f + p[10].y * 0.375f + p[11].y * 0.125f;
			//		z1 = p[4].z * 0.125f + p[5].z * 0.375f + p[6].z * 0.375f+ p[7].z * 0.125f;
			//		z2 = p[8].z * 0.125f + p[9].z * 0.375f + p[10].z * 0.375f + p[11].z * 0.125f;
			//		
			//		ap3[4].x = x0 * 0.125f + x1 * 0.375f + x2 * 0.375f + x3 * 0.125f;
			//		ap3[4].y = y0 * 0.125f + y1 * 0.375f + y2 * 0.375f + y3 * 0.125f;
			//		ap3[4].z = z0 * 0.125f + z1 * 0.375f + z2 * 0.375f + z3 * 0.125f;
			//		
			//		v3ds.sub(p[1],p[0]);
			//		v3dt.sub(p[4],p[0]);
			//		av3N[0].cross(v3ds,v3dt);
			//		av3N[0].normalize();
			//		
			//		v3ds.sub(p[2],p[3]);
			//		v3dt.sub(p[7],p[3]);
			//		av3N[2].cross(v3ds,v3dt);
			//		av3N[2].normalize();
			//		
			//		v3ds.sub(p[13],p[12]);
			//		v3dt.sub(p[8],p[12]);
			//		av3N[6].cross(v3ds,v3dt);
			//		av3N[6].normalize();
			//		
			//		v3ds.sub(p[14],p[15]);
			//		v3dt.sub(p[11],p[15]);
			//		av3N[2].cross(v3ds,v3dt);
			//		av3N[2].normalize();
			//		
			//		x0 *= 0.75f;
			//		x1 *= 0.75f;
			//		x2 *= 0.75f;
			//		x3 *= 0.75f;
			//		y0 *= 0.75f;
			//		y1 *= 0.75f;
			//		y2 *= 0.75f;
			//		y3 *= 0.75f;
			//		z0 *= 0.75f;
			//		z1 *= 0.75f;
			//		z2 *= 0.75f;
			//		z3 *= 0.75f;
			//		
			//		v3ds.set(x2 + x3 - x0 - x1,y2 + y3 - y0 - y1,z2 + z3 - z0 - z1);
			//		v3dt.x = (p[2].x + p[3].x - p[0].x - p[1].x) * 0.09375f + (p[6].x + p[7].x - p[4].x - p[5].x) * 0.28125f + (p[10].x + p[11].x - p[8].x - p[9].x) * 0.28124f + (p[14].x + p[15].x - p[12].x - p[13].x) * 0.09375f;
			//		v3dt.y = (p[2].y + p[3].y - p[0].y - p[1].y) * 0.09375f + (p[6].y + p[7].y - p[4].y - p[5].y) * 0.28125f + (p[10].y + p[11].y - p[8].y - p[9].y) * 0.28124f + (p[14].y + p[15].y - p[12].y - p[13].y) * 0.09375f;
			//		v3dt.z = (p[2].z + p[3].z - p[0].z - p[1].z) * 0.09375f + (p[6].z + p[7].z - p[4].z - p[5].z) * 0.28125f + (p[10].z + p[11].z - p[8].z - p[9].z) * 0.28124f + (p[14].z + p[15].z - p[12].z - p[13].z) * 0.09375f;
			//		
			//		
			//		break;
				default:
					for (s = 0; s < iPatchSubdivisions; s++) {
						index = iPatchSubdivisions * s;
						for (t = 0; t < iPatchSubdivisions; t++) {
							x0 = p[0].x * B0[t] + p[1].x * B1[t] + p[2].x * B2[t] + p[3].x * B3[t];
							x1 = p[4].x * B0[t] + p[5].x * B1[t] + p[6].x * B2[t] + p[7].x * B3[t];
							x2 = p[8].x * B0[t] + p[9].x * B1[t] + p[10].x * B2[t] + p[11].x * B3[t];
							x3 = p[12].x * B0[t] + p[13].x * B1[t] + p[14].x * B2[t] + p[15].x * B3[t];
							y0 = p[0].y * B0[t] + p[1].y * B1[t] + p[2].y * B2[t] + p[3].y * B3[t];
							y1 = p[4].y * B0[t] + p[5].y * B1[t] + p[6].y * B2[t] + p[7].y * B3[t];
							y2 = p[8].y * B0[t] + p[9].y * B1[t] + p[10].y * B2[t] + p[11].y * B3[t];
							y3 = p[12].y * B0[t] + p[13].y * B1[t] + p[14].y * B2[t] + p[15].y * B3[t];
							z0 = p[0].z * B0[t] + p[1].z * B1[t] + p[2].z * B2[t] + p[3].z * B3[t];
							z1 = p[4].z * B0[t] + p[5].z * B1[t] + p[6].z * B2[t] + p[7].z * B3[t];
							z2 = p[8].z * B0[t] + p[9].z * B1[t] + p[10].z * B2[t] + p[11].z * B3[t];
							z3 = p[12].z * B0[t] + p[13].z * B1[t] + p[14].z * B2[t] + p[15].z * B3[t];
							ap3[index].set (x0 * B0[s] + x1 * B1[s] + x2 * B2[s] + x3 * B3[s],
									y0 * B0[s] + y1 * B1[s] + y2 * B2[s] + y3 * B3[s],
									z0 * B0[s] + z1 * B1[s] + z2 * B2[s] + z3 * B3[s]);
							v3ds.set(	x0 * dB0[s] + x1 * dB1[s] + x2 * dB2[s] + x3 * dB3[s],
									y0 * dB0[s] + y1 * dB1[s] + y2 * dB2[s] + y3 * dB3[s],
									z0 * dB0[s] + z1 * dB1[s] + z2 * dB2[s] + z3 * dB3[s]);
							v3dt.set(	B0[s] * (p[0].x * dB0[t] + p[1].x * dB1[t] + p[2].x * dB2[t] + p[3].x * dB3[t]) +
									B1[s] * (p[4].x * dB0[t] + p[5].x * dB1[t] + p[6].x * dB2[t] + p[7].x * dB3[t]) +
									B2[s] * (p[8].x * dB0[t] + p[9].x * dB1[t] + p[10].x * dB2[t] + p[11].x * dB3[t]) +
									B3[s] * (p[12].x * dB0[t] + p[13].x * dB1[t] + p[14].x * dB2[t] + p[15].x * dB3[t]),
									B0[s] * (p[0].y * dB0[t] + p[1].y * dB1[t] + p[2].y * dB2[t] + p[3].y * dB3[t]) +
									B1[s] * (p[4].y * dB0[t] + p[5].y * dB1[t] + p[6].y * dB2[t] + p[7].y * dB3[t]) +
									B2[s] * (p[8].y * dB0[t] + p[9].y * dB1[t] + p[10].y * dB2[t] + p[11].y * dB3[t]) +
									B3[s] * (p[12].y * dB0[t] + p[13].y * dB1[t] + p[14].y * dB2[t] + p[15].y * dB3[t]),
									B0[s] * (p[0].z * dB0[t] + p[1].z * dB1[t] + p[2].z * dB2[t] + p[3].z * dB3[t]) +
									B1[s] * (p[4].z * dB0[t] + p[5].z * dB1[t] + p[6].z * dB2[t] + p[7].z * dB3[t]) +
									B2[s] * (p[8].z * dB0[t] + p[9].z * dB1[t] + p[10].z * dB2[t] + p[11].z * dB3[t]) +
									B3[s] * (p[12].z * dB0[t] + p[13].z * dB1[t] + p[14].z * dB2[t] + p[15].z * dB3[t]));
							av3N[index].cross(v3ds, v3dt);
							av3N[index].normalize();
							index++;
						}
					}
					break;
			}
			if (bBackfaceNormalFlip) flipBackfaceNormals(av3N);
			shade(ap3, av3N, mp, aiColor);
			if (mp.isOpaque()) {
				draw3DTriangleGridGourad(ap3, aiColor, iPatchSubdivisions, iPatchSubdivisions);
			} else {
				int transparency = (int) (Math.min(1f,mp.transmit + mp.filter) * 255f);
				draw3DTriangleGridGouradTransparent(ap3, aiColor, iPatchSubdivisions, iPatchSubdivisions, transparency);
			}
		}
	}
	
	//public final void drawBicubicPatchPhong(Point3f[] p, MaterialProperties mp) {
	//	float x0, x1, x2, x3, y0, y1, y2, y3, z0, z1, z2, z3;
	//	int index, s, t;
	//	boolean left, right, up, down;
	//	left = right = up = down = false;
	//	for (int i = 0; i < 16; i++) {
	//		left |= (p[i].x > 0);
	//		right |= (p[i].x < iWidth);
	//		up |= (p[i].y > 0);
	//		down |= (p[i].y < iHeight);
	//	}
	//	if (left && right && up && down) {
	//		//v3ds.set(0,0,0);
	//		//v3dt.set(0,0,0);
	//		switch (iPatchSubdivisions) {
	//			case 2:
	//				ap3[0].set(p[0]);
	//				ap3[1].set(p[3]);
	//				ap3[2].set(p[12]);
	//				ap3[3].set(p[15]);
	//				
	//				v3ds.sub(p[1],p[0]);
	//				v3dt.sub(p[4],p[0]);
	//				av3N[0].cross(v3ds,v3dt);
	//				av3N[0].normalize();
	//				
	//				v3ds.sub(p[2],p[3]);
	//				v3dt.sub(p[7],p[3]);
	//				av3N[1].cross(v3ds,v3dt);
	//				av3N[1].normalize();
	//				
	//				v3ds.sub(p[13],p[12]);
	//				v3dt.sub(p[8],p[12]);
	//				av3N[2].cross(v3ds,v3dt);
	//				av3N[2].normalize();
	//				
	//				v3ds.sub(p[14],p[15]);
	//				v3dt.sub(p[11],p[15]);
	//				av3N[3].cross(v3ds,v3dt);
	//				av3N[3].normalize();
	//				break;
	//		
	//		//	case 3:
	//		//		float px0 = p[0].x * 0.125f;
	//		//		float py0 = p[0].y * 0.125f;
	//		//		float pz0 = p[0].z * 0.125f;
	//		//		float px3 = p[3].x * 0.125f;
	//		//		float py3 = p[3].y * 0.125f;
	//		//		float pz3 = p[3].z * 0.125f;
	//		//		float px12 = p[12].x * 0.125f;
	//		//		float py12 = p[12].y * 0.125f;
	//		//		float pz12 = p[12].z * 0.125f;
	//		//		float px15 = p[15].x * 0.125f;
	//		//		float py15 = p[15].y * 0.125f;
	//		//		float pz15 = p[15].z * 0.125f;
	//		//		
	//		//		x0 = px0 + p[1].x * 0.375f + p[2].x * 0.375f + px3;
	//		//		y0 = py0 + p[1].y * 0.375f + p[2].y * 0.375f + py3;
	//		//		z0 = pz0 + p[1].z * 0.375f + p[2].z * 0.375f + pz3;
	//		//		
	//		//		x3 = px12 + p[13].x * 0.375f + p[14].x * 0.375f + px15;
	//		//		y3 = py12 + p[13].y * 0.375f + p[14].y * 0.375f + py15;
	//		//		z3 = pz12 + p[13].z * 0.375f + p[14].z * 0.375f + pz15;
	//		//		
	//		//		ap3[0].set(p[0]);
	//		//		ap3[1].set(x0,y0,z0);
	//		//		ap3[2].set(p[3]);
	//		//		ap3[3].x = px0 + p[4].x * 0.375f + p[8].x * 0.375f + px12;
	//		//		ap3[3].y = py0 + p[4].y * 0.375f + p[8].y * 0.375f + py12;
	//		//		ap3[3].z = pz0 + p[4].z * 0.375f + p[8].z * 0.375f + pz12;
	//		//		ap3[5].x = px3 + p[7].x * 0.375f + p[11].x * 0.375f + px15;
	//		//		ap3[5].y = py3 + p[7].y * 0.375f + p[11].y * 0.375f + py15;
	//		//		ap3[5].z = pz3 + p[7].z * 0.375f + p[11].z * 0.375f + pz15;
	//		//		ap3[6].set(p[12]);
	//		//		ap3[7].set(x3,y3,z3);
	//		//		ap3[8].set(p[15]);
	//		//		
	//		//		x1 = p[4].x * 0.125f + p[5].x * 0.375f + p[6].x * 0.375f+ p[7].x * 0.125f;
	//		//		x2 = p[8].x * 0.125f + p[9].x * 0.375f + p[10].x * 0.375f + p[11].x * 0.125f;
	//		//		y1 = p[4].y * 0.125f + p[5].y * 0.375f + p[6].y * 0.375f+ p[7].y * 0.125f;
	//		//		y2 = p[8].y * 0.125f + p[9].y * 0.375f + p[10].y * 0.375f + p[11].y * 0.125f;
	//		//		z1 = p[4].z * 0.125f + p[5].z * 0.375f + p[6].z * 0.375f+ p[7].z * 0.125f;
	//		//		z2 = p[8].z * 0.125f + p[9].z * 0.375f + p[10].z * 0.375f + p[11].z * 0.125f;
	//		//		
	//		//		ap3[4].x = x0 * 0.125f + x1 * 0.375f + x2 * 0.375f + x3 * 0.125f;
	//		//		ap3[4].y = y0 * 0.125f + y1 * 0.375f + y2 * 0.375f + y3 * 0.125f;
	//		//		ap3[4].z = z0 * 0.125f + z1 * 0.375f + z2 * 0.375f + z3 * 0.125f;
	//		//		
	//		//		v3ds.sub(p[1],p[0]);
	//		//		v3dt.sub(p[4],p[0]);
	//		//		av3N[0].cross(v3ds,v3dt);
	//		//		av3N[0].normalize();
	//		//		
	//		//		v3ds.sub(p[2],p[3]);
	//		//		v3dt.sub(p[7],p[3]);
	//		//		av3N[2].cross(v3ds,v3dt);
	//		//		av3N[2].normalize();
	//		//		
	//		//		v3ds.sub(p[13],p[12]);
	//		//		v3dt.sub(p[8],p[12]);
	//		//		av3N[6].cross(v3ds,v3dt);
	//		//		av3N[6].normalize();
	//		//		
	//		//		v3ds.sub(p[14],p[15]);
	//		//		v3dt.sub(p[11],p[15]);
	//		//		av3N[2].cross(v3ds,v3dt);
	//		//		av3N[2].normalize();
	//		//		
	//		//		x0 *= 0.75f;
	//		//		x1 *= 0.75f;
	//		//		x2 *= 0.75f;
	//		//		x3 *= 0.75f;
	//		//		y0 *= 0.75f;
	//		//		y1 *= 0.75f;
	//		//		y2 *= 0.75f;
	//		//		y3 *= 0.75f;
	//		//		z0 *= 0.75f;
	//		//		z1 *= 0.75f;
	//		//		z2 *= 0.75f;
	//		//		z3 *= 0.75f;
	//		//		
	//		//		v3ds.set(x2 + x3 - x0 - x1,y2 + y3 - y0 - y1,z2 + z3 - z0 - z1);
	//		//		v3dt.x = (p[2].x + p[3].x - p[0].x - p[1].x) * 0.09375f + (p[6].x + p[7].x - p[4].x - p[5].x) * 0.28125f + (p[10].x + p[11].x - p[8].x - p[9].x) * 0.28124f + (p[14].x + p[15].x - p[12].x - p[13].x) * 0.09375f;
	//		//		v3dt.y = (p[2].y + p[3].y - p[0].y - p[1].y) * 0.09375f + (p[6].y + p[7].y - p[4].y - p[5].y) * 0.28125f + (p[10].y + p[11].y - p[8].y - p[9].y) * 0.28124f + (p[14].y + p[15].y - p[12].y - p[13].y) * 0.09375f;
	//		//		v3dt.z = (p[2].z + p[3].z - p[0].z - p[1].z) * 0.09375f + (p[6].z + p[7].z - p[4].z - p[5].z) * 0.28125f + (p[10].z + p[11].z - p[8].z - p[9].z) * 0.28124f + (p[14].z + p[15].z - p[12].z - p[13].z) * 0.09375f;
	//		//		
	//		//		
	//		//		break;
	//			default:
	//				for (s = 0; s < iPatchSubdivisions; s++) {
	//					index = iPatchSubdivisions * s;
	//					for (t = 0; t < iPatchSubdivisions; t++) {
	//						x0 = p[0].x * B0[t] + p[1].x * B1[t] + p[2].x * B2[t] + p[3].x * B3[t];
	//						x1 = p[4].x * B0[t] + p[5].x * B1[t] + p[6].x * B2[t] + p[7].x * B3[t];
	//						x2 = p[8].x * B0[t] + p[9].x * B1[t] + p[10].x * B2[t] + p[11].x * B3[t];
	//						x3 = p[12].x * B0[t] + p[13].x * B1[t] + p[14].x * B2[t] + p[15].x * B3[t];
	//						y0 = p[0].y * B0[t] + p[1].y * B1[t] + p[2].y * B2[t] + p[3].y * B3[t];
	//						y1 = p[4].y * B0[t] + p[5].y * B1[t] + p[6].y * B2[t] + p[7].y * B3[t];
	//						y2 = p[8].y * B0[t] + p[9].y * B1[t] + p[10].y * B2[t] + p[11].y * B3[t];
	//						y3 = p[12].y * B0[t] + p[13].y * B1[t] + p[14].y * B2[t] + p[15].y * B3[t];
	//						z0 = p[0].z * B0[t] + p[1].z * B1[t] + p[2].z * B2[t] + p[3].z * B3[t];
	//						z1 = p[4].z * B0[t] + p[5].z * B1[t] + p[6].z * B2[t] + p[7].z * B3[t];
	//						z2 = p[8].z * B0[t] + p[9].z * B1[t] + p[10].z * B2[t] + p[11].z * B3[t];
	//						z3 = p[12].z * B0[t] + p[13].z * B1[t] + p[14].z * B2[t] + p[15].z * B3[t];
	//						ap3[index].set (x0 * B0[s] + x1 * B1[s] + x2 * B2[s] + x3 * B3[s],
	//								y0 * B0[s] + y1 * B1[s] + y2 * B2[s] + y3 * B3[s],
	//								z0 * B0[s] + z1 * B1[s] + z2 * B2[s] + z3 * B3[s]);
	//						v3ds.set(	x0 * dB0[s] + x1 * dB1[s] + x2 * dB2[s] + x3 * dB3[s],
	//								y0 * dB0[s] + y1 * dB1[s] + y2 * dB2[s] + y3 * dB3[s],
	//								z0 * dB0[s] + z1 * dB1[s] + z2 * dB2[s] + z3 * dB3[s]);
	//						v3dt.set(	B0[s] * (p[0].x * dB0[t] + p[1].x * dB1[t] + p[2].x * dB2[t] + p[3].x * dB3[t]) +
	//								B1[s] * (p[4].x * dB0[t] + p[5].x * dB1[t] + p[6].x * dB2[t] + p[7].x * dB3[t]) +
	//								B2[s] * (p[8].x * dB0[t] + p[9].x * dB1[t] + p[10].x * dB2[t] + p[11].x * dB3[t]) +
	//								B3[s] * (p[12].x * dB0[t] + p[13].x * dB1[t] + p[14].x * dB2[t] + p[15].x * dB3[t]),
	//								B0[s] * (p[0].y * dB0[t] + p[1].y * dB1[t] + p[2].y * dB2[t] + p[3].y * dB3[t]) +
	//								B1[s] * (p[4].y * dB0[t] + p[5].y * dB1[t] + p[6].y * dB2[t] + p[7].y * dB3[t]) +
	//								B2[s] * (p[8].y * dB0[t] + p[9].y * dB1[t] + p[10].y * dB2[t] + p[11].y * dB3[t]) +
	//								B3[s] * (p[12].y * dB0[t] + p[13].y * dB1[t] + p[14].y * dB2[t] + p[15].y * dB3[t]),
	//								B0[s] * (p[0].z * dB0[t] + p[1].z * dB1[t] + p[2].z * dB2[t] + p[3].z * dB3[t]) +
	//								B1[s] * (p[4].z * dB0[t] + p[5].z * dB1[t] + p[6].z * dB2[t] + p[7].z * dB3[t]) +
	//								B2[s] * (p[8].z * dB0[t] + p[9].z * dB1[t] + p[10].z * dB2[t] + p[11].z * dB3[t]) +
	//								B3[s] * (p[12].z * dB0[t] + p[13].z * dB1[t] + p[14].z * dB2[t] + p[15].z * dB3[t]));
	//						av3N[index].cross(v3ds, v3dt);
	//						av3N[index].normalize();
	//						index++;
	//					}
	//				}
	//				break;
	//		}
	//		flipBackfaceNormals(av3N);
	//		//shade(ap3, av3N, mp, aiColor);
	//		draw3DTriangleGridPhong(ap3, av3N, mp, iPatchSubdivisions, iPatchSubdivisions);
	//	}
	//}
	
	public final void drawBicubicPatchFlat(Point3f[] p, MaterialProperties mp) {
		float x0, x1, x2, x3, y0, y1, y2, y3, z0, z1, z2, z3;
		int index, s, t;
		boolean left, right, up, down;
		left = right = up = down = false;
		for (int i = 0; i < 16; i++) {
			left |= (p[i].x > 0);
			right |= (p[i].x < iWidth);
			up |= (p[i].y > 0);
			down |= (p[i].y < iHeight);
		}
		if (left && right && up && down) {
			switch (iPatchSubdivisions) {
				case 2:
					ap3[0].set(p[0]);
					ap3[1].set(p[3]);
					ap3[2].set(p[12]);
					ap3[3].set(p[15]);
					break;
				case 3:
					float px0 = p[0].x * 0.125f;
					float py0 = p[0].y * 0.125f;
					float pz0 = p[0].z * 0.125f;
					float px3 = p[3].x * 0.125f;
					float py3 = p[3].y * 0.125f;
					float pz3 = p[3].z * 0.125f;
					float px12 = p[12].x * 0.125f;
					float py12 = p[12].y * 0.125f;
					float pz12 = p[12].z * 0.125f;
					float px15 = p[15].x * 0.125f;
					float py15 = p[15].y * 0.125f;
					float pz15 = p[15].z * 0.125f;
					
					x0 = px0 + p[1].x * 0.375f + p[2].x * 0.375f + px3;
					y0 = py0 + p[1].y * 0.375f + p[2].y * 0.375f + py3;
					z0 = pz0 + p[1].z * 0.375f + p[2].z * 0.375f + pz3;
					
					x3 = px12 + p[13].x * 0.375f + p[14].x * 0.375f + px15;
					y3 = py12 + p[13].y * 0.375f + p[14].y * 0.375f + py15;
					z3 = pz12 + p[13].z * 0.375f + p[14].z * 0.375f + pz15;
					
					ap3[0].set(p[0]);
					ap3[1].set(x0,y0,z0);
					ap3[2].set(p[3]);
					ap3[3].x = px0 + p[4].x * 0.375f + p[8].x * 0.375f + px12;
					ap3[3].y = py0 + p[4].y * 0.375f + p[8].y * 0.375f + py12;
					ap3[3].z = pz0 + p[4].z * 0.375f + p[8].z * 0.375f + pz12;
					ap3[5].x = px3 + p[7].x * 0.375f + p[11].x * 0.375f + px15;
					ap3[5].y = py3 + p[7].y * 0.375f + p[11].y * 0.375f + py15;
					ap3[5].z = pz3 + p[7].z * 0.375f + p[11].z * 0.375f + pz15;
					ap3[6].set(p[12]);
					ap3[7].set(x3,y3,z3);
					ap3[8].set(p[15]);
					
					x1 = p[4].x * 0.125f + p[5].x * 0.375f + p[6].x * 0.375f+ p[7].x * 0.125f;
					x2 = p[8].x * 0.125f + p[9].x * 0.375f + p[10].x * 0.375f + p[11].x * 0.125f;
					y1 = p[4].y * 0.125f + p[5].y * 0.375f + p[6].y * 0.375f+ p[7].y * 0.125f;
					y2 = p[8].y * 0.125f + p[9].y * 0.375f + p[10].y * 0.375f + p[11].y * 0.125f;
					z1 = p[4].z * 0.125f + p[5].z * 0.375f + p[6].z * 0.375f+ p[7].z * 0.125f;
					z2 = p[8].z * 0.125f + p[9].z * 0.375f + p[10].z * 0.375f + p[11].z * 0.125f;
					
					ap3[4].x = x0 * 0.125f + x1 * 0.375f + x2 * 0.375f + x3 * 0.125f;
					ap3[4].y = y0 * 0.125f + y1 * 0.375f + y2 * 0.375f + y3 * 0.125f;
					ap3[4].z = z0 * 0.125f + z1 * 0.375f + z2 * 0.375f + z3 * 0.125f;
					break;
				default:
					for (s = 0; s < iPatchSubdivisions; s++) {
						index = iPatchSubdivisions * s;
						for (t = 0; t < iPatchSubdivisions; t++) {
							x0 = p[0].x * B0[t] + p[1].x * B1[t] + p[2].x * B2[t] + p[3].x * B3[t];
							x1 = p[4].x * B0[t] + p[5].x * B1[t] + p[6].x * B2[t] + p[7].x * B3[t];
							x2 = p[8].x * B0[t] + p[9].x * B1[t] + p[10].x * B2[t] + p[11].x * B3[t];
							x3 = p[12].x * B0[t] + p[13].x * B1[t] + p[14].x * B2[t] + p[15].x * B3[t];
							y0 = p[0].y * B0[t] + p[1].y * B1[t] + p[2].y * B2[t] + p[3].y * B3[t];
							y1 = p[4].y * B0[t] + p[5].y * B1[t] + p[6].y * B2[t] + p[7].y * B3[t];
							y2 = p[8].y * B0[t] + p[9].y * B1[t] + p[10].y * B2[t] + p[11].y * B3[t];
							y3 = p[12].y * B0[t] + p[13].y * B1[t] + p[14].y * B2[t] + p[15].y * B3[t];
							z0 = p[0].z * B0[t] + p[1].z * B1[t] + p[2].z * B2[t] + p[3].z * B3[t];
							z1 = p[4].z * B0[t] + p[5].z * B1[t] + p[6].z * B2[t] + p[7].z * B3[t];
							z2 = p[8].z * B0[t] + p[9].z * B1[t] + p[10].z * B2[t] + p[11].z * B3[t];
							z3 = p[12].z * B0[t] + p[13].z * B1[t] + p[14].z * B2[t] + p[15].z * B3[t];
							ap3[index].x = x0 * B0[s] + x1 * B1[s] + x2 * B2[s] + x3 * B3[s];
							ap3[index].y = y0 * B0[s] + y1 * B1[s] + y2 * B2[s] + y3 * B3[s];
							ap3[index].z = z0 * B0[s] + z1 * B1[s] + z2 * B2[s] + z3 * B3[s];
							index++;
						}
					}
					break;
			}
			
			
			if (mp.isOpaque()) {
				draw3DTriangleGridFlat(ap3, mp.getRGB(), iPatchSubdivisions, iPatchSubdivisions);
			} else {
				int transparency = (int) (Math.min(1f,mp.transmit + mp.filter) * 255f);
				draw3DTriangleGridFlatTransparent(ap3, mp.getRGB(), iPatchSubdivisions, iPatchSubdivisions, transparency);
			}
			//draw3DTriangleGridFlatTransparent(ap3, mp.getRGB(), iPatchSubdivisions, iPatchSubdivisions, 192);
			//draw3DTriangleGridFlat(ap3, mp.getRGB(), iPatchSubdivisions, iPatchSubdivisions);
		}
	}
	
/*
	protected final void draw3DTriangleGridOutline(Point3f[] points, int color, int xSize, int ySize) {
		int offset = xSize * (ySize - 1);
		for (int x = 0; x < xSize - 1; x++) {
			draw3DLine(points[x], points[x + 1], color);
			draw3DLine(points[x + offset], points[x + offset + 1], color);
		}
		offset = xSize - 1;
		for (int y = 0; y < ySize - 1; y++) {
			draw3DLine(points[y * xSize], points[(y + 1) * xSize], color);
			draw3DLine(points[y * xSize + offset], points[(y + 1) * xSize + offset], color);
		}
	}
*/

	//protected final void draw3DTriangleGridPhong(Point3f[] points, Vector3f[] normals, MaterialProperties mp, int xSize, int ySize) {
	//	int index1;
	//	int index2;
	//	int index3;
	//	int index4;
	//	for (int y = 0; y < ySize - 1; y++) {
	//		for (int x = 0; x < xSize -1; x++) {
	//			index1 = y * xSize + x;
	//			index2 = (y + 1) * xSize + x;
	//			index3 = (y + 1) * xSize + x + 1;
	//			index4 = y * xSize + x + 1;
	//			draw3DTrianglePhong(points[index1], points[index2], points[index3], normals[index1], normals[index2], normals[index3], mp);
	//			draw3DTrianglePhong(points[index3], points[index4], points[index1], normals[index3], normals[index4], normals[index1], mp);
	//		}
	//	}
	//}
	
	protected final void draw3DTriangleGridGourad(Point3f[] points, int[] colors, int xSize, int ySize) {
		int index1;
		int index2;
		int index3;
		int index4;
		for (int y = 0; y < ySize - 1; y++) {
			for (int x = 0; x < xSize -1; x++) {
				index1 = y * xSize + x;
				index2 = (y + 1) * xSize + x;
				index3 = (y + 1) * xSize + x + 1;
				index4 = y * xSize + x + 1;
				draw3DTriangleGourad(points[index1], points[index2], points[index3], colors[index1], colors[index2], colors[index3]);
				draw3DTriangleGourad(points[index3], points[index4], points[index1], colors[index3], colors[index4], colors[index1]);
			}
		}
	}
	
	protected final void draw3DTriangleGridGouradTransparent(Point3f[] points, int[] colors, int xSize, int ySize, int transparency) {
		int index1;
		int index2;
		int index3;
		int index4;
		for (int y = 0; y < ySize - 1; y++) {
			for (int x = 0; x < xSize -1; x++) {
				index1 = y * xSize + x;
				index2 = (y + 1) * xSize + x;
				index3 = (y + 1) * xSize + x + 1;
				index4 = y * xSize + x + 1;
				draw3DTriangleGouradTransparent(points[index1], points[index2], points[index3], colors[index1], colors[index2], colors[index3], transparency);
				draw3DTriangleGouradTransparent(points[index3], points[index4], points[index1], colors[index3], colors[index4], colors[index1], transparency);
			}
		}
	}
	
	protected final void draw3DTriangleGridFlat(Point3f[] points, int color, int xSize, int ySize) {
		int index1;
		int index2;
		int index3;
		int index4;
		for (int y = 0; y < ySize - 1; y++) {
			for (int x = 0; x < xSize -1; x++) {
				index1 = y * xSize + x;
				index2 = (y + 1) * xSize + x;
				index3 = (y + 1) * xSize + x + 1;
				index4 = y * xSize + x + 1;
				draw3DTriangleFlat(points[index1], points[index2], points[index3], color);
				draw3DTriangleFlat(points[index3], points[index4], points[index1], color);
			}
		}
	}
	
	protected final void draw3DTriangleGridFlatTransparent(Point3f[] points, int color, int xSize, int ySize, int transparency) {
		int index1;
		int index2;
		int index3;
		int index4;
		for (int y = 0; y < ySize - 1; y++) {
			for (int x = 0; x < xSize -1; x++) {
				index1 = y * xSize + x;
				index2 = (y + 1) * xSize + x;
				index3 = (y + 1) * xSize + x + 1;
				index4 = y * xSize + x + 1;
				draw3DTriangleFlatTransparent(points[index1], points[index2], points[index3], color, transparency);
				draw3DTriangleFlatTransparent(points[index3], points[index4], points[index1], color, transparency);
			}
		}
	}
/*
	protected final void draw3DCubicCurve(Point3f p0, Point3f p1, Point3f p2, Point3f p3, int color) {
		int x0 = (int)((p0.x) * 65536);
		int x1 = (int)((p1.x) * 65536);
		int x2 = (int)((p2.x) * 65536);
		int x3 = (int)((p3.x) * 65536);
		int y0 = (int)((p0.y) * 65536);
		int y1 = (int)((p1.y) * 65536);
		int y2 = (int)((p2.y) * 65536);
		int y3 = (int)((p3.y) * 65536);
		int z0 = (int)((p0.z) * 65536);
		int z1 = (int)((p1.z) * 65536);
		int z2 = (int)((p2.z) * 65536);
		int z3 = (int)((p3.z) * 65536);
		recursiveDeCasteljau(x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, color);
	}

	private final void recursiveDeCasteljau(int x0, int y0, int z0, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3, int color) {
		int h = iHeight * 65536;
		int w = iWidth * 65536;
		if ((x0 < 0 && x1 < 0 && x2 < 0 && x3 < 0) || (y0 < 0 && y1 < 0 && y2 < 0 && y3 < 0) || (x0 >= w && x1 >= w && x2 >= w && x3 >= w) || (y0 >= h && y1 >= h && y2 >= h && y3 >= h)) {
			return;
		}
		int dx = Math.abs((x3 - x0) >> 16);
		int dy = Math.abs((y3 - y0) >> 16);
		int dz = Math.abs((z3 - z0) >> 16);
		//System.out.println(dx + " " + dy + " "
		if ((dx * dx + dy * dy + dz * dz) < 400) {
		//if (Math.abs(x3 - x0) + Math.abs(y3 - y0) + Math.abs(z3 - z0) < 655360) {
			draw3DLine(x0 >> 16, y0 >> 16, z0, x3 >> 16, y3 >> 16, z3, color);
		} else {
			int ax = (x0 + x1) / 2;
			int ay = (y0 + y1) / 2;
			int az = (z0 + z1) / 2;
			int bx = (x1 + x2) / 2;
			int by = (y1 + y2) / 2;
			int bz = (z1 + z2) / 2;
			int cx = (x2 + x3) / 2;
			int cy = (y2 + y3) / 2;
			int cz = (z2 + z3) / 2;
			dx = (ax + bx) / 2;
			dy = (ay + by) / 2;
			dz = (az + bz) / 2;
			int ex = (bx + cx) / 2;
			int ey = (by + cy) / 2;
			int ez = (bz + cz) / 2;
			int fx = (dx + ex) / 2;
			int fy = (dy + ey) / 2;
			int fz = (dz + ez) / 2;
			recursiveDeCasteljau(x0, y0, z0, ax, ay, az, dx, dy, dz, fx, fy, fz, color);
			recursiveDeCasteljau(fx, fy, fz, ex, ey, ez, cx, cy, cz, x3, y3, z3, color);
		}
	}
*/
	protected final void flipBackfaceNormals(Vector3f[] normals) {
		for (int i = 0; i < normals.length; i++) {
			if (normals[i].z > 0) {
				normals[i].x = -normals[i].x;
				normals[i].y = -normals[i].y;
				normals[i].z = -normals[i].z;
			}
		}
	}

	protected final void shade(Point3f[] points, Vector3f[] normals, MaterialProperties mp, int[] colors) {
		int n = points.length;
		for (int i = 0; i < n; i++) {
			colors[i] = shade(points[i], normals[i], mp);
		}
	}
	
	public final int shade(Point3f point, Vector3f normal, MaterialProperties mp) {
		/*
		float diffuse = mp.ambient * fAmbientLight;
		float specular = 0;

		Vector3f LV = new Vector3f();
		for (int l = 0; l < av3LightDirection.length; l++) {
			if (mp.diffuse != 0) {
				float diff = Math.max(av3LightDirection[l].dot(normal) * afLightIntensity[l], 0) ;
				diffuse += (float)Math.pow(diff,mp.brilliance) * mp.diffuse;
			}
			if (abLightSpecular[l] && mp.specular != 0) {
				LV.sub(av3LightDirection[l],v3View);
				LV.scale(1f/(float)Math.sqrt(LV.dot(LV)));
				LV.normalize();
				float spec = LV.dot(normal);
				specular += Math.max((float)Math.pow(spec,1f/mp.roughness) * mp.specular,0);
			}
		}

		float red = Math.min(mp.red * diffuse + specular, 1) * 255;
		float green = Math.min(mp.green * diffuse + specular, 1) * 255;
		float blue = Math.min(mp.blue * diffuse + specular, 1) * 255;

		int color = 0xFF000000 + (((int)red) << 16) + (((int)green) << 8) + ((int)blue);
		return color;
		*/
		return lighting.shade(point, normal, mp);
	}

	public final void drawPoint3D(Point3f p, int size) {
		int index;
		int x = (int)p.x;
		int y = (int)p.y;
		int z = (int)(p.z * 65536);
		int x1 = x - size;
		int y1 = y - size;
		int x2 = x + size;
		int y2 = y + size;
		if ((x1 < 0 && x2 < 0 )|| (y1 < 0 && y2 < 0) || (x1 >= iWidth && x2 >= iWidth) || (y1 >= iHeight && y2 >= iHeight)) {
			return;
		}

		if (x1 < 0) x1 = 0;
		if (y1 < 0) y1 = 0;
		if (x2 >= iWidth) x2 = iWidth - 1;
		if (y2 >= iHeight) y2 = iHeight - 1;
		for (y = y1; y <= y2; y++) {
			for (x = x1; x <= x2; x++) {
				index = iWidth * y + x;
				if (z < aiActiveZBuffer[index]) {
					aiActiveFrameBuffer[index] = color;
					aiActiveZBuffer[index] = z;
				}
			}
		}
	}
	
	public void drawXPoint3D(Point3f p) {
		int x = (int)p.x;
		int y = (int)p.y;
		int z = (int)(p.z * 65536);
		setPixel3D(x, y - 2, z);
		setPixel3D(x - 1, y - 1, z);
		setPixel3D(x, y - 1, z);
		setPixel3D(x + 1, y - 1, z);
		setPixel3D(x - 2, y, z);
		setPixel3D(x - 1, y, z);
		setPixel3D(x, y, z);
		setPixel3D(x + 1, y, z);
		setPixel3D(x + 2, y, z);
		setPixel3D(x - 1, y + 1, z);
		setPixel3D(x, y + 1, z);
		setPixel3D(x + 1, y + 1, z);
		setPixel3D(x, y + 2, z);
	}
	
	public void setPixel3D(int x, int y, int z) {
		if (x < 0 || x >= iWidth || y < 0 || y >= iHeight) return;
		int index = iWidth * y + x;
		if (z < aiActiveZBuffer[index]) {
			aiActiveFrameBuffer[index] = color;
			aiActiveZBuffer[index] = z;
		}
	}
	
	protected final void drawLineStrip3D(Point3f[] points) {
		for (int i = 0; i < points.length - 1;) {
			drawLine3D(points[i], points[++i]);
		}
	}
	/*
	public final void drawLine3D(Point3f p1, Point3f p2) {
		int x1 = (int)p1.x;
		int y1 = (int)p1.y;
		int x2 = (int)p2.x;
		int y2 = (int)p2.y;
		int z1 = (int)((p1.z) * 65536);
		int z2 = (int)((p2.z) * 65536);
		drawLine3D(x1, y1, z1, x2, y2, z2);
	}
	*/
	public final void drawLine3D(Point3f p1, Point3f p2) {
		int x1 = (int)p1.x;
		int y1 = (int)p1.y;
		int x2 = (int)p2.x;
		int y2 = (int)p2.y;
		int z1 = (int)((p1.z) * 65536);
		int z2 = (int)((p2.z) * 65536);
		drawLine3D(x1, y1, z1, x2, y2, z2);
	}
	
	//public void drawLine3D(Point3f p0, Point3f p1) {
	//	System.out.println("drawLine3D(" + p0 + "," + p1 + ")");
	//	int x0, y0, z0, x1, y1, z1;
	//	x0 = (int) (p0.x * 4096);
	//	y0 = (int) (p0.y * 4096);
	//	z0 = (int) ((p0.z + fPolygonOffset) * 4096);
	//	x1 = (int) (p1.x * 4096);
	//	y1 = (int) (p1.y * 4096);
	//	z1 = (int) ((p1.z + fPolygonOffset) * 4096);
	//	
	//	//System.out.println("p0 = " + x0 + " " + y0 + " " + z0);
	//	//System.out.println("p1 = " + x1 + " " + y1 + " " + z1);
	//	
	//	int dx = x1 - x0;
	//	int dy = y1 - y0;
	//	int dz = z1 - z0;
	//	
	//	//System.out.println("d = " + dx + " " + dy + " " + dz);
	//	
	//	int dxdy = (int) ((((long) dx) << 12) / dy);
	//	
	//	System.out.println("dxdy = " + dxdy);
	//	
	//	int y0i = (y0 + 0xfff) & 0xfffff000;
	//	int y1i = (y1 + 0xfff) & 0xfffff000;
	//	
	//	//System.out.println("y0i,y1i = " + y0i + "," + y1i);
	//	int yf = y0i - y0;
	//	int xp = (dxdy * yf) >> 12;
	//	
	//	System.out.println("yf, xp = " + yf + "," + xp);
	//	y0i >>= 12;
	//	y1i >>= 12;
	//	
	//	System.out.println(y0i + " " + y1i);
	//	int index = y0i * iWidth;
	//	int x = x0 + xp;
	//	for (int y = y0i; y < y1i; y++) {
	//		//System.out.println(y);
	//		int ix = (index + (x >> 12));
	//		for (int i = index; i < ix; i++) {
	//			aiActiveFrameBuffer[i] = color;
	//		}
	//		x += dxdy;
	//		index += iWidth;
	//	}
	//}
		
		
	public final void drawGhostLine3D(Point3f p1, Point3f p2, int ghost) {
		int x1 = (int)p1.x;
		int y1 = (int)p1.y;
		int x2 = (int)p2.x;
		int y2 = (int)p2.y;
		int z1 = (int)((p1.z) * 65536);
		int z2 = (int)((p2.z) * 65536);
		drawGhostLine3D(x1, y1, z1, x2, y2, z2, ghost);
	}
/*
//
// fast Bresenham line drwaing algorithm
//
private final void drawLine3D(int x0, int y0, int z0, int x1, int y1, int z1) {
	//
	// drop if out of bounds
	//
	if ((x0 < 0 && x1 < 0 )|| (y0 < 0 && y1 < 0) || (x0 >= iWidth && x1 >= iWidth) || (y0 >= iHeight && y1 >= iHeight)) {
		return;
	}
	z0 = z0 << 16;
	z1 = z1 << 16;
	//
	// clip to screen
	//
	
	int dx = x1 - x0;
	int dy = y1 - y0;
	int dz;
	z0 = z0 << 16;
	z1 = z1 << 16;
	
	
	if (x0 < 0) {
		y0 -= x0 * dy / dx;
		x0 = 0;
	} else if (x1 < 0) {
		y1 -= x1 * dy / dx;
		x1 = 0;
	}
	if (x0 >= iWidth) {
		y0 += (iWidth - 1 - x0) * dy / dx;
		x0 = iWidth - 1;
	} else if (x1 >= iWidth) {
		y1 += (iWidth - 1 - x1) * dy / dx;
		x1 = iWidth - 1;
	}
	if (y0 < 0) {
		x0 -= y0 * dx / dy;
		y0 = 0;
	} else if (y1 < 0) {
		x1 -= y1 * dx / dy;
		y1 = 0;
	}
	if (y0 >= iHeight) {
		x0 += (iHeight - 1 - y0) * dx / dy;
		y0 = iHeight -1;
	} else if (y1 >= iHeight) {
		x1 += (iHeight - 1 - y1) * dx / dy;
		y1 = iHeight - 1;
	}
	if (x0 < 0 || x1 < 0 || y0 < 0 || y1 < 0 || x0 >= iWidth || x1 >= iWidth || y0 >= iWidth || y1 >= iWidth) {
		return;
	}
	
	//
	// draw line
	//
	int dx = x1 - x0;
	int dy = y1 - y0;
	int dz = z1 - z0;
	
	int fbXincr;
	int fbYincr;
	int fbXYincr;
	int dPr;
	int P;
	int dPru;
	if (dx < 0) {
		dx = -dx;
		fbXincr = -1;
	} else {
		fbXincr = 1;
	}
	if (dy < 0) {
		dy = -dy;
		fbYincr = -iWidth;
	} else {
		fbYincr = iWidth;
	}
	fbXYincr = fbXincr + fbYincr;
	int fbA = y0 * iWidth + x0;
	int fbB = y1 * iWidth + x1;
	int zA = z0;
	int zB = z1;
	if (dy < dx) {
		dPr = dy + dy;
		P = -dx;
		dPru = P + P;
		dy = dx >> 1;
		while(dy >= 0) {
			if (zA <= aiActiveZBuffer[fbA]) { 
				aiActiveFrameBuffer[fbA] = color;
				aiActiveZBuffer[fbA] = zA;
			}
			if (zB <= aiActiveZBuffer[fbB]) {
				aiActiveFrameBuffer[fbB] = color;
				aiActiveZBuffer[fbB] = zB;
			}
			if ((P += dPr) < 0) {
				fbA += fbXincr;
				fbB -= fbXincr;
				dy--;
			} else {
				fbA += fbXYincr;
				fbB -= fbXYincr;
				P += dPru;
				dy--;
			}
			zA += dz;
			zB -= dz;
		}
		if (zA <= aiActiveZBuffer[fbA]) {
			aiActiveFrameBuffer[fbA] = color;
			aiActiveZBuffer[fbA] = zA;
		}
		if ((dx & 1) != 0) {
			if (zB <= aiActiveZBuffer[fbB]) {
				aiActiveFrameBuffer[fbB] = color;
				aiActiveZBuffer[fbB] = zB;
			}
		}
	} else {
		dPr = dx + dx;
		P = -dy;
		dPru = P + P;
		dx = dy >> 1;
		while(dx > 0) {
			if (zA <= aiActiveZBuffer[fbA]) { 
				aiActiveFrameBuffer[fbA] = color;
				aiActiveZBuffer[fbA] = zA;
			}
			if (zB <= aiActiveZBuffer[fbB]) {
				aiActiveFrameBuffer[fbB] = color;
				aiActiveZBuffer[fbB] = zB;
			}
			if ((P += dPr) < 0) {
				fbA += fbYincr;
				fbB -= fbYincr;
				dx--;
			} else {
				fbA += fbXYincr;
				fbB -= fbXYincr;
				P += dPru;
				dx--;
			}
		}
		if (zA <= aiActiveZBuffer[fbA]) {
			aiActiveFrameBuffer[fbA] = color;
			aiActiveZBuffer[fbA] = zA;
		}
		if ((dy & 1) != 0) {
			if (zB <= aiActiveZBuffer[fbB]) {
				aiActiveFrameBuffer[fbB] = color;
				aiActiveZBuffer[fbB] = zB;
			}
		}
	}
}

*/
	public final void drawLine3D(int x1, int y1, int z1, int x2, int y2, int z2) {
		int x;
		int y;
		int z;
		int end;
		int edge;
		int index;
		if ((x1 < 0 && x2 < 0 )|| (y1 < 0 && y2 < 0) || (x1 >= iWidth && x2 >= iWidth) || (y1 >= iHeight && y2 >= iHeight)) {
			return;
		}
		
		int dx = x2 - x1;
		int dy = y2 - y1;
		int dz = z2 - z1;
		if (dx == 0 && dy == 0) {
			return;
		}
		if (Math.abs(dx) > Math.abs(dy)) {
			if (dx > 0) {
				x = x1;
				y = y1 << 16 + 32768;
				z = z1;
				dy = (dy << 16) / dx;
				dz = dz / dx;
				end = (x2 < iWidth) ? x2 : iWidth;
			} else {
				x = x2;
				y = y2 << 16 + 32768;
				z = z2;
				dy = (dy << 16) / dx;
				dz = dz / dx;
				end = (x1 < iWidth) ? x1 : iWidth;
			}
			if (x < 0) {
				y -= dy * x;
				z -= dz * x;
				x = 0;
			}
			edge = iHeight<<16;
			while (x < end) {
				if (y >= 0 && y < edge) {
					index = iWidth * (y >> 16) + x;
					if (z <= aiActiveZBuffer[index]) {
						aiActiveFrameBuffer[index] = color;
						aiActiveZBuffer[index] = z;
					}
				}
				x++;
				y += dy;
				z += dz;
			}
		} else {
			if (dy > 0) {
				y = y1;
				x = x1 << 16 + 32768;
				z = z1;
				dx = (dx << 16) / dy;
				dz = dz / dy;
				end = (y2 < iHeight) ? y2 : iHeight;
			} else {
				y = y2;
				x = x2 << 16 + 32768;
				z = z2;
				dx = (dx << 16) / dy;
				dz = dz / dy;
				end = (y1 < iHeight) ? y1 : iHeight;
			}
			if (y < 0) {
				x -= dx * y;
				z -= dz * y;
				y = 0;
			}
			edge = iWidth<<16;
			while (y < end) {
				if (x >= 0 && x < edge) {
					index = iWidth * y + (x >> 16);
					if (z < aiActiveZBuffer[index]) {
						aiActiveFrameBuffer[index] = color;
						aiActiveZBuffer[index] = z;
					}
				}
				y++;
				x += dx;
				z += dz;
			}
		}
	}

	public final void drawGhostLine3D(int x1, int y1, int z1, int x2, int y2, int z2, int ghost) {
		int x;
		int y;
		int z;
		int end;
		int edge;
		int index;
		int ghostColor = ((((color & 0xFF0000) * (255 - ghost)) >> 8) & 0xFF0000) |
				 ((((color & 0xFF00) * (255 - ghost)) >> 8) & 0xFF00) |
				 ((((color & 0xFF) * (255 - ghost)) >> 8) & 0xFF);
					
		if ((x1 < 0 && x2 < 0 )|| (y1 < 0 && y2 < 0) || (x1 >= iWidth && x2 >= iWidth) || (y1 >= iHeight && y2 >= iHeight)) {
			return;
		}
		
		int dx = x2 - x1;
		int dy = y2 - y1;
		int dz = z2 - z1;
		if (dx == 0 && dy == 0) {
			return;
		}
		if (Math.abs(dx) > Math.abs(dy)) {
			if (dx > 0) {
				x = x1;
				y = y1 << 16 + 32768;
				z = z1;
				dy = (dy << 16) / dx;
				dz = dz / dx;
				end = (x2 < iWidth) ? x2 : iWidth;
			} else {
				x = x2;
				y = y2 << 16 + 32768;
				z = z2;
				dy = (dy << 16) / dx;
				dz = dz / dx;
				end = (x1 < iWidth) ? x1 : iWidth;
			}
			if (x < 0) {
				y -= dy * x;
				z -= dz * x;
				x = 0;
			}
			edge = iHeight<<16;
			while (x < end) {
				if (y >= 0 && y < edge) {
					index = iWidth * (y >> 16) + x;
					if (z <= aiActiveZBuffer[index]) {
						aiActiveFrameBuffer[index] = color;
						aiActiveZBuffer[index] = z;
					} else {
						int backColor = aiActiveFrameBuffer[index];
						aiActiveFrameBuffer[index] = (((((backColor & 0xFF0000) * ghost) >> 8) & 0xFF0000) |
									     ((((backColor & 0xFF00) * ghost) >> 8) & 0xFF00) |
									     ((((backColor & 0xFF) * ghost) >> 8) & 0xFF)) + ghostColor;
					}
				}
				x++;
				y += dy;
				z += dz;
			}
		} else {
			if (dy > 0) {
				y = y1;
				x = x1 << 16 + 32768;
				z = z1;
				dx = (dx << 16) / dy;
				dz = dz / dy;
				end = (y2 < iHeight) ? y2 : iHeight;
			} else {
				y = y2;
				x = x2 << 16 + 32768;
				z = z2;
				dx = (dx << 16) / dy;
				dz = dz / dy;
				end = (y1 < iHeight) ? y1 : iHeight;
			}
			if (y < 0) {
				x -= dx * y;
				z -= dz * y;
				y = 0;
			}
			edge = iWidth<<16;
			while (y < end) {
				if (x >= 0 && x < edge) {
					index = iWidth * y + (x >> 16);
					if (z < aiActiveZBuffer[index]) {
						aiActiveFrameBuffer[index] = color;
						aiActiveZBuffer[index] = z;
					} else {
						int backColor = aiActiveFrameBuffer[index];
						//aiActiveFrameBuffer[index] = ((((backColor & 0xFF0000) >> 1) & 0xFF0000) |
						//				 (((backColor & 0xFF00) >> 1) & 0xFF00) |
						//				 (((backColor & 0xFF) >> 1) & 0xFF)) + ghostColor;
						aiActiveFrameBuffer[index] = (((((backColor & 0xFF0000) * ghost) >> 8) & 0xFF0000) |
									     ((((backColor & 0xFF00) * ghost) >> 8) & 0xFF00) |
									     ((((backColor & 0xFF) * ghost) >> 8) & 0xFF)) + ghostColor;
					
					}
				}
				y++;
				x += dx;
				z += dz;
			}
		}
	}
	
	public final void drawRect(int x1, int y1, int x2, int y2) {
		drawLine(x1,y1,x2,y1);
		drawLine(x2,y1,x2,y2);
		drawLine(x2,y2,x1,y2);
		drawLine(x1,y2,x1,y1);
	}

	public final void drawLine(int x1, int y1, int x2, int y2) {
		int x;
		int y;
		int end;
		int edge;
		int index;
		if ((x1 < 0 && x2 < 0 )|| (y1 < 0 && y2 < 0) || (x1 >= iWidth && x2 >= iWidth) || (y1 >= iHeight && y2 >= iHeight)) {
			return;
		}
		int dx = x2 - x1;
		int dy = y2 - y1;
		if (dx == 0 && dy == 0) {
			return;
		}
		if (Math.abs(dx) > Math.abs(dy)) {
			if (dx > 0) {
				x = x1;
				y = y1 << 16 + 32768;
				dy = (dy << 16) / dx;
				end = (x2 < iWidth) ? x2 : iWidth;
			} else {
				x = x2;
				y = y2 << 16 + 32768;
				dy = (dy << 16) / dx;
				end = (x1 < iWidth) ? x1 : iWidth;
			}
			if (x < 0) {
				y -= dy * x;
				x = 0;
			}
			edge = iHeight<<16;
			while (x < end) {
				if (y >= 0 && y < edge) {
					index = iWidth * (y >> 16) + x;
					aiActiveFrameBuffer[index] = color;
				}
				x++;
				y += dy;
			}
		} else {
			if (dy > 0) {
				y = y1;
				x = x1 << 16 + 32768;
				dx = (dx << 16) / dy;
				end = (y2 < iHeight) ? y2 : iHeight;
			} else {
				y = y2;
				x = x2 << 16 + 32768;
				dx = (dx << 16) / dy;
				end = (y1 < iHeight) ? y1 : iHeight;
			}
			if (y < 0) {
				x -= dx * y;
				y = 0;
			}
			edge = iWidth<<16;
			while (y < end) {
				if (x >= 0 && x < edge) {
					index = iWidth * y + (x >> 16);
					aiActiveFrameBuffer[index] = color;
				}
				y++;
				x += dx;
			}
		}
	}

	protected final void xorLine(int x1, int y1, int x2, int y2, int color) {
		int x;
		int y;
		int end;
		int edge;
		int index;
		if ((x1 < 0 && x2 < 0 )|| (y1 < 0 && y2 < 0) || (x1 >= iWidth && x2 >= iWidth) || (y1 >= iHeight && y2 >= iHeight)) {
			return;
		}
		int dx = x2 - x1;
		int dy = y2 - y1;
		if (dx == 0 && dy == 0) {
			return;
		}
		if (Math.abs(dx) > Math.abs(dy)) {
			if (dx > 0) {
				x = x1;
				y = y1 << 16 + 32768;
				dy = (dy << 16) / dx;
				end = (x2 < iWidth) ? x2 : iWidth;
			} else {
				x = x2;
				y = y2 << 16 + 32768;
				dy = (dy << 16) / dx;
				end = (x1 < iWidth) ? x1 : iWidth;
			}
			if (x < 0) {
				y -= dy * x;
				x = 0;
			}
			edge = iHeight<<16;
			while (x < end) {
				if (y >= 0 && y < edge) {
					index = iWidth * (y >> 16) + x;
					aiActiveFrameBuffer[index] ^= color;
				}
				x++;
				y += dy;
			}
		} else {
			if (dy > 0) {
				y = y1;
				x = x1 << 16 + 32768;
				dx = (dx << 16) / dy;
				end = (y2 < iHeight) ? y2 : iHeight;
			} else {
				y = y2;
				x = x2 << 16 + 32768;
				dx = (dx << 16) / dy;
				end = (y1 < iHeight) ? y1 : iHeight;
			}
			if (y < 0) {
				x -= dx * y;
				y = 0;
			}
			edge = iWidth<<16;
			while (y < end) {
				if (x >= 0 && x < edge) {
					index = iWidth * y + (x >> 16);
					aiActiveFrameBuffer[index] ^= color;
				}
				y++;
				x += dx;
			}
		}
	}

	protected final void drawHLine(int y, int color) {
		if (y < 0 || y >= iHeight) {
			return;
		}
		int index = y * iHeight;
		for (int i = 0; i < iHeight; i++, index++) {
			aiActiveFrameBuffer[index] = color;
		}
	}

	protected final void drawVLine(int x, int color) {
		if (x < 0 || x >= iWidth) {
			return;
		}
		int index = x;
		for (int i = 0; i < iWidth; i++, index += iWidth) {
			aiActiveFrameBuffer[index] = color;
		}
	}

	protected final void setPixel(int x, int y, int color) {
		aiActiveFrameBuffer[x + y * iWidth] = color;
	}
	
	
	private final void draw3DTriangleGourad(int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3, int color1, int color2, int color3) {
		
		int[] frameBuffer = aiActiveFrameBuffer;
		int[] zBuffer = aiActiveZBuffer;
		
		int r1 = (color1 & 0x00ff0000);
		int g1 = (color1 & 0x0000ff00) << 8;
		int b1 = (color1 & 0x000000ff) << 16;
		int r2 = (color2 & 0x00ff0000);
		int g2 = (color2 & 0x0000ff00) << 8;
		int b2 = (color2 & 0x000000ff) << 16;
		int r3 = (color3 & 0x00ff0000);
		int g3 = (color3 & 0x0000ff00) << 8;
		int b3 = (color3 & 0x000000ff) << 16;
		
		
		int dx12 = x2 - x1;
		int dy12 = y2 - y1;
		int dz12 = z2 - z1;
		int dr12 = r2 - r1;
		int dg12 = g2 - g1;
		int db12 = b2 - b1;
		
		int dx13 = x3 - x1;
		int dy13 = y3 - y1;
		int dz13 = z3 - z1;
		int dr13 = r3 - r1;
		int dg13 = g3 - g1;
		int db13 = b3 - b1;
		
		int dx23 = x3 - x2;
		int dy23 = y3 - y2;
		int dz23 = z3 - z2;
		int dr23 = r3 - r2;
		int dg23 = g3 - g2;
		int db23 = b3 - b2;
		
		int mx12 = 0;
		int mz12 = 0;
		int mr12 = 0;
		int mg12 = 0;
		int mb12 = 0;
		
		int mx13 = 0;
		int mz13 = 0;
		int mr13 = 0;
		int mg13 = 0;
		int mb13 = 0;
		
		int mx23 = 0;
		int mz23 = 0;
		int mr23 = 0;
		int mg23 = 0;
		int mb23 = 0;
		
		if (dy13 <= 0) return;
		
		if (dy12 > 0) {
			mx12 = (int) ((((long) dx12) << SUB_PIXEL_BITS) / dy12);
			mz12 = (int) ((((long) dz12) << SUB_PIXEL_BITS) / dy12);
			mr12 = (int) ((((long) dr12) << SUB_PIXEL_BITS) / dy12);
			mg12 = (int) ((((long) dg12) << SUB_PIXEL_BITS) / dy12);
			mb12 = (int) ((((long) db12) << SUB_PIXEL_BITS) / dy12);
		}
		mx13 = (int) ((((long) dx13) << SUB_PIXEL_BITS) / dy13);
		mz13 = (int) ((((long) dz13) << SUB_PIXEL_BITS) / dy13);
		mr13 = (int) ((((long) dr13) << SUB_PIXEL_BITS) / dy13);
		mg13 = (int) ((((long) dg13) << SUB_PIXEL_BITS) / dy13);
		mb13 = (int) ((((long) db13) << SUB_PIXEL_BITS) / dy13);
		if (dy23 > 0) {
			mx23 = (int) ((((long) dx23) << SUB_PIXEL_BITS) / dy23);
			mz23 = (int) ((((long) dz23) << SUB_PIXEL_BITS) / dy23);
			mr23 = (int) ((((long) dr23) << SUB_PIXEL_BITS) / dy23);
			mg23 = (int) ((((long) dg23) << SUB_PIXEL_BITS) / dy23);
			mb23 = (int) ((((long) db23) << SUB_PIXEL_BITS) / dy23);
		}
		
		int mxl, mxr, xl, xr;
		int mzl, mzr, zl, zr;
		int mrl, mrr, rl, rr;
		int mgl, mgr, gl, gr;
		int mbl, mbr, bl, br;
		
		int yf1 = SUB_PIXEL_MULTIPLIER - (y1 & SUB_PIXEL_MASK_1);
		int yf3 = SUB_PIXEL_MULTIPLIER - (y3 & SUB_PIXEL_MASK_1);
		
		int y1i = y1 >> SUB_PIXEL_BITS;
		int y2i = y2 >> SUB_PIXEL_BITS;
		int y3i = y3 >> SUB_PIXEL_BITS;
		
		int ytop = y1i;
		int ymid = (y2i < 0) ? 0 : (y2i >= iHeight) ? iHeight - 1: y2i;
		int ybottom = y3i;
		
		if (ytop < ymid && true) {
			if (mx12 < mx13) {
				mxl = mx12;
				mzl = mz12;
				mrl = mr12;
				mgl = mg12;
				mbl = mb12;
				
				mxr = mx13;
				mzr = mz13;
				mrr = mr13;
				mgr = mg13;
				mbr = mb13;
			} else {
				mxl = mx13;
				mzl = mz13;
				mrl = mr13;
				mgl = mg13;
				mbl = mb13;
				
				mxr = mx12;
				mzr = mz12;
				mrr = mr12;
				mgr = mg12;
				mbr = mb12;
			}
			xl = x1 + (int) (((long) mxl * yf1) >> SUB_PIXEL_BITS);
			zl = z1 + (int) (((long) mzl * yf1) >> SUB_PIXEL_BITS);
			rl = r1 + (int) (((long) mrl * yf1) >> SUB_PIXEL_BITS);
			gl = g1 + (int) (((long) mgl * yf1) >> SUB_PIXEL_BITS);
			bl = b1 + (int) (((long) mbl * yf1) >> SUB_PIXEL_BITS);
			
			xr = x1 + (int) (((long) mxr * yf1) >> SUB_PIXEL_BITS);
			zr = z1 + (int) (((long) mzr * yf1) >> SUB_PIXEL_BITS);
			rr = r1 + (int) (((long) mrr * yf1) >> SUB_PIXEL_BITS);
			gr = g1 + (int) (((long) mgr * yf1) >> SUB_PIXEL_BITS);
			br = b1 + (int) (((long) mbr * yf1) >> SUB_PIXEL_BITS);
			
			if (ytop < 0) {
				xl -= mxl * ytop;
				zl -= mzl * ytop;
				rl -= mrl * ytop;
				gl -= mgl * ytop;
				bl -= mbl * ytop;
				
				xr -= mxr * ytop;
				zr -= mzr * ytop;
				rr -= mrr * ytop;
				gr -= mgr * ytop;
				br -= mbr * ytop;
				
				ytop = 0;
			}
			for (int y = ytop; y < ymid; y ++) {
				int xil = xl >> SUB_PIXEL_BITS;
				int xir = xr >> SUB_PIXEL_BITS;
				if (xil < xir) {
					int mzx = (int) ((((long) (zr - zl)) << SUB_PIXEL_BITS) / (xr - xl));
					int mrx = (int) ((((long) (rr - rl)) << SUB_PIXEL_BITS) / (xr - xl));
					int mgx = (int) ((((long) (gr - gl)) << SUB_PIXEL_BITS) / (xr - xl));
					int mbx = (int) ((((long) (br - bl)) << SUB_PIXEL_BITS) / (xr - xl));
					
					int xf = SUB_PIXEL_MULTIPLIER - (xl & SUB_PIXEL_MASK_1);
					
					int z = zl + (int) (((long) mzx * xf) >> SUB_PIXEL_BITS);
					int r = rl + (int) (((long) mrx * xf) >> 24);
					int g = gl + (int) (((long) mgx * xf) >> 24);
					int b = bl + (int) (((long) mbx * xf) >> 24);
					
					int xstart = (xil < 0) ? 0 : xil;
					int xend = (xir > iWidth) ? iWidth : xir;
					if (xil < 0) {
						z -= mzx * xil;
						r -= mrx * xil;
						g -= mgx * xil;
						b -= mbx * xil;
					}
					int index = y * iWidth + xstart;
					for (int x = xstart; x < xend; x++) {
						if (z < zBuffer[index]) {
							frameBuffer[index] = 0xff000000 | (r & 0xff0000) | (g & 0xff0000) >> 8 | ((b & 0xff0000) >> 16);
							zBuffer[index] = z;
						}
						z += mzx;
						r += mrx;
						g += mgx;
						b += mbx;
						index++;
					}
				}
				xl += mxl;
				zl += mzl;
				rl += mrl;
				gl += mgl;
				bl += mbl;
				
				xr += mxr;
				zr += mzr;
				rr += mrr;
				gr += mgr;
				br += mbr;
			}
		}
		
		if (ybottom > ymid && true) {
			if (mx13 > mx23) {
				mxl = mx13;
				mzl = mz13;
				mrl = mr13;
				mgl = mg13;
				mbl = mb13;
				
				mxr = mx23;
				mzr = mz23;
				mrr = mr23;
				mgr = mg23;
				mbr = mb23;
			} else {
				mxl = mx23;
				mzl = mz23;
				mrl = mr23;
				mgl = mg23;
				mbl = mb23;
				
				mxr = mx13;
				mzr = mz13;
				mrr = mr13;
				mgr = mg13;
				mbr = mb13;
			}
			
			//System.out.println("mxl,mxr = " + mxl + "," + mxr);
			
			xl = x3 + (int) (((long) mxl * yf3) >> SUB_PIXEL_BITS);
			zl = z3 + (int) (((long) mzl * yf3) >> SUB_PIXEL_BITS);
			rl = r3 + (int) (((long) mrl * yf3) >> SUB_PIXEL_BITS);
			gl = g3 + (int) (((long) mgl * yf3) >> SUB_PIXEL_BITS);
			bl = b3 + (int) (((long) mbl * yf3) >> SUB_PIXEL_BITS);
			
			xr = x3 + (int) (((long) mxr * yf3) >> SUB_PIXEL_BITS);
			zr = z3 + (int) (((long) mzr * yf3) >> SUB_PIXEL_BITS);
			rr = r3 + (int) (((long) mrr * yf3) >> SUB_PIXEL_BITS);
			gr = g3 + (int) (((long) mgr * yf3) >> SUB_PIXEL_BITS);
			br = b3 + (int) (((long) mbr * yf3) >> SUB_PIXEL_BITS);
			
			//System.out.println("x3,xl,xr = " + x3 + "," + xl + "," + xr);
			
			if (ybottom >= iHeight) {
				int yy = ybottom - iHeight + 1;
				xl -= mxl * yy;
				zl -= mzl * yy;
				rl -= mrl * yy;
				gl -= mgl * yy;
				bl -= mbl * yy;
				
				xr -= mxr * yy;
				zr -= mxr * yy;
				rr -= mrr * yy;
				gr -= mgr * yy;
				br -= mbr * yy;
				
				ybottom = iHeight - 1;
			}
			for (int y = ybottom; y >= ymid; y--) {
				int xil = xl >> SUB_PIXEL_BITS;
				int xir = xr >> SUB_PIXEL_BITS;
				if (xil < xir) {
					
					int mzx = (int) ((((long) (zr - zl)) << SUB_PIXEL_BITS) / (xr - xl));
					int mrx = (int) ((((long) (rr - rl)) << SUB_PIXEL_BITS) / (xr - xl));
					int mgx = (int) ((((long) (gr - gl)) << SUB_PIXEL_BITS) / (xr - xl));
					int mbx = (int) ((((long) (br - bl)) << SUB_PIXEL_BITS) / (xr - xl));
					
					int xf = SUB_PIXEL_MULTIPLIER - (xl & SUB_PIXEL_MASK_1);
					
					int z = zl + (int) (((long) mzx * xf) >> SUB_PIXEL_BITS);
					int r = rl + (int) (((long) mrx * xf) >> 24);
					int g = gl + (int) (((long) mgx * xf) >> 24);
					int b = bl + (int) (((long) mbx * xf) >> 24);
					
					int xstart = (xil < 0) ? 0 : xil;
					int xend = (xir > iWidth) ? iWidth : xir;
					if (xil < 0) {
						z -= mzx * xil;
						r -= mrx * xil;
						g -= mgx * xil;
						b -= mbx * xil;
					}
					int index = y * iWidth + xstart;
					for (int x = xstart; x < xend; x++) {
						if (z < zBuffer[index]) {
							//frameBuffer[index] = 0xff000000 | ((r & 0xff00) <<8) | (g & 0xff00) | ((b & 0xff00) >> 8);
							frameBuffer[index] = 0xff000000 | (r & 0xff0000) | (g & 0xff0000) >> 8 | ((b & 0xff0000) >> 16);
							zBuffer[index] = z;
						}
						z += mzx;
						r += mrx;
						g += mgx;
						b += mbx;
						index++;
					}
				}
				xl -= mxl;
				zl -= mzl;
				rl -= mrl;
				gl -= mgl;
				bl -= mbl;
				
				xr -= mxr;
				zr -= mzr;
				rr -= mrr;
				gr -= mgr;
				br -= mbr;
			}
		}
	}
	
	public final void draw3DTriangleFlat(Point3f p1, Point3f p2, Point3f p3, int color) {
		if ((p2.x - p1.x) * (p3.y - p1.y) - (p2.y - p1.y) * (p3.x - p1.x) > 0) {
			if (bCulling) return;
			if (bMark) color ^= 0xffffff;
		}
		int x1, y1, z1, x2, y2, z2, x3, y3, z3;
		x1 = (int) (p1.x * SUB_PIXEL_MULTIPLIER);
		y1 = (int) (p1.y * SUB_PIXEL_MULTIPLIER);
		z1 = (int) ((p1.z + fPolygonOffset) * SUB_PIXEL_MULTIPLIER);
		x2 = (int) (p2.x * SUB_PIXEL_MULTIPLIER);
		y2 = (int) (p2.y * SUB_PIXEL_MULTIPLIER);
		z2 = (int) ((p2.z + fPolygonOffset) * SUB_PIXEL_MULTIPLIER);
		x3 = (int) (p3.x * SUB_PIXEL_MULTIPLIER);
		y3 = (int) (p3.y * SUB_PIXEL_MULTIPLIER);
		z3 = (int) ((p3.z + fPolygonOffset) * SUB_PIXEL_MULTIPLIER);
		if (y1 < y2) {
			if (y2 < y3) draw3DTriangleFlat(x1, y1, z1, x2, y2, z2, x3, y3, z3, color);
			else if (y1 < y3) draw3DTriangleFlat(x1, y1, z1, x3, y3, z3, x2, y2, z2, color);
			else draw3DTriangleFlat(x3, y3, z3, x1, y1, z1, x2, y2, z2, color);
		} else {
			if (y1 < y3) draw3DTriangleFlat(x2, y2, z2, x1, y1, z1, x3, y3, z3, color);
			else if (y2 < y3) draw3DTriangleFlat(x2, y2, z2, x3, y3, z3, x1, y1, z1, color);
			else draw3DTriangleFlat(x3, y3, z3, x2, y2, z2, x1, y1, z1, color);
		}
	}
	
	public final void draw3DTriangleGourad(Point3f p1, Point3f p2, Point3f p3, int color1, int color2, int color3) {
		if ((p2.x - p1.x) * (p3.y - p1.y) - (p2.y - p1.y) * (p3.x - p1.x) > 0) {
			if (settings.iBackfaceMode == 1) return;
			if (settings.iBackfaceMode == 2) {
				draw3DTriangleFlat(p1, p2, p3, settings.iBackfaceColor);
				return;
			}
		}
		int x1, y1, z1, x2, y2, z2, x3, y3, z3;
		x1 = (int) (p1.x * SUB_PIXEL_MULTIPLIER);
		y1 = (int) (p1.y * SUB_PIXEL_MULTIPLIER);
		z1 = (int) ((p1.z + fPolygonOffset) * SUB_PIXEL_MULTIPLIER);
		x2 = (int) (p2.x * SUB_PIXEL_MULTIPLIER);
		y2 = (int) (p2.y * SUB_PIXEL_MULTIPLIER);
		z2 = (int) ((p2.z + fPolygonOffset) * SUB_PIXEL_MULTIPLIER);
		x3 = (int) (p3.x * SUB_PIXEL_MULTIPLIER);
		y3 = (int) (p3.y * SUB_PIXEL_MULTIPLIER);
		z3 = (int) ((p3.z + fPolygonOffset) * SUB_PIXEL_MULTIPLIER);
		if (y1 < y2) {
			if (y2 < y3) draw3DTriangleGourad(x1, y1, z1, x2, y2, z2, x3, y3, z3, color1, color2, color3);
			else if (y1 < y3) draw3DTriangleGourad(x1, y1, z1, x3, y3, z3, x2, y2, z2, color1, color3, color2);
			else draw3DTriangleGourad(x3, y3, z3, x1, y1, z1, x2, y2, z2, color3, color1, color2);
		} else {
			if (y1 < y3) draw3DTriangleGourad(x2, y2, z2, x1, y1, z1, x3, y3, z3, color2, color1, color3);
			else if (y2 < y3) draw3DTriangleGourad(x2, y2, z2, x3, y3, z3, x1, y1, z1, color2, color3, color1);
			else draw3DTriangleGourad(x3, y3, z3, x2, y2, z2, x1, y1, z1, color3, color2, color1);
		}
	}
		
	private final void draw3DTriangleFlat(int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3, int color) {
		//System.out.print("<" + (x1 >> 12) + "/" + (y1 >> 12) + "/" + (z1 >> 12) + ">");
		//System.out.print("<" + (x2 >> 12) + "/" + (y2 >> 12) + "/" + (z2 >> 12) + ">");
		//System.out.print("<" + (x3 >> 12) + "/" + (y3 >> 12) + "/" + (z3 >> 12) + ">");
		//System.out.println();
		//System.out.println("draw3DTriangleFlat");
		//System.out.println("p1 = " + (((float) x1) / SUB_PIXEL_MULTIPLIER)  + "," + (((float) y1) / SUB_PIXEL_MULTIPLIER) + "," + (((float) z1) / SUB_PIXEL_MULTIPLIER));
		//System.out.println("p2 = " + (((float) x2) / SUB_PIXEL_MULTIPLIER)  + "," + (((float) y2) / SUB_PIXEL_MULTIPLIER) + "," + (((float) z2) / SUB_PIXEL_MULTIPLIER));
		//System.out.println("p3 = " + (((float) x3) / SUB_PIXEL_MULTIPLIER)  + "," + (((float) y3) / SUB_PIXEL_MULTIPLIER) + "," + (((float) z3) / SUB_PIXEL_MULTIPLIER));
		//System.out.println("p1 = " + x1 + "," + y1 + "," + z1);
		//System.out.println("p2 = " + x2 + "," + y2 + "," + z2);
		//System.out.println("p3 = " + x3 + "," + y3 + "," + z3);
		
		int[] frameBuffer = aiActiveFrameBuffer;
		int[] zBuffer = aiActiveZBuffer;
		
		int dx12 = x2 - x1;
		int dy12 = y2 - y1;
		int dz12 = z2 - z1;
		int dx13 = x3 - x1;
		int dy13 = y3 - y1;
		int dz13 = z3 - z1;
		int dx23 = x3 - x2;
		int dy23 = y3 - y2;
		int dz23 = z3 - z2;
		
		int mx12 = 0;
		int mz12 = 0;
		int mx13 = 0;
		int mz13 = 0;
		int mx23 = 0;
		int mz23 = 0;
		
		if (dy13 <= 0) return;
		
		if (dy12 > 0) {
			mx12 = (int) ((((long) dx12) << SUB_PIXEL_BITS) / dy12);
			mz12 = (int) ((((long) dz12) << SUB_PIXEL_BITS) / dy12);
		}
		mx13 = (int) ((((long) dx13) << SUB_PIXEL_BITS) / dy13);
		mz13 = (int) ((((long) dz13) << SUB_PIXEL_BITS) / dy13);
		if (dy23 > 0) {
			mx23 = (int) ((((long) dx23) << SUB_PIXEL_BITS) / dy23);
			mz23 = (int) ((((long) dz23) << SUB_PIXEL_BITS) / dy23);
		}
		
		//System.out.println(mx12 + "\t" + mx13 + "\t" + mx23);
		//System.out.println((mx12 >> SUB_PIXEL_BITS) + "\t" + (mx13 >> SUB_PIXEL_BITS) + "\t" + (mx23 >> SUB_PIXEL_BITS));
		//int y1i = (y1 + SUB_PIXEL_MASK_1) & SUB_PIXEL_MASK_2;
		//int y2i = (y2 + SUB_PIXEL_MASK_1) & SUB_PIXEL_MASK_2;
		//int y3i = (y3 + SUB_PIXEL_MASK_1) & SUB_PIXEL_MASK_2;
		
		int mxl, mxr, xl, xr;
		int mzl, mzr, zl, zr;
		
		//int yf1 = y1i - y1;
		//int yf3 = y3i - y3;
		
		int yf1 = SUB_PIXEL_MULTIPLIER - (y1 & SUB_PIXEL_MASK_1);
		int yf3 = SUB_PIXEL_MULTIPLIER - (y3 & SUB_PIXEL_MASK_1);
		
		int y1i = y1 >> SUB_PIXEL_BITS;
		int y2i = y2 >> SUB_PIXEL_BITS;
		int y3i = y3 >> SUB_PIXEL_BITS;
		
		int ytop = y1i;
		int ymid = (y2i < 0) ? 0 : (y2i >= iHeight) ? iHeight - 1: y2i;
		int ybottom = y3i;
		
		if (ytop < ymid && true) {
			if (mx12 < mx13) {
				mxl = mx12;
				mzl = mz12;
				mxr = mx13;
				mzr = mz13;
			} else {
				mxl = mx13;
				mzl = mz13;
				mxr = mx12;
				mzr = mz12;
			}
			xl = x1 + (int) (((long) mxl * yf1) >> SUB_PIXEL_BITS);
			zl = z1 + (int) (((long) mzl * yf1) >> SUB_PIXEL_BITS);
			xr = x1 + (int) (((long) mxr * yf1) >> SUB_PIXEL_BITS);
			zr = z1 + (int) (((long) mzr * yf1) >> SUB_PIXEL_BITS);
			if (ytop < 0) {
				xl -= mxl * ytop;
				zl -= mzl * ytop;
				xr -= mxr * ytop;
				zr -= mzr * ytop;
				ytop = 0;
			}
			for (int y = ytop; y < ymid; y ++) {
				int xil = xl >> SUB_PIXEL_BITS;
				int xir = xr >> SUB_PIXEL_BITS;
				if (xil < xir) {
					int mzx = (int) ((((long) (zr - zl)) << SUB_PIXEL_BITS) / (xr - xl));
				
					int xf = SUB_PIXEL_MULTIPLIER - (xl & SUB_PIXEL_MASK_1);
					
					int z = zl + (int) (((long) mzx * xf) >> SUB_PIXEL_BITS);
					
					int xstart = (xil < 0) ? 0 : xil;
					int xend = (xir > iWidth) ? iWidth : xir;
					if (xil < 0) {
						z -= mzx * xil;
					}
					int index = y * iWidth + xstart;
					for (int x = xstart; x < xend; x++) {
						if (z < zBuffer[index]) {
							frameBuffer[index] = color;
							zBuffer[index] = z;
						}
						z += mzx;
						index++;
					}
				}
				xl += mxl;
				zl += mzl;
				xr += mxr;
				zr += mzr;
			}
		}
		
		if (ybottom > ymid && true) {
			if (mx13 > mx23) {
				mxl = mx13;
				mzl = mz13;
				mxr = mx23;
				mzr = mz23;
			} else {
				mxl = mx23;
				mzl = mz23;
				mxr = mx13;
				mzr = mz13;
			}
			
			//System.out.println("mxl,mxr = " + mxl + "," + mxr);
			
			xl = x3 + (int) (((long) mxl * yf3) >> SUB_PIXEL_BITS);
			zl = z3 + (int) (((long) mzl * yf3) >> SUB_PIXEL_BITS);
			xr = x3 + (int) (((long) mxr * yf3) >> SUB_PIXEL_BITS);
			zr = z3 + (int) (((long) mzr * yf3) >> SUB_PIXEL_BITS);
			
			//System.out.println("x3,xl,xr = " + x3 + "," + xl + "," + xr);
			
			if (ybottom >= iHeight) {
				int yy = ybottom - iHeight + 1;
				xl -= mxl * yy;
				zl -= mzl * yy;
				xr -= mxr * yy;
				zr -= mzr * yy;
				ybottom = iHeight - 1;
			}
			for (int y = ybottom; y >= ymid; y--) {
				int xil = xl >> SUB_PIXEL_BITS;
				int xir = xr >> SUB_PIXEL_BITS;
				if (xil < xir) {
					
					int mzx = (int) ((((long) (zr - zl)) << SUB_PIXEL_BITS) / (xr - xl));
					int xf = SUB_PIXEL_MULTIPLIER - (xl & SUB_PIXEL_MASK_1);
					
					int z = zl + (int) (((long) mzx * xf) >> SUB_PIXEL_BITS);
				
					int xstart = (xil < 0) ? 0 : xil;
					int xend = (xir > iWidth) ? iWidth : xir;
					if (xil < 0) {
						z -= mzx * xil;
					}
					int index = y * iWidth + xstart;
					for (int x = xstart; x < xend; x++) {
						if (z < zBuffer[index]) {
							//frameBuffer[index] = 0xff000000 | ((r & 0xff00) <<8) | (g & 0xff00) | ((b & 0xff00) >> 8);
							frameBuffer[index] = color;
							zBuffer[index] = z;
						}
						z += mzx;
						index++;
					}
				}
				xl -= mxl;
				zl -= mzl;
				xr -= mxr;
				zr -= mzr;
			}
		}
	}
	
		
	public final void draw3DTriangleFlatOld(Point3f p1, Point3f p2, Point3f p3, int color) {
		int x1, y1, z1, x2, y2, z2, x3, y3, z3;
		int dx1, dx2, dy1, dy2, dz1, dz2, mx1, mx2, mz1, mz2;
		int xstart, xend, yend, zstart, zend, y, z, dz, left, right, i, index;
		if (p1.y <= p2.y && p1.y <= p3.y) {
			x1 = ((int) p1.x) << 16;
			y1 = ((int) p1.y);
			z1 = (int)((p1.z + fPolygonOffset) * 65536);
			if (p2.y < p3.y) {
				x2 = ((int) p2.x) << 16;
				y2 = ((int) p2.y);
				z2 = (int) ((p2.z + fPolygonOffset) * 65536);
				x3 = ((int) p3.x) << 16;
				y3 = ((int) p3.y);
				z3 = (int) ((p3.z + fPolygonOffset) *65536);		
			} else {
				x2 = ((int) p3.x) << 16;
				y2 = ((int) p3.y);
				z2 = (int) ((p3.z + fPolygonOffset) * 65536);
				x3 = ((int) p2.x) << 16;
				y3 = ((int) p2.y);
				z3 = (int) ((p2.z + fPolygonOffset) *65536);
			}
		} else if (p2.y <= p1.y && p2.y <= p3.y) {
			x1 = ((int) p2.x) << 16;
			y1 = ((int) p2.y);
			z1 = (int) ((p2.z + fPolygonOffset) * 65536);
			if (p1.y < p3.y) {
				x2 = ((int) p1.x) << 16;
				y2 = ((int) p1.y);
				z2 = (int) ((p1.z + fPolygonOffset) * 65536);
				x3 = ((int) p3.x) << 16;
				y3 = ((int) p3.y);
				z3 = (int) ((p3.z + fPolygonOffset) * 65536);
			} else {
				x2 = ((int) p3.x) << 16;
				y2 = ((int) p3.y);
				z2 = (int) ((p3.z + fPolygonOffset) * 65536);
				x3 = ((int) p1.x) << 16;
				y3 = ((int) p1.y);
				z3 = (int) ((p1.z + fPolygonOffset) * 65536);
			}
		} else {
			x1 = ((int) p3.x) << 16;
			y1 = ((int) p3.y);
			z1 = (int) ((p3.z + fPolygonOffset) * 65536);
			if (p1.y < p2.y) {
				x2 = ((int) p1.x) << 16;
				y2 = ((int) p1.y);
				z2 = (int) ((p1.z + fPolygonOffset) * 65536);
				x3 = ((int) p2.x) << 16;
				y3 = ((int) p2.y);
				z3 = (int) ((p2.z + fPolygonOffset) * 65536);
			} else {
				x2 = ((int) p2.x) << 16;
				y2 = ((int) p2.y);
				z2 = (int) ((p2.z + fPolygonOffset) * 65536);
				x3 = ((int) p1.x) << 16;
				y3 = ((int) p1.y);
				z3 = (int) ((p1.z + fPolygonOffset) * 65536);
			}
		}
		dx1 = x3 - x1;
		dy1 = y3 - y1;
		dz1 = z3 - z1;
		if (dy1 == 0) {
			return;
		}
		dx2 = x2 - x1;
		dy2 = y2 - y1;
		dz2 = z2 - z1;
		mx1 = dx1 / dy1;
		mz1 = dz1 / dy1;
		xstart = xend = x1;
		zstart = zend = z1;
		y = y1;
		if (dy2 != 0) {
			mx2 = dx2 / dy2;
			mz2 = dz2 / dy2;
			if (y2 < 0) {
				xstart += mx1 * dy2;
				xend += mx2 * dy2;
				zstart += mz1 * dy2;
				zend += mz2 * dy2;
				y = y2;
			} else if (y < 0) {
				xstart -= mx1 * y;
				xend -= mx2 * y;
				zstart -= mz1 * y;
				zend -= mz2 * y;
				y = 0;
			}
			yend = (y2 < iHeight) ? y2 : iHeight;
			index = y * iWidth;
			while (y < yend) {
				if (xstart < xend) {
					left = xstart >> 16;
					right = xend >> 16;
					z = zstart;
					dz = zend-zstart;
				} else {
					left = xend >> 16;
					right = xstart >> 16;
					z = zend;
					dz = zstart-zend;
				}
				if (left != right) {
					dz /= (right - left);
					if (left < 0) {
						z -= left * dz;
						left = 0;
					}
					if (right > iWidth) {
						right = iWidth;
					}
					for (i = left; i < right; i++) {
						if (z < aiActiveZBuffer[index + i]) {
							aiActiveZBuffer[index + i] = z;
							aiActiveFrameBuffer[index + i] = color;
						}
						z += dz;
					}
				}
				xstart += mx1;
				zstart += mz1;
				xend += mx2;
				zend += mz2;
				index += iWidth;
				y++;
			}
		}
		dx2 = x3 - x2;
		dy2 = y3 - y2;
		dz2 = z3 - z2;
		if (dy2 != 0) {
			mx2 = dx2 / dy2;
			mz2 = dz2 / dy2;
			xend = x2;
			zend = z2;
			if (y < 0) {
				xstart -= mx1 * y;
				xend -= mx2 * y;
				zstart -= mz1 * y;
				zend -= mz2 * y;
				y = 0;
			}
			yend = (y3 < iHeight) ? y3 : iHeight;
			index = y * iWidth;
			while (y < yend) {
				if (xstart < xend) {
					left = xstart >> 16;
					right = xend >> 16;
					z = zstart;
					dz = zend - zstart;
				} else {
					left = xend >> 16;
					right = xstart >> 16;
					z = zend;
					dz = zstart - zend;
				}
				if (left != right) {
					dz /= (right - left);
					if (left < 0) {
						z -= left * dz;
						left = 0;
					}
					if (right > iWidth) {
						right = iWidth;
					}
					for (i = left; i < right; i++) {
						if (z < aiActiveZBuffer[index + i]) {
							aiActiveZBuffer[index + i] = z;
							aiActiveFrameBuffer[index + i] = color;
						}
						z += dz;
					}
				}
				xstart += mx1;
				zstart += mz1;
				xend += mx2;
				zend += mz2;
				index += iWidth;
				y++;
			}
		}
	}

	protected final void draw3DTriangleFlatGhost(Point3f p1, Point3f p2, Point3f p3, int color, int ghost) {
		int x1, y1, z1, x2, y2, z2, x3, y3, z3;
		int dx1, dx2, dy1, dy2, dz1, dz2, mx1, mx2, mz1, mz2;
		int xstart, xend, yend, zstart, zend, y, z, dz, left, right, i, index;
		int ghostColor = ((((color & 0xFF0000) * (255 - ghost)) >> 8) & 0xFF0000) |
				 ((((color & 0xFF00) * (255 - ghost)) >> 8) & 0xFF00) |
				 ((((color & 0xFF) * (255 - ghost)) >> 8) & 0xFF);
					
		
		if (p1.y <= p2.y && p1.y <= p3.y) {
			x1 = ((int) p1.x) << 16;
			y1 = ((int) p1.y);
			z1 = (int)((p1.z + fPolygonOffset) * 65536);
			if (p2.y < p3.y) {
				x2 = ((int) p2.x) << 16;
				y2 = ((int) p2.y);
				z2 = (int) ((p2.z + fPolygonOffset) * 65536);
				x3 = ((int) p3.x) << 16;
				y3 = ((int) p3.y);
				z3 = (int) ((p3.z + fPolygonOffset) *65536);		
			} else {
				x2 = ((int) p3.x) << 16;
				y2 = ((int) p3.y);
				z2 = (int) ((p3.z + fPolygonOffset) * 65536);
				x3 = ((int) p2.x) << 16;
				y3 = ((int) p2.y);
				z3 = (int) ((p2.z + fPolygonOffset) *65536);
			}
		} else if (p2.y <= p1.y && p2.y <= p3.y) {
			x1 = ((int) p2.x) << 16;
			y1 = ((int) p2.y);
			z1 = (int) ((p2.z + fPolygonOffset) * 65536);
			if (p1.y < p3.y) {
				x2 = ((int) p1.x) << 16;
				y2 = ((int) p1.y);
				z2 = (int) ((p1.z + fPolygonOffset) * 65536);
				x3 = ((int) p3.x) << 16;
				y3 = ((int) p3.y);
				z3 = (int) ((p3.z + fPolygonOffset) * 65536);
			} else {
				x2 = ((int) p3.x) << 16;
				y2 = ((int) p3.y);
				z2 = (int) ((p3.z + fPolygonOffset) * 65536);
				x3 = ((int) p1.x) << 16;
				y3 = ((int) p1.y);
				z3 = (int) ((p1.z + fPolygonOffset) * 65536);
			}
		} else {
			x1 = ((int) p3.x) << 16;
			y1 = ((int) p3.y);
			z1 = (int) ((p3.z + fPolygonOffset) * 65536);
			if (p1.y < p2.y) {
				x2 = ((int) p1.x) << 16;
				y2 = ((int) p1.y);
				z2 = (int) ((p1.z + fPolygonOffset) * 65536);
				x3 = ((int) p2.x) << 16;
				y3 = ((int) p2.y);
				z3 = (int) ((p2.z + fPolygonOffset) * 65536);
			} else {
				x2 = ((int) p2.x) << 16;
				y2 = ((int) p2.y);
				z2 = (int) ((p2.z + fPolygonOffset) * 65536);
				x3 = ((int) p1.x) << 16;
				y3 = ((int) p1.y);
				z3 = (int) ((p1.z + fPolygonOffset) * 65536);
			}
		}
		dx1 = x3 - x1;
		dy1 = y3 - y1;
		dz1 = z3 - z1;
		if (dy1 == 0) {
			return;
		}
		dx2 = x2 - x1;
		dy2 = y2 - y1;
		dz2 = z2 - z1;
		mx1 = dx1 / dy1;
		mz1 = dz1 / dy1;
		xstart = xend = x1;
		zstart = zend = z1;
		y = y1;
		if (dy2 != 0) {
			mx2 = dx2 / dy2;
			mz2 = dz2 / dy2;
			if (y2 < 0) {
				xstart += mx1 * dy2;
				xend += mx2 * dy2;
				zstart += mz1 * dy2;
				zend += mz2 * dy2;
				y = y2;
			} else if (y < 0) {
				xstart -= mx1 * y;
				xend -= mx2 * y;
				zstart -= mz1 * y;
				zend -= mz2 * y;
				y = 0;
			}
			yend = (y2 < iHeight) ? y2 : iHeight;
			index = y * iWidth;
			while (y < yend) {
				if (xstart < xend) {
					left = xstart >> 16;
					right = xend >> 16;
					z = zstart;
					dz = zend-zstart;
				} else {
					left = xend >> 16;
					right = xstart >> 16;
					z = zend;
					dz = zstart-zend;
				}
				if (left != right) {
					dz /= (right - left);
					if (left < 0) {
						z -= left * dz;
						left = 0;
					}
					if (right > iWidth) {
						right = iWidth;
					}
					for (i = left; i < right; i++) {
						if (z < aiActiveZBuffer[index + i]) {
							aiActiveZBuffer[index + i] = z;
							aiActiveFrameBuffer[index + i] = color;
						} else {
							int backColor = aiActiveFrameBuffer[index + i];
							aiActiveFrameBuffer[index + i] = (((((backColor & 0xFF0000) * ghost) >> 8) & 0xFF0000) |
									     		 ((((backColor & 0xFF00) * ghost) >> 8) & 0xFF00) |
											 ((((backColor & 0xFF) * ghost) >> 8) & 0xFF)) + ghostColor;
						}
						z += dz;
					}
				}
				xstart += mx1;
				zstart += mz1;
				xend += mx2;
				zend += mz2;
				index += iWidth;
				y++;
			}
		}
		dx2 = x3 - x2;
		dy2 = y3 - y2;
		dz2 = z3 - z2;
		if (dy2 != 0) {
			mx2 = dx2 / dy2;
			mz2 = dz2 / dy2;
			xend = x2;
			zend = z2;
			if (y < 0) {
				xstart -= mx1 * y;
				xend -= mx2 * y;
				zstart -= mz1 * y;
				zend -= mz2 * y;
				y = 0;
			}
			yend = (y3 < iHeight) ? y3 : iHeight;
			index = y * iWidth;
			while (y < yend) {
				if (xstart < xend) {
					left = xstart >> 16;
					right = xend >> 16;
					z = zstart;
					dz = zend - zstart;
				} else {
					left = xend >> 16;
					right = xstart >> 16;
					z = zend;
					dz = zstart - zend;
				}
				if (left != right) {
					dz /= (right - left);
					if (left < 0) {
						z -= left * dz;
						left = 0;
					}
					if (right > iWidth) {
						right = iWidth;
					}
					for (i = left; i < right; i++) {
						if (z < aiActiveZBuffer[index + i]) {
							aiActiveZBuffer[index + i] = z;
							aiActiveFrameBuffer[index + i] = color;
						} else {
							int backColor = aiActiveFrameBuffer[index + i];
							aiActiveFrameBuffer[index + i] = (((((backColor & 0xFF0000) * ghost) >> 8) & 0xFF0000) |
									     		 ((((backColor & 0xFF00) * ghost) >> 8) & 0xFF00) |
											 ((((backColor & 0xFF) * ghost) >> 8) & 0xFF)) + ghostColor;
						}
						z += dz;
					}
				}
				xstart += mx1;
				zstart += mz1;
				xend += mx2;
				zend += mz2;
				index += iWidth;
				y++;
			}
		}
	}
	
	protected final void draw3DTriangleFlatTransparent(Point3f p1, Point3f p2, Point3f p3, int color, int transparency) {
		int opacity = 255 - transparency;
		int x1, y1, z1, x2, y2, z2, x3, y3, z3;
		int dx1, dx2, dy1, dy2, dz1, dz2, mx1, mx2, mz1, mz2;
		int xstart, xend, yend, zstart, zend, y, z, dz, left, right, i, index;
		int red = (((color & 0xFF0000) * opacity) & 0xFF000000) >> 8;
		int green = (((color & 0xFF00) * opacity) & 0xFF0000) >> 8;
		int blue = (((color & 0xFF) * opacity) & 0xFF00) >> 8;
		if (p1.y <= p2.y && p1.y <= p3.y) {
			x1 = ((int) p1.x) << 16;
			y1 = ((int) p1.y);
			z1 = (int)((p1.z + fPolygonOffset) * 65536);
			if (p2.y < p3.y) {
				x2 = ((int) p2.x) << 16;
				y2 = ((int) p2.y);
				z2 = (int) ((p2.z + fPolygonOffset) * 65536);
				x3 = ((int) p3.x) << 16;
				y3 = ((int) p3.y);
				z3 = (int) ((p3.z + fPolygonOffset) *65536);		
			} else {
				x2 = ((int) p3.x) << 16;
				y2 = ((int) p3.y);
				z2 = (int) ((p3.z + fPolygonOffset) * 65536);
				x3 = ((int) p2.x) << 16;
				y3 = ((int) p2.y);
				z3 = (int) ((p2.z + fPolygonOffset) *65536);
			}
		} else if (p2.y <= p1.y && p2.y <= p3.y) {
			x1 = ((int) p2.x) << 16;
			y1 = ((int) p2.y);
			z1 = (int) ((p2.z + fPolygonOffset) * 65536);
			if (p1.y < p3.y) {
				x2 = ((int) p1.x) << 16;
				y2 = ((int) p1.y);
				z2 = (int) ((p1.z + fPolygonOffset) * 65536);
				x3 = ((int) p3.x) << 16;
				y3 = ((int) p3.y);
				z3 = (int) ((p3.z + fPolygonOffset) * 65536);
			} else {
				x2 = ((int) p3.x) << 16;
				y2 = ((int) p3.y);
				z2 = (int) ((p3.z + fPolygonOffset) * 65536);
				x3 = ((int) p1.x) << 16;
				y3 = ((int) p1.y);
				z3 = (int) ((p1.z + fPolygonOffset) * 65536);
			}
		} else {
			x1 = ((int) p3.x) << 16;
			y1 = ((int) p3.y);
			z1 = (int) ((p3.z + fPolygonOffset) * 65536);
			if (p1.y < p2.y) {
				x2 = ((int) p1.x) << 16;
				y2 = ((int) p1.y);
				z2 = (int) ((p1.z + fPolygonOffset) * 65536);
				x3 = ((int) p2.x) << 16;
				y3 = ((int) p2.y);
				z3 = (int) ((p2.z + fPolygonOffset) * 65536);
			} else {
				x2 = ((int) p2.x) << 16;
				y2 = ((int) p2.y);
				z2 = (int) ((p2.z + fPolygonOffset) * 65536);
				x3 = ((int) p1.x) << 16;
				y3 = ((int) p1.y);
				z3 = (int) ((p1.z + fPolygonOffset) * 65536);
			}
		}
		dx1 = x3 - x1;
		dy1 = y3 - y1;
		dz1 = z3 - z1;
		if (dy1 == 0) {
			return;
		}
		dx2 = x2 - x1;
		dy2 = y2 - y1;
		dz2 = z2 - z1;
		mx1 = dx1 / dy1;
		mz1 = dz1 / dy1;
		xstart = xend = x1;
		zstart = zend = z1;
		y = y1;
		if (dy2 != 0) {
			mx2 = dx2 / dy2;
			mz2 = dz2 / dy2;
			if (y2 < 0) {
				xstart += mx1 * dy2;
				xend += mx2 * dy2;
				zstart += mz1 * dy2;
				zend += mz2 * dy2;
				y = y2;
			} else if (y < 0) {
				xstart -= mx1 * y;
				xend -= mx2 * y;
				zstart -= mz1 * y;
				zend -= mz2 * y;
				y = 0;
			}
			yend = (y2 < iHeight) ? y2 : iHeight;
			index = y * iWidth;
			while (y < yend) {
				if (xstart < xend) {
					left = xstart >> 16;
					right = xend >> 16;
					z = zstart;
					dz = zend-zstart;
				} else {
					left = xend >> 16;
					right = xstart >> 16;
					z = zend;
					dz = zstart-zend;
				}
				if (left != right) {
					dz /= (right - left);
					if (left < 0) {
						z -= left * dz;
						left = 0;
					}
					if (right > iWidth) {
						right = iWidth;
					}
					for (i = left; i < right; i++) {
						if (z < aiActiveZBuffer[index + i]) {
							int fb = aiActiveFrameBuffer[index + i];
							int fbRed = ((((fb & 0x00FF0000) * transparency) >> 8) + red) & 0x00FF0000;
							int fbGreen = ((((fb & 0x0000FF00) * transparency) >> 8) + green) & 0x0000FF00;
							int fbBlue = ((((fb & 0x000000FF) * transparency) >> 8) + blue) & 0x000000FF;
							aiActiveFrameBuffer[index + i] = 0xFF000000 | fbRed | fbGreen | fbBlue;
						}
						z += dz;
					}
				}
				xstart += mx1;
				zstart += mz1;
				xend += mx2;
				zend += mz2;
				index += iWidth;
				y++;
			}
		}
		dx2 = x3 - x2;
		dy2 = y3 - y2;
		dz2 = z3 - z2;
		if (dy2 != 0) {
			mx2 = dx2 / dy2;
			mz2 = dz2 / dy2;
			xend = x2;
			zend = z2;
			if (y < 0) {
				xstart -= mx1 * y;
				xend -= mx2 * y;
				zstart -= mz1 * y;
				zend -= mz2 * y;
				y = 0;
			}
			yend = (y3 < iHeight) ? y3 : iHeight;
			index = y * iWidth;
			while (y < yend) {
				if (xstart < xend) {
					left = xstart >> 16;
					right = xend >> 16;
					z = zstart;
					dz = zend - zstart;
				} else {
					left = xend >> 16;
					right = xstart >> 16;
					z = zend;
					dz = zstart - zend;
				}
				if (left != right) {
					dz /= (right - left);
					if (left < 0) {
						z -= left * dz;
						left = 0;
					}
					if (right > iWidth) {
						right = iWidth;
					}
					for (i = left; i < right; i++) {
						if (z < aiActiveZBuffer[index + i]) {
							int fb = aiActiveFrameBuffer[index + i];
							int fbRed = ((((fb & 0x00FF0000) * transparency) >> 8) + red) & 0x00FF0000;
							int fbGreen = ((((fb & 0x0000FF00) * transparency) >> 8) + green) & 0x0000FF00;
							int fbBlue = ((((fb & 0x000000FF) * transparency) >> 8) + blue) & 0x000000FF;
							aiActiveFrameBuffer[index + i] = 0xFF000000 | fbRed | fbGreen | fbBlue;
						}
						z += dz;
					}
				}
				xstart += mx1;
				zstart += mz1;
				xend += mx2;
				zend += mz2;
				index += iWidth;
				y++;
			}
		}
	}

	//protected final void draw3DTrianglePhong(Point3f p1, Point3f p2, Point3f p3, Vector3f n1, Vector3f n2, Vector3f n3, MaterialProperties mp) {
	//	int x1, y1, z1, x2, y2, z2, x3, y3, z3;
	//	float nx1, ny1, nz1, nx2, ny2, nz2, nx3, ny3, nz3;
	//	int dx1, dx2, dy1, dy2, dz1, dz2, mx1, mx2, mz1, mz2;
	//	float dnx1, dnx2, dny1, dny2, dnz1, dnz2, mnx1, mnx2, mny1, mny2, mnz1, mnz2;
	//	int xstart, xend, yend, zstart, zend, y, z, dz, left, right, i, index;
	//	float nxstart, nxend, nystart, nyend, nzstart, nzend, nx, ny, nz, dnx, dny, dnz;
	//	Vector3f normal = new Vector3f();
	//	if (p1.y <= p2.y && p1.y <= p3.y) {
	//		x1 = ((int) p1.x) << 16;
	//		y1 = ((int) p1.y);
	//		z1 = (int)((p1.z + fPolygonOffset) * 65536);
	//		nx1 = n1.x;
	//		ny1 = n1.y;
	//		nz1 = n1.z;
	//		if (p2.y < p3.y) {
	//			x2 = ((int) p2.x) << 16;
	//			y2 = ((int) p2.y);
	//			z2 = (int) ((p2.z + fPolygonOffset) * 65536);
	//			nx2 = n2.x;
	//			ny2 = n2.y;
	//			nz2 = n2.z;
	//			x3 = ((int) p3.x) << 16;
	//			y3 = ((int) p3.y);
	//			z3 = (int) ((p3.z + fPolygonOffset) *65536);
	//			nx3 = n3.x;
	//			ny3 = n3.y;
	//			nz3 = n3.z;
	//		} else {
	//			x2 = ((int) p3.x) << 16;
	//			y2 = ((int) p3.y);
	//			z2 = (int) ((p3.z + fPolygonOffset) * 65536);
	//			nx2 = n3.x;
	//			ny2 = n3.y;
	//			nz2 = n3.z;
	//			x3 = ((int) p2.x) << 16;
	//			y3 = ((int) p2.y);
	//			z3 = (int) ((p2.z + fPolygonOffset) *65536);
	//			nx3 = n2.x;
	//			ny3 = n2.y;
	//			nz3 = n2.z;
	//		}
	//	} else if (p2.y <= p1.y && p2.y <= p3.y) {
	//		x1 = ((int) p2.x) << 16;
	//		y1 = ((int) p2.y);
	//		z1 = (int) ((p2.z + fPolygonOffset) * 65536);
	//		nx1 = n2.x;
	//		ny1 = n2.y;
	//		nz1 = n2.z;
	//		if (p1.y < p3.y) {
	//			x2 = ((int) p1.x) << 16;
	//			y2 = ((int) p1.y);
	//			z2 = (int) ((p1.z + fPolygonOffset) * 65536);
	//			nx2 = n1.x;
	//			ny2 = n1.y;
	//			nz2 = n1.z;
	//			x3 = ((int) p3.x) << 16;
	//			y3 = ((int) p3.y);
	//			z3 = (int) ((p3.z + fPolygonOffset) * 65536);
	//			nx3 = n3.x;
	//			ny3 = n3.y;
	//			nz3 = n3.z;
	//		} else {
	//			x2 = ((int) p3.x) << 16;
	//			y2 = ((int) p3.y);
	//			z2 = (int) ((p3.z + fPolygonOffset) * 65536);
	//			nx2 = n3.x;
	//			ny2 = n3.y;
	//			nz2 = n3.z;
	//			x3 = ((int) p1.x) << 16;
	//			y3 = ((int) p1.y);
	//			z3 = (int) ((p1.z + fPolygonOffset) * 65536);
	//			nx3 = n1.x;
	//			ny3 = n1.y;
	//			nz3 = n1.z;
	//		}
	//	} else {
	//		x1 = ((int) p3.x) << 16;
	//		y1 = ((int) p3.y);
	//		z1 = (int) ((p3.z + fPolygonOffset) * 65536);
	//		nx1 = n3.x;
	//		ny1 = n3.y;
	//		nz1 = n3.z;
	//		if (p1.y < p2.y) {
	//			x2 = ((int) p1.x) << 16;
	//			y2 = ((int) p1.y);
	//			z2 = (int) ((p1.z + fPolygonOffset) * 65536);
	//			nx2 = n1.x;
	//			ny2 = n1.y;
	//			nz2 = n1.z;
	//			x3 = ((int) p2.x) << 16;
	//			y3 = ((int) p2.y);
	//			z3 = (int) ((p2.z + fPolygonOffset) * 65536);
	//			nx3 = n2.x;
	//			ny3 = n2.y;
	//			nz3 = n2.z;
	//		} else {
	//			x2 = ((int) p2.x) << 16;
	//			y2 = ((int) p2.y);
	//			z2 = (int) ((p2.z + fPolygonOffset) * 65536);
	//			nx2 = n2.x;
	//			ny2 = n2.y;
	//			nz2 = n2.z;
	//			x3 = ((int) p1.x) << 16;
	//			y3 = ((int) p1.y);
	//			z3 = (int) ((p1.z + fPolygonOffset) * 65536);
	//			nx3 = n1.x;
	//			ny3 = n1.y;
	//			nz3 = n1.z;
	//		}
	//	}
	//	dx1 = x3 - x1;
	//	dy1 = y3 - y1;
	//	dz1 = z3 - z1;
	//	dnx1 = nx3 - nx1;
	//	dny1 = ny3 - ny1;
	//	dnz1 = nz3 - nz1;
	//	if (dy1 == 0) {
	//		return;
	//	}
	//	dx2 = x2 - x1;
	//	dy2 = y2 - y1;
	//	dz2 = z2 - z1;
	//	dnx2 = nx2 - nx1;
	//	dny2 = ny2 - ny1;
	//	dnz2 = nz2 - nz1;
	//	mx1 = dx1 / dy1;
	//	mz1 = dz1 / dy1;
	//	mnx1 = dnx1 / dy1;
	//	mny1 = dny1 / dy1;
	//	mnz1 = dnz1 / dy1;
	//	xstart = xend = x1;
	//	zstart = zend = z1;
	//	nxstart = nxend = nx1;
	//	nystart = nyend = ny1;
	//	nzstart = nzend = nz1;
	//	y = y1;
	//	if (dy2 != 0) {
	//		mx2 = dx2 / dy2;
	//		mz2 = dz2 / dy2;
	//		mnx2 = dnx2 / dy2;
	//		mny2 = dny2 / dy2;
	//		mnz2 = dnz2 / dy2;
	//		if (y2 < 0) {
	//			xstart += mx1 * dy2;
	//			xend += mx2 * dy2;
	//			zstart += mz1 * dy2;
	//			zend += mz2 * dy2;
	//			nxstart += nx1 * dy2;
	//			nxend += nx2 * dy2;
	//			nystart += ny1 * dy2;
	//			nyend += ny2 * dy2;
	//			nzstart += nz1 * dy2;
	//			nzend += nz2 * dy2;
	//			y = y2;
	//		} else if (y < 0) {
	//			xstart -= mx1 * y;
	//			xend -= mx2 * y;
	//			zstart -= mz1 * y;
	//			zend -= mz2 * y;
	//			nxstart -= nx1 * y;
	//			nxend -= nx2 * y;
	//			nystart -= ny1 * y;
	//			nyend -= ny2 * y;
	//			nzstart -= nz1 * y;
	//			nzend -= nz2 * y;
	//			y = 0;
	//		}
	//		yend = (y2 < iHeight) ? y2 : iHeight;
	//		index = y * iWidth;
	//		while (y < yend) {
	//			if (xstart < xend) {
	//				left = xstart >> 16;
	//				right = xend >> 16;
	//				z = zstart;
	//				dz = zend - zstart;
	//				nx = nxstart;
	//				dnx = nxend - nxstart;
	//				ny = nystart;
	//				dny = nyend - nystart;
	//				nz = nzstart;
	//				dnz = nzend - nzstart;
	//			} else {
	//				left = xend >> 16;
	//				right = xstart >> 16;
	//				z = zend;
	//				dz = zstart-zend;
	//				nx = nxend;
	//				dnx = nxstart - nxend;
	//				ny = nyend;
	//				dny = nystart - nyend;
	//				nz = nzend;
	//				dnz = nzstart - nzend;
	//			}
	//			if (left != right) {
	//				dz /= (right - left);
	//				dnx /= (float) (right - left);
	//				dny /= (float) (right - left);
	//				dnz /= (float) (right - left);
	//				if (left < 0) {
	//					z -= left * dz;
	//					nx -= (float) left * dnx;
	//					ny -= (float) left * dny;
	//					nz -= (float) left * dnz;
	//					left = 0;
	//				}
	//				if (right > iWidth) {
	//					right = iWidth;
	//				}
	//				for (i = left; i < right; i++) {
	//					if (z < aiActiveZBuffer[index + i]) {
	//						normal.set(nx,ny,nz);
	//						aiActiveFrameBuffer[index + i] = shade(null,normal,mp);
	//						aiActiveZBuffer[index + i] = z;
	//					}
	//					z += dz;
	//					nx += dnx;
	//					ny += dny;
	//					nz += dnz;
	//				}
	//			}
	//			xstart += mx1;
	//			zstart += mz1;
	//			nxstart += mnx1;
	//			nystart += mny1;
	//			nzstart += mnz1;
	//			xend += mx2;
	//			zend += mz2;
	//			nxend += mnx2;
	//			nyend += mny2;
	//			nzend += mnz2;
	//			index += iWidth;
	//			y++;
	//		}
	//	}
	//	dx2 = x3 - x2;
	//	dy2 = y3 - y2;
	//	dz2 = z3 - z2;
	//	dnx2 = nx3 - nx2;
	//	dny2 = ny3 - ny2;
	//	dnz2 = nz3 - nz2;
	//	if (dy2 != 0) {
	//		mx2 = dx2 / dy2;
	//		mz2 = dz2 / dy2;
	//		mnx2 = dnx2 / dy2;
	//		mny2 = dny2 / dy2;
	//		mnz2 = dnz2 / dy2;
	//		xend = x2;
	//		zend = z2;
	//		nxend = nx2;
	//		nyend = ny2;
	//		nzend = nz2;
	//		if (y < 0) {
	//			xstart -= mx1 * y;
	//			xend -= mx2 * y;
	//			zstart -= mz1 * y;
	//			zend -= mz2 * y;
	//			nxstart -= mnx1 * y;
	//			nxend -= mnx2 * y;
	//			nystart -= mny1 * y;
	//			nyend -= mny2 * y;
	//			nzstart -= mnz1 * y;
	//			nzend -= mnz2 * y;
	//			y = 0;
	//		}
	//		yend = (y3 < iHeight) ? y3 : iHeight;
	//		index = y * iWidth;
	//		while (y < yend) {
	//			if (xstart < xend) {
	//				left = xstart >> 16;
	//				right = xend >> 16;
	//				z = zstart;
	//				dz = zend - zstart;
	//				nx = nxstart;
	//				dnx = nxend - nxstart;
	//				ny = nystart;
	//				dny = nyend - nystart;
	//				nz = nzstart;
	//				dnz = nzend - nzstart;
	//			} else {
	//				left = xend >> 16;
	//				right = xstart >> 16;
	//				z = zend;
	//				dz = zstart - zend;
	//				nx = nxend;
	//				dnx = nxstart - nxend;
	//				ny = nyend;
	//				dny = nystart - nyend;
	//				nz = nzend;
	//				dnz = nzstart - nzend;
	//			}
	//			if (left != right) {
	//				dz /= (right - left);
	//				dnx /= (float) (right - left);
	//				dny /= (float) (right - left);
	//				dnz /= (float) (right - left);
	//				if (left < 0) {
	//					z -= left * dz;
	//					nx -= (float) left * dnx;
	//					ny -= (float) left * dny;
	//					nz -= (float) left * dnz;
	//					left = 0;
	//				}
	//				if (right > iWidth) {
	//					right = iWidth;
	//				}
	//				for (i = left; i < right; i++) {
	//					if (z < aiActiveZBuffer[index + i]) {
	//						normal.set(nx,ny,nz);
	//						aiActiveFrameBuffer[index + i] = shade(null,normal,mp);
	//						aiActiveZBuffer[index + i] = z;
	//					}
	//					z += dz;
	//					nx += dnx;
	//					ny += dny;
	//					nz += dnz;
	//				}
	//			}
	//			xstart += mx1;
	//			zstart += mz1;
	//			nxstart += mnx1;
	//			nystart += mny1;
	//			nzstart += mnz1;
	//			xend += mx2;
	//			zend += mz2;
	//			nxend += mnx2;
	//			nyend += mny2;
	//			nzend += mnz2;
	//			index += iWidth;
	//			y++;
	//		}
	//	}
	//}

	//protected final void draw3DQuadFlat(Point3f p1, Point3f p2, Point3f p3, Point3f p3, int color) {
	//	int x1, x2, x3, x4, y1, y2, y3, y4, z1, z2, z3, z4;
	//	Point3f a,b,c,d;
	//	
	//	/* sort points by y */
	//	if (p1.y <= p2.y && p1.y <= p3.y && p1.y <= p4.y) {
	//		a = p1;
	//		if (p2.y <= p3.y && p2.y <= p4.y) {
	//			b = p2;
	//			if (p3.y <= p4.y) {
	//				c = p3;
	//				d = p4;
	//			} else {
	//				c = p4;
	//				d = p3;
	//			}
	//		} else if (p3.y <= p2.y && p3.y <= p4.y) {
	//			b2 = p3;
	//			if (p2.y <= p4.y) {
	//				c = p2;
	//				d = p4;
	//			} else {
	//				c = p4;
	//				d = p2;
	//			}
	//		} else {
	//			b2 = p4;
	//			if (p2.y <= p3.y) {
	//				c = p2;
	//				d = p3;
	//			} else {
	//				c = p3;
	//				d = p2;
	//			}
	//		}
	//	} else if (p2.y <= p1.y && p2.y <= p3.y && p2.y <= p4.y) {
	//		a = p2;
	//		if (p1.y <= p3.y && p1.y <= p4.y) {
	//			b = p1;
	//			if (p3.y <= p4.y) {
	//				c = p3;
	//				d = p4;
	//			} else {
	//				c = p4;
	//				d = p3;
	//			}
	//		} else if (p3.y <= p1.y && p3.y <= p4.y) {
	//			b2 = p3;
	//			if (p1.y <= p4.y) {
	//				c = p1;
	//				d = p4;
	//			} else {
	//				c = p4;
	//				d = p1;
	//			}
	//		} else {
	//			b2 = p4;
	//			if (p1.y <= p3.y) {
	//				c = p1;
	//				d = p3;
	//			} else {
	//				c = p3;
	//				d = p1;
	//			}
	//		}
	//	} else if (p3.y <= p1.y && p3.y <= p2.y && p3.y <= p4.y) {
	//		a = p3;
	//		if (p2.y <= p1.y && p2.y <= p4.y) {
	//			b = p2;
	//			if (p1.y <= p4.y) {
	//				c = p1;
	//				d = p4;
	//			} else {
	//				c = p4;
	//				d = p1;
	//			}
	//		} else if (p1.y <= p2.y && p1.y <= p4.y) {
	//			b2 = p1;
	//			if (p2.y <= p4.y) {
	//				c = p2;
	//				d = p4;
	//			} else {
	//				c = p4;
	//				d = p2;
	//			}
	//		} else {
	//			b2 = p4;
	//			if (p2.y <= p1.y) {
	//				c = p2;
	//				d = p1;
	//			} else {
	//				c = p1;
	//				d = p2;
	//			}
	//		}
	//	} else {
	//		a = p4;
	//		if (p2.y <= p3.y && p2.y <= p1.y) {
	//			b = p2;
	//			if (p3.y <= p1.y) {
	//				c = p3;
	//				d = p1;
	//			} else {
	//				c = p1;
	//				d = p3;
	//			}
	//		} else if (p3.y <= p2.y && p3.y <= p1.y) {
	//			b2 = p3;
	//			if (p2.y <= p1.y) {
	//				c = p2;
	//				d = p1;
	//			} else {
	//				c = p1;
	//				d = p2;
	//			}
	//		} else {
	//			b2 = p1;
	//			if (p2.y <= p3.y) {
	//				c = p2;
	//				d = p3;
	//			} else {
	//				c = p3;
	//				d = p2;
	//			}
	//		}
	//	}
	//}
			
	//protected final void draw3DTriangleGourad(Point3f p1, Point3f p2, Point3f p3, int color1, int color2, int color3) {
	//	draw3DTriangleFlat(p1, p2, p3, color1);
	//}
		//	if (true) return;
	//	
	//	if (bCulling && (p2.x - p1.x) * (p3.y - p1.y) - (p2.y - p1.y) * (p3.x - p1.x) > 0) return;
	//	int x1, y1, z1, x2, y2, z2, x3, y3, z3;
	//	int dx1, dx2, dy1, dy2, dz1, dz2, mx1, mx2, mz1, mz2;
	//	int xstart, xend, yend, zstart, zend, y, z, dz, left, right, i, index;
	//	int red1, green1, blue1, red2, green2, blue2, red3, green3, blue3;
	//	int dred1, dred2, dgreen1, dgreen2, dblue1, dblue2, mred1, mred2, mgreen1, mgreen2, mblue1, mblue2;
	//	int redstart, redend, greenstart, greenend, bluestart, blueend, red, green, blue, dred, dgreen, dblue;
	//	if (p1.y <= p2.y && p1.y <= p3.y) {
	//		//x1 = ((int) p1.x) << 16;
	//		x1 = (int) (p1.x * 65536);
	//		//y1 = ((int) p1.y);
	//		y1 = (int) (p1.y * 65536);
	//		z1 = (int)((p1.z + fPolygonOffset) * 65536);
	//		red1 = (color1 & 0x00FF0000) >> 8;
	//		green1 = (color1 & 0x0000FF00);
	//		blue1 = (color1 & 0x000000FF) << 8;
	//		if (p2.y < p3.y) {
	//			//x2 = ((int) p2.x) << 16;
	//			x2 = (int) (p2.x * 65536);
	//			//y2 = ((int) p2.y);
	//			y2 = (int) (p2.y * 65536);
	//			z2 = (int) ((p2.z + fPolygonOffset) * 65536);
	//			red2 = (color2 & 0x00FF0000) >> 8;
	//			green2 = (color2 & 0x0000FF00);
	//			blue2 = (color2 & 0x000000FF) << 8;
	//			//x3 = ((int) p3.x) << 16;
	//			x3 = (int) (p3.x * 65536);
	//			//y3 = ((int) p3.y);
	//			y3 = (int) (p3.y * 65536);
	//			z3 = (int) ((p3.z + fPolygonOffset) *65536);
	//			red3 = (color3 & 0x00FF0000) >> 8;
	//			green3 = (color3 & 0x0000FF00);
	//			blue3 = (color3 & 0x000000FF) << 8;
	//		} else {
	//			//x2 = ((int) p3.x) << 16;
	//			x2 = (int) (p3.x * 65536);
	//			//y2 = ((int) p3.y);
	//			y2 = (int) (p3.y * 65536);
	//			z2 = (int) ((p3.z + fPolygonOffset) * 65536);
	//			red2 = (color3 & 0x00FF0000) >> 8;
	//			green2 = (color3 & 0x0000FF00);
	//			blue2 = (color3 & 0x000000FF) << 8;
	//			//x3 = ((int) p2.x) << 16;
	//			x3 = (int) (p2.x * 65536);
	//			//y3 = ((int) p2.y);
	//			y3 = (int) (p2.y * 65536);
	//			z3 = (int) ((p2.z + fPolygonOffset) *65536);
	//			red3 = (color2 & 0x00FF0000) >> 8;
	//			green3 = (color2 & 0x0000FF00);
	//			blue3 = (color2 & 0x000000FF) << 8;
	//		}
	//	} else if (p2.y <= p1.y && p2.y <= p3.y) {
	//		//x1 = ((int) p2.x) << 16;
	//		x1 = (int) (p2.x * 65536);
	//		//y1 = ((int) p2.y);
	//		y1 = (int) (p2.y * 65536);
	//		z1 = (int) ((p2.z + fPolygonOffset) * 65536);
	//		red1 = (color2 & 0x00FF0000) >> 8;
	//		green1 = (color2 & 0x0000FF00);
	//		blue1 = (color2 & 0x000000FF) << 8;
	//		if (p1.y < p3.y) {
	//			//x2 = ((int) p1.x) << 16;
	//			x2 = (int) (p1.x * 65536);
	//			//y2 = ((int) p1.y);
	//			y2 = (int) (p1.y * 65536);
	//			z2 = (int) ((p1.z + fPolygonOffset) * 65536);
	//			red2 = (color1 & 0x00FF0000) >> 8;
	//			green2 = (color1 & 0x0000FF00);
	//			blue2 = (color1 & 0x000000FF) << 8;
	//			//x3 = ((int) p3.x) << 16;
	//			x3 = (int) (p3.x * 65536);
	//			//y3 = ((int) p3.y);
	//			y3 = (int) (p3.y * 65536);
	//			z3 = (int) ((p3.z + fPolygonOffset) * 65536);
	//			red3 = (color3 & 0x00FF0000) >> 8;
	//			green3 = (color3 & 0x0000FF00);
	//			blue3 = (color3 & 0x000000FF) << 8;
	//		} else {
	//			//x2 = ((int) p3.x) << 16;
	//			x2 = (int) (p3.x * 65536);
	//			//y2 = ((int) p3.y);
	//			y2 = (int) (p3.y * 65536);
	//			z2 = (int) ((p3.z + fPolygonOffset) * 65536);
	//			red2 = (color3 & 0x00FF0000) >> 8;
	//			green2 = (color3 & 0x0000FF00);
	//			blue2 = (color3 & 0x000000FF) << 8;
	//			//x3 = ((int) p1.x) << 16;
	//			x3 = (int) (p1.x * 65536);
	//			//y3 = ((int) p1.y);
	//			y3 = (int) (p1.y * 65536);
	//			z3 = (int) ((p1.z + fPolygonOffset) * 65536);
	//			red3 = (color1 & 0x00FF0000) >> 8;
	//			green3 = (color1 & 0x0000FF00);
	//			blue3 = (color1 & 0x000000FF) << 8;
	//		}
	//	} else {
	//		//x1 = ((int) p3.x) << 16;
	//		x1 = (int) (p3.x * 65536);
	//		//y1 = ((int) p3.y);
	//		y1 = (int) (p3.y * 65536);
	//		z1 = (int) ((p3.z + fPolygonOffset) * 65536);
	//		red1 = (color3 & 0x00FF0000) >> 8;
	//		green1 = (color3 & 0x0000FF00);
	//		blue1 = (color3 & 0x000000FF) << 8;
	//		if (p1.y < p2.y) {
	//			//x2 = ((int) p1.x) << 16;
	//			x2 = (int) (p1.x * 65536);
	//			//y2 = ((int) p1.y);
	//			y2 = (int) (p1.y * 65536);
	//			z2 = (int) ((p1.z + fPolygonOffset) * 65536);
	//			red2 = (color1 & 0x00FF0000) >> 8;
	//			green2 = (color1 & 0x0000FF00);
	//			blue2 = (color1 & 0x000000FF) << 8;
	//			//x3 = ((int) p2.x) << 16;
	//			x3 = (int) (p2.x * 65536);
	//			//y3 = ((int) p2.y);
	//			y3 = (int) (p2.y * 65536);
	//			z3 = (int) ((p2.z + fPolygonOffset) * 65536);
	//			red3 = (color2 & 0x00FF0000) >> 8 ;
	//			green3 = (color2 & 0x0000FF00);
	//			blue3 = (color2 & 0x000000FF) << 8;
	//		} else {
	//			//x2 = ((int) p2.x) << 16;
	//			x2 = (int) (p2.x * 65536);
	//			//y2 = ((int) p2.y);
	//			y2 = (int) (p2.y * 65536);
	//			z2 = (int) ((p2.z + fPolygonOffset) * 65536);
	//			red2 = (color2 & 0x00FF0000) >> 8;
	//			green2 = (color2 & 0x0000FF00);
	//			blue2 = (color2 & 0x000000FF) << 8;
	//			//x3 = ((int) p1.x) << 16;
	//			x3 = (int) (p1.x * 65536);
	//			//y3 = ((int) p1.y);
	//			y3 = (int) (p1.y * 65536);
	//			z3 = (int) ((p1.z + fPolygonOffset) * 65536);
	//			red3 = (color1 & 0x00FF0000) >> 8;
	//			green3 = (color1 & 0x0000FF00);
	//			blue3 = (color1 & 0x000000FF) << 8;
	//		}
	//	}
	//	dx1 = x3 - x1;
	//	dy1 = (y3 - y1) >> 16;
	//	dz1 = z3 - z1;
	//	dred1 = red3 - red1;
	//	dgreen1 = green3 - green1;
	//	dblue1 = blue3 - blue1;
	//	if (dy1 == 0) {
	//		return;
	//	}
	//	dx2 = x2 - x1;
	//	dy2 = (y2 - y1) >> 16;
	//	dz2 = z2 - z1;
	//	dred2 = red2 - red1;
	//	dgreen2 = green2 - green1;
	//	dblue2 = blue2 - blue1;
	//	mx1 = dx1 / dy1;
	//	mz1 = dz1 / dy1;
	//	mred1 = dred1 / dy1;
	//	mgreen1 = dgreen1 / dy1;
	//	mblue1 = dblue1 / dy1;
	//	xstart = xend = x1;
	//	zstart = zend = z1;
	//	redstart = redend = red1;
	//	greenstart = greenend = green1;
	//	bluestart = blueend = blue1;
	//	y = y1;
	//	//System.out.println("y1 = " + y1);
	//	//System.out.println("y2 = " + y2);
	//	//System.out.println("y3 = " + y3);
	//	//System.out.println("dy1 = " + dy1);
	//	//System.out.println("dy2 = " + dy2);
	//	
	//	if (dy2 != 0) {
	//		mx2 = dx2 / dy2;
	//		mz2 = dz2 / dy2;
	//		mred2 = dred2 / dy2;
	//		mgreen2 = dgreen2 / dy2;
	//		mblue2 = dblue2 / dy2;
	//		if (y2 < 0) {
	//			xstart += mx1 * dy2;
	//			xend += mx2 * dy2;
	//			zstart += mz1 * dy2;
	//			zend += mz2 * dy2;
	//			redstart += mred1 * dy2;
	//			redend += mred2 * dy2;
	//			greenstart += mgreen1 * dy2;
	//			greenend += mgreen2 * dy2;
	//			bluestart += mblue1 * dy2;
	//			blueend += mblue2 * dy2;
	//			y = y2;
	//		} else if (y < 0) {
	//			xstart -= mx1 * y;
	//			xend -= mx2 * y;
	//			zstart -= mz1 * y;
	//			zend -= mz2 * y;
	//			redstart -= mred1 * y;
	//			redend -= mred2 * y;
	//			greenstart -= mgreen1 * y;
	//			greenend -= mgreen2 * y;
	//			bluestart -= mblue1 * y;
	//			blueend -= mblue2 * y;
	//			y = 0;
	//		}
	//		yend = (y2 < (iHeight << 16)) ? y2 : iHeight << 16;
	//		index = (y >> 16) * iWidth;
	//		while (y < yend) {
	//			//System.out.println(y);
	//			if (xstart < xend) {
	//				left = xstart >> 16;
	//				right = xend >> 16;
	//				z = zstart;
	//				dz = zend - zstart;
	//				red = redstart;
	//				dred = redend - redstart;
	//				green = greenstart;
	//				dgreen = greenend - greenstart;
	//				blue = bluestart;
	//				dblue = blueend - bluestart;
	//			} else {
	//				left = xend >> 16;
	//				right = xstart >> 16;
	//				z = zend;
	//				dz = zstart - zend;
	//				red = redend;
	//				dred = redstart-redend;
	//				green = greenend;
	//				dgreen = greenstart-greenend;
	//				blue = blueend;
	//				dblue = bluestart-blueend;
	//			}
	//			if (left != right) {
	//				dz /= (right - left);
	//				dred /= (right - left);
	//				dgreen /= (right - left);
	//				dblue /= (right - left);
	//				if (left < 0) {
	//					z -= left * dz;
	//					red -= left * dred;
	//					green -= left * dgreen;
	//					blue -= left * dblue;
	//					left = 0;
	//				}
	//				if (right > iWidth) {
	//					right = iWidth;
	//				}
	//				for (i = left; i < right; i++) {
	//					if (z < aiActiveZBuffer[index + i]) {
	//						aiActiveFrameBuffer[index + i] = 0xFF000000 | ((red & 0xFF00)<<8) | (green & 0xFF00) | (blue >> 8);
	//						aiActiveZBuffer[index + i] = z;
	//					}
	//					z += dz;
	//					red += dred;
	//					green += dgreen;
	//					blue += dblue;
	//					}
	//			}
	//			xstart += mx1;
	//			zstart += mz1;
	//			redstart += mred1;
	//			greenstart += mgreen1;
	//			bluestart += mblue1;
	//			xend += mx2;
	//			zend += mz2;
	//			redend += mred2;
	//			greenend += mgreen2;
	//			blueend += mblue2;
	//			index += iWidth;
	//			y += 65536;
	//		}
	//	}
	//	dx2 = x3 - x2;
	//	dy2 = (y3 - y2) >> 16;
	//	dz2 = z3 - z2;
	//	dred2 = red3 - red2;
	//	dgreen2 = green3 - green2;
	//	dblue2 = blue3 - blue2;
	//	if (dy2 != 0) {
	//		mx2 = dx2 / dy2;
	//		mz2 = dz2 / dy2;
	//		mred2 = dred2 / dy2;
	//		mgreen2 = dgreen2 / dy2;
	//		mblue2 = dblue2 / dy2;
	//		xend = x2;
	//		zend = z2;
	//		redend = red2;
	//		greenend = green2;
	//		blueend = blue2;
	//		if (y < 0) {
	//			xstart -= mx1 * y;
	//			xend -= mx2 * y;
	//			zstart -= mz1 * y;
	//			zend -= mz2 * y;
	//			redstart -= mred1 * y;
	//			redend -= mred2 * y;
	//			greenstart -= mgreen1 * y;
	//			greenend -= mgreen2 * y;
	//			bluestart -= mblue1 * y;
	//			blueend -= mblue2 * y;
	//			y = 0;
	//		}
	//		yend = (y3 < (iHeight << 16)) ? y3 : iHeight << 16;
	//		index = (y >> 16) * iWidth;
	//		while (y < yend) {
	//			//System.out.println(y);
	//			if (xstart < xend) {
	//				left = xstart >> 16;
	//				right = xend >> 16;
	//				z = zstart;
	//				dz = zend - zstart;
	//				red = redstart;
	//				dred = redend - redstart;
	//				green = greenstart;
	//				dgreen = greenend - greenstart;
	//				blue = bluestart;
	//				dblue = blueend - bluestart;
	//			} else {
	//				left = xend >> 16;
	//				right = xstart >> 16;
	//				z = zend;
	//				dz = zstart - zend;
	//				red = redend;
	//				dred = redstart - redend;
	//				green = greenend;
	//				dgreen = greenstart - greenend;
	//				blue = blueend;
	//				dblue = bluestart - blueend;
	//			}
	//			if (left != right) {
	//				dz /= (right - left);
	//				dred /= (right - left);
	//				dgreen /= (right - left);
	//				dblue /= (right - left);
	//				if (left < 0) {
	//					z -= left * dz;
	//					red -= left * dred;
	//					green -= left * dgreen;
	//					blue -= left * dblue;
	//					left = 0;
	//				}
	//				if (right > iWidth) {
	//					right = iWidth;
	//				}
	//				for (i = left; i < right; i++) {
	//					if (z < aiActiveZBuffer[index + i]) {
	//						aiActiveFrameBuffer[index + i] = 0xFF000000 | ((red & 0xFF00)<<8) | (green & 0xFF00) | (blue >> 8);
	//						aiActiveZBuffer[index + i] = z;
	//					}
	//					z += dz;
	//					red += dred;
	//					green += dgreen;
	//					blue += dblue;
	//				}
	//			}
	//			xstart += mx1;
	//			zstart += mz1;
	//			redstart += mred1;
	//			greenstart += mgreen1;
	//			bluestart += mblue1;
	//			xend += mx2;
	//			zend += mz2;
	//			redend += mred2;
	//			greenend += mgreen2;
	//			blueend += mblue2;
	//			index += iWidth;
	//			y += 65536;
	//		}
	//	}
	//}
	
	protected final void draw3DTriangleGouradTransparent(Point3f p1, Point3f p2, Point3f p3, int color1, int color2, int color3, int transparency) {
		if (bCulling && (p2.x - p1.x) * (p3.y - p1.y) - (p2.y - p1.y) * (p3.x - p1.x) > 0) return;
		int opacity = 255 - transparency;
		int x1, y1, z1, x2, y2, z2, x3, y3, z3;
		int dx1, dx2, dy1, dy2, dz1, dz2, mx1, mx2, mz1, mz2;
		int xstart, xend, yend, zstart, zend, y, z, dz, left, right, i, index;
		int red1, green1, blue1, red2, green2, blue2, red3, green3, blue3;
		int dred1, dred2, dgreen1, dgreen2, dblue1, dblue2, mred1, mred2, mgreen1, mgreen2, mblue1, mblue2;
		int redstart, redend, greenstart, greenend, bluestart, blueend, red, green, blue, dred, dgreen, dblue;
		if (p1.y <= p2.y && p1.y <= p3.y) {
			//x1 = ((int) p1.x) << 16;
			x1 = (int) (p1.x * 65536);
			y1 = ((int) p1.y);
			z1 = (int)((p1.z + fPolygonOffset) * 65536);
			red1 = (color1 & 0x00FF0000) >> 8;
			green1 = (color1 & 0x0000FF00);
			blue1 = (color1 & 0x000000FF) << 8;
			if (p2.y < p3.y) {
				//x2 = ((int) p2.x) << 16;
				x2 = (int) (p2.x * 65536);
				y2 = ((int) p2.y);
				z2 = (int) ((p2.z + fPolygonOffset) * 65536);
				red2 = (color2 & 0x00FF0000) >> 8;
				green2 = (color2 & 0x0000FF00);
				blue2 = (color2 & 0x000000FF) << 8;
				//x3 = ((int) p3.x) << 16;
				x3 = (int) (p3.x * 65536);
				y3 = ((int) p3.y);
				z3 = (int) ((p3.z + fPolygonOffset) *65536);
				red3 = (color3 & 0x00FF0000) >> 8;
				green3 = (color3 & 0x0000FF00);
				blue3 = (color3 & 0x000000FF) << 8;
			} else {
				//x2 = ((int) p3.x) << 16;
				x2 = (int) (p3.x * 65536);
				y2 = ((int) p3.y);
				z2 = (int) ((p3.z + fPolygonOffset) * 65536);
				red2 = (color3 & 0x00FF0000) >> 8;
				green2 = (color3 & 0x0000FF00);
				blue2 = (color3 & 0x000000FF) << 8;
				//x3 = ((int) p2.x) << 16;
				x3 = (int) (p2.x * 65536);
				y3 = ((int) p2.y);
				z3 = (int) ((p2.z + fPolygonOffset) *65536);
				red3 = (color2 & 0x00FF0000) >> 8;
				green3 = (color2 & 0x0000FF00);
				blue3 = (color2 & 0x000000FF) << 8;
			}
		} else if (p2.y <= p1.y && p2.y <= p3.y) {
			//x1 = ((int) p2.x) << 16;
			x1 = (int) (p2.x * 65536);
			y1 = ((int) p2.y);
			z1 = (int) ((p2.z + fPolygonOffset) * 65536);
			red1 = (color2 & 0x00FF0000) >> 8;
			green1 = (color2 & 0x0000FF00);
			blue1 = (color2 & 0x000000FF) << 8;
			if (p1.y < p3.y) {
				//x2 = ((int) p1.x) << 16;
				x2 = (int) (p1.x * 65536);
				y2 = ((int) p1.y);
				z2 = (int) ((p1.z + fPolygonOffset) * 65536);
				red2 = (color1 & 0x00FF0000) >> 8;
				green2 = (color1 & 0x0000FF00);
				blue2 = (color1 & 0x000000FF) << 8;
				//x3 = ((int) p3.x) << 16;
				x3 = (int) (p3.x * 65536);
				y3 = ((int) p3.y);
				z3 = (int) ((p3.z + fPolygonOffset) * 65536);
				red3 = (color3 & 0x00FF0000) >> 8;
				green3 = (color3 & 0x0000FF00);
				blue3 = (color3 & 0x000000FF) << 8;
			} else {
				//x2 = ((int) p3.x) << 16;
				x2 = (int) (p3.x * 65536);
				y2 = ((int) p3.y);
				z2 = (int) ((p3.z + fPolygonOffset) * 65536);
				red2 = (color3 & 0x00FF0000) >> 8;
				green2 = (color3 & 0x0000FF00);
				blue2 = (color3 & 0x000000FF) << 8;
				//x3 = ((int) p1.x) << 16;
				x3 = (int) (p1.x * 65536);
				y3 = ((int) p1.y);
				z3 = (int) ((p1.z + fPolygonOffset) * 65536);
				red3 = (color1 & 0x00FF0000) >> 8;
				green3 = (color1 & 0x0000FF00);
				blue3 = (color1 & 0x000000FF) << 8;
			}
		} else {
			//x1 = ((int) p3.x) << 16;
			x1 = (int) (p3.x * 65536);
			y1 = ((int) p3.y);
			z1 = (int) ((p3.z + fPolygonOffset) * 65536);
			red1 = (color3 & 0x00FF0000) >> 8;
			green1 = (color3 & 0x0000FF00);
			blue1 = (color3 & 0x000000FF) << 8;
			if (p1.y < p2.y) {
				//x2 = ((int) p1.x) << 16;
				x2 = (int) (p1.x * 65536);
				y2 = ((int) p1.y);
				z2 = (int) ((p1.z + fPolygonOffset) * 65536);
				red2 = (color1 & 0x00FF0000) >> 8;
				green2 = (color1 & 0x0000FF00);
				blue2 = (color1 & 0x000000FF) << 8;
				//x3 = ((int) p2.x) << 16;
				x3 = (int) (p2.x * 65536);
				y3 = ((int) p2.y);
				z3 = (int) ((p2.z + fPolygonOffset) * 65536);
				red3 = (color2 & 0x00FF0000) >> 8 ;
				green3 = (color2 & 0x0000FF00);
				blue3 = (color2 & 0x000000FF) << 8;
			} else {
				//x2 = ((int) p2.x) << 16;
				x2 = (int) (p2.x * 65536);
				y2 = ((int) p2.y);
				z2 = (int) ((p2.z + fPolygonOffset) * 65536);
				red2 = (color2 & 0x00FF0000) >> 8;
				green2 = (color2 & 0x0000FF00);
				blue2 = (color2 & 0x000000FF) << 8;
				//x3 = ((int) p1.x) << 16;
				x3 = (int) (p1.x * 65536);
				y3 = ((int) p1.y);
				z3 = (int) ((p1.z + fPolygonOffset) * 65536);
				red3 = (color1 & 0x00FF0000) >> 8;
				green3 = (color1 & 0x0000FF00);
				blue3 = (color1 & 0x000000FF) << 8;
			}
		}
		dx1 = x3 - x1;
		dy1 = y3 - y1;
		dz1 = z3 - z1;
		dred1 = red3 - red1;
		dgreen1 = green3 - green1;
		dblue1 = blue3 - blue1;
		if (dy1 == 0) {
			return;
		}
		dx2 = x2 - x1;
		dy2 = y2 - y1;
		dz2 = z2 - z1;
		dred2 = red2 - red1;
		dgreen2 = green2 - green1;
		dblue2 = blue2 - blue1;
		mx1 = dx1 / dy1;
		mz1 = dz1 / dy1;
		mred1 = dred1 / dy1;
		mgreen1 = dgreen1 / dy1;
		mblue1 = dblue1 / dy1;
		xstart = xend = x1;
		zstart = zend = z1;
		redstart = redend = red1;
		greenstart = greenend = green1;
		bluestart = blueend = blue1;
		y = y1;
		if (dy2 != 0) {
			mx2 = dx2 / dy2;
			mz2 = dz2 / dy2;
			mred2 = dred2 / dy2;
			mgreen2 = dgreen2 / dy2;
			mblue2 = dblue2 / dy2;
			if (y2 < 0) {
				xstart += mx1 * dy2;
				xend += mx2 * dy2;
				zstart += mz1 * dy2;
				zend += mz2 * dy2;
				redstart += mred1 * dy2;
				redend += mred2 * dy2;
				greenstart += mgreen1 * dy2;
				greenend += mgreen2 * dy2;
				bluestart += mblue1 * dy2;
				blueend += mblue2 * dy2;
				y = y2;
			} else if (y < 0) {
				xstart -= mx1 * y;
				xend -= mx2 * y;
				zstart -= mz1 * y;
				zend -= mz2 * y;
				redstart -= mred1 * y;
				redend -= mred2 * y;
				greenstart -= mgreen1 * y;
				greenend -= mgreen2 * y;
				bluestart -= mblue1 * y;
				blueend -= mblue2 * y;
				y = 0;
			}
			yend = (y2 < iHeight) ? y2 : iHeight;
			index = y * iWidth;
			while (y < yend) {
				if (xstart < xend) {
					left = xstart >> 16;
					right = xend >> 16;
					z = zstart;
					dz = zend - zstart;
					red = redstart;
					dred = redend - redstart;
					green = greenstart;
					dgreen = greenend - greenstart;
					blue = bluestart;
					dblue = blueend - bluestart;
				} else {
					left = xend >> 16;
					right = xstart >> 16;
					z = zend;
					dz = zstart - zend;
					red = redend;
					dred = redstart-redend;
					green = greenend;
					dgreen = greenstart-greenend;
					blue = blueend;
					dblue = bluestart-blueend;
				}
				if (left != right) {
					dz /= (right - left);
					dred /= (right - left);
					dgreen /= (right - left);
					dblue /= (right - left);
					if (left < 0) {
						z -= left * dz;
						red -= left * dred;
						green -= left * dgreen;
						blue -= left * dblue;
						left = 0;
					}
					if (right > iWidth) {
						right = iWidth;
					}
					for (i = left; i < right; i++) {
						if (z < aiActiveZBuffer[index + i]) {
							//aiActiveFrameBuffer[index + i] = 0xFF000000 | ((red & 0xFF00)<<8) | (green & 0xFF00) | (blue >> 8);
							//aiActiveZBuffer[index + i] = z;
							int fb = aiActiveFrameBuffer[index + i];
							int fbRed = (((fb & 0x00FF0000) * transparency) >> 8) & 0x00FF0000;
							int fbGreen = (((fb & 0x0000FF00) * transparency) >> 8) & 0x0000FF00;
							int fbBlue = (((fb & 0x000000FF) * transparency) >> 8) & 0x000000FF;
							fbRed += ((red * opacity) & 0xFF0000);
							fbGreen += (((green * opacity) & 0xFF0000) >> 8);
							fbBlue += (((blue * opacity) & 0xFF0000) >> 16);
							aiActiveFrameBuffer[index + i] = 0xFF000000 | fbRed | fbGreen | fbBlue;
						}
						z += dz;
						red += dred;
						green += dgreen;
						blue += dblue;
						}
				}
				xstart += mx1;
				zstart += mz1;
				redstart += mred1;
				greenstart += mgreen1;
				bluestart += mblue1;
				xend += mx2;
				zend += mz2;
				redend += mred2;
				greenend += mgreen2;
				blueend += mblue2;
				index += iWidth;
				y++;
			}
		}
		dx2 = x3 - x2;
		dy2 = y3 - y2;
		dz2 = z3 - z2;
		dred2 = red3 - red2;
		dgreen2 = green3 - green2;
		dblue2 = blue3 - blue2;
		if (dy2 != 0) {
			mx2 = dx2 / dy2;
			mz2 = dz2 / dy2;
			mred2 = dred2 / dy2;
			mgreen2 = dgreen2 / dy2;
			mblue2 = dblue2 / dy2;
			xend = x2;
			zend = z2;
			redend = red2;
			greenend = green2;
			blueend = blue2;
			if (y < 0) {
				xstart -= mx1 * y;
				xend -= mx2 * y;
				zstart -= mz1 * y;
				zend -= mz2 * y;
				redstart -= mred1 * y;
				redend -= mred2 * y;
				greenstart -= mgreen1 * y;
				greenend -= mgreen2 * y;
				bluestart -= mblue1 * y;
				blueend -= mblue2 * y;
				y = 0;
			}
			yend = (y3 < iHeight) ? y3 : iHeight;
			index = y * iWidth;
			while (y < yend) {
				if (xstart < xend) {
					left = xstart >> 16;
					right = xend >> 16;
					z = zstart;
					dz = zend - zstart;
					red = redstart;
					dred = redend - redstart;
					green = greenstart;
					dgreen = greenend - greenstart;
					blue = bluestart;
					dblue = blueend - bluestart;
				} else {
					left = xend >> 16;
					right = xstart >> 16;
					z = zend;
					dz = zstart - zend;
					red = redend;
					dred = redstart - redend;
					green = greenend;
					dgreen = greenstart - greenend;
					blue = blueend;
					dblue = bluestart - blueend;
				}
				if (left != right) {
					dz /= (right - left);
					dred /= (right - left);
					dgreen /= (right - left);
					dblue /= (right - left);
					if (left < 0) {
						z -= left * dz;
						red -= left * dred;
						green -= left * dgreen;
						blue -= left * dblue;
						left = 0;
					}
					if (right > iWidth) {
						right = iWidth;
					}
					for (i = left; i < right; i++) {
						if (z < aiActiveZBuffer[index + i]) {
							//aiActiveFrameBuffer[index + i] = 0xFF000000 | ((red & 0xFF00)<<8) | (green & 0xFF00) | (blue >> 8);
							//aiActiveZBuffer[index + i] = z;
							int fb = aiActiveFrameBuffer[index + i];
							int fbRed = (((fb & 0x00FF0000) * transparency) >> 8) & 0x00FF0000;
							int fbGreen = (((fb & 0x0000FF00) * transparency) >> 8) & 0x0000FF00;
							int fbBlue = (((fb & 0x000000FF) * transparency) >> 8) & 0x000000FF;
							fbRed += ((red * opacity) & 0xFF0000);
							fbGreen += ((green * opacity) & 0xFF0000) >> 8;
							fbBlue += ((blue * opacity) & 0xFF0000) >> 16;
							aiActiveFrameBuffer[index + i] = 0xFF000000 | fbRed | fbGreen | fbBlue;
						}
						z += dz;
						red += dred;
						green += dgreen;
						blue += dblue;
					}
				}
				xstart += mx1;
				zstart += mz1;
				redstart += mred1;
				greenstart += mgreen1;
				bluestart += mblue1;
				xend += mx2;
				zend += mz2;
				redend += mred2;
				greenend += mgreen2;
				blueend += mblue2;
				index += iWidth;
				y++;
			}
		}
	}
}
