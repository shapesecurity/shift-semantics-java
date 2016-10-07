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
package com.shapesecurity.shift.semantics.visitor;

import com.shapesecurity.functional.Pair;
import com.shapesecurity.functional.data.HashTable;
import com.shapesecurity.functional.data.ImmutableList;
import com.shapesecurity.functional.data.Maybe;
import com.shapesecurity.functional.data.Monoid;
import com.shapesecurity.shift.ast.BreakStatement;
import com.shapesecurity.shift.ast.ContinueStatement;
import com.shapesecurity.shift.ast.DoWhileStatement;
import com.shapesecurity.shift.ast.ForInStatement;
import com.shapesecurity.shift.ast.ForOfStatement;
import com.shapesecurity.shift.ast.ForStatement;
import com.shapesecurity.shift.ast.LabeledStatement;
import com.shapesecurity.shift.ast.Module;
import com.shapesecurity.shift.ast.Node;
import com.shapesecurity.shift.ast.Script;
import com.shapesecurity.shift.ast.SwitchStatement;
import com.shapesecurity.shift.ast.SwitchStatementWithDefault;
import com.shapesecurity.shift.ast.TryFinallyStatement;
import com.shapesecurity.shift.ast.WhileStatement;
import com.shapesecurity.shift.visitor.Director;
import com.shapesecurity.shift.visitor.MonoidalReducer;
import org.jetbrains.annotations.NotNull;

// Almost identical to JumpReducer, but also tracks the number of Finally statements that a jump exits. Try-catch statements with no syntactic finally are considered to have empty finally blocks.
// Gives a map from break/continue statements to the statement/loop broken. For labelled statements, map goes to the body of the statement, not the LabelledStatement
// Relies on the AST being valid: in particular, does not check that labelled continues are breaking loops rather than just statements.
// TODO could be waaaaay more typesafe than a map from nodes to nodes.
public class FinallyJumpReducer extends MonoidalReducer<FinallyJumpReducer.State> {
	public static final FinallyJumpReducer INSTANCE = new FinallyJumpReducer();

	private FinallyJumpReducer() {
		super(new StateMonoid());
	}

	public static HashTable<Node, Pair<Node, Integer>> extract(@NotNull FinallyJumpReducer.State state) {
		assert state.unlabelledBreaks.length == 0;
		assert state.labelledBreaks.length == 0;
		assert state.unlabelledContinues.length == 0;
		assert state.labelledContinues.length == 0;
		return state.knownJumps;
	}

	@NotNull
	public static HashTable<Node, Pair<Node, Integer>> analyze(@NotNull Script script) {
		return FinallyJumpReducer.extract(Director.reduceScript(INSTANCE, script));
	}

	@NotNull
	public static HashTable<Node, Pair<Node, Integer>> analyze(@NotNull Module module) {
		return FinallyJumpReducer.extract(Director.reduceModule(INSTANCE, module));
	}


	@NotNull
	private State loopHelper(@NotNull Node loopNode, @NotNull State body) { // no labels in loop bounds
		HashTable<Node, Pair<Node, Integer>> knownJumps = body.knownJumps;
		knownJumps = body.unlabelledBreaks.foldLeft((table, kv) -> table.put(kv.left, new Pair<>(loopNode, kv.right)), knownJumps);
		knownJumps = body.unlabelledContinues.foldLeft((table, kv) -> table.put(kv.left, new Pair<>(loopNode, kv.right)), knownJumps);
		return new State(
			HashTable.emptyUsingIdentity(),
			body.labelledBreaks,
			HashTable.emptyUsingIdentity(),
			body.labelledContinues,
			knownJumps
		);
	}

	@NotNull
	private State switchHelper(@NotNull Node switchNode, @NotNull State body) { // no labels in discriminant
		return new State(
			HashTable.emptyUsingIdentity(),
			body.labelledBreaks,
			body.unlabelledContinues,
			body.labelledContinues,
			body.unlabelledBreaks.foldLeft((table, kv) -> table.put(kv.left, new Pair<>(switchNode, kv.right)), body.knownJumps)
		);
	}

