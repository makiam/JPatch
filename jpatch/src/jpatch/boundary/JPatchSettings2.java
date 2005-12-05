/*
 * $Id$
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
package jpatch.boundary;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.prefs.*;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;
import javax.swing.tree.*;

/**
 * @author sascha
 *
 */
public class JPatchSettings2 implements TreeNode, TableModel {
	private Preferences userPrefs = Preferences.userRoot().node("/JPatch/Preferences");
	private Map mapDefaults = new HashMap();
	private List<Field> fields = new ArrayList<Field>();
	private List<TreeNode> children = new ArrayList<TreeNode>();
	private TreeNode parent = null;
	private String nodeName = "settings";
	private TableModel tableModel = new AbstractTableModel() {
		public int getRowCount() {
			return fields.size();
		}

		public int getColumnCount() {
			return 3;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return fields.get(rowIndex).getName().replace('_', ' ');
			case 1:
				return fields.get(rowIndex).getType().getSimpleName();
			case 2:
				try {
					Object o = fields.get(rowIndex).get(JPatchSettings2.this);
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
		
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 2;
		}
	};
	
	private TableCellRenderer tableCellRenderer = new DefaultTableCellRenderer() {
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (column < 3) {
				Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				if (!isDefault(fields.get(row)))
					component.setFont(component.getFont().deriveFont(Font.BOLD));
//				return new JLabel(value.getClass().getSimpleName() + " " + value);
				return component;
			} else {
				
				JCheckBox button = new JCheckBox("Reset");
				button.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						System.out.println("*");
						
					}
					
				});
				return button;
			}
		}
	};
	
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	
	public void setParent(TreeNode parent) {
		this.parent = parent;
	}
	
	public List<Field> getFields() {
		return fields;
	}
	
	public TableCellRenderer getTableCellRenderer() {
		return tableCellRenderer;
	}
	
	public void initTree() {
		try {
			for (Field field:getClass().getFields()) {
				if (JPatchSettings2.class.isAssignableFrom(field.getType())) {
					JPatchSettings2 childNode = (JPatchSettings2) field.get(this);
					childNode.initTree();
					children.add(childNode);
				} else {
					fields.add(field);
				}
			}
			Collections.sort(children, new Comparator<TreeNode>() {
				public int compare(TreeNode o1, TreeNode o2) {
					return o1.toString().compareTo(o2.toString());
				}
			});
			Collections.sort(fields, new Comparator<Field>() {
				public int compare(Field o1, Field o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void dump(String prefix) {
		try {
			for (Field field:getClass().getFields()) {
				if (JPatchSettings2.class.isAssignableFrom(field.getType()))
					((JPatchSettings2) field.get(this)).dump(prefix + field.getName() + ".");
				else
					System.out.println(prefix + field.getName() + "\t" + field.getType() + "\t" + field.get(this) + isDefault(field));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void save() {
		save("");
	}
	
	void save(String prefix) {
		try {
			for (Field field:getClass().getFields())
				writeField(prefix, field);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void load(String prefix) {
		try {
			for (Field field:getClass().getFields())
				readField(prefix, field);
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
	
	void readField(String prefix, Field field) throws IllegalAccessException, IOException, ClassNotFoundException, InstantiationException {
		if (field.getType().equals(int.class))
			field.setInt(this, userPrefs.getInt(prefix + field.getName(), field.getInt(this)));
		else if (field.getType().equals(long.class))
			field.setLong(this, userPrefs.getLong(prefix + field.getName(), field.getLong(this)));
		else if (field.getType().equals(short.class))
			field.setShort(this, (short) userPrefs.getInt(prefix + field.getName(), field.getShort(this)));
		else if (field.getType().equals(byte.class))
			field.setByte(this, (byte) userPrefs.getInt(prefix + field.getName(), field.getByte(this)));
		else if (field.getType().equals(char.class))
			field.setChar(this, (char) userPrefs.getInt(prefix + field.getName(), field.getChar(this)));
		else if (field.getType().equals(float.class))
			field.setFloat(this, userPrefs.getFloat(prefix + field.getName(), field.getFloat(this)));
		else if (field.getType().equals(double.class))
			field.setDouble(this, userPrefs.getDouble(prefix + field.getName(), field.getDouble(this)));
		else if (field.getType().equals(boolean.class))
			field.setBoolean(this, userPrefs.getBoolean(prefix + field.getName(), field.getBoolean(this)));
		else if (field.getType().equals(String.class))
			field.set(this, userPrefs.get(prefix + field.getName(), (String) field.get(this)));
		else if (field.getType().isEnum())
			field.set(this, Enum.valueOf((Class<Enum>) field.getType(), userPrefs.get(prefix + field.getName(), field.get(this).toString())));
		else if (JPatchSettings2.class.isAssignableFrom(field.getType())) {
			JPatchSettings2 child = (JPatchSettings2) field.getType().newInstance();
			child.load(prefix + field.getName() + ".");
			child.setNodeName(field.getName());
			child.setParent(this);
			field.set(this, child);
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
			ByteArrayInputStream bais = new ByteArrayInputStream(userPrefs.getByteArray(prefix + field.getName(), baos.toByteArray()));
			ObjectInputStream ois = new ObjectInputStream(bais);
			field.set(this, ois.readObject());
		}
	}
	
	void writeField(String prefix, Field field) throws IllegalAccessException, IOException {
		if (field.getType().equals(int.class))
			userPrefs.putInt(prefix + field.getName(), field.getInt(this));
		else if (field.getType().equals(long.class))
			userPrefs.putLong(prefix + field.getName(), field.getLong(this));
		else if (field.getType().equals(short.class))
			userPrefs.putInt(prefix + field.getName(), field.getShort(this));
		else if (field.getType().equals(byte.class))
			userPrefs.putInt(prefix + field.getName(), field.getByte(this));
		else if (field.getType().equals(char.class))
			userPrefs.putInt(prefix + field.getName(), field.getChar(this));
		else if (field.getType().equals(float.class))
			userPrefs.putFloat(prefix + field.getName(), field.getFloat(this));
		else if (field.getType().equals(double.class))
			userPrefs.putDouble(prefix + field.getName(), field.getDouble(this));
		else if (field.getType().equals(boolean.class))
			userPrefs.putBoolean(prefix + field.getName(), field.getBoolean(this));
		else if (field.getType().equals(String.class))
			userPrefs.put(prefix + field.getName(), (String) field.get(this));
		else if (field.getType().isEnum())
			userPrefs.put(prefix + field.getName(), field.get(this).toString());
		else if (JPatchSettings2.class.isAssignableFrom(field.getType()))
			((JPatchSettings2) field.get(this)).save(prefix + field.getName() + ".");
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
			userPrefs.putByteArray(prefix + field.getName(), baos.toByteArray());
		}
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
		return nodeName;
	}

	/*
	 * TableModel interface implementation
	 */
	
	public int getRowCount() {
		return tableModel.getRowCount();
	}

	public int getColumnCount() {
		return tableModel.getColumnCount();
	}

	public String getColumnName(int columnIndex) {
		return tableModel.getColumnName(columnIndex);
	}

	public Class<?> getColumnClass(int columnIndex) {
		return tableModel.getColumnClass(columnIndex);
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return tableModel.isCellEditable(rowIndex, columnIndex);
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return tableModel.getValueAt(rowIndex, columnIndex);
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		tableModel.setValueAt(aValue, rowIndex, columnIndex);		
	}

	public void addTableModelListener(TableModelListener l) {
		tableModel.addTableModelListener(l);	
	}

	public void removeTableModelListener(TableModelListener l) {
		tableModel.removeTableModelListener(l);
	}
}