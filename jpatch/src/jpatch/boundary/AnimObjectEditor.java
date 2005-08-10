package jpatch.boundary;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import javax.vecmath.*;
import buoy.widget.*;
import buoy.event.*;
import jpatch.entity.*;

public class AnimObjectEditor extends BDialog{
	private AnimObject animObject;
	
	private BTextField textName;
	private BTextField textPosX;
	private BTextField textPosY;
	private BTextField textPosZ;
	private BTextField textRoll;
	private BTextField textPitch;
	private BTextField textYaw;
	//private BTextField textRotW;
	private BTextField textScale;
	private BTextField textFocalLength;
	private BTextField textIntensity;
	private BTextField textSize;
	private BSlider sliderSubdiv;
	private BCheckBox cbActive;
	private BuoyUtils.ColorSelector colorSelector;
	private FormContainer form;
	private BTextArea textRib;
	private BTextArea textPov;
	
	public AnimObjectEditor(AnimObject object, WindowWidget parent) {
		super(parent, "Edit " + object.getName() + " at frame " + (Animator.getInstance().getPosition() + 1), true);
		animObject = object;
		BTabbedPane tabbedPane = null;
		textName = new BTextField(animObject.getName(), 20);
		textPosX = new BTextField("" + animObject.getPosition().x, 20);
		textPosY = new BTextField("" + animObject.getPosition().y, 20);
		textPosZ = new BTextField("" + animObject.getPosition().z, 20);
		textRoll = new BTextField("" + animObject.getRoll() * 180 / Math.PI, 20);
		textPitch = new BTextField("" + animObject.getPitch() * 180 / Math.PI, 20);
		textYaw = new BTextField("" + animObject.getYaw() * 180 / Math.PI, 20);
		//textRotW = new BTextField("" + animObject.getOrientation().w, 20);
		if (animObject instanceof Camera) {
			form = new FormContainer(2, 9);
			Camera camera = (Camera) animObject;
			textFocalLength = new BTextField("" + camera.getFocalLength(), 20);
		} else if (animObject instanceof AnimModel) {
			form = new FormContainer(2, 10);
			AnimModel animModel = (AnimModel) animObject;
			sliderSubdiv = new BSlider(animModel.getSubdivisionOffset(), -2, 2, BSlider.HORIZONTAL);
			sliderSubdiv.setSnapToTicks(true);
			sliderSubdiv.setMinorTickSpacing(1);
			sliderSubdiv.setMajorTickSpacing(1);
			sliderSubdiv.setShowTicks(true);
			sliderSubdiv.setShowLabels(true);
			Dictionary dict;
			dict = new Hashtable();
			dict.put(new Integer(-2), new JLabel("-2"));
			dict.put(new Integer(-1), new JLabel("-1"));
			dict.put(new Integer(0), new JLabel("0"));
			dict.put(new Integer(1), new JLabel("+1"));
			dict.put(new Integer(2), new JLabel("+2"));
			((JSlider) sliderSubdiv.getComponent()).setLabelTable(dict);
		
			textScale = new BTextField("" + animModel.getScale(), 20);
			textRib = new BTextArea(animModel.getRenderString("renderman", ""), 10, 80);
			textPov = new BTextArea(animModel.getRenderString("povray", ""), 10, 80);
			textRib.setFont(new Font("Monospaced",Font.PLAIN,12));
			textPov.setFont(new Font("Monospaced",Font.PLAIN,12));
			tabbedPane = new BTabbedPane();
			tabbedPane.add(new BScrollPane(textPov), "POV-Ray");
			tabbedPane.add(new BScrollPane(textRib), "RenderMan");
		} else if (animObject instanceof AnimLight) {
			form = new FormContainer(2, 12);
			AnimLight light = (AnimLight) animObject;
			cbActive = new BCheckBox("", light.isActive());
			textIntensity = new BTextField("" + light.getIntensity(), 20);
			textSize = new BTextField("" + light.getSize(), 20);
			colorSelector = new BuoyUtils.ColorSelector(light.getColor().get(), this);
			textRib = new BTextArea(light.getRenderString("renderman", ""), 10, 80);
			textPov = new BTextArea(light.getRenderString("povray", ""), 10, 80);
			textRib.setFont(new Font("Monospaced",Font.PLAIN,12));
			textPov.setFont(new Font("Monospaced",Font.PLAIN,12));
			tabbedPane = new BTabbedPane();
			tabbedPane.add(new BScrollPane(textPov), "POV-Ray");
			tabbedPane.add(new BScrollPane(textRib), "RenderMan");
		}
		LayoutInfo east = new LayoutInfo(LayoutInfo.EAST, LayoutInfo.NONE);
		LayoutInfo west = new LayoutInfo(LayoutInfo.NORTHWEST, LayoutInfo.NONE);
		int i = 0;
		form.add(new BLabel("Name:    "), 0, i, east);
		form.add(textName, 1, i++, west);
		if (animObject instanceof AnimLight) {
			form.add(new BLabel("Number:    "), 0, i, east);
			BTextField textNumber = new BTextField("" + ((AnimLight) animObject).getNumber(), 20);
			textNumber.setEditable(false);
			form.add(textNumber, 1, i++, west);
		}
		form.add(new BLabel("Position X:    "), 0, i, east);
		form.add(textPosX, 1, i++, west);
		form.add(new BLabel("Position Y:    "), 0, i, east);
		form.add(textPosY, 1, i++, west);
		form.add(new BLabel("Position Z:    "), 0, i, east);
		form.add(textPosZ, 1, i++, west);
		//if (!(animObject instanceof AnimLight)) {
			form.add(new BLabel("Roll:    "), 0, i, east);
			form.add(textRoll, 1, i++, west);
			form.add(new BLabel("Pitch:    "), 0, i, east);
			form.add(textPitch, 1, i++, west);
			form.add(new BLabel("Yaw:    "), 0, i, east);
			form.add(textYaw, 1, i++, west);
			//form.add(new BLabel("Rotation W:    "), 0, i, east);
			//form.add(textRotW, 1, i++, west);
		//}
		if (animObject instanceof Camera) {
			form.add(new BLabel("Focal Length:    "), 0, i, east);
			form.add(textFocalLength, 1, i++, west);
		} else if (animObject instanceof AnimModel) {
			form.add(new BLabel("Scale:    "), 0, i, east);
			form.add(textScale, 1, i++, west);
			form.add(new BLabel("Subdivision offset:    "), 0, i, east);
			form.add(sliderSubdiv, 1, i++, west);
		} else if (animObject instanceof AnimLight) {
			form.add(new BLabel("Size:    "), 0, i, east);
			form.add(textSize, 1, i++, west);
			form.add(new BLabel("Intensity:    "), 0, i, east);
			form.add(textIntensity, 1, i++, west);
			form.add(new BLabel("Color:    "), 0, i, east);
			form.add(colorSelector, 1, i++, west);
			form.add(new BLabel("Turned on:    "), 0, i, east);
			form.add(cbActive, 1, i++, west);
		}
		
		RowContainer buttons = new RowContainer();
		BButton buttonOK = new BButton("OK");
		BButton buttonCancel = new BButton("Cancel");
		buttons.add(buttonOK);
		buttons.add(buttonCancel);
		
		ColumnContainer content = new ColumnContainer();
		content.add(form);
		if (tabbedPane != null) content.add(tabbedPane);
		content.add(buttons);
		
		setContent(content);
		pack();
		((Window) getComponent()).setLocationRelativeTo(parent.getComponent());
		addEventLink(WindowClosingEvent.class, this, "dispose");
		buttonCancel.addEventLink(CommandEvent.class, this, "dispose");
		buttonOK.addEventLink(CommandEvent.class, this, "set");
		setResizable(false);
		setVisible(true);
	}
	
