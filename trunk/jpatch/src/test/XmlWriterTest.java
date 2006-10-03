package test;

import java.io.IOException;
import java.io.PrintWriter;

import jpatch.auxilary.XmlWriter;
import jpatch.entity.ControlPoint;
import jpatch.entity.Model;

public class XmlWriterTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		
		XmlWriter xmlWriter = new XmlWriter(new PrintWriter(System.out));
		Model model = new Model();
		
		ControlPoint cp0 = new ControlPoint(model);
		ControlPoint cp1 = new ControlPoint(model);
		ControlPoint cp2 = new ControlPoint(model);
		ControlPoint cp3 = new ControlPoint(model);
		cp0.position.set(-5, 0, 0);
		cp1.position.set( 0, 5, 0);
		cp2.position.set( 5, 0, 0);
		cp3.position.set( 0,-5, 0);
		cp0.setLoop(true);
		cp0.setNext(cp1);
		cp1.setNext(cp2);
		cp2.setNext(cp3);
		cp3.setNext(cp0);
		cp0.setPrev(cp3);
		cp1.setPrev(cp0);
		cp2.setPrev(cp1);
		cp3.setPrev(cp2);
		model.addCurve(cp0);
		model.initControlPoints();
		
		xmlWriter.startDocument();
		model.writeXml(xmlWriter);
		xmlWriter.endDocument();
	}

}
