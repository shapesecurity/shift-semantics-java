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

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface MemberAssignmentProperty {
	class StaticValue implements MemberAssignmentProperty {
		@NotNull
		public final NodeWithValue value;

		public StaticValue(@NotNull NodeWithValue value) {
			this.value = value;
		}
	}

	class Getter implements MemberAssignmentProperty {
		@NotNull
		public final LiteralFunction value;

		public Getter(@NotNull LiteralFunction value) {
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {
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
		@NotNull
		public final LiteralFunction value;

		public Setter(@NotNull LiteralFunction value) {
			this.value = value;
		}
	}
}
