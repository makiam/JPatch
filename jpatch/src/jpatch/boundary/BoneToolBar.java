package jpatch.boundary;

import javax.swing.*;
import jpatch.boundary.action.*;

public final class BoneToolBar extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ButtonGroup bgAction;
	
	private AbstractButton buttonSelectMove;
	private AbstractButton buttonAdd;
	private AbstractButton buttonDetach;
	private AbstractButton buttonDelete;
	private AbstractButton buttonRemove;
	
	public BoneToolBar(ButtonGroup action) {
		//super("Bone Panel");
		
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		bgAction = action;
		buttonSelectMove = new JPatchToggleButton(new SelectMoveBoneAction());
		buttonAdd = new JPatchToggleButton(new AddBoneAction());
		buttonDetach = new JPatchButton(new DetachBoneAction());
		buttonDelete = new JPatchButton(new DeleteBoneAction());
		buttonRemove = new JPatchButton(new RemoveBoneAction());
		
		bgAction.add(buttonSelectMove);
		bgAction.add(buttonAdd);
		
		add(JPatchSeparator.createVerticalSeparator());
		add(buttonSelectMove);
		add(buttonAdd);
		add(buttonDetach);
		add(buttonDelete);
		add(buttonRemove);
		//add(new JPatchButton(new TestShowBoneTreeAction()));
		/*
		buttonSelectMove = new JToggleButton(new SelectMoveAction());
		//buttonMagnet = new JToggleButton(new MagnetAction());
		buttonAdd = new JToggleButton(new AddControlPointAction());
		buttonAddMulti = new JToggleButton(new AddMultiControlPointAction());
		buttonBias = new JToggleButton(new ChangeBiasAction());
		buttonScale = new JToggleButton(new ScaleAction());
		buttonRotate = new JToggleButton(new RotateAction());
		
		bgAction.add(buttonSelectMove);
		//bgAction.add(buttonMagnet);
		bgAction.add(buttonAdd);
		bgAction.add(buttonAddMulti);
		bgAction.add(buttonRotate);
		bgAction.add(buttonScale);
		bgAction.add(buttonBias);
		
		add(buttonSelectMove);
		//add(buttonMagnet);
		add(buttonAdd);
		add(buttonAddMulti);
		add(buttonScale);
		add(buttonRotate);
		addSeparator();
		add(buttonBias);
		MainFrame.getInstance().getKeyAdapter().addButton(buttonAdd,new JPatchKey(KeyEvent.VK_A));
		MainFrame.getInstance().getKeyAdapter().addButton(buttonAddMulti,new JPatchKey(KeyEvent.VK_A,KeyEvent.SHIFT_DOWN_MASK));
		MainFrame.getInstance().getKeyAdapter().addButton(buttonSelectMove, new JPatchKey(KeyEvent.VK_ESCAPE));
		MainFrame.getInstance().getKeyAdapter().addButton(buttonRotate, new JPatchKey(KeyEvent.VK_R));
		MainFrame.getInstance().getKeyAdapter().addButton(buttonScale, new JPatchKey(KeyEvent.VK_S));
		*/
	}
	
	public void reset() {
		buttonSelectMove.doClick();
	}
}
