package jpatch.renderer;

import java.awt.Image;
import java.util.*;
import javax.vecmath.*;
import jpatch.entity.*;
import jpatch.boundary.*;

import inyo.*;

public class InyoRenderer3 {
	
	private Image image;
	private List models;
	private List lights;
	private Camera camera;
	private PatchTesselator3 patchTesselator = new PatchTesselator3();
	public InyoRenderer3(List models, Camera camera, List lights) {
		this.models = models;
		this.camera = camera;
		this.lights = lights;
	}
	
	public Image render(JPatchInyoInterface inyo) {
		//Model model = MainFrame.getInstance().getModel();
		//model.computePatches();
		
		JPatchSettings settings = JPatchSettings.getInstance();
		
		inyo.setImageSize(settings.iRenderHeight, settings.iRenderWidth, 1);
		//inyo.setAspectRatio() ???
		
		Matrix4d cam = new Matrix4d(camera.getTransform());
		cam.invert();
		inyo.setCamera(cam);
		inyo.setCameraFocalLength((float) camera.getFocalLength());
		
		inyo.setMaxRecursionDepth(settings.inyoSettings.iRecursion);
		inyo.setSoftShadowSamples(settings.inyoSettings.iShadowSamples);
		inyo.setTransparentShadows(settings.inyoSettings.bTransparentShadows);
		inyo.setCaustics(settings.inyoSettings.bEnableCaustics, settings.inyoSettings.bOversampleCaustics);
		inyo.setUseAmbientOcclusion(settings.inyoSettings.bEnableAmbientOcclusion);
		inyo.setAmbientOcclusion(settings.inyoSettings.fAmbientOcclusionDistance);
		inyo.setAmbientOcclusionSamples(settings.inyoSettings.iAmbientOcclusionSamples);
		inyo.ambientOcclusionColorBleed(settings.inyoSettings.fAmbientOcclusionColorbleed);
		inyo.setOversample(settings.inyoSettings.iSupersample, settings.inyoSettings. iSamplingMode == 1);
		
		inyo.setShowStats(false);
		
		/*
		 * background
		 */
		float[] skyColor = new float[3];
		settings.cBackgroundColor.getRGBColorComponents(skyColor);
		inyo.setSkyColor(skyColor[0], skyColor[1], skyColor[2]);
		
		/*
		 * lightsources
		 */
		for (Iterator it = lights.iterator(); it.hasNext(); ) {
			AnimLight light = (AnimLight) it.next();
			if (light.isActive()) {
				Point3d p = light.getPosition();
				inyo.addLight(p.x, p.y, p.z, light.getIntensity());
				Color3f c = light.getColor();
				inyo.setLightColor(c.x, c.y, c.z);
				inyo.setLightRadius(light.getSize());
			}
		}
		
		
		for (Iterator it = models.iterator(); it.hasNext(); ) {
			AnimModel animModel = (AnimModel) it.next();
			Model model = animModel.getModel();
			int subdiv = JPatchSettings.getInstance().inyoSettings.iSubdivMode + animModel.getSubdivisionOffset();
			if (subdiv < 2) subdiv = 2;
			if (subdiv > 5) subdiv = 5;
			patchTesselator.tesselate(model, subdiv, animModel.getTransform(), true);
	
			
			
			for (Iterator iterator = model.getMaterialList().iterator(); iterator.hasNext();) {
				JPatchMaterial material = (JPatchMaterial)iterator.next();
				PatchTesselator3.Vertex[] vtx = patchTesselator.getPerMaterialVertexArray(material);
				int[][] triangles = patchTesselator.getPerMaterialTriangleArray();
				if (triangles.length > 0) {
					inyo.objectBegin();
					
					
					MaterialProperties mp = material.getMaterialProperties();
					inyo.addMaterial(mp.red, mp.green, mp.blue);
					inyo.setMaterialFilter(mp.filter);
					inyo.setMaterialTransmit(mp.transmit);
					inyo.setMaterialAmbient(mp.ambient);
					inyo.setMaterialDiffuse(mp.diffuse);
					inyo.setMaterialBrilliance(mp.brilliance);
					inyo.setMaterialSpecular(mp.specular);
					inyo.setMaterialRoughness(mp.roughness);
					inyo.setMaterialMetallic(mp.metallic);
					inyo.setMaterialReflection(mp.reflectionMin, mp.reflectionMax, mp.reflectionFalloff);
					inyo.setMaterialRefraction(mp.refraction);
					inyo.setMaterialConserveEnergy(mp.conserveEnergy);
					inyo.setMaterialTexture(material.getRenderString("inyo",""));
					for (int i = 0; i < vtx.length; i++) {
						PatchTesselator3.Vertex v = vtx[i];
						inyo.addVertex(v.p.x, v.p.y, v.p.z, v.r.x, v.r.y, v.r.z, v.n.x, v.n.y, v.n.z);
					}
					
					for (int i = 0; i < triangles.length; i++) {
						inyo.addTriangle(triangles[i][0], triangles[i][1], triangles[i][2]);
					}
					
					inyo.objectEnd();
				}
				
			}
			
			
		}
		
		inyo.startRendering(new InyoJPatchInterface() {
			/**
			 * Tell JPatch about the rendering progress
			 * @param progress 0.0 means rendering just started, 0.5 means half way done, 1.0 means rendering finished.
			 */
			public void progress(double progress) { }
			
			/**
			 * Pass the rendered image back to JPatch
			 * @param image the final image
			 */
			public void renderingDone(java.awt.Image image) {
				InyoRenderer3.this.image = image;
			}
		});
		try {
			/*
			 * ugly while loop to wait until the image is ready
			 */
			while (image == null) {
				Thread.sleep(100);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return image;
	}
}
