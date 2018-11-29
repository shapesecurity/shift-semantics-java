package com.shapesecurity.shift.es2017.semantics;

import com.shapesecurity.shift.es2017.parser.Parser;
import com.shapesecurity.shift.es2017.semantics.visitor.ReconstructingReducer;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ReconstructingReducerTest {

	@Test
	public void testReconstructingReducer() throws Exception {
		String programText = "var z; typeof a; eval; ({ set a(b) { a = 0; }, get a(){} }).a = 0; " +
				"(function() { 'use strict'; try { undefined = 0; } catch (e) {} })(); " +
				"({a: 1}).a+(-1)+1-1*1/1%1|1&1^1<<1>>>1>>1; b = 1<1>1<=1>=1==1===1!=1!==!1;" +
				"b++; b in {}; f = (function (a) { return typeof 1;});" +
				"f(); f(1); f(1, []); f(1, [], null); f(1, [], null, this); f(1, [], null, this, /a/g); new f;" +
				"delete b; if (true) delete { a: 1}.a; if (false) ; else a = ~1; try { throw 'err'; } catch (e) {};" +
				"a instanceof Number; a = 2e308; a = 1.1; o = {}; (function() { 'use strict'; f(this); o.a = 1; delete o.a; })();" +
				"a = {'a':1}; for (b in a) ;" +
				"i = 1, j = 1; switch(i) { case --j: break; }" +
				"(function(a){arguments = {0:'a', 1:'b'}; arguments[0]=1; c = arguments[0]})(2);" +
				"(function A() { \n" +
				"  try { a = 1; return a;} catch(e) { a = 5; } finally { a = 2;}\n" +
				"})();" +
				"(function A() { \n" +
				"  label: try { } finally { break label; }; return 0;\n" +
				"})();";

		Semantics asg = Explicator.deriveSemantics(Parser.parseScript(programText));
		Semantics reconstructedAsg = new Semantics(new ReconstructingReducer().visit(asg.node), asg.locals, asg.scriptVarDecls, asg.scopeLookup, asg.functionScopes);
		assertTrue(asg.node.equals(reconstructedAsg.node));
	}
}
