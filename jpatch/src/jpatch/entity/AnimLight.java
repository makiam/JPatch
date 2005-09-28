package jpatch.entity;

import javax.vecmath.*;

public class AnimLight extends AnimObject {
	
	protected RenderExtension re = new RenderExtension(new String[] {
		"povray", "light_source {\n\t$position color rgb $color * $intensity\n\tparallel\n\t#if ($size > 0)\n\t\tarea_light <$size,0,0>,<0,$size,0>,10,10 adaptive 1 jitter circular orient\n\t#end\n}\n",
		"renderman", "Attribute \"light\" \"shadows\" \"on\"\nLightSource \"distantlight\" $number \"from\" $position \"intensity\" $intensity \"lightcolor\" $color\n"
	});
	protected Color3f color = new Color3f(1,1,1);
	protected float fIntensity = 1;
	protected float fSize = 0;
	protected int iNumber;
	protected boolean bActive = true;
	
	public AnimLight() {
		strName = "New Lightsource";
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
}
