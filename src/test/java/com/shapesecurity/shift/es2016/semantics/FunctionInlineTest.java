package com.shapesecurity.shift.es2016.semantics;

import com.shapesecurity.shift.es2016.parser.JsError;
import com.shapesecurity.shift.es2016.parser.Parser;
import com.shapesecurity.shift.es2016.semantics.asg.*;
import org.junit.Assert;
import org.junit.Test;

public class FunctionInlineTest {
	@Test
	public void testFunctionInlines() throws JsError {
		String source = "(function() { var x = 0; var y = 9; return x + y; })()";
		Semantics s = Explicator.deriveSemantics(Parser.parseScript(source), list -> true);
		Assert.assertTrue(s.node instanceof Block);
		Block b = (Block) s.node;
		Assert.assertEquals(1, b.children.length);
		Assert.assertTrue(b.children.maybeHead().fromJust() instanceof BlockWithValue);
		BlockWithValue blockWithValue = (BlockWithValue) b.children.maybeHead().fromJust();
		Assert.assertEquals(1, blockWithValue.head.children.length);
		Assert.assertTrue(blockWithValue.result instanceof BlockWithValue);
		Assert.assertTrue(blockWithValue.head.children.maybeHead().fromJust() instanceof VariableAssignment);
	}

	@Test
	public void testFunctionWithArguments() throws JsError {
		testDoesNotInline("(function(x, y) {return x + y; })(1, 2)");
	}

	@Test
	public void testFunctionContainingThis() throws JsError {
		testDoesNotInline("(function() { return this.a })()");
	}

	@Test
	public void testFunctionContainingArguments() throws JsError {
		testDoesNotInline("(function() { return arguments; })()");
	}

	private void testDoesNotInline(String source) throws JsError {
		Semantics s = Explicator.deriveSemantics(Parser.parseScript(source));
		Assert.assertTrue(s.node instanceof Block);
		Block b = (Block) s.node;
		Assert.assertTrue(b.children.maybeHead().fromJust() instanceof Call);
		Assert.assertTrue(((Call) b.children.maybeHead().fromJust()).callee instanceof LiteralFunction);
	}
}
