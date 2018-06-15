package com.shapesecurity.shift.es2016.semantics;

import com.shapesecurity.functional.F;
import com.shapesecurity.functional.Pair;
import com.shapesecurity.functional.data.ImmutableList;
import com.shapesecurity.functional.data.Maybe;
import com.shapesecurity.shift.es2016.ast.Directive;
import com.shapesecurity.shift.es2016.ast.FunctionBody;
import com.shapesecurity.shift.es2016.ast.Script;
import com.shapesecurity.shift.es2016.scope.Scope;
import com.shapesecurity.shift.es2016.scope.Variable;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralFunction;
import com.shapesecurity.shift.es2016.semantics.asg.Node;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.WeakHashMap;

public class ExplicatorWithLocation {
	private static class Implementation extends Explicator {
		@NotNull
		final WeakHashMap<LiteralFunction, FunctionBody> locations = new WeakHashMap<>();

		Implementation(@Nonnull Script script) {
			super(script);
		}

		Implementation(@Nonnull Script script, @Nonnull F<ImmutableList<Directive>, Boolean> isCandidateForInlining) {
			super(script, isCandidateForInlining);
		}

		@Override
		@Nonnull
		LiteralFunction explicateGeneralFunction(
				@Nonnull Maybe<Variable> name, @Nonnull Scope scope, @Nonnull ImmutableList<Variable> parameters,
				@Nonnull FunctionBody functionBody, boolean strict
		) {
			LiteralFunction result = super.explicateGeneralFunction(name, scope, parameters, functionBody, strict);
			this.locations.put(result, functionBody);
			return result;
		}
	}

	@Nonnull
	public static Pair<Semantics, WeakHashMap<LiteralFunction, FunctionBody>> deriveSemanticsWithLocation(@Nonnull Script script) {
		return deriveSemanticsWithLocationHelper(script, new Implementation(script));
	}

	@Nonnull
	public static Pair<Semantics, WeakHashMap<LiteralFunction, FunctionBody>> deriveSemanticsWithLocation(@Nonnull Script script, @Nonnull F<ImmutableList<Directive>, Boolean> isCandidateForInlining) {
		return deriveSemanticsWithLocationHelper(script, new Implementation(script, isCandidateForInlining));
	}

	@Nonnull
	private static Pair<Semantics, WeakHashMap<LiteralFunction, FunctionBody>> deriveSemanticsWithLocationHelper(@Nonnull Script script, @Nonnull Implementation exp) {
		Node result = exp.explicate();
		ImmutableList<Variable> maybeGlobals = exp.functionVariablesHelper(script);
		ImmutableList<Variable> scriptLocals =
				maybeGlobals.filter(x -> !exp.scopeLookup.isGlobal(x)).append(exp.currentState.getAdditionalVariables());
		ImmutableList<String> scriptVarDecls =
				maybeGlobals.filter(x -> exp.scopeLookup.isGlobal(x) && x.declarations.isNotEmpty()).map(x -> x.name);
		return Pair.of(new Semantics(result, scriptLocals, scriptVarDecls, exp.scopeLookup, exp.functionScopes), exp.locations);
	}
}
