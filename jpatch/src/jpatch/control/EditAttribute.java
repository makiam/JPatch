package jpatch.control;

import javax.vecmath.Tuple3f;
import javax.vecmath.Tuple3d;

import jpatch.control.edit2.JPatchUndoableEdit;
import jpatch.entity.Attribute;

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
 * @see jpatch.entity.Attribute
 */
public final class EditAttribute {
	
	private EditAttribute() { } 	// private default constructor makes sure this class can not be instantiated.
	
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
	public static JPatchUndoableEdit changeAttribute(Attribute.Double attribute, double value, boolean changeNow) {
		return new Double(attribute, value, changeNow);
	}
	
	/**
	 * Facotory method that creates a new JPatchUndoableEdit which manipulates an integer Attribute.
	 * @param attribute the Attribute to edit
	 * @param value the changed value
	 * @param changeNow set to true to apply the change now
	 * @return a new JPatchUndoableEdit object that encapsulates the specified modification
	 */
	public static JPatchUndoableEdit changeAttribute(Attribute.Integer attribute, int value, boolean changeNow) {
		return new Integer(attribute, value, changeNow);
	}
	
	/**
	 * Facotory method that creates a new JPatchUndoableEdit which manipulates a Tuple Attribute.
	 * @param attribute the Attribute to edit
	 * @param tuple the changed tuple
	 * @param changeNow set to true to apply the change now
	 * @return a new JPatchUndoableEdit object that encapsulates the specified modification
	 */
	public static JPatchUndoableEdit changeAttribute(Attribute.Tuple3 attribute, Tuple3d tuple, boolean changeNow) {
		return changeAttribute(attribute, tuple.x, tuple.y, tuple.z, changeNow);
	}
	
	/**
	 * Facotory method that creates a new JPatchUndoableEdit which manipulates a Tuple Attribute.
	 * @param attribute the Attribute to edit
	 * @param tuple the changed tuple
	 * @param changeNow set to true to apply the change now
	 * @return a new JPatchUndoableEdit object that encapsulates the specified modification
	 */
	public static JPatchUndoableEdit changeAttribute(Attribute.Tuple3 attribute, Tuple3f tuple, boolean changeNow) {
		return changeAttribute(attribute, tuple.x, tuple.y, tuple.z, changeNow);
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
	public static JPatchUndoableEdit changeAttribute(Attribute.Tuple3 attribute, double x, double y, double z, boolean changeNow) {
		return new Tuple(attribute, x, y, z, changeNow);
	}
	
	/**
	 * Facotory method that creates a new JPatchUndoableEdit which manipulates an Enum Attribute.
	 * @param attribute the Attribute to edit
	 * @param e the changed Enum
	 * @param changeNow set to true to apply the change now
	 * @return a new JPatchUndoableEdit object that encapsulates the specified modification
	 */
	public static JPatchUndoableEdit changeAttribute(Attribute.Enum attribute, java.lang.Enum e, boolean changeNow) {
		return new Enum(attribute, e, changeNow);
	}
	
	/**
	 * Facotory method that creates a new JPatchUndoableEdit which manipulates a String Attribute.
	 * @param attribute the Attribute to edit
	 * @param string the changed string
	 * @param changeNow set to true to apply the change now
	 * @return a new JPatchUndoableEdit object that encapsulates the specified modification
	 */
	public static JPatchUndoableEdit changeAttribute(Attribute.String attribute, java.lang.String string, boolean changeNow) {
		return new String(attribute, string, changeNow);
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
	private static final class Double extends SwapperEdit {
		private final Attribute.Double attribute;
		double value;
		
		private Double(Attribute.Double attribute, double value, boolean changeNow) {
			this.attribute = attribute;
			this.value = value;
			if (changeNow) {
				swap();
				applied = true;
			}
		}
		
		@Override
		protected void swap() {
			double tmp = attribute.get();
			attribute.set(value);
			value = tmp;
		}
	}
	
	/**
	 * JPatchUndoableEdit that modifies an integer Attribute
	 */
	private static final class Integer extends SwapperEdit {
		private final Attribute.Integer attribute;
		int value;
		
		private Integer(Attribute.Integer attribute, int value, boolean changeNow) {
			this.attribute = attribute;
			this.value = value;
			if (changeNow) {
				swap();
				applied = true;
			}
		}
		
		@Override
		protected void swap() {
			int tmp = attribute.get();
			attribute.set(value);
			value = tmp;
		}
	}
	
	/**
	 * JPatchUndoableEdit that modifies a tuple Attribute
	 */
	private static final class Tuple extends SwapperEdit {
		private final Attribute.Tuple3 attribute;
		double x, y, z;
		
		private Tuple(Attribute.Tuple3 attribute, double x, double y, double z, boolean changeNow) {
			this.attribute = attribute;
			this.x = x;
			this.y = y;
			this.z = z;
			if (changeNow) {
				swap();
				applied = true;
			}
		}
		
		@Override
		protected void swap() {
			double tmpX = attribute.x.get();
			double tmpY = attribute.y.get();
			double tmpZ = attribute.z.get();
			attribute.set(x, y, z);
			x = tmpX;
			y = tmpY;
			z = tmpZ;
		}
	}
	
	/**
	 * JPatchUndoableEdit that modifies an enum Attribute
	 */
	private static final class Enum extends SwapperEdit {
		private final Attribute.Enum attribute;
		java.lang.Enum e;
		
		private Enum(Attribute.Enum attribute, java.lang.Enum e, boolean changeNow) {
			this.attribute = attribute;
			this.e = e;
			if (changeNow) {
				swap();
				applied = true;
			}
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected void swap() {
			java.lang.Enum tmp = attribute.get();
			attribute.set(e);
			e = tmp;
		}
	}
	
	/**
	 * JPatchUndoableEdit that modifies a string Attribute
	 */
	private static final class String extends SwapperEdit {
		private final Attribute.String attribute;
		java.lang.String string;
		
		private String(Attribute.String attribute, java.lang.String string, boolean changeNow) {
			this.attribute = attribute;
			this.string = string;
			if (changeNow) {
				swap();
				applied = true;
			}
		}
		
		@Override
		protected void swap() {
			java.lang.String tmp = attribute.get();
			attribute.set(string);
			string = tmp;
		}
	}
}
