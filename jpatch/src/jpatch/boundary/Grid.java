package jpatch.boundary;

import javax.vecmath.*;

import jpatch.boundary.settings.Settings;

public class Grid {
	
	public static final int NONE = 0;
	public static final int XY = 1;
	public static final int XZ = 2;
	public static final int YZ = 3;
	
	private int iPlane = XZ;
//	private int iSize = 25;
//	private boolean bSnap = true;
	private Settings settings = Settings.getInstance();
	
	public Grid() {
	}
	
	public void setPlane(int plane) {
		iPlane = plane;
	}
	
//	public boolean snap() {
//		return bSnap;
//	}
//	
//	public void snap(boolean enable) {
//		bSnap = enable;
//	}
	
//	public float getSpacing() {
//		return fSpacing;
//	}
//	
//	public void setSpacing(float spacing) {
//		fSpacing = spacing;
//	}
	
	public boolean isSnapping() {
		return settings.viewports.snapToGrid && (iPlane != NONE);
	}
	
	public void paint(ViewDefinition viewDef) {
		JPatchDrawable2 drawable = viewDef.getDrawable();
		Matrix4f m4View = viewDef.getScreenMatrix();
		if (viewDef.getView() != ViewDefinition.BIRDS_EYE && viewDef.getCamera() == null) {
			drawPlanarGrid(drawable, m4View, (int) viewDef.getWidth(), (int) viewDef.getHeight());
		} else if (settings.viewports.showGroundPlaneInModeler && MainFrame.getInstance().getModel() != null || settings.viewports.showGroundPlaneInAnimator && MainFrame.getInstance().getAnimation() != null) {
			drawBirdsEyeGrid(drawable, viewDef.getMatrix());
		}
	}

	private void drawPlanarGrid(JPatchDrawable2 drawable, Matrix4f m4View, int width, int height) {
		float gridScreenSpacing;
		if (MainFrame.getInstance().getModel() != null)
			gridScreenSpacing = settings.viewports.modelerGridSpacing * m4View.getScale();
		else
			gridScreenSpacing = settings.viewports.animatorGridSpacing * m4View.getScale();
		float dx = width / 2;
		float dy = height / 2;
		float xcenter = m4View.m03;
		float ycenter = m4View.m13;
		if (gridScreenSpacing >= 4)
		{
			int start = Math.round((- xcenter)/gridScreenSpacing);
			int end = Math.round((2 * dx - xcenter)/gridScreenSpacing);
			for (int x = start; x <= end; x++)
			{
				if (x % 5 == 0) drawable.setColor(settings.colors.majorGrid);
				else drawable.setColor(settings.colors.minorGrid);
				drawable.drawLine((int)(xcenter + x*gridScreenSpacing),0,(int)(xcenter + x*gridScreenSpacing),(int)height);
			}
			start = Math.round((- ycenter)/gridScreenSpacing);
			end = Math.round((2 * dy - ycenter)/gridScreenSpacing);
			for (int y = start; y <= end; y++)
			{
				if (y % 5 == 0) drawable.setColor(settings.colors.majorGrid);
				else drawable.setColor(settings.colors.minorGrid);
				drawable.drawLine(0,(int)(ycenter + y*gridScreenSpacing),(int)width,(int)(ycenter + y*gridScreenSpacing));
			}
			drawable.setColor(settings.colors.minorGrid);
			drawable.drawLine((int)(xcenter + 0*dx - 1),0,(int)(xcenter + 0*dx - 1),(int)(height));
			drawable.drawLine((int)(xcenter + 0*dx + 1),0,(int)(xcenter + 0*dx + 1),(int)(height));
			drawable.drawLine(0,(int)(ycenter + 0*dy - 1),(int)width,(int)(ycenter + 0*dy - 1));
			drawable.drawLine(0,(int)(ycenter + 0*dy + 1),(int)width,(int)(ycenter + 0*dy + 1));
		}
		drawable.setColor(settings.colors.majorGrid);
		drawable.drawLine((int)(xcenter + 0*dx),0,(int)(xcenter + 0*dx),(int)(height));
		drawable.drawLine(0,(int)(ycenter + 0*dy),(int)width,(int)(ycenter + 0*dy));
	}
	
	
	private void drawBirdsEyeGrid(JPatchDrawable2 drawable, Matrix4f m4View) {
		int iSize = settings.viewports.groundPlaneSize;
		float fSpacing = settings.viewports.groundPlaneSpacing;
		float max = fSpacing * iSize;
		float f;
		Point3f a = new Point3f();
		Point3f b = new Point3f();
		for (int n = -iSize; n <= iSize; n++) {
			f = fSpacing * n;
			if (n % 5 == 0) {
				drawable.setColor(settings.colors.majorGrid);
			} else {
				drawable.setColor(settings.colors.minorGrid);
			}
			switch(iPlane) {
				case XZ:
					a.set(-max,0,f);
					b.set(max,0,f);
					break;
				case XY:
					a.set(-max,f,0);
					b.set(max,f,0);
					break;
				case YZ:
					a.set(0,-max,f);
					b.set(0,max,f);
			}
			m4View.transform(a);
			m4View.transform(b);
			drawable.drawLine(a,b);
			switch(iPlane) {
				case XZ:
					a.set(f,0,-max);
					b.set(f,0,max);
					break;
				case XY:
					a.set(f,-max,0);
					b.set(f,max,0);
					break;
				case YZ:
					a.set(0,f,-max);
					b.set(0,f,max);
			}
			m4View.transform(a);
			m4View.transform(b);
			drawable.drawLine(a,b);
		}
	}
	
