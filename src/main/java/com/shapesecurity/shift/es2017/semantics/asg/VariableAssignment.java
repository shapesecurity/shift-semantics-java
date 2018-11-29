/*
 * Copyright 2016 Shape Security, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shapesecurity.shift.es2017.semantics.asg;

import com.shapesecurity.functional.data.Either;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Objects;

public class VariableAssignment implements NodeWithValue {
	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) return true;
		if (!(o instanceof VariableAssignment)) return false;
		VariableAssignment that = (VariableAssignment) o;
		return strict == that.strict &&
				Objects.equals(ref, that.ref) &&
				Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ref, value, strict);
	}

	@Nonnull
	public final Either<GlobalReference, LocalReference> ref;
	@Nonnull
	public final NodeWithValue value;
	public final boolean strict;

	public VariableAssignment(
			@Nonnull Either<GlobalReference, LocalReference> ref, @Nonnull NodeWithValue value, boolean strict
	) {
		this.ref = ref;
		this.value = value;
		this.strict = strict;
	}

	public VariableAssignment(@Nonnull GlobalReference ref, @Nonnull NodeWithValue value, boolean strict) {
		this.ref = Either.left(ref);
		this.value = value;
		this.strict = strict;
	}

	public VariableAssignment(@Nonnull LocalReference ref, @Nonnull NodeWithValue value, boolean strict) {
		this.ref = Either.right(ref);
		this.value = value;
		this.strict = strict;
	}
}
