package com.shapesecurity.shift.es2016.semantics.ast;

import com.shapesecurity.functional.data.ImmutableList;
import com.shapesecurity.functional.data.Maybe;
import com.shapesecurity.functional.data.Monoid;
import com.shapesecurity.shift.es2016.ast.*;
import com.shapesecurity.shift.es2016.reducer.Director;
import com.shapesecurity.shift.es2016.reducer.MonoidalReducer;

import javax.annotation.Nonnull;

public class ThisOrArgumentsOrTryCatchFinallyChecker {
	public static boolean containsThisOrArgumentsOrTryCatchFinally(FunctionDeclaration declaration) {
		return declaration.body.statements.exists(statement -> Director.reduceStatement(Reducer.INSTANCE, statement)); // ignoring FormalParameters because ES5 formal parameters can't contain expressions
	}

	public static boolean containsThisOrArgumentsOrTryCatchFinally(FunctionExpression expr) {
		return expr.body.statements.exists(statement -> Director.reduceStatement(Reducer.INSTANCE, statement));
	}

	// Note: Does not support arguments shadowing
	private static class Reducer extends MonoidalReducer<Boolean> {
		static Reducer INSTANCE = new Reducer();
		private Reducer() {
			super(Monoid.BOOLEAN_OR);
		}

		@Nonnull
		@Override
		public Boolean reduceIdentifierExpression(@Nonnull IdentifierExpression node) {
			return node.name.equals("arguments");
		}

		@Nonnull
		@Override
		public Boolean reduceAssignmentTargetIdentifier(@Nonnull AssignmentTargetIdentifier node) {
			return node.name.equals("arguments");
		}

		@Nonnull
		@Override
		public Boolean reduceThisExpression(@Nonnull ThisExpression node) {
			return true;
		}

		@Nonnull
		public Boolean reduceFunctionBody(@Nonnull FunctionBody node, @Nonnull ImmutableList<Boolean> directives, @Nonnull ImmutableList<Boolean> statements) {
			return false;
		}

		@Nonnull
		@Override
		public Boolean reduceTryCatchStatement(
				@Nonnull TryCatchStatement node,
				@Nonnull Boolean body,
				@Nonnull Boolean catchClause) {
			return true;
		}

		@Nonnull
		@Override
		public Boolean reduceTryFinallyStatement(
				@Nonnull TryFinallyStatement node,
				@Nonnull Boolean body,
				@Nonnull Maybe<Boolean> catchClause,
				@Nonnull Boolean finalizer) {
			return true;
		}
	}
}
