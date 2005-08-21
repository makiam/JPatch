/**
 * $Id: NewLatheEditorEdit.java,v 1.5 2005/08/21 15:32:08 lois Exp $
 */
package jpatch.control.edit;

import jpatch.auxilary.Functions;
import jpatch.entity.ControlPoint;
import jpatch.entity.Curve;

/**
 * 
 * @author lois
 * @version $Revision: 1.5 $
 * 
 */
public class NewLatheEditorEdit extends JPatchCompoundEdit {
	
	/**
	* Creates and performes the edit
	*/
	public NewLatheEditorEdit(int iSegments, int iForm, int iFill, int iSize, int iDistance, int iRotation, boolean bCloseTop, boolean bCloseBottom) {
		
//		float epsilon = 3f / MainFrame.getInstance().getJPatchScreen().getActiveViewport().getViewDefinition().getMatrix().getScale();
		float epsilon = 0.1f; // FIXME
		
		// calculate control points
		ControlPoint[] cpts = new ControlPoint[iSegments/2 + (bCloseTop ? 1 : 0) + (bCloseBottom ? 1: 0) + 1];

		double dyr = 0;
		boolean first = true;
		int cpidx = 0; // controlpoint index
		
		// curve
		for (int i = 0; i <= iSegments/2; i++) {
			// fill, size
			double dx = Math.sin(Math.PI*i*(100f-iFill)/25f/(float)iSegments)*iSize/10f;
			double dy = Math.cos(Math.PI*i*(100f-iFill)/25f/(float)iSegments)*iSize/10f;
			// form			
			if (iForm >= 0) 
				dx = dx * (90-iForm)/90;
			else 
				dy = dy * (90+iForm)/90;
			// rotation
		    double dxr = dx * Math.cos(Math.PI*iRotation/180) - dy * Math.sin(Math.PI*iRotation/180);
		    dyr = dx * Math.sin(Math.PI*iRotation/180) + dy * Math.cos(Math.PI*iRotation/180);				
			// distance
			dxr = dxr + iDistance/10f;
			
			// close first control point
			if (first && bCloseTop) {
				first = false;
				cpts[cpidx++] = new ControlPoint(0f, (float)dyr, 0f);
				cpts[0].setMode(ControlPoint.PEAK);
			}
			
			// add controlpoint
			cpts[cpidx] = new ControlPoint(0f, (float)dyr, (float)dxr);
			if (iFill == 50 || iFill == 0) // correct spere & torus curvature
				cpts[cpidx].setMagnitude(Functions.optimumCurvature(iFill == 0 ? iSegments/2 : iSegments));
			if (cpidx > 0) 
				if (iFill == 0 && cpidx == iSegments/2) { // close torus loop
					cpts[cpidx-1].setNext(cpts[0]);
					cpts[0].setPrev(cpts[cpidx-1]);
					cpts[0].setLoop(true);
				} else 
					cpts[cpidx].appendTo(cpts[cpidx-1]);
			
			if (bCloseTop && cpidx == 1)
				cpts[1].setMode(ControlPoint.PEAK);
			
			cpidx++;
		}
		
		// close last cp
		if (bCloseBottom) {
			cpts[cpidx] = new ControlPoint(0f, (float)dyr, 0f);
			cpts[cpidx].setMode(ControlPoint.PEAK);
			cpts[cpidx-1].setMode(ControlPoint.PEAK);
			cpts[cpidx].appendTo(cpts[cpidx-1]);
		}

		// add edits
		Curve curve = new Curve(cpts[0]); //, MainFrame.getInstance().getModel());
		//curve.validate();
		addEdit(new CreateCurveEdit(curve));	// create the new curve
		addEdit(new ValidateCurveEdit(curve));  // validate the curve
		addEdit(new NewLatheEdit(cpts, iSegments, epsilon)); // lathe the curve
	}	
}
