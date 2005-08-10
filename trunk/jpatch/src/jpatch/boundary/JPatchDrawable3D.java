package jpatch.boundary;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;

import javax.swing.*;
import javax.vecmath.*;

import jpatch.entity.*;

public final class JPatchDrawable3D implements JPatchDrawable2 {
	
	private static final byte SUB_PIXEL_BITS = 16;
	private static final int SUB_PIXEL_MULTIPLIER = 65536;
	private static final int SUB_PIXEL_MASK_1 = 0xffff;
	private static final float POLYGON_OFFSET = 1;
	
	private JPatchDrawableEventListener listener;
	private Component component;
	private Graphics2D g;
	//private Matrix4f m4Transform = new Matrix4f();
	private boolean bPerspective = false;
	private float fFocalLength = 50;
	private float fW;
	private float fNearClip = 1;
	private int iPointSize = 3;
	private int iXoff = 0;
	private int iYoff = 0;
	private BufferedImage image;
	private int[] aiDepthBuffer;
	private int[] aiColorBuffer;
	private int iWidth, iHeight;
	private int iColor;
	
//	private Point3f A = new Point3f();
//	private Point3f B = new Point3f();
//	private Point3f C = new Point3f();
//	private Point3f D = new Point3f();
//	private Point3f E = new Point3f();
	private Point3f Pab = new Point3f();
	private Point3f Pbc = new Point3f();
	private Point3f Pca = new Point3f();
	private Color3f Cab = new Color3f();
	private Color3f Cbc = new Color3f();
	private Color3f Cca = new Color3f();
	
	public JPatchDrawable3D(final JPatchDrawableEventListener listener, boolean lightweight) {
		this.listener = listener;
		if (lightweight) {
			component = new JPanel() {
				private static final long serialVersionUID = 1L;
				public void paint(Graphics g) {
					if (image == null || image.getWidth(this) != getWidth() || image.getHeight(this) != getHeight()) {
						updateImage();
					}
					g.drawImage(image, 0, 0, this);
				}
				public void update(Graphics g) {
					paint(g);
				}
				public String toString() {
					return "Viewport2D JPanel @" + hashCode();
				}
			};
		} else {
			component = new Canvas() {
				private static final long serialVersionUID = 1L;
				public void paint(Graphics g) {
					if (image == null || image.getWidth(this) != getWidth() || image.getHeight(this) != getHeight()) {
						updateImage();
					}
					g.drawImage(image, 0, 0, this);
				}
				public void update(Graphics g) {
					paint(g);
				}
				public String toString() {
					return "Viewport2D Canvas @" + hashCode();
				}
			};
		}
		
//		component.addComponentListener(new ComponentAdapter() {
//			public void componentResized(ComponentEvent e) {
//				iXoff = e.getComponent().getWidth() >> 1;
//				iYoff = e.getComponent().getHeight() >> 1;
//				fW = fFocalLength * iXoff / 12.5f;
//			}
//		});
	}
	
