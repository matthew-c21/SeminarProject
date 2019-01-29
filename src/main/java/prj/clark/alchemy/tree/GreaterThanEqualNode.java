package prj.clark.alchemy.tree;

import prj.clark.alchemy.data.AlchemyBoolean;

public class GreaterThanEqualNode extends NumericBinaryOperator {

    public GreaterThanEqualNode(Valued left, Valued right) {
        super(left, right, (l, r) ->
                AlchemyBoolean.of(Double.parseDouble(l.toString()) >= Double.parseDouble(r.toString())));
    }
}
