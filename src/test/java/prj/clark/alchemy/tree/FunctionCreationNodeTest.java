package prj.clark.alchemy.tree;

import org.junit.Assert;
import org.junit.Test;
import prj.clark.alchemy.data.*;
import prj.clark.alchemy.env.DefaultContext;
import prj.clark.alchemy.err.LangException;

import java.util.Arrays;
import java.util.Collections;

public class FunctionCreationNodeTest {
    @Test
    public void generatedValueOfCorrectType() throws LangException {
        Valued n = new FunctionCreationNode(new StatementListNode(Collections.emptyList()), Collections.emptyList());
        Assert.assertEquals(RawFunction.getInstance(), n.evaluate(new DummyContext()).getType());
    }

    @Test
    public void generatedEmptyFunctionWorksCorrectly() throws LangException {
        Valued n = new FunctionCreationNode(new StatementListNode(Collections.emptyList()), Collections.emptyList());
        Assert.assertEquals(Empty.get(), ((Function) n.evaluate(new DummyContext())).apply(Collections.emptyList()));
    }

    @Test
    public void generatedBasicFunctionWorksCorrectly() throws LangException {
        StatementListNode body = new StatementListNode(Collections.singletonList(
                new AdditionNode(
                        new IdentifierNode("x"),
                        new AdditionNode(
                                new IdentifierNode("y"),
                                new IdentifierNode("z")
                        )
                )
        ));

        Valued n = new FunctionCreationNode(body, Arrays.asList("x", "y", "z"));
        Assert.assertEquals(AlchemyInt.of(6), ((Function) n.evaluate(new DefaultContext())).apply(Arrays.asList(AlchemyInt.of(1), AlchemyInt.of(2), AlchemyInt.of(3))));
    }
}