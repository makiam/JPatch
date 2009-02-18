package com.jpatch.afw.control;

import com.jpatch.afw.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class UndoExplorer {
	private static final ListModel EMPTY_LIST_MODEL = new AbstractListModel() {
		public Object getElementAt(int index) {
			throw new UnsupportedOperationException();
		}
		public int getSize() {
			return 0;
		}
	};
	private static final TableModel EMPTY_TABLE_MODEL = new AbstractTableModel() {
		public int getColumnCount() {
			return 0;
		}
		public int getRowCount() {
			return 0;
		}
		public Object getValueAt(int arg0, int arg1) {
			throw new UnsupportedOperationException();
		}
	};
	
	private final JPatchUndoManager undoManager;
	private JList undoList = new JList();
	private JList editList = new JList(EMPTY_LIST_MODEL);
	private JTable inspector = new JTable(EMPTY_TABLE_MODEL);
	private JComponent component;
	
	public UndoExplorer(JPatchUndoManager undoMngr) {
		JScrollPane undoListSP = new JScrollPane(undoList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollPane editListSP = new JScrollPane(editList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollPane inspectorSP = new JScrollPane(inspector, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		JSplitPane rightSplit = new JSplitPane(SwingConstants.VERTICAL, editListSP, inspectorSP);
		component = new JSplitPane(SwingConstants.VERTICAL, undoListSP, rightSplit);
		
		undoManager = undoMngr;
		undoList.setModel(undoManager.asListModel());
		undoManager.addUndoListener(new JPatchUndoListener() {

			public void editAdded(JPatchUndoManager undoManager) {
				update();
			}

			public void redoPerformed(JPatchUndoManager undoManager) {
				update();
			}

			public void undoPerformed(JPatchUndoManager undoManager) {
				update();
			}
			
			private void update() {
				final int position = undoManager.getPosition();
				if (position >= 0) {
					undoList.setSelectedIndex(position);
				} else {
					undoList.clearSelection();
				}
				undoList.setModel(undoManager.asListModel());
			}
		});
		
		undoList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				final int index = undoList.getSelectedIndex();
				if (index >= 0) {
					final ListModel listModel = undoManager.getListModelForEdit(index);
					editList.setModel(listModel);
				} else {
					editList.setModel(EMPTY_LIST_MODEL);
				}
			}
		});
		
		editList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				final int index = editList.getSelectedIndex();
				if (index >= 0) {
					final TableModel tableModel = Utils.reflectionTableModel(editList.getSelectedValue());
					inspector.setModel(tableModel);
				} else {
					inspector.setModel(EMPTY_TABLE_MODEL);
				}
			}
		});
	}
	
	public JComponent getComponent() {
		return component;
	}
}
