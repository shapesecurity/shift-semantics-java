package com.shapesecurity.shift.es2016.semantics;

import com.shapesecurity.shift.es2016.ast.Script;
import com.shapesecurity.shift.es2016.parser.JsError;
import com.shapesecurity.shift.es2016.parser.Parser;
import org.junit.Test;

public class ExplicationSucceedsTest {
	private void assertSucceeds(String source) {
		try {
			Script tree = Parser.parseScript(source);
			Explicator.deriveSemantics(tree);
		} catch (JsError e) {
			throw new RuntimeException("Parse failed!");
		}
	}

	@Test
	public void testReturnReturn() {
		assertSucceeds("function f(){ return function g(){ return 0; } }");
	}
}
