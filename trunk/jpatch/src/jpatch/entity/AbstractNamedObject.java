package jpatch.entity;

import jpatch.entity.attributes2.*;

public abstract class AbstractNamedObject {
	public StringAttr name = new StringAttr(this.toString());
	
	public String getName() {
		return name.getString();
	}
	
	protected String getXmlName() {
		return name.getString().
				replaceAll("&","&amp;").
				replaceAll("\"","&quot;").
				replaceAll(">","&gt;").
				replaceAll("<","&lt;");
	}
}
