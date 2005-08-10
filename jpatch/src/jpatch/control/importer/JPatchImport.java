package jpatch.control.importer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import jpatch.boundary.Rotoscope;
import jpatch.boundary.ViewDefinition;
import jpatch.boundary.selection.PointSelection;
import jpatch.boundary.selection.Selection;
import jpatch.control.ModelImporter;
import jpatch.entity.ControlPoint;
import jpatch.entity.JPatchMaterial;
import jpatch.entity.MaterialProperties;
import jpatch.entity.Model;
import jpatch.entity.Morph;
import jpatch.entity.Patch;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class JPatchImport extends DefaultHandler
implements ModelImporter {
	private Model model;
	
	private static final int NULL = 0;
	private static final int MODEL = 1;
	private static final int MATERIAL = 2;
	private static final int MESH = 3;
	private static final int PATCH = 4;
	private static final int SELECTION = 5;
	private static final int ROTOSCOPE = 6;
	private static final int MORPH = 7;
	private static final int TARGET = 8;
	private static final int LIPSYNC = 9;
	
	private int iState = NULL;
	private ArrayList listCp = new ArrayList();
	private ArrayList listAttach = new ArrayList();
	private ArrayList listHook = new ArrayList();
	
	private boolean bCurveClosed;
	private ControlPoint cpFirst;
	private ControlPoint cp;
	private ControlPoint cpPrev;
	private Rotoscope rotoscope;
	private int iRotoscopeView;
	private JPatchMaterial material;
	private Selection selection;
	private Morph morph;
	private List listMaterials = new ArrayList();
	private ArrayList listCandidateFivePointPatch = new ArrayList();
		
	private CharReader charReader = new CharReader();
	
	private class CharReader {
		protected void string(String srt) {
		}
	}
	
	public final String importModel(Model model, String filename) {
		this.model = model;
		XMLReader xmlReader;
		try {
			xmlReader = XMLReaderFactory.createXMLReader();//"com.sun.org.apache.xerces.internal.parsers.SAXParser");
			xmlReader.setContentHandler(this);
			//xmlReader.setFeature("http://xml.org/sax/features/validation",true);
			xmlReader.parse(new InputSource(filename));
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		model.addCandidateFivePointPatchList(listCandidateFivePointPatch);
		model.computePatches();
		return "";
	}
	
	/**
	* start of XML ContentHander
	**/
	
	public void startElement(String namespaceURI, String localName, String qName, Attributes attributes) {
		switch(iState) {
			case NULL:
				if (localName.equals("model")) {
					iState = MODEL;
				}
				break;
			case MODEL:
				if (localName.equals("name")) {
					charReader = new ModelNameCharReader(model);
				} else if (localName.equals("material")) {
					iState = MATERIAL;
					material = createMaterial(attributes);
				} else if (localName.equals("mesh")) {
					iState = MESH;
				} else if (localName.equals("selection")) {
					iState = SELECTION;
					charReader = new ArrayCharReader();
					selection = createSelection(attributes);
				} else if (localName.equals("rotoscope")) {
					iState = ROTOSCOPE;
					rotoscope = createRotoscope(attributes);
				}	
				break;
			case MESH:
				if (localName.equals("curve")) {
					parseCurve(attributes);
					cpFirst = null;
				} else if (localName.equals("cp")) {
					cp = createCp(attributes);
					listCp.add(cp);
					if (cpFirst == null) {
						cpFirst = cp;
					}
				} else if (localName.equals("patch")) {
					iState = PATCH;
					material = getPatchMaterial(attributes);
				} else if (localName.equals("morph")) {
					iState = MORPH;
					morph = createMorph(attributes);
				} else if (localName.equals("lipsync")) {
					iState = LIPSYNC;
				}
				break;
			case PATCH:
				if (localName.equals("points")) {
					charReader = new ArrayCharReader();
				}
				break;
			case MATERIAL:
				if (localName.equals("name")) {
					charReader = new MaterialNameCharReader(material);
				} else if (localName.equals("color")) {
					parseMaterialColor(attributes,material.getMaterialProperties());
				} else if (localName.equals("finish")) {
					parseMaterialFinish(attributes,material.getMaterialProperties());
				} else if (localName.equals("reflection")) {
					parseMaterialReflection(attributes,material.getMaterialProperties());
				} else if (localName.equals("refraction")) {
					parseMaterialRefraction(attributes,material.getMaterialProperties());
				} else if (localName.equals("renderer")) {
					StringBuffer format = new StringBuffer();
					StringBuffer version = new StringBuffer();
					parseMaterialRenderer(attributes,format,version);
					charReader = new RenderStringCharReader(format.toString(),version.toString());
				}
				break;
			case ROTOSCOPE:
				if (localName.equals("image")) {
					charReader = new RotoscopeImageCharReader(rotoscope);
				} else if (localName.equals("display")) {
					parseRotoscopeDisplay(attributes,rotoscope);
				}
				break;
			case MORPH:
				if (localName.equals("target")) {
					iState = TARGET;
				}
				break;
			case TARGET:
				if (localName.equals("point")) {
					parseMorphVector(attributes,morph);
				}
				break;
			case LIPSYNC:
				if (localName.equals("map")) {
					parseLipsyncMap(attributes);
				}
				break;
		}
	}

	public void endElement(String namespaceURI, String localName, String qName) {
		//System.out.println("end " + iState + " " + qName);
		switch(iState) {
			case MODEL:
				if (localName.equals("model")) {
					//((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).reload();
					iState = NULL;
				} else if (localName.equals("name")) {
					charReader = new CharReader();
				}
				break;
			case MATERIAL:
				if (localName.equals("material")) {
					//System.out.println(material.getName());
					if (material.getName().equals("Default Material")) {
						List matList = model.getMaterialList();
						JPatchMaterial mat = null;
						loop:
						for (Iterator it = matList.iterator(); it.hasNext(); ) {
							mat = (JPatchMaterial) it.next();
							if (mat.getName().equals("Default Material")) {
								break loop;
							}
						}
						mat.getMaterialProperties().set(material.getMaterialProperties());
						mat.setRenderStrings(material.getRenderStrings());
						material = mat;
					} else {
						model.addMaterial(material);
					}
					listMaterials.add(material);
					iState = MODEL;
				} else if (localName.equals("name")) {
					charReader = new CharReader();
				} else if (localName.equals("renderer")) {
					String format = ((RenderStringCharReader)charReader).getFormat();
					String version = ((RenderStringCharReader)charReader).getVersion();
					String renderstring = ((RenderStringCharReader)charReader).getRenderString();
					//System.out.println("---");
					//System.out.println(renderstring);
					//System.out.println("---");
					material.setRenderString(format,version,renderstring);
					charReader = new CharReader();
				}
				break;
			case MESH:
				if (localName.equals("mesh")) {
					attach();
					hook();
					iState = MODEL;
				} else if (localName.equals("curve")) {
					if (bCurveClosed) {
						cpPrev.setNext(cpFirst);
						cpFirst.setPrev(cpPrev);
						cpFirst.setLoop(true);
					}
					//System.out.println("addCurve");
					model.addCurve(cpFirst);
					cpPrev = null;
				} else if (localName.equals("cp")) {
					if (cpPrev != null) {
						cpPrev.setNext(cp);
					}
					cp.setPrev(cpPrev);
					cpPrev = cp;
					cp = null;
				}
				break;
			case PATCH:
				if (localName.equals("points")) {
					int[] aiPoint = ((ArrayCharReader) charReader).getIntArray();
					ControlPoint[] acp = new ControlPoint[aiPoint.length];
					for (int i = 0; i < acp.length; i++) {
						acp[i] = (ControlPoint) listCp.get(aiPoint[i]);
					}
					Patch patch = new Patch(acp);
					patch.setMaterial(material);
					model.addPatch(patch,null);
					if (acp.length == 10) {		// five point patch
						ControlPoint[] acp5 = new ControlPoint[] {
							trueHead(acp[0]),
							trueHead(acp[2]),
							trueHead(acp[4]),
							trueHead(acp[6]),
							trueHead(acp[8])
							//acp[0].getHead(),
							//acp[2].getHead(),
							//acp[4].getHead(),
							//acp[6].getHead(),
							//acp[8].getHead()
						};
						listCandidateFivePointPatch.add(acp5);
					}
					charReader = new CharReader();
				} else if (localName.equals("patch")) {
					iState = MESH;
				}
				break;
			case SELECTION:
				if (localName.equals("selection")) {
					iState = MODEL;
					int[] aiPoint = ((ArrayCharReader) charReader).getIntArray();
					for (int i = 0; i < aiPoint.length; i++) {
						((PointSelection) selection).addControlPoint((ControlPoint) listCp.get(aiPoint[i]));
					}
					model.addSelection(selection);
				}
				break;
			case ROTOSCOPE:
				if (localName.equals("rotoscope")) {
					iState = MODEL;
					if (rotoscope.isValid()) {
						model.setRotoscope(iRotoscopeView,rotoscope);
					}
				} else if(localName.equals("image")) {
					charReader = new CharReader();
				}
				break;
			case MORPH:
				if (localName.equals("morph")) {
					model.addExpression(morph);
					iState = MESH;
				}
				break;
			case TARGET:
				if (localName.equals("target")) {
					iState = MORPH;
				}
				break;
			case LIPSYNC:
				if (localName.equals("lipsync")) {
					iState = MESH;
				}
				break;
		}
	}
	
	public void characters(char[] ch, int start, int length) {
		String string = new String(ch,start,length);
		//System.out.println("characters " + start + " " + length + " " + string);
		charReader.string(string);
	}
	
	/**
	* end of XML ContentHandler
	**/
	
	private Rotoscope createRotoscope(Attributes attributes) {
		Rotoscope rotoscope = new Rotoscope();
		for (int index = 0; index < attributes.getLength(); index++) {
			String localName = attributes.getLocalName(index);
			String value = attributes.getValue(index);
			iRotoscopeView = -1;
			if (localName.equals("view")) {
				if (value.equals("front")) iRotoscopeView = ViewDefinition.FRONT;
				if (value.equals("rear")) iRotoscopeView = ViewDefinition.REAR;
				if (value.equals("top")) iRotoscopeView = ViewDefinition.TOP;
				if (value.equals("bottom")) iRotoscopeView = ViewDefinition.BOTTOM;
				if (value.equals("left")) iRotoscopeView = ViewDefinition.LEFT;
				if (value.equals("right")) iRotoscopeView = ViewDefinition.RIGHT;
			}
		}
		return rotoscope;
	}
	
	private Morph createMorph(Attributes attributes) {
		//System.out.println("createMorph");
		Morph morph = new Morph(Morph.MORPH,"");
		for (int index = 0; index < attributes.getLength(); index++) {
			String localName = attributes.getLocalName(index);
			String value = attributes.getValue(index);
			if (localName.equals("name")) {
				morph.setName(value);
			} else if (localName.equals("min")) {
				morph.setMin((new Float(value)).floatValue());
			} else if (localName.equals("max")) {
				morph.setMax((new Float(value)).floatValue());
			} else if (localName.equals("value")) {
				morph.setValue((new Float(value)).floatValue());
			}
		}
		return morph;
	}
	
	private void parseLipsyncMap(Attributes attributes) {
		String phoneme = "";
		int morphIndex = -1;
		for (int index = 0; index < attributes.getLength(); index++) {
			String localName = attributes.getLocalName(index);
			String value = attributes.getValue(index);
			if (localName.equals("phoneme")) phoneme = value;
			else if (localName.equals("morph")) morphIndex = Integer.parseInt(value);
		}
		model.setMorphFor(phoneme, (Morph) model.getMorphList().get(morphIndex));
	}
	
	private void parseMorphVector(Attributes attributes, Morph morph) {
		ControlPoint cp = null;
		Vector3f vector = new Vector3f();
		for (int index = 0; index < attributes.getLength(); index++) {
			String localName = attributes.getLocalName(index);
			String value = attributes.getValue(index);
			if (localName.equals("nr")) {
				cp = (ControlPoint) listCp.get((new Integer(value)).intValue());
			} else if (localName.equals("x")) {
				vector.x = (new Float(value)).floatValue();
			} else if (localName.equals("y")) {
				vector.y = (new Float(value)).floatValue();
			} else if (localName.equals("z")) {
				vector.z = (new Float(value)).floatValue();
			}
		}
		//morph.dump();
		morph.add(cp, vector);
	}
	
	private Selection createSelection(Attributes attributes) {
		Selection selection = new PointSelection();
		for (int index = 0; index < attributes.getLength(); index++) {
			String localName = attributes.getLocalName(index);
			String value = attributes.getValue(index);
			if (localName.equals("name")) {
				selection.setName(value);
			}
		}
		return selection;
	}
				
	private JPatchMaterial getPatchMaterial(Attributes attributes) {
		for (int index = 0; index < attributes.getLength(); index++) {
			String localName = attributes.getLocalName(index);
			String value = attributes.getValue(index);
			if (localName.equals("material")) {
				int number = Integer.parseInt(value);
				if (number == -1) return null;
				return (JPatchMaterial) listMaterials.get(number);
			}
		}
		return null;
	}
				
	private JPatchMaterial createMaterial(Attributes attributes) {
		JPatchMaterial material = new JPatchMaterial();
		//iMaterialNumber = 0;
		//for (int index = 0; index < attributes.getLength(); index++) {
		//	String localName = attributes.getLocalName(index);
		//	String value = attributes.getValue(index);
		//	if (localName.equals("nr")) {
		//		iMaterialNumber = (new Integer(value)).intValue();
		//	}
		//}
		//return material;
		return material;
	}
	
	private ControlPoint createCp(Attributes attributes) {
		ControlPoint controlPoint = new ControlPoint();
		controlPoint.setMode(ControlPoint.JPATCH_G1);
		Point3f p3 = controlPoint.getPosition();
		for (int index = 0; index < attributes.getLength(); index++) {
			String localName = attributes.getLocalName(index);
			String value = attributes.getValue(index);
			if (localName.equals("x")) {
				p3.x = (new Float(value)).floatValue();
			} else if (localName.equals("y")) {
				p3.y = (new Float(value)).floatValue();
			} else if (localName.equals("z")) {
				p3.z = (new Float(value)).floatValue();
			} else if (localName.equals("attach")) {
				listAttach.add(controlPoint);
				listAttach.add(new Integer(value));
			} else if (localName.equals("hook")) {
				//if (fHookPos > -1) {
					listHook.add(controlPoint);
					listHook.add(new Integer(value));
				//	listHook.add(new Float(fHookPos));
				//	iHookTo = -1;
				//	fHookPos = -1;
				//} else {
				//	iHookTo = (new Integer(value)).intValue();
				//}
			} else if (localName.equals("hookpos")) {
				//if (iHookTo > -1) {
				//	listHook.add(controlPoint);
				//	listHook.add(new Integer(iHookTo));
				//	listHook.add(new Float(value));
				//	iHookTo = -1;
				//	fHookPos = -1;
				//} else {
				//	fHookPos = (new Float(value)).floatValue();
				//}
				controlPoint.setHookPos((new Float(value)).floatValue());
			} else if (localName.equals("magnitude")) {
				controlPoint.setMagnitude((new Float(value)).floatValue());
			//} else if (localName.equals("alpha")) {
			//	controlPoint.setAlpha((new Float(value)).floatValue());
			//} else if (localName.equals("gamma")) {
			//	controlPoint.setGamma((new Float(value)).floatValue());
			} else if (localName.equals("in_magnitude")) {
				controlPoint.setInMagnitude((new Float(value)).floatValue());
			//} else if (localName.equals("in_alpha")) {
			//	controlPoint.setInAlpha((new Float(value)).floatValue());
			//} else if (localName.equals("in_gamma")) {
			//	controlPoint.setInGamma((new Float(value)).floatValue());
			} else if (localName.equals("out_magnitude")) {
				controlPoint.setOutMagnitude((new Float(value)).floatValue());
			//} else if (localName.equals("out_alpha")) {
			//	controlPoint.setOutAlpha((new Float(value)).floatValue());
			//} else if (localName.equals("out_gamma")) {
			//	controlPoint.setOutGamma((new Float(value)).floatValue());
			} else if (localName.equals("mode")) {
				if (value.equals("peak")) {
					controlPoint.setMode(ControlPoint.PEAK);
				} else if (value.equals("spatch")) {
					controlPoint.setMode(ControlPoint.SPATCH_ROUND);
				}//else if (value.equals("smooth")) {
				//	controlPoint.setMode(ControlPoint.AM_SMOOTH);
				//} else if (value.equals("c0")) {
				//	controlPoint.setMode(ControlPoint.JPATCH_C0);
				//} else if (value.equals("g3")) {
				//	controlPoint.setMode(ControlPoint.JPATCH_G3);
				//} else if (value.equals("c1")) {
				//	controlPoint.setMode(ControlPoint.JPATCH_C1);
				//}
			}
		}
		controlPoint.setPosition(p3);
		return controlPoint;
	}
	
	private void parseRotoscopeDisplay(Attributes attributes, Rotoscope rotoscope) {
		float x,y;
		x = y = 0;
		for (int index = 0; index < attributes.getLength(); index++) {
			String localName = attributes.getLocalName(index);
			String value = attributes.getValue(index);
			if (localName.equals("x")) {
				x = (new Float(value)).floatValue();
			} else if (localName.equals("y")) {
				y = (new Float(value)).floatValue();
			} else if (localName.equals("scale")) {
				rotoscope.setScale((new Float(value)).floatValue());
			} else if (localName.equals("opacity")) {
				rotoscope.setOpacity((new Integer(value)).intValue());
			}
		}
		rotoscope.setPosition(x,y);
	}
			
	private void parseMaterialColor(Attributes attributes, MaterialProperties mp) {
		for (int index = 0; index < attributes.getLength(); index++) {
			String localName = attributes.getLocalName(index);
			String value = attributes.getValue(index);
			if (localName.equals("r")) {
				mp.red = (new Float(value)).floatValue();
			} else if (localName.equals("g")) {
				mp.green = (new Float(value)).floatValue();
			} else if (localName.equals("b")) {
				mp.blue = (new Float(value)).floatValue();
			} else if (localName.equals("filter")) {
				mp.filter = (new Float(value)).floatValue();
			} else if (localName.equals("transmit")) {
				mp.transmit = (new Float(value)).floatValue();
			}
		}
	}
	
	private void parseMaterialFinish(Attributes attributes, MaterialProperties mp) {
		for (int index = 0; index < attributes.getLength(); index++) {
			String localName = attributes.getLocalName(index);
			String value = attributes.getValue(index);
			if (localName.equals("ambient")) {
				mp.ambient = (new Float(value)).floatValue();
			} else if (localName.equals("diffuse")) {
				mp.diffuse = (new Float(value)).floatValue();
			} else if (localName.equals("brilliance")) {
				mp.brilliance = (new Float(value)).floatValue();
			} else if (localName.equals("specular")) {
				mp.specular = (new Float(value)).floatValue();
			} else if (localName.equals("roughness")) {
				mp.roughness = (new Float(value)).floatValue();
			} else if (localName.equals("metallic")) {
				mp.metallic = (new Float(value)).floatValue();
			}
		}
	}
	
	private void parseMaterialReflection(Attributes attributes, MaterialProperties mp) {
		for (int index = 0; index < attributes.getLength(); index++) {
			String localName = attributes.getLocalName(index);
			String value = attributes.getValue(index);
			if (localName.equals("amount")) {
				mp.reflectionMin = (new Float(value)).floatValue();
				mp.reflectionMax = mp.reflectionMin;
				mp.reflectionFalloff = 1;
			} else if (localName.equals("min")) {
				mp.reflectionMin = (new Float(value)).floatValue();
			} else if (localName.equals("max")) {
				mp.reflectionMax = (new Float(value)).floatValue();
			} else if (localName.equals("falloff")) {
				mp.reflectionFalloff = (new Float(value)).floatValue();
			} else if (localName.equals("conserve_energy")) {
				mp.conserveEnergy = (new Boolean(value)).booleanValue();
				//System.out.println(mp.conserveEnergy + " " + value);
			}
		}
	}
	
	private void parseMaterialRefraction(Attributes attributes, MaterialProperties mp) {
		for (int index = 0; index < attributes.getLength(); index++) {
			String localName = attributes.getLocalName(index);
			String value = attributes.getValue(index);
			if (localName.equals("index")) {
				mp.refraction = (new Float(value)).floatValue();
			}
		}
	}
	
	private void parseMaterialRenderer(Attributes attributes, StringBuffer format, StringBuffer version) {
		for (int index = 0; index < attributes.getLength(); index++) {
			String localName = attributes.getLocalName(index);
			String value = attributes.getValue(index);
			if (localName.equals("format")) {
				format.append(value);
				//System.out.println("format = " + format);
			} else if (localName.equals("version")) {
				version.append(value);
				//System.out.println("version = " + version);
			}
		}
	}
	
	private void parseCurve(Attributes attributes) {
		bCurveClosed = false;
		for (int index = 0; index < attributes.getLength(); index++) {
			String localName = attributes.getLocalName(index);
			String value = attributes.getValue(index);
			if (localName.equals("closed")) {
				bCurveClosed = (new Boolean(value)).booleanValue();
			}
		}
	}
	
	private void hook() {
		for (int i = 0; i < listHook.size();) {
			//((ControlPoint)listHook.get(i++)).hookTo((ControlPoint)listCp.get(((Integer)listHook.get(i++)).intValue()),((Float)listHook.get(i)).floatValue());
			ControlPoint cp = (ControlPoint) listHook.get(i++);
			ControlPoint parentHook = (ControlPoint) listCp.get(((Integer) listHook.get(i++)).intValue());
			cp.setParentHook(parentHook);
			if (cp.getHookPos() == 0) {
				parentHook.setChildHook(cp);
			}
		}
	}
	
	private void attach() {
		for (int i = 0; i < listAttach.size(); i++) {
			((ControlPoint)listAttach.get(i++)).attachTo((ControlPoint)listCp.get(((Integer)listAttach.get(i)).intValue()));
		}
	}
	
	private ControlPoint trueHead(ControlPoint cp) {
		return (cp.getParentHook() == null) ? cp.getHead() : cp.getParentHook().getHead();
	}
	
	class ArrayCharReader extends CharReader {
		private String str = "";
		
		protected void string(String str) {
			this.str += str;
		}
		
		protected String[] getStringArray() {
			return str.split(",");
		}
		
		protected int[] getIntArray() {
			String[] astr = str.split(",");
			int[] ai = new int[astr.length];
			for (int i = 0; i < astr.length; i++) {
				//System.out.println(astr[i]);
				ai[i] = (new Integer(astr[i])).intValue();
			}
			return ai;
		}
	}
		
	class ModelNameCharReader extends CharReader {
		private Model model;
		
		private ModelNameCharReader(Model model) {
			this.model = model;
		}
		
		protected void string(String str) {
			model.setName(str);
		}
	}
	
	class MaterialNameCharReader extends CharReader {
		private JPatchMaterial material;
		
		private MaterialNameCharReader(JPatchMaterial material) {
			this.material = material;
		}
		
		protected void string(String str) {
			material.setName(str);
		}
	}
	
	class RotoscopeImageCharReader extends CharReader {
		private Rotoscope rotoscope;
		
		private RotoscopeImageCharReader(Rotoscope rotoscope) {
			this.rotoscope = rotoscope;
		}
		
		protected void string(String str) {
			rotoscope.loadImageFromFile(str);
		}
	}
	
	class RenderStringCharReader extends CharReader {
		private StringBuffer stringBuffer = new StringBuffer();
		private String format;
		private String version;
		
		private RenderStringCharReader(String format, String version) {
			this.format = format;
			this.version = version;
			//System.out.println("renderer: " + format + " " + version);
		}
		
		protected void string(String str) {
			stringBuffer.append(str);
		}
		
		protected String getRenderString() {
			return stringBuffer.toString();
		}
		
		protected String getFormat() {
			return format;
		}
		
		protected String getVersion() {
			return version;
		}
	}
}
