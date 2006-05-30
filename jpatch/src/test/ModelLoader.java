/*
 * $Id$
 *
 * Copyright (c) 2005 Sascha Ledinsky
 *
 * This file is part of JPatch.
 *
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package test;

import java.awt.event.*;

import javax.swing.*;

import jpatch.entity.*;
import jpatch.boundary.*;
import jpatch.boundary.settings.*;
import jpatch.control.importer.*;

public class ModelLoader {

	private Viewport2 viewport;
	private ViewDefinition viewDef = new ViewDefinition(ViewDefinition.FRONT);
	
	public static void main(String[] args) {
		new ModelLoader();
	}
	
	public ModelLoader() {
		/*
		 * render backfacing patches and set quality to a low setting.
		 */
		Settings.getInstance().realtimeRenderer.backfacingPatches = RealtimeRendererSettings.Backface.RENDER;
		Settings.getInstance().realtimeRenderer.realtimeRenererQuality = 1;
		
		/*
		 * Create frame, set resolution and close-operation.
		 */
		JFrame frame = new JFrame("JPatch Model loader");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		
		/*
		 * Create a new model, animationMasterImporter, and load the model
		 */
		final Model model = new Model();
		AnimationMasterImport importer = new AnimationMasterImport();
		System.out.println(importer.importModel(model, "/home/sascha/CartoonRabbit.mdl"));
		
		/*
		 * Create a JPatchDrawableEventListener
		 * it's display method is called whenever the drawable has to render itself.
		 */
		JPatchDrawableEventListener listener = new JPatchDrawableEventListener() {
			public void display(JPatchDrawable2 drawable) {
				viewport.prepare();
				viewport.drawOrigin();
				viewport.drawModel(model, null);
				viewport.drawInfo();
			}
		};
		
		/*
		 * Create the JPatchDrawable (OpenGL), set projection to orthogolan
		 */
		final JPatchDrawable2 drawable = new JPatchDrawableGL(listener, false);
		drawable.setProjection(JPatchDrawable2.ORTHOGONAL);
		
		/*
		 * Create the viewport, set viewdef properties
		 */
		viewport = new Viewport2(drawable, viewDef);
		viewDef.setDrawable(drawable);
		viewDef.renderPoints(false);
		viewDef.renderCurves(false);
		viewDef.renderPatches(true);
		viewDef.renderBones(false);
		viewDef.setLighting(RealtimeLighting.createThreepointLight());
		viewDef.setScale(0.005f);
		viewDef.moveView(0, 0.5f);
		
		/*
		 * add the drawable to the frame and show the frame
		 */
		frame.add(drawable.getComponent());
		frame.setVisible(true);
		
		/*
		 * start swing timer to slowly rotate the model. All rendering must be done on
		 * the event-dispaching thread, so if not using a javax.swing.Timer, be careful!
		 */
		Timer t = new Timer(10, new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				viewDef.rotateView(0.02f, 0.0f);				
			}
		});
		t.start();
	}
}
