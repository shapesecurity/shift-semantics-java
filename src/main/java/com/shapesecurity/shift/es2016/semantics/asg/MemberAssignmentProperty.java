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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Objects;

public interface MemberAssignmentProperty {
	class StaticValue implements MemberAssignmentProperty {
		@Nonnull
		public final NodeWithValue value;

		@Override
		public boolean equals(@Nullable Object o) {
			if (this == o) return true;
			if (!(o instanceof StaticValue)) return false;
			StaticValue that = (StaticValue) o;
			return Objects.equals(value, that.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}

		public StaticValue(@Nonnull NodeWithValue value) {
			this.value = value;
		}
	}

	class Getter implements MemberAssignmentProperty {
		@Nonnull
		public final LiteralFunction value;

		public Getter(@Nonnull LiteralFunction value) {
			this.value = value;
		}

		@Override
		public boolean equals(@Nullable Object o) {
			if (this == o) return true;
			if (!(o instanceof Getter)) return false;
			Getter getter = (Getter) o;
			return Objects.equals(value, getter.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}
	}

	class Setter implements MemberAssignmentProperty {
		@Nonnull
		public final LiteralFunction value;

		public Setter(@Nonnull LiteralFunction value) {
			this.value = value;
		}
	}
}
