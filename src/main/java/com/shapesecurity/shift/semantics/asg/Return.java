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
package com.shapesecurity.shift.semantics.asg;

import com.shapesecurity.functional.data.Maybe;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Objects;

public class Return implements Node {
	@Nonnull
	public final Maybe<NodeWithValue> expression;

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) return true;
		if (!(o instanceof Return)) return false;
		Return aReturn = (Return) o;
		return Objects.equals(expression, aReturn.expression);
	}

	@Override
	public int hashCode() {
		return Objects.hash(expression);
	}

	public Return(@Nonnull Maybe<NodeWithValue> expression) {
		this.expression = expression;
	}

	public Return(@Nonnull NodeWithValue expression) {
		this.expression = Maybe.of(expression);
	}
}
