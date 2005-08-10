package patterns;

import java.io.*;
import bsh.*;

public class TextureParser {
	private static String texturePath;
	
	public static void setTexturePath(String path) {
		texturePath = path;
	}
	
	public static Texture parseTexture(String filename) {
		if (texturePath == null) throw new IllegalStateException("texturePath not set!");
		Pigment3D pigment = null;
		Vector3D normal = null;
		try {
			Interpreter interpreter = new Interpreter();
			System.out.println("loading texture " + texturePath + File.separatorChar + filename);
			interpreter.source(texturePath + File.separatorChar + filename);
			pigment = (Pigment3D) interpreter.get("pigment");
			normal = (Vector3D) interpreter.get("normal");
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return new Texture(pigment, normal);
	}
}
