package jpatch.boundary;

import java.awt.*;
import javax.swing.*;
import jpatch.boundary.action.*;
import jpatch.boundary.tools.*;

public final class MeshToolBar extends JToolBar {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int DEFAULT = 0;
	public static final int ADD = 1;
	public static final int ADD_LOCK = 2;
	public static final int ROTATE = 3;
	public static final int ROTOSCOPE = 4;
	public static final int MAGNET = 5;
	public static final int VIEW_ROTATE = 6;
	public static final int VIEW_MOVE = 7;
	public static final int VIEW_ZOOM = 8;
	
	private ButtonGroup bgAction;
	//private ButtonGroup bgView;
	private ButtonGroup bgTangentMode = new ButtonGroup();
	
	private AbstractAction selectMoveAction = new SelectMoveAction();
//	private AbstractAction magnetAction = new MagnetAction();
	private AbstractAction weightAction = new WeightSelectionAction();
	private AbstractAction addControlPointAction = new AddControlPointAction();
	private AbstractAction addMultiControlPointAction = new AddMultiControlPointAction();
	private AbstractAction rotateAction = new RotateAction();
	private AbstractAction removeAction = new RemoveControlPointAction();
	private AbstractAction deleteAction = new DeleteControlPointAction();
	private AbstractAction cloneAction = new CloneAction();
	private AbstractAction extrudeAction = new ExtrudeAction();
	private AbstractAction latheAction = new LatheAction();
	private AbstractAction latheEditorAction = new LatheEditorAction();
	private AbstractAction addBoneAction = new AddBoneAction();
	
	private AbstractButton buttonSelectMove = Command.getButtonFor("default tool");//new JPatchToggleButton(selectMoveAction);
	private AbstractButton buttonWeight = new JPatchToggleButton(weightAction);
	private AbstractButton buttonAdd = new JPatchToggleButton(addControlPointAction);
	private AbstractButton buttonAddMulti = new JPatchToggleButton(addMultiControlPointAction);
	private AbstractButton buttonRotate = new JPatchToggleButton(rotateAction);
//	private AbstractButton buttonRemove = new JPatchButton(removeAction);
//	private AbstractButton buttonDelete = new JPatchButton(deleteAction);
	private AbstractButton buttonAddBone = new JPatchToggleButton(addBoneAction);
	
	private AbstractButton buttonClone = new JPatchButton(cloneAction);
	private AbstractButton buttonExtrude = new JPatchButton(extrudeAction);
	private AbstractButton buttonLathe = new JPatchButton(latheAction);
	private AbstractButton buttonLatheEditor = new JPatchButton(latheEditorAction);
	private AbstractButton buttonPeak = new JPatchToggleButton(new PeakAction());
	private AbstractButton buttonRound = new JPatchToggleButton(new RoundAction());
	private AbstractButton buttonFivePoint = new JPatchButton(new MakeFivePointPatchAction());
	
	private AbstractButton buttonComputePatches = new JPatchButton(new ComputePatchesAction());
	private AbstractButton buttonRotoscope = new JPatchToggleButton(new RotoscopeAction());
	
	private int iMode = DEFAULT;
	
	public MeshToolBar(ButtonGroup action) {
		//super("Mesh Panel",SwingConstants.VERTICAL);
		setFloatable(false);
		setOrientation(JToolBar.VERTICAL);
		//setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		/* Buttons */
		buttonWeight.setEnabled(false);
		
		/* ButtonGroups */
		bgAction = action;
		
		bgAction.add(buttonSelectMove);
		bgAction.add(buttonRotate);
		bgAction.add(buttonWeight);
		bgAction.add(buttonAdd);
		bgAction.add(buttonAddMulti);
		bgAction.add(buttonRotoscope);
		bgAction.add(buttonAddBone);
		
		bgTangentMode.add(buttonPeak);
		bgTangentMode.add(buttonRound);
		
		buttonRound.setSelected(true);
		
		/* add buttons */
		add(JPatchSeparator.createVerticalSeparator());
		add(buttonSelectMove);
		add(buttonAdd);
		//add(buttonAddMulti);
		add(buttonAddBone);
		//add(JPatchSeparator.createVerticalSeparator());
		add(buttonRotate);
		add(buttonWeight);
		add(JPatchSeparator.createVerticalSeparator());
		add(buttonRotoscope);
		//add(buttonRemove);
		//add(buttonDelete);
		add(JPatchSeparator.createVerticalSeparator());
		add(new JPatchToggleButton(new TangentAction()));
		add(buttonPeak);
		add(buttonRound);
		add(JPatchSeparator.createVerticalSeparator());
		add(new JPatchButton(new DetachControlPointsAction()));
		add(JPatchSeparator.createVerticalSeparator());
		add(buttonClone);
		add(buttonExtrude);
		add(buttonLathe);
		add(buttonLatheEditor);
		add(JPatchSeparator.createVerticalSeparator());
		add(buttonFivePoint);
		add(buttonComputePatches);
		
//		buttonAdd.getAction().setEnabled(false);
		
		/* set keyboard shortcuts */
//		setFocusable(false);
	}
	
	public AbstractButton getWeightButton() {
		return buttonWeight;
	}
	
	public void addKeyBindings() {
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("next curve"), new NextCurveAction(1));
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("extend selection"), new ExtendSelectionAction());
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("insert point"), new InsertControlPointAction());
		
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("default tool"), buttonSelectMove);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("rotate tool"), buttonRotate);
//		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("magnet tool"), buttonMagnet);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("add point"), buttonAdd);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("add multiple points"), buttonAddMulti);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("remove points"), removeAction);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("delete points"), deleteAction);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("clone"), cloneAction);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("compute patches"), buttonComputePatches);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("extrude"), extrudeAction);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("lathe"), latheAction);
	}
	
	public void removeKeyBindings() {
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("next curve"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("extend selection"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("insert point"));
		
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("default tool"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("rotate tool"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("magnet tool"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("add point"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("add multiple points"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("remove points"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("delete points"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("clone"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("compute patches"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("extrude"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("lathe"));
	}
	
	public AbstractButton[] getMorphButtons() {
		return new AbstractButton[] {
			buttonSelectMove,
			buttonRotate,
//			buttonMagnet,
			buttonRotoscope
		};
	}
	
	public void dumpComponents() {
		Component[] acomp = getComponents();
		for (int i = 0; i < acomp.length; i++) {
			System.out.println(acomp[i].getName());
		}
	}
	
	public void selectButton(int button) {
		switch (button) {
			case DEFAULT:
				buttonSelectMove.setSelected(true);
			break;
			case ADD:
				buttonAdd.setSelected(true);
			break;
			case ADD_LOCK:
				buttonAddMulti.setSelected(true);
			break;
			case ROTATE:
				buttonRotate.setSelected(true);
			break;
			case ROTOSCOPE:
				buttonRotoscope.setSelected(true);
			break;
		}
	}
	
	public void setMode(int mode) {
		iMode = mode;
	}
	
	public int getMode() {
		return iMode;
	}
	
	public void reset() {
		//buttonSelectMove.doClick();
		buttonSelectMove.setSelected(true);
//		MainFrame.getInstance().getJPatchScreen().setTool(new DefaultTool());
		iMode = DEFAULT;
	}
}
