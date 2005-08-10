package jpatch.control.importer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

import jpatch.boundary.selection.PointSelection;
import jpatch.control.ModelImporter;
import jpatch.entity.ControlPoint;
import jpatch.entity.JPatchMaterial;
import jpatch.entity.Model;

public class SPatchImport implements ModelImporter {
	private static final int MAX_LAYERS = 8;
	
	public final String importModel(Model model, String filename) {
		BufferedReader brFile;
		String strLine;
		String[] astrPart;
		Point3f[] ap3Point = new Point3f[0];
		int[] aiLayer = new int[0];
		ControlPoint[] acpHead = new ControlPoint[0];
		int iVersion = 1;
		int iHighestLayer = 0;
		PointSelection[] layers = new PointSelection[MAX_LAYERS];
		JPatchMaterial[] materials = new JPatchMaterial[MAX_LAYERS];
		materials[0] = new JPatchMaterial(new Color3f(0.549f, 0.6745f, 0.4667f));
		materials[1] = new JPatchMaterial(new Color3f(0.4784f, 0.5373f, 0.6706f));
		materials[2] = new JPatchMaterial(new Color3f(0.6745f, 0.4667f, 0.4667f));
		materials[3] = new JPatchMaterial(new Color3f(0.6902f, 0.6627f, 0.4509f));
		materials[4] = new JPatchMaterial(new Color3f(0.6941f, 0.4471f, 0.6549f));
		materials[5] = new JPatchMaterial(new Color3f(0.549f, 0.6745f, 0.4667f));
		materials[6] = new JPatchMaterial(new Color3f(0.4784f, 0.5373f, 0.6706f));
		materials[7] = new JPatchMaterial(new Color3f(0.6745f, 0.4667f, 0.4667f));
		for (int l = 0; l < MAX_LAYERS; l++) {
			layers[l] = new PointSelection();
			layers[l].setName("sPatch Layer " + l);
			materials[l].setName("sPatch Layer " + l);
		}
		try {
			brFile = new BufferedReader(new FileReader(filename));
			while ((strLine = brFile.readLine()) != null) {
				if (strLine.startsWith("version")) {
					astrPart = strLine.split("\\s");
					iVersion = (new Integer(astrPart[1])).intValue();
				} else if (strLine.startsWith("num_points")) {
					astrPart = strLine.split("\\s");
					int iNumPoints = (new Integer(astrPart[1])).intValue();
					ap3Point = new Point3f[iNumPoints];
					aiLayer = new int[iNumPoints];
					acpHead = new ControlPoint[iNumPoints];
					for (int p = 0;p < iNumPoints;p++) {
						strLine = brFile.readLine().replaceFirst("\\s*","");
						astrPart = strLine.split("\\s");
						float x = (new Float(astrPart[0])).floatValue();
						float y = (new Float(astrPart[1])).floatValue();
						float z = (new Float(astrPart[2])).floatValue();
						strLine = brFile.readLine().replaceFirst("\\s*","");
						int iLayer = (new Integer(strLine)).intValue();
						if (iLayer > iHighestLayer) iHighestLayer = iLayer;
						strLine = brFile.readLine().replaceFirst("\\s*","");
						if (iVersion == 2) {
//							int iVisible = (new Integer(strLine)).intValue();
							strLine = brFile.readLine().replaceFirst("\\s*","");
						}
						int iNumSpstrLines = (new Integer(strLine)).intValue();
						for (int s = 0;s < iNumSpstrLines;s++) {
							strLine = brFile.readLine().replaceFirst("\\s*","");
							astrPart = strLine.split("\\s");
//							int iSpstrLineID = (new Integer(astrPart[0])).intValue();
//							int iPointOnSpstrLine = (new Integer(astrPart[1])).intValue();
						}
						ap3Point[p] = new Point3f(x,y,-z);
						aiLayer[p] = iLayer;
				 	}
				} else if (strLine.startsWith("num_curves")) {
					astrPart = strLine.split("\\s");
					int iNumCurves = (new Integer(astrPart[1])).intValue();
					for (int c = 0;c < iNumCurves;c++) {
						strLine = brFile.readLine().replaceFirst("\\s*","");
						astrPart = strLine.split("\\s");
						int iPointsOnSpline = (new Integer(astrPart[0])).intValue();
						//Curve curve = new Curve();
						//curve.closed = intToBoolean[(new Integer(astrPart[1])).intValue()];
						int iClosed = (new Integer(astrPart[1])).intValue();
						ControlPoint cpLast = null;
						ControlPoint cpFirst = null;
						for (int p = 0;p < iPointsOnSpline;p++) {
							strLine = brFile.readLine().replaceFirst("\\s*","");
							astrPart = strLine.split("\\s");
							int iPointID = (new Integer(astrPart[0])).intValue();
							ControlPoint cp = new ControlPoint(ap3Point[iPointID]);
							
							//cp.addToGroup(aiLayer[iPointID]);
							if (p == 0) {
								cpFirst = cp;
							}
							
							int iPeak = (new Integer(astrPart[1])).intValue();
							if (iPeak == 1) {
								cp.setMode(ControlPoint.SPATCH_ROUND);
							} else {
								cp.setMode(ControlPoint.PEAK);
							}
							
							//cp.layer = layers[pointID];
							if (acpHead[iPointID] == null) {
								acpHead[iPointID] = cp;
								layers[aiLayer[iPointID]].addControlPoint(cp);
							} else {
								cp.attachTo(acpHead[iPointID]);
							}
							float fCurvature = (new Float(astrPart[2])).floatValue();
							cp.setMagnitude(fCurvature * 3);
							if (cpLast != null) {
								cp.appendTo(cpLast);
							}
							cpLast = cp;
						}
						if (iClosed == 1) {
							cpFirst.setLoop(true);
							cpFirst.setPrev(cpLast);
							cpLast.setNext(cpFirst);
						}
						model.addCurve(cpFirst);
					}
				}
			}
			brFile.close();
			model.computePatches();
			for (int l = 0; l <= iHighestLayer; l++) {
				model.addSelection(layers[l]);
				model.addMaterial(materials[l]);
				layers[l].applyMaterial(materials[l]);
				//((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodesWereInserted(layer[l].getParent(),aiIndex);
			}
		}
		catch (IOException e)
		{
			return "error while importing sPatch model: " + e.getMessage();
		}
		//MainFrame.getInstance().getJPatchScreen().update_all();
		return "";
	}
}
