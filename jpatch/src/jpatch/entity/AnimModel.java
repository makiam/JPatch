package jpatch.entity;

public class AnimModel extends AnimObject {
	
	protected RenderExtension re = new RenderExtension(new String[] {
		"povray", "",
		"renderman", "Attribute \"visibility\" \"string transmission\" [\"shader\"]\n"
	});
	protected Model model;
	protected int iSubdivisionOffset = 0;
	
	public AnimModel() { }
	
	public AnimModel(Model model) {
		strName = model.getName();
		this.model = model;
	}
	
	public AnimModel(String name, Model model) {
		strName = name;
		this.model = model;
	}
	
	public Model getModel() {
		return model;
	}
	
	public String toString() {
		return strName;
	}
	
	public void setModel(Model model) {
		this.model = model;
	}
	
	public void setName(String name) {
		strName = name;
	}
	
	public void setSubdivisionOffset(int offset) {
		iSubdivisionOffset = offset;
	}
	
	public int getSubdivisionOffset() {
		return iSubdivisionOffset;
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
