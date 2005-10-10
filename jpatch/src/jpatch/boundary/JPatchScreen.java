package jpatch.boundary;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jpatch.auxilary.*;
import jpatch.entity.*;
import jpatch.boundary.action.*;
import jpatch.boundary.mouse.*;
import jpatch.boundary.tools.*;

public final class JPatchScreen extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int SINGLE = 1;
	public static final int HORIZONTAL_SPLIT = 2;
	public static final int VERTICAL_SPLIT = 3;
	public static final int QUAD = 4;
	
	public static final int LIGHT_OFF = 0;
	public static final int LIGHT_SIMPLE = 1;
	public static final int LIGHT_HEAD = 2;
	public static final int LIGHT_THREE_POINT = 3;
	
	public static final int NUMBER_OF_VIEWPORTS = 4;
	
	public static final int JAVA2D = 0;
	public static final int SOFTWARE = 1;
	public static final int OPENGL = 2;
	
	private Viewport2 activeViewport;
	
	//private JPatchCanvas[] aComponent = new JPatchCanvas[NUMBER_OF_VIEWPORTS];
	private JPatchDrawable2[] aDrawable = new JPatchDrawable2[NUMBER_OF_VIEWPORTS];
	private ViewDefinition[] aViewDef;
	private Viewport2[] aViewport = new Viewport2[NUMBER_OF_VIEWPORTS];
	
	private boolean bSnapToGrid = JPatchSettings.getInstance().bGridSnap;
	private float fGridSpacing = JPatchSettings.getInstance().fGridSpacing;
	
	private int iMode;
	//protected ControlPoint cpCursor = new ControlPoint(0,0,0);
	
	private boolean bBackfaceNormalFlip = false;
	private int iBackfaceCulling = 0;
	private boolean bSynchronized = JPatchSettings.getInstance().bSyncWindows;
	private int iLightMode = JPatchSettings.getInstance().iLightingMode;
	private boolean bStickyLight = JPatchSettings.getInstance().bStickyLight;
	private JPatchTool tool;
	
	private PopupMouseListener popupMouseListener = new PopupMouseListener(MouseEvent.BUTTON3);
	private MoveZoomRotateMouseAdapter moveZoomRotateMouseAdapter = new MoveZoomRotateMouseAdapter();
	
	private boolean bPopupEnabled;
	private ActiveViewportMouseAdapter activeViewportMouseAdapter = new ActiveViewportMouseAdapter();
	private boolean bShowTangents = false;
	private TangentTool tangentTool = new TangentTool();
	
	public JPatchScreen(Model model,int mode,ViewDefinition[] viewDefinitions) {
		aViewDef = viewDefinitions;
		setLightingMode(iLightMode);
		initScreen();
		setFocusable(false);
		setMode(mode);
		
		enablePopupMenu(true);
//		activeViewport = aDrawable[0];
		snapToGrid(bSnapToGrid);
	}
	
	public void initScreen() {
		int mode = iMode;
		setMode(0);
		if (JPatchSettings.getInstance().iRealtimeRenderer == OPENGL && !JoglInstall.isInstalled())
			JOptionPane.showMessageDialog(MainFrame.getInstance(), new JLabel("Can't use OpenGL display: native JOGL libraries not found."), "Warning", JOptionPane.WARNING_MESSAGE);
		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
			final int I = i;
			JPatchDrawableEventListener listener = new JPatchDrawableEventListener() {
				public void display(JPatchDrawable2 drawable) {
					aViewport[I].prepare();
					aViewport[I].drawRotoscope();
					aViewport[I].drawGrid();
					aViewport[I].drawModel(MainFrame.getInstance().getModel());
					aViewport[I].drawOrigin();
					if (tool != null)
						aViewport[I].drawTool(tool);
					if (bShowTangents)
						aViewport[I].drawTool(tangentTool);
					aViewport[I].drawInfo();
					if (aViewport[I] == activeViewport)
						aViewport[I].drawActiveBorder();
				}
			};
			switch (JPatchSettings.getInstance().iRealtimeRenderer) {
				case JAVA2D: aDrawable[i] = new JPatchDrawable2D(listener, false); break;
				case SOFTWARE: aDrawable[i] = new JPatchDrawable3D(listener, false); break;
				case OPENGL: {
					if (JoglInstall.isInstalled())
						aDrawable[i] = new JPatchDrawableGL(listener, false);
					else
						aDrawable[i] = new JPatchDrawable3D(listener, false);
				} break;
			}
			aDrawable[i].setProjection(JPatchDrawable2.ORTHOGONAL);
			aViewport[i] = new Viewport2(aDrawable[i], aViewDef[i]);
			aViewDef[i].setDrawable(aDrawable[i]);
			//aComponent[i] = new JPatchCanvas(model,aViewDef[i]);
			add(aDrawable[i].getComponent());
			aDrawable[i].getComponent().setFocusable(false);
			//aViewDef[i].setLighting(RealtimeLighting.createThreepointLight()); // FIXME
			activeViewport = aViewport[0];
		}
		setMode(mode);
	}
	
	public void switchRenderer(int renderer) {
		JPatchSettings.getInstance().iRealtimeRenderer = renderer;
		initScreen();
		update_all();
	}
	
	public ViewDefinition getViewDefinition(Component component) {
		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
			if (aDrawable[i].getComponent() == component)
				return aViewDef[i];
		}
		throw new IllegalArgumentException("component not found");
	}
	
	public Viewport2 getViewport(Component component) {
		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
			if (aDrawable[i].getComponent() == component)
				return aViewport[i];
		}
		throw new IllegalArgumentException("component not found");
	}
	
	public Viewport2 getActiveViewport() {
		return activeViewport;
	}
	
	public void setActiveViewport(Component component) {
		setActiveViewport(getViewport(component));
	}
	
	public void setActiveViewport(Viewport2 viewport) {
		if (viewport != activeViewport) {
			Viewport2 old = activeViewport;
			activeViewport = viewport;
			old.getDrawable().display();
			viewport.getDrawable().display();
		}
	}
	
	public boolean isSynchronized() {
		return bSynchronized;
	}
	
	public boolean flipBackfacingNormals() {
		return bBackfaceNormalFlip;
	}
	
	public int cullBackfacingPolys() {
		return iBackfaceCulling;
	}
	
	public void flipBackfacingNormals(boolean flip) {
//		bBackfaceNormalFlip = flip;
//		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
//			JPatchDrawable drawable = aComponent[i].getDrawable();
//			if (drawable != null) drawable.getLighting().setBackfaceNormalFlip(flip);
//			//if (drawable instanceof ZBufferRenderer) ((ZBufferRenderer) drawable).setBackfaceNormalFlip(flip);
//		}
	}
	
	public void cullBackfacingPolys(int mode) {
//		iBackfaceCulling = mode;
//		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
//			JPatchDrawable drawable = aComponent[i].getDrawable();
//			if (drawable instanceof ZBufferRenderer) ((ZBufferRenderer) drawable).setCulling(mode);
//		}
	}
	
	public void synchronize(boolean sync) {
		bSynchronized = sync;
	}
	
	public boolean showTangents() {
		return bShowTangents;
	}
	
	public void showTangents(boolean enable) {
		bShowTangents = enable;
	}
	
	public TangentTool getTangentTool() {
		return tangentTool;
	}
	
	public void setLightingMode(int mode) {
		iLightMode = mode;
		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
			switch(iLightMode) {
				case LIGHT_OFF:
					aViewDef[i].setLighting(null);
					break;
				case LIGHT_SIMPLE:
					aViewDef[i].setLighting(RealtimeLighting.createSimpleLight());
					break;
				case LIGHT_HEAD:
					aViewDef[i].setLighting(RealtimeLighting.createHeadLight());
					break;
				case LIGHT_THREE_POINT:
					aViewDef[i].setLighting(RealtimeLighting.createThreepointLight());
					break;
			}
		}
		update_all();
	}
	
	public int getLightingMode() {
		return iLightMode;
	}
	
