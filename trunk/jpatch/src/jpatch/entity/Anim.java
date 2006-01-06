package jpatch.entity;

import java.util.*;
import javax.swing.tree.*;

import jpatch.boundary.*;

public class Anim implements MutableTreeNode {

	private ArrayList<AnimModel> listModels = new ArrayList<AnimModel>();
	private ArrayList<AnimLight> listLights = new ArrayList<AnimLight>();
	private ArrayList<Camera> listCameras = new ArrayList<Camera>();
	private HashMap<AnimObject, MotionCurveSet> mapMotionCurves = new HashMap<AnimObject, MotionCurveSet>();
	private AnimTreeNode treenodeModels = new AnimTreeNode("Models", listModels);
	private AnimTreeNode treenodeLights = new AnimTreeNode("Lights", listLights);
	private AnimTreeNode treenodeCameras = new AnimTreeNode("Cameras", listCameras);

	private boolean bInserted;
	private String strName = "New Animation";
	
	public Anim() {
		addCamera(new Camera("Camera 1"), null);
	}
	
	public void addModel(AnimModel animModel, MotionCurveSet mcs) {
		MainFrame.getInstance().getTreeModel().insertNodeInto(animModel, treenodeModels, listModels.size());
		setMotionCurveFor(animModel, mcs);
	}
	
	public void addLight(AnimLight animLight, MotionCurveSet mcs) {
		MainFrame.getInstance().getTreeModel().insertNodeInto(animLight, treenodeLights, listLights.size());
		setMotionCurveFor(animLight, mcs);
	}
	
	public void addCamera(Camera camera, MotionCurveSet mcs) {
		MainFrame.getInstance().getTreeModel().insertNodeInto(camera, treenodeCameras, listCameras.size());
		setMotionCurveFor(camera, mcs);
	}
	
	public void removeModel(AnimModel animModel) {
		MainFrame.getInstance().getTreeModel().removeNodeFromParent(animModel);
	}
	
	public void removeLight(AnimLight animLight) {
		MainFrame.getInstance().getTreeModel().removeNodeFromParent(animLight);
	}
	
	public void removeCamera(Camera camera) {
		MainFrame.getInstance().getTreeModel().removeNodeFromParent(camera);
	}
	
	public List<AnimModel> getModels() {
		return listModels;
	}
	
	public List<AnimLight> getLights() {
		return listLights;
	}
	
	public List<Camera> getCameras() {
		return listCameras;
	}
	
	public AnimTreeNode getTreenodeModels() {
		return treenodeModels;
	}
	
	public AnimTreeNode getTreenodeLights() {
		return treenodeLights;
	}
	
	public AnimTreeNode getTreenodeCameras() {
		return treenodeCameras;
	}
	
	public String toString() {
		return strName;
	}
	
	private void setMotionCurveFor(AnimObject animObject, MotionCurveSet mcs) {
		if (mcs == null)
			mcs = MotionCurveSet.createMotionCurveSetFor(animObject);
		mapMotionCurves.put(animObject, mcs);
	}
	
/* MutableTreeNode interface implementation */
	
	public void insert(MutableTreeNode child, int index) {
		throw new UnsupportedOperationException("Can't insert nodes into model");
	}

	public void remove(int index) {
		throw new UnsupportedOperationException("Can't remove nodes from model");
	}

	public void remove(MutableTreeNode node) {
		throw new UnsupportedOperationException("Can't remove nodes from model");
	}

	public void setUserObject(Object object) {
		throw new UnsupportedOperationException();
	}

	public void removeFromParent() {
		throw new UnsupportedOperationException();
	}

	public void setParent(MutableTreeNode newParent) {
		bInserted = true;
	}

	public TreeNode getChildAt(int childIndex) {
		switch(childIndex) {
		case 0:
			return treenodeModels;
		case 1:
			return treenodeLights;
		case 2:
			return treenodeCameras;
		}
		throw new ArrayIndexOutOfBoundsException();
	}

	public int getChildCount() {
		return 3;
	}

	public TreeNode getParent() {
		return bInserted ? MainFrame.getInstance().getRootTreenode() : null;
	}

	public int getIndex(TreeNode node) {
		return 0;
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
				return i < 4;
			}

			public Object nextElement() {
				return getChildAt(i++);
			}
		};
	}
	
	private class AnimTreeNode implements MutableTreeNode {
		private List list;
		private String strName;
		
		public AnimTreeNode(String name, List list) {
			strName = name;
			this.list = list;
		}
		
		public String toString() {
			return strName;
		}
		
		public void setUserObject(Object object) {
			throw new UnsupportedOperationException();
		}

		public void removeFromParent() {
			throw new UnsupportedOperationException();
		}

		public void setParent(MutableTreeNode newParent) {
		}

		public TreeNode getParent() {
			return Anim.this;
		}

		public boolean getAllowsChildren() {
			return true;
		}

		public void insert(MutableTreeNode child, int index) {
			list.add(index, child);
			child.setParent(this);
		}

		public void remove(int index) {
			list.remove(index);
		}

		public void remove(MutableTreeNode node) {
			list.remove(node);
		}

		public TreeNode getChildAt(int childIndex) {
			return (TreeNode) list.get(childIndex);
		}

		public int getChildCount() {
			return list.size();
		}

		public int getIndex(TreeNode node) {
			return list.indexOf(node);
		}

		public boolean isLeaf() {
			return list.size() <= 0;
		}

		public Enumeration children() {
			return Collections.enumeration(list);
		}
	}	
}