	private void updateImage() {
		image = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_RGB);
		aiColorBuffer = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		aiDepthBuffer = new int[aiColorBuffer.length];
		g = image.createGraphics();
		iWidth = image.getWidth();
		iHeight = image.getHeight();
		iXoff = iWidth >> 1;
		iYoff = iHeight >> 1;
		fW = fFocalLength * iXoff / 12.5f;
	}
	
	public Component getComponent() {
		return component;
	}
	
	public void drawImage(BufferedImage image, int x, int y, float scaleX, float scaleY) {
		AffineTransform affineTransform = new AffineTransform(scaleX, 0, 0, scaleY, x, y);
		g.drawImage(image, affineTransform, null);
	}
	
	public void drawString(String string, int x, int y) {
		g.drawString(string, x, y);
	}
	
	public void display() {
		if (component.getWidth() <= 0 || component.getHeight() <= 0)
			return;
		if (g == null || image == null) updateImage();
		listener.display(JPatchDrawable3D.this);
		Graphics cg = component.getGraphics();
		if (cg != null) component.paint(cg);
	}
	
	public void clear(int mode, Color3f color) {
		int col = color.get().getRGB();
		for (int i = 0, n = aiColorBuffer.length; i < n; i++) {
			aiColorBuffer[i] = col;
			aiDepthBuffer[i] = Integer.MAX_VALUE;
		}
//		if ((mode & COLOR_BUFFER) != 0) {
//			g.fillRect(0, 0, component.getWidth(), component.getHeight());
//		}
	}
	
	public void setProjection(int projection) {
		bPerspective = (projection == PERSPECTIVE);
	}
	
	public void setClipping(float near, float far) {
		fNearClip = near;
	}
	
	//public void setTransformation(Matrix4f transform) {
	//	m4Transform.set(transform);
	//}
	
	public void setFocalLength(float focalLength) {
		fFocalLength = focalLength;
		fW = fFocalLength * iXoff / 35f;
	}
	
	public void setGhostRenderingEnabled(boolean enable) { }
	
	public void setTransparentRenderingEnabled(boolean enable) { }
	
	public void setLightingEnable(boolean enable) { }
	
	public void setColor(Color3f color) {
		iColor = color.get().getRGB();
	}
	
	public void setColor(Color4f color) {
		//g.setColor(color.get());
	}
	
	public void setMaterial(MaterialProperties mp) {
		throw new UnsupportedOperationException(this.getClass().getName() + " does not support lighting.");
	}
	
	public void setLighting(RealtimeLighting lighting) {
		throw new UnsupportedOperationException(this.getClass().getName() + " does not support lighting.");
	}
	
	public void setTransform(Matrix4f transform) {
		throw new UnsupportedOperationException(this.getClass().getName() + " does not support transform.");
	}
	
	public void setPointSize(int size) {
		iPointSize = size;
	}
	
	public void drawPoint(Point3f p) {
		int x, y, z;
		if (bPerspective) {
			if (p.z > fNearClip) {
				x = iXoff + (int) ((p.x / p.z) * fW);
				y = iYoff - (int) ((p.y / p.z) * fW);
				z = (int) (-Integer.MAX_VALUE / p.z);
			} else {
				x = iXoff + (int) p.x;
				y = iYoff - (int) p.y;
				z = (int) (p.z * 65536);
			}
			int x1 = x - (iPointSize >> 1);
			int y1 = y - (iPointSize >> 1);
			int x2 = x1 + iPointSize;
			int y2 = y1 + iPointSize;
			if ((x1 < 0 && x2 < 0 )|| (y1 < 0 && y2 < 0) || (x1 >= iWidth && x2 >= iWidth) || (y1 >= iHeight && y2 >= iHeight)) {
				return;
			}
			
			if (x1 < 0) x1 = 0;
			if (y1 < 0) y1 = 0;
			if (x2 >= iWidth) x2 = iWidth - 1;
			if (y2 >= iHeight) y2 = iHeight - 1;
			int index = y1 * iWidth + x1;
			for (y = y1; y < y2; y++) {
				for (x = x1; x < x2; x++) {
					if (z < aiDepthBuffer[index]) {
						aiDepthBuffer[index] = iColor;
						aiColorBuffer[index] = z;
					}
					index++;
				}
				index += iWidth - iPointSize;
			}
			
		}	
	}
	
	public void drawLine(Point3f p0, Point3f p1) {
		if (bPerspective) {
			if (p0.z < fNearClip) {
				if (p1.z < fNearClip) return;
				float s = (p0.z - fNearClip) / (p1.z - p0.z);
				int x = (int) ((p0.x * (1 - s) + p1.x * s) * fW);
				int y = (int) ((p0.y * (1 - s) + p1.y * s) * fW);
				int xx = (int) ((p1.x / p1.z) * fW);
				int yy = (int) ((p1.y / p1.z) * fW);
				drawLine(iXoff + x, iYoff - y, (int) (-Integer.MAX_VALUE / fNearClip), iXoff + xx, iYoff - yy, (int) (-Integer.MAX_VALUE / p1.z));
			} else if (p1.z < fNearClip) {
				float s = (p1.z - fNearClip) / (p0.z - p1.z);
				int x = (int) ((p1.x * (1 - s) + p0.x * s) * fW);
				int y = (int) ((p1.y * (1 - s) + p0.y * s) * fW);
				int xx = (int) ((p0.x / p0.z) * fW);
				int yy = (int) ((p0.y / p0.z) * fW);
				drawLine(iXoff + x, iYoff - y, (int) (-Integer.MAX_VALUE / p0.z), iXoff + xx, iYoff - yy, (int) (-Integer.MAX_VALUE / fNearClip));
			} else {
				int x = (int) ((p0.x / p0.z) * fW);
				int y = (int) ((p0.y / p0.z) * fW);
				int xx = (int) ((p1.x / p1.z) * fW);
				int yy = (int) ((p1.y / p1.z) * fW);
				drawLine(iXoff + x, iYoff - y, (int) (-Integer.MAX_VALUE / p0.z), iXoff + xx, iYoff - yy, (int) (-Integer.MAX_VALUE / p1.z));
			}
		} else 
			drawLine((int) (iXoff + p0.x), (int) (iYoff - p0.y), (int) (p0.z * 65536), (int) (iXoff + p1.x), (int) (iYoff - p1.y), (int) (p1.z * 65536));
	}
	
	
	private void drawScreenTriangle(Point3f p1, Point3f p2, Point3f p3) {
		if ((p2.x - p1.x) * (p3.y - p1.y) - (p2.y - p1.y) * (p3.x - p1.x) > 0) {
//			if (bCulling) return;
//			if (bMark) color ^= 0xffffff;
		}
		int x1, y1, z1, x2, y2, z2, x3, y3, z3;
		x1 = (int) (p1.x * SUB_PIXEL_MULTIPLIER);
		y1 = (int) (p1.y * SUB_PIXEL_MULTIPLIER);
		z1 = (int) ((p1.z + POLYGON_OFFSET) * SUB_PIXEL_MULTIPLIER);
		x2 = (int) (p2.x * SUB_PIXEL_MULTIPLIER);
		y2 = (int) (p2.y * SUB_PIXEL_MULTIPLIER);
		z2 = (int) ((p2.z + POLYGON_OFFSET) * SUB_PIXEL_MULTIPLIER);
		x3 = (int) (p3.x * SUB_PIXEL_MULTIPLIER);
		y3 = (int) (p3.y * SUB_PIXEL_MULTIPLIER);
		z3 = (int) ((p3.z + POLYGON_OFFSET) * SUB_PIXEL_MULTIPLIER);
		if (y1 < y2) {
			if (y2 < y3) drawTriangle(x1, y1, z1, x2, y2, z2, x3, y3, z3);
			else if (y1 < y3) drawTriangle(x1, y1, z1, x3, y3, z3, x2, y2, z2);
			else drawTriangle(x3, y3, z3, x1, y1, z1, x2, y2, z2);
		} else {
			if (y1 < y3) drawTriangle(x2, y2, z2, x1, y1, z1, x3, y3, z3);
			else if (y2 < y3) drawTriangle(x2, y2, z2, x3, y3, z3, x1, y1, z1);
			else drawTriangle(x3, y3, z3, x2, y2, z2, x1, y1, z1);
		}
	}
	
	private void drawScreenTriangle(Point3f p1, Color3f c1, Point3f p2, Color3f c2, Point3f p3, Color3f c3) {
		int x1, y1, z1, x2, y2, z2, x3, y3, z3;
		int r1, g1, b1, r2, g2, b2, r3, g3, b3;
		
		if (bPerspective) {
			x1 = (int) ((iXoff + p1.x / p1.z * fW) * SUB_PIXEL_MULTIPLIER);
			y1 = (int) ((iYoff - p1.y / p1.z * fW) * SUB_PIXEL_MULTIPLIER);
			z1 = (int) (-Integer.MAX_VALUE / (p1.z + POLYGON_OFFSET));
			x2 = (int) ((iXoff + p2.x / p2.z * fW) * SUB_PIXEL_MULTIPLIER);
			y2 = (int) ((iYoff - p2.y / p2.z * fW) * SUB_PIXEL_MULTIPLIER);
			z2 = (int) (-Integer.MAX_VALUE / (p2.z + POLYGON_OFFSET));
			x3 = (int) ((iXoff + p3.x / p3.z * fW) * SUB_PIXEL_MULTIPLIER);
			y3 = (int) ((iYoff - p3.y / p3.z * fW) * SUB_PIXEL_MULTIPLIER);
			z3 = (int) (-Integer.MAX_VALUE / (p3.z + POLYGON_OFFSET));
		} else {
			x1 = (int) (p1.x * SUB_PIXEL_MULTIPLIER);
			y1 = (int) (p1.y * SUB_PIXEL_MULTIPLIER);
			z1 = (int) ((p1.z + POLYGON_OFFSET) * SUB_PIXEL_MULTIPLIER);
			x2 = (int) (p2.x * SUB_PIXEL_MULTIPLIER);
			y2 = (int) (p2.y * SUB_PIXEL_MULTIPLIER);
			z2 = (int) ((p2.z + POLYGON_OFFSET) * SUB_PIXEL_MULTIPLIER);
			x3 = (int) (p3.x * SUB_PIXEL_MULTIPLIER);
			y3 = (int) (p3.y * SUB_PIXEL_MULTIPLIER);
			z3 = (int) ((p3.z + POLYGON_OFFSET) * SUB_PIXEL_MULTIPLIER);
		}
		
		r1 = (int) (c1.x * 255) << 16;
		g1 = (int) (c1.y * 255) << 16;
		b1 = (int) (c1.z * 255) << 16;
		r2 = (int) (c2.x * 255) << 16;
		g2 = (int) (c2.y * 255) << 16;
		b2 = (int) (c2.z * 255) << 16;
		r3 = (int) (c3.x * 255) << 16;
		g3 = (int) (c3.y * 255) << 16;
		b3 = (int) (c3.z * 255) << 16;
		if (r1 < 0) r1 = 0;
		if (r2 < 0) r2 = 0;
		if (r3 < 0) r3 = 0;
		if (g1 < 0) g1 = 0;
		if (g2 < 0) g2 = 0;
		if (g3 < 0) g3 = 0;
		if (b1 < 0) b1 = 0;
		if (b2 < 0) b2 = 0;
		if (b3 < 0) b3 = 0;
		if (r1 > 16777216) r1 = 16777216;
		if (r2 > 16777216) r2 = 16777216;
		if (r3 > 16777216) r3 = 16777216;
		if (g1 > 16777216) g1 = 16777216;
		if (g2 > 16777216) g2 = 16777216;
		if (g3 > 16777216) g3 = 16777216;
		if (b1 > 16777216) b1 = 16777216;
		if (b2 > 16777216) b2 = 16777216;
		if (b3 > 16777216) b3 = 16777216;
		
		if (y1 < y2) {
			if (y2 < y3) drawTriangle(x1, y1, z1, r1, g1, b1, x2, y2, z2, r2, g2, b2, x3, y3, z3, r3, g3, b3);
			else if (y1 < y3) drawTriangle(x1, y1, z1, r1, g1, b1, x3, y3, z3, r3, g3, b3, x2, y2, z2, r2, g2, b2);
			else drawTriangle(x3, y3, z3, r3, g3, b3, x1, y1, z1, r1, g1, b1, x2, y2, z2, r2, g2, b2);
		} else {
			if (y1 < y3) drawTriangle(x2, y2, z2, r2, g2, b2, x1, y1, z1, r1, g1, b1, x3, y3, z3, r3, g3, b3);
			else if (y2 < y3) drawTriangle(x2, y2, z2, r2, g2, b2, x3, y3, z3, r3, g3, b3, x1, y1, z1, r1, g1, b1);
			else drawTriangle(x3, y3, z3, r3, g3, b3, x2, y2, z2, r2, g2, b2, x1, y1, z1, r1, g1, b1);
		}
	}
	
	public void drawTriangle(Point3f p0, Color4f c0, Point3f p1, Color4f c1, Point3f p2, Color4f c2) {
		//FIXME
	}
	
	public void drawTriangle(Point3f p0, Vector3f n0, Point3f p1, Vector3f n1, Point3f p2, Vector3f n2) {
		drawTriangle(p0, p1, p2);
	}
	
	public void drawCurve(Curve curve) { }
	
	public boolean isShadingSupported() {
		return true;
	}
	
	public boolean isDepthBufferSupported() {
		return true;
	}
	
	public boolean isLightingSupported() {
		return false;
	}
	
	public boolean isTransformSupported() {
		return false;
	}
	
	public Graphics getGraphics() {
		return g;
	}
	
	private final void drawLine(int x1, int y1, int z1, int x2, int y2, int z2) {
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
					if (z <= aiDepthBuffer[index]) {
						aiColorBuffer[index] = iColor;
						aiDepthBuffer[index] = z;
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
					if (z < aiDepthBuffer[index]) {
						aiColorBuffer[index] = iColor;
						aiDepthBuffer[index] = z;
					}
				}
				y++;
				x += dx;
				z += dz;
			}
		}
	}
	
	private final void drawTriangle(
			int x1, int y1, int z1, int r1, int g1, int b1,
			int x2, int y2, int z2, int r2, int g2, int b2,
			int x3, int y3, int z3, int r3, int g3, int b3) {
		
		//System.out.println(z1 + " " + z2 + " " + z3);
		int[] frameBuffer = aiColorBuffer;
		int[] zBuffer = aiDepthBuffer;
			
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

	private final void drawTriangle(int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3) {
		int[] frameBuffer = aiColorBuffer;
		int[] zBuffer = aiDepthBuffer;
		
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
							frameBuffer[index] = iColor;
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
							frameBuffer[index] = iColor;
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
	
	public final void drawTriangle(Point3f pa, Point3f pb, Point3f pc) { //, Vector3d na, Vector3d nb, Vector3d nc) {
//		A.x = -pa.x / pa.z * fW + iXoff;
//		A.y = pa.y / pa.z * fW + iYoff;
//		B.x = -pb.x / pb.z * fW + iXoff;
//		B.y = pb.y / pb.z * fW + iYoff;
//		C.x = -pc.x / pc.z * fW + iXoff;
//		C.y = pc.y / pc.z * fW + iYoff;
//		if (A.x >= 0 && A.x < iWidth && A.y >= 0 && A.y < iHeight || B.x >= 0 && B.x < iWidth && B.y >= 0 && B.y < iHeight || C.x >= 0 && C.x < iWidth && C.y >= 0 && C.y < iHeight) {
//			if (A.y < B.y && A.y < C.y) {
//				if (B.y < C.y) {
//					float f = (B.y - A.y) / (C.y - A.y);
//					pd.interpolate(pa,pc,f);
//					//nd.interpolate(na,nc,f);
//					D.x = -pd.x / pd.z * fW + iXoff;
//					D.y = pd.y / pd.z * fW + iYoff;
//					if (B.x < D.x) {
//						drawScreenTriangle(A, B, D);
//						drawScreenTriangle(C, B, D);
//					} else {
//						drawScreenTriangle(A, D, B);
//						drawScreenTriangle(C, D, B);
//					}
//				} else if (B.y > C.y) {
//					float f = (C.y - A.y) / (B.y - A.y);
//					pd.interpolate(pa,pb,f);
//					//nd.interpolate(na,nb,f);
//					D.x = -pd.x / pd.z * fW + iXoff;
//					D.y = pd.y / pd.z * fW + iYoff;
//					if (C.x < D.x) {
//						drawScreenTriangle(A, C, D);
//						drawScreenTriangle(B, C, D);
//					} else {
//						drawScreenTriangle(A, D, C);
//						drawScreenTriangle(B, D, C);
//					}
//				} else {
//					if (B.x < C.x) {
//						drawScreenTriangle(A, B, C);
//					} else {
//						drawScreenTriangle(A, C, B);
//					}
//				}
//			} else if (B.y < A.y && B.y < C.y) {
//				if (A.y < C.y) {
//					float f = (A.y - B.y) / (C.y - B.y);
//					pd.interpolate(pb,pc,f);
//					//nd.interpolate(nb,nc,f);
//					D.x = -pd.x / pd.z * fW + iXoff;
//					D.y = pd.y / pd.z * fW + iYoff;
//					if (A.x < D.x) {
//						drawScreenTriangle(B, A, D);
//						drawScreenTriangle(C, A, D);
//					} else {
//						drawScreenTriangle(B, D, A);
//						drawScreenTriangle(C, D, A);
//					}
//				} else if (A.y > C.y) {
//					float f = (C.y - B.y) / (A.y - B.y);
//					pd.interpolate(pb,pa,f);
//					//nd.interpolate(nb,na,f);
//					D.x = -pd.x / pd.z * fW + iXoff;
//					D.y = pd.y / pd.z * fW + iYoff;
//					if (C.x < D.x) {
//						drawScreenTriangle(B, C, D);
//						drawScreenTriangle(A, C, D);
//					} else {
//						drawScreenTriangle(B, D, C);
//						drawScreenTriangle(A, D, C);
//					}
//				} else {
//					if (A.x < C.x) {
//						drawScreenTriangle(B, A, C);
//					} else {
//						drawScreenTriangle(B, C, A);
//					}
//				}
//			} else {
//				if (A.y < B.y) {
//					float f = (A.y - C.y) / (B.y - C.y);
//					pd.interpolate(pc,pb,f);
//					//nd.interpolate(nc,nb,f);
//					D.x = -pd.x / pd.z * fW + iXoff;
//					D.y = pd.y / pd.z * fW + iYoff;
//					if (A.x < D.x) {
//						drawScreenTriangle(C, A, D);
//						drawScreenTriangle(B, A, D);
//					} else {
//						drawScreenTriangle(C, D, A);
//						drawScreenTriangle(B, D, A);
//					}
//				} else if (A.y > B.y) {
//					float f = (B.y - C.y) / (A.y - C.y);
//					pd.interpolate(pc,pa,f);
//					//nd.interpolate(nc,na,f);
//					D.x = -pd.x / pd.z * fW + iXoff;
//					D.y = pd.y / pd.z * fW + iYoff;
//					if (B.x < D.x) {
//						drawScreenTriangle(C, B, D);
//						drawScreenTriangle(A, B, D);
//					} else {
//						drawScreenTriangle(C, D, B);
//						drawScreenTriangle(A, D, B);
//					}
//				} else {
//					if (A.x < B.x) {
//						drawScreenTriangle(C, A, B);
//					} else {
//						drawScreenTriangle(C, B, A);
//					}
//				}
//			}
//		}
	}

	public final void drawTriangle(Point3f pa, Color3f ca, Point3f pb, Color3f cb, Point3f pc, Color3f cc) { //, Vector3d na, Vector3d nb, Vector3d nc) {
		if (bPerspective) {
			/*
			 * Check if triangle intersects near clipping plane and split if necessary
			 */
			if (pa.z > fNearClip) {
				if (pb.z > fNearClip) {
					if (pc.z > fNearClip) {									// triangle is entirely behind near clipping plane, draw it
						drawScreenTriangle(pa, ca, pb, cb, pc, cc);			
					} else {												// only c is on front of near clipping plane, split triangle
						float tca = (fNearClip - pc.z) / (pa.z - pc.z);		
						float tbc = (fNearClip - pb.z) / (pc.z - pb.z);
						Pca.interpolate(pc, pa, tca);
						Cca.interpolate(cc, ca, tca);
						Pbc.interpolate(pb, pc, tbc);
						Cbc.interpolate(cb, cc, tbc);
						drawScreenTriangle(pa, ca, pb, cb, Pbc, Cbc);
						drawScreenTriangle(Pbc, Cbc, Pca, Cca, pa, ca);
					}
				} else {
					if (pc.z > fNearClip) {									// only b is in front of near clipping plane, split triangle
						float tab = (fNearClip - pa.z) / (pb.z - pa.z);		
						float tbc = (fNearClip - pb.z) / (pc.z - pb.z);
						Pab.interpolate(pa, pb, tab);
						Cab.interpolate(ca, cb, tab);
						Pbc.interpolate(pb, pc, tbc);
						Cbc.interpolate(cb, cc, tbc);
						drawScreenTriangle(pc, cc, pa, ca, Pab, Cab);
						drawScreenTriangle(Pab, Cab, Pbc, Cbc, pc, cc);
					} else {												// b and c are in front of near clipping plane, split triange
						float tca = (fNearClip - pc.z) / (pa.z - pc.z);		
						float tab = (fNearClip - pa.z) / (pb.z - pa.z);
						Pca.interpolate(pc, pa, tca);
						Cca.interpolate(cc, ca, tca);
						Pab.interpolate(pa, pb, tab);
						Cab.interpolate(ca, cb, tab);
						drawScreenTriangle(pa, ca, Pab, Cab, Pca, Cca);
					}
				}
			} else {														
				if (pb.z > fNearClip) {
					if (pc.z > fNearClip) {									// only a is in front of near clipping plane, split triangle
						float tab = (fNearClip - pa.z) / (pb.z - pa.z);		
						float tca = (fNearClip - pc.z) / (pa.z - pc.z);
						Pab.interpolate(pa, pb, tab);
						Cab.interpolate(ca, cb, tab);
						Pca.interpolate(pc, pa, tca);
						Cca.interpolate(cc, ca, tca);
						drawScreenTriangle(pb, cb, pc, cc, Pca, Cca);
						drawScreenTriangle(Pca, Cca, Pab, Cab, pb, cb);
					} else {												// a and c are in front of near clipping plane, split triange
						float tca = (fNearClip - pc.z) / (pa.z - pc.z);		
						float tbc = (fNearClip - pb.z) / (pc.z - pb.z);
						Pca.interpolate(pc, pa, tca);
						Cca.interpolate(cc, ca, tca);
						Pbc.interpolate(pb, pc, tbc);
						Cbc.interpolate(cb, cc, tbc);
						drawScreenTriangle(pb, cb, Pbc, Cbc, Pab, Cab);
					}
				} else {
					if (pc.z > fNearClip) {									// a and b are in front of near clipping plane, split triangle
						float tca = (fNearClip - pc.z) / (pa.z - pc.z);		
						float tbc = (fNearClip - pb.z) / (pc.z - pb.z);
						Pca.interpolate(pc, pa, tca);
						Cca.interpolate(cc, ca, tca);
						Pbc.interpolate(pb, pc, tbc);
						Cbc.interpolate(cb, cc, tbc);
						drawScreenTriangle(pc, cc, Pca, Cca, Pbc, Cbc);
					}														// triangle is entirely in fron of near clipping plane, skip it
				}
			}
		}
	}
}
