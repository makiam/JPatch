package jpatch.renderer;

import jpatch.entity.*;

public interface JPatchRenderer {
	String getRenderer();
	String getExtension();
	String getDefaultShaderString();
	String getShaderString();
	void setShaderString(String shaderString);
	void resetShaderString();
	String shader(MaterialProperties materialProperties);
	void writeToFile(String filename);
}

