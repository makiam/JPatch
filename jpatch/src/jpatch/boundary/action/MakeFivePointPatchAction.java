package jpatch.boundary.action;

import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import jpatch.control.edit.*;
import jpatch.boundary.*;

import jpatch.entity.*;

public final class MakeFivePointPatchAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public MakeFivePointPatchAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/fivepointpatch.png")));
		putValue(Action.SHORT_DESCRIPTION,"make five point patch");
		//putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("clone"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		Model model = MainFrame.getInstance().getModel();
		if (ps != null) {
			for (Curve curve = model.getFirstCurve(); curve != null; curve = curve.getNext()) {
				for (ControlPoint cp = curve.getStart(); cp != null; cp = cp.getNextCheckNextLoop()) {
					if (!ps.contains(cp) && cp.getParentHook() != null && ps.contains(cp.getParentHook().getHead())) {
						ps.addControlPoint(cp);
				      	}
				}
			}
                	
			test:
			if (ps != null) {
				ControlPoint[] acp = ps.getControlPointArray();
				HashMap mapHeadList = new HashMap();
				HashSet setHeads = new HashSet();
				for (int i = 0; i < acp.length; i++) {
					ControlPoint head = acp[i].trueHead();
					setHeads.add(head);
					if (acp[i].getParentHook() != null) {
						ArrayList neighbors = (ArrayList) mapHeadList.get(head);
						if (neighbors == null) {
							neighbors = new ArrayList();
							mapHeadList.put(head,neighbors);
						}
						if (acp[i].getNext() != null) {
							neighbors.add(acp[i]);
							neighbors.add(acp[i].getNext());
						}
						if (acp[i].getPrev() != null) {
							neighbors.add(acp[i]);
							neighbors.add(acp[i].getPrev());
						}
					}
				}
				
				//System.out.println(setHeads);
				//
				//for (Curve curve = model.getFirstCurve(); curve != null; curve = curve.getNext()) {
				//	for (ControlPoint cp = curve.getStart(); cp != null; cp = cp.getNextCheckNextLoop()) {
				//		if (cp.getParentHook() != null && setHeads.contains(cp.getParentHook().getHead())) {
				//			ControlPoint head = cp.getParentHook().getHead();
				//			//setHeads.add(head);
				//			
				//			ArrayList neighbors = (ArrayList) mapHeadList.get(head);
				//			if (neighbors == null) {
				//				neighbors = new ArrayList();
				//				mapHeadList.put(head,neighbors);
				//			}
				//			if (cp.getNext() != null) {
				//				neighbors.add(cp);
				//				neighbors.add(cp.getNext());
				//			}
				//			if (cp.getPrev() != null) {
				//				neighbors.add(cp);
				//				neighbors.add(cp.getPrev());
				//			}
				//			
				//	      	}
				//	}
				//}
							
							
				for (int i = 0; i < acp.length; i++) {
					ControlPoint head = acp[i].trueHead();
					if (acp[i].getParentHook() == null) {
						ArrayList neighbors = (ArrayList) mapHeadList.get(head);
						if (neighbors == null) {
							neighbors = new ArrayList();
							mapHeadList.put(head,neighbors);
						}
						ControlPoint[] stack = acp[i].getStack();
						for (int j = 0; j < stack.length; j++) {
							if (stack[j].getNext() != null) {
								neighbors.add(stack[j]);
								neighbors.add(stack[j].getNext());
							}
							if (stack[j].getPrev() != null) {
								neighbors.add(stack[j]);
								neighbors.add(stack[j].getPrev());
							}
						}
					}
				}
				//for (Iterator it = setHeads.iterator(); it.hasNext(); ) {
				//	ControlPoint head = (ControlPoint) it.next();
				//	//System.out.println("HEAD " + head.number());
				//	ArrayList neighbors = (ArrayList) mapHeadList.get(head);
				//	for (Iterator it2 = neighbors.iterator(); it2.hasNext(); ) {
				//		System.out.println("\t" + ((ControlPoint) it2.next()).number());
				//	}
				//}
				if (setHeads.size() == 5) {
					//System.out.println();
					ArrayList fivePointPatch = new ArrayList();
					ArrayList toGoList = new ArrayList(setHeads);
					ControlPoint neighborA;
					ControlPoint neighborB;
					ControlPoint currentHead = (ControlPoint) toGoList.get(0);
					ControlPoint first = currentHead;
					toGoList.remove(currentHead);
					for (int i = 0; i < 5; i++) {
						//System.out.println(currentHead.number());
						//System.out.println(toGoList);
						ArrayList neighborList = (ArrayList) mapHeadList.get(currentHead);
						//System.out.println("neighborlist:" + neighborList);
						Iterator neighborIterator = neighborList.iterator();
						boolean found = false;
						loop:
						while (neighborIterator.hasNext()) {
							neighborA = (ControlPoint) neighborIterator.next();
							neighborB = (ControlPoint) neighborIterator.next();
							//System.out.println(neighborA.number() + " " + neighborA.trueHead().number() + " " + neighborB.number() + " " + neighborB.trueHead().number());
							if (toGoList.contains(neighborB.trueHead()) || (toGoList.size() == 0 && neighborB.trueHead() == first)) {
								fivePointPatch.add(neighborA);
								fivePointPatch.add(neighborB);
								currentHead = neighborB.trueHead();
								toGoList.remove(currentHead);
								found = true;
								break loop;
							}
						}
						if (!found) {
							//System.out.println("*break*");
							break test;
						}
					}
					//for (Iterator it = fivePointPatch.iterator(); it.hasNext(); ) {
					//	System.out.println(((ControlPoint) it.next()).number());
					//}
					if (fivePointPatch.size() == 10) {
						acp = new ControlPoint[10];
						for (int i = 0; i < 10; i++) {
							acp[i] = (ControlPoint) fivePointPatch.get(i);
						}
						if (
							acp[1].trueCp() != acp[2].trueCp() &&
							acp[3].trueCp() != acp[4].trueCp() &&
							acp[5].trueCp() != acp[6].trueCp() &&
							acp[7].trueCp() != acp[8].trueCp() &&
							acp[9].trueCp() != acp[0].trueCp()
						) {
							Patch patch = new Patch(acp);
							ControlPoint[] acp5 = new ControlPoint[] {
									acp[0].trueHead(),
									acp[2].trueHead(),
									acp[4].trueHead(),
									acp[6].trueHead(),
									acp[8].trueHead()
									//acp[0].getHead(),
									//acp[2].getHead(),
									//acp[4].getHead(),
									//acp[6].getHead(),
									//acp[8].getHead()
								};
							model.getCandidateFivePointPatchList().add(acp5);
							MainFrame.getInstance().getUndoManager().addEdit(new AtomicAddPatch(patch));
							MainFrame.getInstance().getJPatchScreen().update_all();
						}
					}
				}
			}
		}
	}
}
