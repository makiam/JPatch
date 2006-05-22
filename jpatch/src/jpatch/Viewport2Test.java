package jpatch;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import jpatch.auxilary.*;
import jpatch.boundary.*;
import jpatch.entity.*;
import javax.vecmath.*;
import javax.imageio.*;

public class Viewport2Test implements JPatchDrawableEventListener {
	
	private AliasWavefrontObj obj;
	private BufferedImage image;
	
	//private Lighting lighting = Lighting.createThreePointLight();
	private String[] name = new String[] { "single", "split v", "split h", "quad" };
	private float[] angle = new float[] { 0.0f, 0.0f, 0.0f, 0.0f };
	private float[] speed = new float[] { 0.005f, 0.005f, 0.005f, 0.005f };
	private Vector3f[] vector = new Vector3f[] {
		new Vector3f(-1.0f, 1.0f, 0.0f),
		new Vector3f(0.0f, -1.0f, 1.0f),
		new Vector3f(1.0f, 0.0f, -1.0f),
		new Vector3f(1.0f, 1.0f, 1.0f)
	};
	private float[] phase = new float[] { 0, 1, 2, 3 };
	private int iMode = 0;
	private int iDisplay = 0;
	private JPatchDrawable2[] avp = new JPatchDrawable2[4];
	private JFrame frame;
	private JPanel panel = new JPanel();
	private JPanel buttons = new JPanel();
	private boolean bLightweight = false;
	private RealtimeLighting rtl = RealtimeLighting.createTestLight();
	
	//public static void main(String[] args) {
	//	new Viewport2Test(args[0]);
	//}
	
	class SwitchModeActionListener implements ActionListener {
		int mode;
		SwitchModeActionListener(int mode) {
			this.mode = mode;
		}
		public void actionPerformed(ActionEvent e) {
			switchMode(mode);
		}
	}
	
	class SwitchDisplayActionListener implements ActionListener {
		int display;
		SwitchDisplayActionListener(int mode) {
			this.display = mode;
		}
		public void actionPerformed(ActionEvent e) {
			switchDisplay(display);
		}
	}
	
	class SwitchLightweightActionListener implements ActionListener {
		boolean lightweight;
		SwitchLightweightActionListener(boolean lightweight) {
			this.lightweight = lightweight;
		}
		public void actionPerformed(ActionEvent e) {
			bLightweight = lightweight;
			switchDisplay(iDisplay);
		}
	}
	
