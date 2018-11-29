package com.shapesecurity.shift.es2017.semantics;

import com.shapesecurity.functional.Pair;
import com.shapesecurity.functional.data.NonEmptyImmutableList;
import com.shapesecurity.shift.es2017.ast.AssignmentExpression;
import com.shapesecurity.shift.es2017.ast.ExpressionStatement;
import com.shapesecurity.shift.es2017.ast.FunctionBody;
import com.shapesecurity.shift.es2017.ast.FunctionDeclaration;
import com.shapesecurity.shift.es2017.ast.FunctionExpression;
import com.shapesecurity.shift.es2017.ast.Script;
import com.shapesecurity.shift.es2017.semantics.ExplicatorWithLocation;
import com.shapesecurity.shift.es2017.parser.Parser;
import com.shapesecurity.shift.es2017.semantics.Semantics;
import com.shapesecurity.shift.es2017.semantics.asg.Block;
import com.shapesecurity.shift.es2017.semantics.asg.LiteralFunction;
import com.shapesecurity.shift.es2017.semantics.asg.Node;
import com.shapesecurity.shift.es2017.semantics.asg.VariableAssignment;
import org.junit.Test;

import java.util.WeakHashMap;

import static org.junit.Assert.assertEquals;

public class ExplicatorWithLocationTest {
	@Test
	public void testFunctionExpression() throws Exception {
		Script tree = Parser.parseScript("x = function(){};");
		Pair<Semantics, WeakHashMap<LiteralFunction, FunctionBody>> res = ExplicatorWithLocation.deriveSemanticsWithLocation(tree);

		FunctionExpression astFunction = (FunctionExpression) ((AssignmentExpression) ((ExpressionStatement) tree.statements.maybeHead().fromJust()).expression).expression;

		VariableAssignment va = (VariableAssignment) ((NonEmptyImmutableList<Node>) ((Block) res.left.node).children).head;
		LiteralFunction asgFunction = (LiteralFunction) va.value;

		assertEquals(astFunction.body, res.right().get(asgFunction));
	}

	@Test
	public void testFunctionDeclaration() throws Exception {
		Script tree = Parser.parseScript("function x(){};");
		Pair<Semantics, WeakHashMap<LiteralFunction, FunctionBody>> res = ExplicatorWithLocation.deriveSemanticsWithLocation(tree);

		FunctionDeclaration astFunction = ((FunctionDeclaration) tree.statements.maybeHead().fromJust());

		VariableAssignment va = (VariableAssignment) ((NonEmptyImmutableList<Node>) ((Block) res.left.node).children).head;
		LiteralFunction asgFunction = (LiteralFunction) va.value;

		assertEquals(astFunction.body, res.right().get(asgFunction));
	}
}
