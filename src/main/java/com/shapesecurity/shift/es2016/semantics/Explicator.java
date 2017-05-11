/*
 * Copyright 2016 Shape Security, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shapesecurity.shift.es2016.semantics;

import com.shapesecurity.functional.F;
import com.shapesecurity.functional.Pair;
import com.shapesecurity.functional.data.Either;
import com.shapesecurity.functional.data.HashTable;
import com.shapesecurity.functional.data.ImmutableList;
import com.shapesecurity.functional.data.Maybe;
import com.shapesecurity.shift.es2016.ast.ArrayExpression;
import com.shapesecurity.shift.es2016.ast.AssignmentExpression;
import com.shapesecurity.shift.es2016.ast.AssignmentTarget;
import com.shapesecurity.shift.es2016.ast.AssignmentTargetIdentifier;
import com.shapesecurity.shift.es2016.ast.BinaryExpression;
import com.shapesecurity.shift.es2016.ast.BindingIdentifier;
import com.shapesecurity.shift.es2016.ast.BlockStatement;
import com.shapesecurity.shift.es2016.ast.BreakStatement;
import com.shapesecurity.shift.es2016.ast.CallExpression;
import com.shapesecurity.shift.es2016.ast.CompoundAssignmentExpression;
import com.shapesecurity.shift.es2016.ast.ComputedMemberAssignmentTarget;
import com.shapesecurity.shift.es2016.ast.ComputedMemberExpression;
import com.shapesecurity.shift.es2016.ast.ComputedPropertyName;
import com.shapesecurity.shift.es2016.ast.ConditionalExpression;
import com.shapesecurity.shift.es2016.ast.ContinueStatement;
import com.shapesecurity.shift.es2016.ast.DataProperty;
import com.shapesecurity.shift.es2016.ast.DebuggerStatement;
import com.shapesecurity.shift.es2016.ast.Directive;
import com.shapesecurity.shift.es2016.ast.DoWhileStatement;
import com.shapesecurity.shift.es2016.ast.EmptyStatement;
import com.shapesecurity.shift.es2016.ast.Expression;
import com.shapesecurity.shift.es2016.ast.ExpressionStatement;
import com.shapesecurity.shift.es2016.ast.ExpressionSuper;
import com.shapesecurity.shift.es2016.ast.ForInStatement;
import com.shapesecurity.shift.es2016.ast.ForStatement;
import com.shapesecurity.shift.es2016.ast.FormalParameters;
import com.shapesecurity.shift.es2016.ast.FunctionBody;
import com.shapesecurity.shift.es2016.ast.FunctionDeclaration;
import com.shapesecurity.shift.es2016.ast.FunctionExpression;
import com.shapesecurity.shift.es2016.ast.Getter;
import com.shapesecurity.shift.es2016.ast.IdentifierExpression;
import com.shapesecurity.shift.es2016.ast.IfStatement;
import com.shapesecurity.shift.es2016.ast.LabeledStatement;
import com.shapesecurity.shift.es2016.ast.LiteralBooleanExpression;
import com.shapesecurity.shift.es2016.ast.LiteralInfinityExpression;
import com.shapesecurity.shift.es2016.ast.LiteralNullExpression;
import com.shapesecurity.shift.es2016.ast.LiteralNumericExpression;
import com.shapesecurity.shift.es2016.ast.LiteralRegExpExpression;
import com.shapesecurity.shift.es2016.ast.LiteralStringExpression;
import com.shapesecurity.shift.es2016.ast.MemberExpression;
import com.shapesecurity.shift.es2016.ast.Module;
import com.shapesecurity.shift.es2016.ast.NewExpression;
import com.shapesecurity.shift.es2016.ast.ObjectExpression;
import com.shapesecurity.shift.es2016.ast.ObjectProperty;
import com.shapesecurity.shift.es2016.ast.PropertyName;
import com.shapesecurity.shift.es2016.ast.ReturnStatement;
import com.shapesecurity.shift.es2016.ast.Script;
import com.shapesecurity.shift.es2016.ast.Setter;
import com.shapesecurity.shift.es2016.ast.SpreadElementExpression;
import com.shapesecurity.shift.es2016.ast.Statement;
import com.shapesecurity.shift.es2016.ast.StaticMemberAssignmentTarget;
import com.shapesecurity.shift.es2016.ast.StaticMemberExpression;
import com.shapesecurity.shift.es2016.ast.StaticPropertyName;
import com.shapesecurity.shift.es2016.ast.Super;
import com.shapesecurity.shift.es2016.ast.SwitchCase;
import com.shapesecurity.shift.es2016.ast.SwitchStatementWithDefault;
import com.shapesecurity.shift.es2016.ast.ThisExpression;
import com.shapesecurity.shift.es2016.ast.ThrowStatement;
import com.shapesecurity.shift.es2016.ast.TryCatchStatement;
import com.shapesecurity.shift.es2016.ast.TryFinallyStatement;
import com.shapesecurity.shift.es2016.ast.UnaryExpression;
import com.shapesecurity.shift.es2016.ast.UpdateExpression;
import com.shapesecurity.shift.es2016.ast.VariableDeclaration;
import com.shapesecurity.shift.es2016.ast.VariableDeclarationExpression;
import com.shapesecurity.shift.es2016.ast.VariableDeclarationStatement;
import com.shapesecurity.shift.es2016.ast.WhileStatement;
import com.shapesecurity.shift.es2016.ast.WithStatement;
import com.shapesecurity.shift.es2016.ast.operators.UpdateOperator;
import com.shapesecurity.shift.es2016.scope.Declaration;
import com.shapesecurity.shift.es2016.scope.GlobalScope;
import com.shapesecurity.shift.es2016.scope.Scope;
import com.shapesecurity.shift.es2016.scope.ScopeAnalyzer;
import com.shapesecurity.shift.es2016.scope.ScopeLookup;
import com.shapesecurity.shift.es2016.scope.Variable;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.BinaryOperation;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.BinaryOperator;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.Equality;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.FloatMath;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.In;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.InstanceOf;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.IntMath;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.RelationalComparison;
import com.shapesecurity.shift.es2016.semantics.asg.BlockWithValue;
import com.shapesecurity.shift.es2016.semantics.asg.DeleteGlobalProperty;
import com.shapesecurity.shift.es2016.semantics.asg.GlobalReference;
import com.shapesecurity.shift.es2016.semantics.asg.Halt;
import com.shapesecurity.shift.es2016.semantics.asg.IfElse;
import com.shapesecurity.shift.es2016.semantics.asg.Keys;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralBoolean;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralEmptyObject;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralFunction;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralInfinity;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralNull;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralNumber;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralRegExp;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralString;
import com.shapesecurity.shift.es2016.semantics.asg.LocalReference;
import com.shapesecurity.shift.es2016.semantics.asg.MemberAccess;
import com.shapesecurity.shift.es2016.semantics.asg.MemberAssignment;
import com.shapesecurity.shift.es2016.semantics.asg.MemberAssignmentProperty;
import com.shapesecurity.shift.es2016.semantics.asg.MemberCall;
import com.shapesecurity.shift.es2016.semantics.asg.MemberDefinition;
import com.shapesecurity.shift.es2016.semantics.asg.New;
import com.shapesecurity.shift.es2016.semantics.asg.Node;
import com.shapesecurity.shift.es2016.semantics.asg.NodeWithValue;
import com.shapesecurity.shift.es2016.semantics.asg.Return;
import com.shapesecurity.shift.es2016.semantics.asg.SwitchStatement;
import com.shapesecurity.shift.es2016.semantics.asg.TemporaryReference;
import com.shapesecurity.shift.es2016.semantics.asg.This;
import com.shapesecurity.shift.es2016.semantics.asg.Throw;
import com.shapesecurity.shift.es2016.semantics.asg.TryCatchFinally;
import com.shapesecurity.shift.es2016.semantics.asg.TypeCoercionNumber;
import com.shapesecurity.shift.es2016.semantics.asg.TypeCoercionString;
import com.shapesecurity.shift.es2016.semantics.asg.TypeofGlobal;
import com.shapesecurity.shift.es2016.semantics.asg.UnaryOperation.Negation;
import com.shapesecurity.shift.es2016.semantics.asg.UnaryOperation.Not;
import com.shapesecurity.shift.es2016.semantics.asg.UnaryOperation.Typeof;
import com.shapesecurity.shift.es2016.semantics.asg.Void;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.Logic;
import com.shapesecurity.shift.es2016.semantics.asg.Block;
import com.shapesecurity.shift.es2016.semantics.asg.Break;
import com.shapesecurity.shift.es2016.semantics.asg.BreakTarget;
import com.shapesecurity.shift.es2016.semantics.asg.Call;
import com.shapesecurity.shift.es2016.semantics.asg.DeleteProperty;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralEmptyArray;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralUndefined;
import com.shapesecurity.shift.es2016.semantics.asg.Loop;
import com.shapesecurity.shift.es2016.semantics.asg.RequireObjectCoercible;
import com.shapesecurity.shift.es2016.semantics.asg.UnaryOperation.BitwiseNot;
import com.shapesecurity.shift.es2016.semantics.asg.UnaryOperation.VoidOp;
import com.shapesecurity.shift.es2016.semantics.asg.VariableAssignment;
import com.shapesecurity.shift.es2016.semantics.visitor.FinallyJumpReducer;

import javax.annotation.Nonnull;

import java.util.ArrayList;

public class Explicator {
	@Nonnull
	final GlobalScope scope;
	@Nonnull
	final Either<Script, Module> program;
	@Nonnull
	final HashTable<com.shapesecurity.shift.es2016.ast.Node, Pair<com.shapesecurity.shift.es2016.ast.Node, Integer>> jumpMap;
	@Nonnull
	final ScopeLookup scopeLookup;
	@Nonnull
	HashTable<com.shapesecurity.shift.es2016.ast.Node, Pair<BreakTarget, Maybe<BreakTarget>>> targets = HashTable.emptyUsingEquality();
	// map from AST loops and labelled statement to their corresponding Target nodes. Loops have an outer and an inner, for break and continue respectively.
	@Nonnull
	ImmutableList<ImmutableList<Variable>> temporaries = ImmutableList.of(ImmutableList.empty());
	// Stack of function-local temporaries. Whenever you begin explicating a function, push a new empty list; when you finish, pop it. TODO this is a somewhat hacky way of accomplishing this. It would be better to be intraproceedural or something.

	// todo runtime errors for with, direct eval
	Explicator(@Nonnull Script script) {
		this.program = Either.left(script);
		this.scope = ScopeAnalyzer.analyze(script);
		this.jumpMap = FinallyJumpReducer.analyze(script);
		this.scopeLookup = new ScopeLookup(this.scope);
	}

	Explicator(@Nonnull Module module) {
		this.program = Either.right(module);
		this.scope = ScopeAnalyzer.analyze(module);
		this.jumpMap = FinallyJumpReducer.analyze(module);
		this.scopeLookup = new ScopeLookup(this.scope);
	}

	@Nonnull
	public static Semantics deriveSemantics(@Nonnull Script script) {
		Explicator exp = new Explicator(script);
		Node result = exp.explicate();
		ImmutableList<Variable> maybeGlobals = exp.functionVariablesHelper(script);
		ImmutableList<Variable> scriptLocals =
			maybeGlobals.filter(x -> !exp.scopeLookup.isGlobal(x)).append(exp.temporaries.maybeHead().fromJust());
		ImmutableList<String> scriptVarDecls =
			maybeGlobals.filter(x -> exp.scopeLookup.isGlobal(x) && x.declarations.isNotEmpty()).map(x -> x.name);
		return new Semantics(result, scriptLocals, scriptVarDecls);
	}

	@Nonnull
	public static Semantics deriveSemantics(@Nonnull Module module) {
		Explicator exp = new Explicator(module);
		Node result = exp.explicate();
		ImmutableList<Variable> scriptLocals =
			exp.functionVariablesHelper(module)
				.filter(x -> !exp.scopeLookup.isGlobal(x))
				.append(exp.temporaries.maybeHead().fromJust());
		return new Semantics(result, scriptLocals, ImmutableList.empty());
	}

	Node explicate() {
		return this.program.either(
			script -> explicateBody(script.statements, isStrict(script.directives)),
			module -> explicateBody(module.items.map(i -> (Statement) i), true)
		);
	}

	ImmutableList<Variable> simpleParamsHelper(@Nonnull FormalParameters params) {
		assert params.rest.isNothing();
		return params.items.map(
			b -> scopeLookup.findVariableDeclaredBy((BindingIdentifier) b).fromJust()
		);
	}

	boolean isStrict(@Nonnull ImmutableList<Directive> directives) {
		return directives.find(d -> d.rawValue.equals("use strict")).isJust();
	}

	@Nonnull
	NodeWithValue variableAssignmentHelper(
			@Nonnull Either<GlobalReference, LocalReference> ref, @Nonnull NodeWithValue rhs, boolean strict
	) { // handles const-ness correctly. not to be used with variable or function declarations.
		if (ref.isRight()) {
			Variable variable = ref.right().fromJust().variable;
			if (variable.declarations.exists(decl -> decl.kind == Declaration.Kind.Const || decl.kind == Declaration.Kind.FunctionExpressionName)) { // note that this is an AssignmentExpression or similar, not a VariableDeclaration, so this is *not* the initialization of the variable, i.e., it is certainly an improper write to a constant variable.
				assert variable.declarations.length == 1;
				// TODO maybe warn about writing to a constant variable?
				if (strict || variable.declarations.maybeHead()
					.fromJust().kind == Declaration.Kind.Const) { // writing to const variables is always a TypeError, even in strict mode: https://github.com/tc39/test262/pull/430#issuecomment-139423863
					return new BlockWithValue(
						ImmutableList.of(rhs, new Throw(new New(new GlobalReference("TypeError"), ImmutableList.empty()))),
						// TODO throw an actual TypeError, not whatever the global TypeError value happens to be at the moment (it is writable)
						LiteralUndefined.INSTANCE
					);
				} else {
					return rhs; // silently fail to write to function-expression names
				}
			}
		}
		return new VariableAssignment(
			ref,
			rhs,
			strict
		);
	}

	// given a FunctionDeclaration or FunctionExpression, find all the variables created in it or its children not crossing function boundaries
	// which is to say, all variables which calling this function recursively might shadow

	ImmutableList<Variable> functionVariablesHelper(@Nonnull com.shapesecurity.shift.es2016.ast.Node node) {
		return scopeLookup.findScopeFor(node).maybe(ImmutableList.<Variable>empty(), this::functionVariablesHelper);
	}

	// helper for the above. find variables in the given scope and its descendants, up to function boundaries
	// TODO should maybe be a Scope or ScopeLookup method (but probably not)
	ImmutableList<Variable> functionVariablesHelper(@Nonnull Scope scope) {
		ImmutableList<Variable> initial = ImmutableList.from(new ArrayList<>(scope.variables()));
		if (scope.type == Scope.Type.FunctionName) {
			assert scope.children.length == 1; // in ES5, anyway...
			assert scope.children.maybeHead().fromJust().type == Scope.Type.Function;
			return initial.append(this.functionVariablesHelper(scope.children.maybeHead().fromJust()));
		}
		// this will need to include parameter scope in ES6
		return initial.append(scope.children.flatMap(s ->
			s.type == Scope.Type.Function || s.type == Scope.Type.FunctionName
				? ImmutableList.empty()
				: this.functionVariablesHelper(s)
		));
	}

	@Nonnull
	Either<GlobalReference, LocalReference> refHelper(AssignmentTargetIdentifier assignmentTargetIdentifier) {
		Variable variable = scopeLookup.findVariableReferencedBy(assignmentTargetIdentifier);
		return scopeLookup.isGlobal(variable) ?
				Either.left(new GlobalReference(assignmentTargetIdentifier.name)) :
				Either.right(new LocalReference(variable));
	}

	@Nonnull
	Either<GlobalReference, LocalReference> refHelper(BindingIdentifier bindingIdentifier) {
		Variable variable = scopeLookup.findVariableReferencedBy(bindingIdentifier).fromJust();
		return scopeLookup.isGlobal(variable) ?
			Either.left(new GlobalReference(bindingIdentifier.name)) :
			Either.right(new LocalReference(variable));
	}

	@Nonnull
	Either<GlobalReference, LocalReference> refHelper(@Nonnull IdentifierExpression identifierExpression) {
		return scopeLookup.isGlobal(scopeLookup.findVariableReferencedBy(identifierExpression)) ?
			Either.left(new GlobalReference(identifierExpression.name)) :
			Either.right(new LocalReference(scopeLookup.findVariableReferencedBy(identifierExpression)));
	}

	@Nonnull
	Either<GlobalReference, LocalReference> refHelper(@Nonnull Variable variable) {
		return scopeLookup.isGlobal(variable) ?
			Either.left(new GlobalReference(variable.name)) :
			Either.right(new LocalReference(variable));
	}

	// for function, block, caseblock, and script bodies, not arbitrary lists of statements. Performs hoisting.
	// TODO ensure that function declarations in switch statements get hoisted to the top of the switch, per 13.12.6 (I think)
	@Nonnull
	Block explicateBody(@Nonnull ImmutableList<Statement> statements, boolean strict) {
		ImmutableList<Node> res = ImmutableList.empty();
		// hoist functions
		for (Statement s : statements) {
			if (s instanceof FunctionDeclaration) {
				FunctionDeclaration functionDeclaration = (FunctionDeclaration) s;
				Variable variable = scopeLookup.findVariablesForFuncDecl(functionDeclaration).left;
				Either<GlobalReference, LocalReference> ref = refHelper(variable);

				Scope myScope = scopeLookup.findScopeFor(functionDeclaration).fromJust();
				Maybe<Variable> name = Maybe.empty();
				ImmutableList<Variable> parameters = simpleParamsHelper(functionDeclaration.params);
				res = res.cons(new VariableAssignment(
					ref,
					explicateGeneralFunction(name, myScope, parameters, functionDeclaration.body, strict),
					strict
				));
			}
		}
		// then evaluate everything
		res = res.reverse().append(statements.map(s -> explicateStatement(s, strict)));
		return new Block(res);
	}

	@Nonnull
	LiteralFunction explicateGeneralFunction(
		@Nonnull Maybe<Variable> name, @Nonnull Scope scope, @Nonnull ImmutableList<Variable> parameters,
		@Nonnull FunctionBody functionBody, boolean strict
	) {
		Maybe<Variable> arguments = Maybe.empty();
		if (scope.type == Scope.Type.FunctionName) {
			scope = scope.children.maybeHead().fromJust();
		}
		for (Variable v : scope.variables()) {
			if (v.name.equals("arguments")) {
				arguments = Maybe.of(v);
				break;
			}
		}
		strict = strict || isStrict(functionBody.directives);
		this.temporaries = this.temporaries.cons(ImmutableList.empty());
		Block body = explicateBody(functionBody.statements, strict);
		ImmutableList<Variable> fnTemporaries = this.temporaries.maybeHead().fromJust();
		this.temporaries = this.temporaries.maybeTail().fromJust();
		ImmutableList<Variable> locals = fnTemporaries.append(functionVariablesHelper(scope)); // todo concatlists, I guess
		// TODO capture may have duplicate entries: `function g(x){function h(){x+x}}`
		ImmutableList<Variable> captured = scope.through.entries()
			.flatMap(p -> p.right.map(r -> {
				if (r.node instanceof BindingIdentifier) {
					return scopeLookup.findVariableReferencedBy((BindingIdentifier) r.node).fromJust();
				} else if (r.node instanceof AssignmentTargetIdentifier) {
					return scopeLookup.findVariableReferencedBy((AssignmentTargetIdentifier) r.node);
				} else {
					return scopeLookup.findVariableReferencedBy((IdentifierExpression) r.node);
				}
			}))
			.filter(v -> !scopeLookup.isGlobal(v)); // everything needing capture
		return new LiteralFunction(name, arguments, parameters, locals, captured, body, strict);
	}

	@Nonnull
	Node explicateStatement(@Nonnull Statement statement, boolean strict) {
		if (statement instanceof BlockStatement) {
			return explicateBody(((BlockStatement) statement).block.statements, strict);
		} else if (statement instanceof BreakStatement) {
			Pair<com.shapesecurity.shift.es2016.ast.Node, Integer> _break = jumpMap.get(statement).fromJust();
			return new Break(targets.get(_break.left).fromJust().left, _break.right);
		} else if (statement instanceof ContinueStatement) {
			Pair<com.shapesecurity.shift.es2016.ast.Node, Integer> _break = jumpMap.get(statement).fromJust();
			return new Break(targets.get(_break.left).fromJust().right.fromJust(), _break.right);
		} else if (statement instanceof DebuggerStatement) {
			return Void.INSTANCE;
		} else if (statement instanceof DoWhileStatement) { // exactly the same as WhileStatement, except that the test is last instead of first
			DoWhileStatement doWhileStatement = (DoWhileStatement) statement;
			BreakTarget outerTarget = new BreakTarget();
			BreakTarget innerTarget = new BreakTarget();
			targets = targets.put(statement, new Pair<>(outerTarget, Maybe.of(innerTarget)));
			Break breakNode = new Break(outerTarget, 0);
			Block body = new Block(ImmutableList.<Node>of(
				explicateStatement(doWhileStatement.body, strict),
				innerTarget,
				new IfElse(
					explicateExpressionReturningValue(doWhileStatement.test, strict),
					new Block(Void.INSTANCE),
					new Block(breakNode)
				)
			));
			Loop loop = new Loop(body);
			return new Block(ImmutableList.of(loop, outerTarget));
		} else if (statement instanceof ExpressionStatement) {
			return explicateExpressionDiscardingValue(((ExpressionStatement) statement).expression, strict);
		} else if (statement instanceof EmptyStatement) {
			return Void.INSTANCE;
		} else if (statement instanceof ForInStatement) {
			/*
			Desugaring:
			for(x in o) {
				...
			}

			becomes

			__o = o;
			__t = keys(__o);
			__i = 0;
			while(__i < __t.length) {
				if(__t[__i] in __o) {
					x = __t[__i];
					...
				}
				__i = __i + 1;
			}
			 */
			ForInStatement forInStatement = (ForInStatement) statement;
			BreakTarget outerTarget = new BreakTarget();
			BreakTarget innerTarget = new BreakTarget();
			targets = targets.put(statement, new Pair<>(outerTarget, Maybe.of(innerTarget)));
			Break breakNode = new Break(outerTarget, 0);

			Either<GlobalReference, LocalReference> reference;
			boolean isDeclaration;
			if (forInStatement.left instanceof VariableDeclaration) {
				VariableDeclaration variableDeclaration = (VariableDeclaration) forInStatement.left;
				assert variableDeclaration.declarators.length == 1; // TODO maybe remove this
				// assumes ES5
				reference = refHelper((BindingIdentifier) variableDeclaration.declarators.maybeHead().fromJust().binding);
				isDeclaration = true;
			} else if (forInStatement.left instanceof AssignmentTargetIdentifier) {
				reference = refHelper((AssignmentTargetIdentifier) forInStatement.left);
				isDeclaration = false;
			} else {
				throw new UnsupportedOperationException("ES6 not supported: " + forInStatement.left.getClass().getSimpleName());
			}

			return makeUnvaluedTemporary(object -> makeUnvaluedTemporary(counter -> makeUnvaluedTemporary(keys -> {
				Block body = new Block(ImmutableList.<Node>of(
					new IfElse(
						new RelationalComparison(
							RelationalComparison.Operator.LessThan,
							counter,
							new MemberAccess(keys, new LiteralString("length"))
						),
						new Block(Void.INSTANCE),
						new Block(breakNode)
					))
					.append(ImmutableList.of(
						new IfElse(
							new In(new MemberAccess(keys, counter), object),
							new Block(ImmutableList.of(
								isDeclaration ?
									new VariableAssignment(reference, new MemberAccess(keys, counter), strict) :
									variableAssignmentHelper(reference, new MemberAccess(keys, counter), strict),
								explicateStatement(forInStatement.body, strict)
							)),
							new Block(Void.INSTANCE)
						),
						innerTarget,
						new VariableAssignment(
							counter,
							new FloatMath(FloatMath.Operator.Plus, counter, new LiteralNumber(1)),
							false
						)
					)));
				Loop loop = new Loop(body);
				return new Block(ImmutableList.of(
					new VariableAssignment(object, explicateExpressionReturningValue(forInStatement.right, strict), false),
					new VariableAssignment(keys, new Keys(object), false),
					new VariableAssignment(counter, new LiteralNumber(0), false),
					loop,
					outerTarget
				));
			})));
		} else if (statement instanceof ForStatement) {
			ForStatement forStatement = (ForStatement) statement;
			BreakTarget outerTarget = new BreakTarget();
			BreakTarget innerTarget = new BreakTarget();
			targets = targets.put(statement, new Pair<>(outerTarget, Maybe.of(innerTarget)));
			Break breakNode = new Break(outerTarget, 0);

			Block body = new Block(forStatement.test.map(t -> ((ImmutableList<Node>) ImmutableList.<Node>of(
				new IfElse(
					explicateExpressionReturningValue(t, strict),
					new Block(Void.INSTANCE),
					new Block(breakNode)
				)))).orJust(ImmutableList.empty())
				.append(ImmutableList.of(
					explicateStatement(forStatement.body, strict),
					innerTarget
				))
				.append(forStatement.update.map(e -> ((ImmutableList<Node>) ImmutableList.of((Node) explicateExpressionDiscardingValue(
					e,
					strict
				)))).orJust(ImmutableList.empty())));
			Loop loop = new Loop(body);
			return new Block(ImmutableList.of(forStatement.init.map(i -> (Node) new Block(ImmutableList.of(
				explicateVariableDeclarationExpression(i, strict),
				loop
			))).orJust(loop), outerTarget));
		} else if (statement instanceof FunctionDeclaration) {
			// todo maybe warn people about using block-level function declarations.
			FunctionDeclaration functionDeclaration = (FunctionDeclaration) statement;
			Pair<Variable, Maybe<Variable>> functionVariables = scopeLookup.findVariablesForFuncDecl(functionDeclaration);
			if (functionVariables.right.isJust()) {
				// this is from annex B.3.3
				return new VariableAssignment(
					new LocalReference(functionVariables.right.fromJust()),
					new LocalReference(functionVariables.left),
					false
				);
			} else {
				return Void.INSTANCE;
			}
		} else if (statement instanceof IfStatement) {
			IfStatement ifStatement = (IfStatement) statement;
			NodeWithValue test = explicateExpressionReturningValue(ifStatement.test, strict);
			Node consequent = explicateStatement(ifStatement.consequent, strict);
			Node alternate = ifStatement.alternate.map(s -> explicateStatement(s, strict)).orJust(Void.INSTANCE);
			return new IfElse(test, new Block(consequent), new Block(alternate));
		} else if (statement instanceof LabeledStatement) {
			LabeledStatement labeledStatement = (LabeledStatement) statement;
			BreakTarget target = new BreakTarget();
			targets = targets.put(labeledStatement.body, new Pair<>(target, Maybe.empty()));
			return new Block(ImmutableList.of(explicateStatement(labeledStatement.body, strict), target));
		} else if (statement instanceof ReturnStatement) {
			ReturnStatement returnStatement = (ReturnStatement) statement;
			return new Return(
				returnStatement.expression.map(e -> explicateExpressionReturningValue(e, strict))
			);
		} else if (statement instanceof com.shapesecurity.shift.es2016.ast.SwitchStatement) {
			// TODO hoist function declarations. This will require modifying asg SwitchStatements to have a Declarations block of statements, horrifyingly enough. probably also need to fix scope analysis.
			com.shapesecurity.shift.es2016.ast.SwitchStatement switchStatement = (com.shapesecurity.shift.es2016.ast.SwitchStatement) statement;
			BreakTarget target = new BreakTarget();
			targets = targets.put(statement, new Pair<>(target, Maybe.empty()));

			return new Block(ImmutableList.of(
				let(
					explicateExpressionReturningValue(switchStatement.discriminant, strict),
					d -> new Block(new SwitchStatement(
						d,
						switchStatement.cases.map(c -> explicateSwitchCase(c, strict)),
						new Block(ImmutableList.empty()),
						ImmutableList.empty()
					))
				),
				target
			));
		} else if (statement instanceof SwitchStatementWithDefault) {
			// TODO hoist function declarations. This will require modifying asg SwitchStatements to have a Declarations block of statements, horrifyingly enough. probably also need to fix scope analysis.
			SwitchStatementWithDefault switchStatement = (SwitchStatementWithDefault) statement;
			BreakTarget target = new BreakTarget();
			targets = targets.put(statement, new Pair<>(target, Maybe.empty()));

			return new Block(ImmutableList.of(
				let(
					explicateExpressionReturningValue(switchStatement.discriminant, strict),
					d -> new Block(new SwitchStatement(
						d,
						switchStatement.preDefaultCases.map(c -> explicateSwitchCase(c, strict)),
						new Block(switchStatement.defaultCase.consequent.map(c -> explicateStatement(c, strict))),
						switchStatement.postDefaultCases.map(c -> explicateSwitchCase(c, strict))
					))
				),
				target
			));
		} else if (statement instanceof ThrowStatement) {
			ThrowStatement throwStatement = (ThrowStatement) statement;
			return new Throw(
				explicateExpressionReturningValue(throwStatement.expression, strict)
			);
		} else if (statement instanceof TryCatchStatement) {
			TryCatchStatement tryCatchStatement = (TryCatchStatement) statement;
			Variable catchVariable =
				scopeLookup.findVariableDeclaredBy((BindingIdentifier) tryCatchStatement.catchClause.binding).fromJust();
			return new TryCatchFinally(
				explicateBody(tryCatchStatement.body.statements, strict),
				Maybe.of(new Pair<>(catchVariable, explicateBody(tryCatchStatement.catchClause.body.statements, strict))),
				new Block(ImmutableList.empty())
			);
		} else if (statement instanceof TryFinallyStatement) {
			TryFinallyStatement tryFinallyStatement = (TryFinallyStatement) statement;
			Maybe<Pair<Variable, Block>> catchBody = tryFinallyStatement.catchClause.map(c ->
				new Pair<>(
					scopeLookup.findVariableDeclaredBy((BindingIdentifier) c.binding).fromJust(),
					explicateBody(c.body.statements, strict)
				)
			);
			return new TryCatchFinally(
				explicateBody(tryFinallyStatement.body.statements, strict),
				catchBody,
				explicateBody(tryFinallyStatement.finalizer.statements, strict)
			);
		} else if (statement instanceof VariableDeclarationStatement) {
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) statement;
			return explicateVariableDeclaration(variableDeclarationStatement.declaration, strict);
		} else if (statement instanceof WhileStatement) {
			WhileStatement whileStatement = (WhileStatement) statement;
			BreakTarget outerTarget = new BreakTarget();
			BreakTarget innerTarget = new BreakTarget();
			targets = targets.put(statement, new Pair<>(outerTarget, Maybe.of(innerTarget)));
			Break breakNode = new Break(outerTarget, 0);
			Block body = new Block(ImmutableList.<Node>of(
				new IfElse(
					explicateExpressionReturningValue(whileStatement.test, strict),
					new Block(Void.INSTANCE),
					new Block(breakNode)
				),
				explicateStatement(whileStatement.body, strict),
				innerTarget
			));
			Loop loop = new Loop(body);
			return new Block(ImmutableList.of(loop, outerTarget));
		} else if (statement instanceof WithStatement) {
			return Halt.INSTANCE; // TODO maybe warn.
		}
		throw new UnsupportedOperationException("ES6 not supported: " + statement.getClass().getSimpleName());
	}

	@Nonnull
	NodeWithValue explicateExpressionReturningValue(Expression expression, boolean strict) {
		return explicateExpression(expression, true, strict);
	}

	@Nonnull
	NodeWithValue explicateExpressionDiscardingValue(Expression expression, boolean strict) {
		return explicateExpression(expression, false, strict);
	}

	// Here, keepValue is just used for statements like i++, to avoid creating unnecessary temporaries.
	@Nonnull
	NodeWithValue explicateExpression(Expression expression, boolean keepValue, boolean strict) {
		if (expression instanceof ArrayExpression) {
			ArrayExpression arrayExpression = (ArrayExpression) expression;
			if (arrayExpression.elements.exists(Maybe::isNothing)) { // ie, contains hole
				return letWithValue(
					LiteralEmptyArray.INSTANCE,
					arr -> new BlockWithValue(ImmutableList.cons(
						new MemberAssignment(
							arr,
							new LiteralString("length"),
							new LiteralNumber(arrayExpression.elements.length),
							false
						),
						arrayExpression.elements.mapWithIndex((ind, p) -> p.maybe(
							Void.INSTANCE,
							e -> explicateSpreadElementExpression(arr, ind, e, strict)
						))
					), arr)
				);
			} else if (arrayExpression.elements.isEmpty()) {
				return LiteralEmptyArray.INSTANCE;
			}
			return letWithValue(
					LiteralEmptyArray.INSTANCE,
				arr -> new BlockWithValue(
					arrayExpression.elements.mapWithIndex((ind, p) -> p.map(e -> explicateSpreadElementExpression(
						arr,
						ind,
						e,
						strict
					))
						.orJust(LiteralUndefined.INSTANCE)),
					arr
				)
			);
		} else if (expression instanceof AssignmentExpression) {
			AssignmentExpression assignmentExpression = (AssignmentExpression) expression;
			AssignmentTarget binding = assignmentExpression.binding;
			if (binding instanceof StaticMemberAssignmentTarget) {
				StaticMemberAssignmentTarget staticMemberAssignmentTarget = (StaticMemberAssignmentTarget) binding;
				return new MemberAssignment(
					explicateExpressionSuper(staticMemberAssignmentTarget.object, strict),
					new LiteralString(staticMemberAssignmentTarget.property),
					explicateExpressionReturningValue(assignmentExpression.expression, strict),
					strict
				);
			} else if (binding instanceof ComputedMemberAssignmentTarget) {
				ComputedMemberAssignmentTarget computedMemberAssignmentTarget = (ComputedMemberAssignmentTarget) binding;
				return letWithValue(
					// semantics: evaluate obj, evaluate prop, coerce obj to object, coerce prop to string. in that order. TODO find a better way of doing this.
					explicateExpressionSuper(computedMemberAssignmentTarget.object, strict),
					objectRef -> letWithValue(
						explicateExpressionReturningValue(computedMemberAssignmentTarget.expression, strict),
						fieldRef -> new BlockWithValue(
							ImmutableList.of(new RequireObjectCoercible(objectRef)),
							letWithValue(
								new TypeCoercionString(fieldRef),
								prop -> new MemberAssignment(
									objectRef,
									prop,
									explicateExpressionReturningValue(assignmentExpression.expression, strict),
									strict
								)
							)
						)
					)
				);
			} else if (binding instanceof AssignmentTargetIdentifier) {
				AssignmentTargetIdentifier assignmentTargetIdentifier = (AssignmentTargetIdentifier) binding;
				Either<GlobalReference, LocalReference> ref = refHelper(assignmentTargetIdentifier);
				NodeWithValue rhs = explicateExpressionReturningValue(assignmentExpression.expression, strict);
				return variableAssignmentHelper(
					ref,
					rhs,
					strict
				);
			} else {
				throw new UnsupportedOperationException("ES6 not supported: " + binding.getClass().getSimpleName());
			}
		} else if (expression instanceof BinaryExpression) {
			return explicateBinaryExpression((BinaryExpression) expression, strict);
		} else if (expression instanceof CallExpression) {
			CallExpression c = (CallExpression) expression;
			// abort on direct eval. todo maybe warn.
			if (c.callee instanceof IdentifierExpression && (((IdentifierExpression) c.callee).name.equals("eval"))) {
				return Halt.INSTANCE;
			}
			ImmutableList<NodeWithValue> arguments =
				c.arguments.map(a -> explicateExpressionReturningValue((Expression) a, strict));
			if (c.callee instanceof MemberExpression) {
				MemberExpression memberExpression = (MemberExpression) c.callee;
				NodeWithValue field = memberExpression instanceof StaticMemberExpression
						? new LiteralString(((StaticMemberExpression) memberExpression).property)
						: explicateExpressionReturningValue(
							((ComputedMemberExpression) memberExpression).expression,
							strict
						);
				return new MemberCall(explicateExpressionSuper(memberExpression.object, strict), field, arguments);
			}
			NodeWithValue callee = explicateExpressionSuper(c.callee, strict);
			return new Call(Maybe.empty(), callee, arguments);
		} else if (expression instanceof CompoundAssignmentExpression) {
			return explicateCompoundAssignmentExpression((CompoundAssignmentExpression) expression, strict);
		} else if (expression instanceof ComputedMemberExpression) {
			ComputedMemberExpression computedMemberExpression = (ComputedMemberExpression) expression;
			return new MemberAccess(
				explicateExpressionSuper(computedMemberExpression.object, strict),
				explicateExpressionReturningValue(computedMemberExpression.expression, strict)
			);
		} else if (expression instanceof ConditionalExpression) {
			ConditionalExpression conditionalExpression = (ConditionalExpression) expression;
			return makeTemporary(
				// TODO there are several other ways of doing this; the temporary is not necessary if we have proper completion values
				ref -> new BlockWithValue(
					ImmutableList.of(
						new IfElse(
							explicateExpressionReturningValue(conditionalExpression.test, strict),
							new Block(new VariableAssignment(
								ref,
								explicateExpressionReturningValue(conditionalExpression.consequent, strict),
								false
							)),
							new Block(new VariableAssignment(
								ref,
								explicateExpressionReturningValue(conditionalExpression.alternate, strict),
								false
							))
						)),
					ref
				)
			);
		} else if (expression instanceof FunctionExpression) {
			FunctionExpression functionExpression = (FunctionExpression) expression;
			return explicateGeneralFunction(
				functionExpression.name.flatMap(scopeLookup::findVariableDeclaredBy),
				scopeLookup.findScopeFor(functionExpression).fromJust(),
				simpleParamsHelper(functionExpression.params),
				functionExpression.body,
				strict
			);
		} else if (expression instanceof StaticMemberExpression) {
			StaticMemberExpression staticMemberExpression = (StaticMemberExpression) expression;
			return new MemberAccess(
				explicateExpressionSuper(staticMemberExpression.object, strict),
				new LiteralString(staticMemberExpression.property)
			);
		} else if (expression instanceof IdentifierExpression) {
			IdentifierExpression identifierExpression = (IdentifierExpression) expression;
			return Either.extract(refHelper(identifierExpression));
		} else if (expression instanceof LiteralBooleanExpression) {
			return new LiteralBoolean(((LiteralBooleanExpression) expression).value);
		} else if (expression instanceof LiteralInfinityExpression) {
			return LiteralInfinity.INSTANCE;
		} else if (expression instanceof LiteralNumericExpression) {
			return new LiteralNumber(((LiteralNumericExpression) expression).value);
		} else if (expression instanceof LiteralNullExpression) {
			return LiteralNull.INSTANCE;
		} else if (expression instanceof LiteralRegExpExpression) {
			LiteralRegExpExpression literalRegExpExpression = (LiteralRegExpExpression) expression;
			return new LiteralRegExp(literalRegExpExpression.pattern, literalRegExpExpression.global, literalRegExpExpression.ignoreCase, literalRegExpExpression.multiLine, literalRegExpExpression.sticky, literalRegExpExpression.unicode);
		} else if (expression instanceof LiteralStringExpression) {
			return new LiteralString(((LiteralStringExpression) expression).value);
		} else if (expression instanceof ObjectExpression) { // TODO this would be faster/better as Object.defineProperties (all at once, instead of one at a time), probably
			ObjectExpression objectExpression = (ObjectExpression) expression;
			if (objectExpression.properties.isEmpty()) {
				return LiteralEmptyObject.INSTANCE;
			}
			return letWithValue(
				LiteralEmptyObject.INSTANCE,
				obj -> new BlockWithValue(
					objectExpression.properties.map(p -> explicateObjectProperty(obj, p, strict)),
					obj
				)
			);
		} else if (expression instanceof NewExpression) {
			NewExpression c = (NewExpression) expression;
			ImmutableList<NodeWithValue> arguments =
				c.arguments.map(a -> explicateExpressionReturningValue((Expression) a, strict));
			NodeWithValue callee = explicateExpressionSuper(c.callee, strict);
			return new New(callee, arguments);
		} else if (expression instanceof ThisExpression) {
			return new This(strict && this.temporaries.length > 1); // temporaries.length > 1 iff we are within a function. global-code `this` is the same in non-strict mode as strict.
		} else if (expression instanceof UnaryExpression) {
			return explicateUnaryExpression((UnaryExpression) expression, strict);
		} else if (expression instanceof UpdateExpression) {
			UpdateExpression updateExpression = (UpdateExpression) expression;
			if (updateExpression.operand instanceof AssignmentTargetIdentifier) {
				Either<GlobalReference, LocalReference> binding = refHelper((AssignmentTargetIdentifier) updateExpression.operand);
				NodeWithValue valExpr = new TypeCoercionNumber(Either.extract(binding));
				if (keepValue && !updateExpression.isPrefix) {
					return letWithValue(
						valExpr,
						oldVal -> new BlockWithValue(
							new Block(variableAssignmentHelper(
								binding,
								new FloatMath(
									updateExpression.operator == UpdateOperator.Increment ? FloatMath.Operator.Plus : FloatMath.Operator.Minus,
									oldVal,
									new LiteralNumber(1)
								),
								strict
							)),
							oldVal
						)
					);
				} else {
					return variableAssignmentHelper(
						binding,
						new FloatMath(
							updateExpression.operator == UpdateOperator.Increment ? FloatMath.Operator.Plus : FloatMath.Operator.Minus,
							valExpr,
							new LiteralNumber(1)
						),
						strict
					);
				}
			} else {
				NodeWithValue object;
				NodeWithValue field;
				if (updateExpression.operand instanceof ComputedMemberAssignmentTarget) {
					ComputedMemberAssignmentTarget computedMemberAssignmentTarget = (ComputedMemberAssignmentTarget) updateExpression.operand;
					object = explicateExpressionSuper(computedMemberAssignmentTarget.object, strict);
					field = explicateExpressionReturningValue(computedMemberAssignmentTarget.expression, strict);
				} else if (updateExpression.operand instanceof StaticMemberAssignmentTarget) {
					StaticMemberAssignmentTarget staticMemberAssignmentTarget = (StaticMemberAssignmentTarget) updateExpression.operand;
					object = explicateExpressionSuper(staticMemberAssignmentTarget.object, strict);
					field = new LiteralString(staticMemberAssignmentTarget.property);
				} else {
					throw new RuntimeException("Not reached:" + updateExpression.operand.getClass().getSimpleName());
				}

				return letWithValue(
					// semantics: evaluate obj, evaluate prop, ensure obj is object, coerce prop to string, coerce obj[prop] to number. in that order. TODO find a better way of doing this.
					object,
					objectRef -> letWithValue(
						field,
						fieldRef -> new BlockWithValue(
							ImmutableList.of(new RequireObjectCoercible(objectRef)),
							letWithValue(
								new TypeCoercionString(fieldRef),
								// todo we don't actually need a new temporary for this (here or elsewhere); we can just reuse fieldRef
								prop -> (keepValue && !updateExpression.isPrefix) ?
									letWithValue(
										new TypeCoercionNumber(new MemberAccess(objectRef, prop)),
										oldVal -> new BlockWithValue(
											ImmutableList.of(new MemberAssignment(
												objectRef,
												prop,
												new FloatMath(
													updateExpression.operator == UpdateOperator.Increment ? FloatMath.Operator.Plus : FloatMath.Operator.Minus,
													oldVal,
													new LiteralNumber(1)
												),
												strict
											)),
											oldVal
										)
									) :
									new MemberAssignment(
										objectRef,
										prop,
										new FloatMath(
											updateExpression.operator == UpdateOperator.Increment ? FloatMath.Operator.Plus : FloatMath.Operator.Minus,
											new TypeCoercionNumber(new MemberAccess(objectRef, prop)),
											new LiteralNumber(1)
										),
										strict
									)
							)
						)
					)
				);
			}
		}
		throw new UnsupportedOperationException("ES6 not supported: " + expression.getClass().getSimpleName());
	}

	@Nonnull
	Pair<NodeWithValue, Block> explicateSwitchCase(@Nonnull SwitchCase switchCase, boolean strict) {
		return new Pair<>(
			explicateExpressionReturningValue(switchCase.test, strict),
			new Block(switchCase.consequent.map(s -> explicateStatement(s, strict)))
		);
	}

	@Nonnull
	Node explicateVariableDeclarationExpression(
		@Nonnull VariableDeclarationExpression variableDeclarationExpression, boolean strict
	) {
		if (variableDeclarationExpression instanceof VariableDeclaration) {
			return explicateVariableDeclaration((VariableDeclaration) variableDeclarationExpression, strict);
		} else if (variableDeclarationExpression instanceof Expression) {
			return explicateExpressionReturningValue((Expression) variableDeclarationExpression, strict);
		} else {
			throw new RuntimeException("Not reached");
		}
	}

	@Nonnull
	Node explicateVariableDeclaration(@Nonnull VariableDeclaration variableDeclaration, boolean strict) {
		return new Block(
			Maybe.catMaybes(variableDeclaration.declarators.map(d -> d.init.isNothing() ? Maybe.empty() :
				Maybe.of(new VariableAssignment(
					refHelper((BindingIdentifier) d.binding),
					explicateExpressionReturningValue(d.init.fromJust(), strict),
					strict
				))
			))
		);
	}

	@Nonnull
	NodeWithValue explicateUnaryExpression(@Nonnull UnaryExpression unaryExpression, boolean strict) {
		switch (unaryExpression.operator) {
			case Plus:
				return new TypeCoercionNumber(explicateExpressionReturningValue(unaryExpression.operand, strict));
			case Minus:
				return new Negation(explicateExpressionReturningValue(unaryExpression.operand, strict));
			case LogicalNot:
				return new Not(explicateExpressionReturningValue(unaryExpression.operand, strict));
			case BitNot:
				return new BitwiseNot(explicateExpressionReturningValue(unaryExpression.operand, strict));
			case Void:
				return new VoidOp(explicateExpressionDiscardingValue(unaryExpression.operand, strict));
			case Delete:
				if (unaryExpression.operand instanceof IdentifierExpression) {
					IdentifierExpression identifierExpression = (IdentifierExpression) unaryExpression.operand;
					assert !strict; // In strict mode, this is a syntax error.
					if (scopeLookup.isGlobal(scopeLookup.findVariableReferencedBy(identifierExpression))) {
						return new DeleteGlobalProperty(identifierExpression.name);
					} else {
						// No local variables may be deleted.
						return new LiteralBoolean(false);
					}
				} else if (unaryExpression.operand instanceof ComputedMemberExpression) {
					ComputedMemberExpression computedMemberExpression = (ComputedMemberExpression) unaryExpression.operand;
					return new DeleteProperty(
						explicateExpressionSuper(computedMemberExpression.object, strict),
						explicateExpressionReturningValue(computedMemberExpression.expression, strict),
						strict
					);
				} else if (unaryExpression.operand instanceof StaticMemberExpression) {
					StaticMemberExpression staticMemberExpression = (StaticMemberExpression) unaryExpression.operand;
					return new DeleteProperty(
						explicateExpressionSuper(staticMemberExpression.object, strict),
						new LiteralString(staticMemberExpression.property),
						strict
					);
				} else {
					// Deleting anything other than a reference simply evaluates the thing and returns true (for some reason).
					return new BlockWithValue(
						new Block(explicateExpressionDiscardingValue(unaryExpression.operand, strict)),
						new LiteralBoolean(true)
					);
				}
			case Typeof:
				// globals not declared by us need special treatment to avoid throwing when nonexistent
				if (unaryExpression.operand instanceof IdentifierExpression) {
					IdentifierExpression identifierExpression = (IdentifierExpression) unaryExpression.operand;
					Variable var = scopeLookup.findVariableReferencedBy(identifierExpression);
					if (scopeLookup.isGlobal(var) && var.declarations.isEmpty()) {
						return new TypeofGlobal(var.name);
					}
					// else fall through
				}

				return new Typeof(explicateExpressionReturningValue(unaryExpression.operand, strict));
		}
		throw new RuntimeException("Not reached");
	}

	@Nonnull
	NodeWithValue explicateBinaryExpression(@Nonnull BinaryExpression binaryExpression, boolean strict) {
		NodeWithValue right = explicateExpressionReturningValue(binaryExpression.right, strict);

		if (binaryExpression.operator == com.shapesecurity.shift.es2016.ast.operators.BinaryOperator.Sequence) { // unlike all other cases, we do not need the RHS.
			return new BlockWithValue(
				ImmutableList.of(explicateExpressionDiscardingValue(binaryExpression.left, strict)),
				right
			);
		}

		NodeWithValue left = explicateExpressionReturningValue(binaryExpression.left, strict);

		switch (binaryExpression.operator) {
			case Equal:
				return new Equality(
					Equality.Operator.Eq,
					left,
					right
				);
			case NotEqual:
				return new Equality(
					Equality.Operator.Neq,
					left,
					right
				);
			case StrictEqual:
				return new Equality(
					Equality.Operator.StrictEq,
					left,
					right
				);
			case StrictNotEqual:
				return new Equality(
					Equality.Operator.StrictNeq,
					left,
					right
				);
			case LogicalAnd:
				return new Logic(
					Logic.Operator.And,
					left,
					right
				);
			case LogicalOr:
				return new Logic(
					Logic.Operator.Or,
					left,
					right
				);
			case LessThan:
				return new RelationalComparison(
					RelationalComparison.Operator.LessThan,
					left,
					right
				);
			case LessThanEqual:
				return new RelationalComparison(
					RelationalComparison.Operator.LessThanEqual,
					left,
					right
				);
			case GreaterThan:
				return new RelationalComparison(
					RelationalComparison.Operator.GreaterThan,
					left,
					right
				);
			case GreaterThanEqual:
				return new RelationalComparison(
					RelationalComparison.Operator.GreaterThanEqual,
					left,
					right
				);
			case Plus:
				return new FloatMath(
					FloatMath.Operator.Plus,
					left,
					right
				);
			case Minus:
				return new FloatMath(
					FloatMath.Operator.Minus,
					left,
					right
				);
			case Mul:
				return new FloatMath(
					FloatMath.Operator.Mul,
					left,
					right
				);
			case Div:
				return new FloatMath(
					FloatMath.Operator.Div,
					left,
					right
				);
			case Rem:
				return new FloatMath(
					FloatMath.Operator.Rem,
					left,
					right
				);
			case BitwiseAnd:
				return new IntMath(
					IntMath.Operator.BitwiseAnd,
					left,
					right
				);
			case BitwiseOr:
				return new IntMath(
					IntMath.Operator.BitwiseOr,
					left,
					right
				);
			case BitwiseXor:
				return new IntMath(
					IntMath.Operator.BitwiseXor,
					left,
					right
				);
			case Left:
				return new IntMath(
					IntMath.Operator.LeftShift,
					left,
					right
				);
			case Right:
				return new IntMath(
					IntMath.Operator.RightShift,
					left,
					right
				);
			case UnsignedRight:
				return new IntMath(
					IntMath.Operator.UnsignedRightShift,
					left,
					right
				);
			case In:
				return new In(
					left,
					right
				);
			case Instanceof:
				return new InstanceOf(
					left,
					right
				);
		}

		throw new RuntimeException("Not reached");
	}

	@Nonnull
	NodeWithValue explicateCompoundAssignmentExpression(
		@Nonnull CompoundAssignmentExpression compoundAssignmentExpression, boolean strict
	) {
		BinaryOperator operator;
		switch (compoundAssignmentExpression.operator) {
			case AssignPlus:
				operator = FloatMath.Operator.Plus;
				break;
			case AssignMinus:
				operator = FloatMath.Operator.Minus;
				break;
			case AssignMul:
				operator = FloatMath.Operator.Mul;
				break;
			case AssignDiv:
				operator = FloatMath.Operator.Div;
				break;
			case AssignRem:
				operator = FloatMath.Operator.Rem;
				break;
			case AssignBitAnd:
				operator = IntMath.Operator.BitwiseAnd;
				break;
			case AssignBitOr:
				operator = IntMath.Operator.BitwiseOr;
				break;
			case AssignBitXor:
				operator = IntMath.Operator.BitwiseXor;
				break;
			case AssignLeftShift:
				operator = IntMath.Operator.LeftShift;
				break;
			case AssignRightShift:
				operator = IntMath.Operator.RightShift;
				break;
			case AssignUnsignedRightShift:
				operator = IntMath.Operator.UnsignedRightShift;
				break;
			default:
				throw new RuntimeException("Not reached");
		}
		if (compoundAssignmentExpression.binding instanceof AssignmentTargetIdentifier) {
			Either<GlobalReference, LocalReference> binding = refHelper((AssignmentTargetIdentifier) compoundAssignmentExpression.binding);
			NodeWithValue reference = Either.extract(binding);
			return variableAssignmentHelper(
				binding,
				BinaryOperation.fromOperator(
					operator,
					reference,
					explicateExpressionReturningValue(compoundAssignmentExpression.expression, strict)
				),
				strict
			);
		} else {
			NodeWithValue object;
			NodeWithValue field;
			NodeWithValue value;
			if (compoundAssignmentExpression.binding instanceof ComputedMemberAssignmentTarget) {
				ComputedMemberAssignmentTarget computedMemberAssignmentTarget =
					(ComputedMemberAssignmentTarget) compoundAssignmentExpression.binding;
				object = explicateExpressionSuper(computedMemberAssignmentTarget.object, strict);
				field = explicateExpressionReturningValue(computedMemberAssignmentTarget.expression, strict);
				value = explicateExpressionReturningValue(compoundAssignmentExpression.expression, strict);
			} else if (compoundAssignmentExpression.binding instanceof StaticMemberAssignmentTarget) {
				StaticMemberAssignmentTarget staticMemberAssignmentTarget = (StaticMemberAssignmentTarget) compoundAssignmentExpression.binding;
				object = explicateExpressionSuper(staticMemberAssignmentTarget.object, strict);
				field = new LiteralString(staticMemberAssignmentTarget.property);
				value = explicateExpressionReturningValue(compoundAssignmentExpression.expression, strict);
			} else {
				throw new RuntimeException("Not reached: " + compoundAssignmentExpression.binding.getClass().getSimpleName());
			}
			return letWithValue(
				// semantics: evaluate obj, evaluate prop, coerce obj to object, coerce prop to string. in that order. TODO find a better way of doing this.
				object,
				objectRef -> letWithValue(
					field,
					fieldRef -> new BlockWithValue(
						ImmutableList.of(new RequireObjectCoercible(objectRef)),
						letWithValue(
							new TypeCoercionString(fieldRef),
							prop -> new MemberAssignment(
								objectRef,
								prop,
								BinaryOperation.fromOperator(
									operator,
									new MemberAccess(objectRef, prop),
									value
								),
								strict
							)
						)
					)
				)
			);
		}
	}

	@Nonnull
	NodeWithValue explicateExpressionSuper(@Nonnull ExpressionSuper node, boolean strict) {
		return node instanceof Super
			? explicateSuper((Super) node)
			: explicateExpressionReturningValue((Expression) node, strict);
	}

	@Nonnull
	NodeWithValue explicateSuper(@Nonnull Super node) {
		throw new UnsupportedOperationException("ES6 not supported: Super");
	}

	@Nonnull
	Node explicateObjectProperty(@Nonnull LocalReference ref, @Nonnull ObjectProperty objectProperty, boolean strict) {
		if (objectProperty instanceof DataProperty) {
			DataProperty dataProperty = (DataProperty) objectProperty;
			return new MemberDefinition(
				ref,
				explicatePropertyName(dataProperty.name, strict),
				explicateExpressionReturningValue(((DataProperty) objectProperty).expression, strict)
			);
		} else if (objectProperty instanceof Getter) {
			Getter getter = (Getter) objectProperty;
			return new MemberDefinition(
				ref,
				explicatePropertyName(getter.name, strict),
				new MemberAssignmentProperty.Getter(explicateGeneralFunction(
					Maybe.empty(),
					scopeLookup.findScopeFor(getter).fromJust(),
					ImmutableList.empty(),
					getter.body,
					strict
				))
			);
		} else if (objectProperty instanceof Setter) {
			Setter setter = (Setter) objectProperty;
			return new MemberDefinition(
				ref,
				explicatePropertyName(setter.name, strict),
				new MemberAssignmentProperty.Setter(explicateGeneralFunction(
					Maybe.empty(),
					scopeLookup.findScopeFor(setter).fromJust(),
					scopeLookup.findVariableDeclaredBy((BindingIdentifier) setter.param).toList(),
					setter.body,
					strict
				))
			);
		}
		throw new UnsupportedOperationException("ES6 not supported: " + objectProperty.getClass().getSimpleName());
	}

	@Nonnull
	NodeWithValue explicatePropertyName(@Nonnull PropertyName propertyName, boolean strict) {
		if (propertyName instanceof ComputedPropertyName) {
			// TODO not a part of ES5
			return explicateExpressionReturningValue(((ComputedPropertyName) propertyName).expression, strict);
		} else if (propertyName instanceof StaticPropertyName) {
			return new LiteralString(((StaticPropertyName) propertyName).value);
		}
		throw new RuntimeException("Not reached");
	}

	@Nonnull
	Node explicateSpreadElementExpression(
		@Nonnull LocalReference arr, int index, @Nonnull SpreadElementExpression spreadElementExpression, boolean strict
	) { // TODO assigning every individual element of the array is extremely inefficient
		if (spreadElementExpression instanceof Expression) {
			return new MemberDefinition(
				arr,
				new LiteralNumber(index),
				explicateExpressionReturningValue((Expression) spreadElementExpression, strict)
			); // TODO could be non-strict assignment always
		}
		throw new UnsupportedOperationException("ES6 not supported: SpreadElement");
	}

	@Nonnull
	NodeWithValue makeTemporary(@Nonnull F<LocalReference, NodeWithValue> withTemporary) {
		LocalReference ref = new TemporaryReference();
		ImmutableList<Variable> curTemps = this.temporaries.maybeHead().fromJust().cons(ref.variable);
		this.temporaries = this.temporaries.maybeTail().fromJust().cons(curTemps);
		return withTemporary.apply(ref);
	}

	@Nonnull
	Node makeUnvaluedTemporary(@Nonnull F<LocalReference, Node> withTemporary) { // TODO would be nice to join this with the above
		LocalReference ref = new TemporaryReference();
		ImmutableList<Variable> curTemps = this.temporaries.maybeHead().fromJust().cons(ref.variable);
		this.temporaries = this.temporaries.maybeTail().fromJust().cons(curTemps);
		return withTemporary.apply(ref);
	}

	@Nonnull
	NodeWithValue letWithValue(@Nonnull NodeWithValue value, @Nonnull F<LocalReference, NodeWithValue> function) {
		F<LocalReference, NodeWithValue> wrapped =
			ref ->
				new BlockWithValue(new Block(new VariableAssignment(ref, value, false)), function.apply(ref));
		return makeTemporary(wrapped);
	}

	@Nonnull
	Node let(@Nonnull NodeWithValue value, @Nonnull F<LocalReference, Node> function) {
		return makeUnvaluedTemporary(ref ->
			new Block(ImmutableList.of(
				new VariableAssignment(ref, value, false),
				function.apply(ref)
			)));
	}
}
