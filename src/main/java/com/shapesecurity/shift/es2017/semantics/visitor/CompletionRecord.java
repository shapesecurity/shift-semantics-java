package com.shapesecurity.shift.es2017.semantics.visitor;

import com.shapesecurity.shift.es2017.semantics.asg.BreakTarget;
import com.shapesecurity.shift.es2017.semantics.asg.NodeWithValue;

import java.util.function.Function;

public abstract class CompletionRecord {

	// modelling ReturnIfAbrupt
	public abstract <T extends NodeWithValue> CompletionRecord mapNormal(Function<? super NodeWithValue, T> f);

	public static class Normal extends CompletionRecord {
		public final NodeWithValue value;

		Normal(NodeWithValue value) {
			this.value = value;
		}

		@Override
		public <T extends NodeWithValue> Normal mapNormal(Function<? super NodeWithValue, T> f) {
			return new Normal(f.apply(this.value));
		}
	}

	public static class Break extends CompletionRecord {
		public final BreakTarget target;

		Break(BreakTarget target) {
			this.target = target;
		}

		@Override
		public <T extends NodeWithValue> Break mapNormal(Function<? super NodeWithValue, T> f) {
			return this;
		}
	}

	public static class Continue extends CompletionRecord {
		public final BreakTarget target;

		Continue(BreakTarget target) {
			this.target = target;
		}

		@Override
		public <T extends NodeWithValue> Continue mapNormal(Function<? super NodeWithValue, T> f) {
			return this;
		}
	}

	public static class Return extends CompletionRecord {
		public final NodeWithValue value;

		Return(NodeWithValue value) {
			this.value = value;
		}

		@Override
		public <T extends NodeWithValue> Return mapNormal(Function<? super NodeWithValue, T> f) {
			return this;
		}
	}

	public static class Throw extends CompletionRecord {
		public final NodeWithValue value;

		Throw(NodeWithValue value) {
			this.value = value;
		}

		@Override
		public <T extends NodeWithValue> Throw mapNormal(Function<? super NodeWithValue, T> f) {
			return this;
		}
	}
}
