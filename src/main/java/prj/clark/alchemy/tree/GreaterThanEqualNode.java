package prj.clark.alchemy.tree;

import prj.clark.alchemy.data.AlchemyBoolean;
import prj.clark.alchemy.data.Numeric;

public class GreaterThanEqualNode extends NumericBinaryOperator {

    public GreaterThanEqualNode(Valued left, Valued right) {
        super(left, right, (l, r) -> {
            Numeric a = (Numeric) l;
            Numeric b = (Numeric) r;
            return AlchemyBoolean.of(a.floatValue() >= b.floatValue());
        });
    }
}
