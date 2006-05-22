package jpatch.boundary.sidebar;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import javax.vecmath.*;

import jpatch.boundary.*;
import jpatch.boundary.action.*;
import jpatch.control.edit.*;
import jpatch.entity.*;

@SuppressWarnings("serial")
public class AnimObjectPanel extends SidePanel implements ChangeListener {

	private static final Font FONT = new Font("monospaced", Font.PLAIN, 12);
	private AnimObject animObject;
	private JPatchInput inputName;
	private JPatchInput inputPosX;
	private JPatchInput inputPosY;
	private JPatchInput inputPosZ;
	private JPatchInput inputRoll;
	private JPatchInput inputPitch;
	private JPatchInput inputYaw;
	private JPatchInput inputScale;
	private JPatchInput inputSize;
	private JPatchInput inputFocalLength;
	private JPatchInput inputIntensity;
	
	
	public AnimObjectPanel(AnimObject animObject) {
		this.animObject = animObject;
		add(new JButton(new DeleteAnimObjectAction(animObject)));
	
		if (animObject instanceof AnimLight) {
			JButton buttonColor = new JButton("Set color...");
			add(buttonColor);
			buttonColor.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					AnimLight light = (AnimLight) AnimObjectPanel.this.animObject;
					Color color = JColorChooser.showDialog(MainFrame.getInstance(), light.getName() + " color", light.getColor().get());
					if (color != null) {
						MotionCurveSet.Light mcs = (MotionCurveSet.Light) MainFrame.getInstance().getAnimation().getCurvesetFor(light);
						AtomicModifyMotionCurve.Color3f edit = new AtomicModifyMotionCurve.Color3f(mcs.color, MainFrame.getInstance().getAnimation().getPosition(), new Color3f(color));
						MainFrame.getInstance().getUndoManager().addEdit(edit);
						light.setColor(new Color3f(color));
						MainFrame.getInstance().getJPatchScreen().update_all();
						MainFrame.getInstance().getTimelineEditor().repaint();
					}
				}
			});
		}
		
		JButton buttonPovRay = new JButton("Pov-Ray...");
		add(buttonPovRay);
		buttonPovRay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JTextArea textArea = new JTextArea(AnimObjectPanel.this.animObject.getRenderString("povray", ""), 15, 60);
				textArea.setFont(FONT);
				textArea.setTabSize(4);
				if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), new JScrollPane(textArea), AnimObjectPanel.this.animObject.getName() + " POV-Ray output", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
					AnimObjectPanel.this.animObject.setRenderString("povray", "", textArea.getText());
			}
		});
		JButton buttonRenderman = new JButton("RenderMAN...");
		add(buttonRenderman);
		buttonRenderman.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JTextArea textArea = new JTextArea(AnimObjectPanel.this.animObject.getRenderString("renderman", ""), 15, 60);
				textArea.setFont(FONT);
				textArea.setTabSize(4);
				if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), new JScrollPane(textArea), AnimObjectPanel.this.animObject.getName() + " RenderMAN output", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
					AnimObjectPanel.this.animObject.setRenderString("renderman", "", textArea.getText());
			}
		});
		
		JPatchInput.setDimensions(100,100,20);
		inputName = new JPatchInput("Name:",animObject.getName());
		inputPosX = new JPatchInput("Position X:",animObject.getPositionDouble().x);
		inputPosY = new JPatchInput("Position Y:",animObject.getPositionDouble().y);
		inputPosZ = new JPatchInput("Position Z:",animObject.getPositionDouble().z);
		inputRoll = new JPatchInput("Roll:",Math.toDegrees(animObject.getRoll()));
		inputPitch = new JPatchInput("Pitch:",Math.toDegrees(animObject.getPitch()));
		inputYaw = new JPatchInput("Yaw:",Math.toDegrees(animObject.getYaw()));
		
		inputName.addChangeListener(this);
		inputPosX.addChangeListener(this);
		inputPosY.addChangeListener(this);
		inputPosZ.addChangeListener(this);
		inputRoll.addChangeListener(this);
		inputPitch.addChangeListener(this);
		inputYaw.addChangeListener(this);
		
