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

package com.shapesecurity.shift.es2017.semantics.visitor;

import com.shapesecurity.functional.Pair;
import com.shapesecurity.functional.data.HashTable;
import com.shapesecurity.functional.data.ImmutableList;
import com.shapesecurity.functional.data.Maybe;
import com.shapesecurity.functional.data.Monoid;
import com.shapesecurity.shift.es2017.ast.*;
import com.shapesecurity.shift.es2017.ast.Module;
import com.shapesecurity.shift.es2017.semantics.BrokenThrough;
import com.shapesecurity.shift.es2017.reducer.Director;
import com.shapesecurity.shift.es2017.reducer.MonoidalReducer;

import javax.annotation.Nonnull;

// Almost identical to JumpReducer, but also tracks the number of finally clauses that a jump exits (i.e., 'finally {...}' blocks that a 'break' or 'return' is within and its target is not)
// Gives a map from break/continue statements to the statement/loop broken and return statements to the function body being returned. For labelled statements, map goes to the body of the statement, not the LabelledStatement
// Relies on the AST being valid: in particular, does not check that labelled continues are breaking loops rather than just statements.
// TODO could be waaaaay more typesafe than a map from nodes to nodes.
public class FinallyJumpReducer extends MonoidalReducer<FinallyJumpReducer.State> {
	@Nonnull
	public static final FinallyJumpReducer INSTANCE = new FinallyJumpReducer();

	private FinallyJumpReducer() {
		super(new StateMonoid());
	}

	@Nonnull
	public static HashTable<Node, Pair<Node, ImmutableList<BrokenThrough>>> extract(@Nonnull FinallyJumpReducer.State state) {
		assert state.unlabelledBreaks.length == 0;
		assert state.labelledBreaks.length == 0;
		assert state.unlabelledContinues.length == 0;
		assert state.labelledContinues.length == 0;
		assert state.returns.length == 0;
		return state.knownJumps;
	}

	@Nonnull
	public static HashTable<Node, Pair<Node, ImmutableList<BrokenThrough>>> analyze(@Nonnull Script script) {
		return FinallyJumpReducer.extract(Director.reduceScript(INSTANCE, script));
	}

	@Nonnull
	public static HashTable<Node, Pair<Node, ImmutableList<BrokenThrough>>> analyze(@Nonnull Module module) {
		return FinallyJumpReducer.extract(Director.reduceModule(INSTANCE, module));
	}


	@Nonnull
	private static State loopHelper(@Nonnull Node loopNode, @Nonnull State body) { // no labels in loop bounds
		HashTable<Node, Pair<Node, ImmutableList<BrokenThrough>>> knownJumps = body.knownJumps;
		knownJumps = body.unlabelledBreaks.foldLeft((table, kv) -> table.put(kv.left, new Pair<>(loopNode, kv.right)), knownJumps);
		knownJumps = body.unlabelledContinues.foldLeft((table, kv) -> table.put(kv.left, new Pair<>(loopNode, kv.right)), knownJumps);
		return new State(
			HashTable.emptyUsingIdentity(),
			body.labelledBreaks,
			HashTable.emptyUsingIdentity(),
			body.labelledContinues,
			body.returns,
			knownJumps
		);
	}

	@Nonnull
	private State switchHelper(@Nonnull Node switchNode, @Nonnull State body) { // no labels in discriminant
		return new State(
			HashTable.emptyUsingIdentity(),
			body.labelledBreaks,
			body.unlabelledContinues,
			body.labelledContinues,
			body.returns,
			body.unlabelledBreaks.foldLeft((table, kv) -> table.put(kv.left, new Pair<>(switchNode, kv.right)), body.knownJumps)
		);
	}

	@Nonnull
	@Override
	public State reduceBreakStatement(@Nonnull BreakStatement node) {
		if (node.label.isJust()) {
			String label = node.label.fromJust();
			return new State(
				HashTable.emptyUsingIdentity(),
				HashTable.<String, ImmutableList<Pair<BreakStatement, ImmutableList<BrokenThrough>>>>emptyUsingEquality().put(
					label,
					ImmutableList.of(new Pair<>(node, ImmutableList.empty()))
				),
				HashTable.emptyUsingIdentity(),
				HashTable.emptyUsingEquality(),
				HashTable.emptyUsingIdentity(),
				HashTable.emptyUsingIdentity()
			);
		} else {
			return new State(
				HashTable.<BreakStatement, ImmutableList<BrokenThrough>>emptyUsingIdentity().put(node, ImmutableList.empty()),
				HashTable.emptyUsingEquality(),
				HashTable.emptyUsingIdentity(),
				HashTable.emptyUsingEquality(),
				HashTable.emptyUsingIdentity(),
				HashTable.emptyUsingIdentity()
			);
		}
	}

