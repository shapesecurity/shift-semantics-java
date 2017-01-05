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

public class MemberDefinition implements Node {
	@NotNull
	public final NodeWithValue object;
	@NotNull
	public final NodeWithValue fieldExpression;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MemberDefinition)) return false;
		MemberDefinition that = (MemberDefinition) o;
		return Objects.equals(object, that.object) &&
				Objects.equals(fieldExpression, that.fieldExpression) &&
				Objects.equals(property, that.property);
	}

	@Override
	public int hashCode() {
		return Objects.hash(object, fieldExpression, property);
	}

	@NotNull
	public final MemberAssignmentProperty property;

	public MemberDefinition(
		@NotNull NodeWithValue object, @NotNull NodeWithValue fieldExpression, @NotNull MemberAssignmentProperty property
	) {
		this.object = object;
		this.fieldExpression = fieldExpression;
		this.property = property;
	}

	public MemberDefinition(
		@NotNull NodeWithValue object, @NotNull NodeWithValue fieldExpression, @NotNull NodeWithValue staticValue
	) {
		this.object = object;
		this.fieldExpression = fieldExpression;
		this.property = new MemberAssignmentProperty.StaticValue(staticValue);
	}
}
