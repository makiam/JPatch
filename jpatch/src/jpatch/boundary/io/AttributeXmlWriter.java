package jpatch.boundary.io;

import java.io.*;
import javax.vecmath.*;
import jpatch.auxilary.*;
import jpatch.entity.*;

public class AttributeXmlWriter {
	
	public void writeAttribute(PrintStream out, String prefix, ScalarAttribute attribute) {
		out.append(prefix).append("<attribute name=\"").append(attribute.getName()).append("\" ");
		writeAttributeTag(out, attribute);
		out.append(">");
		out.println();
		if (attribute.isKeyed()) {
			// TODO: append motioncurve data
		} else {
			out.append(prefix).append("\t<value>").append(getAttributeValue(attribute)).append("</value>");
			out.println();
		}
		if (attribute instanceof ScalarAttribute.BoundedDouble) {
			writeAttribute(out, prefix + "\t", ((ScalarAttribute.BoundedDouble) attribute).min);
			writeAttribute(out, prefix + "\t", ((ScalarAttribute.BoundedDouble) attribute).max);
		} else if (attribute instanceof ScalarAttribute.Tuple3Attr) {
			writeAttribute(out, prefix + "\t", ((ScalarAttribute.Tuple3Attr) attribute).x);
			writeAttribute(out, prefix + "\t", ((ScalarAttribute.Tuple3Attr) attribute).y);
			writeAttribute(out, prefix + "\t", ((ScalarAttribute.Tuple3Attr) attribute).z);
			if (attribute instanceof ScalarAttribute.Rotation) {
				writeAttribute(out, prefix + "\t", ((ScalarAttribute.Rotation) attribute).order);
			}
		}
		out.append(getAttributeValue(attribute)).append("</attribute>");
		out.println();
	}
	
	private void writeAttributeTag(PrintStream out, ScalarAttribute attribute) {
		throw new IllegalArgumentException();
	}
	
	private void writeAttributeTag(PrintStream out, ScalarAttribute.Boolean attribute) {
		out.append("type=\"boolean\"");
	}
	
	private void writeAttributeTag(PrintStream out, ScalarAttribute.BoundedDouble attribute) {
		out.append("type=\"bounded double\"");
	}
	
	private void writeAttributeTag(PrintStream out, ScalarAttribute.BoundedInteger attribute) {
		out.append("type=\"bounded integer\" ");
		out.append("min=\"").append(Integer.toString(attribute.getMin())).append("\" ");
		out.append("max=\"").append(Integer.toString(attribute.getMax())).append("\"");
	}
	
	private void writeAttributeTag(PrintStream out, ScalarAttribute.Double attribute) {
		out.append("type=\"double\"");
	}
	
	private void writeAttributeTag(PrintStream out, ScalarAttribute.Enum attribute) {
		out.append("type=\"enum\"");
	}
	
	private void writeAttributeTag(PrintStream out, ScalarAttribute.Integer attribute) {
		out.append("type=\"integer\"");
	}
	
	private void writeAttributeTag(PrintStream out, ScalarAttribute.KeyedBoolean attribute) {
		out.append("type=\"keyed boolean\"");
	}
	
	private void writeAttributeTag(PrintStream out, ScalarAttribute.Limit attribute) {
		out.append("type=\"limit\"");
	}
	
	private void writeAttributeTag(PrintStream out, ScalarAttribute.Rotation attribute) {
		out.append("type=\"rotation\"");
	}
	
	private void writeAttributeTag(PrintStream out, ScalarAttribute.String attribute) {
		if (attribute.isUseTextArea())
			out.append("type=\"text\"");
		else
			out.append("type=\"string\"");
	}
	
	private void writeAttributeTag(PrintStream out, ScalarAttribute.Tuple3Attr attribute) {
		if (attribute.getTupleClass().equals(javax.vecmath.Point3d.class))
			out.append("type=\"point\"");
		if (attribute.getTupleClass().equals(Vector3d.class))
			out.append("type=\"vector\"");
		if (attribute.getTupleClass().equals(Scale3d.class))
			out.append("type=\"scale\"");
	}
	
	@SuppressWarnings("unused")
	private String getAttributeValue(ScalarAttribute attribute) {
		throw new IllegalArgumentException();
	}
	
	@SuppressWarnings("unused")
	private String getAttributeValue(ScalarAttribute.Boolean attribute) {
		return Boolean.toString(attribute.get());
	}
	
	@SuppressWarnings("unused")
	private String getAttributeValue(ScalarAttribute.Double attribute) {
		return Double.toString(attribute.get());
	}
	
	@SuppressWarnings("unused")
	private String getAttributeValue(ScalarAttribute.Integer attribute) {
		return Integer.toString(attribute.get());
	}
	
	@SuppressWarnings("unused")
	private String getAttributeValue(ScalarAttribute.Enum attribute) {
		return attribute.get().name();
	}
}
