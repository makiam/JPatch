package jpatch.entity;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;

import jpatch.boundary.*;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.vecmath.*;

public class AnimModel extends AnimObject {
	private boolean bParent = false;
	private String strFileName;
	
	protected OLDModel model;
	protected int iSubdivisionOffset = 0;
	
	private Transformable anchor;
	private Matrix4d m4AnchoredTransform = new Matrix4d();
	
	public AnimModel() { }
	
	public AnimModel(OLDModel model, String filename) {
		strName = model.getName();
		strFileName = filename;
		this.model = model;
		model.setAnimModel(this);
		setRenderString("renderman", "", "Attribute \"visibility\" \"string transmission\" [\"shader\"]\n");
	}
	
//	public AnimModel(String name, Model model) {
//		strName = name;
//		this.model = model;
//		model.setAnimModel(this);
//	}
	
	@Override
	public Matrix4d getTransform() {
		if (anchor == null)
			return super.getTransform();
		m4AnchoredTransform.set(super.getTransform());
		Vector4f v = new Vector4f(anchor.getPosition());
		m4AnchoredTransform.transform(v);
		m4AnchoredTransform.m03 -= v.x;
		m4AnchoredTransform.m13 -= v.y;
		m4AnchoredTransform.m23 -= v.z;
		return m4AnchoredTransform;
	}
	
//	@Override
//	public void getBounds(Point3f p0, Point3f p1) {
//		super.getBounds(p0, p1);
//		if (anchor != null) {
//			Vector3f v = new Vector3f(anchor.getPosition());
//			m4ScaledTransform.transform(v);
//			p0.sub(v);
//			p1.sub(v);
//		}
//	}
	
	public Transformable getAnchor() {
		return anchor;
	}
	
	public void setAnchor(Transformable anchor) {
		this.anchor = anchor;
	}
	
	public OLDModel getModel() {
		return model;
	}
	
	public String toString() {
		return strName;
	}
	
	public void setModel(OLDModel model) {
		this.model = model;
	}
	
	public void setName(String name) {
		strName = name;
	}
	
	public void setFilename(String filename) {
		strFileName = filename;
	}
	
	public void setSubdivisionOffset(int offset) {
		iSubdivisionOffset = offset;
	}
	
	public int getSubdivisionOffset() {
		return iSubdivisionOffset;
	}
	
	public StringBuffer renderStrings(String prefix) {
		return re.xml(prefix);
	}
	
	public void xml(PrintStream out, String prefix) {
		out.append(prefix).append("<model>\n");
		out.append(prefix).append("\t<name>" + getName() + "</name>\n");
		out.append(prefix).append("\t<filename>" + strFileName + "</filename>\n");
		if (iSubdivisionOffset != 0)
			out.append("\t\t<subdivisionoffset>" + iSubdivisionOffset + "</subdivisionoffset>").append("\n");
		out.append(prefix).append(renderStrings("\t")).append("\n");
		MainFrame.getInstance().getAnimation().getCurvesetFor(this).xml(out, prefix + "\t");
		out.append(prefix).append("</model>").append("\n");
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
//		return (TreeNode) model.getMorphList().get(childIndex);
	}

	public int getChildCount() {
//		return model.getMorphList().size();
//		return 0;
		return 2;
	}

	public int getIndex(TreeNode node) {
		return model.getMorphList().indexOf(node);
//		return -1;
	}

	public boolean getAllowsChildren() {
		return true;
//		return false;
	}

	public boolean isLeaf() {
		return false;
//		return true;
	}

	public Enumeration children() {
//		return Collections.enumeration(model.getMorphList());
//		return null;
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
