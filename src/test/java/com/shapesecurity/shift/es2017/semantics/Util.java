package com.shapesecurity.shift.es2017.semantics;

import com.shapesecurity.shift.es2017.semantics.asg.Node;
import com.shapesecurity.shift.es2017.semantics.visitor.Director;
import com.shapesecurity.shift.es2017.semantics.visitor.ReconstructingReducer;

import javax.annotation.Nonnull;

final class Util {

	private Util() {

	}

	@Nonnull
	static Semantics rebuildSemantics(@Nonnull Semantics asg) {
		Node reducedNode = new Director<>(ReconstructingReducer.create()).reduceNode(asg.node);
		return new Semantics(reducedNode, asg.locals, asg.scriptVarDecls, asg.scopeLookup, asg.functionScopes);
	}
}
