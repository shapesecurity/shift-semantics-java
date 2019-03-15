package com.shapesecurity.shift.es2017.semantics.asgvisitor;

import com.shapesecurity.functional.data.ImmutableList;
import com.shapesecurity.shift.es2017.semantics.Semantics;
import com.shapesecurity.shift.es2017.semantics.asg.Node;
import com.shapesecurity.shift.es2017.semantics.visitor.Director;
import com.shapesecurity.shift.es2017.semantics.visitor.NodeAdaptingReducer;
import com.shapesecurity.shift.es2017.semantics.visitor.ReconstructingReducer;
import com.shapesecurity.shift.es2017.semantics.asg.Block;
import com.shapesecurity.shift.es2017.semantics.visitor.Reducer;

import javax.annotation.Nonnull;

public class BlockSquasher extends ReconstructingReducer {

	public static Reducer<Node> create() {
		return new NodeAdaptingReducer(new BlockSquasher());
	}

	protected BlockSquasher() {
		super();
	}

	public static Semantics reduce(@Nonnull Semantics semantics) {
		return new Semantics(
			new Director<>(BlockSquasher.create()).reduceNode(semantics.node),
			semantics.locals,
			semantics.scriptVarDecls,
			semantics.scopeLookup,
			semantics.functionScopes
		);
	}


	@Nonnull
	@Override
	public Block reduceBlock(@Nonnull Block block, @Nonnull ImmutableList<Node> children) {
		return new Block(children.flatMap(node -> {
			if (node instanceof Block) {
				return ((Block) node).children;
			} else {
				return ImmutableList.of(node);
			}
		}));
	}

}
