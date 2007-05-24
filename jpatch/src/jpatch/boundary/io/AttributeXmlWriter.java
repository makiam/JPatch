package jpatch.boundary.io;

import java.io.*;
import javax.vecmath.*;
import jpatch.auxilary.*;
import jpatch.entity.*;

public class AttributeXmlWriter {
	
	public void writeAttribute(PrintStream out, String prefix, Attribute attribute) {
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
		if (attribute instanceof Attribute.BoundedDouble) {
			writeAttribute(out, prefix + "\t", ((Attribute.BoundedDouble) attribute).min);
			writeAttribute(out, prefix + "\t", ((Attribute.BoundedDouble) attribute).max);
		} else if (attribute instanceof Attribute.Tuple3Attr) {
			writeAttribute(out, prefix + "\t", ((Attribute.Tuple3Attr) attribute).x);
			writeAttribute(out, prefix + "\t", ((Attribute.Tuple3Attr) attribute).y);
			writeAttribute(out, prefix + "\t", ((Attribute.Tuple3Attr) attribute).z);
			if (attribute instanceof Attribute.Rotation) {
				writeAttribute(out, prefix + "\t", ((Attribute.Rotation) attribute).order);
			}
		}
		out.append(getAttributeValue(attribute)).append("</attribute>");
		out.println();
	}
	
	private void writeAttributeTag(PrintStream out, Attribute attribute) {
		throw new IllegalArgumentException();
	}
	
	private void writeAttributeTag(PrintStream out, Attribute.Boolean attribute) {
		out.append("type=\"boolean\"");
	}
	
	private void writeAttributeTag(PrintStream out, Attribute.BoundedDouble attribute) {
		out.append("type=\"bounded double\"");
	}
	
	private void writeAttributeTag(PrintStream out, Attribute.BoundedInteger attribute) {
		out.append("type=\"bounded integer\" ");
		out.append("min=\"").append(Integer.toString(attribute.getMin())).append("\" ");
		out.append("max=\"").append(Integer.toString(attribute.getMax())).append("\"");
	}
	
	private void writeAttributeTag(PrintStream out, Attribute.Double attribute) {
		out.append("type=\"double\"");
	}
	
	private void writeAttributeTag(PrintStream out, Attribute.Enum attribute) {
		out.append("type=\"enum\"");
	}
	
	private void writeAttributeTag(PrintStream out, Attribute.Integer attribute) {
		out.append("type=\"integer\"");
	}
	
	private void writeAttributeTag(PrintStream out, Attribute.KeyedBoolean attribute) {
		out.append("type=\"keyed boolean\"");
	}
	
	private void writeAttributeTag(PrintStream out, Attribute.Limit attribute) {
		out.append("type=\"limit\"");
	}
	
	private void writeAttributeTag(PrintStream out, Attribute.Rotation attribute) {
		out.append("type=\"rotation\"");
	}
	
	private void writeAttributeTag(PrintStream out, Attribute.String attribute) {
		if (attribute.isUseTextArea())
			out.append("type=\"text\"");
		else
			out.append("type=\"string\"");
	}
	
	private void writeAttributeTag(PrintStream out, Attribute.Tuple3Attr attribute) {
		if (attribute.getTupleClass().equals(javax.vecmath.Point3d.class))
			out.append("type=\"point\"");
		if (attribute.getTupleClass().equals(Vector3d.class))
			out.append("type=\"vector\"");
		if (attribute.getTupleClass().equals(Scale3d.class))
			out.append("type=\"scale\"");
	}
	
	@SuppressWarnings("unused")
	private String getAttributeValue(Attribute attribute) {
		throw new IllegalArgumentException();
	}
	
	@SuppressWarnings("unused")
	private String getAttributeValue(Attribute.Boolean attribute) {
		return Boolean.toString(attribute.get());
	}
	
	@SuppressWarnings("unused")
	private String getAttributeValue(Attribute.Double attribute) {
		return Double.toString(attribute.get());
	}
	
	@SuppressWarnings("unused")
	private String getAttributeValue(Attribute.Integer attribute) {
		return Integer.toString(attribute.get());
	}
	
	@SuppressWarnings("unused")
	private String getAttributeValue(Attribute.Enum attribute) {
		return attribute.get().name();
	}
}
