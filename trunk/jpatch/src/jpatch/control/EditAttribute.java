package jpatch.control;

import javax.vecmath.*;

import jpatch.control.edit2.JPatchUndoableEdit;
import com.jpatch.afw.attributes.*;
import com.jpatch.afw.control.AbstractUndoableEdit;

/**
 * This class provides factory methods to manipulate Attributes.
 * It can not be instantiated.
 * 
 * All factory methods that create new edits provide a boolean changeNow flag.
 * If set to true, the change on the attribute is performed immediately.
 * Setting changeNow to false makes sense if the change has already been
 * applied and the edit is used to make the change undoable.
 * 
 * @author sascha
 * @see jpatch.entity.ScalarAttribute
 */
public abstract class AttributeEdit extends AbstractUndoableEdit {
	
	private AttributeEdit() { } 	// private default constructor makes sure this class can not be instantiated.
	
	@Override
	public final void performUndo() {
		super.performUndo();
		swap();
	}
	
	@Override
	public final void performRedo() {
		super.performRedo();
		swap();
	}
	
	protected abstract void swap();
	
	/* * * * * * * * * * * * *
	 * static factory methods
	 * * * * * * * * * * * * */
	
	/**
	 * Facotory method that creates a new JPatchUndoableEdit which manipulates a double Attribute.
	 * @param attribute the Attribute to edit
	 * @param value the changed value
	 * @param changeNow set to true to apply the change now
	 * @return a new JPatchUndoableEdit object that encapsulates the specified modification
	 */
	public static JPatchUndoableEdit changeAttribute(DoubleAttr attr, double value, boolean changeNow) {
		return new Double(attr, value, changeNow);
	}
	
	/**
	 * Facotory method that creates a new JPatchUndoableEdit which manipulates an integer Attribute.
	 * @param attribute the Attribute to edit
	 * @param value the changed value
	 * @param changeNow set to true to apply the change now
	 * @return a new JPatchUndoableEdit object that encapsulates the specified modification
	 */
	public static JPatchUndoableEdit changeAttribute(IntAttr attr, int value, boolean changeNow) {
		return new Integer(attr, value, changeNow);
	}
	
	/**
	 * Facotory method that creates a new JPatchUndoableEdit which manipulates a boolean Attribute.
	 * @param attribute the Attribute to edit
	 * @param value the changed value
	 * @param changeNow set to true to apply the change now
	 * @return a new JPatchUndoableEdit object that encapsulates the specified modification
	 */
	public static JPatchUndoableEdit changeAttribute(BooleanAttr attr, boolean value, boolean changeNow) {
		return new Boolean(attr, value, changeNow);
	}
	
	/**
	 * Facotory method that creates a new JPatchUndoableEdit which manipulates a Tuple Attribute.
	 * @param attribute the Attribute to edit
	 * @param tuple the changed tuple
	 * @param changeNow set to true to apply the change now
	 * @return a new JPatchUndoableEdit object that encapsulates the specified modification
	 */
	public static JPatchUndoableEdit changeAttribute(Tuple2Attr attr, Tuple2d tuple, boolean changeNow) {
		return changeAttribute(attr, tuple.x, tuple.y, changeNow);
	}
	
	/**
	 * Facotory method that creates a new JPatchUndoableEdit which manipulates a Tuple Attribute.
	 * @param attribute the Attribute to edit
	 * @param x the changed x value
	 * @param y the changed y value
	 * @param changeNow set to true to apply the change now
	 * @return a new JPatchUndoableEdit object that encapsulates the specified modification
	 */
	public static JPatchUndoableEdit changeAttribute(Tuple2Attr attr, double x, double y, boolean changeNow) {
		return new Tuple2(attr, x, y, changeNow);
	}
	
	/**
	 * Facotory method that creates a new JPatchUndoableEdit which manipulates a Tuple Attribute.
	 * @param attribute the Attribute to edit
	 * @param tuple the changed tuple
	 * @param changeNow set to true to apply the change now
	 * @return a new JPatchUndoableEdit object that encapsulates the specified modification
	 */
	public static JPatchUndoableEdit changeAttribute(Tuple3Attr attr, Tuple3f tuple, boolean changeNow) {
		return changeAttribute(attr, tuple.x, tuple.y, tuple.z, changeNow);
	}
	
	/**
	 * Facotory method that creates a new JPatchUndoableEdit which manipulates a Tuple Attribute.
	 * @param attribute the Attribute to edit
	 * @param x the changed x value
	 * @param y the changed y value
	 * @param z the changed z value
	 * @param changeNow set to true to apply the change now
	 * @return a new JPatchUndoableEdit object that encapsulates the specified modification
	 */
	public static JPatchUndoableEdit changeAttribute(Tuple3Attr attr, double x, double y, double z, boolean changeNow) {
		return new Tuple3(attr, x, y, z, changeNow);
	}
	
