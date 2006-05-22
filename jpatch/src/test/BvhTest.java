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

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.vecmath.*;

public class BvhTest {

	List<Channel> channels = new ArrayList<Channel>();
	float[][] data;
	Joint root;
	int frame;
	float sleep;
	
	public static void main(String[] args) throws Exception {
		final BvhTest bvhTest = new BvhTest();
		bvhTest.parseFile(new File(args[0]));
		JFrame frame = new JFrame();
		frame.setSize(800, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		final Matrix4f m1 = new Matrix4f(
			 3,  0,  0, 200,
			 0, -3,  0, 300,
			 0,  0,  0,   0,
			 0,  0,  0,   1
		);
		final Matrix4f m2 = new Matrix4f(
				 0,  0,  3, 200,
				 0, -3,  0, 300,
				 0,  0,  0,   0,
				 0,  0,  0,   1
			);
		bvhTest.setFrame(0);
		JPanel panel1 = new JPanel() {
			public void paint(Graphics g) {
				super.paint(g);
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				bvhTest.root.draw((Graphics2D) g, m1);
				g.drawString("Front view", 8, 16);
				g.drawString("Frame " + bvhTest.frame, 8, 32);
			}
		};
		JPanel panel2 = new JPanel() {
			public void paint(Graphics g) {
				super.paint(g);
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				bvhTest.root.draw((Graphics2D) g, m2);
				g.drawString("Side view", 8, 16);
				g.drawString("Frame " + bvhTest.frame, 8, 32);
			}
		};
		panel1.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		panel1.setBackground(Color.BLACK);
		panel1.setForeground(Color.WHITE);
		panel2.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		panel2.setBackground(Color.BLACK);
		panel2.setForeground(Color.WHITE);
		frame.setLayout(new GridLayout(1, 2));
		frame.add(panel1);
		frame.add(panel2);
		frame.setVisible(true);
		
		for(;;) {
			for (int i = 0; i < bvhTest.data.length; i++) {
				bvhTest.setFrame(i);
				panel1.repaint();
				panel2.repaint();
				Thread.sleep((int) (bvhTest.sleep * 1000));
			}
		}
	}

	void parseFile(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		Joint joint = null;
		boolean hierarchy = false;
		String name = null;
		int frame = 0;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			String[] token = line.split("\\s+");
			if (token[0].equals("HIERARCHY")) {
				hierarchy = true;
				continue;
			} else if (token[0].equals("MOTION")) {
				hierarchy = false;
				continue;
			}
			if (!hierarchy) {
				if (token[0].equals("Frames:")) {
					data = new float[Integer.parseInt(token[1])][channels.size()];
					continue;
				} else if (token[0].equals("Frame")) {
					sleep = Float.parseFloat(token[2]);
					continue;
				}
				for (int i = 0; i < token.length; i++)
					data[frame][i] = Float.parseFloat(token[i]);
				frame++;
			}
			if (token[0].equals("CHANNELS")) {
				int numChannels = Integer.parseInt(token[1]);
				for (int i = 0; i < numChannels; i++) {
					if (token[i + 2].equals("Xposition"))
						channels.add(new Channel(joint, Channel.Type.X_POSITION));
					else if (token[i + 2].equals("Yposition"))
						channels.add(new Channel(joint, Channel.Type.Y_POSITION));
					else if (token[i + 2].equals("Zposition"))
						channels.add(new Channel(joint, Channel.Type.Z_POSITION));
					else if (token[i + 2].equals("Zrotation"))
						channels.add(new Channel(joint, Channel.Type.Z_ROTATION));
					else if (token[i + 2].equals("Xrotation"))
						channels.add(new Channel(joint, Channel.Type.X_ROTATION));
					else if (token[i + 2].equals("Yrotation"))
						channels.add(new Channel(joint, Channel.Type.Y_ROTATION));
				}
			} else if (token[0].equals("OFFSET")) {
				joint.offset.x = Float.parseFloat(token[1]);
				joint.offset.y = Float.parseFloat(token[2]);
				joint.offset.z = Float.parseFloat(token[3]);
			} else if (token[0].equals("ROOT") || token[0].equals("JOINT")) {
				name = token[1];
			} else if (token[0].equals("{")) {
				Joint child = new Joint();
				if (joint != null)
					joint.children.add(child);
				child.parent = joint;
				joint = child;
				joint.name = name;
				if (root == null)
					root = joint;
			} else if (token[0].equals("}")) {
				joint = joint.parent;
			} else {
				name = line;
			}
		}
	}
	
