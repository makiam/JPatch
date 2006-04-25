/*
 * $Id: AbstractSettings.java,v 1.7 2006/04/25 16:23:04 sascha_l Exp $
 *
 * Copyright (c) 2005 Sascha Ledinsky
 *
 * This file is part of JPatch.
 *
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package jpatch.boundary.settings;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;
import java.util.prefs.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.vecmath.*;

import jpatch.entity.Model;

/**
 * @author sascha
 *
 */
public abstract class AbstractSettings implements TreeNode {
	private static final Preferences JPATCH_ROOT_NODE = Preferences.userRoot().node("/JPatch/settings/preferences");
	private Map mapDefaults = new HashMap();
	private List<Field> fields = new ArrayList<Field>();
	private List<TreeNode> children = new ArrayList<TreeNode>();
	private TreeNode parent = null;
	private String nodeName = "preferences";
	private Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/settings.png"));
	private TableModel tableModel = new AbstractTableModel() {
		public int getRowCount() {
			return fields.size();
		}

		public int getColumnCount() {
			return 2;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return fields.get(rowIndex).getName().replace('_', ' ');
//			case 1:
//				return fields.get(rowIndex).getType().getSimpleName();
			case 1:
				try {
					Object o = fields.get(rowIndex).get(AbstractSettings.this);
					if (o != null)
						return o;
					else
						return "";
							
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					return null;
				}
			case 3:
				return null;
			default:
				return null;
			}
		}
		
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			try {
				Field field = fields.get(rowIndex);
				if (field.getType().equals(String.class)) {
					fields.get(rowIndex).set(AbstractSettings.this, aValue);	
				} else if (field.getType().equals(int.class)) {
					fields.get(rowIndex).set(AbstractSettings.this, Integer.parseInt((String) aValue));
				} else if (field.getType().equals(long.class)) {
					fields.get(rowIndex).set(AbstractSettings.this, Long.parseLong((String) aValue));
				} else if (field.getType().equals(short.class)) {
					fields.get(rowIndex).set(AbstractSettings.this, Short.parseShort((String) aValue));
				} else if (field.getType().equals(byte.class)) {
					fields.get(rowIndex).set(AbstractSettings.this, Byte.parseByte((String) aValue));
				} else if (field.getType().equals(char.class)) {
					char[] ca = ((String) aValue).toCharArray();
					if (ca.length > 0)
						fields.get(rowIndex).set(AbstractSettings.this, ca[0]);
				} else if (field.getType().equals(float.class)) {
					fields.get(rowIndex).set(AbstractSettings.this, Float.parseFloat((String) aValue));
				} else if (field.getType().equals(double.class)) {
					fields.get(rowIndex).set(AbstractSettings.this, Double.parseDouble((String) aValue));
				}
			} catch (Exception e) {
//				e.printStackTrace();
			}
		}
		
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 1;
		}
	};
	
	private JTable table = new JTable();
	
	public  Preferences getRootNode() {
		return JPATCH_ROOT_NODE;
	}
	
	private TreeCellRenderer treeCellRenderer = new DefaultTreeCellRenderer() {
		public Component getTreeCellRendererComponent(JTree tree,Object value,boolean sel,boolean expanded,boolean leaf,int row,boolean hasFocus) {
			super.getTreeCellRendererComponent(tree,value,sel,expanded,leaf,row,hasFocus);
			setIcon(((AbstractSettings) value).getIcon());
			return this;
		}
	};
	
	private TableCellRenderer tableCellRenderer = new DefaultTableCellRenderer() {
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//			JLabel label = new JLabel();
//			label.setBackground(getBackground());
//			label.setForeground(getForeground());
//			label.setBorder(getBorder());
//			label.setText(getText());
//			label.setFont(getFont())
			if (column == 0) {
//				setFocusable(false);
//				setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
//				setText(getText() + ": ");
			} else {
				setFocusable(true);
//				setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
			}
			
//				if (value instanceof Color) {
//					JLabel label = new JLabel();
//					label.setBackground((Color) value);
//					label.setOpaque(true);
//					return label;
//				}
//				if (value instanceof Boolean)
//					return tableCellEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
//				if (row < fields.size() && !isDefault(fields.get(row)))
//					setFont(getFont().deriveFont(Font.BOLD));
				if (value instanceof Color) {
					Color color = (Color) value;
					setText(" [" + color.getRed() + " " + color.getBlue() + " " + color.getGreen() + "]");
//					label.setBackground((Color) value);
//					label.setOpaque(true);
					setIcon(new ColorIcon(color));
				} else if (value instanceof Color3f) {
					Color color = ((Color3f) value).get();
					setText(" [" + color.getRed() + " " + color.getBlue() + " " + color.getGreen() + "]");
//					label.setBackground((Color) value);
//					label.setOpaque(true);
					setIcon(new ColorIcon(color));
				} else {
					setIcon(null);
				}
				if (value instanceof File) {
					File file = (File) value;
					setText(file.getAbsolutePath());
				}
//				return new JLabel(value.getClass().getSimpleName() + " " + value);
				if (column == 1) {
					setOpaque(true);
					if (!isSelected)
						setBackground(Color.WHITE);
					setToolTipText("Click to edit");
				} else {
					setOpaque(false);
//					setBackground(new JPanel().getBackground());
					setBorder(noFocusBorder);
					setToolTipText(null);
				}
//				setOpaque(true);
				return this;
			
		}
	};
	
	private DefaultCellEditor tableCellEditor = new DefaultCellEditor(new JTextField()) {
//		private JCheckBox checkBox = new JCheckBox();
//		private JComboBox comboBox = new JComboBox();
//		private TableCellEditor checkBoxCellEditor = new DefaultCellEditor(checkBox);
//		private TableCellEditor comboBoxCellEditor = new DefaultCellEditor(comboBox);
		public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, final int row, int column) {
			if (value instanceof Boolean) {
				final JComboBox comboBox = new JComboBox();
				comboBox.addItem(true);
				comboBox.addItem(false);
				try {
					comboBox.setSelectedItem(fields.get(row).get(AbstractSettings.this));
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				comboBox.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
//						System.out.println(event);
//						System.out.println("boolean itemChanged " + comboBox.hashCode() + " " + comboBox.getSelectedItem());
						try {
							fields.get(row).set(AbstractSettings.this, comboBox.getSelectedItem());
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}
				});
				return comboBox;
			} else if (value instanceof Enum) {
				final JComboBox comboBox = new JComboBox();
				for (Object o:((Enum) value).getDeclaringClass().getEnumConstants())
					comboBox.addItem(o);
				try {
					comboBox.setSelectedItem(fields.get(row).get(AbstractSettings.this));
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				comboBox.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
//						System.out.println(event);
//						System.out.println("boolean itemChanged " + comboBox.hashCode() + " " + comboBox.getSelectedItem());
						try {
							System.out.println(fields.get(row));
							fields.get(row).set(AbstractSettings.this, comboBox.getSelectedItem());
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}
				});
				return comboBox;
