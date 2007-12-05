package com.jpatch.entity;

import com.jpatch.afw.attributes.*;
import com.jpatch.boundary.*;

import javax.vecmath.*;

public class IkConstraint {
	private final Bone upperArm;
	private final Bone lowerArm;
	private final Bone hand;
	private final XFormNode elbowTarget;
	private final XFormNode handTarget;
	
	public IkConstraint(Bone upperArm, Bone lowerArm, Bone hand, XFormNode elbowTarget, XFormNode handTarget) {
		this.upperArm = upperArm;
		this.lowerArm = lowerArm;
		this.hand = hand;
		this.elbowTarget = elbowTarget;
		this.handTarget = handTarget;
		
		AttributePostChangeListener apcl = new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				solve();
//				Main.getInstance().repaintViewports();
			}
		};
		elbowTarget.getTranslationAttribute().addAttributePostChangeListener(apcl);
		handTarget.getTranslationAttribute().addAttributePostChangeListener(apcl);
	}
	
	private void solve() {
		System.out.println("IK solver called");
		/* work in world space */
		upperArm.computeWorldMatrices();
		lowerArm.computeWorldMatrices();
		hand.computeWorldMatrices();
		elbowTarget.computeWorldMatrices();
		handTarget.computeWorldMatrices();
		
		Point3d shoulder = new Point3d();
		upperArm.local2WorldMatrix.transform(shoulder);
		Point3d elbow = new Point3d();
		elbowTarget.local2WorldMatrix.transform(elbow);
		Point3d hand = new Point3d();
		handTarget.local2WorldMatrix.transform(hand);
		
		double a = upperArm.getLengthAttribute().getDouble();	// shoulder-elbow distance
		double b = lowerArm.getLengthAttribute().getDouble(); 	// elbow-hand distamce
		double c = shoulder.distance(hand);						// shoulder-target distance;
		
		System.out.println("shoulder at " + shoulder);
		System.out.println("elbow target at " + elbow);
		System.out.println("hand target at " + hand);
		
		System.out.println("a = " + a);
		System.out.println("b = " + b);
		System.out.println("c = " + c);
		
		if (c > a + b) {
			return;		// TODO fixme
		}
		
		double cosC = (a * a + b * b - c * c) / ( 2 * a * b);
		double sinC = Math.sqrt(1 - cosC * cosC);
		double sinA = a / c * sinC;
		double sinB = b / c * sinC;
		double angleA = Math.asin(sinA);
		double angleC = Math.acos(cosC);
//		double angleC2 = Math.acos(cosC);
		double angleB = Math.asin(sinB);
//		double angleB2 = Math.PI - angleA - angleC;
		
		
		System.out.println(Math.toDegrees(angleA + angleB + angleC));
		
		Vector3d shoulderElbow = new Vector3d();
		shoulderElbow.sub(elbow, shoulder);
		Vector3d shoulderHand = new Vector3d();
		shoulderHand.sub(hand, shoulder);
		Vector3d shoulderAxis = new Vector3d();
		shoulderAxis.cross(shoulderHand, shoulderElbow);
		
		Vector3d z = new Vector3d(0, 0, 1);
		Vector3d t = new Vector3d(shoulderHand);
		System.out.println(t);
		t.normalize();
		double angle0 = Math.acos(t.dot(z));
		
		Vector3d axis = new Vector3d(shoulderAxis);
		Matrix4d m = new Matrix4d();
		m.invert(upperArm.axisRotation2WorldMatrix);
//		System.out.println("upperArm.axisRotation2WolrdMatrix = " + upperArm.axisRotation2WorldMatrix);
		m.transform(axis);
		axis.normalize();
		
		AxisAngle4d axisAngle0 = new AxisAngle4d(new Vector3d(0, 1, 0), angle0);
		AxisAngle4d axisAngleB = new AxisAngle4d(axis, angleB);
		
		System.out.println("world axis = " + shoulderAxis);
		System.out.println("local axis = " + axis);
//		System.out.println("angle = " + angleA);
		Matrix3d m0 = new Matrix3d();
		m0.set(axisAngle0);
		Matrix3d mb = new Matrix3d();
		mb.set(axisAngleB);
//		mb.mul(m0, mb);
		upperArm.rotation.setRotation(mb);
		upperArm.rotationAttr.setTuple(upperArm.rotation);
		lowerArm.rotationAttr.setTuple(0, 180 - Math.toDegrees(angleC), 0);
		
		System.out.println("angle 0 = " + Math.toDegrees(angle0));
		System.out.println("angle A = " + Math.toDegrees(angleA));
		System.out.println("angle B = " + Math.toDegrees(angleB));
		System.out.println("angle C = " + Math.toDegrees(angleC));
		
	}
}
