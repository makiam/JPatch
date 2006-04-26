package jpatch.entity;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.tree.*;

import jpatch.boundary.*;

public class Animation implements MutableTreeNode {

	private float fStart = 0;
	private float fEnd = 200;
	private float fPosition = 0;
	private float fFramerate = 24;
	
	private List<AnimModel> listModels = new ArrayList<AnimModel>();
	private List<AnimLight> listLights = new ArrayList<AnimLight>();
	private List<Camera> listCameras = new ArrayList<Camera>();
	private Map<AnimObject, MotionCurveSet> mapMotionCurves = new HashMap<AnimObject, MotionCurveSet>();
	private AnimTreeNode treenodeModels = new AnimTreeNode("Models", listModels);
	private AnimTreeNode treenodeLights = new AnimTreeNode("Lights", listLights);
	private AnimTreeNode treenodeCameras = new AnimTreeNode("Cameras", listCameras);
	private Map<Model, Pose> mapClipboardPose = new HashMap<Model, Pose>();
	
	private RenderExtension re = new RenderExtension(new String[] {
			"povray", "",
			"renderman", ""
	});
	
	private boolean bInserted;
	private String strName = "New Animation";
	
//	public Animation() {
//		addCamera(new Camera("Camera 1"), null);
//	}
	
	public float getEnd() {
		return fEnd;
	}

	public void setEnd(float end) {
		fEnd = end;
	}

	public float getFramerate() {
		return fFramerate;
	}

	public void setFramerate(float framerate) {
		fFramerate = framerate;
	}

	public float getPosition() {
		return fPosition;
	}

	public Pose getClipboardPose(Model model) {
		Pose pose = mapClipboardPose.get(model);
		if (pose == null) {
			pose = new Pose(model);
			mapClipboardPose.put(model, pose);
		}
		return pose;
	}
	
	public void setPosition(float position) {
		if (position < fStart)
			position = fStart;
		else if (position > fEnd)
			position = fEnd;
		fPosition = position;
//		System.out.println("position = " + position);
		for (AnimModel animModel:listModels) {
			mapMotionCurves.get(animModel).setPosition(position);
//			animModel.getModel().applyMorphs();
//			animModel.getModel().setPose();
		}
		for (AnimLight animLight:listLights) {
			mapMotionCurves.get(animLight).setPosition(position);
		}
		for (Camera camera:listCameras) {
			mapMotionCurves.get(camera).setPosition(position);
		}
		MainFrame.getInstance().getTimelineEditor().setCurrentFrame((int) position);
		MainFrame.getInstance().getVcrControls().setPosition((int) position);
	}

	public void rethink() {
		setPosition(fPosition);
	}
	
	public float getStart() {
		return fStart;
	}

	public void setStart(float start) {
		fStart = start;
	}	
	
	public void addModel(AnimModel animModel, MotionCurveSet mcs) {
		MainFrame.getInstance().getTreeModel().insertNodeInto(animModel, treenodeModels, listModels.size());
		setCurvesetFor(animModel, mcs);
	}
	
	public void addLight(AnimLight animLight, MotionCurveSet mcs) {
		MainFrame.getInstance().getTreeModel().insertNodeInto(animLight, treenodeLights, listLights.size());
		setCurvesetFor(animLight, mcs);
	}
	
	public void addCamera(final Camera camera, MotionCurveSet mcs) {
//		if (MainFrame.getInstance().getAnimation() != null)
			MainFrame.getInstance().getTreeModel().insertNodeInto(camera, treenodeCameras, listCameras.size());
		JMenuItem menuItem = new JRadioButtonMenuItem(camera.getName().toLowerCase());
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.getInstance().getJPatchScreen().getActiveViewport().getViewDefinition().setCamera(camera);
			}
		});
		MainFrame.getInstance().getViewMenu().add(menuItem);
		setCurvesetFor(camera, mcs);
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
	
	public Iterable<AnimObject> getObjects() {
		return new Iterable<AnimObject>() {
			public Iterator<AnimObject> iterator() {
				final Iterator<AnimModel> itModels = listModels.iterator();
				final Iterator<AnimLight> itLights = listLights.iterator();
				final Iterator<Camera> itCameras = listCameras.iterator();
				return new Iterator<AnimObject>() {
					public boolean hasNext() {
						return itCameras.hasNext();
					}
					public AnimObject next() {
						if (itModels.hasNext())
							return itModels.next();
						if (itLights.hasNext())
							return itLights.next();
						return itCameras.next();
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
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
	
	public MotionCurveSet getCurvesetFor(AnimObject animObject) {
		return mapMotionCurves.get(animObject);
	}
	
	private void setCurvesetFor(AnimObject animObject, MotionCurveSet mcs) {
		if (mcs == null)
			mcs = MotionCurveSet.createMotionCurveSetFor(animObject);
		mapMotionCurves.put(animObject, mcs);
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
	
	public void dump() {
		System.out.println("Current position:" + getPosition());
//		for (AnimObject animObject:getObjects()) {
//			System.out.println("animObject: " + animObject.getName());
//			MotionCurveSet mcs = getCurvesetFor(animObject);
//			StringBuffer sb = new StringBuffer();
//			mcs.xml(sb, "    ");
//			System.out.println(sb);
//		}
		System.out.println(xml("\t"));
	}
	
	public StringBuffer xml(String prefix) {
		StringBuffer sb = new StringBuffer();
		sb.append(prefix).append("<choreography>\n");
		sb.append(prefix).append("\t<name>" + strName + "</name>\n");
		sb.append(prefix).append("\t<start>" + fStart + "</start>\n");
		sb.append(prefix).append("\t<end>" + fEnd + "</end>\n");
		sb.append(prefix).append("\t<framerate>" + fFramerate + "</framerate>\n");
//		sb.append(prefix).append("\t<prefix>" + strPrefix + "</prefix>").append("\n");
		sb.append(prefix).append(renderStrings("\t")).append("\n");
		for (AnimObject animObject:getObjects())
			animObject.xml(sb, prefix + "\t");
		sb.append(prefix).append("</choreography>\n");
		return sb;
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
			return Animation.this;
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
