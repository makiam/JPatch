package jpatch.boundary;

import java.awt.*;
import java.awt.event.*;

public class VirtualCanvas {
	float width, height, left, top, viewWidth, viewHeight, viewLeft, viewTop;
	SmartScrollPane parent;
	
	public VirtualCanvas(SmartScrollPane parent) {
		this.parent = parent;
		width = 1000;
		height = 100;
		left = 0;
		top = 0;
		
		viewWidth = 500;
		viewHeight = 100;
		viewLeft = 0;
		viewTop = 0;
	}
	
	public void paint(Graphics g) {
		//
		//int pixelHeight = parent.getCanvasHeight();
		//int pixelWidth = parent.getCanvasWidth();
		//g.setClip(0, 0, pixelWidth, pixelHeight);
		//g.setColor(Color.BLACK);
		//g.fillRect(0, 0, pixelWidth, pixelHeight);
		//
		//g.setColor(Color.WHITE);
		//int pl = (int) ((left - viewLeft) / viewWidth * pixelWidth);
		//int pt = (int) ((top - viewTop) / viewHeight * pixelHeight);
		//int pw = (int) (pixelWidth * (width / viewWidth));
		//int ph = (int) (pixelHeight * (height / viewHeight));
                //
		//g.fillRect(pl, pt, pw, ph);
		//g.setColor(Color.RED);
		//g.drawRect(pl + 5, pt + 5, pw - 10, ph - 10);
		//g.drawRect(pl + 10, pt + 10, pw - 20, ph - 20);
		//g.drawRect(pl + 20, pt + 20, pw - 40, ph - 40);
	}
	
	void repaint() {
		paint(parent.getGraphics());
	}
	
	float getWidth() {
		return width;
	}
	
	float getHeight() {
		return height;
	}
	
	float getLeft() {
		return left;
	}
	
	float getTop() {
		return top;
	}
	
	float getViewWidth() {
		return viewWidth;
	}
	
	float getViewHeight() {
		return viewHeight;
	}
	
	float getViewLeft() {
		return viewLeft;
	}
	
	float getViewTop() {
		return viewTop;
	}
	
	//void setWidth(float width) {
	//	this.width = width;
	//}
	//
	//void setHeight(float height) {
	//	this.height = height;
	//}
	//
	//void setLeft(float left) {
	//	this.left = left;
	//}
	//
	//void setTop(float top) {
	//	this.top = top;
	//}
	
	float getSliderPosH() {
		if ((width - viewWidth) == 0 || viewLeft == 0) return 0;
		return (viewLeft - left) / (width - viewWidth);
	}
	
	float getSliderSizeH() {
		return viewWidth / width;
	}
	
	float getSliderPosV() {
		if ((height - viewHeight) == 0 || viewTop == 0) return 0;
		return viewTop / (height - viewHeight);
	}
	
	float getSliderSizeV() {
		return viewHeight / height;
	}
	
	void setViewLeft(float vl) {
		viewLeft = (vl < left) ? left : vl;
		if (viewLeft + viewWidth > left + width) viewLeft = left + width - viewWidth;
	}
	
	void setViewWidth(float vw) {
		viewWidth = (vw > width) ? width : vw;
	}
	
	void setViewTop(float vt) {
		viewTop = (vt < top) ? top : vt;
		if (viewTop + viewHeight > top + height) viewTop = top + height - viewHeight;
	}
	
	void setViewHeight(float vh) {
		viewHeight = (vh > height) ? height : vh;
	}
	
	public void mousePressed(MouseEvent mouseEvent) {
		;
	}
}
