package jpatch.boundary.dialog;

import com.sun.java_cup.internal.shift_action;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import jpatch.boundary.*;
import jpatch.boundary.action.*;
import jpatch.boundary.ui.JPatchButton;

public class KeyMappingDialog extends JDialog {
	private Font font1 = new Font("Sans Serif", Font.PLAIN, 12);
	private Font font2 = new Font("Monospaced", Font.PLAIN, 12);
	private Font font3 = new Font("Monospaced", Font.BOLD, 12);

	Map<String, KeyStroke> keyMap = Actions.getInstance().getKeyMapping();
	List<String> keyList = new ArrayList<String>(keyMap.keySet());
	String editingKey;
	
	public KeyMappingDialog() {
		super(MainFrame.getInstance(), "Keyboard mapping", true);
		
		setLayout(new BorderLayout());
		Collections.sort(keyList);
		TableModel tableModel = new AbstractTableModel() {
			public int getRowCount() {
				return keyList.size();
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
		
		final JTable table = new JTable(tableModel);
		final JButton[] buttons = new JButton[keyList.size()];
		for (int i = 0; i < buttons.length; i++) {
			buttons[i] = new JButton("set...");
			buttons[i].setPreferredSize(new Dimension(50, 20));
		}
		table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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
					setToolTipText("Doubleclick to edit");
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
//				((JTextField) component).setSelectionColor(Color.WHITE);
				component.setFont(font3);
//				((JTextField) component).setSelectionStart(0);
//				((JTextField) component).setSelectionEnd(text.length());
				editingKey = keyList.get(row);
				return component;
			}
		});
		
		table.setBackground(getBackground());
		table.getColumnModel().getColumn(0).setHeaderValue("Action");
		table.getColumnModel().getColumn(0).setMaxWidth(200);
		table.getColumnModel().getColumn(0).setMinWidth(200);
		
		table.getColumnModel().getColumn(1).setHeaderValue("KeyStroke");
		
		table.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.isPopupTrigger())
					showPopup(e);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger())
					showPopup(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger())
					showPopup(e);
			}
			
			private void showPopup(MouseEvent e) {
				if (table.getColumnModel().getColumnIndexAtX(e.getX()) != 1)
					return;
				final int row = (e.getY() / table.getRowHeight());
				final String key = keyList.get(row);
				JPopupMenu popupMenu = new JPopupMenu();
//				JLabel label = new JLabel(keyList.get(row));
//				label.setFont(new Font("Sans Serif", Font.BOLD, 12));
//				popupMenu.add(label);
				JMenuItem itemEnter = new JMenuItem("Enter KeyStroke manually...");
				JMenuItem itemClear = new JMenuItem("Clear");
				JMenuItem itemPrev = new JMenuItem("Reset to previous");
				JMenuItem itemDefault = new JMenuItem("Reset to default");
				
				itemEnter.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						KeyStrokeDialog dialog = new KeyStrokeDialog(keyMap.get(key), key);
						if (dialog.okOption())
							keyMap.put(key, dialog.keyStroke());
						table.repaint();
					}
				});
				
				itemClear.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						keyMap.put(key, null);
						table.repaint();
					}
				});
				
				itemPrev.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						keyMap.put(key, KeyStroke.getKeyStroke((String) Actions.getInstance().getAction(key).getValue(JPatchAction.ACCELERATOR)));
						table.repaint();
					}
				});
				
				itemDefault.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						keyMap.put(key, Actions.getInstance().getDefaultKeyStroke(key));
						table.repaint();
					}
				});
				
				popupMenu.add(itemEnter);
				popupMenu.add(itemClear);
				popupMenu.add(itemPrev);
				popupMenu.add(itemDefault);
				popupMenu.show(table, e.getX(), e.getY());
			}
		});
		add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		JButton buttonReset = new JButton("Reset");
		JButton buttonOk = new JButton("OK");
		JButton buttonCancel = new JButton("Cancel");
		buttonPanel.add(buttonReset);
		JPanel separator = new JPanel();
		separator.setPreferredSize(new Dimension(40, 10));
		buttonPanel.add(separator);
		buttonPanel.add(buttonOk);
		buttonPanel.add(buttonCancel);
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
				for (String key : keyMap.keySet()) {
					KeyStroke ks = keyMap.get(key);
					String accelerator = ks == null ? null : ks.toString();
					Actions.getInstance().getAction(key).putValue(JPatchAction.ACCELERATOR, accelerator);
				}
				Actions.getInstance().saveKeySettings();
			}
		});
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttonReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(KeyMappingDialog.this, "This will reset all key-bindings to their default values", "Reset to defaults", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
					for (String key : keyMap.keySet()) {
						keyMap.put(key, Actions.getInstance().getDefaultKeyStroke(key));
					}
					table.repaint();
				}
			}
		});
		add(buttonPanel, BorderLayout.SOUTH);
		setSize(500, 400);
		setLocationRelativeTo(MainFrame.getInstance());
		setVisible(true);
	}
	
	class EditorTextField extends JTextField implements KeyListener {
		private KeyStroke ks;
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
			ks = KeyStroke.getKeyStrokeForEvent(e);
			setText(ks.toString());
//			setBackground(Color.WHITE);
			repaint();
		}

		public void keyReleased(KeyEvent e) {
			e.consume();
			setBackground(Color.WHITE);
			repaint();
			transferFocus();
			keyMap.put(editingKey, ks);
		}
	}
	
	
