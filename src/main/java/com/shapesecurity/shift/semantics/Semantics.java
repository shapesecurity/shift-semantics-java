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
package com.shapesecurity.shift.semantics;

import com.shapesecurity.functional.data.ImmutableList;
import com.shapesecurity.shift.es2016.scope.Variable;
import com.shapesecurity.shift.semantics.asg.Node;
import javax.annotation.Nonnull;

public class Semantics {
	@Nonnull
	public final Node node;
	@Nonnull
	public final ImmutableList<Variable> locals;
	@Nonnull
	public final ImmutableList<String> scriptVarDecls;

	public Semantics(@Nonnull Node node, @Nonnull ImmutableList<Variable> locals, @Nonnull ImmutableList<String> scriptVarDecls) {
		this.node = node;
		this.locals = locals;
		this.scriptVarDecls = scriptVarDecls;
	}
}
