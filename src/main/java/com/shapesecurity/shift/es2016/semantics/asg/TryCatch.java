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

package com.shapesecurity.shift.es2016.semantics.asg;

import com.shapesecurity.functional.Pair;
import com.shapesecurity.functional.data.Maybe;
import com.shapesecurity.shift.es2016.scope.Variable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Objects;

public class TryCatch implements Node {
	@Nonnull
	public final Block tryBody;
	@Nonnull
	public final Pair<Variable, Block> catchBody;

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) return true;
		if (!(o instanceof TryCatch)) return false;
		TryCatch that = (TryCatch) o;
		return Objects.equals(tryBody, that.tryBody) &&
				Objects.equals(catchBody, that.catchBody);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tryBody, catchBody);
	}

	public TryCatch(@Nonnull Block tryBody, @Nonnull Pair<Variable, Block> catchBody) {
		this.tryBody = tryBody;
		this.catchBody = catchBody;
	}
}
