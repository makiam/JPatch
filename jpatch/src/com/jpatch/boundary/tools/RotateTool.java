package com.jpatch.boundary.tools;

import com.jpatch.boundary.Viewport;
import com.jpatch.boundary.ViewportGl;
import com.jpatch.settings.ColorSettings;
import com.jpatch.settings.Settings;

import javax.media.opengl.GL;
import javax.vecmath.Matrix3f;
import javax.vecmath.Point3f;

import static javax.media.opengl.GL.*;

public class RotateTool implements JPatchTool {
	private static final int SEGMENTS = 72;
	private final ColorSettings colorSettings = Settings.getInstance().colors;
	private final Point3f[][] points = new Point3f[3][SEGMENTS + 1];
	static int n = 0;
	
	public RotateTool() {
		for (int i = 0; i < SEGMENTS; i++) {
			float sin = (float) Math.sin(2 * Math.PI / SEGMENTS * i);
			float cos = (float) Math.cos(2 * Math.PI / SEGMENTS * i);
			points[0][i] = new Point3f(0, -sin, cos);
			points[1][i] = new Point3f(cos, 0, sin);
			points[2][i] = new Point3f(cos, -sin, 0);
		}
	}
	
	public void draw(Viewport viewport) {
		GL gl = ((ViewportGl) viewport).getGl();
		gl.glDisable(GL_LIGHTING);
//		System.out.println("drawing rotateTool " + n++);
		Point3f p = new Point3f();
		for (int i = 0; i < 3; i++) {
			switch (i) {
			case 0:
				gl.glColor3f(colorSettings.xAxis.x, colorSettings.xAxis.y, colorSettings.xAxis.z);
				break;
			case 1:
				gl.glColor3f(colorSettings.yAxis.x, colorSettings.yAxis.y, colorSettings.yAxis.z);
				break;
			case 2:
				gl.glColor3f(colorSettings.zAxis.x, colorSettings.zAxis.y, colorSettings.zAxis.z);
				break;
			}
			gl.glBegin(GL_LINE_STRIP);
			for (int j = 0; j <= SEGMENTS; j++) {
				p.set(points[i][(j < SEGMENTS) ? j : 0]);
				viewport.getViewDef().getMatrix().transform(p);
				gl.glVertex3f(p.x, p.y, p.z);
			}
			gl.glEnd();
		}
	}
	
	public void registerListeners(Viewport[] viewports) {
		// TODO Auto-generated method stub
		
	}
	
	public void unregisterListeners(Viewport[] viewports) {
		// TODO Auto-generated method stub
		
	}

}
