package jpatch.boundary;

import java.awt.*;
import java.awt.event.*;
import jpatch.entity.*;

public class MotionCurveDisplay extends VirtualCanvas {
	AnimObject animObject;
	int X = -1;
	
	public MotionCurveDisplay(SmartScrollPane smartScrollPane) {
		super(smartScrollPane);
		init();
	}
	
	public void init() {
		width = viewWidth = MainFrame.getInstance().getAnimation().getEnd() - MainFrame.getInstance().getAnimation().getStart() + 1;
		if (MainFrame.getInstance().getSelection() != null && MainFrame.getInstance().getSelection().getHotObject() instanceof AnimObject)
			animObject = (AnimObject) MainFrame.getInstance().getSelection().getHotObject();
		height = viewHeight = 1;
		if (animObject != null) {
			MotionCurveSet motionCurveSet = MainFrame.getInstance().getAnimation().getCurvesetFor(animObject);
			height = motionCurveSet.motionCurveList.size();
			viewHeight = Math.min(3, height);
		}
		//if (animObject != null && animObject instanceof AnimModel) height = viewHeight = ((AnimModel) animObject).getModel().getMorphList().size();
		top = viewTop = 0;
		left = viewLeft = MainFrame.getInstance().getAnimation().getStart() - 0.5f;
	}
	
	public void paint(Graphics g) {
		int pixelHeight = parent.getCanvasHeight();
		int pixelWidth = parent.getCanvasWidth();
		g.setClip(0, 0, pixelWidth, pixelHeight);
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, pixelWidth, pixelHeight);
		g.setColor(Color.WHITE);
		
		float pos = MainFrame.getInstance().getAnimation().getPosition();
		
		//MotionCurve.RotationCurve mrc = Animator.getInstance().tempCamCurve;
		//for (int i = 0; i < 50000; i+= 500) {
		//	javax.vecmath.Point3f p = mrc.rotationAt(i);
		//	g.setColor(Color.RED);
		//	g.drawRect(i / 100, (int) (p.x * 20), 2, 2);
		//	g.setColor(Color.GREEN);
		//	g.drawRect(i / 100, (int) (p.y * 20), 2, 2);
		//	g.setColor(Color.BLUE);
		//	g.drawRect(i / 100, (int) (p.z * 20), 2, 2);
		//}
		
		//if (model != null) {
		//	{
		//		MotionCurve.PointCurve mpc = Animator.getInstance().getPositionCurveFor(animModel);
		//		int yy = (int) (pixelHeight * ((y - viewTop) / viewHeight));
		//		int h = (int) ((double) pixelHeight / viewHeight * 0.25);
		//		int ym = yy + h;
		//		g.setColor(new Color(0x7777ff));
		//		g.drawLine(0, ym, pixelWidth, ym);
		//		for (int i = 0, n = mpc.getKeyCount(); i < n; i++) {
		//			MotionCurve.MotionKey mk = mpc.getKey(i);
		//			int x = (int) (((double) mk.position / 10000.0 - viewLeft) * (double) pixelWidth / (double) viewWidth);
		//			if (mk.position == pos) g.setColor(Color.WHITE);
		//			else g.setColor(Color.YELLOW);
		//			g.fillRect(x - 2, ym - 2, 5, 5);
		//			if (mk == selectedKey) {
		//				g.setColor(Color.GREEN);
		//				g.drawRect(x - 3, ym - 3, 6, 6);
		//			}
		//		}
		//		ym += h;
		//		g.setColor(new Color(0x7777ff));
		//		g.drawLine(0, ym, pixelWidth, ym);
		//		MotionCurve.QuaternionCurve mqc = Animator.getInstance().getOrientationCurveFor(animModel);
		//		for (int i = 0, n = mqc.getKeyCount(); i < n; i++) {
		//			MotionCurve.MotionKey mk = mqc.getKey(i);
		//			int x = (int) (((double) mk.position / 10000.0 - viewLeft) * (double) pixelWidth / (double) viewWidth);
		//			if (mk.position == pos) g.setColor(Color.WHITE);
		//			else g.setColor(Color.YELLOW);
		//			g.fillRect(x - 2, ym - 2, 5, 5);
		//			if (mk == selectedKey) {
		//				g.setColor(Color.GREEN);
		//				g.drawRect(x - 3, ym - 3, 6, 6);
		//			}
		//		}
		//		ym += h;
		//		g.setColor(new Color(0x7777ff));
		//		g.drawLine(0, ym, pixelWidth, ym);
		//		MotionCurve.PointCurve mcc = Animator.getInstance().getPositionCurveFor(Animator.getInstance().getActiveCamera());
		//		for (int i = 0, n = mcc.getKeyCount(); i < n; i++) {
		//			MotionCurve.MotionKey mk = mcc.getKey(i);
		//			int x = (int) (((double) mk.position / 10000.0 - viewLeft) * (double) pixelWidth / (double) viewWidth);
		//			if (mk.position == pos) g.setColor(Color.WHITE);
		//			else g.setColor(Color.YELLOW);
		//			g.fillRect(x - 2, ym - 2, 5, 5);
		//			if (mk == selectedKey) {
		//				g.setColor(Color.GREEN);
		//				g.drawRect(x - 3, ym - 3, 6, 6);
		//			}
		//		}
		//		g.setColor(Color.WHITE);
		//		g.drawString("Position", 4, yy + h - 8);
		//		g.drawString("Orientation", 4, yy + 2 * h - 8);
		//		g.drawString("Camera", 4, yy + 3 * h - 8);
		//		y++;
		//	}
		MotionCurveSet motionCurveSet = MainFrame.getInstance().getAnimation().getCurvesetFor(animObject);
		
