package com.jpatch.afw;

import java.awt.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public final class ReflectionTable {
	private static final TableCellRenderer CELL_RENDERER = new DefaultTableCellRenderer() {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (value == null) {
				setText("null");
			}
			setBackground(column == 4 ? Color.WHITE : Color.LIGHT_GRAY);
			ReflectionTableModel model = (ReflectionTableModel) table.getModel();
			setForeground(column < 4 || value == null || model.fields[row].getType().isPrimitive() ? Color.BLACK : Color.BLUE);
			return this;
		}
	};
	
	public static void setupTable(JTable table, Class type) {
		setupTable(table, type, null);
	}
	
	public static void setupTable(JTable table, Object object) {
		setupTable(table, object.getClass(), object);
	}
	
	public static void setupTable(final JTable table, Class type, Object object) {
		table.setModel(new ReflectionTableModel(type, object));
		table.setDefaultRenderer(Object.class, CELL_RENDERER);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				System.out.println(e);
				if (!e.getValueIsAdjusting() && e.getFirstIndex() >= 0) {
					ReflectionTableModel model = (ReflectionTableModel) table.getModel();
					try {
						Object o = model.fields[e.getFirstIndex()].get(model.object);
						table.setModel(new ReflectionTableModel(o.getClass(), o));
					} catch (IllegalAccessException e1) {
						throw new RuntimeException(e1);
					}
				}
			}
		});
		table.getColumnModel().getColumn(0).setPreferredWidth(100);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);
		table.getColumnModel().getColumn(2).setPreferredWidth(100);
		table.getColumnModel().getColumn(3).setPreferredWidth(100);
		table.getColumnModel().getColumn(4).setPreferredWidth(400);
		table.setPreferredSize(new Dimension(800, 400));
	}
	
	private static class ReflectionTableModel extends AbstractTableModel {
		private final Class type;
		private Object object;
		
		private final Field[] fields;
		
		public ReflectionTableModel(Class type, Object object) {
			this.type = type;
			
			final List<Field> allFields = new ArrayList<Field>();
			for (Class c = type; c != null; c = c.getSuperclass()) {
				List<Field> fieldList = new ArrayList<Field>();
				for (Field field : c.getDeclaredFields()) {
					if (!Modifier.isStatic(field.getModifiers()) && ! field.isSynthetic()) {
						field.setAccessible(true);
						fieldList.add(field);
					}
				}
				Collections.sort(fieldList, new Comparator<Field>() {
					public int compare(Field o1, Field o2) {
						return o2.getName().compareTo(o1.getName());
					}
				});
				allFields.addAll(fieldList);
			}
			Collections.reverse(allFields);
			fields = allFields.toArray(new Field[allFields.size()]);
			
			setObject(object);
		}
		
		public void setObject(Object object) {
			if (object != null && !type.isInstance(object)) {
				throw new IllegalArgumentException(object + " is not of type " + type);
			}
			this.object = object;
		}
		
		public int getColumnCount() {
			return 5;
		}
	
		public int getRowCount() {
			return fields.length;
		}
	
		public Object getValueAt(int rowIndex, int columnIndex) {
			Field field = fields[rowIndex];
			switch (columnIndex) {
			case 0:
				return field.getDeclaringClass().getSimpleName();
			case 1:
				return Modifier.toString(field.getModifiers());
			case 2:
				return field.getType().getSimpleName();
			case 3:
				return field.getName();
			case 4:
				try {
					Object value = field.get(object);
					if (value == null) return null;
					if (value instanceof Object[]) return Arrays.toString((Object[]) value);
					if (value instanceof byte[]) return Arrays.toString((byte[]) value);
					if (value instanceof short[]) return Arrays.toString((short[]) value);
					if (value instanceof char[]) return Arrays.toString((char[]) value);
					if (value instanceof int[]) return Arrays.toString((int[]) value);
					if (value instanceof long[]) return Arrays.toString((long[]) value);
					if (value instanceof boolean[]) return Arrays.toString((boolean[]) value);
					if (value instanceof float[]) return Arrays.toString((float[]) value);
					if (value instanceof double[]) return Arrays.toString((double[]) value);
					return value;
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			default:
				throw new AssertionError();
			}
		}

		@Override
		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return "declaring class";
			case 1:
				return "modifiers";
			case 2:
				return "type";
			case 3:
				return "name";
			case 4:
				return "value";
			default:
				throw new AssertionError();
			}
		}
	}
	
	public static void main(String[] agrs) {
		JFrame frame = new JFrame("Reflection Table");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JTable table = new JTable();
		
		HashMap map = new HashMap();
		map.put("a", 1);
		map.put("b", 2);
		map.put("c", 3);
		setupTable(table, table);
		frame.add(new JScrollPane(table));
		frame.setSize(800, 600);
		frame.setVisible(true);
	}
}