//	public void setActiveViewport(Viewport viewport) {
//		if (viewport != activeViewport) {
//			activeViewport = viewport;
//			//for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
//			//	aComponent[i].drawActiveViewportMarker(aComponent[i] == viewport);
//			//}
//			update_all();
//		}
//	}
	
//	public Viewport getActiveViewport() {
//		return activeViewport;
//	}
	
	public void setStickyLight(boolean sticky) {
		bStickyLight = sticky;
//		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
//			aComponent[i].getLighting().setStickyLight(sticky);
//		}
//		update_all();
	}
	
	public boolean snapToGrid() {
		return bSnapToGrid;
	}
	
	public void snapToGrid(boolean enable) {
		bSnapToGrid = enable;
		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
//			aComponent[i].getGrid().snap(enable);
		}
		update_all();
		JPatchSettings.getInstance().bGridSnap = enable;
	}
	
	public float getGridSpacing() {
		return fGridSpacing;
	}
	
	public void setGridSpacing(float gridSpacing) {
		fGridSpacing = gridSpacing;
		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
//			aComponent[i].getGrid().setSpacing(gridSpacing);
		}
		update_all();
	}
	
	public boolean isStickyLight() {
		return bStickyLight;
	}
	
	public void single_update(Component component) {
		if (bSynchronized) {
			update_all();
		} else {
			getViewDefinition(component).getDrawable().display();
		}
	}
	
	public void full_update() {
		//System.out.println("full_update()");
		if (!bSynchronized) {
			update_all();
		}
	}
	
	public void update_all() {
		switch(iMode) {
			case SINGLE:
				aDrawable[0].display();
				break;
			case HORIZONTAL_SPLIT:
				aDrawable[0].display();
				aDrawable[1].display();
				break;
			case VERTICAL_SPLIT:
				aDrawable[0].display();
				aDrawable[2].display();
				break;
			case QUAD:
				aDrawable[0].display();
				aDrawable[1].display();
				aDrawable[2].display();
				aDrawable[3].display();
				break;
		}
	}
	
	public void zoomToFit_all() {
		switch(iMode) {
			case SINGLE:
				ZoomToFitAction.zoomToFit(aViewport[0]);
				break;
			case HORIZONTAL_SPLIT:
				ZoomToFitAction.zoomToFit(aViewport[0]);
				ZoomToFitAction.zoomToFit(aViewport[1]);
				break;
			case VERTICAL_SPLIT:
				ZoomToFitAction.zoomToFit(aViewport[0]);
				ZoomToFitAction.zoomToFit(aViewport[2]);
				break;
			case QUAD:
				ZoomToFitAction.zoomToFit(aViewport[0]);
				ZoomToFitAction.zoomToFit(aViewport[1]);
				ZoomToFitAction.zoomToFit(aViewport[2]);
				ZoomToFitAction.zoomToFit(aViewport[3]);
				break;
		}
	}
	
