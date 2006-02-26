package jpatch.boundary.mouse;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import jpatch.boundary.MainFrame;
import jpatch.boundary.Selection;
import jpatch.boundary.ViewDefinition;
import jpatch.boundary.settings.Settings;
import jpatch.boundary.tools.DefaultTool;
import jpatch.control.edit.AtomicInsertControlPoint;
import jpatch.control.edit.JPatchActionEdit;
import jpatch.entity.ControlPoint;

/**
 * implements the knife tool.
 * @author torf
 */
public class KnifeMouseAdapter extends JPatchMouseAdapter {
	
	private int iStartX = 0, iStartY = 0, iEndX = 0, iEndY = 0;
	
	/** Max. length of a line segment when drawing */
	private static int SEGMENT_LENGTH = 20;
	
	/** Max. levels of subdivision for intersection tests */
	private static int MAX_SUBDIVISION = 10;
	
	/** normal vector of the cutting plane */
	private Vector3f v3Normal = new Vector3f();
	
	/** support vector of the cutting plane */
	private Point3f p3Support = new Point3f();
	
	private Color cLineColor = new Color(Settings.getInstance().colors.background.get().getRGB() ^ Settings.getInstance().colors.selection.get().getRGB());
	
	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			iStartX = e.getX();
			iStartY = e.getY();
			iEndX = iStartX;
			iEndY = iStartY;
			((Component) e.getSource()).addMouseMotionListener(this);
		} if (e.getButton() == MouseEvent.BUTTON3) {
			//cancel operation
			cleanUp((Component)e.getSource());
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {				
		int iEventX = e.getX();
		int iEventY = e.getY();		
				
		Graphics2D graphics = (Graphics2D)((Component)e.getSource()).getGraphics();
		drawSegmentedLineTo(graphics,iEndX,iEndY); //remove old line
		drawSegmentedLineTo(graphics,iEventX,iEventY); //draw new line
		
		//remember coordinates
		iEndX = iEventX;
		iEndY = iEventY;		
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {		
		
		if (e.getButton() == MouseEvent.BUTTON1) {					
			Selection selection = MainFrame.getInstance().getSelection();
			
			if (selection==null) {
				MainFrame.getInstance().getJPatchScreen().update_all();
				return;
			}
			
			/* 
			 * Calculate cutting plane. The plane is defined by the start point of the operation (p3Support)
			 * the direction of view (v3ViewDirection) and the direction of the operation (v3OperationDirection).
			 * Both vectors are combined into the normal of the plane (v3Normal).
			 */
			iEndX = e.getX();
			iEndY = e.getY();
			ViewDefinition viewDefinition = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component)e.getSource());
			Matrix4f m4View = viewDefinition.getScreenMatrix();
			Matrix4f m4Inverse = new Matrix4f();
			m4Inverse.invert(m4View);
			Vector3f v3ViewDirection = new Vector3f(0,0,1);
			Vector3f v3OperationDirection = new Vector3f(iEndX-iStartX,iEndY-iStartY,0);
			p3Support = new Point3f(iStartX,iStartY,0);			
			m4Inverse.transform(v3ViewDirection);
			m4Inverse.transform(v3OperationDirection);
			m4Inverse.transform(p3Support);
			v3Normal.cross(v3ViewDirection,v3OperationDirection);						
						
			//System.out.print("\nCutting: ");
			
			JPatchActionEdit actionEdit = new JPatchActionEdit("Knife tool");
			
			//Boundary
			int minX = Math.min(iStartX,iEndX);
			int maxX = Math.max(iStartX,iEndX);
			int minY = Math.min(iStartY,iEndY);
			int maxY = Math.max(iStartY,iEndY);
			
			//loop through selected control points
			for (Iterator it = selection.getObjects().iterator(); it.hasNext(); ) {
				Object key = it.next();
				if (!(key instanceof ControlPoint)) {
					continue;
				}
				ControlPoint cpHead = (ControlPoint) key;
				//System.out.println("");				
				//loop through all attached control points
				for (ControlPoint cpCurrent=cpHead; cpCurrent!=null; cpCurrent=cpCurrent.getPrevAttached()) {
					
					//System.out.print("<" + cpCurrent.number() + "> ");
				
					ControlPoint cpNext = cpCurrent.getNextCheckNextLoop();
					if (cpNext==null || !selection.contains(cpNext.getHead())) {
						//System.out.print("n ");
						continue;
					}
					
					Point3f p0 = new Point3f(cpCurrent.getReferencePosition()); 
					Point3f p1 = new Point3f(cpCurrent.getReferenceOutTangent());
					Point3f p2 = new Point3f(cpNext.getReferenceInTangent());
					Point3f p3 = new Point3f(cpNext.getReferencePosition());
					
					//Search for intersection points
					LinkedList<Point3f> intersectionList = new LinkedList<Point3f>();
					searchIntersections(p0,p1,p2,p3,intersectionList,0);
					
					//System.out.print(intersectionList.size() + " ");
					
					//Insert new points												
					ControlPoint cpLast = cpCurrent;		
					for (Iterator<Point3f> p3it=intersectionList.iterator(); p3it.hasNext(); ) {
						Point3f p3Current = p3it.next();
						ControlPoint cpThis = new ControlPoint(new Point3f(p3Current));					
					
						//If a intersection found in the step before does not lie on the
						//cut line drawn by the user do not add it.
						m4View.transform(p3Current);						
						if (p3Current.x<minX || p3Current.x>maxX || p3Current.y<minY || p3Current.y>maxY) {
							//System.out.print("d ");
							continue;
						}
						
						//Finally insert control point
						actionEdit.addEdit(new AtomicInsertControlPoint(cpThis,cpLast));
						cpLast = cpThis;
					}
					
				} //attached points
				
			} //selected points
			
			//Perform operation
			MainFrame.getInstance().getUndoManager().addEdit(actionEdit);
			
			//Clean up
			cleanUp((Component)e.getSource());
		}
	}
	
	/**
	 * draws a straight line from (iStartX,iStartY) to (iX,iY) using
	 * the standard selection color and XOR mode. The line is split up into smaller
	 * segments to reduce the buffer used by XOR.
	 *   
	 * @param graphics graphics object to draw into
	 * @param iX x coordinate of point to draw line to
	 * @param iY y coordinate of point to draw line to
	 * @see KnifeMouseAdapter.SEGMENT_LENGTH
	 */
	private void drawSegmentedLineTo(Graphics2D graphics, int iX, int iY) {
		int iDeltaX = iX - iStartX;
		int iDeltaY = iY - iStartY;
		
		if (iDeltaX==0 && iDeltaY==0) {
			return;
		}
		
		double dLength = Math.sqrt(iDeltaX*iDeltaX + iDeltaY*iDeltaY);		
		
		//set draw style
		graphics.setXORMode(cLineColor);
		graphics.setStroke(new BasicStroke(1.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,0.0f,new float[] { 5.0f, 5.0f }, 0.0f));
		
		int iSegments = (int)Math.ceil(dLength/SEGMENT_LENGTH);
		double dSegX = iDeltaX/(double)iSegments;
		double dSegY = iDeltaY/(double)iSegments;
		
		for (int a=0; a<iSegments; a++) {
			graphics.drawLine(
					(int)(iStartX+a*dSegX),
					(int)(iStartY+a*dSegY),
					(int)(iStartX+(a+1)*dSegX),
					(int)(iStartY+(a+1)*dSegY)
			);
		}
	}
	
	/**
	 * Performs plane-spline intersection tests. All intersections found will be added to intersectionList. 
	 * 
	 * To check for intersections between the cutting plane and the spline 
	 * the algorithm checks for intersections between the hull polygon of the spline and the plane. This is safe because 
	 * the spline is always contained in its hull polygon (and therefore, if the spline intersects the plane, the poly will 
	 * also intersect it). To calculate the actual point of intersection, the hull polygon is subdivided recursively with 
	 * de Casteljau's algorithm.
	 *  
	 * @param p0 First point of hull polygon. Will be modified.
	 * @param p1 Second point of hull polygon. Will be modified.
	 * @param p2 Third point of hull polygon. Will be modified.
	 * @param p3 Fourth point of hull polygon. Will be modified.
	 * @param intersectionList List of prior intersections.
	 */
	private void searchIntersections(Point3f p0, Point3f p1, Point3f p2, Point3f p3, List<Point3f> intersectionList,int level) {
		int iSide0 = getSideOfPlane(p0);
		int iSide1 = getSideOfPlane(p1);
		int iSide2 = getSideOfPlane(p2);
		int iSide3 = getSideOfPlane(p3);
		
		if (iSide0==iSide1 && iSide1==iSide2 && iSide2==iSide3) {
			//Spline does not intersect plane
			return;
		}
		
		if (level<MAX_SUBDIVISION) {
			//Subdivide into two halves
			Point3f pn0 = new Point3f();
			Point3f pn1 = new Point3f();
			Point3f pn2 = new Point3f();
			Point3f pn3 = new Point3f();
			deCasteljauSplit(p0,p1,p2,p3,pn0,pn1,pn2,pn3);
			
			searchIntersections(p0,p1,p2,p3,intersectionList,level+1);
			searchIntersections(pn0,pn1,pn2,pn3,intersectionList,level+1);
		} else {
			//search for intersection points
			if (iSide0 != iSide1) {
				float x = 0.5f*(p0.x+p1.x);
				float y = 0.5f*(p0.y+p1.y);
				float z = 0.5f*(p0.z+p1.z);
				Point3f p = new Point3f(x,y,z);
				intersectionList.add(p);
			}
			if (iSide1 != iSide2) {
				float x = 0.5f*(p1.x+p2.x);
				float y = 0.5f*(p1.y+p2.y);
				float z = 0.5f*(p1.z+p2.z);
				Point3f p = new Point3f(x,y,z);
				intersectionList.add(p);
			}
			if (iSide2 != iSide3) {
				float x = 0.5f*(p2.x+p3.x);
				float y = 0.5f*(p2.y+p3.y);
				float z = 0.5f*(p2.z+p3.z);
				Point3f p = new Point3f(x,y,z);
				intersectionList.add(p);
			}
		}
	}
	
	/**
	 * Calculates on which side of the cutting plane the given point is.
	 * 
	 * @param point Point to check.
	 * @return 0 if the point lies on the plane, 1 if it lies on the side the normal points to, -1 otherwise.
	 */
	private int getSideOfPlane(Point3f point) {		
		return ((int)Math.signum((point.x-p3Support.x)*v3Normal.x + (point.y-p3Support.y)*v3Normal.y + (point.z-p3Support.z)*v3Normal.z));
	}
	
	/**
	 * Splits a bezier curve into two using de Casteljau's algorithm. The curve is taken to
	 * be saved in p0-p3. The first half is stored in p0-p3, the second one in pn0-pn3.
	 * 
	 * @param p0 First control point of bezier curve.
	 * @param p1 Second control point of bezier curve.
	 * @param p2 Third control point of bezier curve.
	 * @param p3 Fourth control point of bezier curve.
	 * @param pn0 Placeholder for first control point of second half.
	 * @param pn1 Placeholder for second control point of second half.
	 * @param pn2 Placeholder for third control point of second half.
	 * @param pn3 Placeholder for fourth control point of second half.
	 * @author sascha (taken from jpatch.boundary.Viewport2)
	 */
	private void deCasteljauSplit(Point3f p0, Point3f p1, Point3f p2, Point3f p3, Point3f pn0, Point3f pn1, Point3f pn2, Point3f pn3) {
		pn0.set((p1.x + p2.x) * 0.5f, (p1.y + p2.y) * 0.5f, (p1.z + p2.z) * 0.5f);
		pn3.set(p3);
		pn2.set((p2.x + p3.x) * 0.5f, (p2.y + p3.y) * 0.5f, (p2.z + p3.z) * 0.5f);
		pn1.set((pn2.x + pn0.x) * 0.5f, (pn2.y + pn0.y) * 0.5f, (pn2.z + pn0.z) * 0.5f);
		p1.set((p0.x + p1.x) * 0.5f, (p0.y + p1.y) * 0.5f, (p0.z + p1.z) * 0.5f);
		p2.set((p1.x + pn0.x) * 0.5f, (p1.y + pn0.y) * 0.5f, (p1.z + pn0.z) * 0.5f);
		p3.set((p2.x + pn1.x) * 0.5f, (p2.y + pn1.y) * 0.5f, (p2.z + pn1.z) * 0.5f);
		pn0.set(p3);
	}
	
	/**
	 * cleans up after the tool is finished: 
	 * - removes mouse listener from given component
	 * - resets MainFrame's help text
	 * - reenables MainFrame's popup menu
	 * - switches back to the default tool
	 * - repaints all viewports
	 * @param component Component to remove mouse listener from.
	 */
	private void cleanUp(Component component) {
		component.removeMouseMotionListener(this);
		MainFrame.getInstance().setHelpText("");
		MainFrame.getInstance().getJPatchScreen().setTool(new DefaultTool());
		MainFrame.getInstance().getJPatchScreen().enablePopupMenu(true);
		MainFrame.getInstance().getJPatchScreen().update_all();
	}

}
