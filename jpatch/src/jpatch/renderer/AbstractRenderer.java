package jpatch.renderer;

import jpatch.entity.*;

public abstract class AbstractRenderer implements JPatchRenderer {
	protected String strRenderer;
	protected String strExtension;
	protected String strDefaultShaderString;
	protected String strShaderString;
	
	public String getRenderer() {
		return strRenderer;
	}
	
	public String getExtension() {
		return strExtension;
	}
	
	public String getDefaultShaderString() {
		return strDefaultShaderString;
	}
	
	public String getShaderString() {
		return strShaderString;
	}
	
	public void setShaderString(String shaderString) {
		strShaderString = shaderString;
	}
	
	public void resetShaderString() {
		strShaderString = strDefaultShaderString;
	}
	
	public String shader(MaterialProperties materialProperties) {
		String s = strShaderString;
		s = s.replaceAll("\\$opacity",String.valueOf(materialProperties.opacity()));
		s = s.replaceAll("\\$ambient",String.valueOf(materialProperties.ambient));
		s = s.replaceAll("\\$diffuse",String.valueOf(materialProperties.diffuse));
		s = s.replaceAll("\\$brilliance",String.valueOf(materialProperties.brilliance));
		s = s.replaceAll("\\$specular",String.valueOf(materialProperties.specular));
		s = s.replaceAll("\\$roughness",String.valueOf(materialProperties.roughness));
		s = s.replaceAll("\\$metallic",String.valueOf(materialProperties.metallic));
		s = s.replaceAll("\\$reflection_min",String.valueOf(materialProperties.reflectionMin));
		s = s.replaceAll("\\$reflection_max",String.valueOf(materialProperties.reflectionMax));
		s = s.replaceAll("\\$reflection_falloff",String.valueOf(materialProperties.reflectionFalloff));
		s = s.replaceAll("\\$conserve_energy",String.valueOf(materialProperties.conserveEnergy));
		s = s.replaceAll("\\$refraction",String.valueOf(materialProperties.refraction));
		s = s.replaceAll("\\$r",String.valueOf(materialProperties.red));
		s = s.replaceAll("\\$g",String.valueOf(materialProperties.green));
		s = s.replaceAll("\\$b",String.valueOf(materialProperties.blue));
		s = s.replaceAll("\\$filter",String.valueOf(materialProperties.filter));
		s = s.replaceAll("\\$transmit",String.valueOf(materialProperties.transmit));
		return s;
	}
	
	public static String shader(MaterialProperties materialProperties, String s) {
		s = s.replaceAll("\\$opacity",String.valueOf(materialProperties.opacity()));
		s = s.replaceAll("\\$ambient",String.valueOf(materialProperties.ambient));
		s = s.replaceAll("\\$diffuse",String.valueOf(materialProperties.diffuse));
		s = s.replaceAll("\\$brilliance",String.valueOf(materialProperties.brilliance));
		s = s.replaceAll("\\$specular",String.valueOf(materialProperties.specular));
		s = s.replaceAll("\\$roughness",String.valueOf(materialProperties.roughness));
		s = s.replaceAll("\\$metallic",String.valueOf(materialProperties.metallic));
		s = s.replaceAll("\\$reflection_min",String.valueOf(materialProperties.reflectionMin));
		s = s.replaceAll("\\$reflection_max",String.valueOf(materialProperties.reflectionMax));
		s = s.replaceAll("\\$reflection_falloff",String.valueOf(materialProperties.reflectionFalloff));
		s = s.replaceAll("\\$conserve_energy",String.valueOf(materialProperties.conserveEnergy));
		s = s.replaceAll("\\$refraction",String.valueOf(materialProperties.refraction));
		s = s.replaceAll("\\$r",String.valueOf(materialProperties.red));
		s = s.replaceAll("\\$g",String.valueOf(materialProperties.green));
		s = s.replaceAll("\\$b",String.valueOf(materialProperties.blue));
		s = s.replaceAll("\\$filter",String.valueOf(materialProperties.filter));
		s = s.replaceAll("\\$transmit",String.valueOf(materialProperties.transmit));
		return s;
	}
	
	public static String light(AnimLight light, String s) {
		s = s.replaceAll("\\$number",String.valueOf(light.getNumber()));
		s = s.replaceAll("\\$intensity",String.valueOf(light.getIntensity()));
		s = s.replaceAll("\\$size",String.valueOf(light.getSize()));
		return s;
	}
	
	public abstract void writeToFile(String filename);
}
