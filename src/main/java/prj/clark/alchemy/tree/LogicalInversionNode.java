package prj.clark.alchemy.tree;

import prj.clark.alchemy.data.*;
import prj.clark.alchemy.env.Context;

public class LogicalInversionNode implements Node {
    private Node node;

    public LogicalInversionNode(Node node) {
        this.node = node;
    }

    @Override
    public Data evaluate(Context ctx) {
        // TODO(matthew-c21) - Find a good way to cache this value.
        boolean result = node.evaluate(ctx).toBoolean();

        return AlchemyBoolean.of(! result);
    }
}
