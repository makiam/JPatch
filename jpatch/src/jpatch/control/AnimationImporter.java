package jpatch.control;

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
	private static final int SEQUENCE = 1;
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
	private MotionCurve2 motionCurve;
	private MotionCurveSet motionCurveSet;
	private String strRendererFormat;
	
	private Animator animation = Animator.getInstance();
	
	public void loadAnimation(String filename) {
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
	}
	
	public void startElement(String namespaceURI, String localName, String qName, Attributes attributes) {
		switch(iState) {
			case IDLE: {
				if (localName.equals("sequence")) {
					iState = SEQUENCE;
				}
			}
			break;
			case SEQUENCE: {
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
					animObject = animation.getActiveCamera();;
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
			case SEQUENCE: {
				if (localName.equals("name")) animation.setName(sbChars.toString());
				else if (localName.equals("start")) animation.setStart(Float.parseFloat(sbChars.toString()));
				else if (localName.equals("end")) animation.setEnd(Float.parseFloat(sbChars.toString()));
				else if (localName.equals("framerate")) animation.setFramerate(Float.parseFloat(sbChars.toString()));
				else if (localName.equals("prefix")) animation.setPrefix(sbChars.toString());
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
					motionCurveSet = new MotionCurveSet.Model((AnimModel) animObject);
				}
				else if (localName.equals("model")) {
					iState = SEQUENCE;
					motionCurveSet.populateList();
					animation.addObject(animObject, animModelFilename, motionCurveSet);
					animObject = null;
				}
				else if (localName.equals("renderer"))
					((AnimModel) animObject).setRenderString(strRendererFormat, "", sbChars.toString());
			}
			break;
			case CAMERA: {
				if (localName.equals("name")) animObject.setName(sbChars.toString());
				else if (localName.equals("camera")) {
					iState = SEQUENCE;
					motionCurveSet.populateList();
					animation.setMotionCurveSetFor(animObject, motionCurveSet);
					animObject = null;
				}
			}
			break;
			case LIGHT: {
				if (localName.equals("name")) animObject.setName(sbChars.toString());
				else if (localName.equals("lightsource")) {
					iState = SEQUENCE;
					motionCurveSet.populateList();
					animation.addObject(animObject, null, motionCurveSet);
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
		
		final int MORPH = 1;
		final int ORIENTATION = 2;
		final int SCALE = 3;
		int type = 0;
		
		for (int index = 0; index < attributes.getLength(); index++) {
			String localName = attributes.getLocalName(index);
			String value = attributes.getValue(index);
			if (localName.equals("type")) {
				if (value.equals("morph")) {
					type = MORPH;
				} else if (value.equals("position")) {
					motionCurve = MotionCurve2.createPositionCurve();
					motionCurveSet.position = (MotionCurve2.Point3d) motionCurve;
					iState = POINT3D_CURVE;
				} else if (value.equals("orientation")) {
					type = ORIENTATION;
				} else if (value.equals("scale")) {
					type = SCALE;
				} else if (value.equals("focallength")) {
					motionCurve = MotionCurve2.createFocalLengthCurve();
					((MotionCurveSet.Camera) motionCurveSet).focalLength = (MotionCurve2.Float) motionCurve;
					iState = FLOAT_CURVE;
				} else if (value.equals("size")) {
					motionCurve = MotionCurve2.createSizeCurve();
					((MotionCurveSet.Light) motionCurveSet).size = (MotionCurve2.Float) motionCurve;
					iState = FLOAT_CURVE;
				} else if (value.equals("intensity")) {
					motionCurve = MotionCurve2.createIntensityCurve();
					((MotionCurveSet.Light) motionCurveSet).intensity = (MotionCurve2.Float) motionCurve;
					iState = FLOAT_CURVE;
				} else if (value.equals("color")) {
					motionCurve = MotionCurve2.createColorCurve();
					((MotionCurveSet.Light) motionCurveSet).color = (MotionCurve2.Color3f) motionCurve;
					iState = COLOR3F_CURVE;
				}
			} else if (localName.equals("subtype")) {
				if (type == ORIENTATION) {
					if (value.equals("quaternion")) {
						motionCurve = MotionCurve2.createOrientationCurve();
						motionCurveSet.orientation = (MotionCurve2.Quat4f) motionCurve;
						iState = QUAT4F_CURVE;
					}
				} else if (type == SCALE) {
					if (value.equals("uniform")) {
						motionCurve = MotionCurve2.createScaleCurve();
						((MotionCurveSet.Model) motionCurveSet).scale = (MotionCurve2.Float) motionCurve;
						iState = FLOAT_CURVE;
					}
				}
			} else if (type == MORPH && localName.equals("morph")) {
				//FIXME
//				MorphTarget morph = (MorphTarget) ((AnimModel) animObject).getModel().getMorphList().get(Integer.parseInt(value));
//				motionCurve = MotionCurve2.createMorphCurve(morph);
//				((MotionCurveSet.Model) motionCurveSet).setMorphCurve(morph, (MotionCurve2.Float) motionCurve);
//				iState = FLOAT_CURVE;
			} else if (localName.equals("interpolation")) {
				if (value.equals("linear")) motionCurve.setInterpolationMethod(MotionCurve2.LINEAR);
				else if (value.equals("cubic")) motionCurve.setInterpolationMethod(MotionCurve2.CUBIC);
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
				motionCurve.addKey(new MotionKey2.Float(frame, (float) x));
			}
			break;
			
			case POINT3D_CURVE: {
				motionCurve.addKey(new MotionKey2.Point3d(frame, new Point3d(x, y, z)));
			}
			break;
			
			case QUAT4F_CURVE: {
				motionCurve.addKey(new MotionKey2.Quat4f(frame, new Quat4f((float) x, (float) y, (float) z, (float) w)));
			}
			break;
			
			case COLOR3F_CURVE: {
				motionCurve.addKey(new MotionKey2.Color3f(frame, new Color3f((float) x, (float) y, (float) z)));
			}
			break;
			default: throw new IllegalStateException();
		}
	}
}

