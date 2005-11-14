package jpatch.boundary.action;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.vecmath.*;
import jpatch.boundary.*;

import jpatch.auxilary.*;
import jpatch.entity.*;

public final class AssignPointsToBonesAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	
	public AssignPointsToBonesAction() {
		super("autoassign controlpoints");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		Selection selection = MainFrame.getInstance().getSelection();
		ArrayList boneList = new ArrayList();
		ArrayList cpList = new ArrayList();
		for (Iterator it = selection.getObjects().iterator(); it.hasNext(); ) {
			Object object = it.next();
			if (object instanceof Bone.BoneTransformable) {
				Bone.BoneTransformable bt = (Bone.BoneTransformable) object;
				Bone bone = bt.getBone();
				Bone parent = bone.getParentBone();
				if (bt.isEnd()) {
					if (parent == null) {
						if (selection.contains(bone.getBoneStart())) {
							boneList.add(bone);
						}
					} else {
						if (selection.contains(parent.getBoneEnd())) {
							boneList.add(bone);
						}
					}
				}
			} else if (object instanceof ControlPoint) {
				cpList.add(object);
			}
		}
		
		System.out.println("Autoassigning " + cpList.size() + " controlpoints to " + boneList.size() + " bones.");
		
		for (int i = 0, n = cpList.size(); i < n; i++) {
			ControlPoint cp = (ControlPoint) cpList.get(i);
			Point3f p = cp.getReferencePosition();
			float minDist = Float.MAX_VALUE;
			float closestPosOnLine = 0;
			float closestDistToLine = 0;
			Bone closestBone = null;
			for (int j = 0, m = boneList.size(); j < m; j++) {
				Bone bone = (Bone) boneList.get(j);
//				Bone parent = (bone.getParentBone() != null && bone.getParentBone().getChildBones().size() == 1) ? bone.getParentBone() : null;
				Bone child = bone.getChildBones().size() == 1 ? (Bone) bone.getChildBones().get(0) : null;
				Point3f p0 = bone.getReferenceStart();
				Point3f p1 = bone.getReferenceEnd();
				float l = p0.distance(p1);
				float posOnLine = Utils3D.positionOnLine(p0, p1, p);
				float posOnSegment = posOnLine < 0 ? 0 : posOnLine > 1 ? 1 : posOnLine;
				Point3f pBone = new Point3f();
				pBone.interpolate(p0, p1, posOnSegment);
				float distance = pBone.distance(p) / l;
				pBone.interpolate(p0, p1, posOnLine);
				float distToLine = pBone.distance(p) / l;
//				System.out.println("b=" + bone + "par=" + parent + " child=" + child + " pos=" + cp.getPosition() + " cl=" + closestBone + " pol=" + posOnLine + " dtl=" + distToLine + " d=" + distance);
//				if (closestBone != null) {
//					if (closestBone == parent) {
//						if (Math.abs(posOnLine) < distToLine && distToLine < minDist) {
//							minDist = distToLine;
//							closestBone = bone;
//							closestPosOnLine = posOnLine;
//							closestDistToLine = distToLine;
//						} else if (distance < minDist) {
//							minDist = distance;
//							closestBone = bone;
//							closestPosOnLine = posOnLine;
//							closestDistToLine = distToLine;
//						}
//					} else if (closestBone == child) {
//						if ((1 - posOnLine) > distToLine && distToLine < minDist) {
//							minDist = distToLine;
//							closestBone = bone;
//							closestPosOnLine = posOnLine;
//							closestDistToLine = distToLine;	
//						} else if (distance < minDist) {
//							minDist = distance;
//							closestBone = bone;
//							closestPosOnLine = posOnLine;
//							closestDistToLine = distToLine;
//						}
//					}
//				} else
				if (distance < minDist) {
					minDist = distance;
					if (child != null && distToLine > 1 - posOnLine) {
						closestBone = child;
						closestPosOnLine = posOnLine - 1;
					} else {
						closestBone = bone;
						closestPosOnLine = posOnLine;
					}
					closestDistToLine = distToLine;
				}
			}
			System.out.println(cp + " -> " + closestBone + " b=" + closestPosOnLine + " d=" + closestDistToLine);
			cp.setBone(closestBone, closestPosOnLine, closestDistToLine);
		}
	}
}

