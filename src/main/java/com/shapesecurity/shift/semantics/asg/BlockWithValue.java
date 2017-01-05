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

import com.shapesecurity.functional.data.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BlockWithValue implements NodeWithValue {
	@NotNull
	public Block head;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BlockWithValue)) return false;
		BlockWithValue that = (BlockWithValue) o;
		return Objects.equals(head, that.head) &&
				Objects.equals(result, that.result);
	}

	@Override
	public int hashCode() {
		return Objects.hash(head, result);
	}

	@NotNull

	public NodeWithValue result;

	public BlockWithValue(@NotNull Block head, @NotNull NodeWithValue result) {
		this.head = head;
		this.result = result;
	}

	public BlockWithValue(@NotNull ImmutableList<Node> children, @NotNull NodeWithValue result) {
		this.head = new Block(children);
		this.result = result;
	}
}