	@Nonnull
	@Override
	public State reduceContinueStatement(@Nonnull ContinueStatement node) {
		if (node.label.isJust()) {
			String label = node.label.fromJust();
			return new State(
				HashTable.emptyUsingIdentity(),
				HashTable.emptyUsingEquality(),
				HashTable.emptyUsingIdentity(),
				HashTable.<String, ImmutableList<Pair<ContinueStatement, ImmutableList<BrokenThrough>>>>emptyUsingEquality().put(
					label,
					ImmutableList.of(new Pair<>(node, ImmutableList.empty()))
				),
				HashTable.emptyUsingIdentity(),
				HashTable.emptyUsingIdentity()
			);
		} else {
			return new State(
				HashTable.emptyUsingIdentity(),
				HashTable.emptyUsingEquality(),
				HashTable.<ContinueStatement, ImmutableList<BrokenThrough>>emptyUsingIdentity().put(node, ImmutableList.empty()),
				HashTable.emptyUsingEquality(),
				HashTable.emptyUsingIdentity(),
				HashTable.emptyUsingIdentity()
			);
		}
	}

	@Nonnull
	@Override
	public State reduceDoWhileStatement(@Nonnull DoWhileStatement node, @Nonnull State body, @Nonnull State test) {
		return loopHelper(node, super.reduceDoWhileStatement(node, body, test));
	}

	@Nonnull
	@Override
	public State reduceForInStatement(
		@Nonnull ForInStatement node, @Nonnull State left, @Nonnull State right, @Nonnull State body
	) {
		return loopHelper(node, super.reduceForInStatement(node, left, right, body));
	}

	@Nonnull
	@Override
	public State reduceForOfStatement(
		@Nonnull ForOfStatement node, @Nonnull State left, @Nonnull State right, @Nonnull State body
	) {
		return loopHelper(node, super.reduceForOfStatement(node, left, right, body));
	}

	@Nonnull
	@Override
	public State reduceForStatement(
		@Nonnull ForStatement node, @Nonnull Maybe<State> init, @Nonnull Maybe<State> test, @Nonnull Maybe<State> update,
		@Nonnull State body
	) {
		return loopHelper(node, super.reduceForStatement(node, init, test, update, body));
	}

	@Nonnull
	@Override
	public State reduceLabeledStatement(@Nonnull LabeledStatement node, @Nonnull State body) {
		ImmutableList<Pair<BreakStatement, ImmutableList<BrokenThrough>>> newBreaks = body.labelledBreaks.get(node.label).orJust(ImmutableList.empty());
		ImmutableList<Pair<ContinueStatement, ImmutableList<BrokenThrough>>> newContinues =
			body.labelledContinues.get(node.label).orJust(ImmutableList.empty());
		HashTable<String, ImmutableList<Pair<BreakStatement, ImmutableList<BrokenThrough>>>> labelledBreaks = body.labelledBreaks.remove(node.label);
		HashTable<String, ImmutableList<Pair<ContinueStatement, ImmutableList<BrokenThrough>>>> labelledContinues =
			body.labelledContinues.remove(node.label);

		HashTable<Node, Pair<Node, ImmutableList<BrokenThrough>>> knownJumps = body.knownJumps;
		knownJumps = newBreaks.foldLeft((table, kv) -> table.put(kv.left, new Pair<>(node.body, kv.right)), knownJumps);
		knownJumps = newContinues.foldLeft((table, kv) -> table.put(kv.left, new Pair<>(node.body, kv.right)), knownJumps);

		return new State(
			body.unlabelledBreaks,
			labelledBreaks,
			body.unlabelledContinues,
			labelledContinues,
			body.returns,
			knownJumps
		);
	}


	@Nonnull
	@Override
	public State reduceSwitchStatement(
		@Nonnull SwitchStatement node, @Nonnull State discriminant, @Nonnull ImmutableList<State> cases
	) {
		return switchHelper(node, super.reduceSwitchStatement(node, discriminant, cases));
	}

