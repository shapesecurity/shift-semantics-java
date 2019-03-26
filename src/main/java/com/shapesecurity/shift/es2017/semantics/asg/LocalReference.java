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

import com.shapesecurity.shift.es2017.scope.Variable;

import javax.annotation.Nonnull;

public class LocalReference implements NodeWithValue {
	@Nonnull
	public Variable variable;


	public LocalReference(@Nonnull Variable variable) {
		this.variable = variable;
	}

	@Override
	public boolean equalsIgnoringChildren(@Nonnull Node node) {
		return node.getClass() == LocalReference.class && this.variable.equals(((LocalReference) node).variable);
	}
}
