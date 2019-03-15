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

import javax.annotation.Nonnull;

public class LiteralRegExp implements Literal {
	@Nonnull
	public final String pattern;
	public final boolean global;
	public final boolean ignoreCase;
	public final boolean multiLine;
	public final boolean sticky;
	public final boolean unicode;


	public LiteralRegExp(@Nonnull String pattern, boolean global, boolean ignoreCase, boolean multiLine, boolean sticky, boolean unicode) {
		this.pattern = pattern;
		this.global = global;
		this.ignoreCase = ignoreCase;
		this.multiLine = multiLine;
		this.sticky = sticky;
		this.unicode = unicode;
	}

	@Override
	public boolean equalsIgnoringChildren(@Nonnull Node node) {
		return node instanceof LiteralRegExp &&
			this.pattern.equals(((LiteralRegExp) node).pattern) &&
			this.global == ((LiteralRegExp) node).global &&
			this.ignoreCase == ((LiteralRegExp) node).ignoreCase &&
			this.multiLine == ((LiteralRegExp) node).ignoreCase &&
			this.sticky == ((LiteralRegExp) node).sticky &&
			this.unicode == ((LiteralRegExp) node).unicode;
	}
}