	@NotNull
	@Override
	public State reduceBreakStatement(@NotNull BreakStatement node) {
		if (node.label.isJust()) {
			String label = node.label.fromJust();
			return new State(
				HashTable.emptyUsingIdentity(),
				HashTable.<String, ImmutableList<Pair<BreakStatement, Integer>>>emptyUsingEquality().put(
					label,
					ImmutableList.of(new Pair<>(node, 0))
				),
				HashTable.emptyUsingIdentity(),
				HashTable.emptyUsingEquality(),
				HashTable.emptyUsingIdentity()
			);
		} else {
			return new State(
				HashTable.<BreakStatement, Integer>emptyUsingIdentity().put(node, 0),
				HashTable.emptyUsingEquality(),
				HashTable.emptyUsingIdentity(),
				HashTable.emptyUsingEquality(),
				HashTable.emptyUsingIdentity()
			);
		}
	}

	@NotNull
	@Override
	public State reduceContinueStatement(@NotNull ContinueStatement node) {
		if (node.label.isJust()) {
			String label = node.label.fromJust();
			return new State(
				HashTable.emptyUsingIdentity(),
				HashTable.emptyUsingEquality(),
				HashTable.emptyUsingIdentity(),
				HashTable.<String, ImmutableList<Pair<ContinueStatement, Integer>>>emptyUsingEquality().put(
					label,
					ImmutableList.of(new Pair<>(node, 0))
				),
				HashTable.emptyUsingIdentity()
			);
		} else {
			return new State(
				HashTable.emptyUsingIdentity(),
				HashTable.emptyUsingEquality(),
				HashTable.<ContinueStatement, Integer>emptyUsingIdentity().put(node, 0),
				HashTable.emptyUsingEquality(),
				HashTable.emptyUsingIdentity()
			);
		}
	}

	@NotNull
	@Override
	public State reduceDoWhileStatement(@NotNull DoWhileStatement node, @NotNull State body, @NotNull State test) {
		return loopHelper(node, super.reduceDoWhileStatement(node, body, test));
	}

	@NotNull
	@Override
	public State reduceForInStatement(
		@NotNull ForInStatement node, @NotNull State left, @NotNull State right, @NotNull State body
	) {
		return loopHelper(node, super.reduceForInStatement(node, left, right, body));
	}

	@NotNull
	@Override
	public State reduceForOfStatement(
		@NotNull ForOfStatement node, @NotNull State left, @NotNull State right, @NotNull State body
	) {
		return loopHelper(node, super.reduceForOfStatement(node, left, right, body));
	}

	@NotNull
	@Override
	public State reduceForStatement(
		@NotNull ForStatement node, @NotNull Maybe<State> init, @NotNull Maybe<State> test, @NotNull Maybe<State> update,
		@NotNull State body
	) {
		return loopHelper(node, super.reduceForStatement(node, init, test, update, body));
	}

	@NotNull
	@Override
	public State reduceLabeledStatement(@NotNull LabeledStatement node, @NotNull State body) {
		ImmutableList<Pair<BreakStatement, Integer>> newBreaks = body.labelledBreaks.get(node.label).orJust(ImmutableList.empty());
		ImmutableList<Pair<ContinueStatement, Integer>> newContinues =
			body.labelledContinues.get(node.label).orJust(ImmutableList.empty());
		HashTable<String, ImmutableList<Pair<BreakStatement, Integer>>> labelledBreaks = body.labelledBreaks.remove(node.label);
		HashTable<String, ImmutableList<Pair<ContinueStatement, Integer>>> labelledContinues =
			body.labelledContinues.remove(node.label);

		HashTable<Node, Pair<Node, Integer>> knownJumps = body.knownJumps;
		knownJumps = newBreaks.foldLeft((table, kv) -> table.put(kv.left, new Pair<>(node.body, kv.right)), knownJumps);
		knownJumps = newContinues.foldLeft((table, kv) -> table.put(kv.left, new Pair<>(node.body, kv.right)), knownJumps);

		return new State(
			body.unlabelledBreaks,
			labelledBreaks,
			body.unlabelledContinues,
			labelledContinues,
			knownJumps
		);
	}


