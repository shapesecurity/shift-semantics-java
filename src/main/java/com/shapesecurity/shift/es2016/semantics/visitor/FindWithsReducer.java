package com.shapesecurity.shift.es2016.semantics.visitor;

import com.shapesecurity.functional.Pair;
import com.shapesecurity.functional.data.ConcatList;
import com.shapesecurity.functional.data.ImmutableList;
import com.shapesecurity.functional.data.Monoid;
import com.shapesecurity.functional.data.MultiHashTable;
import com.shapesecurity.shift.es2016.ast.FunctionBody;
import com.shapesecurity.shift.es2016.ast.Program;
import com.shapesecurity.shift.es2016.ast.WithStatement;
import com.shapesecurity.shift.es2016.reducer.Director;
import com.shapesecurity.shift.es2016.reducer.MonoidalReducer;

import javax.annotation.Nonnull;

public class FindWithsReducer extends MonoidalReducer<Pair<ConcatList<WithStatement>, MultiHashTable<FunctionBody, WithStatement>>> {
	// Gives a map from function bodies to the WithStatements they contain.
	// Would need to be changed slightly for ES6, where you can have with statements in parameter lists (ugh), but that doesn't come up here.
	private static class StateMonoid implements Monoid<Pair<ConcatList<WithStatement>, MultiHashTable<FunctionBody, WithStatement>>> {
		// TODO: it would be better to use the FreePairingMonoid, but shape-functional-java doesn't provide an implementation for MultiHashTableIdentityMergeMonoid
		static StateMonoid INSTANCE = new StateMonoid();

		@Nonnull
		@Override
		public Pair<ConcatList<WithStatement>, MultiHashTable<FunctionBody, WithStatement>> identity() {
			return Pair.of(ConcatList.empty(), MultiHashTable.emptyUsingIdentity());
		}

		@Nonnull
		@Override
		public Pair<ConcatList<WithStatement>, MultiHashTable<FunctionBody, WithStatement>> append(Pair<ConcatList<WithStatement>, MultiHashTable<FunctionBody, WithStatement>> left, Pair<ConcatList<WithStatement>, MultiHashTable<FunctionBody, WithStatement>> right) {
			return Pair.of(left.left.append(right.left), left.right.merge(right.right));
		}
	}

	private static FindWithsReducer INSTANCE = new FindWithsReducer();
	private FindWithsReducer() {
		super(StateMonoid.INSTANCE);
	}

	public static Pair<ConcatList<WithStatement>, MultiHashTable<FunctionBody, WithStatement>> reduce(@Nonnull Program program) {
		return Director.reduceProgram(INSTANCE, program);
	}

	@Nonnull
	@Override
	public Pair<ConcatList<WithStatement>, MultiHashTable<FunctionBody, WithStatement>> reduceWithStatement(@Nonnull WithStatement node, @Nonnull Pair<ConcatList<WithStatement>, MultiHashTable<FunctionBody, WithStatement>> object, @Nonnull Pair<ConcatList<WithStatement>, MultiHashTable<FunctionBody, WithStatement>> body) {
		Pair<ConcatList<WithStatement>, MultiHashTable<FunctionBody, WithStatement>> s = super.reduceWithStatement(node, object, body);
		return Pair.of(s.left.append1(node), s.right);
	}

	@Nonnull
	@Override
	public Pair<ConcatList<WithStatement>, MultiHashTable<FunctionBody, WithStatement>> reduceFunctionBody(@Nonnull FunctionBody node, @Nonnull ImmutableList<Pair<ConcatList<WithStatement>, MultiHashTable<FunctionBody, WithStatement>>> directives, @Nonnull ImmutableList<Pair<ConcatList<WithStatement>, MultiHashTable<FunctionBody, WithStatement>>> statements) {
		Pair<ConcatList<WithStatement>, MultiHashTable<FunctionBody, WithStatement>> s = super.reduceFunctionBody(node, directives, statements);
		return Pair.of(s.left, s.left.foldLeft((acc, statement) -> acc.put(node, statement), s.right));
	}
}
