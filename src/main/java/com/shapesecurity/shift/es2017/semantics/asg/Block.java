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

import com.shapesecurity.functional.data.ImmutableList;
import com.shapesecurity.shift.es2017.ast.Directive;

import javax.annotation.Nonnull;

public class Block implements Node {
	@Nonnull
	public final ImmutableList<Node> children;

	// Note: Directives are being added to blocks in the cases where we are transforming the AST with new blocks and want
	// to ensure the directives are being applied to the block (i.e ensuring directives in IIFE's are executed). This is
	// not meant for users inserting directives in their code for blocks, but for the explicator to populate in situations
	// needing this functionality.
	@Nonnull
	public final ImmutableList<Directive> directives;

	public Block(@Nonnull ImmutableList<Node> children) {
		this.children = children;
		this.directives = ImmutableList.empty();
	}

	public Block(@Nonnull ImmutableList<Node> children, @Nonnull ImmutableList<Directive> directives) {
		this.children = children;
		this.directives = directives;
	}

	public Block(@Nonnull Node child) {
		this.children = ImmutableList.of(child);
		this.directives = ImmutableList.empty();
	}

	@Override
	public boolean equalsIgnoringChildren(@Nonnull Node node) {
		return node instanceof Block && this.directives.equals(((Block) node).directives);
	}

	public boolean isNoOp() {
		return !children.exists(c -> { // is no-op <==> no non-trivial ops
			if (c instanceof Void || c instanceof Literal || c instanceof LocalReference) { // TODO it might probably be better for each node to have a isNoOp method, which this method could call. or have a no-op interface, maybe.
				return false;
			} else if (c instanceof Block) {
				return !((Block) c).isNoOp();
			} else {
				return true;
			}
		});
	}
}
