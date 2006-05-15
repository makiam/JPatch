package jpatch.control.importer;

import java.io.*;
import javax.vecmath.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import jpatch.control.importer.*;
import jpatch.entity.*;
import jpatch.boundary.*;
import jpatch.boundary.settings.Settings;

public class AnimationImporter extends DefaultHandler {
	
	private static final int IDLE = 0;
	private static final int CHOREOGRAPHY = 1;
	private static final int MODEL = 2;
	private static final int FLOAT_CURVE = 3;
	private static final int POINT3D_CURVE = 4;
	private static final int COLOR3F_CURVE = 5;
	private static final int QUAT4F_CURVE = 6;
	private static final int CAMERA =7;
	private static final int LIGHT = 8;
	
	private int iState = IDLE, iPrevState = IDLE;
	private StringBuffer sbChars;
	
	private AnimObject animObject;
	private String animModelFilename;
	private MotionCurve motionCurve;
	private MotionCurveSet motionCurveSet;
	private String strRendererFormat;
	
	private Animation animation;
	
	public void loadAnimation(String filename) {
		MainFrame.getInstance().newAnimation();
		animation = MainFrame.getInstance().getAnimation();
		animation.removeCamera(animation.getCameras().get(0));
		XMLReader xmlReader = null;
		try {
			xmlReader = XMLReaderFactory.createXMLReader();
		} catch (SAXException e) {
			try {
				xmlReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
			} catch (SAXException ee) {
				ee.printStackTrace();
			}
		}
		try {
			xmlReader.setContentHandler(this);
			xmlReader.parse(new InputSource(filename));
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		File file = new File(filename);
		animation.setFile(file);
		MainFrame.getInstance().setFilename(file.getName());
	}
	
	public void startElement(String namespaceURI, String localName, String qName, Attributes attributes) {
		switch(iState) {
			case IDLE: {
				if (localName.equals("choreography")) {
					iState = CHOREOGRAPHY;
				}
			}
			break;
			case CHOREOGRAPHY: {
				if (
					localName.equals("name") ||
					localName.equals("start") ||
					localName.equals("end") ||
					localName.equals("framerate") ||
					localName.equals("prefix")
				) sbChars = new StringBuffer();
				else if (localName.equals("model")) {
					iState = MODEL;
					animObject = new AnimModel();
				}
				else if (localName.equals("camera")) {
					iState = CAMERA;
					animObject = new Camera("");
					motionCurveSet = new MotionCurveSet.Camera((Camera) animObject);
				}
				else if (localName.equals("lightsource")) {
					iState = LIGHT;
					animObject = new AnimLight();
					motionCurveSet = new MotionCurveSet.Light((AnimLight) animObject);
				}
				else if (localName.equals("renderer")) parseRenderer(attributes);
			}
			break;
			case MODEL: {
				if (
					localName.equals("name") ||
					localName.equals("filename") ||
					localName.equals("subdivisionoffset")
				) sbChars = new StringBuffer();
				else if (localName.equals("motioncurve")) {
					iPrevState = MODEL;
					prepareMotioncurve(attributes);
				}
				else if (localName.equals("renderer")) parseRenderer(attributes);
			}
			break;
			case CAMERA: {
				if (localName.equals("name")) sbChars = new StringBuffer();
				else if (localName.equals("motioncurve")) {
					iPrevState = CAMERA;
					prepareMotioncurve(attributes);
				}
			}
			break;
			case LIGHT: {
				if (localName.equals("name")) sbChars = new StringBuffer();
				else if (localName.equals("motioncurve")) {
					iPrevState = LIGHT;
					prepareMotioncurve(attributes);
				}
				else if (localName.equals("renderer")) parseRenderer(attributes);
				else if (localName.equals("inactive")) ((AnimLight) animObject).setActive(false);
			}
			break;
			case FLOAT_CURVE:
			case POINT3D_CURVE:
			case QUAT4F_CURVE:
			case COLOR3F_CURVE: {
				if (localName.equals("key")) parseKey(attributes);
			}
			break;
		}
	}
	
	public void endElement(String namespaceURI, String localName, String qName) {
		switch(iState) {
			case CHOREOGRAPHY: {
//				if (localName.equals("name")) animation.setName(sbChars.toString());
				if (localName.equals("start")) animation.setStart(Float.parseFloat(sbChars.toString()));
				else if (localName.equals("end")) animation.setEnd(Float.parseFloat(sbChars.toString()));
				else if (localName.equals("framerate")) animation.setFramerate(Float.parseFloat(sbChars.toString()));
//				else if (localName.equals("prefix")) animation.setPrefix(sbChars.toString());
				else if (localName.equals("sequence")) iState = IDLE;
				else if (localName.equals("renderer"))
					animation.setRenderString(strRendererFormat, "", sbChars.toString());
			}
			break;
			case MODEL: {
				if (localName.equals("name")) animObject.setName(sbChars.toString());
				else if (localName.equals("subdivisionoffset")) {
					((AnimModel) animObject).setSubdivisionOffset(Integer.parseInt(sbChars.toString()));
				}
				else if (localName.equals("filename")) {
					animModelFilename = sbChars.toString();
					Model model = new Model();
					(new JPatchImport()).importModel(model, Settings.getInstance().export.modelDirectory.getPath() + File.separatorChar + animModelFilename);
					((AnimModel) animObject).setModel(model);
					model.setAnimModel((AnimModel) animObject);
					((AnimModel) animObject).setFilename(animModelFilename);
//					motionCurveSet = new MotionCurveSet.Model((AnimModel) animObject);
					motionCurveSet = MotionCurveSet.createMotionCurveSetFor(animObject);
				}
				else if (localName.equals("model")) {
					iState = CHOREOGRAPHY;
//					motionCurveSet.populateList();
					animation.addModel((AnimModel) animObject, motionCurveSet);
					animObject = null;
				}
				else if (localName.equals("renderer"))
					((AnimModel) animObject).setRenderString(strRendererFormat, "", sbChars.toString());
			}
			break;
			case CAMERA: {
				if (localName.equals("name")) animObject.setName(sbChars.toString());
				else if (localName.equals("camera")) {
					iState = CHOREOGRAPHY;
					motionCurveSet.populateList();
					animation.addCamera((Camera) animObject, motionCurveSet);
//					animation.setMotionCurveSetFor(animObject, motionCurveSet);
					animObject = null;
				}
			}
			break;
			case LIGHT: {
				if (localName.equals("name")) animObject.setName(sbChars.toString());
				else if (localName.equals("lightsource")) {
					iState = CHOREOGRAPHY;
					motionCurveSet.populateList();
					animation.addLight((AnimLight) animObject, motionCurveSet);
					animObject = null;
				}
				else if (localName.equals("renderer"))
					((AnimLight) animObject).setRenderString(strRendererFormat, "", sbChars.toString());
			}
			break;
			case FLOAT_CURVE:
			case POINT3D_CURVE:
			case QUAT4F_CURVE:
			case COLOR3F_CURVE: {
				if (localName.equals("motioncurve")) iState = iPrevState;
			}
			break;
		}
	}

	public void characters(char[] ch, int start, int length) {
		if (sbChars != null) sbChars.append(ch, start, length);
	}
	
	private void prepareMotioncurve(Attributes attributes) {
		
		final int AVAR = 1;
		final int ORIENTATION = 2;
		final int SCALE = 3;
		int type = 0;
		
		for (int index = 0; index < attributes.getLength(); index++) {
			String localName = attributes.getLocalName(index);
			String value = attributes.getValue(index);
			if (localName.equals("type")) {
				if (value.equals("avar")) {
					type = AVAR;
				} else if (value.equals("position")) {
					motionCurve = MotionCurve.createPositionCurve();
					motionCurveSet.position = (MotionCurve.Point3d) motionCurve;
					iState = POINT3D_CURVE;
				} else if (value.equals("orientation")) {
					type = ORIENTATION;
				} else if (value.equals("scale")) {
					type = SCALE;
				} else if (value.equals("focallength")) {
					motionCurve = MotionCurve.createFocalLengthCurve();
					((MotionCurveSet.Camera) motionCurveSet).focalLength = (MotionCurve.Float) motionCurve;
					iState = FLOAT_CURVE;
				} else if (value.equals("size")) {
					motionCurve = MotionCurve.createSizeCurve();
					((MotionCurveSet.Light) motionCurveSet).size = (MotionCurve.Float) motionCurve;
					iState = FLOAT_CURVE;
				} else if (value.equals("intensity")) {
					motionCurve = MotionCurve.createIntensityCurve();
					((MotionCurveSet.Light) motionCurveSet).intensity = (MotionCurve.Float) motionCurve;
					iState = FLOAT_CURVE;
				} else if (value.equals("color")) {
					motionCurve = MotionCurve.createColorCurve();
					((MotionCurveSet.Light) motionCurveSet).color = (MotionCurve.Color3f) motionCurve;
					iState = COLOR3F_CURVE;
				}
			} else if (localName.equals("subtype")) {
				if (type == ORIENTATION) {
					if (value.equals("quaternion")) {
						motionCurve = MotionCurve.createOrientationCurve();
						motionCurveSet.orientation = (MotionCurve.Quat4f) motionCurve;
						iState = QUAT4F_CURVE;
					}
				} else if (type == SCALE) {
					if (value.equals("uniform")) {
						motionCurve = MotionCurve.createScaleCurve();
						((MotionCurveSet.Model) motionCurveSet).scale = (MotionCurve.Float) motionCurve;
						iState = FLOAT_CURVE;
					}
				}
			} else if (type == AVAR && localName.equals("id")) {
				//FIXME
				System.out.println("motionCurveSet = " + motionCurveSet);
				Morph morph = ((MotionCurveSet.Model) motionCurveSet).getMorphById(value);
				System.out.println("avar = " + morph);
				motionCurve = ((MotionCurveSet.Model) motionCurveSet).morph(morph);
				motionCurve.clear();
//				MorphTarget morph = (MorphTarget) ((AnimModel) animObject).getModel().getMorphList().get(Integer.parseInt(value));
//				motionCurve = MotionCurve2.createMorphCurve(morph);
//				((MotionCurveSet.Model) motionCurveSet).setMorphCurve(morph, (MotionCurve2.Float) motionCurve);
				iState = FLOAT_CURVE;
			} else if (localName.equals("interpolation")) {
				if (value.equals("linear")) motionCurve.setInterpolationMethod(MotionCurve.LINEAR);
				else if (value.equals("cubic")) motionCurve.setInterpolationMethod(MotionCurve.CUBIC);
			}
		}
	}
	
	private void parseRenderer(Attributes attributes) {
		for (int index = 0; index < attributes.getLength(); index++) {
			String localName = attributes.getLocalName(index);
			String value = attributes.getValue(index);
			if (localName.equals("format")) strRendererFormat = value;
		}
		sbChars = new StringBuffer();
	}
	
	private void parseKey(Attributes attributes) {
		float frame = 0;
		double x = 0, y = 0, z = 0, w = 0;
		for (int index = 0; index < attributes.getLength(); index++) {
			String localName = attributes.getLocalName(index);
			String value = attributes.getValue(index);
			if (localName.equals("frame")) frame = Float.parseFloat(value);
			else if (localName.equals("value") || localName.equals("x") || localName.equals("r")) x = Double.parseDouble(value);
			else if (localName.equals("y") || localName.equals("g")) y = Double.parseDouble(value);
			else if (localName.equals("z") || localName.equals("b")) z = Double.parseDouble(value);
			else if (localName.equals("w")) w = Double.parseDouble(value);
		}
		switch (iState) {
			case FLOAT_CURVE: {
				motionCurve.addKey(new MotionKey.Float(frame, (float) x));
			}
			break;
			
			case POINT3D_CURVE: {
				motionCurve.addKey(new MotionKey.Point3d(frame, new Point3d(x, y, z)));
			}
			break;
			
			case QUAT4F_CURVE: {
				motionCurve.addKey(new MotionKey.Quat4f(frame, new Quat4f((float) x, (float) y, (float) z, (float) w)));
			}
			break;
			
			case COLOR3F_CURVE: {
				motionCurve.addKey(new MotionKey.Color3f(frame, new Color3f((float) x, (float) y, (float) z)));
			}
			break;
			default: throw new IllegalStateException();
		}
	}
}

