package jpatch.entity;

import java.io.PrintStream;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.vecmath.*;

import jpatch.control.importer.*;
import jpatch.boundary.MainFrame;

public class AnimLight extends AnimObject {
	
	private static final Model lightModel = new Model();
	private static final Model spotModel = new Model();
	private boolean bParent;
	
	static {
		new JPatchImport().importModel(lightModel, ClassLoader.getSystemResource("jpatch/models/light.jpt").toString());
		new JPatchImport().importModel(spotModel, ClassLoader.getSystemResource("jpatch/models/spot.jpt").toString());
	}
	
	protected Color3f color = new Color3f(1,1,1);
	protected float fIntensity = 1;
	protected float fSize = 0;
	protected int iNumber;
	protected boolean bActive = true;
	
	public AnimLight() {
		strName = "New Lightsource";
		setRenderString("povray", "", "light_source {\n\t$position color rgb $color * $intensity\n\tparallel\n\t#if ($size > 0)\n\t\tarea_light <$size,0,0>,<0,$size,0>,10,10 adaptive 1 jitter circular orient\n\t#end\n}\n");
		setRenderString("renderman", "", "Attribute \"light\" \"shadows\" \"on\"\nLightSource \"distantlight\" $number \"from\" $position \"intensity\" $intensity \"lightcolor\" $color\n");
	}

	public Color3f getColor() {
		return color;
	}
	
	public void setColor(Color3f color) {
		this.color.set(color);
	}
	
	public float getIntensity() {
		return fIntensity;
	}
	
	public void setIntensity(float intensity) {
		fIntensity = intensity;
	}
	
	public float getSize() {
		return fSize;
	}
	
	public void setSize(float size) {
		fSize = size;
	}
	
	public void setNumber(int number) {
		iNumber = number;
	}
	
	public int getNumber() {
		return iNumber;
	}
	
	public boolean isActive() {
		return bActive;
	}
	
	public void setActive(boolean state) {
		bActive = state;
	}
	
	public void setRenderString(String format, String version, String renderString) {
		re.setRenderString(format, version, renderString);
	}
	
	public String getRenderString(String format, String version) {
		return re.getRenderString(format, version);
	}
	
	public StringBuffer renderStrings(String prefix) {
		return re.xml(prefix);
	}
	
	public void xml(PrintStream out, String prefix) {
		out.append(prefix).append("<lightsource>\n");
		out.append(prefix).append("\t<name>" + getName() + "</name>\n");
		if (!isActive())
			out.append(prefix).append("\t<inactive/>").append("\n");
		out.append(prefix).append(renderStrings("\t")).append("\n");
		MainFrame.getInstance().getAnimation().getCurvesetFor(this).xml(out, prefix + "\t");
		out.append(prefix).append("</lightsource>").append("\n");
	}
	
	public Model getModel() {
		return lightModel;
	}
	
	public void removeFromParent() {
		MainFrame.getInstance().getAnimation().removeLight(this);
	}

	public void setParent(MutableTreeNode newParent) {
		bParent = true;
	}

	public TreeNode getParent() {
		return bParent ? MainFrame.getInstance().getAnimation().getTreenodeLights() : null;
	}
}