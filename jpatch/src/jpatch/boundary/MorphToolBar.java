package jpatch.boundary;

import java.awt.*;
import javax.swing.*;
import jpatch.boundary.action.*;
import jpatch.boundary.tools.*;

public final class MorphToolBar extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int DEFAULT = 0;
	//public static final int ADD = 1;
	//public static final int ADD_LOCK = 2;
	public static final int ROTATE = 3;
	public static final int ROTOSCOPE = 4;
	public static final int MAGNET = 5;
	public static final int VIEW_ROTATE = 6;
	public static final int VIEW_MOVE = 7;
	public static final int VIEW_ZOOM = 8;
	//
	private ButtonGroup bgAction;
	////private ButtonGroup bgView;
	//private ButtonGroup bgTangentMode = new ButtonGroup();
	//
	private AbstractAction selectMoveAction = new SelectMoveAction();
	private AbstractAction magnetAction = new MagnetAction();
	//private AbstractAction addControlPointAction = new AddControlPointAction();
	//private AbstractAction addMultiControlPointAction = new AddMultiControlPointAction();
	private AbstractAction rotateAction = new RotateAction();
	//private AbstractAction removeAction = new RemoveControlPointAction();
	//private AbstractAction deleteAction = new DeleteControlPointAction();
	//private AbstractAction cloneAction = new CloneAction();
	//private AbstractAction extrudeAction = new ExtrudeAction();
	//private AbstractAction latheAction = new LatheAction();
	//
	private AbstractButton buttonSelectMove = new JPatchToggleButton(selectMoveAction);
	private AbstractButton buttonMagnet = new JPatchToggleButton(magnetAction);
	//private AbstractButton buttonAdd = new JPatchToggleButton(addControlPointAction);
	//private AbstractButton buttonAddMulti = new JPatchToggleButton(addMultiControlPointAction);
	private AbstractButton buttonRotate = new JPatchToggleButton(rotateAction);
	//private AbstractButton buttonRemove = new JPatchButton(removeAction);
	//private AbstractButton buttonDelete = new JPatchButton(deleteAction);
	//private AbstractButton buttonClone = new JPatchButton(cloneAction);
	//private AbstractButton buttonExtrude = new JPatchButton(extrudeAction);
	//private AbstractButton buttonLathe = new JPatchButton(latheAction);
	//private AbstractButton buttonPeak = new JPatchToggleButton(new PeakAction());
	//private AbstractButton buttonRound = new JPatchToggleButton(new RoundAction());
	//private AbstractButton buttonFivePoint = new JPatchButton(new MakeFivePointPatchAction());
	//
	//private AbstractButton buttonComputePatches = new JPatchButton(new ComputePatchesAction());
	private AbstractButton buttonRotoscope = new JPatchToggleButton(new RotoscopeAction());
	//
	private int iMode = DEFAULT;
	
	public MorphToolBar(ButtonGroup action) {
		
		//buttonSelectMove = buttons[0];
		//buttonMagnet = buttons[1];
		//buttonRotate = buttons[2];
		//buttonRotoscope = buttons[3];
		
		//super("Mesh Panel",SwingConstants.VERTICAL);
		
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		/* Buttons */
		
		/* ButtonGroups */
		bgAction = action;
		//
		bgAction.add(buttonSelectMove);
		bgAction.add(buttonRotate);
		bgAction.add(buttonMagnet);
		//bgAction.add(buttonAdd);
		//bgAction.add(buttonAddMulti);
		bgAction.add(buttonRotoscope);
		//
		//bgTangentMode.add(buttonPeak);
		//bgTangentMode.add(buttonRound);
		//
		//buttonRound.setSelected(true);
		
		/* add buttons */
		add(JPatchSeparator.createVerticalSeparator());
		add(buttonSelectMove);
		//add(buttonAdd);
		//add(buttonAddMulti);
		//add(JPatchSeparator.createVerticalSeparator());
		add(buttonRotate);
		add(buttonMagnet);
		add(JPatchSeparator.createVerticalSeparator());
		add(buttonRotoscope);
		//add(buttonRemove);
		//add(buttonDelete);
		//add(JPatchSeparator.createVerticalSeparator());
		//add(new JPatchToggleButton(new TangentAction()));
		//add(buttonPeak);
		//add(buttonRound);
		//add(JPatchSeparator.createVerticalSeparator());
		//add(new JPatchButton(new DetachControlPointsAction()));
		//add(JPatchSeparator.createVerticalSeparator());
		//add(buttonClone);
		//add(buttonExtrude);
		//add(buttonLathe);
		//add(JPatchSeparator.createVerticalSeparator());
		//add(buttonFivePoint);
		//add(buttonComputePatches);
		
		
		///* set keyboard shortcuts */
		//MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("default tool"), buttonSelectMove);
		//MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("rotate tool"), buttonRotate);
		//MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("magnet tool"), buttonMagnet);
		//MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("add point"), buttonAdd);
		//MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("add multiple points"), buttonAddMulti);
		//MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("remove points"), removeAction);
		//MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("delete points"), deleteAction);
		//MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("clone"), cloneAction);
		//MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("compute patches"), buttonComputePatches);
		//MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("extrude"), extrudeAction);
		//MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("lathe"), latheAction);
		//setFocusable(false);
	}
	
	public void addKeyBindings() {
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("default tool"), buttonSelectMove);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("rotate tool"), buttonRotate);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("magnet tool"), buttonMagnet);
	}
	
	public void removeKeyBindings() {
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("default tool"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("rotate tool"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("magnet tool"));
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
			//case ADD:
			//	buttonAdd.setSelected(true);
			//break;
			//case ADD_LOCK:
			//	buttonAddMulti.setSelected(true);
			//break;
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
		MainFrame.getInstance().getJPatchScreen().setTool(new DefaultTool());
		iMode = DEFAULT;
	}
}
