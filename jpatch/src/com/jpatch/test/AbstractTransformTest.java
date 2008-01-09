package com.jpatch.test;

import trashcan.*;
import com.jpatch.entity.*;

public class AbstractTransformTest {
	AbstractTransformNode node1 = new TransformNode();
	AbstractTransformNode node2 = new TransformNode();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new AbstractTransformTest();
	}

	AbstractTransformTest() {
		System.out.println("start");
		System.out.println("node1=" + node1);
		System.out.println("node2=" + node2);
		dump();
		System.out.println("adding node 2 to node 1");
		node1.getChildrenAttribute().add(node2);
		dump();
		System.out.println("removing node 2 from node 1");
		node1.getChildrenAttribute().remove(node2);
		dump();
		System.out.println("setting node 2 parent to node 1");
		node2.getParentAttribute().setValue(node1);
		dump();
		System.out.println("setting node 2 parent to null");
		node2.getParentAttribute().setValue(null);
		dump();
		System.out.println("finished");
	}
	
	void dump() {
		dump(node1);
		dump(node2);
	}
	void dump(AbstractTransformNode node) {
		String nodename = (node == node1) ? "node1" : (node == node2) ? "node2" : "?";
		System.out.println(nodename + " parent=" + node.getParentAttribute().getValue() + " children=" + node.getChildrenAttribute().getElements());
	}
}