	public Viewport2Test(String objResource, String mtlResource) throws Exception {
		//System.out.println(objFile);
		obj = (new JarObjReader()).readObj(objResource, mtlResource);
		image = ImageIO.read(ClassLoader.getSystemResource("objects/lena.jpg"));
		//image = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.);
		//image.getGraphics().drawImage(im, 0, 0, null);
		//for (int i = 0; i < obj.v.length; i++) {
		//	obj.v[i].scale(40);
		//	//obj.v[i].z -= 500;
		//}
		
		for (int i = 0; i < 4; vector[i++].normalize()) ;
		
		frame = new JFrame("Viewport Test");
		
		switchDisplay(0);
		
		//Viewport2 viewport2 = new ViewportGL(this);
		//viewport2.setProjection(Viewport2.ORTHOGONAL);
		//frame.add(viewport2.getComponent());
		frame.setSize(800, 600);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				frame.dispose();
			}
		});
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		
		for (int i = 0; i < 4; i++) {
			JButton button = new JButton(name[i]);
			button.addActionListener(new SwitchModeActionListener(i));
			buttons.add(button);
		}
		JButton button = new JButton("2D");
		button.addActionListener(new SwitchDisplayActionListener(1));
		buttons.add(button);
		button = new JButton("GL");
		button.addActionListener(new SwitchDisplayActionListener(0));
		buttons.add(button);
		button = new JButton("3D");
		button.addActionListener(new SwitchDisplayActionListener(2));
		buttons.add(button);
		button = new JButton("light");
		button.addActionListener(new SwitchLightweightActionListener(true));
		buttons.add(button);
		button = new JButton("heavy");
		button.addActionListener(new SwitchLightweightActionListener(false));
		buttons.add(button);
		
		frame.add(buttons, BorderLayout.NORTH);
		
		frame.setVisible(true);
		//frame.doLayout();
		
		for(;;) {
			switch(iMode) {
				case 0: {
					avp[0].display();
				}
				break;
				case 1: {
					avp[0].display();
					avp[1].display();
				}
				break;
				case 2: {
					avp[0].display();
					avp[2].display();
				}
				break;
				case 3: {
					avp[0].display();
					avp[1].display();
					avp[2].display();
					avp[3].display();
				}
				break;
			}
			for (int i = 0; i < 4; angle[i] += speed[i++]);
			//panelButtons.repaint();
			//try {
			//	Thread.sleep(20);
			//} catch (InterruptedException e) {
			//	e.printStackTrace();
			//}
		}
			//angle += 0.02f;
			//try {
			//	Thread.currentThread().sleep(10);
			//} catch (Exception e) {
			//}
		
	}
	
	public void switchDisplay(int display) {
		iDisplay = display;
		if (avp[0] != null) panel.remove(avp[0].getComponent());
		if (avp[1] != null) panel.remove(avp[1].getComponent());
		if (avp[2] != null) panel.remove(avp[2].getComponent());
		if (avp[3] != null) panel.remove(avp[3].getComponent());
		switch(display) {
			case 0: {
				for (int i = 0; i < avp.length; i++) {
					avp[i] = new JPatchDrawableGL(this, bLightweight);
					avp[i].setProjection(JPatchDrawable2.PERSPECTIVE);
				}
			}
			break;
			case 1: {
				for (int i = 0; i < avp.length; i++) {
					avp[i] = new JPatchDrawable2D(this, bLightweight);
					avp[i].setProjection(JPatchDrawable2.PERSPECTIVE);
				}
			}
			break;
			case 2: {
				for (int i = 0; i < avp.length; i++) {
					avp[i] = new JPatchDrawable3D(this, bLightweight);
					avp[i].setProjection(JPatchDrawable2.PERSPECTIVE);
				}
			}
		}
		switchMode(iMode);
	}
	
	public void switchMode(int mode) {
		iMode = mode;
		panel.remove(avp[0].getComponent());
		panel.remove(avp[1].getComponent());
		panel.remove(avp[2].getComponent());
		panel.remove(avp[3].getComponent());
		switch(mode) {
			case 0: {
				panel.setLayout(new GridLayout(1,1));
				panel.add(avp[0].getComponent());
//				avp[0].setImage(image);
			}
			break;
			case 1: {
				panel.setLayout(new GridLayout(2,1));
				panel.add(avp[0].getComponent());
				panel.add(avp[1].getComponent());
//				avp[0].setImage(image);
//				avp[1].setImage(image);
			}
			break;
			case 2: {
				panel.setLayout(new GridLayout(1,2));
				panel.add(avp[0].getComponent());
				panel.add(avp[2].getComponent());
//				avp[0].setImage(image);
//				avp[2].setImage(image);
			}
			break;
			case 3: {
				panel.setLayout(new GridLayout(2,2));
				panel.add(avp[0].getComponent());
				panel.add(avp[1].getComponent());
				panel.add(avp[2].getComponent());
				panel.add(avp[3].getComponent());
//				avp[0].setImage(image);
//				avp[1].setImage(image);
//				avp[2].setImage(image);
//				avp[3].setImage(image);
			}
			break;
		}
		//panel.repaint();
		panel.doLayout();
		buttons.repaint();
		//frame.repaint();
		//panel.
	}
	
	public void display(JPatchDrawable2 vp) {
		vp.setProjection(JPatchDrawable2.PERSPECTIVE);
		if (vp.isLightingSupported())
			vp.setLighting(rtl);
		vp.clear(JPatchDrawable2.COLOR_BUFFER | JPatchDrawable2.DEPTH_BUFFER, new Color3f(0,0,1));
		vp.setColor(new Color3f(1,1,1));
		vp.setPointSize(3);
		
		
		
		//Point3f pa = new Point3f();
		//Point3f pb = new Point3f();
		//int segments = 32;
		//for (int i = 0; i < segments; i++) {
		//	for (int j = 0; j < segments; j++) {
		//		float a = (float) j / (float) segments * 2 * (float) Math.PI + (float) Math.random();
		//		float b = (float) (j + 1) / (float) segments * 2 * (float) Math.PI;
		//		float xa = (float) Math.cos(a);
		//		float ya = (float) Math.sin(a);
		//		float xb = (float) Math.cos(b);
		//		float yb = (float) Math.sin(b);
		//		pa.set(xa * 10 * i, ya * 10 * i, 0);
		//		pb.set(xb * 10 * i, yb * 10 * i, 0);
		//		vp.drawPoint(pa);
		//		vp.drawLine(pa, pb);
		//	}
		//}
				
		//vp.drawPoint(new Point3f(0,0,0));
		int v;
		if (vp == avp[0]) v = 0;
		else if (vp == avp[1]) v = 1;
		else if (vp == avp[2]) v = 2;
		else v = 3;
		
		float scaleX = 1.5f + 1.4f * (float) Math.cos(phase[v]);
		float scaleY = 1.5f + 1.4f * (float) Math.sin(phase[v]);
		phase[v] += 0.01f;
		
		vp.drawImage(image, -50 + (int) (300 * scaleY), -50 + (int) (300 * scaleX), scaleX, scaleY);
//		vp.drawImage(150, 50, scaleX, scaleY);
//		vp.drawImage(50, 150, scaleX, scaleY);
//		vp.drawImage(150, 150, scaleX, scaleY);
		
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		matrix.setRotation(new AxisAngle4f(vector[v], angle[v]));
		matrix.setScale(1.0f);
		matrix.setTranslation(new Vector3f(0, -4, 50 + 40 * (float) Math.sin(phase[v])));
		//for (int i = 0; i < obj.v.length; i++) {
		//	
		//	vp.drawPoint(obj.v[i]);
		//}
		
		//rtl.transform(matrix);
		MaterialProperties mp = new MaterialProperties();
		Color3f c0 = new Color3f();
		Color3f c1 = new Color3f();
		Color3f c2 = new Color3f();
		Point3f p0 = new Point3f();
		Point3f p1 = new Point3f();
		Point3f p2 = new Point3f();
		Vector3f n0 = new Vector3f();
		Vector3f n1 = new Vector3f();
		Vector3f n2 = new Vector3f();
		for (int i = 0; i < obj.fv.length; i++) {
			AliasWavefrontMat mat = obj.mat[i];
			mp.red = mat.Kd.x;
			mp.green = mat.Kd.y;
			mp.blue = mat.Kd.z;
//			mp.ambient = 0;
//			mp.diffuse = 1;
//			mp.specular = 1;
//			mp.roughness = 0.05f;
			if (vp.isLightingSupported())
				vp.setMaterial(mp);
			for (int j = 0; j < obj.fv[i].length; j++) {
				p0.set(obj.v[obj.fv[i][j][0]]);
				p1.set(obj.v[obj.fv[i][j][1]]);
				p2.set(obj.v[obj.fv[i][j][2]]);
				n0.set(obj.vn[obj.fn[i][j][0]]);
				n1.set(obj.vn[obj.fn[i][j][1]]);
				n2.set(obj.vn[obj.fn[i][j][2]]);
				matrix.transform(p0);
				matrix.transform(p1);
				matrix.transform(p2);
				matrix.transform(n0);
				matrix.transform(n1);
				matrix.transform(n2);
				n0.normalize();
				n1.normalize();
				n2.normalize();
				//lighting.shade(p0, n0, mp, c0);
				//lighting.shade(p1, n1, mp, c1);
				//lighting.shade(p2, n2, mp, c2);
				//c0.clamp(0, 1);
				//c1.clamp(0, 1);
				//c2.clamp(0, 1);
				//vp.drawTriangle(p0, c0, p1, c1, p2, c2);
				//vp.drawPoint(p0);
				if (false && vp.isShadingSupported()) {
					if (vp.isLightingSupported()) {
						vp.drawTriangle(p0, n0, p1, n1, p2, n2);
						vp.drawPoint(p0);
						vp.drawPoint(p1);
						vp.drawPoint(p2);
					} else {
						rtl.shade(p0, n0, mp, c0);
						rtl.shade(p1, n1, mp, c1);
						rtl.shade(p2, n2, mp, c2);
						c0.clamp(0, 1);
						c1.clamp(0, 1);
						c2.clamp(0, 1);
						vp.drawTriangle(p0, c0, p1, c1, p2, c2);
						vp.drawPoint(p0);
						vp.drawPoint(p1);
						vp.drawPoint(p2);
					}
				} else {
					float z = -p0.z / 10 - 5;
					//System.out.println(z);
					if (z < 0)
						z = 0;
					if (z > 1)
						z = 1;
					//System.out.println(z);
					vp.setColor(new Color3f(1, z, z));
					vp.drawLine(p0, p1);
					vp.drawLine(p1, p2);
					vp.drawLine(p2, p0);
					vp.drawPoint(p0);
					vp.drawPoint(p1);
					vp.drawPoint(p2);
				}
			}
		}
		vp.setColor(new Color3f(0,1,1));
		vp.drawString("ABCDEFGHIJKLMNOPQRSTUVWXYZ", 20, 20);
		vp.drawString("abcdefghijklmnopqrstuvwxyz", 20, 40);
		vp.drawString("0123456789+-*/!\"§$%&/()=\\?", 20, 60);
		vp.drawString("äöüÄÖÜß@€<>|,.;:_~#'^°'`{}[]", 20, 80);
		
//		Graphics g = vp.getGraphics();
//		if (g != null) {
//			g.setColor(Color.WHITE);
//			g.drawString(vp.getComponent().toString(), 16, 16);
//		}
	}
}
