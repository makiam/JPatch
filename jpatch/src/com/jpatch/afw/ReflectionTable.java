package com.jpatch.afw;

import com.jpatch.*;
import com.jpatch.boundary.Main;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;


public final class ReflectionTable {
	private static final TableCellRenderer CELL_RENDERER = new DefaultTableCellRenderer() {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (value == null) {
				setText("null");
			}
			boolean selectable = (((ObjectTableModel) table.getModel()).isSelectable(column));
			setBackground(selectable ? Color.WHITE : Color.LIGHT_GRAY);
//			ReflectionTableModel model = (ReflectionTableModel) table.getModel();
//			setForeground(column < 4 || value == null || model.fields[row].getType().isPrimitive() ? Color.BLACK : Color.BLUE);
			return this;
		}
	};
	
	public static void setupTable(final JTable table, Object object, final BrowserListener browserListener) {
		setTableModel(table, object.getClass(), object);
		table.setDefaultRenderer(Object.class, CELL_RENDERER);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Point point = e.getPoint();
				int row = table.rowAtPoint(point);
				int column = table.columnAtPoint(point);
				if (((ObjectTableModel) table.getModel()).isSelectable(column)) {
					Object o = table.getModel().getValueAt(row, column);
					browserListener.goTo(o);
				}
			}
		});
//		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
//			public void valueChanged(ListSelectionEvent e) {
//				System.out.println(e);
//				if (!e.getValueIsAdjusting() && e.getFirstIndex() >= 0) {
//					e.
//					Object o = ((ObjectTableModel) table.getModel()).getObject(e.getFirstIndex());
//					if (o != null) {
//						setTableModel(table, o.getClass(), o);
//					}
//				}
//			}
//		});
	}
	
	private static void setTableModel(final JTable table, Class type, Object object) {
		if (type.isArray()) {
			table.setModel(new ArrayTableModel(object));
		} else if (object instanceof Collection) {
			table.setModel(new ArrayTableModel(((Collection) object).toArray()));
		} else if (object instanceof Map) {
			table.setModel(new MapTableModel((Map) object));
		} else {
			table.setModel(new ReflectionTableModel(type, object));
		}
	}
	
	private static abstract class ObjectTableModel extends AbstractTableModel {
		public abstract boolean isSelectable(int columnIndex);
	}
	
	private static class ReflectionTableModel extends ObjectTableModel {
		private final Class type;
		private Object object;
		
		private final Field[] fields;
		
		public ReflectionTableModel(Class type, Object object) {
			this.type = type;
			
			final List<Field> allFields = new ArrayList<>();
			for (Class c = type; c != null; c = c.getSuperclass()) {
				List<Field> fieldList = new ArrayList<>();
				for (Field field : c.getDeclaredFields()) {
					if (!Modifier.isStatic(field.getModifiers()) && ! field.isSynthetic()) {
						field.setAccessible(true);
						fieldList.add(field);
					}
				}
				Collections.sort(fieldList, (Field o1, Field o2) -> o2.getName().compareTo(o1.getName()));
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
		
                @Override
		public int getColumnCount() {
			return 2;
		}
	
                @Override
		public int getRowCount() {
			return fields.length;
		}
	
                @Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Field field = fields[rowIndex];
			switch (columnIndex) {
//			case 0:
//				return field.getDeclaringClass().getSimpleName();
//			case 1:
//				return Modifier.toString(field.getModifiers());
//			case 2:
//				return field.getType().getSimpleName();
			case 0:
				return field.getName();
			case 1:
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
//			case 0:
//				return "declaring class";
//			case 1:
//				return "modifiers";
//			case 2:
//				return "type";
			case 0:
				return "name";
			case 1:
				return "value";
			default:
				throw new AssertionError();
			}
		}

		@Override
		public boolean isSelectable(int columnIndex) {
			return columnIndex == 1;
		}
	}
	
	private static class ArrayTableModel extends ObjectTableModel {
		private final Object array;
		
		public ArrayTableModel(Object array) {
			this.array = array;
		}
		
                @Override
		public int getColumnCount() {
			return 1;
		}
	
                @Override
		public int getRowCount() {
			return Array.getLength(array);
		}
	
                @Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return Array.get(array, rowIndex);
			default:
				throw new AssertionError();
			}
		}

		@Override
		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return "value";
			default:
				throw new AssertionError();
			}
		}

		@Override
		public boolean isSelectable(int columnIndex) {
			return true;
		}
	}
	
	private static class MapTableModel extends ObjectTableModel {
		private final Map.Entry<?, ?>[] entries;
		
		public MapTableModel(Map<?, ?> map) {
			entries = (Map.Entry<?, ?>[]) new ArrayList<Map.Entry>(map.entrySet()).toArray(new Map.Entry[map.size()]);
		}
		
                @Override
		public int getColumnCount() {
			return 2;
		}
	
                @Override
		public int getRowCount() {
			return entries.length;
		}
	
                @Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return entries[rowIndex].getKey();
			case 1:
				return entries[rowIndex].getValue();
			default:
				throw new AssertionError();
			}
		}

		@Override
		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return "key";
			case 1:
				return "value";
			default:
				throw new AssertionError();
			}
		}

		@Override
		public boolean isSelectable(int columnIndex) {
			return true;
		}
	}
	
	private static interface BrowserListener {
		public void goTo(Object object);
	}
	
	public static void main(String[] agrs) {
		JFrame frame = new JFrame("Reflection Table");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JTable table = new JTable();
		
		Map map = new HashMap();
		map.put("a", 1);
		map.put("b", 2);
		map.put("c", 3);
		
		List list = new ArrayList();
		list.add("test");
		list.add(27);
		list.add(null);
		list.add(new Object());
		
		try {
			Launcher.main(null);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		setupTable(table, Main.getInstance(), null);
		frame.add(new JScrollPane(table));
		frame.setSize(800, 600);
		frame.setVisible(true);
	}
}