	private void set() {
		double posX = 0;
		double posY = 0;
		double posZ = 0;
		double roll = 0;
		double pitch = 0;
		double yaw = 0;
		float scale = 0;
		float size = 0;
		float intensity = 0;
		float focalLength = 0;
		
		try {
			posX = Double.parseDouble(textPosX.getText());
			posY = Double.parseDouble(textPosY.getText());
			posZ = Double.parseDouble(textPosZ.getText());
			roll = Double.parseDouble(textRoll.getText()) / 180 * Math.PI;
			pitch = Double.parseDouble(textPitch.getText()) / 180 * Math.PI;
			yaw = Double.parseDouble(textYaw.getText()) / 180 * Math.PI;
			if (animObject instanceof Camera) {
				focalLength = Float.parseFloat(textFocalLength.getText());
				Camera camera = (Camera) animObject;
				camera.setName(textName.getText());
				camera.setPosition(new Point3d(posX, posY, posZ));
				camera.setOrientation(roll, pitch, yaw);
				camera.setFocalLength(focalLength);
				
			} else if (animObject instanceof AnimModel) {
				AnimModel animModel = (AnimModel) animObject;
				scale = Float.parseFloat(textScale.getText());
				animModel.setName(textName.getText());
				animModel.setPosition(new Point3d(posX, posY, posZ));
				animModel.setOrientation(roll, pitch, yaw);
				animModel.setScale(scale);
				animModel.setRenderString("povray", "", textPov.getText());
				animModel.setRenderString("renderman", "", textRib.getText());
				animModel.setSubdivisionOffset(sliderSubdiv.getValue());
			} else if (animObject instanceof AnimLight) {
				AnimLight animLight = (AnimLight) animObject;
				size = Float.parseFloat(textSize.getText());
				intensity = Float.parseFloat(textIntensity.getText());
				animLight.setName(textName.getText());
				animLight.setPosition(new Point3d(posX, posY, posZ));
				animLight.setOrientation(roll, pitch, yaw);
				animLight.setSize(size);
				animLight.setIntensity(intensity);
				animLight.setActive(cbActive.getState());
				if (!animLight.getColor().get().equals(colorSelector.getColor())) animLight.setColor(new Color3f(colorSelector.getColor()));
				animLight.setRenderString("povray", "", textPov.getText());
				animLight.setRenderString("renderman", "", textRib.getText());
			}
			Animator.getInstance().updateCurvesFor(animObject);
			dispose();
			Animator.getInstance().rerenderViewports();
		} catch (Exception exception) {
			;
		}
	}
	//private void setPosition() {
	//	try {
	//		double X = Double.parseDouble(textX.getText());
	//		double Y = Double.parseDouble(textY.getText());
	//		double Z = Double.parseDouble(textZ.getText());
	//		animObject.setPosition(new Point3d(X, Y, Z));
	//		dispose();
	//		Animator.getInstance().setObjectPosition(animObject);
	//		Animator.getInstance().rerenderViewports();
	//	} catch (Exception exception) {
	//		;
	//	}
	//}
	//
	//private void setRotation() {
	//	try {
	//		double X = Double.parseDouble(textX.getText());
	//		double Y = Double.parseDouble(textY.getText());
	//		double Z = Double.parseDouble(textZ.getText());
	//		Matrix3d rot = new Matrix3d();
	//		Matrix3d rotX = new Matrix3d();
	//		Matrix3d rotY = new Matrix3d();
	//		Matrix3d rotZ = new Matrix3d();
	//		rot.setIdentity();
	//		rotX.rotX(X / 180 * Math.PI);
	//		rotY.rotY(Y / 180 * Math.PI);
	//		rotZ.rotZ(Z / 180 * Math.PI);
	//		rot.mul(rotY);
	//		rot.mul(rotX);
	//		rot.mul(rotZ);
	//		animObject.getTransform().setRotationScale(rot);
	//		dispose();
	//		Animator.getInstance().setObjectOrientation(animObject);
	//		Animator.getInstance().rerenderViewports();
	//	} catch (Exception exception) {
	//		;
	//	}
	//}
}