	/**
	 * Facotory method that creates a new JPatchUndoableEdit which manipulates an Enum Attribute.
	 * @param attribute the Attribute to edit
	 * @param e the changed Enum
	 * @param changeNow set to true to apply the change now
	 * @return a new JPatchUndoableEdit object that encapsulates the specified modification
	 */
	public static JPatchUndoableEdit changeAttribute(GenericAttr attr, Object value, boolean changeNow) {
		return new Generic(attr, value, changeNow);
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * private inner classes that implement the required JPatchUndoableEdits
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	/*
	 * all of the following edits are "swapper" edits. They store a reference
	 * to the attribute to change and the changed value itself. On undo() or redo()
	 * they simply swap the current value (as stored in the attribute) with
	 * the value stored in the edit (this is actually implemented in
	 * SwapperEdit, which is the superclass of all of the following edits).
	 * 
	 * All these edits provide a boolean changeNow flag in their constructor.
	 * If set to true, the change on the attribute is performed immediately.
	 * Setting changeNow to false makes sense if the change has already been
	 * applied and the edit is used to make the change undoable.
	 */
	 
	/**
	 * JPatchUndoableEdit that modifies a double Attribute
	 */
	private static final class Double extends AbstractUndoableEdit {
		private final DoubleAttr attr;
		double value;
		
		private Double(DoubleAttr attr, double value, boolean changeNow) {
			this.attr = attr;
			this.value = value;
			if (changeNow) {
				swap();
				applied = true;
			}
		}
		
		@Override
		protected void swap() {
			double tmp = attr.getDouble();
			attr.setDouble(value);
			value = tmp;
		}
	}
	
	/**
	 * JPatchUndoableEdit that modifies an integer Attribute
	 */
	private static final class Integer extends SwapperEdit {
		private final IntAttr attr;
		private int value;
		
		private Integer(IntAttr attr, int value, boolean changeNow) {
			this.attr = attr;
			this.value = value;
			if (changeNow) {
				swap();
				applied = true;
			}
		}
		
		@Override
		protected void swap() {
			int tmp = attr.getInt();
			attr.setInt(value);
			value = tmp;
		}
	}
	
	/**
	 * JPatchUndoableEdit that modifies an integer Attribute
	 */
	private static final class Boolean extends SwapperEdit {
		private final BooleanAttr attr;
		private boolean value;
		
		private Boolean(BooleanAttr attr, boolean value, boolean changeNow) {
			this.attr = attr;
			this.value = value;
			if (changeNow) {
				swap();
				applied = true;
			}
		}
		
		@Override
		protected void swap() {
			boolean tmp = attr.getBoolean();
			attr.setBoolean(value);
			value = tmp;
		}
	}
	
	/**
	 * JPatchUndoableEdit that modifies a tuple Attribute
	 */
	private static final class Tuple2 extends SwapperEdit {
		private final Tuple2Attr attr;
		private final Tuple2d value = new Point2d();
		
		private Tuple2(Tuple2Attr attr, double x, double y, boolean changeNow) {
			this.attr = attr;
			this.value.set(x, y);
			if (changeNow) {
				swap();
				applied = true;
			}
		}
		
		@Override
		protected void swap() {
			double tmpX = attr.getX();
			double tmpY = attr.getY();
			attr.setTuple(value);
			value.set(tmpX, tmpY);
		}
	}
	
	/**
	 * JPatchUndoableEdit that modifies a tuple Attribute
	 */
	private static final class Tuple3 extends SwapperEdit {
		private final Tuple3Attr attr;
		private final Tuple3d value = new Point3d();
		
		private Tuple3(Tuple3Attr attr, double x, double y, double z, boolean changeNow) {
			this.attr = attr;
			this.value.set(x, y, z);
			if (changeNow) {
				swap();
				applied = true;
			}
		}
		
		@Override
		protected void swap() {
			double tmpX = attr.getX();
			double tmpY = attr.getY();
			double tmpZ = attr.getZ();
			attr.setTuple(value);
			value.set(tmpX, tmpY, tmpZ);
		}
	}
	
	/**
	 * JPatchUndoableEdit that modifies a generic Attribute
	 */
	private static final class Generic extends SwapperEdit {
		private final GenericAttr attr;
		private Object value;
		
		private Generic(GenericAttr attr, Object value, boolean changeNow) {
			this.attr = attr;
			this.value = value;
			if (changeNow) {
				swap();
				applied = true;
			}
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected void swap() {
			Object tmp = attr.getValue();
			attr.setValue(value);
			value = tmp;
		}
	}
}
