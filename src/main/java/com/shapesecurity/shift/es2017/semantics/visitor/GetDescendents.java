package com.shapesecurity.shift.es2017.semantics.visitor;

import com.shapesecurity.functional.data.ConcatList;
import com.shapesecurity.functional.data.ImmutableList;
import com.shapesecurity.functional.data.Monoid;
import com.shapesecurity.shift.es2017.semantics.asg.Node;

import javax.annotation.Nonnull;

// order not defined, but is deterministic
public final class GetDescendents extends MonoidalReducer<ConcatList<Node>> {

	public static ImmutableList<Node> getDescendants(@Nonnull Node node) {
		return new Director<>(new GetDescendents()).reduceNode(node).toList();
	}

	private GetDescendents() {
		super(new Monoid.ConcatListAppend<>());
	}

	@Nonnull
	@Override
	public ConcatList<Node> reduceAll(@Nonnull Node node, @Nonnull ConcatList<Node> reduced) {
		return reduced.append1(node);
	}
}
