package jpatch.entity;

import java.util.*;
import jpatch.auxilary.*;

/**
* This class is used to store additional, renderer specific attributes.
* It's used by JPatchMatrerial, AnimLight and AnimModel
**/

public class RenderExtension {
	private final Map mapDefault = new HashMap();
	private Map mapRenderStrings = new HashMap();
	
	public RenderExtension(String[] defaults) {
		if (defaults.length % 2 != 0) throw new IllegalArgumentException();
		for (int i = 0; i < defaults.length; i += 2) {
			mapDefault.put(defaults[i], defaults[i + 1]);
			mapRenderStrings.put(defaults[i], defaults[i + 1]);
		}
	}
	
	public void setRenderString(String format, String version, String renderString) {
		String key = format;
		if (mapRenderStrings.containsKey(key)) {
			mapRenderStrings.remove(key);
		}
		mapRenderStrings.put(key,renderString);
	}
	
	public String getRenderString(String format, String version) {
		String key = format;
		if (mapRenderStrings.containsKey(key)) {
			return (String)mapRenderStrings.get(key);
		}
		return null;
	}
	
	public StringBuffer xml(String prefix) {
		StringBuffer sb = new StringBuffer();
		Iterator iterator = mapRenderStrings.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String)iterator.next();
			String renderString = (String)mapRenderStrings.get(key);
			if (!renderString.equals((String)mapDefault.get(key))) {
				String r;
				r = renderString.replaceAll("&","&amp;");
				r = r.replaceAll("\"","&quot;");
				r = r.replaceAll(">","&gt;");
				r = r.replaceAll("<","&lt;");
				sb.append(prefix).append("<renderer format=").append(XMLutils.quote(key)).append(">");
				sb.append(r);
				sb.append("</renderer>").append("\n");
			}
		}
		return sb;
	}
	
	public Map getMap() {
		return mapRenderStrings;
	}
	
	public void setMap(Map map) {
		mapRenderStrings = map;
	}
}
