package prj.clark.alchemy.tree;

import org.junit.Assert;
import org.junit.Test;
import prj.clark.alchemy.data.*;
import prj.clark.alchemy.env.Context;
import prj.clark.alchemy.err.LangException;

import static org.junit.Assert.*;

public class ConditionalTest {
    private static final Valued TRUTHY = new LiteralNode(AlchemyBoolean.of(true));
    private static final Valued FALSEY = new LiteralNode(AlchemyBoolean.of(false));

    // Some garbage data to test against.
    private static final Data RESULT = AlchemyInt.of(3);
    private static final Valued DATA = new LiteralNode(RESULT);
    private static final Valued FAILURE = new FailNode();
    private static final Context ctx = new DummyContext();

    // The use of the failure node ensures that the extra condition is not evaluated.
    private static class FailNode implements Valued {
        @Override
        public Data evaluate(Context ctx) {
            fail();

            // Unreachable.
            return null;
        }
    }

    @Test
    public void trueCaseExecutesFirstNode() throws LangException {
        Valued n = new Conditional(DATA, FAILURE, TRUTHY);
        Assert.assertEquals(RESULT, n.evaluate(ctx));
    }

    @Test
    public void falseCaseExecutesSecondNode() throws LangException {
        Valued n = new Conditional(FAILURE, DATA, FALSEY);
        Assert.assertEquals(RESULT, n.evaluate(ctx));
    }
}