//	private GridBagConstraints gridBagConstraints(int x, int y, int anchor) {
//		return new GridBagConstraints(x, y, 1, 1, 1.0, 1.0, anchor, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
//	}
	class KeyStrokeDialog extends JDialog {
		JButton ok = new JButton("OK");
		JButton cancel = new JButton("Cancel");
		private KeyStroke ks;
		private boolean okOption = false;
		
		KeyStrokeDialog(KeyStroke ks, String action) {
			super(KeyMappingDialog.this, "Enter KeyStroke", true);
			this.ks = ks;
			JPanel buttonPanel = new JPanel();
			Box textPanel = Box.createVerticalBox();
			buttonPanel.add(ok);
			buttonPanel.add(cancel);
			setLayout(new BorderLayout());
//			textPanel.add(new JLabel("KeyStroke for \"" + action + "\":"));
			KeyStrokeTextField keyStrokeTextField = new KeyStrokeTextField(ks);
			textPanel.add(keyStrokeTextField);
			textPanel.setBorder(new TitledBorder("KeyStroke for \"" + action + "\":"));
			add(textPanel, BorderLayout.CENTER);
			add(buttonPanel, BorderLayout.SOUTH);
			ok.addActionListener(keyStrokeTextField);
			cancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					KeyStrokeDialog.this.ks = null;
					okOption = false;
					dispose();
				}
			});
			pack();
			setLocationRelativeTo(KeyMappingDialog.this);
			setVisible(true);
		}
		
		boolean okOption() {
			return okOption;
		}
		
		KeyStroke keyStroke() {
			return ks;
		}
		
		class KeyStrokeTextField extends JTextField implements CaretListener, ActionListener {
			
			KeyStrokeTextField(KeyStroke ks) {
				super(20);
				if (ks != null) {
					setText(ks.toString());
					ok.setEnabled(true);
				} else {
					setBackground(Color.YELLOW);
					ok.setEnabled(false);
				}
				addCaretListener(this);
				addActionListener(this);
			}

			public void caretUpdate(CaretEvent e) {
				ks = KeyStroke.getKeyStroke(getText());
				if (ks == null) {
					setBackground(Color.YELLOW);
					ok.setEnabled(false);
				} else {
					setBackground(Color.WHITE);
					ok.setEnabled(true);
				}
			}

			public void actionPerformed(ActionEvent e) {
				if (ks != null) {
					okOption = true;
					KeyStrokeDialog.this.dispose();
				}
			}
		}
	}
}
