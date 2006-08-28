package jpatch.entity;

public class Selection extends AbstractNamedObject {
	private Model model;

	public void setParent(JPatchObject parent) {
		model = (Model) parent;
	}
	
}
