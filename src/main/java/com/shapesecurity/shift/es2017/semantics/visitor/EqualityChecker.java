package com.shapesecurity.shift.es2017.semantics.visitor;

import com.shapesecurity.functional.data.ImmutableList;
import com.shapesecurity.shift.es2017.semantics.asg.Node;

import javax.annotation.Nonnull;
import java.util.Iterator;

public final class EqualityChecker {

	private EqualityChecker() {

	}

	public static boolean nodesAreEqual(@Nonnull Node node1, @Nonnull Node node2) {
		if (node1 == node2) {
			return true;
		}
		if (!node1.equalsIgnoringChildren(node2)) {
			return false;
		}
		ImmutableList<Node> nodeList1 = GetDescendents.getDescendants(node1);
		ImmutableList<Node> nodeList2 = GetDescendents.getDescendants(node2);
		if (nodeList1.length != nodeList2.length) {
			return false;
		}
		Iterator<Node> node1Iterator = nodeList1.iterator();
		Iterator<Node> node2Iterator = nodeList2.iterator();
		while (node1Iterator.hasNext() && node2Iterator.hasNext()) {
			if (!node1Iterator.next().equalsIgnoringChildren(node2Iterator.next())) {
				return false;
			}
		}
		return !node1Iterator.hasNext() && !node2Iterator.hasNext();
	}

}
