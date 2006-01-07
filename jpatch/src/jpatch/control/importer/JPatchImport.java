package jpatch.control.importer;

import java.util.*;


import javax.vecmath.*;

import jpatch.auxilary.Utils3D;
import jpatch.boundary.*;
import jpatch.control.ModelImporter;
import jpatch.entity.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

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
	private static final int SKELETON = 10;
	private static final int BONE = 11;
	private static final int DOF = 12;
	private static final int DOF_TARGET = 13;
	
	private int iState = NULL;
	private ArrayList listCp = new ArrayList();
	private ArrayList listAttach = new ArrayList();
	private ArrayList listHook = new ArrayList();
	private HashMap mapBones = new HashMap();
	
	private boolean bCurveClosed;
	private ControlPoint cpFirst;
	private ControlPoint cp;
	private ControlPoint cpPrev;
	private Rotoscope rotoscope;
	private Bone bone;
	private RotationDof dof;
	private int iRotoscopeView;
	private JPatchMaterial material;
	private String selectionName;
	private Morph morph;
	private MorphTarget morphTarget;
	private List listMaterials = new ArrayList();
	private List listBones = new ArrayList();
	private Map mapCpParentBone = new HashMap();
	private Map mapBoneParents = new HashMap();
	private ArrayList listCandidateFivePointPatch = new ArrayList();
	private String strRendererFormat;
	private String strRendererVersion;
	
//	private CharReader charReader = new CharReader();
	private StringBuffer sbChars = new StringBuffer();
	private int[] aiList;
	private float[] afList;
