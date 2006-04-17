package jpatch.boundary.dialog;

import com.sun.java_cup.internal.shift_action;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import jpatch.boundary.*;
import jpatch.boundary.action.*;

public class KeyMappingDialog extends JDialog {
	
	
	public KeyMappingDialog() {
		super(MainFrame.getInstance(), "Keyboard mapping", true);
		
		
		final Map<String, KeyStroke> keyMap = Actions.getInstance().getKeyMapping();
		final List<String> keyList = new ArrayList(keyMap.keySet());
		Collections.sort(keyList);
		TableModel tableModel = new AbstractTableModel() {
			public int getRowCount() {
				return keyMap.size();
			}
			public int getColumnCount() {
				return 2;
			}
			public Object getValueAt(int row, int column) {
				switch(column) {
				case 0:
					return keyList.get(row);
				case 1:
					return keyMap.get(keyList.get(row));
				case 2:
					return new JButton("...");
				}
				throw new ArrayIndexOutOfBoundsException("row " + row + ", column " + column);
			}
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return columnIndex == 1;
			}
		};
		
		JTable table = new JTable(tableModel);
		final JButton[] buttons = new JButton[keyMap.size()];
		for (int i = 0; i < buttons.length; i++) {
			buttons[i] = new JButton("set...");
			buttons[i].setPreferredSize(new Dimension(50, 20));
		}
		table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			private Font font1 = new Font("Sans Serif", Font.PLAIN, 12);
			private Font font2 = new Font("Monospaced", Font.BOLD, 12);
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				switch (column) {
				case 0:
					setBackground(KeyMappingDialog.this.getBackground());
					setFont(font1);
				break;
				case 1:
					setBackground(Color.WHITE);
					setFont(font2);
				break;
				}
				return this;
			}
		});
		
		
		table.setDefaultEditor(Object.class, new DefaultCellEditor(new EditorTextField()) {
			public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, final int row, int column) {
				Component component = super.getTableCellEditorComponent(table, value, isSelected, row, column);
				String text = "ENTER KEY COMBINATION";
				((JTextField) component).setText(text);
//				component.setBackground(Color.BLUE);
//				component.setForeground(Color.WHITE);
				component.setBackground(((JTextField) component).getSelectionColor());
				((JTextField) component).setSelectionColor(new Color(0x7777ff));
				component.setFont(new Font("Monospaced", Font.BOLD, 12));
//				((JTextField) component).setSelectionStart(0);
//				((JTextField) component).setSelectionEnd(text.length());
				return component;
			}
		});
		
		table.setBackground(getBackground());
		table.getColumnModel().getColumn(0).setHeaderValue("Action");
		table.getColumnModel().getColumn(1).setHeaderValue("KeyStroke");
		add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		setSize(400, 400);
		setLocationRelativeTo(MainFrame.getInstance());
		setVisible(true);
	}
	
	static class EditorTextField extends JTextField implements KeyListener {
		
		EditorTextField() {
			setInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
			setInputMap(WHEN_FOCUSED, null);
			setInputMap(WHEN_IN_FOCUSED_WINDOW, null);
			setActionMap(null);
			addKeyListener(this);
			setFocusTraversalKeysEnabled(false);
			requestFocusInWindow();
		}

		public void keyTyped(KeyEvent e) {
			e.consume();
		}

		public void keyPressed(KeyEvent e) {
			e.consume();
			setText(KeyStroke.getKeyStrokeForEvent(e).toString());
			setBackground(Color.WHITE);
			repaint();
		}

		public void keyReleased(KeyEvent e) {
			e.consume();
			transferFocus();
		}
	}
//	private GridBagConstraints gridBagConstraints(int x, int y, int anchor) {
//		return new GridBagConstraints(x, y, 1, 1, 1.0, 1.0, anchor, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
//	}
}