	@Nonnull
	@Override
	public State reduceSwitchStatementWithDefault(
		@Nonnull SwitchStatementWithDefault node, @Nonnull State discriminant, @Nonnull ImmutableList<State> preDefaultCases,
		@Nonnull State defaultCase, @Nonnull ImmutableList<State> postDefaultCases
	) {
		return switchHelper(
			node,
			super.reduceSwitchStatementWithDefault(node, discriminant, preDefaultCases, defaultCase, postDefaultCases)
		);
	}

	@Nonnull
	public State reduceTryCatchStatement(@Nonnull TryCatchStatement node, @Nonnull State body, @Nonnull State catchClause) {
		return super.reduceTryCatchStatement(node, body.observeTryWithCatch(), catchClause);
	}

	@Nonnull
	@Override
	public State reduceTryFinallyStatement(
		@Nonnull TryFinallyStatement node, @Nonnull State block, @Nonnull Maybe<State> catchClause, @Nonnull State finalizer
	) {
		// We model try-catch-finally as a separate try-catch inside of the try of a try-finally
		if (catchClause.isJust()) {
			block = block.observeTryWithCatch();
		}
		block = block.observeTryWithFinally();
		catchClause = catchClause.map(State::observeTryWithFinally);
		finalizer = finalizer.observeFinally();
		return super.reduceTryFinallyStatement(node, block, catchClause, finalizer);
	}

	@Nonnull
	@Override
	public State reduceWhileStatement(@Nonnull WhileStatement node, @Nonnull State test, @Nonnull State body) {
		return loopHelper(node, super.reduceWhileStatement(node, test, body));
	}


	@Nonnull
	@Override
	public State reduceReturnStatement(@Nonnull ReturnStatement node, @Nonnull Maybe<State> expression) {
		return super.reduceReturnStatement(node, expression).observeReturn(node);
	}

	@Nonnull
	public State reduceFunctionBody(@Nonnull FunctionBody node, @Nonnull ImmutableList<State> directives, @Nonnull ImmutableList<State> statements) {
		State superState = super.reduceFunctionBody(node, directives, statements);
		assert superState.unlabelledBreaks.length == 0;
		assert superState.labelledBreaks.length == 0;
		assert superState.unlabelledContinues.length == 0;
		assert superState.labelledContinues.length == 0;

		return new State(
				HashTable.emptyUsingIdentity(),
				HashTable.emptyUsingEquality(),
				HashTable.emptyUsingIdentity(),
				HashTable.emptyUsingEquality(),
				HashTable.emptyUsingIdentity(),
				superState.returns.foldLeft((table, kv) -> table.put(kv.left, new Pair<>(node, kv.right)), superState.knownJumps)
		);
	}

	static final class State {
		@Nonnull
		private final HashTable<BreakStatement, ImmutableList<BrokenThrough>> unlabelledBreaks;
		@Nonnull
		private final HashTable<String, ImmutableList<Pair<BreakStatement, ImmutableList<BrokenThrough>>>> labelledBreaks;
		@Nonnull
		private final HashTable<ContinueStatement, ImmutableList<BrokenThrough>> unlabelledContinues;
		@Nonnull
		private final HashTable<String, ImmutableList<Pair<ContinueStatement, ImmutableList<BrokenThrough>>>> labelledContinues;
		@Nonnull
		private final HashTable<ReturnStatement, ImmutableList<BrokenThrough>> returns;
		@Nonnull
		private final HashTable<Node, Pair<Node, ImmutableList<BrokenThrough>>> knownJumps;

		State(
				@Nonnull HashTable<BreakStatement, ImmutableList<BrokenThrough>> unlabelledBreaks,
				@Nonnull HashTable<String, ImmutableList<Pair<BreakStatement, ImmutableList<BrokenThrough>>>> labelledBreaks,
				@Nonnull HashTable<ContinueStatement, ImmutableList<BrokenThrough>> unlabelledContinues,
				@Nonnull HashTable<String, ImmutableList<Pair<ContinueStatement, ImmutableList<BrokenThrough>>>> labelledContinues,
				@Nonnull HashTable<ReturnStatement, ImmutableList<BrokenThrough>> returns,
				@Nonnull HashTable<Node, Pair<Node, ImmutableList<BrokenThrough>>> knownJumps
		) {
			this.unlabelledBreaks = unlabelledBreaks;
			this.labelledBreaks = labelledBreaks;
			this.unlabelledContinues = unlabelledContinues;
			this.labelledContinues = labelledContinues;
			this.returns = returns;
			this.knownJumps = knownJumps;
		}

