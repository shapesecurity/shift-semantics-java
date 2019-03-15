package com.shapesecurity.shift.es2017.semantics.asg;

import com.shapesecurity.functional.data.ImmutableList;
import com.shapesecurity.functional.data.Maybe;
import com.shapesecurity.shift.es2017.semantics.BrokenThrough;

import javax.annotation.Nonnull;

public class ReturnAfterFinallies implements Node {
	@Nonnull
	public final Maybe<LocalReference> savedValue;

	@Nonnull
	public final ImmutableList<BrokenThrough> broken;

	public ReturnAfterFinallies(@Nonnull Maybe<LocalReference> savedValue, @Nonnull ImmutableList<BrokenThrough> broken) {
		this.savedValue = savedValue;
		this.broken = broken;
	}

	@Override
	public boolean equalsIgnoringChildren(@Nonnull Node node) {
		return node instanceof ReturnAfterFinallies &&
			this.savedValue.isJust() == ((ReturnAfterFinallies) node).savedValue.isJust()
			&& this.broken.equals(((ReturnAfterFinallies) node).broken);
	}

}