	public boolean correctPosition(Tuple3f from, Tuple3f to) {
		float gridSpacing;
		if (MainFrame.getInstance().getModel() != null)
			gridSpacing = settings.viewports.modelerGridSpacing;
		else
			gridSpacing = settings.viewports.animatorGridSpacing;
		if (settings.viewports.snapToGrid && iPlane != NONE) {
			to.x = (iPlane == YZ) ? from.x : Math.round(to.x / gridSpacing) * gridSpacing;
			to.y = (iPlane == XZ) ? from.y : Math.round(to.y / gridSpacing) * gridSpacing;
			to.z = (iPlane == XY) ? from.z : Math.round(to.z / gridSpacing) * gridSpacing;
		}
		return (!from.equals(to));
	}
	
	public boolean correctZPosition(Tuple3f from, Tuple3f to) {
		float gridSpacing;
		if (MainFrame.getInstance().getModel() != null)
			gridSpacing = settings.viewports.modelerGridSpacing;
		else
			gridSpacing = settings.viewports.animatorGridSpacing;
		if (settings.viewports.snapToGrid && iPlane != NONE) {
			to.x = (iPlane != YZ) ? from.x : Math.round(to.x / gridSpacing) * gridSpacing;
			to.y = (iPlane != XZ) ? from.y : Math.round(to.y / gridSpacing) * gridSpacing;
			to.z = (iPlane != XY) ? from.z : Math.round(to.z / gridSpacing) * gridSpacing;
		}
		return (!from.equals(to));
	}
	
	public void correctVector(Tuple3f t, int plane) {
		float gridSpacing;
		if (MainFrame.getInstance().getModel() != null)
			gridSpacing = settings.viewports.modelerGridSpacing;
		else
			gridSpacing = settings.viewports.animatorGridSpacing;
		if (plane != NONE) {
			t.x = (plane == YZ) ? t.x : Math.round(t.x / gridSpacing) * gridSpacing;
			t.y = (plane == XZ) ? t.y : Math.round(t.y / gridSpacing) * gridSpacing;
			t.z = (plane == XY) ? t.z : Math.round(t.z / gridSpacing) * gridSpacing;
		}
	}
	
	public void correctZVector(Tuple3f t) {
		float gridSpacing;
		if (MainFrame.getInstance().getModel() != null)
			gridSpacing = settings.viewports.modelerGridSpacing;
		else
			gridSpacing = settings.viewports.animatorGridSpacing;
		if (settings.viewports.snapToGrid && iPlane != NONE) {
			t.x = (iPlane != YZ) ? 0 : Math.round(t.x / gridSpacing) * gridSpacing;
			t.y = (iPlane != XZ) ? 0 : Math.round(t.y / gridSpacing) * gridSpacing;
			t.z = (iPlane != XY) ? 0 : Math.round(t.z / gridSpacing) * gridSpacing;
		}
	}
	
	public Vector3f getCorrectionVector(Tuple3f t3) {
		float gridSpacing;
		if (MainFrame.getInstance().getModel() != null)
			gridSpacing = settings.viewports.modelerGridSpacing;
		else
			gridSpacing = settings.viewports.animatorGridSpacing;
		Vector3f v3 = new Vector3f();
		if (settings.viewports.snapToGrid && iPlane != NONE) {
			v3.set(t3);
			v3.x = (iPlane == YZ) ? v3.x : Math.round(v3.x / gridSpacing) * gridSpacing;
			v3.y = (iPlane == XZ) ? v3.y : Math.round(v3.y / gridSpacing) * gridSpacing;
			v3.z = (iPlane == XY) ? v3.z : Math.round(v3.z / gridSpacing) * gridSpacing;
			v3.sub(t3);
		}
		return v3;
	}
	
	public Vector3f getZCorrectionVector(Tuple3f t3) {
		float gridSpacing;
		if (MainFrame.getInstance().getModel() != null)
			gridSpacing = settings.viewports.modelerGridSpacing;
		else
			gridSpacing = settings.viewports.animatorGridSpacing;
		Vector3f v3 = new Vector3f();
		if (settings.viewports.snapToGrid && iPlane != NONE) {
			v3.set(t3);
			v3.x = (iPlane != YZ) ? v3.x : Math.round(v3.x / gridSpacing) * gridSpacing;
			v3.y = (iPlane != XZ) ? v3.y : Math.round(v3.y / gridSpacing) * gridSpacing;
			v3.z = (iPlane != XY) ? v3.z : Math.round(v3.z / gridSpacing) * gridSpacing;
			v3.sub(t3);
		}
		return v3;
	}
}

