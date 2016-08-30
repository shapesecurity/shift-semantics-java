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
import com.shapesecurity.functional.data.Maybe;
import com.shapesecurity.shift.scope.Variable;

public class LiteralFunction implements Literal {
	public final Maybe<Variable> name;
	public final Maybe<Variable> arguments;
	public final ImmutableList<Variable> parameters;
	public final ImmutableList<Variable> locals;
	public final ImmutableList<Variable> captured;
	public final Block body;
	public final boolean isStrict;

	public LiteralFunction(
		Maybe<Variable> name, Maybe<Variable> arguments, ImmutableList<Variable> parameters, ImmutableList<Variable> locals,
		ImmutableList<Variable> captured, Block body, boolean isStrict
	) {
		this.name = name;
		this.arguments = arguments;
		this.parameters = parameters;
		this.locals = locals;
		this.captured = captured;
		this.body = body;
		this.isStrict = isStrict;
	}
}
