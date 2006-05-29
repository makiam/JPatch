package jpatch.control.importer;

import java.io.*;
import java.util.*;

import javax.vecmath.*;
import jpatch.boundary.*;
import jpatch.control.*;
import jpatch.control.edit.AtomicChangePatchMaterial;
import jpatch.entity.*;


public class AnimationMasterImport implements ModelImporter {
	
	private static final int UNDEF = 0;
	private static final int MESH = 1;
	private static final int PATCHES = 2;
	private static final int GROUP = 3;
	
	private HashMap mapHashCp = new HashMap();	// map to store Hash-controlpoints (so of course this is a HashMap ;-)
 	private HashMap mapControlPoint = new HashMap(); // map to store JPatch-controlpoints
	private ArrayList lstCandidateFivePointPatch = new ArrayList();
	private ArrayList lstGroup = new ArrayList();
	private ArrayList lstGroupName = new ArrayList();
	private HashMap mapGroupMaterials = new HashMap();
	
	private int iGroup = 0;
	// public methods
	
	public final String importModel(Model model, String filename) {
		ArrayList lstHashMesh = new ArrayList();	// list to store all splines
		BufferedReader brFile;
		String strLine;
		String[] astrPart;
		int iMode = UNDEF;
		try {
			brFile = new BufferedReader(new FileReader(filename));
			while ((strLine = brFile.readLine()) != null) {
				switch (iMode) {
					case UNDEF:
					if (strLine.equals("[MESH]")) {
						iMode = MESH;
						lstHashMesh = new ArrayList();
					}
					if (strLine.equals("[PATCHES]")) {
						iMode = PATCHES;
						//System.out.println("PATCHES");
					}
					if (strLine.equals("[GROUP]")) {
						iMode = GROUP;
						//model.addMaterial(new JPatchMaterial());
						iGroup++;
						//System.out.println("GROUP");
					}
					break;
					case MESH:
					if (strLine.equals("[ENDMESH]")) {
						iMode = UNDEF;
					}
					if (strLine.startsWith("Version=")) {
						astrPart = strLine.split("=");
						if (!astrPart[1].equals("2")) {
							System.err.println("unknown mesh version");
						}
					}
					if (strLine.startsWith("Splines=")) {								// the line should look like: Splines=number_of_splines
						astrPart = strLine.split("=");
						int iSplines = (new Integer(astrPart[1])).intValue();
						//System.out.println("Splines = " + iSplines);
						for (int s = 0; s < iSplines; s++) {
							//System.out.println("s = " + s);
							ArrayList lstSpline = new ArrayList();
							strLine = brFile.readLine();
							if (strLine.startsWith("CPs=")) {						// the line should look like: CPs=number_of_controlpoints
								astrPart = strLine.split("=");
								int iCPs = (new Integer(astrPart[1])).intValue();
								//System.out.println("CPs = " + iCPs);
								for (int c = 0; c < iCPs; c++) {
									//System.out.println("c = " + c);
									strLine = brFile.readLine();					// read first line of cp
																	// this line contains: flags welded num [hookPos]
									astrPart = strLine.split("\\s");
									long lFlags = (new Long(astrPart[0])).longValue();
									int iWeld = (new Integer(astrPart[1])).intValue();
									int iNum = (new Integer(astrPart[2])).intValue();
									float fHookPos = 0;
									if (HashCp.isHook(lFlags)) {
										fHookPos = (new Float(astrPart[3])).floatValue();
									}
									strLine = brFile.readLine();					// read second line of cp
																	// this line contains: {weldedto | x y z}
									int iWeldedTo = -1;
									Point3f p3Position = new Point3f(); 
									if (HashCp.isWeld(iWeld)) {
										iWeldedTo = (new Integer(strLine)).intValue();
									} else {
										astrPart = strLine.split("\\s");
										p3Position.x = (new Float(astrPart[0])).floatValue();
										p3Position.y = (new Float(astrPart[1])).floatValue();
										p3Position.z = -(new Float(astrPart[2])).floatValue();
									}
									strLine = brFile.readLine();					// read the third line of cp
																	// this line contains: {0 | aplha gamma magnitude}
									astrPart = strLine.split("\\s");
									float fInAlpha;
									float fInGamma = 0f;
									float fInMagnitude = 1f;
									fInAlpha = (new Float(astrPart[0])).floatValue();
									if (astrPart.length == 3) {
										fInGamma = (new Float(astrPart[1])).floatValue();
										fInMagnitude = (new Float(astrPart[2])).floatValue();
									}
									strLine = brFile.readLine();					// read the forth line of cp
																	// this line contains: {0 | aplha gamma magnitude}
									astrPart = strLine.split("\\s");
									float fOutAlpha;
									float fOutGamma = 0f;
									float fOutMagnitude = 1f;
									fOutAlpha = (new Float(astrPart[0])).floatValue();
									if (astrPart.length == 3) {
										fOutGamma = (new Float(astrPart[1])).floatValue();
										fOutMagnitude = (new Float(astrPart[2])).floatValue();
									}
									HashCp hashCp = new HashCp(lFlags,iWeld,iNum,fHookPos,p3Position,iWeldedTo,fInAlpha,fInGamma,fInMagnitude,fOutAlpha,fOutGamma,fOutMagnitude); // create a new HashCp object
									lstSpline.add(hashCp);						// add it to the spline
									Integer key = new Integer(iNum);
									mapHashCp.put(key,hashCp);					// add it to the map
									mapControlPoint.put(key,new ControlPoint(p3Position)); 		// add a new ControlPoint to the map
								}
							}
							lstHashMesh.add(lstSpline);
						}
					}
					break;
					case (PATCHES):
					if (strLine.equals("[ENDPATCHES]")) {
						iMode = UNDEF;
					}
					if (strLine.startsWith("Version=")) {
						astrPart = strLine.split("=");
						if (!astrPart[1].equals("3")) {
							System.err.println("unknown patches version!");
						}
					}
					//if (strLine.startsWith("Count=")) {								// the line should look like: Count=number_of_patches
					//	astrPart = strLine.split("=");
					//	int iPatches = (new Integer(astrPart[1])).intValue();
					//	for (int p = 0; p < iPatches; p++) {
							//strLine = brFile.readLine();
							astrPart = strLine.split("\\s");						// 5pp looks like cp cp cp cp cp ? ? ? ? ? 0
							if (astrPart.length == 11) {
								ControlPoint[] acp5pp = new ControlPoint[5];
								for (int cp = 0; cp < 5; cp++) {
									int hcp = (new Integer(astrPart[cp])).intValue();
									Integer key = new Integer(getHeadKey(hcp));
									acp5pp[cp] = (ControlPoint)mapControlPoint.get(key);
								}
								lstCandidateFivePointPatch.add(acp5pp);
							}
					//	}
					//}			
					break;
					case(GROUP):
					if (strLine.equals("[ENDGROUP]")) {
						iMode = UNDEF;
					}
					if (strLine.startsWith("Name=")) {
						astrPart = strLine.split("=");
						//model.getMaterial(iGroup).setName(astrPart[1]);
						lstGroupName.add(astrPart[1]);
						//System.out.println("group Name=" + astrPart[1]);
					}
					if (strLine.startsWith("DiffuseColor=")) {
						astrPart = strLine.split("=");
						String[] rgb = astrPart[1].split("\\s");
						//System.out.println("rgb " + rgb[0] + " " + rgb[1] + " " + rgb[2]);
						float b = (new Float(rgb[0])).floatValue();
						float g = (new Float(rgb[1])).floatValue();
						float r = (new Float(rgb[2])).floatValue();
						if (r > 1 || g > 1 || b > 1) {
							float R = r;
							float G = g;
							float B = b;
							r = B / 256;
							g = G / 256;
							b = R / 256;
						}
						JPatchMaterial material = new JPatchMaterial();
						material.setColor(new Color3f(r,g,b));
						mapGroupMaterials.put(new Integer(iGroup),material);
						model.addMaterial(material);
						//model.getMaterial(iGroup).setColor(new Color3f(r,g,b));
					}
					if (strLine.startsWith("Count=")) {								// the line should look like: Count=number_of_points
						astrPart = strLine.split("=");
						int iPoints = (new Integer(astrPart[1])).intValue();
						int[] aiPoints = new int[iPoints];
						for (int p = 0; p < iPoints; p++) {
							strLine = brFile.readLine();					
							int cp = (new Integer(strLine)).intValue();
							aiPoints[p] = cp;
						}
						lstGroup.add(aiPoints);
						//System.out.println("addGroup");
					}
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			return "error while importing Animation:Master model: " + e.getMessage();
		}
		
		//System.out.println("*");
		model.addCandidateFivePointPatchList(lstCandidateFivePointPatch);
		
		//System.out.println("lstHashMesh.size() = " + lstHashMesh.size());
		for (int s = 0; s < lstHashMesh.size(); s++) {
			//System.out.println("s = " + s);
			ArrayList lstSpline = (ArrayList)lstHashMesh.get(s);
			ControlPoint cpLast = null;
			for (int c = 0; c < lstSpline.size(); c++) {
				//System.out.println("c = " + c);
				HashCp hashCp = (HashCp)lstSpline.get(c);
				Integer key = new Integer(hashCp.getNum());
				ControlPoint cp = (ControlPoint)mapControlPoint.get(key);
				if (c == 0) {
					//System.out.println("addCurve");
					model.addCurve(cp);
				} else {
					cp.appendTo(cpLast);
				}
				if (hashCp.isWeld() && !hashCp.isHook()) {
					key = new Integer(getHeadKey(hashCp.getNum()));
					cp.attachTo((ControlPoint)mapControlPoint.get(key));
				}
				if (hashCp.isLoop()) {
					ControlPoint start = cp.getStart();
					cp.setNext(start);
					start.setPrev(cp);
					start.setLoop(true);
				}
				cpLast = cp;
			}
		}
		for (int s = 0; s < lstHashMesh.size(); s++) {
			ArrayList lstSpline = (ArrayList)lstHashMesh.get(s);
			for (int c = 0; c < lstSpline.size(); c++) {
				HashCp hashCp = (HashCp)lstSpline.get(c);
				Integer key = new Integer(hashCp.getNum());
				ControlPoint cp = (ControlPoint)mapControlPoint.get(key);
				cp.setInMagnitude(hashCp.getInMagnitude());
				cp.setOutMagnitude(hashCp.getOutMagnitude());
				if (hashCp.isHook()) {
					key = new Integer(getHookEndKey(hashCp.getNum()));
					ControlPoint hook = ((ControlPoint)mapControlPoint.get(key)).addHook(hashCp.getHookPos(), model);
					cp.attachTo(hook);
				}
				if (hashCp.isSmooth()) {
					cp.setMode(ControlPoint.AM_SMOOTH);
				} else {
					cp.setMode(ControlPoint.PEAK);
				}
			}
		}
		
		
		//for (int c = 0; c < iCount; c++) {
		//	if (aHashCp[c] != null) {									// set all the
		//	//if (isWeld(aHashCp[c].iWeld)) {								// attributes
		//	if (isWeld(aHashCp[c].iWeld) && !isHook(aHashCp[c].lFlags)) {	
		//		aControlPoint[c].attachTo(aControlPoint[getHead(aHashCp[c]).iNum]);
		//		System.out.println("attached" + aControlPoint[c] + " to " + aControlPoint[aHashCp[c].iWeldedTo]);
		//	}
		//	if (isHook(aHashCp[c].lFlags)) {
		//		ControlPoint hook = aControlPoint[getHookEnd(aHashCp[c]).iNum].addHook(aHashCp[c].fHookPos);
		//		//ControlPoint hook = aControlPoint[aHashCp[c].iNum].addHook(aHashCp[c].fHookPos);
		//		aControlPoint[c].attachTo(hook);
		//	}
		//	if (isLoop(aHashCp[c].lFlags)) {
		//		aControlPoint[c].closeLoop();
		//	}
		//	if (isSmooth(aHashCp[c].lFlags)) {
		//		aControlPoint[c].setMode(ControlPoint.AM_SMOOTH);
		//	} else {
		//		aControlPoint[c].setMode(ControlPoint.PEAK);
		//	}
		//	aControlPoint[c].setInCurvature(aHashCp[c].fInMagnitude / 3f);
		//	aControlPoint[c].setOutCurvature(aHashCp[c].fOutMagnitude / 3f);
		//	}
		//}
		
		model.computePatches();
		//System.out.println("iGroup = " + iGroup);
		//System.out.println("lstGroup.size() = " + lstGroup.size());
		//System.out.println("materials.size() = " + model.getMaterialList().size());
		for (int group = 0; group < lstGroup.size(); group++) {
			int[] aiPoints = (int[])lstGroup.get(group);
			ArrayList pointList = new ArrayList();
			
			
			//JPatchMaterial material = model.getMaterial(group + 1);
			
			for (int p = 0; p < aiPoints.length; p++) {
				Integer key = new Integer(aiPoints[p]);
				ControlPoint cp = (ControlPoint)mapControlPoint.get(key);
				//if (cp.isHead() || cp.isTargetHook()) {
				pointList.add(cp.getHead());
				//}
				//cp.getHead().addToGroup(group + 1);
			}
			Selection selection = new Selection(pointList);
			selection.setName((String) lstGroupName.get(group));
			model.addSelection(selection);
			JPatchMaterial material = (JPatchMaterial) mapGroupMaterials.get(new Integer(group + 1));
			if (material != null) {
				for (Iterator it = model.getPatchSet().iterator(); it.hasNext(); ) {
					Patch patch = (Patch) it.next();
					if (patch.isSelected(selection))
						patch.setMaterial(material);
				}
				material.setName(selection.getName());
			}
		}
		
		//MainFrame.getInstance().getJPatchScreen().update_all();
		return "";
	}
	
	// private methods
	
	private int getHeadKey(int key) {
		HashCp hcp = (HashCp)mapHashCp.get(new Integer(key));
		if (hcp.isWeld()) {
			return getHeadKey(hcp.getWeldedTo());
		}
		else return key;
	}
	
	private int getHookEndKey(int key) {
		HashCp hcp = (HashCp)mapHashCp.get(new Integer(key));
		if (hcp.isWeld() && hcp.isHook()) {
			return getHookEndKey(hcp.getWeldedTo());
		}
		else return key;
	}
}
