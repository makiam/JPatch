/*
 * $Id: Viewport2.java,v 1.2 2005/08/10 15:13:41 sascha_l Exp $
 *
 * Copyright (c) 2005 Sascha Ledinsky
 *
 * This file is part of JPatch.
 *
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package jpatch.boundary;

import javax.vecmath.Point3f;

import jpatch.entity.*;
import javax.vecmath.*;

/**
 * @author sascha
 *
 */
public class Viewport2 {
	private static int iCurveSubdiv = 5;
	
	private static float[] cB0;
	private static float[] cB1;
	private static float[] cB2;
	private static float[] cB3;
	
	private static Point3f p0 = new Point3f();
	private static Point3f p1 = new Point3f();
	private static Point3f p2 = new Point3f();
	private static Point3f p3 = new Point3f();
	
	private JPatchDrawable2 drawable;
	private ViewDefinition viewDefinition;
	private JPatchSettings settings = JPatchSettings.getInstance();
	
	private Matrix4f m4View = new Matrix4f();
	
	static {
		init();
	}
	
	private static void init() {
		cB0 = new float[iCurveSubdiv];
		cB1 = new float[iCurveSubdiv];
		cB2 = new float[iCurveSubdiv];
		cB3 = new float[iCurveSubdiv];
		for (int i = 0; i < iCurveSubdiv; i++) {
			float s = (float) i / (float) (iCurveSubdiv - 1);
			cB0[i] = (1 - s) * (1 - s) * (1 - s);
			cB1[i] = 3 * s * (1 - s) * (1 - s);
			cB2[i] = 3 * s * s * (1 - s);
			cB3[i] = s * s * s;
		}
	}
	
	public Viewport2(JPatchDrawable2 drawable, ViewDefinition viewDefinition) {
		this.drawable = drawable;
		this.viewDefinition = viewDefinition;
	}
	
	public void prepare() {
		if (drawable.isTransformSupported())
			drawable.setTransform(viewDefinition.getScreenMatrix());
		else
			m4View.set(viewDefinition.getScreenMatrix());
		drawable.clear(JPatchDrawable2.COLOR_BUFFER | JPatchDrawable2.DEPTH_BUFFER, new Color3f(settings.cBackground)); // FIXME
	}
	
	public void drawModel(Model model) {
		System.out.println("drawModel");
		if (viewDefinition.renderCurves()) {
			drawable.setColor(new Color3f(settings.cCurve)); // FIXME
			for (Curve curve = model.getFirstCurve(); curve != null; curve = curve.getNext()) {
				if (!curve.getStart().isStartHook())
					drawCurve(curve);
			}
		}
	}
	
	private void drawCurve(Curve curve) {
		for (ControlPoint cp = curve.getStart(); cp != null; cp = cp.getNextCheckNextLoop()) {
			if (cp.getNext() != null) {
				if (!drawable.isTransformSupported()) {
					p0.set(cp.getPosition());
					p1.set(cp.getOutTangent());
					p2.set(cp.getNext().getInTangent());
					p3.set(cp.getNext().getPosition());
					m4View.transform(p0);
					m4View.transform(p1);
					m4View.transform(p2);
					m4View.transform(p3);
					drawCurveSegment(p0, p1, p2, p3);
				} else {
					drawCurveSegment(cp.getPosition(), cp.getOutTangent(), cp.getNext().getInTangent(), cp.getNext().getPosition());
				}
			}
		}
	}
	
	private void drawCurveSegment(Point3f pa, Point3f pb, Point3f pc, Point3f pd) {
		p0.set(p0);
		for (int t = 0; t < iCurveSubdiv - 1; t++) {
			p1.set(
				cB0[t] * pa.x + cB1[t] * pb.x + cB2[t] * pc.x + cB3[t] * pd.x,
				cB0[t] * pa.y + cB1[t] * pb.y + cB2[t] * pc.y + cB3[t] * pd.y,
				cB0[t] * pa.z + cB1[t] * pb.z + cB2[t] * pc.z + cB3[t] * pd.z
			);
			drawable.drawLine(p0, p1);
			p0.set(p1);
		}
		drawable.drawLine(p0, pd);
	}
}
