package jpatch.entity;

public abstract class AbstractNamedObject extends AbstractJPatchXObject {
	public Attribute.Name name = new Attribute.Name(this);
	
	public String getName() {
		return name.get();
	}
	
	protected String getXmlName() {
		return name.get().
				replaceAll("&","&amp;").
				replaceAll("\"","&quot;").
				replaceAll(">","&gt;").
				replaceAll("<","&lt;");
	}
}
