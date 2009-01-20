package com.jpatch.entity.sds2;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.vecmath.*;

public class Explorer {
	private static Color FACE_COLOR = new Color(0x8888ff);
	private static Color EDGE_COLOR = new Color(0x88ff88);
	private static Color VERTEX_COLOR = new Color(0xff8888);
	
	private JComponent mainPanel = new JPanel(new BorderLayout());
	private JComponent entityPanel = new JPanel(new GridLayout(0, 2));
	private JComponent navBar = new JPanel(new BorderLayout());
	private JButton backButton = new JButton("<");
	private JButton forwardButton = new JButton(">");
	private JLabel title = new JLabel();
	
	private Object currentObject;
	private Stack<Object> history = new Stack<Object>();
	private Stack<Object> future = new Stack<Object>();
	
	public Explorer() {
		navBar.add(backButton, BorderLayout.WEST);
		navBar.add(title, BorderLayout.CENTER);
		navBar.add(forwardButton, BorderLayout.EAST);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(entityPanel, BorderLayout.NORTH);
		mainPanel.add(navBar, BorderLayout.NORTH);
		mainPanel.add(panel, BorderLayout.CENTER);
		title.setOpaque(true);
		
		backButton.setEnabled(false);
		forwardButton.setEnabled(false);
		
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				back();
			}
		});
		forwardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				forward();
			}
		});
	}

	public JComponent getComponent() {
		return mainPanel;
	}


	private void setFace(Face face) {
		entityPanel.removeAll();
		title.setBackground(FACE_COLOR);
		title.setText(face.toString());
		label("Material", face.getMaterial());
		label("isHelper", face.isHelper());
		for (int i = 0; i < face.getSides(); i++) {
			linkTo("Edge" + i, face.getEdges()[i]);
		}
		linkTo("Facepoint", face.getFacePoint());
	}
	
	private void setEdge(HalfEdge edge) {
		entityPanel.removeAll();
		title.setBackground(EDGE_COLOR);
		title.setText(edge.toString());
		label("BoundaryType", edge.getBoundaryType());
		linkTo("pair", edge.getPair());
		linkTo("next", edge.getNext());
		linkTo("prev", edge.getPrev());
		linkTo("face", edge.getFace());
		linkTo("pairFace", edge.getPairFace());
		label("index", edge.getFaceEdgeIndex());
		linkTo("vertex", edge.getVertex());
		linkTo("pairVertex", edge.getPairVertex());
		linkTo("edgePoint", edge.getEdgePoint());
	}
	
	private void setVertex(AbstractVertex vertex) {
		entityPanel.removeAll();
		title.setBackground(VERTEX_COLOR);
		title.setText(vertex.toString());
		Point3d p = vertex.getPos();
		label("x", p.x);
		label("y", p.y);
		label("z", p.z);
		label("BoundaryType", vertex.boundaryType);
		if (vertex.getEdges() != null) {
			for (int i = 0; i < vertex.getEdges().length; i++) {
				linkTo("Edge" + i, vertex.getEdges()[i]);
			}
		} else {
			label("No Edges", "HELPER VERTEX");
		}
		linkTo("vertexPoint", vertex.getVertexPoint());
		VertexId id = vertex.vertexId;
		if (id instanceof VertexId.EdgePointId) {
			linkTo("parent edge", ((VertexId.EdgePointId) id).halfEdge);
		} else if (id instanceof VertexId.FacePointId) {
			linkTo("parent face", ((VertexId.FacePointId) id).face);
		} else if (id instanceof VertexId.VertexPointId) {
			linkTo("parent vertex", ((VertexId.VertexPointId) id).parentVertex);
		}
	}
	
	private void jumpTo(Object object) {
		System.out.println("jumpTo(" + object + ") called");
		currentObject = object;
		if (object instanceof Face) {
			setFace((Face) object);
		} else if (object instanceof HalfEdge) {
			setEdge((HalfEdge) object);
		} else if (object instanceof AbstractVertex) {
			setVertex((AbstractVertex) object);
		}
//		entityPanel.repaint();
	}
	
	public void goTo(Object object) {
		if (currentObject != null) {
			history.add(currentObject);
			backButton.setEnabled(true);
		}
		jumpTo(object);
		future.clear();
		forwardButton.setEnabled(false);
	}
	
	private void back() {
		assert currentObject != null;
		future.push(currentObject);
		forwardButton.setEnabled(true);
		jumpTo(history.pop());
		if (history.isEmpty()) {
			backButton.setEnabled(false);
		}
	}
	
	private void forward() {
		assert currentObject != null;
		history.push(currentObject);
		backButton.setEnabled(true);
		jumpTo(future.pop());
		if (future.isEmpty()) {
			forwardButton.setEnabled(false);
		}
	}
	
	private void linkTo(String text, final Object object) {
		entityPanel.add(new JLabel(text));
		if (object != null) {
			JButton linkButton = new JButton(object.toString());
			linkButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					goTo(object);
				}
			});
			if (object instanceof Face) {
				linkButton.setBackground(FACE_COLOR);
			} else if (object instanceof HalfEdge) {
				linkButton.setBackground(EDGE_COLOR);
			} else if (object instanceof AbstractVertex) {
				linkButton.setBackground(VERTEX_COLOR);
			} 
			entityPanel.add(linkButton);
		} else {
			entityPanel.add(new JLabel("null"));
		}
	}
	
	private void label(String text, Object object) {
		entityPanel.add(new JLabel(text));
		entityPanel.add(new JLabel(object != null ? object.toString() : "null"));
	}
}