		State() {
			this.unlabelledBreaks = HashTable.emptyUsingIdentity();
			this.labelledBreaks = HashTable.emptyUsingEquality();
			this.unlabelledContinues = HashTable.emptyUsingIdentity();
			this.labelledContinues = HashTable.emptyUsingEquality();
			this.returns = HashTable.emptyUsingIdentity();
			this.knownJumps = HashTable.emptyUsingIdentity();
		}

		State(@Nonnull State a, @Nonnull State b) {
			this.unlabelledBreaks = a.unlabelledBreaks.merge(b.unlabelledBreaks);
			this.labelledBreaks = a.labelledBreaks.merge(b.labelledBreaks, ImmutableList::append);
			this.unlabelledContinues = a.unlabelledContinues.merge(b.unlabelledContinues);
			this.labelledContinues = a.labelledContinues.merge(b.labelledContinues, ImmutableList::append);
			this.returns = a.returns.merge(b.returns);
			this.knownJumps = a.knownJumps.merge(b.knownJumps);
		}


		@Nonnull
		State observeTryWithCatch() {
			return new State(
					this.unlabelledBreaks.map(x -> x.cons(BrokenThrough.TRY_WITHOUT_FINALLY)),
					this.labelledBreaks.map(l -> l.map(p -> new Pair<>(p.left, p.right.cons(BrokenThrough.TRY_WITHOUT_FINALLY)))),
					this.unlabelledContinues.map(x -> x.cons(BrokenThrough.TRY_WITHOUT_FINALLY)),
					this.labelledContinues.map(l -> l.map(p -> new Pair<>(p.left, p.right.cons(BrokenThrough.TRY_WITHOUT_FINALLY)))),
					this.returns.map(x -> x.cons(BrokenThrough.TRY_WITHOUT_FINALLY)),
					this.knownJumps
			);
		}

		@Nonnull
		State observeTryWithFinally() {
			return new State(
					this.unlabelledBreaks.map(x -> x.cons(BrokenThrough.TRY_WITH_FINALLY)),
					this.labelledBreaks.map(l -> l.map(p -> new Pair<>(p.left, p.right.cons(BrokenThrough.TRY_WITH_FINALLY)))),
					this.unlabelledContinues.map(x -> x.cons(BrokenThrough.TRY_WITH_FINALLY)),
					this.labelledContinues.map(l -> l.map(p -> new Pair<>(p.left, p.right.cons(BrokenThrough.TRY_WITH_FINALLY)))),
					this.returns.map(x -> x.cons(BrokenThrough.TRY_WITH_FINALLY)),
					this.knownJumps
			);
		}

		@Nonnull
		State observeFinally() {
			return new State(
					this.unlabelledBreaks.map(x -> x.cons(BrokenThrough.FINALLY)),
					this.labelledBreaks.map(l -> l.map(p -> new Pair<>(p.left, p.right.cons(BrokenThrough.FINALLY)))),
					this.unlabelledContinues.map(x -> x.cons(BrokenThrough.FINALLY)),
					this.labelledContinues.map(l -> l.map(p -> new Pair<>(p.left, p.right.cons(BrokenThrough.FINALLY)))),
					this.returns.map(x -> x.cons(BrokenThrough.FINALLY)),
					this.knownJumps
			);
		}

		@Nonnull
		State observeReturn(ReturnStatement node) {
			return new State(
					this.unlabelledBreaks,
					this.labelledBreaks,
					this.unlabelledContinues,
					this.labelledContinues,
					this.returns.put(node, ImmutableList.empty()),
					this.knownJumps
			);
		}
	}

	private static final class StateMonoid implements Monoid<State> {
		@Override
		@Nonnull
		public State identity() {
			return new State();
		}

		@Override
		@Nonnull
		public State append(@Nonnull State a, @Nonnull State b) {
			if (a == b) {
				return a;
			}
			return new State(a, b);
		}
	}
}

