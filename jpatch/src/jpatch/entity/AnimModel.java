package jpatch.entity;

import java.util.Enumeration;

import jpatch.boundary.*;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class AnimModel extends AnimObject {
	private boolean bParent = false;
	
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

	public void removeFromParent() {
		MainFrame.getInstance().getAnimation().removeModel(this);
	}

	public void setParent(MutableTreeNode newParent) {
		bParent = true;
	}

	public TreeNode getParent() {
		return bParent ? MainFrame.getInstance().getAnimation().getTreenodeModels() : null;
	}
	
	public TreeNode getChildAt(int childIndex) {
		switch(childIndex) {
		case 0:
			return model.getTreenodeExpressions();
		case 1:
			return model.getTreenodeBones();
		}
		throw new ArrayIndexOutOfBoundsException();
	}

	public int getChildCount() {
		return 2;
	}

	public int getIndex(TreeNode node) {
		return -1;
	}

	public boolean getAllowsChildren() {
		return true;
	}

	public boolean isLeaf() {
		return false;
	}

	public Enumeration children() {
		return new Enumeration() {
			private int i = 0;
			public boolean hasMoreElements() {
				return i < 2;
			}

			public Object nextElement() {
				return getChildAt(i++);
			}
		};
	}
}
