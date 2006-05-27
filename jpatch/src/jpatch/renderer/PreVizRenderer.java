package jpatch.renderer;

import inyo.JPatchInyoInterface;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;
import javax.vecmath.*;

import jpatch.boundary.*;
import jpatch.entity.*;
import jpatch.boundary.*;
import jpatch.boundary.settings.*;

public class PreVizRenderer implements Renderer {

	private List<AnimModel> models;
	private List<AnimLight> lights;
	private Camera camera;
	private PatchTesselator3 patchTesselator = new PatchTesselator3();
	private volatile boolean abort = false;
	
	public PreVizRenderer(List<AnimModel> models, Camera camera, List<AnimLight> lights) {
		this.models = models;
		this.camera = camera;
		this.lights = lights;
	}
	
	public Image render() {
		final RendererSettings rendererSettings = Settings.getInstance().export;
		JPatchDrawable3D drawable = new JPatchDrawable3D(rendererSettings.imageWidth, rendererSettings.imageHeight);
		
		/*
		 * set projection, transform and focallength
		 */
		drawable.setProjection(JPatchDrawable2.PERSPECTIVE);
		drawable.setFocalLength(camera.getFocalLength());
		Matrix4d cameraTransform = new Matrix4d(camera.getTransform());
		cameraTransform.invert();
		
		/*
		 * set background
		 */
		drawable.clear(JPatchDrawable2.COLOR_BUFFER | JPatchDrawable2.DEPTH_BUFFER, rendererSettings.backgroundColor);
		
		/*
		 * set lighting
		 */
		AnimLight[] animLights = lights.toArray(new AnimLight[lights.size()]);
		RealtimeLighting lighting = RealtimeLighting.createAnimLight(animLights, cameraTransform);
		
		/*
		 * render models
		 */
		for (AnimModel animModel : models) {
			
			Model model = animModel.getModel();
			cameraTransform = new Matrix4d(camera.getTransform());
			cameraTransform.invert();
			Matrix4d modelTransform = new Matrix4d(animModel.getTransform());
			cameraTransform.mul(modelTransform);
			
			patchTesselator.tesselate(model, rendererSettings.previz.subdivisionLevel, cameraTransform, true);
			for (JPatchMaterial material : model.getMaterialList()) {
				if (abort)
					return null;
				
				PatchTesselator3.Vertex[] vtx = patchTesselator.getPerMaterialVertexArray(material);
				MaterialProperties mp = material.getMaterialProperties();
				int[][] t = patchTesselator.getPerMaterialTriangleArray();
				if (t.length > 0) {
					Color3f c0 = new Color3f();
					Color3f c1 = new Color3f();
					Color3f c2 = new Color3f();
					Point3f p0 = new Point3f();
					Point3f p1 = new Point3f();
					Point3f p2 = new Point3f();
					for (int i = 0; i < t.length; i++) {
						if (abort)
							return null;
						p0.set(vtx[t[i][0]].p);
						p1.set(vtx[t[i][1]].p);
						p2.set(vtx[t[i][2]].p);
						Vector3f n0 = vtx[t[i][0]].n;
						Vector3f n1 = vtx[t[i][1]].n;
						Vector3f n2 = vtx[t[i][2]].n;
						lighting.shade(p0, n0, mp, c0);
						lighting.shade(p1, n1, mp, c1);
						lighting.shade(p2, n2, mp, c2);
						drawable.drawTriangle(p0, c0, p1, c1, p2, c2);
					}
				}
			}
		}
		
		BufferedImage image = drawable.getImage();
		return image;
	}
			
	public void abort() {
		// TODO Auto-generated method stub

	}

}
