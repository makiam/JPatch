package jpatch.boundary;

import javax.swing.*;

import jpatch.boundary.action.*;
import jpatch.control.edit.*;

public final class MainToolBar extends JToolBar implements JPatchUndoManager.UndoListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ButtonGroup bgAction;
	private ButtonGroup bgView;
	//private ButtonGroup bgMode;
	
	private AbstractAction viewSingleAction = new ViewSingleAction();
	private AbstractAction viewSplitHorizontalAction = new ViewSplitHorizontalAction();
	private AbstractAction viewSplitVerticalAction = new ViewSplitVerticalAction();
	private AbstractAction viewQuadAction = new ViewQuadAction();
	
	private AbstractAction undoAction = new UndoAction();
	private AbstractAction redoAction = new RedoAction();
	
	private AbstractAction viewRotateAction = new ViewRotateAction();
	private AbstractAction viewMoveAction = new ViewMoveAction();
	private AbstractAction viewZoomAction = new ViewZoomAction();
	
	private AbstractAction xLockAction = new XLockAction();
	private AbstractAction yLockAction = new YLockAction();
	private AbstractAction zLockAction = new ZLockAction();
	
	private AbstractAction gridSnapAction = new GridSnapAction();
	private AbstractAction hideAction = new HideAction();
	
	private AbstractButton buttonSingle = new JPatchToggleButton(viewSingleAction);
	private AbstractButton buttonHorizontal = new JPatchToggleButton(viewSplitHorizontalAction);
	private AbstractButton buttonVertical = new JPatchToggleButton(viewSplitVerticalAction);
	private AbstractButton buttonQuad = new JPatchToggleButton(viewQuadAction);
	
	private AbstractButton buttonUndo = new JPatchButton(undoAction);
	private AbstractButton buttonRedo = new JPatchButton(redoAction);
	
	private AbstractButton buttonRotate = new JPatchToggleButton(viewRotateAction);
	private AbstractButton buttonMove = new JPatchToggleButton(viewMoveAction);
	private AbstractButton buttonZoom = new JPatchToggleButton(viewZoomAction);
	
	private AbstractButton buttonXLock = new JPatchToggleButton(xLockAction);
	private AbstractButton buttonYLock = new JPatchToggleButton(yLockAction);
	private AbstractButton buttonZLock = new JPatchToggleButton(zLockAction);
	
	private AbstractButton buttonGridSnap = new JPatchToggleButton(gridSnapAction);
	private AbstractButton buttonHide = new JPatchToggleButton(hideAction);
	
	//private AbstractButton buttonMesh;
	//private AbstractButton buttonMorph;
	//private AbstractButton buttonBone;
	
	public MainToolBar(ButtonGroup action) {
		setFloatable(false);
		setOrientation(JToolBar.HORIZONTAL);
		
		/* Buttons */
		buttonXLock.setSelectedIcon(new ImageIcon(getClass().getClassLoader().getResource("jpatch/images/xlocked.png")));
		buttonYLock.setSelectedIcon(new ImageIcon(getClass().getClassLoader().getResource("jpatch/images/ylocked.png")));
		buttonZLock.setSelectedIcon(new ImageIcon(getClass().getClassLoader().getResource("jpatch/images/zlocked.png")));
		buttonGridSnap.setSelectedIcon(new ImageIcon(getClass().getClassLoader().getResource("jpatch/images/grid_snap.png")));
		buttonHide.setSelectedIcon(new ImageIcon(getClass().getClassLoader().getResource("jpatch/images/hide2.png")));
		
//		buttonGridSnap.setEnabled(false);
		//buttonSingle.setSelected(true);
		
		/* ButtonGroups */
		bgAction = action;
		bgView = new ButtonGroup();
		//bgMode = new ButtonGroup();
		
		bgAction.add(buttonRotate);
		bgAction.add(buttonMove);
		bgAction.add(buttonZoom);

		
		bgView.add(buttonSingle);
		bgView.add(buttonHorizontal);
		bgView.add(buttonVertical);
		bgView.add(buttonQuad);
		
		buttonGridSnap.setSelected(JPatchSettings.getInstance().bGridSnap);
		
		/* add the buttons */
		add(new JPatchButton(new NewAction()));
		add(new JPatchButton(new ImportJPatchAction()));
		add(new JPatchButton(new SaveAsAction(false)));
		add(JPatchSeparator.createHorizontalSeparator());
		add(buttonSingle);
		add(buttonHorizontal);
		add(buttonVertical);
		add(buttonQuad);
		add(JPatchSeparator.createHorizontalSeparator());
		add(buttonRotate);
		add(buttonMove);
		add(buttonZoom);
		add(JPatchSeparator.createHorizontalSeparator());
		add(new JPatchButton(new ZoomToFitAction()));
		add(JPatchSeparator.createHorizontalSeparator());
		add(buttonUndo);
		add(buttonRedo);
		add(JPatchSeparator.createHorizontalSeparator());
		add(buttonXLock);
		add(buttonYLock);
		add(buttonZLock);
		add(JPatchSeparator.createHorizontalSeparator());
		add(buttonGridSnap);
		add(buttonHide);
		
		//>>>>> test-replace
		/* set keyboard shortcuts 
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("single view"), buttonSingle);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("horizontal split view"), buttonHorizontal);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("vertical split view"), buttonVertical);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("quad view"), buttonQuad);
		
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("rotate view"), buttonRotate);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("move view"), buttonMove);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("zoom view"), buttonZoom);
		
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("undo"), undoAction);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("redo"), redoAction);
		
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("lock x"), buttonXLock);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("lock y"), buttonYLock);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("lock z"), buttonZLock);
		
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("grid"), buttonGridSnap);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("hide"), buttonHide);
		*/
		addKeyBindings();
		//<<<<< test-replace
		
		setFocusable(false);
		buttonUndo.setEnabled(false);
		buttonRedo.setEnabled(false);
		MainFrame.getInstance().getUndoManager().addUndoListener(this);
	}
	
	public void undoStateChanged(JPatchUndoManager undoManager) {
		buttonUndo.setEnabled(undoManager.canUndo());
		buttonUndo.setToolTipText("undo " + undoManager.undoName() + KeyMapping.getKeyString("undo"));
		buttonRedo.setEnabled(undoManager.canRedo());
		buttonRedo.setToolTipText("redo " + undoManager.redoName() + KeyMapping.getKeyString("redo"));
	}
	
	//>>>>> test-add
	public void addKeyBindings() {
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("single view"), buttonSingle);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("horizontal split view"), buttonHorizontal);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("vertical split view"), buttonVertical);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("quad view"), buttonQuad);
		
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("rotate view"), buttonRotate);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("move view"), buttonMove);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("zoom view"), buttonZoom);
		
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("undo"), undoAction);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("redo"), redoAction);
		
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("lock x"), buttonXLock);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("lock y"), buttonYLock);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("lock z"), buttonZLock);
		
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("grid"), buttonGridSnap);
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("hide"), buttonHide);
	}
	
	public void removeKeyBindings() {
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("single view"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("horizontal split view"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("vertical split view"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("quad view"));
		
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("rotate view"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("move view"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("zoom view"));
		
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("undo"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("redo"));
		
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("lock x"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("lock y"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("lock z"));
		
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("grid"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("hide"));		
	}
	//<<<<< test-add
	
	public void setScreenMode(JPatchScreen screen) {
		buttonSingle.setSelected(false);
		buttonHorizontal.setSelected(false);
		buttonVertical.setSelected(false);
		buttonQuad.setSelected(false);
		
		switch (screen.getMode()) {
			case JPatchScreen.SINGLE:
				buttonSingle.setSelected(true);
				break;
			case JPatchScreen.HORIZONTAL_SPLIT:
				buttonHorizontal.setSelected(true);
				break;
			case JPatchScreen.VERTICAL_SPLIT:
				buttonVertical.setSelected(true);
				break;
			case JPatchScreen.QUAD:
				buttonQuad.setSelected(true);
				break;
		}
	}
}
