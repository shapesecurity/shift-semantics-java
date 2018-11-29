package com.shapesecurity.shift.es2017.semantics.asg;

import com.shapesecurity.functional.data.ImmutableList;
import com.shapesecurity.functional.data.Maybe;
import com.shapesecurity.shift.es2017.semantics.BrokenThrough;

import javax.annotation.Nonnull;
import java.util.Objects;

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
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ReturnAfterFinallies that = (ReturnAfterFinallies) o;
		return Objects.equals(savedValue, that.savedValue) &&
				Objects.equals(broken, that.broken);
	}

	@Override
	public int hashCode() {
		return Objects.hash(savedValue, broken);
	}
}
