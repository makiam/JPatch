package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;

import jpatch.entity.*;

public class NewCpTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Model model = new Model();
		ControlPoint cp1 = new ControlPoint(model);
		ControlPoint cp2 = new ControlPoint(model);
		ControlPoint cp3 = new ControlPoint(model);
		ControlPoint cp4 = new ControlPoint(model);
		cp1.setPrev(cp4);
		cp1.setNext(cp2);
		cp2.setPrev(cp1);
		cp2.setNext(cp3);
		cp3.setPrev(cp2);
		cp3.setNext(cp4);
		cp4.setPrev(cp3);
		cp4.setNext(cp1);
		cp1.setLoop(true);
		cp2.hookPos.set(0.5);
		
		ControlPoint cp5 = new ControlPoint(model);
		ControlPoint cp6 = new ControlPoint(model);
		ControlPoint cp7 = new ControlPoint(model);
		ControlPoint cp8 = new ControlPoint(model);
		cp5.setNext(cp6);
		cp6.setPrev(cp5);
		cp6.setNext(cp7);
		cp7.setPrev(cp6);
		cp7.setNext(cp8);
		cp8.setPrev(cp7);
		cp3.setNextAttached(cp6);
		cp6.setPrevAttached(cp3);
		
		model.addCurve(cp1);
		model.addCurve(cp5);
		
		cp5.reverseCurve();
		
		String filename = "modeltest.tmp";
		PrintStream output = new PrintStream(filename);
		model.xml(output, "");
		output.close();
		XmlLoader loader = new XmlLoader();
		loader.parse(new BufferedReader(new FileReader(filename)));
		loader.parse(new BufferedReader(new FileReader(filename)));
	}
}
