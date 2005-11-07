package jpatch.boundary;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import buoy.widget.*;
import buoy.event.*;
import jpatch.entity.*;

public class PoseSliders extends BFrame {
	private BComboBox cbModels = new BComboBox();
	private AnimObject activeObject = Animator.getInstance().getActiveCamera();
	private BorderContainer content = new BorderContainer();
	private FormContainer morphs = new FormContainer(2, 2);
	private GridContainer buttons = new GridContainer(2, 1);
	private BScrollPane scrollPane = new BScrollPane(morphs);
	private BButton buttonEdit = new BButton("edit");
	private BButton buttonRemove = new BButton("remove");
	private Object eventEdit;
	private Object eventRemove;
	private HashMap mapSlider;
	
	public PoseSliders() {
		super("Object properties");
		init();
		content.add(cbModels, BorderContainer.NORTH);
		content.add(scrollPane, BorderContainer.CENTER);
		content.add(buttons, BorderContainer.SOUTH);
		//setContent(content);
		//setBounds(new Rectangle(0, 0, 200, 400));
		((JComponent) buttonEdit.getComponent()).setPreferredSize(new Dimension(100,20));
		((JComponent) buttonRemove.getComponent()).setPreferredSize(new Dimension(100,20));
		buttons.add(buttonEdit, 0, 0);
		buttons.add(buttonRemove, 1, 0);
		
		//pack();
		//Rectangle r = getBounds();
		//r.height = 300;
		//setBounds(r);
		//setVisible(true);
		cbModels.addEventLink(ValueChangedEvent.class, new Object() {
			void processEvent() {
				activeObject = (AnimObject) cbModels.getSelectedValue();
				init();
				Animator.getInstance().rerenderViewports();
				Animator.getInstance().reinitMotionCurveDisplay();
			}
		});
	}
	
	public Widget getContent() {
		return content;
	}
	
	public AnimObject getActiveObject() {
		return activeObject;
	}
	
	public void setActiveObject(AnimObject object) {
		activeObject = object;
		init();
	}
	
	public void moveSliders() {
		if (activeObject != null && activeObject instanceof AnimModel) {
			AnimModel activeModel = (AnimModel) activeObject;
			for (Iterator it = activeModel.getModel().getMorphList().iterator(); it.hasNext(); ) {
				MorphTarget morph = (MorphTarget) it.next();
				BSlider slider = (BSlider) mapSlider.get(morph);
				slider.setValue(morph.getSliderValue());
			}
		}
	}
	
	public void init() {
		mapSlider = new HashMap();
		final Animator animator = Animator.getInstance();
		//ArrayList listModels = new ArrayList();
		//for (Iterator it = Animator.getInstance().getObjectList().iterator(); it.hasNext(); ) {
		//	final Object o = it.next();
		//	if (true || o instanceof AnimModel) listModels.add(o);
		//}
		cbModels.setContents(new ArrayList(animator.getObjectList()));
		cbModels.setSelectedValue(activeObject);
		morphs.removeAll();
		//activeObject = animator.getActiveCamera();
		//if (listModels.contains(activeObject)) cbModels.setSelectedValue(activeObject);
		//else if (listModels.size() > 0) {
		//	cbModels.setSelectedIndex(0);
		//	activeObject = (AnimObject) listModels.get(0);
		//} else activeObject = null;
		if (activeObject != null) {
			final AnimModel activeModel = (activeObject instanceof AnimModel) ? (AnimModel) activeObject : null;
			buttonEdit.removeEventLink(CommandEvent.class, eventEdit);
			buttonRemove.removeEventLink(CommandEvent.class, eventRemove);
			eventEdit = new Object() {
				private void processEvent() {
					new AnimObjectEditor(activeObject, animator);
				}
			};
			eventRemove = new Object() {
				private void processEvent() {
					if (JOptionPane.showConfirmDialog(animator.getComponent(), "Do you want to remove " + (activeObject).getName(), "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) animator.removeObject(activeObject);
				}
			};
			buttonEdit.addEventLink(CommandEvent.class, eventEdit);
			buttonRemove.addEventLink(CommandEvent.class, eventRemove);
			if (activeModel != null) {
				Model model = activeModel.getModel();
				morphs.setColumnCount(2);
				if (model.getMorphList().size() > 0) {
					morphs.setRowCount(model.getMorphList().size());
					for (int row = 0; row < model.getMorphList().size(); row++) {
						final MorphTarget morph = (MorphTarget) model.getMorphList().get(row);
						final BSlider slider = new BSlider(morph.getSliderValue(), 0, 100, BSlider.HORIZONTAL);
						mapSlider.put(morph, slider);
						BLabel label = new BLabel(morph.toString());
						morphs.add(label, 0, row);
						morphs.add(slider, 1, row);
						((JComponent) label.getComponent()).setPreferredSize(new Dimension(80,20));
						((JComponent) slider.getComponent()).setPreferredSize(new Dimension(100,20));
						slider.addEventLink(ValueChangedEvent.class, new Object() {
							void processEvent() {
								morph.unapply();
								morph.setSliderValue(slider.getValue());
								morph.apply();
								Animator.getInstance().setMorphValue(activeModel, morph);
							}
						});
					}
				}
			}
		}
	}
}
