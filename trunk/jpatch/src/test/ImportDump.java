package test;

import java.io.*;
import java.util.*;
import jpatch.entity.*;
import jpatch.control.edit.*;

public class ImportDump {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(args[0]));
		String line;
		Map<String, ControlPoint> map = new HashMap<String, ControlPoint>();
		boolean newCurve = true;
		Model model = new Model();
		while ((line = reader.readLine()) != null) {
			if (line.equals("------------- patches -------------"))
				break;
			String parts[] = line.split("\\s+");
			if (parts.length != 13 || !parts[1].startsWith("cp")) {
				newCurve = true;
				continue;
			}
			String cp = parts[1];
			float x = Float.parseFloat(parts[10].substring(1, parts[10].length() - 1));
			float y = Float.parseFloat(parts[11].substring(0, parts[11].length() - 1));
			float z = Float.parseFloat(parts[12].substring(0, parts[12].length() - 1));
			ControlPoint point = new ControlPoint(x, y, z);
			map.put(cp, point);
			if (newCurve) {
				model.addCurve(point);
				newCurve = false;
			}
		}
		reader = new BufferedReader(new FileReader(args[0]));
		while ((line = reader.readLine()) != null) {
			if (line.equals("------------- patches -------------"))
				break;
			String parts[] = line.split("\\s+");
			if (parts.length < 13)
				continue;
			String cp = parts[1];
			if (!cp.startsWith("cp"))
				continue;
			String next = parts[2];
			String prev = parts[3];
			boolean loop = Boolean.parseBoolean(parts[4]);
			String na = parts[5];
			String pa = parts[6];
			String phook = parts[7];
			String chook = parts[8];
			float hpos = Float.parseFloat(parts[9]);
			ControlPoint point = map.get(cp);
			point.setLoop(loop);
			point.setNext(map.get(next));
			point.setPrev(map.get(prev));
			point.setNextAttached(map.get(na));
			point.setPrevAttached(map.get(pa));
			point.setParentHook(map.get(phook));
			point.setChildHook(map.get(chook));
			point.setHookPos(hpos);
		}
		reader = new BufferedReader(new FileReader(args[0]));
		boolean patch = false;
		while ((line = reader.readLine()) != null) {
			if (line.equals("------------- patches -------------")) {
				patch = true;
				continue;
			}
			if (!line.startsWith("Patch@"))
				patch = false;
			if (!patch)
				continue;
			String parts[] = line.split("\\s+");
			int p;
			for (p = 0; p < parts.length; p++)
				if (parts[p].startsWith("cp_"))
					break;
			ControlPoint[] c = new ControlPoint[parts.length - p];
			for (int i = 0; i < c.length; i++)
				c[i] = map.get(parts[i + p]);
			model.addPatch(c, null);
		}
		System.out.println(model.xml(""));
	}
}