//	private class CharReader {
//		protected void string(String srt) {
//		}
//	}
	
	public final String importModel(Model model, String filename) {
		this.model = model;
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
			//xmlReader.setFeature("http://xml.org/sax/features/validation",true);
			xmlReader.parse(new InputSource(filename));
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		for (Iterator it = mapBones.keySet().iterator(); it.hasNext(); ) {
			ControlPoint cp = (ControlPoint) it.next();
			int i = ((Integer) mapBones.get(cp)).intValue();
			Bone bone = (Bone) listBones.get(i);
			Point3f p = cp.getReferencePosition();
			Point3f p0 = bone.getReferenceStart();
			Point3f p1 = bone.getReferenceEnd();
			float l = p0.distance(p1);
			float posOnLine = Utils3D.positionOnLine(p0, p1, p);
			Point3f pBone = new Point3f();
			pBone.interpolate(p0, p1, posOnLine);
			float distToLine = pBone.distance(p) / l;
			cp.setBone(bone, posOnLine, distToLine, Boolean.TRUE.equals(mapCpParentBone.get(cp)));
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
//					charReader = new ModelNameCharReader(model);
					sbChars.setLength(0);
				} else if (localName.equals("material")) {
					iState = MATERIAL;
					material = createMaterial(attributes);
				} else if (localName.equals("mesh")) {
					iState = MESH;
				} else if (localName.equals("selection")) {
					iState = SELECTION;
//					charReader = new ArrayCharReader();
					sbChars.setLength(0);
					aiList = null;
					afList = null;
					createSelection(attributes);
				} else if (localName.equals("rotoscope")) {
					iState = ROTOSCOPE;
					rotoscope = createRotoscope(attributes);
				} else if (localName.equals("skeleton")) {
					iState = SKELETON;
				}
				break;
			case SKELETON:
				if (localName.equals("bone")) {
					bone = createBone(attributes);
					iState = BONE;
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
//					charReader = new ArrayCharReader();
					sbChars.setLength(0);
				}
				break;
			case MATERIAL:
				if (localName.equals("name")) {
//					charReader = new MaterialNameCharReader(material);
					sbChars.setLength(0);
				} else if (localName.equals("color")) {
					parseMaterialColor(attributes,material.getMaterialProperties());
				} else if (localName.equals("finish")) {
					parseMaterialFinish(attributes,material.getMaterialProperties());
				} else if (localName.equals("reflection")) {
					parseMaterialReflection(attributes,material.getMaterialProperties());
				} else if (localName.equals("refraction")) {
					parseMaterialRefraction(attributes,material.getMaterialProperties());
				} else if (localName.equals("renderer")) {
					parseMaterialRenderer(attributes, strRendererFormat, strRendererVersion);
//					charReader = new RenderStringCharReader(format.toString(),version.toString());
					sbChars.setLength(0);
				}
				break;
			case ROTOSCOPE:
				if (localName.equals("image")) {
//					charReader = new RotoscopeImageCharReader(rotoscope);
					sbChars.setLength(0);
				} else if (localName.equals("display")) {
					parseRotoscopeDisplay(attributes,rotoscope);
				}
				break;
			case MORPH:
				if (localName.equals("target")) {
					morphTarget = createMorphTarget(attributes);
					iState = TARGET;
				}
				break;
			case TARGET:
			case DOF_TARGET:
				if (localName.equals("point")) {
					parseMorphVector(attributes, morphTarget);
				}
				break;
			case LIPSYNC:
				if (localName.equals("map")) {
					parseLipsyncMap(attributes);
				}
				break;
			case BONE:
				if (localName.equals("parent")) {
					for (int index = 0; index < attributes.getLength(); index++) {
						if (attributes.getLocalName(index).equals("id"))
							mapBoneParents.put(bone, new Integer(attributes.getValue(index)));
					}
				} else if (localName.equals("start")) {
					bone.setStart(createPoint(attributes));
				} else if (localName.equals("end")) {
					bone.setEnd(createPoint(attributes));
				} else if (localName.equals("color")) {
					bone.setColor(createColor(attributes));
				} else if (localName.equals("influence")) {
					for (int index = 0; index < attributes.getLength(); index++) {
						if (attributes.getLocalName(index).equals("start"))
							bone.setStartInfluence(Float.parseFloat(attributes.getValue(index)));
						else if (attributes.getLocalName(index).equals("end"))
							bone.setEndInfluence(Float.parseFloat(attributes.getValue(index)));
					}
				} else if (localName.equals("dof")) {
					for (int index = 0; index < attributes.getLength(); index++) {
						String name = attributes.getLocalName(index);
						String value = attributes.getValue(index);
						if (name.equals("type")) {
							if (value.equals("yaw"))
								dof = new RotationDof(bone, RotationDof.ORTHO_1, model);
							else if (value.equals("pitch"))
								dof = new RotationDof(bone, RotationDof.ORTHO_2, model);
							else if (value.equals("roll"))
								dof = new RotationDof(bone, RotationDof.RADIAL, model);
						} else if (name.equals("assignment")) {
							if (value.equals("rigid"))
								dof.setMode(RotationDof.RIGID);
							else if (value.equals("soft"))
								dof.setMode(RotationDof.SOFT);
							else if (value.equals("smooth"))
								dof.setMode(RotationDof.SMOOTH);
						} else if (name.equals("min")) {
//							System.out.println("min");
							dof.setMin(Float.parseFloat(value));
						} else if (name.equals("max")) {
							dof.setMax(Float.parseFloat(value));
						} else if (name.equals("current")) {
							dof.setValue(Float.parseFloat(value));
						} else if (name.equals("flipped")) {
							dof.setFlipped(Boolean.parseBoolean(value));
						}
					}
					MainFrame.getInstance().getTreeModel().insertNodeInto(dof, bone, bone.getDofs().size());
					iState = DOF;
//					bone.insert(dof, bone.getChildCount());
				}
				break;
			case DOF:
//				if (localName.equals("axis")) {
////					dof.setAxis(new Vector3f(createPoint(attributes)));
//				} else if (localName.equals("angle")) {
//					for (int index = 0; index < attributes.getLength(); index++) {
//						if (attributes.getLocalName(index).equals("min"))
//							dof.setMin(Float.parseFloat(attributes.getValue(index)));
//						else if (attributes.getLocalName(index).equals("max"))
//							dof.setMax(Float.parseFloat(attributes.getValue(index)));
//						else if (attributes.getLocalName(index).equals("current"))
//							dof.setValue(Float.parseFloat(attributes.getValue(index)));
//					}
				if (localName.equals("target")) {
					morphTarget = createMorphTarget(attributes);
					iState = DOF_TARGET;
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
//					charReader = new CharReader();
					model.setName(sbChars.toString());
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
//					charReader = new CharReader();
					material.setName(sbChars.toString());
				} else if (localName.equals("renderer")) {
					material.setRenderString(strRendererFormat, strRendererVersion, sbChars.toString());
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
					int[] aiPoint = splitIntoIntArray(sbChars.toString());
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
				} else if (localName.equals("patch")) {
					iState = MESH;
				}
				break;
			case SELECTION:
				if (localName.equals("selection")) {
					iState = MODEL;
					Selection selection;
					if (aiList == null) {
						int[] aiPoint = splitIntoIntArray(sbChars.toString());
						HashSet pointSet = new HashSet();
						for (int i = 0; i < aiPoint.length; i++) {
							pointSet.add(listCp.get(aiPoint[i]));
						}
						selection = new Selection(pointSet);
					} else {
						HashMap pointMap = new HashMap();
						for (int i = 0; i < aiList.length; i++) {
							pointMap.put(listCp.get(aiList[i]), new Float(afList[i]));
						}
						selection = new Selection(pointMap);
					}
					selection.setName(selectionName);
					model.addSelection(selection);
				} else if (localName.equals("points")) {
					aiList = splitIntoIntArray(sbChars.toString());
					sbChars.setLength(0);
				} else if (localName.equals("pointweights")) {
					afList = splitIntoFloatArray(sbChars.toString());
					sbChars.setLength(0);
//					for (int i = 0; i < afList.length; System.out.println(afList[i++]));
				}
				break;
			case ROTOSCOPE:
				if (localName.equals("rotoscope")) {
					iState = MODEL;
					if (rotoscope.isValid()) {
						model.setRotoscope(iRotoscopeView,rotoscope);
					}
				} else if(localName.equals("image")) {
					rotoscope.loadImageFromFile(sbChars.toString());
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
					morph.addTarget(morphTarget);
					iState = MORPH;
				}
				break;
			case DOF_TARGET:
				if (localName.equals("target")) {
					dof.addTarget(morphTarget);
					iState = DOF;
				}
				break;
			case LIPSYNC:
				if (localName.equals("lipsync")) {
					iState = MESH;
				}
				break;
			case DOF:
				if (localName.equals("dof")) {
					iState = BONE;
					if (dof.getTargets().size() > 0)
						dof.addTarget(new MorphTarget(0));
				}
				break;
			case BONE:
				if (localName.equals("bone")) {
					listBones.add(bone);
					iState = SKELETON;
				}
				break;
			case SKELETON:
				if (localName.equals("skeleton")) {
					for (Iterator it = listBones.iterator(); it.hasNext(); ) {
						Bone bone = (Bone) it.next();
//						System.out.println(bone.getName());
						if (mapBoneParents.containsKey(bone)) {
							Bone parent = (Bone) listBones.get(((Integer) mapBoneParents.get(bone)).intValue());
//							parent.insert(bone, parent.getChildCount());
							bone.setParent(parent);
						} 
						model.addBone(bone);					
					}
					iState = MODEL;
				}
				break;
		}
	}
	
	public void characters(char[] ch, int start, int length) {
		sbChars.append(ch, start, length);
//		String string = new String(ch,start,length);
//		//System.out.println("characters " + start + " " + length + " " + string);
//		charReader.string(string);
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
		Morph morph = new Morph("", model);
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
		morph.addTarget(new MorphTarget(0));
		return morph;
	}
	
	private MorphTarget createMorphTarget(Attributes attributes) {
		MorphTarget morphTarget = new MorphTarget(0);
		for (int index = 0; index < attributes.getLength(); index++) {
			String localName = attributes.getLocalName(index);
			String value = attributes.getValue(index);
			if (localName.equals("value")) {
				morphTarget.setPosition(Float.parseFloat(value));
			}
		}
		return morphTarget;
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
		model.setMorphFor(phoneme, (MorphTarget) model.getMorphList().get(morphIndex));
	}
	
	private void parseMorphVector(Attributes attributes, MorphTarget morph) {
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
		morph.addPoint(cp, vector);
	}
	
	private void createSelection(Attributes attributes) {
//		NewSelection selection = new NewSelection();
//		for (int index = 0; index < attributes.getLength(); index++) {
//			String localName = attributes.getLocalName(index);
//			String value = attributes.getValue(index);
//			if (localName.equals("name")) {
//				selection.setName(value);
//			}
//		}
//		return selection;
		for (int index = 0; index < attributes.getLength(); index++) {
			String localName = attributes.getLocalName(index);
			String value = attributes.getValue(index);
			if (localName.equals("name")) {
				selectionName = value;
			}
		}
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
	
	private Bone createBone(Attributes attributes) {
		Bone bone = new Bone(model, new Point3f(), new Vector3f());
		for (int index = 0; index < attributes.getLength(); index++) {
			if (attributes.getLocalName(index).equals("name"))
				bone.setName(attributes.getValue(index));
		}
		return bone;
	}
	
	private Color3f createColor(Attributes attributes) {
		Color3f color = new Color3f();
		for (int index = 0; index < attributes.getLength(); index++) {
			String localName = attributes.getLocalName(index);
			if (localName.equals("r"))
				color.x = Float.parseFloat(attributes.getValue(index));
			else if (localName.equals("g"))
				color.y = Float.parseFloat(attributes.getValue(index));
			else if (localName.equals("b"))
				color.z = Float.parseFloat(attributes.getValue(index));
		}
		return color;
	}
	
	private Point3f createPoint(Attributes attributes) {
		Point3f point = new Point3f();
		for (int index = 0; index < attributes.getLength(); index++) {
			String localName = attributes.getLocalName(index);
			if (localName.equals("x"))
				point.x = Float.parseFloat(attributes.getValue(index));
			else if (localName.equals("y"))
				point.y = Float.parseFloat(attributes.getValue(index));
			else if (localName.equals("z"))
				point.z = Float.parseFloat(attributes.getValue(index));
		}
		return point;
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
			} else if (localName.equals("bone")) {
				mapBones.put(controlPoint, new Integer(value));
			} else if (localName.equals("parent")) {
				mapCpParentBone.put(controlPoint, new Boolean(value));
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
	
	private void parseMaterialRenderer(Attributes attributes, String format, String version) {
		for (int index = 0; index < attributes.getLength(); index++) {
			String localName = attributes.getLocalName(index);
			String value = attributes.getValue(index);
			if (localName.equals("format"))
				format = value;
			else if (localName.equals("version"))
				version = value;
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
	
//	private String[] splitIntoStringArray(String string) {
//		return string.trim().split("[\\s,]+");
//	}
	
	private int[] splitIntoIntArray(String string) {
//		System.out.println("\"" + string + "\"");
		String[] number = string.trim().split("[\\s,]+");
		int[] ai = new int[number.length];
		for (int i = 0; i < number.length; i++)
			ai[i] = Integer.parseInt(number[i]);
		return ai;
	}
	
	private float[] splitIntoFloatArray(String string) {
		String[] number = string.trim().split("[\\s,]+");
		float[] af = new float[number.length];
		for (int i = 0; i < number.length; i++)
			af[i] = Float.parseFloat(number[i]);
		return af;
	}
//	class ArrayCharReader extends CharReader {
//		private String str = "";
//		
//		protected void string(String str) {
//			this.str += str;
//		}
//		
//		protected String[] getStringArray() {
//			return str.split(",");
//		}
//		
//		protected int[] getIntArray() {
//			String[] astr = str.split(",");
//			int[] ai = new int[astr.length];
//			for (int i = 0; i < astr.length; i++) {
//				//System.out.println(astr[i]);
//				ai[i] = (new Integer(astr[i])).intValue();
//			}
//			return ai;
//		}
//	}
//		
//	class ModelNameCharReader extends CharReader {
//		private Model model;
//		
//		private ModelNameCharReader(Model model) {
//			this.model = model;
//		}
//		
//		protected void string(String str) {
//			model.setName(str);
//		}
//	}
//	
//	class MaterialNameCharReader extends CharReader {
//		private JPatchMaterial material;
//		
//		private MaterialNameCharReader(JPatchMaterial material) {
//			this.material = material;
//		}
//		
//		protected void string(String str) {
//			material.setName(str);
//		}
//	}
//	
//	class RotoscopeImageCharReader extends CharReader {
//		private Rotoscope rotoscope;
//		
//		private RotoscopeImageCharReader(Rotoscope rotoscope) {
//			this.rotoscope = rotoscope;
//		}
//		
//		protected void string(String str) {
//			rotoscope.loadImageFromFile(str);
//		}
//	}
//	
//	class RenderStringCharReader extends CharReader {
//		private StringBuffer stringBuffer = new StringBuffer();
//		private String format;
//		private String version;
//		
//		private RenderStringCharReader(String format, String version) {
//			this.format = format;
//			this.version = version;
//			//System.out.println("renderer: " + format + " " + version);
//		}
//		
//		protected void string(String str) {
//			stringBuffer.append(str);
//		}
//		
//		protected String getRenderString() {
//			return stringBuffer.toString();
//		}
//		
//		protected String getFormat() {
//			return format;
//		}
//		
//		protected String getVersion() {
//			return version;
//		}
//	}
}
