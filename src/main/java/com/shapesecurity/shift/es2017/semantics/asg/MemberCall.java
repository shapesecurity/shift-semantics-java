package com.shapesecurity.shift.es2017.semantics.asg;

import com.shapesecurity.functional.data.ImmutableList;

import javax.annotation.Nonnull;

public class MemberCall implements NodeWithValue {
	@Nonnull
	public final NodeWithValue object;

	@Nonnull
	public final NodeWithValue fieldExpression;

	@Nonnull
	public final ImmutableList<NodeWithValue> arguments;

	public MemberCall(@Nonnull NodeWithValue object, @Nonnull NodeWithValue fieldExpression, @Nonnull ImmutableList<NodeWithValue> arguments) {
		this.object = object;
		this.fieldExpression = fieldExpression;
		this.arguments = arguments;
	}
}
