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
import com.shapesecurity.functional.data.Maybe;
import com.shapesecurity.shift.es2017.scope.Variable;

import javax.annotation.Nonnull;

public class LiteralFunction implements Literal {
	@Nonnull
	public final Maybe<Variable> name;
	@Nonnull
	public final Maybe<Variable> arguments;
	@Nonnull
	public final ImmutableList<Variable> parameters;
	@Nonnull
	public final ImmutableList<Variable> locals;


	@Nonnull
	public final ImmutableList<Variable> captured;
	@Nonnull
	public final Block body;
	@Nonnull
	public final boolean isStrict;

	public LiteralFunction(
		@Nonnull Maybe<Variable> name, @Nonnull Maybe<Variable> arguments, @Nonnull ImmutableList<Variable> parameters, @Nonnull ImmutableList<Variable> locals,
		@Nonnull ImmutableList<Variable> captured, @Nonnull Block body, boolean isStrict
	) {
		this.name = name;
		this.arguments = arguments;
		this.parameters = parameters;
		this.locals = locals;
		this.captured = captured;
		this.body = body;
		this.isStrict = isStrict;
	}

	@Override
	public boolean equalsIgnoringChildren(@Nonnull Node node) {
		return node instanceof LiteralFunction &&
			this.name.equals(((LiteralFunction) node).name) &&
			this.arguments.equals(((LiteralFunction) node).arguments) &&
			this.parameters.equals(((LiteralFunction) node).parameters) &&
			this.locals.equals(((LiteralFunction) node).locals) &&
			this.captured.equals(((LiteralFunction) node).captured) &&
			this.isStrict == ((LiteralFunction) node).isStrict;
	}
}