//		Box panelPosition = Box.createVerticalBox();
//		panelPosition.setBorder(new TitledBorder("Position"));
//		panelPosition.add(inputPosX);
//		panelPosition.add(inputPosY);
//		panelPosition.add(inputPosZ);
//		Box panelOrientation = Box.createVerticalBox();
//		panelOrientation.setBorder(new TitledBorder("Orientation"));
//		panelOrientation.add(inputYaw);
//		panelOrientation.add(inputPitch);
//		panelOrientation.add(inputRoll);
		JPanel detailPanel = MainFrame.getInstance().getSideBar().getDetailPanel();
		detailPanel.removeAll();
		detailPanel.add(inputName);
//		detailPanel.add(panelPosition);
//		detailPanel.add(panelOrientation);
		detailPanel.add(new JSeparator(JSeparator.HORIZONTAL));
		detailPanel.add(inputPosX);
		detailPanel.add(inputPosY);
		detailPanel.add(inputPosZ);
		detailPanel.add(new JSeparator(JSeparator.HORIZONTAL));
		detailPanel.add(inputYaw);
		detailPanel.add(inputPitch);
		detailPanel.add(inputRoll);
		detailPanel.add(new JSeparator(JSeparator.HORIZONTAL));
		if (animObject instanceof Camera) {
			inputFocalLength = new JPatchInput("Focal length:", ((Camera) animObject).getFocalLength());
			inputFocalLength.addChangeListener(this);
			detailPanel.add(inputFocalLength);
		} else if (animObject instanceof AnimModel) {
			inputScale = new JPatchInput("Scale:", ((AnimModel) animObject).getScale());
			inputScale.addChangeListener(this);
			detailPanel.add(inputScale);
		} else if (animObject instanceof AnimLight) {
			inputSize = new JPatchInput("Size:", ((AnimLight) animObject).getSize());
			inputSize.addChangeListener(this);
			detailPanel.add(inputSize);
			inputIntensity = new JPatchInput("Intensity:", ((AnimLight) animObject).getIntensity());
			inputIntensity.addChangeListener(this);
			detailPanel.add(inputIntensity);
		}
	}
	
	public void updateFields() {
		inputName.setValue(animObject.getName());
		inputPosX.setValue(animObject.getPositionDouble().x);
		inputPosY.setValue(animObject.getPositionDouble().y);
		inputPosZ.setValue(animObject.getPositionDouble().z);
		inputRoll.setValue(Math.toDegrees(animObject.getRoll()));
		inputPitch.setValue(Math.toDegrees(animObject.getPitch()));
		inputYaw.setValue(Math.toDegrees(animObject.getYaw()));
		if (animObject instanceof Camera) {
			inputFocalLength.setValue(((Camera) animObject).getFocalLength());
		} else if (animObject instanceof AnimModel) {
			inputScale.setValue(((AnimModel) animObject).getScale());
		} else if (animObject instanceof AnimLight) {
			inputSize.setValue(((AnimLight) animObject).getSize());
			inputIntensity.setValue(((AnimLight) animObject).getIntensity());
		}
	}
	
	public void stateChanged(ChangeEvent e) {
		Point3d position = animObject.getPositionDouble();
		double roll = animObject.getRoll();
		double pitch = animObject.getPitch();
		double yaw = animObject.getYaw();
		
		if (e.getSource() == inputName) {
			animObject.setName(inputName.getStringValue());
			((DefaultTreeModel) MainFrame.getInstance().getTree().getModel()).nodeChanged(animObject);
		} else if (e.getSource() == inputPosX) {
			position.x = inputPosX.getDoubleValue();
			setPosition(position);
		} else if (e.getSource() == inputPosY) {
			position.y = inputPosY.getDoubleValue();
			setPosition(position);
		} else if (e.getSource() == inputPosZ) {
			position.z = inputPosZ.getDoubleValue();
			setPosition(position);
		} else if (e.getSource() == inputYaw) {
			yaw = Math.toRadians(inputYaw.getDoubleValue());
			setOrientation(yaw, pitch, roll);
		} else if (e.getSource() == inputPitch) {
			pitch = Math.toRadians(inputPitch.getDoubleValue());
			setOrientation(yaw, pitch, roll);
		} else if (e.getSource() == inputRoll) {
			roll = Math.toRadians(inputRoll.getDoubleValue());
			setOrientation(yaw, pitch, roll);
		} else if (e.getSource() == inputScale) {
			float newScale = inputScale.getFloatValue();
			MotionCurveSet.Model mcs = (MotionCurveSet.Model) MainFrame.getInstance().getAnimation().getCurvesetFor(animObject);
			AtomicModifyMotionCurve.Float edit = new AtomicModifyMotionCurve.Float(mcs.scale, MainFrame.getInstance().getAnimation().getPosition(), newScale);
			JPatchActionEdit actionEdit = new JPatchActionEdit("change scale");
			actionEdit.addEdit(edit);
			MainFrame.getInstance().getUndoManager().addEdit(actionEdit);
			((AnimModel) animObject).setScale(newScale);
			MainFrame.getInstance().getJPatchScreen().update_all();
			MainFrame.getInstance().getTimelineEditor().repaint();
		} else if (e.getSource() == inputFocalLength) {
			float newFocalLength = inputFocalLength.getFloatValue();
			MotionCurveSet.Camera mcs = (MotionCurveSet.Camera) MainFrame.getInstance().getAnimation().getCurvesetFor(animObject);
			AtomicModifyMotionCurve.Float edit = new AtomicModifyMotionCurve.Float(mcs.focalLength, MainFrame.getInstance().getAnimation().getPosition(), newFocalLength);
			JPatchActionEdit actionEdit = new JPatchActionEdit("change focal length");
			actionEdit.addEdit(edit);
			MainFrame.getInstance().getUndoManager().addEdit(actionEdit);
			((Camera) animObject).setFocalLength(newFocalLength);
			MainFrame.getInstance().getJPatchScreen().update_all();
			MainFrame.getInstance().getTimelineEditor().repaint();
		} else if (e.getSource() == inputSize) {
			float newSize = inputSize.getFloatValue();
			MotionCurveSet.Light mcs = (MotionCurveSet.Light) MainFrame.getInstance().getAnimation().getCurvesetFor(animObject);
			AtomicModifyMotionCurve.Float edit = new AtomicModifyMotionCurve.Float(mcs.size, MainFrame.getInstance().getAnimation().getPosition(), newSize);
			JPatchActionEdit actionEdit = new JPatchActionEdit("change light size");
			actionEdit.addEdit(edit);
			MainFrame.getInstance().getUndoManager().addEdit(actionEdit);
			((AnimLight) animObject).setSize(newSize);
			MainFrame.getInstance().getJPatchScreen().update_all();
			MainFrame.getInstance().getTimelineEditor().repaint();
		} else if (e.getSource() == inputIntensity) {
			float newIntensity = inputIntensity.getFloatValue();
			MotionCurveSet.Light mcs = (MotionCurveSet.Light) MainFrame.getInstance().getAnimation().getCurvesetFor(animObject);
			AtomicModifyMotionCurve.Float edit = new AtomicModifyMotionCurve.Float(mcs.intensity, MainFrame.getInstance().getAnimation().getPosition(), newIntensity);
			JPatchActionEdit actionEdit = new JPatchActionEdit("change light size");
			actionEdit.addEdit(edit);
			MainFrame.getInstance().getUndoManager().addEdit(actionEdit);
			((AnimLight) animObject).setIntensity(newIntensity);
			MainFrame.getInstance().getJPatchScreen().update_all();
			MainFrame.getInstance().getTimelineEditor().repaint();
		}
	}

	private void setPosition(Point3d pos) {
		MotionCurveSet mcs = MainFrame.getInstance().getAnimation().getCurvesetFor(animObject);
		AtomicModifyMotionCurve.Point3d edit = new AtomicModifyMotionCurve.Point3d(mcs.position, MainFrame.getInstance().getAnimation().getPosition(), pos);
		MainFrame.getInstance().getUndoManager().addEdit(edit);
		animObject.setPosition(pos);
		MainFrame.getInstance().getJPatchScreen().update_all();
		MainFrame.getInstance().getTimelineEditor().repaint();
	}
	
	private void setOrientation(double yaw, double pitch, double roll) {
		MotionCurveSet mcs = MainFrame.getInstance().getAnimation().getCurvesetFor(animObject);
		animObject.setOrientation(roll, pitch, yaw);
		AtomicModifyMotionCurve.Quat4f edit = new AtomicModifyMotionCurve.Quat4f(mcs.orientation, MainFrame.getInstance().getAnimation().getPosition(), animObject.getOrientation());
		MainFrame.getInstance().getUndoManager().addEdit(edit);
		MainFrame.getInstance().getJPatchScreen().update_all();
		MainFrame.getInstance().getTimelineEditor().repaint();
	}
}
