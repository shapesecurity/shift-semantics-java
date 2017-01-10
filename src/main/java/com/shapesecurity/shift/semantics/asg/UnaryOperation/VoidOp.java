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
package com.shapesecurity.shift.semantics.asg.UnaryOperation;

import com.shapesecurity.shift.semantics.asg.NodeWithValue;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public class VoidOp extends UnaryOperation {

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof VoidOp)) return false;
		VoidOp voidOp = (VoidOp) o;
		return Objects.equals(expression, voidOp.expression);
	}

	@Override
	public int hashCode() {
		return Objects.hash(expression);
	}

	@NotNull
	public final NodeWithValue expression;

	@NotNull
	public NodeWithValue expression() {
		return this.expression;
	}

	public VoidOp(@NotNull NodeWithValue expression) {
		this.expression = expression;
	}
}