	@NotNull
	@Override
	public State reduceSwitchStatement(
		@NotNull SwitchStatement node, @NotNull State discriminant, @NotNull ImmutableList<State> cases
	) {
		return switchHelper(node, super.reduceSwitchStatement(node, discriminant, cases));
	}

	@NotNull
	@Override
	public State reduceSwitchStatementWithDefault(
		@NotNull SwitchStatementWithDefault node, @NotNull State discriminant, @NotNull ImmutableList<State> preDefaultCases,
		@NotNull State defaultCase, @NotNull ImmutableList<State> postDefaultCases
	) {
		return switchHelper(
			node,
			super.reduceSwitchStatementWithDefault(node, discriminant, preDefaultCases, defaultCase, postDefaultCases)
		);
	}


	@NotNull
	@Override
	public State reduceTryFinallyStatement(
		@NotNull TryFinallyStatement node, @NotNull State block, @NotNull Maybe<State> catchClause, @NotNull State finalizer
	) {
		return super.reduceTryFinallyStatement(node, block, catchClause, finalizer.incrementFinalizers());
	}

	@NotNull
	@Override
	public State reduceWhileStatement(@NotNull WhileStatement node, @NotNull State test, @NotNull State body) {
		return loopHelper(node, super.reduceWhileStatement(node, test, body));
	}


	public static final class State {
		@NotNull
		private final HashTable<BreakStatement, Integer> unlabelledBreaks;
		@NotNull
		private final HashTable<String, ImmutableList<Pair<BreakStatement, Integer>>> labelledBreaks;
		@NotNull
		private final HashTable<ContinueStatement, Integer> unlabelledContinues;
		@NotNull
		private final HashTable<String, ImmutableList<Pair<ContinueStatement, Integer>>> labelledContinues;
		@NotNull
		private final HashTable<Node, Pair<Node, Integer>> knownJumps;

		public State(
			@NotNull HashTable<BreakStatement, Integer> unlabelledBreaks,
			@NotNull HashTable<String, ImmutableList<Pair<BreakStatement, Integer>>> labelledBreaks,
			@NotNull HashTable<ContinueStatement, Integer> unlabelledContinues,
			@NotNull HashTable<String, ImmutableList<Pair<ContinueStatement, Integer>>> labelledContinues,
			@NotNull HashTable<Node, Pair<Node, Integer>> knownJumps
		) {
			this.unlabelledBreaks = unlabelledBreaks;
			this.labelledBreaks = labelledBreaks;
			this.unlabelledContinues = unlabelledContinues;
			this.labelledContinues = labelledContinues;
			this.knownJumps = knownJumps;
		}

		public State() {
			this.unlabelledBreaks = HashTable.emptyUsingIdentity();
			this.labelledBreaks = HashTable.emptyUsingEquality();
			this.unlabelledContinues = HashTable.emptyUsingIdentity();
			this.labelledContinues = HashTable.emptyUsingEquality();
			this.knownJumps = HashTable.emptyUsingIdentity();
		}

		public State(@NotNull State a, @NotNull State b) {
			this.unlabelledBreaks = a.unlabelledBreaks.merge(b.unlabelledBreaks);
			this.labelledBreaks = a.labelledBreaks.merge(b.labelledBreaks, ImmutableList::append);
			this.unlabelledContinues = a.unlabelledContinues.merge(b.unlabelledContinues);
			this.labelledContinues = a.labelledContinues.merge(b.labelledContinues, ImmutableList::append);
			this.knownJumps = a.knownJumps.merge(b.knownJumps);
		}

		public State incrementFinalizers() {
			return new State(
				this.unlabelledBreaks.map(x -> x + 1),
				this.labelledBreaks.map(l -> l.map(p -> new Pair<>(p.left, p.right + 1))),
				this.unlabelledContinues.map(x -> x + 1),
				this.labelledContinues.map(l -> l.map(p -> new Pair<>(p.left, p.right + 1))),
				this.knownJumps
			);
		}
	}

	private static final class StateMonoid implements Monoid<State> {
		@Override
		@NotNull
		public State identity() {
			return new State();
		}

		@Override
		@NotNull
		public State append(State a, State b) {
			if (a == b) {
				return a;
			}
			return new State(a, b);
		}
	}
}

