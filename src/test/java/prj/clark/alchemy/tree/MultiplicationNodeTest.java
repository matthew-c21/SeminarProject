package prj.clark.alchemy.tree;

import prj.clark.alchemy.data.Data;

public class MultiplicationNodeTest extends NumericBinaryOperatorNodeTest {

    @Override
    protected Data apply(Valued a, Valued b) {
        return new MultiplicationNode(a, b).evaluate(null);
    }

    @Override
    protected double apply(double a, double b) {
        return a * b;
    }
}