		float y = top;
		if (motionCurveSet != null) {
			for (int c = 0, nc = motionCurveSet.motionCurveList.size(); c < nc; c++) {
				MotionCurve2 motionCurve = (MotionCurve2) motionCurveSet.motionCurveList.get(c);
				int yy = (int) (pixelHeight * ((y - viewTop) / viewHeight) + (double) pixelHeight / viewHeight * 0.05);
				int h = (int) ((double) pixelHeight / viewHeight * 0.9);
				if (yy > -h && yy < pixelHeight + h) {
					if (false)
						g.setColor(new Color(0x007700));
					else
						g.setColor(new Color(0x0000cc));
					g.fillRect(0, yy, pixelWidth, h);
				}
				y++;
			}
		}
		
		g.setColor(Color.BLACK);
		float frameWidth = (float) pixelWidth / viewWidth;
		if (frameWidth > 4) {
			for (int x = 0, f = 0; x < pixelWidth; x++) {
				float t = viewLeft + (float) x * viewWidth / (float) pixelWidth;
				if ((int) t > f) {
					f = (int) Math.round(t);
					g.drawLine(x-1, 0, x-1, pixelHeight);
				}
			}
		}
		
		y = top;
		MotionKey2 selectedKey = null;//Animator.getInstance().getActiveKey();
		if (motionCurveSet != null) {
			for (int c = 0, nc = motionCurveSet.motionCurveList.size(); c < nc; c++) {
				MotionCurve2 motionCurve = (MotionCurve2) motionCurveSet.motionCurveList.get(c);
				int yy = (int) (pixelHeight * ((y - viewTop) / viewHeight) + (double) pixelHeight / viewHeight * 0.05);
				int h = (int) ((double) pixelHeight / viewHeight * 0.9);
				
				if (yy > -h && yy < pixelHeight + h) {
					
					
					
					if (motionCurve instanceof MotionCurve2.Float) {
						if (false) g.setColor(new Color(0x33cc33));
						else g.setColor(new Color(0x7777ff));
						for (int x = 0; x < pixelWidth; x++) {
							float t = viewLeft + (float) x * viewWidth / (float) pixelWidth;
							float min = ((MotionCurve2.Float) motionCurve).getMin();
							float max = ((MotionCurve2.Float) motionCurve).getMax();
							float value = ((MotionCurve2.Float) motionCurve).getFloatAt(t);
							value = clamp(value, min, max);
							int yv = yy + (int) (h * (1.0 - ((value - min) / (max - min))));
							int y0 = yy + (int) (h * (1.0 - ((0.0 - min) / (max - min))));
							g.drawLine(x, y0, x, yv);
						}
						//g.drawString(MotionCurve.MotionKey.positionString(t), x, yy + 32);
					}
					
					for (int k = 0, nk = motionCurve.getKeyCount(); k < nk; k++) {
						MotionKey2 mk = motionCurve.getKey(k);
						int x = (int) ((mk.getPosition() - viewLeft) * (float) pixelWidth / (float) viewWidth);
						if (mk instanceof MotionKey2.Float) {
							float value = ((MotionKey2.Float) mk).getFloat();
							float min = ((MotionCurve2.Float) motionCurve).getMin();
							float max = ((MotionCurve2.Float) motionCurve).getMax();
							value = clamp(value, min, max);
							int ym = yy + (int) (h * (1.0 - (value - min) / (max - min)));
							g.setColor((mk.getPosition() == pos) ? Color.WHITE : Color.ORANGE);
							g.fillRect(x - 2, ym - 2, 5, 5);
							if (mk == selectedKey) {
								g.setColor(Color.YELLOW);
								//g.drawRect(x - 3, ym - 3, 6, 6);
								g.drawRect(x - 4, ym - 4, 8, 8);
							}
						}
						else {
							int ym = yy + h / 2;
							if (mk instanceof MotionKey2.Color3f) {
								g.setColor(((MotionKey2.Color3f) mk).getColor3f().get());
								g.fillRect(x - 4, ym - 4, 9, 9);
								g.setColor(Color.WHITE);
								g.drawRect(x - 5, ym - 5, 10, 10);
								if (mk == selectedKey) {
									g.setColor(Color.YELLOW);
									//g.drawRect(x - 5, ym - 5, 10, 10);
									g.drawRect(x - 6, ym - 6, 12, 12);
								}
							} else {
								g.setColor((mk.getPosition() == pos) ? Color.WHITE : Color.ORANGE);
								g.fillRect(x - 2, ym - 2, 5, 5);
									if (mk == selectedKey) {
									g.setColor(Color.YELLOW);
									//g.drawRect(x - 3, ym - 3, 6, 6);
									g.drawRect(x - 4, ym - 4, 8, 8);
								}
							}
						}
					}
					g.setColor(Color.WHITE);
					g.drawString(motionCurve.getName() + " [" + motionCurve.getInterpolationMethod().toString() + "]", 4, yy + 16);
				}
				y++;
			}
		}
		//if (animObject instanceof AnimModel) {
		//	Model model = ((AnimModel) animObject).getModel();
		//	for (Iterator it = model.getMorphList().iterator(); it.hasNext(); ) {
		//		Morph morph = (Morph) it.next();
		//		MotionCurve2.Float motionCurve = ((MotionCurveSet.Model) motionCurveSet).morph(morph);
		//		int yy = (int) (pixelHeight * ((y - viewTop) / viewHeight));
		//		int h = (int) ((double) pixelHeight / viewHeight * 0.9);
		//		g.setColor(new Color(0x0000cc));
		//		g.fillRect(0, yy, pixelWidth, h);
		//		
		//		g.setColor(new Color(0x7777ff));
		//		if (yy > -h && yy < pixelHeight + h) {
		//			for (int x = 0; x < pixelWidth; x++) {
		//				float t = viewLeft + (float) x * viewWidth / (float) pixelWidth;
		//				float v = motionCurve.getFloatAt(t);
		//				int yv = yy + (int) (h * (1.0 - ((v - morph.getMin()) / (morph.getMax() - morph.getMin()))));
		//				int y0 = yy + (int) (h * (1.0 - ((0.0 - morph.getMin()) / (morph.getMax() - morph.getMin()))));
		//				g.drawLine(x, y0, x, yv);
		//				
		//				//g.drawString(MotionCurve.MotionKey.positionString(t), x, yy + 32);
		//			}
		//			for (int i = 0, n = motionCurve.getKeyCount(); i < n; i++) {
		//				MotionKey2.Float mk = (MotionKey2.Float) motionCurve.getKey(i);
		//				int x = (int) ((mk.getPosition() - viewLeft) * (float) pixelWidth / (float) viewWidth);
		//			//	int x = (int) (((double) mk.position / 10000.0 - viewLeft) * (double) pixelWidth / (double) viewWidth);
		//				int ym = yy + (int) (h * (1.0 - ((mk.getFloat() - morph.getMin()) / (morph.getMax() - morph.getMin()))));
		//				if (mk.getPosition() == pos) g.setColor(Color.WHITE);
		//				else g.setColor(Color.YELLOW);
		//				g.fillRect(x - 2, ym - 2, 5, 5);
		//				//if (mk == selectedKey) {
		//				//	g.setColor(Color.GREEN);
		//				//	g.drawRect(x - 3, ym - 3, 6, 6);
		//				//}
		//			}
		//		}
		//		g.setColor(Color.WHITE);
		//		g.drawString(morph.getName(), 4, yy + 16);
		//		y++;
		//	}
		//}
		
		
		X = (int) ((pos - viewLeft) * (float) pixelWidth / viewWidth);
		//g.setXORMode(Color.BLACK);
		g.setColor(Color.RED);
		g.drawLine(X, 0, X, pixelHeight - 1);
	}
	
	public void mousePressed(MouseEvent mouseEvent) {
		//int t = (int) (10000.0 * (viewLeft + (double) mouseEvent.getX() * viewWidth / (double) parent.getCanvasWidth()));
		//selectedKey = null;
		//loop:
		//if (model != null) {
		//	int pixelHeight = parent.getCanvasHeight();
		//	int pixelWidth = parent.getCanvasWidth();
		//	double y = top;
		//	{
		//		int yy = (int) (pixelHeight * ((y - viewTop) / viewHeight));
		//		int h = (int) ((double) pixelHeight / viewHeight * 0.75);
		//		int ym = yy + h;
		//		MotionCurve.PointCurve mcc = Animator.getInstance().getPositionCurveFor(Animator.getInstance().getActiveCamera());
		//		for (int i = 0, n = mcc.getKeyCount(); i < n; i++) {
		//			MotionCurve.MotionKey mk = mcc.getKey(i);
		//			int x = (int) (((double) mk.position / 10000.0 - viewLeft) * (double) pixelWidth / (double) viewWidth);
		//			if (mouseEvent.getX() > x - 5 && mouseEvent.getX() < x + 5 && mouseEvent.getY() > ym - 5 && mouseEvent.getY() < ym + 5) {
		//				t = mk.position;
		//				selectedKey = mk;
		//				selectedCamera = Animator.getInstance().getActiveCamera();
		//				break loop;
		//			}
		//		}
		//	}
		//	selectedCamera = null;
		//	y++;
		//	for (Iterator it = model.getMorphList().iterator(); it.hasNext(); ) {
		//		Morph morph = (Morph) it.next();
		//		MotionCurve motionCurve = Animator.getInstance().getCurveFor(morph);
		//		int yy = (int) (pixelHeight * ((y - viewTop) / viewHeight));
		//		int h = (int) ((double) pixelHeight / viewHeight * 0.9);
		//		if (yy > -h && yy < pixelHeight + h) {
		//			for (int i = 0, n = motionCurve.getKeyCount(); i < n; i++) {
		//				MotionCurve.MotionKey mk = motionCurve.getKey(i);
		//				int x = (int) (((double) mk.position / 10000.0 - viewLeft) * (double) pixelWidth / (double) viewWidth);
		//				int ym = yy + (int) (h * (1.0 - ((mk.value - morph.getMin()) / (morph.getMax() - morph.getMin()))));
		//				if (mouseEvent.getX() > x - 5 && mouseEvent.getX() < x + 5 && mouseEvent.getY() > ym - 5 && mouseEvent.getY() < ym + 5) {
		//					t = mk.position;
		//					selectedKey = mk;
		//					break loop;
		//				}
		//			}
		//		}
		//		y++;
		//	}
		//}
		//Animator.getInstance().setPosition(t);
		//parent.repaint();
		////System.out.println(MotionCurve.MotionKey.positionString(t));
		//Animator.getInstance().stop();
		
		if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
			MotionCurveSet motionCurveSet = MainFrame.getInstance().getAnimation().getCurvesetFor(animObject);
			
			int pixelHeight = parent.getCanvasHeight();
			int pixelWidth = parent.getCanvasWidth();
			float t = Math.round(viewLeft + (float) mouseEvent.getX() * viewWidth / (float) parent.getCanvasWidth());
			
//			Animator.getInstance().setActiveKey(null);
//			Animator.getInstance().setActiveCurve(null);
			float y = top;
			loop:
			if (motionCurveSet != null) {
				for (int c = 0, nc = motionCurveSet.motionCurveList.size(); c < nc; c++) {
					MotionCurve2 motionCurve = (MotionCurve2) motionCurveSet.motionCurveList.get(c);
					int yy = (int) (pixelHeight * ((y - viewTop) / viewHeight) + (double) pixelHeight / viewHeight * 0.05);
					int h = (int) ((double) pixelHeight / viewHeight * 0.9);
					if (yy > -h && yy < pixelHeight + h) {
						for (int k = 0, nk = motionCurve.getKeyCount(); k < nk; k++) {
							MotionKey2 mk = motionCurve.getKey(k);
							int x = (int) ((mk.getPosition() - viewLeft) * (float) pixelWidth / (float) viewWidth);
							int ym;
							if (mk instanceof MotionKey2.Float) {
								float value = ((MotionKey2.Float) mk).getFloat();
								float min = ((MotionCurve2.Float) motionCurve).getMin();
								float max = ((MotionCurve2.Float) motionCurve).getMax();
								value = clamp(value, min, max);
								ym = yy + (int) (h * (1.0 - (value - min) / (max - min)));
							}
							else ym = yy + h / 2;
							
							if (mouseEvent.getX() > x - 6 && mouseEvent.getX() < x + 6 && mouseEvent.getY() > ym - 6 && mouseEvent.getY() < ym + 6) {
								t = mk.getPosition();
//								Animator.getInstance().setActiveKey(mk);
//								Animator.getInstance().setActiveCurve(motionCurve);
								break loop;
							}
						}
//						if (mouseEvent.getY() >= yy && mouseEvent.getY() <= yy + h) Animator.getInstance().setActiveCurve(motionCurve);
					}
					y++;
				}
				
			}
//			Animator.getInstance().setPosition(t);
		}
		else if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
			MotionCurveSet motionCurveSet = MainFrame.getInstance().getAnimation().getCurvesetFor(animObject);
			int pixelHeight = parent.getCanvasHeight();
			float y = top;
			if (motionCurveSet != null) {
				for (int c = 0, nc = motionCurveSet.motionCurveList.size(); c < nc; c++) {
					MotionCurve2 motionCurve = (MotionCurve2) motionCurveSet.motionCurveList.get(c);
					int yy = (int) (pixelHeight * ((y - viewTop) / viewHeight) + (double) pixelHeight / viewHeight * 0.05);
					int h = (int) ((double) pixelHeight / viewHeight * 0.9);
					if (mouseEvent.getY() >= yy && mouseEvent.getY() <= yy + h) {
						if (motionCurve.getInterpolationMethod() == MotionCurve2.CUBIC) motionCurve.setInterpolationMethod(MotionCurve2.LINEAR);
						else if (motionCurve.getInterpolationMethod() == MotionCurve2.LINEAR) motionCurve.setInterpolationMethod(MotionCurve2.CUBIC);
					}
					y++;
				}
			}
		}
		
		parent.repaint();
	}
	
	private float clamp(float v, float min, float max) {
		return Math.min(max, Math.max(min, v));
	}
	
	//public MotionCurve.MotionKey getSelectedKey() {
	//	return selectedKey;
	//}
	//
	//public void setSelectedKey(MotionCurve.MotionKey key) {
	//	selectedKey = key;
	//}
	
	//public void updateTimeline() {
	//	int pixelHeight = parent.getCanvasHeight();
	//	int pixelWidth = parent.getCanvasWidth();
	//	Graphics g = parent.getGraphics();
	//	g.setClip(0, 0, pixelWidth, pixelHeight);
	//	g.setXORMode(Color.BLACK);
	//	g.drawLine(X, 0, X, pixelHeight - 1);
	//	X = (int) (((double) Animator.getInstance().getPosition() / 10000.0 - viewLeft) * (double) pixelWidth / viewWidth);
	//	g.drawLine(X, 0, X, pixelHeight - 1);
	//}
}