//				checkBox.setSelected((Boolean) value);
//				return checkBoxCellEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
//			} else if (value instanceof Enum) {
//				for (ActionListener l:comboBox.getActionListeners())
//					comboBox.removeActionListener(l);
//				comboBox.addActionListener(new ActionListener() {
//					public void actionPerformed(ActionEvent e) {
//						System.out.println("enum itemChanged " + this + " " + comboBox.getSelectedItem());
//						try {
//							fields.get(row).set(JPatchSettings2.this, comboBox.getSelectedItem());
//						} catch (IllegalArgumentException e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						} catch (IllegalAccessException e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
//					}
//				});
//				comboBox.removeAllItems();
//				for (Object o:((Enum) value).getDeclaringClass().getEnumConstants())
//					comboBox.addItem(o);
//				return comboBoxCellEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
			} else if (value instanceof Color) {
				Color color = JColorChooser.showDialog(table, "Choose a color", (Color) value);
				if (color == null)
					color = (Color) value;
				try {
					fields.get(row).set(AbstractSettings.this, color);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				JLabel label = (JLabel) tableCellRenderer.getTableCellRendererComponent(table, value, isSelected, true, row, column);
				JLabel newLabel = new JLabel();
				newLabel.setForeground(label.getForeground());
				newLabel.setBackground(label.getBackground());
				newLabel.setBorder(label.getBorder());
				newLabel.setText(" [" + color.getRed() + " " + color.getBlue() + " " + color.getGreen() + "]");
				newLabel.setIcon(new ColorIcon(color));
				newLabel.setOpaque(label.isOpaque());
//				label.setBackground(color);
//				label.setOpaque(true);
				table.repaint();
				return newLabel;
			} else if (value instanceof Color3f) {
				Color color = JColorChooser.showDialog(table, "Choose a color", ((Color3f) value).get());
				if (color == null)
					color = ((Color3f) value).get();
				try {
					fields.get(row).set(AbstractSettings.this, new Color3f(color));
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				JLabel label = (JLabel) tableCellRenderer.getTableCellRendererComponent(table, value, isSelected, true, row, column);
				JLabel newLabel = new JLabel();
				newLabel.setForeground(label.getForeground());
				newLabel.setBackground(label.getBackground());
				newLabel.setBorder(label.getBorder());
				newLabel.setText(" [" + color.getRed() + " " + color.getBlue() + " " + color.getGreen() + "]");
				newLabel.setIcon(new ColorIcon(color));
				newLabel.setOpaque(label.isOpaque());
//				label.setBackground(color);
//				label.setOpaque(true);
				table.repaint();
				return newLabel;
			} else if (value instanceof File) {
				File file = (File) value;
				JFileChooser fileChooser = new JFileChooser();
//				System.out.println(file + " " + file.isFile() + " " + file.isDirectory());
				if (file.isDirectory()) {
					fileChooser.setSelectedFile(file);
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				} else {
					fileChooser.setSelectedFile(file);
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				}
				if (fileChooser.showDialog(table, "Select") == JFileChooser.APPROVE_OPTION) {
					file = fileChooser.getSelectedFile();
					try {
						fields.get(row).set(AbstractSettings.this, file);
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				JLabel label = (JLabel) tableCellRenderer.getTableCellRendererComponent(table, value, isSelected, true, row, column);
				JLabel newLabel = new JLabel();
				newLabel.setForeground(label.getForeground());
				newLabel.setBackground(label.getBackground());
				newLabel.setBorder(label.getBorder());
				newLabel.setText(file.getAbsolutePath());
				newLabel.setFont(label.getFont());
				newLabel.setOpaque(label.isOpaque());
//				label.setBackground(color);
//				label.setOpaque(true);
				table.repaint();
				return newLabel;
//				return tableCellRenderer.getTableCellRendererComponent(table, value, isSelected, true, row, column);
//			} else if (value instanceof String) {
//				final JTextField textField = new JTextField();
//				try {
//					textField.setText((String) fields.get(row).get(JPatchSettings2.this));
//				} catch (IllegalAccessException e) {
//					e.printStackTrace();
//				}
//				class Updater {
//					void update(Component component) {
//						try {
//							System.out.println("update" + row);
//							fields.get(row).set(JPatchSettings2.this, textField.getText());
//							component.transferFocus();
//							table.repaint();
//						} catch (IllegalAccessException e) {
//							e.printStackTrace();
//						}
//					}
//				};
//				final Updater updater = new Updater();
//				
//				textField.addActionListener(new ActionListener() {
//					public void actionPerformed(ActionEvent event) {
//						System.out.println("actionPerformed");
//						updater.update((Component) event.getSource());
//					}
//				});
//				textField.addFocusListener(new FocusListener() {
//					public void focusGained(FocusEvent event) { }
//					
//					public void focusLost(FocusEvent event) {
//						System.out.println("focus");
//						updater.update((Component) event.getSource());
//					}
//				});
//				textField.addCaretListener(new CaretListener() {
//
//					public void caretUpdate(CaretEvent e) {
//						System.out.println("caret " + e);
//						
//					}
//					
//				});
//				
//				return textField;
//			}
			}
			return super.getTableCellEditorComponent(table, value, isSelected, row, column);
		}
	};
	
	public AbstractSettings() {
		table.setModel(tableModel);
		table.setShowGrid(false);
		table.setBackground(new JPanel().getBackground());
		table.setShowHorizontalLines(true);
		table.setGridColor(Color.WHITE);
		table.setShowVerticalLines(false);
		table.getColumnModel().getColumn(0).setHeaderValue("Preference Name");
//		table.getColumnModel().getColumn(1).setHeaderValue("Type");
		table.getColumnModel().getColumn(1).setHeaderValue("Value");
		table.setDefaultEditor(Object.class, tableCellEditor);
		table.setDefaultRenderer(Object.class, tableCellRenderer);
		tableCellEditor.setClickCountToStart(1);
	}
	
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	
	public void setParent(TreeNode parent) {
		this.parent = parent;
	}
	
	public List<Field> getFields() {
		return fields;
	}
	
	public TreeCellRenderer getTreeCellRenderer() {
		return treeCellRenderer;
	}
	
	public TableCellEditor getTableCellEditor() {
		return tableCellEditor;
	}
	
	public JTable getTable() {
		return table;
	}
	
	public void initTree() {
		try {
			for (Field field:getClass().getFields()) {
				if (AbstractSettings.class.isAssignableFrom(field.getType())) {
					AbstractSettings childNode = (AbstractSettings) field.get(this);
					childNode.initTree();
					childNode.setParent(this);
					childNode.setNodeName(field.getName());
					children.add(childNode);
				} else {
					fields.add(field);
				}
			}
//			Collections.sort(children, new Comparator<TreeNode>() {
//				public int compare(TreeNode o1, TreeNode o2) {
//					return o1.toString().compareTo(o2.toString());
//				}
//			});
//			Collections.sort(fields, new Comparator<Field>() {
//				public int compare(Field o1, Field o2) {
//					return o1.getName().compareTo(o2.getName());
//				}
//			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void dump(String prefix) {
		try {
			for (Field field:getClass().getFields()) {
				if (AbstractSettings.class.isAssignableFrom(field.getType()))
					((AbstractSettings) field.get(this)).dump(prefix + field.getName() + ".");
				else
					System.out.println(prefix + field.getName() + "\t" + field.getType() + "\t" + field.get(this) + isDefault(field));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void save() {
//		System.out.println("saving settings...");
		save(JPATCH_ROOT_NODE);
	}
	
	public void load() {
		load(JPATCH_ROOT_NODE);
	}
	
	void save(Preferences node) {
		try {
			for (Field field:getClass().getFields())
				writeField(node, field);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void load(Preferences node) {
//		System.out.println("load " + node);
		try {
			for (Field field:getClass().getFields())
				readField(node, field);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void storeDefaults() {
		try {
			for (Field field:getClass().getFields())
				mapDefaults.put(field.getName(), field.get(this));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	boolean isDefault(Field field) {
		try {
			return field.get(this).equals(mapDefaults.get(field.getName()));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	void readField(Preferences node, Field field) throws IllegalAccessException, IOException, ClassNotFoundException, InstantiationException {
		if (field.getType().equals(int.class))
			field.setInt(this, node.getInt(field.getName(), field.getInt(this)));
		else if (field.getType().equals(long.class))
			field.setLong(this, node.getLong(field.getName(), field.getLong(this)));
		else if (field.getType().equals(short.class))
			field.setShort(this, (short) node.getInt(field.getName(), field.getShort(this)));
		else if (field.getType().equals(byte.class))
			field.setByte(this, (byte) node.getInt(field.getName(), field.getByte(this)));
		else if (field.getType().equals(char.class))
			field.setChar(this, (char) node.getInt(field.getName(), field.getChar(this)));
		else if (field.getType().equals(float.class))
			field.setFloat(this, node.getFloat(field.getName(), field.getFloat(this)));
		else if (field.getType().equals(double.class))
			field.setDouble(this, node.getDouble(field.getName(), field.getDouble(this)));
		else if (field.getType().equals(boolean.class))
			field.setBoolean(this, node.getBoolean(field.getName(), field.getBoolean(this)));
		else if (field.getType().equals(String.class))
			field.set(this, node.get(field.getName(), (String) field.get(this)));
		else if (field.getType().equals(Color.class))
			field.set(this, new Color((node.getInt(field.getName(), ((Color3f) field.get(this)).get().getRGB()))));
		else if (field.getType().equals(Color3f.class))
			field.set(this, new Color3f(new Color((node.getInt(field.getName(), ((Color3f) field.get(this)).get().getRGB())))));
		else if (field.getType().isEnum())
			field.set(this, Enum.valueOf((Class<Enum>) field.getType(), node.get(field.getName(), field.get(this).toString())));
		else if (AbstractSettings.class.isAssignableFrom(field.getType())) {
			AbstractSettings child = (AbstractSettings) field.get(this);
//			AbstractSettings child = (AbstractSettings) field.getType().newInstance();
			child.load(node.node(field.getName()));
//			child.setNodeName(field.getName());
//			child.setParent(this);
//			field.set(this, child);
		} else if (field.getType().isArray())
			throw new IllegalArgumentException("Can't load arrays!");
		else {
			Object object = field.get(this);
			if (!(object instanceof Serializable))
				throw new IllegalArgumentException("Can't store non-serializable objects");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(field.get(this));
			oos.flush();
			baos.flush();
			ByteArrayInputStream bais = new ByteArrayInputStream(node.getByteArray(field.getName(), baos.toByteArray()));
			ObjectInputStream ois = new ObjectInputStream(bais);
			field.set(this, ois.readObject());
		}
	}
	
	void writeField(Preferences node, Field field) throws IllegalAccessException, IOException {
		if (field.getType().equals(int.class))
			node.putInt(field.getName(), field.getInt(this));
		else if (field.getType().equals(long.class))
			node.putLong(field.getName(), field.getLong(this));
		else if (field.getType().equals(short.class))
			node.putInt(field.getName(), field.getShort(this));
		else if (field.getType().equals(byte.class))
			node.putInt(field.getName(), field.getByte(this));
		else if (field.getType().equals(char.class))
			node.putInt(field.getName(), field.getChar(this));
		else if (field.getType().equals(float.class))
			node.putFloat(field.getName(), field.getFloat(this));
		else if (field.getType().equals(double.class))
			node.putDouble(field.getName(), field.getDouble(this));
		else if (field.getType().equals(boolean.class))
			node.putBoolean(field.getName(), field.getBoolean(this));
		else if (field.getType().equals(Color.class))
			node.putInt(field.getName(), ((Color) field.get(this)).getRGB());
		else if (field.getType().equals(Color3f.class))
			node.putInt(field.getName(), ((Color3f) field.get(this)).get().getRGB());
		else if (field.getType().equals(String.class))
			node.put(field.getName(), (String) field.get(this));
		else if (field.getType().isEnum())
			node.put(field.getName(), field.get(this).toString());
		else if (AbstractSettings.class.isAssignableFrom(field.getType()))
			((AbstractSettings) field.get(this)).save(node.node(field.getName()));
		else if (field.getType().isArray())
			throw new IllegalArgumentException("Can't store arrays!");
		else {
			Object object = field.get(this);
			if (!(object instanceof Serializable))
				throw new IllegalArgumentException("Can't store non-serializable objects");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(field.get(this));
			oos.flush();
			baos.flush();
			node.putByteArray(field.getName(), baos.toByteArray());
		}
	}
	
	public TableModel getTableModel() {
		return tableModel;
	}
	
	public Icon getIcon() {
		return icon;
	}
	
	/*
	 * TreeNode interface implementation
	 */
	
	public TreeNode getChildAt(int childIndex) {
		return children.get(childIndex);
	}

	public int getChildCount() {
		return children.size();
	}

	public TreeNode getParent() {
		return parent;
	}

	public int getIndex(TreeNode node) {
		return children.indexOf(node);
	}

	public boolean getAllowsChildren() {
		return true;
	}

	public boolean isLeaf() {
		return getChildCount() == 0;
	}

	public Enumeration children() {
		return Collections.enumeration(children);
	}
	
	public String toString() {
		return nodeName.replace('_', ' ');
	}

//	/*
//	 * TableModel interface implementation
//	 */
//	
//	public int getRowCount() {
//		return tableModel.getRowCount();
//	}
//
//	public int getColumnCount() {
//		return tableModel.getColumnCount();
//	}
//
//	public String getColumnName(int columnIndex) {
//		return tableModel.getColumnName(columnIndex);
//	}
//
//	public Class<?> getColumnClass(int columnIndex) {
//		return tableModel.getColumnClass(columnIndex);
//	}
//
//	public boolean isCellEditable(int rowIndex, int columnIndex) {
//		return tableModel.isCellEditable(rowIndex, columnIndex);
//	}
//
//	public Object getValueAt(int rowIndex, int columnIndex) {
//		return tableModel.getValueAt(rowIndex, columnIndex);
//	}
//
//	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
//		tableModel.setValueAt(aValue, rowIndex, columnIndex);		
//	}
//
//	public void addTableModelListener(TableModelListener l) {
//		tableModel.addTableModelListener(l);	
//	}
//
//	public void removeTableModelListener(TableModelListener l) {
//		tableModel.removeTableModelListener(l);
//	}
	
	public class ColorIcon implements Icon {
		private Color color;
		/* (non-Javadoc)
		 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
		 */
		
		public ColorIcon(Color color) {
			this.color = color;
		}
		
		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(color);
			g.fillRect(2, 2, 10, 10);
			g.setColor(Color.BLACK);
			g.drawRect(2, 2, 10, 10);
		}

		/* (non-Javadoc)
		 * @see javax.swing.Icon#getIconWidth()
		 */
		public int getIconWidth() {
			// TODO Auto-generated method stub
			return 10;
		}

		/* (non-Javadoc)
		 * @see javax.swing.Icon#getIconHeight()
		 */
		public int getIconHeight() {
			// TODO Auto-generated method stub
			return 10;
		}
		
	}
}