	void setFrame(int frame) {
		root.reset();
		for (int i = 0; i < data[frame].length; i++)
			channels.get(i).set(data[frame][i]);
		root.set();
		this.frame = frame;
	}
	
	static class Joint {
		String name;
		Joint parent;
		Vector3f offset = new Vector3f();
		Vector3f position = new Vector3f();
		Vector3f rotation = new Vector3f();
		Point3f transformedPosition;
		List<Joint> children = new ArrayList<Joint>();
		Matrix3f m;
		
		void dump(String prefix) {
			System.out.println(prefix + name + " " + transformedPosition);
			for (Joint child:children)
				child.dump(prefix + "    ");
		}
		
		void reset() {
			transformedPosition = null;
			position.set(0, 0, 0);
			rotation.set(0, 0, 0);
			m = null;
			for (Joint child:children)
				child.reset();
		}
		
		void set() {
			if (parent != null) 
				transformedPosition = new Point3f(parent.transformedPosition);
			else
				transformedPosition = new Point3f();
			Matrix3f mx = new Matrix3f();
			Matrix3f my = new Matrix3f();
			Matrix3f mz = new Matrix3f();
			mx.setIdentity();
			my.setIdentity();
			mz.setIdentity();
			mx.rotX(rotation.x / 180 * (float) Math.PI);
			my.rotY(rotation.y / 180 * (float) Math.PI);
			mz.rotZ(rotation.z / 180 * (float) Math.PI);
			
			m = new Matrix3f();
			if (parent == null)
				m.setIdentity();
			else
				m.set(parent.m);
			m.mul(mz);
			m.mul(mx);
			m.mul(my);
			Vector3f v = new Vector3f(offset);
			v.add(position);
//			m.transform(v);
			if (parent != null) {
//				m.mul(parent.m);
				parent.m.transform(v);
			}
			transformedPosition.add(v);
			for (Joint child:children)
				child.set();
		}
		
		void draw(Graphics2D g, Matrix4f m) {
			if (parent != null) {
				Point3f p0 = new Point3f(parent.transformedPosition);
				Point3f p1 = new Point3f(transformedPosition);
				m.transform(p0);
				m.transform(p1);
				Line2D.Float l;
				l = new Line2D.Float(p0.x, p0.y, p1.x, p1.y);
				g.draw(l);
				Rectangle2D.Float r = new Rectangle2D.Float(p1.x - 2, p1.y - 2, 5, 5);
				g.fill(r);
			}
			for (Joint child:children)
				child.draw(g, m);
		}
	}
	
	static class Channel {
		enum Type { X_POSITION, Y_POSITION, Z_POSITION, Z_ROTATION, X_ROTATION, Y_ROTATION }
		Type type;
		Joint joint;
		
		public Channel(Joint joint, Type type) {
			this.joint = joint;
			this.type = type;
		}
		
		public String toString() {
			return joint.name + " " + type;
		}
		
		void set(float value) {
			switch (type) {
			case X_POSITION:
				joint.position.x = value;
				break;
			case Y_POSITION:
				joint.position.y = value;
				break;
			case Z_POSITION:
				joint.position.z = value;
				break;
			case Z_ROTATION:
				joint.rotation.z = value;
				break;
			case X_ROTATION:
				joint.rotation.x = value;
				break;
			case Y_ROTATION:
				joint.rotation.y = value;
				break;
			}
		}
	}
}