//	public void prepareBackground(Component component) {
//		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//		if (bSynchronized) {
//			for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
//				if (aComponent[i].isVisible()) {
//					((JPatchCanvas)aComponent[i]).prepareBackground();
//				}
//			}
//		} else {
//			((JPatchCanvas)component).prepareBackground();
//		}
//		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//	}
//	
//	public void clearBackground() {
//		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
//			//if (aComponent[i].isVisible()) {
//				((JPatchCanvas)aComponent[i]).clearBackground();
//			//}
//		}
//	}
	
	//public void set3DCursor(Point3f cursor) {
	//	cpCursor.setPosition(cursor);
	//}
	//public void set3DCursor(float x, float y, float z) {
	//	cpCursor.setPosition(x,y,z);
	//}
	//public ControlPoint get3DCursor() {
	//	return cpCursor;
	//}
	
	public void addMouseListeners(MouseListener mouseAdapter) {
		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
			aDrawable[i].getComponent().addMouseListener(mouseAdapter);
		}
		MainFrame.getInstance().getDefaultToolTimer().stop();
	}
	
	public void setTool(JPatchTool tool) {
		this.tool = tool;
		removeAllMouseListeners();
		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
//			((JPatchCanvas)aComponent[i]).setTool(tool);
			aViewport[i].setTool(tool);
		}
		update_all();
	}
	
	public JPatchTool getTool() {
		return tool;
	}
	
	public void removeAllMouseListeners() {
		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
			MouseListener[] aMouseListener = aDrawable[i].getComponent().getMouseListeners();
			MouseMotionListener[] aMouseMotionListener = aDrawable[i].getComponent().getMouseMotionListeners();
			MouseWheelListener[] aMouseWheelListener = aDrawable[i].getComponent().getMouseWheelListeners();
			for (int m = 0; m < aMouseListener.length; m++) {
				aDrawable[i].getComponent().removeMouseListener(aMouseListener[m]);
			}
			for (int m = 0; m < aMouseMotionListener.length; m++) {
				aDrawable[i].getComponent().removeMouseMotionListener(aMouseMotionListener[m]);
			}
			for (int m = 0; m < aMouseWheelListener.length; m++) {
				aDrawable[i].getComponent().removeMouseWheelListener(aMouseWheelListener[m]);
			}
		}
		enablePopupMenu(false);
		enablePopupMenu(true);
		addMMBListener();
	}
	/*
	public void rerender() {
		switch(mode) {
			case SINGLE:
				aComponent[0].render();
			break;
			case HORIZONTAL_SPLIT:
				aComponent[0].render();
				aComponent[1].render();
			break;
			case VERTICAL_SPLIT:
				aComponent[0].render();
				aComponent[2].render();
			break;
			case QUAD:
				aComponent[0].render();
				aComponent[1].render();
				aComponent[2].render();
				aComponent[3].render();
			break;
		}
	}
	*/
	public void enablePopupMenu(boolean enable) {
		//System.out.println("popup: " + bPopupEnabled + " " + enable);
		if (enable != bPopupEnabled) {
			for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
				if (enable) {
					aDrawable[i].getComponent().addMouseListener(popupMouseListener);
				} else {
					aDrawable[i].getComponent().removeMouseListener(popupMouseListener);
				}
			}
			bPopupEnabled = enable;
		}
	}
	
	public void addMMBListener() {
		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
			aDrawable[i].getComponent().addMouseListener(moveZoomRotateMouseAdapter);
			aDrawable[i].getComponent().addMouseWheelListener(moveZoomRotateMouseAdapter);
			aDrawable[i].getComponent().addMouseListener(activeViewportMouseAdapter);
			aDrawable[i].getComponent().addMouseWheelListener(activeViewportMouseAdapter);
			if (bShowTangents) {
				aDrawable[i].getComponent().addMouseListener(tangentTool);
			}
		}
	}
	
	public int getMode() {
		return iMode;
	}
	
	public void setMode(int mode) {
		iMode = mode;
		int h = getHeight();
		int w = getWidth();
		int h2 = h/2;
		int w2 = w/2;
//		aComponent[0].setVisible(false);
//		aComponent[1].setVisible(false);
//		aComponent[2].setVisible(false);
//		aComponent[3].setVisible(false);
//		aComponent[0].clearImage();
//		aComponent[1].clearImage();
//		aComponent[2].clearImage();
//		aComponent[3].clearImage();
		//update_all();
		if (h > 0 && w > 0) {
			switch(mode) {
				case 0:
					aDrawable[0].getComponent().setVisible(false);
					aDrawable[1].getComponent().setVisible(false);
					aDrawable[2].getComponent().setVisible(false);
					aDrawable[3].getComponent().setVisible(false);
				break;
				case SINGLE:
//					activeViewport = aComponent[0];
					aDrawable[0].getComponent().setBounds(0,0,w,h);
					aDrawable[0].getComponent().setVisible(true);
					aDrawable[1].getComponent().setVisible(false);
					aDrawable[2].getComponent().setVisible(false);
					aDrawable[3].getComponent().setVisible(false);
				break;
				case HORIZONTAL_SPLIT:
//					if (activeViewport != aComponent[0] && activeViewport != aComponent[1]) {
//						activeViewport = aComponent[0];
//					}
					aDrawable[0].getComponent().setBounds(0,0,w2 - 1,h);
					aDrawable[0].getComponent().setVisible(true);
					aDrawable[1].getComponent().setBounds(w2 + 1,0,w2 - 1,h);
					aDrawable[1].getComponent().setVisible(true);
					aDrawable[2].getComponent().setVisible(false);
					aDrawable[3].getComponent().setVisible(false);
				break;
				case VERTICAL_SPLIT:
//					if (activeViewport != aComponent[0] && activeViewport != aComponent[2]) {
//						activeViewport = aComponent[0];
//					}
					aDrawable[0].getComponent().setBounds(0,0,w,h2 - 1);
					aDrawable[0].getComponent().setVisible(true);
					aDrawable[1].getComponent().setVisible(false);
					aDrawable[2].getComponent().setBounds(0,h2 + 1,w,h2 - 1);
					aDrawable[2].getComponent().setVisible(true);
					aDrawable[3].getComponent().setVisible(false);
				break;
				case QUAD:
					aDrawable[0].getComponent().setBounds(0,0,w2 - 1,h2 - 1);
					aDrawable[0].getComponent().setVisible(true);
					aDrawable[1].getComponent().setBounds(w2 + 1,0,w2 - 1,h2 - 1);
					aDrawable[1].getComponent().setVisible(true);
					aDrawable[2].getComponent().setBounds(0,h2 + 1,w2 - 1,h2 - 1);
					aDrawable[2].getComponent().setVisible(true);
					aDrawable[3].getComponent().setBounds(w2 + 1,h2 + 1,w2 - 1,h2 - 1);
					aDrawable[3].getComponent().setVisible(true);

				break;
			}
		}
		
		JPatchSettings.getInstance().iScreenMode = iMode;
		//JPatchSettings.getInstance().saveSettings();
		
		//update_all();
		/*
		iMode = mode;
		removeAll();
		switch(mode) {
			case SINGLE:
				setLayout(new GridLayout(1,1));
				add(aComponent[0], BorderLayout.CENTER);
				break;
			case HORIZONTAL_SPLIT:
				setLayout(new GridLayout(1,2));
				add(aComponent[0]);
				add(aComponent[1]);
				break;
			case VERTICAL_SPLIT:
				setLayout(new GridLayout(1,2));
				add(aComponent[0]);
				add(aComponent[2]);
				break;
			case QUAD:
				setLayout(new GridLayout(2,2));
				add(aComponent[0]);
				add(aComponent[1]);
				add(aComponent[2]);
				add(aComponent[3]);
				break;
		}
		validate();
		*/
	}
	
	public void doLayout() {
		setMode(iMode);
	}
	
	public void resetMode(int mode) {
		setMode(mode);
	}
}
