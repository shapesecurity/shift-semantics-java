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

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DeleteProperty implements NodeWithValue {
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DeleteProperty)) return false;
		DeleteProperty that = (DeleteProperty) o;
		return strict == that.strict &&
				Objects.equals(object, that.object) &&
				Objects.equals(fieldExpression, that.fieldExpression);
	}

	@Override
	public int hashCode() {
		return Objects.hash(object, fieldExpression, strict);
	}

	@NotNull
	public final NodeWithValue object;
	@NotNull
	public final NodeWithValue fieldExpression;
	public final boolean strict;

	public DeleteProperty(@NotNull NodeWithValue object, @NotNull NodeWithValue fieldExpression, boolean strict) {
		this.object = object;
		this.fieldExpression = fieldExpression;
		this.strict = strict;
	}
}
