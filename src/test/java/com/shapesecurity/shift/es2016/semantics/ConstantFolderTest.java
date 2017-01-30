package com.shapesecurity.shift.es2016.semantics;

import com.shapesecurity.shift.es2016.ast.Script;
import com.shapesecurity.shift.es2016.parser.Parser;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.FloatMath;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.IntMath;
import com.shapesecurity.shift.es2016.semantics.asg.Block;
import com.shapesecurity.shift.es2016.semantics.asg.BlockWithValue;
import com.shapesecurity.shift.es2016.semantics.asg.Call;
import com.shapesecurity.shift.es2016.semantics.asg.IfElse;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralBoolean;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralFunction;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralNull;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralNumber;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralString;
import com.shapesecurity.shift.es2016.semantics.asg.Node;
import com.shapesecurity.shift.es2016.semantics.asg.UnaryOperation.Not;
import com.shapesecurity.shift.es2016.semantics.asg.VariableAssignment;
import com.shapesecurity.shift.es2016.semantics.asgvisitor.BlockSquasher;
import com.shapesecurity.shift.es2016.semantics.visitor.ConstantFolder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// TODO: assert that things that shouldn't change don't change

public class ConstantFolderTest {
    @Test
    public void testLogicalNegationOfNumber() throws Exception {
        {
            String programText = "f(!0)"; // f(true)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust() instanceof Not);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            assertTrue(((Call) ((Block) reducedAsg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust() instanceof LiteralBoolean);
            assertTrue(((LiteralBoolean) ((Call) ((Block) reducedAsg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value);
        }
        {
            String programText = "f(!1)"; // f(false)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust() instanceof Not);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            assertTrue(((Call) ((Block) reducedAsg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust() instanceof LiteralBoolean);
            assertFalse(((LiteralBoolean) ((Call) ((Block) reducedAsg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value);
        }
        {
            String programText = "f(!0.000000000001)"; // f(false)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust() instanceof Not);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            assertTrue(((Call) ((Block) reducedAsg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust() instanceof LiteralBoolean);
            assertFalse(((LiteralBoolean) ((Call) ((Block) reducedAsg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value);
        }
    }

    @Test
    public void testLogicalNegationOfLogicalNegationOfLogicalNegation() throws Exception {
        {
            String programText = "f(!!!x)"; // f(!x)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((Not) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).expression instanceof Not);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            assertFalse(((Not) ((Call) ((Block) reducedAsg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).expression instanceof Not);
        }
        {
            String programText = "f(!!!!x)"; // f(!!x)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((Not) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).expression instanceof Not);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            assertTrue(((Not) ((Call) ((Block) reducedAsg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).expression instanceof Not);
            assertFalse(((Not) ((Not) ((Call) ((Block) reducedAsg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).expression).expression instanceof Not);
        }
        {
            String programText = "f(!!!!!x)"; // f(!x)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((Not) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).expression instanceof Not);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            assertFalse(((Not) ((Call) ((Block) reducedAsg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).expression instanceof Not);
        }
        {
            String programText = "f(!!!!!!x)"; // f(!!x)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((Not) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).expression instanceof Not);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            assertTrue(((Not) ((Call) ((Block) reducedAsg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).expression instanceof Not);
            assertFalse(((Not) ((Not) ((Call) ((Block) reducedAsg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).expression).expression instanceof Not);
        }
    }

    @Test
    public void testLogicalNegationOfBoolean() throws Exception {
        {
            String programText = "f(!true)"; // f(false)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust() instanceof Not);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            assertTrue(((Call) ((Block) reducedAsg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust() instanceof LiteralBoolean);
            assertFalse(((LiteralBoolean) ((Call) ((Block) reducedAsg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value);
        }
        {
            String programText = "f(!false)"; // f(true)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust() instanceof Not);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            assertTrue(((Call) ((Block) reducedAsg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust() instanceof LiteralBoolean);
            assertTrue(((LiteralBoolean) ((Call) ((Block) reducedAsg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value);
        }
        {
            String programText = "f(!!true)"; // f(true)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust() instanceof Not);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            assertTrue(((Call) ((Block) reducedAsg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust() instanceof LiteralBoolean);
            assertTrue(((LiteralBoolean) ((Call) ((Block) reducedAsg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value);
        }
        {
            String programText = "f(!!!true)"; // f(false)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust() instanceof Not);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            assertTrue(((Call) ((Block) reducedAsg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust() instanceof LiteralBoolean);
            assertFalse(((LiteralBoolean) ((Call) ((Block) reducedAsg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value);
        }
    }

    @Test
    public void testConditionals() throws Exception {
        {
            String programText = "f(true ? 1 : 0)"; // f(1)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((BlockWithValue) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).head.children.maybeHead().fromJust() instanceof IfElse);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue(((BlockWithValue) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).head.children.maybeHead().fromJust() instanceof VariableAssignment);
            assertTrue(((VariableAssignment) ((BlockWithValue) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).head.children.maybeHead().fromJust()).value instanceof LiteralNumber);
            assertEquals(1.0, ((LiteralNumber) ((VariableAssignment) ((BlockWithValue) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).head.children.maybeHead().fromJust()).value).value, 0.0);
        }
        {
            String programText = "f(1 ? 1 : 0)"; // f(1)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((BlockWithValue) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).head.children.maybeHead().fromJust() instanceof IfElse);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue(((BlockWithValue) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).head.children.maybeHead().fromJust() instanceof VariableAssignment);
            assertTrue(((VariableAssignment) ((BlockWithValue) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).head.children.maybeHead().fromJust()).value instanceof LiteralNumber);
            assertEquals(1.0, ((LiteralNumber) ((VariableAssignment) ((BlockWithValue) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).head.children.maybeHead().fromJust()).value).value, 0.0);

        }
        {
            String programText = "f(true ? \"1\" : 0)"; // f("1")
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((BlockWithValue) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).head.children.maybeHead().fromJust() instanceof IfElse);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue(((BlockWithValue) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).head.children.maybeHead().fromJust() instanceof VariableAssignment);
            assertTrue(((VariableAssignment) ((BlockWithValue) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).head.children.maybeHead().fromJust()).value instanceof LiteralString);
            assertEquals("1", ((LiteralString) ((VariableAssignment) ((BlockWithValue) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).head.children.maybeHead().fromJust()).value).value);
        }
        {
            String programText = "f(true ? null : 0)"; // f(null)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((BlockWithValue) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).head.children.maybeHead().fromJust() instanceof IfElse);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue(((BlockWithValue) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).head.children.maybeHead().fromJust() instanceof VariableAssignment);
            assertTrue(((VariableAssignment) ((BlockWithValue) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).head.children.maybeHead().fromJust()).value instanceof LiteralNull);
        }
        {
            String programText = "f(true ? function(){} : 0)"; // f(function(){})
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((BlockWithValue) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).head.children.maybeHead().fromJust() instanceof IfElse);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue(((BlockWithValue) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).head.children.maybeHead().fromJust() instanceof VariableAssignment);
            assertTrue(((VariableAssignment) ((BlockWithValue) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).head.children.maybeHead().fromJust()).value instanceof LiteralFunction);
        }
    }

    @Test
    public void testFloatMath() throws Exception {
        {
            String programText = "f(2.0 + 11.33)"; // f(13.33)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((FloatMath) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).operator instanceof FloatMath.Operator);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue((((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()) instanceof LiteralNumber);
            assertEquals(13.33, ((LiteralNumber) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value, 0.0);
        }
        {
            String programText = "f('a' + 'b')"; // f('ab')
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((FloatMath) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).operator instanceof FloatMath.Operator);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue((((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()) instanceof LiteralString);
            assertEquals("ab", ((LiteralString) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value);
        }
        {
            String programText = "f('a' + 11.33)"; // f('a13.33')
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((FloatMath) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).operator instanceof FloatMath.Operator);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue((((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()) instanceof LiteralString);
            assertEquals("a11.33", ((LiteralString) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value);
        }
        {
            String programText = "f(11.33 + 'a')"; // f('13.33a')
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((FloatMath) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).operator instanceof FloatMath.Operator);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue((((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()) instanceof LiteralString);
            assertEquals("11.33a", ((LiteralString) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value);
        }
    }

    @Test
    public void testIntMath() throws Exception {
        {
            String programText = "f(5 << 1)"; // f(10)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((IntMath) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).operator instanceof IntMath.Operator);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue((((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()) instanceof LiteralNumber);
            assertEquals(10, ((LiteralNumber) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value, 0.0);
        }
        {
            String programText = "f('5' << '1')"; // f(10)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((IntMath) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).operator instanceof IntMath.Operator);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue((((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()) instanceof LiteralNumber);
            assertEquals(10, ((LiteralNumber) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value, 0.0);
        }
        {
            String programText = "f('5' << 1)"; // f(10)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((IntMath) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).operator instanceof IntMath.Operator);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue((((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()) instanceof LiteralNumber);
            assertEquals(10, ((LiteralNumber) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value, 0.0);
        }
        {
            String programText = "f(5 << '1')"; // f(10)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((IntMath) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).operator instanceof IntMath.Operator);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue((((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()) instanceof LiteralNumber);
            assertEquals(10, ((LiteralNumber) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value, 0.0);
        }
        {
            String programText = "f('-5' << '-1')"; // f(10)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((IntMath) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).operator instanceof IntMath.Operator);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue((((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()) instanceof LiteralNumber);
            assertEquals(-2147483648, ((LiteralNumber) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value, 0.0);
        }

        {
            String programText = "f(10 >> 1)"; // f(10)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((IntMath) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).operator instanceof IntMath.Operator);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue((((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()) instanceof LiteralNumber);
            assertEquals(5, ((LiteralNumber) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value, 0.0);
        }
        {
            String programText = "f('10' >> '1')"; // f(10)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((IntMath) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).operator instanceof IntMath.Operator);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue((((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()) instanceof LiteralNumber);
            assertEquals(5, ((LiteralNumber) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value, 0.0);
        }
        {
            String programText = "f('10' >> 1)"; // f(10)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((IntMath) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).operator instanceof IntMath.Operator);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue((((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()) instanceof LiteralNumber);
            assertEquals(5, ((LiteralNumber) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value, 0.0);
        }
        {
            String programText = "f(10 >> '1')"; // f(10)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((IntMath) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).operator instanceof IntMath.Operator);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue((((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()) instanceof LiteralNumber);
            assertEquals(5, ((LiteralNumber) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value, 0.0);
        }
        {
            String programText = "f('-10' >> '-1')"; // f(10)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((IntMath) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).operator instanceof IntMath.Operator);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue((((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()) instanceof LiteralNumber);
            assertEquals(-1, ((LiteralNumber) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value, 0.0);
        }
        {
            String programText = "f('10' >> '-1')"; // f(10)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((IntMath) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).operator instanceof IntMath.Operator);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue((((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()) instanceof LiteralNumber);
            assertEquals(0, ((LiteralNumber) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value, 0.0);
        }
        {
            String programText = "f('-10' >> '1')"; // f(10)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((IntMath) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).operator instanceof IntMath.Operator);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue((((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()) instanceof LiteralNumber);
            assertEquals(-5, ((LiteralNumber) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value, 0.0);
        }

        {
            String programText = "f(10 >>> 1)"; // f(10)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((IntMath) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).operator instanceof IntMath.Operator);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue((((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()) instanceof LiteralNumber);
            assertEquals(5, ((LiteralNumber) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value, 0.0);
        }
        {
            String programText = "f('10' >>> '1')"; // f(10)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((IntMath) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).operator instanceof IntMath.Operator);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue((((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()) instanceof LiteralNumber);
            assertEquals(5, ((LiteralNumber) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value, 0.0);
        }
        {
            String programText = "f('10' >>> 1)"; // f(10)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((IntMath) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).operator instanceof IntMath.Operator);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue((((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()) instanceof LiteralNumber);
            assertEquals(5, ((LiteralNumber) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value, 0.0);
        }
        {
            String programText = "f(10 >>> '1')"; // f(10)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((IntMath) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).operator instanceof IntMath.Operator);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue((((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()) instanceof LiteralNumber);
            assertEquals(5, ((LiteralNumber) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value, 0.0);
        }
        {
            String programText = "f('-10' >>> '-1')"; // f(10)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((IntMath) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).operator instanceof IntMath.Operator);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue((((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()) instanceof LiteralNumber);
            assertEquals(1, ((LiteralNumber) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value, 0.0);
        }
        {
            String programText = "f('-10' >>> '1')"; // f(10)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((IntMath) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).operator instanceof IntMath.Operator);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue((((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()) instanceof LiteralNumber);
            assertEquals(2147483643, ((LiteralNumber) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value, 0.0);
        }
        {
            String programText = "f('10' >>> '-1')"; // f(10)
            Script ast = Parser.parseScript(programText);
            Semantics asg = Explicator.deriveSemantics(ast);
            assertTrue(((IntMath) ((Call) ((Block) asg.node).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).operator instanceof IntMath.Operator);
            Semantics reducedAsg = ConstantFolder.reduce(asg);
            Node reducedNode = new BlockSquasher().visit(reducedAsg.node);
            assertTrue((((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()) instanceof LiteralNumber);
            assertEquals(0, ((LiteralNumber) ((Call) ((Block) reducedNode).children.maybeHead().fromJust()).arguments.maybeHead().fromJust()).value, 0.0);
        }

        // TODO: &, |, ^
    }
}
