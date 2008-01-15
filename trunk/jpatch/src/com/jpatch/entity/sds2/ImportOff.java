package com.jpatch.entity.sds2;

import com.jpatch.entity.*;

import java.io.*;
import java.util.*;

import javax.vecmath.*;

public class ImportOff {
	public static Sds importOff(InputStream in) throws IOException {
		Sds sds = new Sds();
		Material material = new BasicMaterial(new Color3f(0, 0, 1));
		
		List<BaseVertex> vertexList = new ArrayList<BaseVertex>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String[] tokens;
		String line;
		if (!(line = reader.readLine()).equals("OFF")) {
			throw new IOException("Illegal file format");
		}
		line = reader.readLine();
		tokens = line.trim().split("\\s+");
		int numVertices = Integer.parseInt(tokens[0]);
		int numFaces = Integer.parseInt(tokens[1]);
		for (int i = 0; i < numVertices; i++) {
			line = reader.readLine();
			tokens = line.trim().split("\\s+");
			BaseVertex vertex = new BaseVertex(Double.parseDouble(tokens[0]), Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]));
			vertexList.add(vertex);
		}
		
		for (int i = 0; i < numFaces; i++) {
			line = reader.readLine();
			if (line.equals("")) {
				continue;
			}
			tokens = line.trim().split("\\s+");
			BaseVertex[] vertices = new BaseVertex[Integer.parseInt(tokens[0])];
			for (int j = 0; j < vertices.length; j++) {
				vertices[j] = vertexList.get(Integer.parseInt(tokens[j + 1]));
			}
			Face face = sds.addFace(0, vertices);
			face.setMaterial(material);
		}
		return sds;
	}
	
	public static void main(String[] args) throws IOException {
		InputStream in = ClassLoader.getSystemResourceAsStream("off/cube.off");
		importOff(in);
	}
}
