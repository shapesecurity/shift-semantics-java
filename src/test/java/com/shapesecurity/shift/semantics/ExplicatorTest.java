package com.shapesecurity.shift.semantics;

import com.shapesecurity.functional.data.NonEmptyImmutableList;
import com.shapesecurity.shift.parser.Parser;
import com.shapesecurity.shift.semantics.asg.Block;
import com.shapesecurity.shift.semantics.asg.Call;
import com.shapesecurity.shift.semantics.asg.GlobalReference;
import com.shapesecurity.shift.semantics.asg.LiteralNumber;
import com.shapesecurity.shift.semantics.asg.Node;
import com.shapesecurity.shift.semantics.asg.VariableAssignment;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExplicatorTest {
	@Test
	public void testAssignment() throws Exception {
		Semantics s = Explicator.deriveSemantics(Parser.parseScript("a=0"));
		assertTrue(s.node instanceof Block);
		assertEquals(1, ((Block) s.node).children.length);
		assertTrue(((NonEmptyImmutableList<Node>) ((Block) s.node).children).head instanceof VariableAssignment);
		VariableAssignment va = (VariableAssignment) ((NonEmptyImmutableList<Node>) ((Block) s.node).children).head;
		assertTrue(va.ref.isLeft());
		assertEquals("a", va.ref.left().fromJust().name);
		assertTrue(va.value instanceof LiteralNumber);
		assertEquals(0.0, ((LiteralNumber) va.value).value, 0.0);
	}

	@Test
	public void testCall() throws Exception {
		Semantics s = Explicator.deriveSemantics(Parser.parseModule("a(0)"));
		assertTrue(s.node instanceof Block);
		assertEquals(1, ((Block) s.node).children.length);
		assertTrue(((NonEmptyImmutableList<Node>) ((Block) s.node).children).head instanceof Call);
		Call c = (Call) ((NonEmptyImmutableList<Node>) ((Block) s.node).children).head;
		assertTrue(c.callee instanceof GlobalReference);
		assertEquals("a", ((GlobalReference) c.callee).name);
		assertEquals(1, c.arguments.length);
		assertTrue(c.arguments.maybeHead().fromJust() instanceof LiteralNumber);
		assertEquals(0.0, ((LiteralNumber) c.arguments.maybeHead().fromJust()).value, 0.0);
	}

	public static String COVERAGE_PROGRAM =
		"var z; typeof a; ({ set a(b) { a = 0; }, get a(){} }).a = 0; " +
		"(function() { 'use strict'; try { undefined = 0; } catch (e) {} })(); " +
		"({a: 1}).a+(-1)+1-1*1/1%1|1&1^1<<1>>>1>>1; b = 1<1>1<=1>=1==1===1!=1!==!1;" +
		"a=b++; a.b++; --a[b]; b in {}; f = (function (a) { return typeof 1;});" +
		"a+=a; a-=a; a*=a; a/=a; a%=a; a&=a; a|=a; a^=a; a<<=a; a>>=a; a>>>=a;" +
		"a.b+=a; a[b]+=a; a = a && a || a; a ? a : a;" +
		"f(); a.b(); a[b](); f(1); f(0, []); f(0, [1], null); f(1, [,,], null, this, /a/g); new f;" +
		"delete b; delete 0; if (true) delete { a: 1}.a; if (false) ; else a = ~1; try { throw 'err'; } catch (e) {};" +
		"a instanceof Number; a = 2e308; a = 1.1;" +
		"(function f(o) { f = 0; delete o; })();" +
		"(function f(o) { 'use strict'; f = 0; o.a = 1; delete o.a; delete o['a']; })({});" +
		"debugger; for(var b in a); for(b in a); for(var a;0;); while(0) continue; do;while(0);" +
		"i = 1, j = 1; switch(i) { default: break; } switch(i) { case --j: break; }" +
		"(function(a){arguments = {0:'a', 1:'b'}; arguments[0]=1; c = arguments[0]})(2);" +
		"function A() {} { function A() {} function B() {} };" +
		"(function A() { \n" +
		"  try { a = 1; return a;} catch(e) { a = 5; } finally { a = 2;}\n" +
		"})();" +
		"(function A() { \n" +
		"  label: try { } finally { break label; }; return 0;\n" +
		"})();" +
		"with(eval());";

	// WARN: this is a hack to get decent coverage numbers
	@Test
	public void testCoverage() throws Exception {
		Explicator.deriveSemantics(Parser.parseScript(COVERAGE_PROGRAM));
	}
}
