package prj.clark.alchemy.tree;

import prj.clark.alchemy.data.AlchemyFloat;
import prj.clark.alchemy.data.AlchemyInt;
import prj.clark.alchemy.data.Numeric;

public class MultiplicationNode extends NumericBinaryOperator {
    public MultiplicationNode(Valued left, Valued right) {
        super(left, right, (l, r) -> {
            Numeric a = (Numeric) l;
            Numeric b = (Numeric) r;
            if (a.isInteger() && b.isInteger()) {
                return AlchemyInt.of(a.intValue() * b.intValue());
            } else {
                return AlchemyFloat.of(a.floatValue() * b.floatValue());
            }
        });
    }
}
