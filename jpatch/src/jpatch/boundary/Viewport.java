package jpatch.boundary;

import java.awt.event.*;

public interface Viewport {
	public ViewDefinition getViewDefinition();
	public void render();
	//public void reset();
	public int getHeight();
	public int getWidth();
	
	public void addComponentListener(ComponentListener l);
	public void removeComponentListener(ComponentListener l);
	/*
	public void controlPointAdded(ControlPoint cp);
	public void controlPointRemoved(ControlPoint cp);
	public void curveSegmentAdded(ControlPoint cp);
	public void curveSegmentRemoved(ControlPoint cp);
	*/
	public Grid getGrid();
}

