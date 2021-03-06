package prj.clark.alchemy.tree;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Before;
import org.junit.Test;
import prj.clark.alchemy.AlchemyLexer;
import prj.clark.alchemy.AlchemyParser;
import prj.clark.alchemy.data.*;
import prj.clark.alchemy.env.AlchemyContext;
import prj.clark.alchemy.env.Context;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class NodeFactoryTest {
    private Context ctx;

    private static final NodeFactory factory = new NodeFactory();

    @Before
    public void setUp() {
        ctx = new AlchemyContext();
    }

    private static List<Node> parse(String content) {
        ANTLRInputStream is = new ANTLRInputStream(content);
        AlchemyLexer lexer = new AlchemyLexer(is);
        CommonTokenStream cts = new CommonTokenStream(lexer);
        AlchemyParser parser = new AlchemyParser(cts);
        AlchemyParser.FileContext fileContext = parser.file();
        return factory.getAll(fileContext);
    }

    private static Valued parseSingleValue(String content) {
        List<Node> nodes = parse(content);

        assertEquals(1, nodes.size());
        assertTrue(nodes.get(0) instanceof Valued);

        return (Valued) nodes.get(0);
    }

    @Test
    public void listCreation() {
        assertEquals(
                new EagerAlchemyList(Arrays.asList(
                        AlchemyInt.of(1),
                        AlchemyInt.of(2),
                        AlchemyInt.of(3),
                        AlchemyInt.of(4),
                        AlchemyInt.of(5))),
                parseSingleValue("[1, 2, 3, 4, 5]").evaluate(ctx));
    }

    @Test
    public void stringLiteral() {
        assertEquals(AlchemyString.of("hello\n"), parseSingleValue("\"hello\\n\"").evaluate(ctx));
    }

    @Test
    public void intLiteral() {
        // TODO(matthew-c21) - Unary negation doesn't exist yet.
        assertEquals(AlchemyInt.of(100), parseSingleValue("100").evaluate(ctx));
    }

    @Test
    public void floatLiteral() {
        assertEquals(AlchemyFloat.of(11.125), parseSingleValue("11.125").evaluate(ctx));
    }

    @Test
    public void booleanLiteral() {
        assertEquals(AlchemyBoolean.TRUE, parseSingleValue("True").evaluate(ctx));
        assertEquals(AlchemyBoolean.FALSE, parseSingleValue("False").evaluate(ctx));
    }

    @Test
    public void tupleCreation() {
        assertEquals(new AlchemyTuple(Arrays.asList(AlchemyInt.of(1), AlchemyFloat.of(0.5), AlchemyCharacter.of('h'))),
                parseSingleValue("(1, 0.5, 'h')").evaluate(ctx));
    }

    @Test
    public void functionGeneration() {
        Data i = parseSingleValue("(a, b) { a + b / 2 }").evaluate(ctx);

        assertTrue(i instanceof Invokable);

        assertEquals(AlchemyFloat.of("8"), ((Invokable)i).invoke(Arrays.asList(AlchemyInt.of(4), AlchemyInt.of(8))));
    }

    @Test
    public void functionWithExtendedDefinitions() {
        Data i = parseSingleValue("(a, b) { (a * b) / c } with c = a + b / 2").evaluate(ctx);

        assertTrue(i instanceof Invokable);

        assertEquals(AlchemyFloat.of(1), ((Invokable)i).invoke(Arrays.asList(AlchemyInt.of(1), AlchemyInt.of(2))));
    }

    @Test
    public void allBinaryOperators() {
        List<Node> nodes = parse(
                "a ^ b\n" +
                        "a * b\n" +
                        "a / b\n" +
                        "a % b\n" +
                        "a + b\n" +
                        "a - b\n" +
                        "a < b\n" +
                        "a > b\n" +
                        "a <= b\n" +
                        "a >= b\n" +
                        "a == b\n" +
                        "a != b\n" +
                        "a >> b\n" +
                        "a << b\n" +
                        "a::b \n" +
                        "a and b \n" +
                        "a or b\n" +
                        "a ++ b\n");

        assertTrue(nodes.get(0) instanceof ExponentialNode);
        assertTrue(nodes.get(1) instanceof MultiplicationNode);
        assertTrue(nodes.get(2) instanceof DivisionNode);
        assertTrue(nodes.get(3) instanceof ModulusNode);
        assertTrue(nodes.get(4) instanceof AdditionNode);
        assertTrue(nodes.get(5) instanceof SubtractionNode);
        assertTrue(nodes.get(6) instanceof LessThanNode);
        assertTrue(nodes.get(7) instanceof GreaterThanNode);
        assertTrue(nodes.get(8) instanceof LessThanEqualNode);
        assertTrue(nodes.get(9) instanceof GreaterThanEqualNode);
        assertTrue(nodes.get(10) instanceof EqualNode);
        assertTrue(nodes.get(11) instanceof NotEqualNode);
        assertTrue(nodes.get(12) instanceof FeedFirstNode);
        assertTrue(nodes.get(13) instanceof FeedLastNode);
        assertTrue(nodes.get(14) instanceof ConcatenationNode);
        assertTrue(nodes.get(15) instanceof AndNode);
        assertTrue(nodes.get(16) instanceof OrNode);
        assertTrue(nodes.get(17) instanceof StringConcatenationNode);
    }

    @Test
    public void indexing() {
        AlchemyList l = new EagerAlchemyList(Arrays.asList(AlchemyInt.of(1), AlchemyInt.of(2), AlchemyInt.of(3)));
        ctx.bind("l", l);

        assertEquals(AlchemyInt.of(1), parseSingleValue("l[0]").evaluate(ctx));
        assertEquals(
                new AlchemySlice.AlchemySliceBuilder(l).setStart(AlchemyInt.of(1)).build(),
                parseSingleValue("l[1:]").evaluate(ctx));
    }

    @Test
    public void standardBinding() {
        assertFalse(ctx.search("foo").isPresent());

        List<Node> nodes = parse("foo = 'f'");
        assertEquals(1, nodes.size());

        nodes.get(0).execute(ctx);

        assertTrue(ctx.search("foo").isPresent());
        assertEquals(AlchemyCharacter.of('f'), ctx.search("foo").get());
    }

    @Test
    public void functionBinding() {
        assertFalse(ctx.search("f").isPresent());

        List<Node> nodes = parse("defn f(a, b) { c } with c = 2*a + b");

        assertEquals(1, nodes.size());

        nodes.get(0).execute(ctx);

        assertTrue(ctx.search("f").isPresent());

        Invokable i = (Invokable) (ctx.search("f").get());

        assertEquals(AlchemyInt.of(30), i.invoke(Arrays.asList(AlchemyInt.of(10), AlchemyInt.of(10))));
    }

    @Test
    public void rangeCreation() {
        assertEquals(
                new AlchemyRange.AlchemyRangeBuilder()
                        .setFirst(AlchemyInt.of(1))
                        .setStop(AlchemyInt.of(10))
                        .build(),
                parseSingleValue("[1..10]").evaluate(ctx));
    }

    @Test
    public void slice() {
        AlchemyList l = new AlchemyRange.AlchemyRangeBuilder().setFirst(AlchemyInt.of(1)).build();

        ctx.bind("l", l);

        AlchemySlice.AlchemySliceBuilder b = new AlchemySlice.AlchemySliceBuilder(l);

        assertEquals(AlchemyInt.of(1), parseSingleValue("l[0]").evaluate(ctx));

        assertEquals(
                b.setStart(AlchemyInt.of(1)).setStop(AlchemyInt.of(50)).build(),
                parseSingleValue("l[1:50]").evaluate(ctx));

        assertEquals(
                b.setStart(AlchemyInt.of(0)).setStop(AlchemyInt.of(30)).setSkip(AlchemyInt.of(3)).build(),
                parseSingleValue("l[0:30:3]").evaluate(ctx));

        assertEquals(
                b.setStart(AlchemyInt.of(0)).setStop(AlchemyInt.of(30)).setSkip(AlchemyInt.of(3)).build(),
                parseSingleValue("l[:30:3]").evaluate(ctx));
    }

    @Test
    public void characterLiteral() {
        assertEquals(AlchemyCharacter.of("\\n"), parseSingleValue("'\\n'").evaluate(ctx));
    }

    @Test
    public void identifier() {
        ctx.bind("foo", AlchemyString.of("seminar"));

        assertEquals(AlchemyString.of("seminar"), parseSingleValue("foo").evaluate(ctx));
    }

    @Test
    public void commentProducesEmptyList() {
        assertEquals(0,parse("/* This is a \n * multi-line\n * comment. */").size());
    }

    @Test
    public void infixFunction() {
        ctx.bindFunction("f",
                new GeneratedFunction(
                        new AdditionNode(
                                new IdentifierNode("a"),
                                new IdentifierNode("b")),
                        ctx,
                        Arrays.asList("a", "b")));

        assertEquals(AlchemyInt.of(100), parseSingleValue("30 `f` 70").evaluate(ctx));
    }

    @Test
    public void functionInvocation() {
        ctx.bindFunction("f",
                new GeneratedFunction(
                        new SubtractionNode(
                                new IdentifierNode("a"),
                                new IdentifierNode("b")),
                        ctx,
                        Arrays.asList("a", "b")));

        assertEquals(AlchemyInt.of(-40), parseSingleValue("f(30, 70)").evaluate(ctx));
    }

    @Test
    public void logicalInversion() {
        assertEquals(AlchemyBoolean.FALSE, parseSingleValue("!True").evaluate(ctx));
        assertEquals(AlchemyBoolean.TRUE, parseSingleValue("!False").evaluate(ctx));
        assertEquals(AlchemyBoolean.FALSE, parseSingleValue("!\"hello\"").evaluate(ctx));
    }

    @Test
    public void conditional() {
        assertEquals(AlchemyInt.of(1), parseSingleValue("True ? 1 : 2").evaluate(ctx));
        assertEquals(AlchemyInt.of(2), parseSingleValue("False ? 1 : 2").evaluate(ctx));
    }

    @Test
    public void unaryNegation() {
        assertEquals(AlchemyInt.of(-12), parseSingleValue("-12").evaluate(ctx));
    }
}