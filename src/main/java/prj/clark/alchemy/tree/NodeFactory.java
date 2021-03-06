package prj.clark.alchemy.tree;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import prj.clark.alchemy.AlchemyParser;
import prj.clark.alchemy.data.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * This is basically just a sample class for converting the expression nodes of a parse tree into chunks of an AST.
 * It does not cover a solid chunk of the actual language.
 */
public class NodeFactory {
    private static final Map<String, BiFunction<Valued, Valued, Valued>> BINOP_SUPPLIER = new HashMap<>();

    static {
        BINOP_SUPPLIER.put("^", ExponentialNode::new);
        BINOP_SUPPLIER.put("*", MultiplicationNode::new);
        BINOP_SUPPLIER.put("/", DivisionNode::new);
        BINOP_SUPPLIER.put("%", ModulusNode::new);
        BINOP_SUPPLIER.put("+", AdditionNode::new);
        BINOP_SUPPLIER.put("-", SubtractionNode::new);
        BINOP_SUPPLIER.put("<", LessThanNode::new);
        BINOP_SUPPLIER.put(">", GreaterThanNode::new);
        BINOP_SUPPLIER.put("<=", LessThanEqualNode::new);
        BINOP_SUPPLIER.put(">=", GreaterThanEqualNode::new);
        BINOP_SUPPLIER.put("==", EqualNode::new);
        BINOP_SUPPLIER.put("!=", NotEqualNode::new);
        BINOP_SUPPLIER.put("<<", FeedLastNode::new);
        BINOP_SUPPLIER.put(">>", FeedFirstNode::new);
        BINOP_SUPPLIER.put("::", ConcatenationNode::new);
        BINOP_SUPPLIER.put("and", AndNode::new);
        BINOP_SUPPLIER.put("or", OrNode::new);
        BINOP_SUPPLIER.put("++", StringConcatenationNode::new);
    }

    public List<Node> getAll(AlchemyParser.FileContext ctx) {
        return ctx.statement().
                stream()
                .map(this::get)
                .collect(Collectors.toList());
    }

    public Node get(AlchemyParser.FunctionDeclarationContext ctx) {
        return new BindingNode(ctx.IDENTIFIER().getText(), get(ctx.lambda()), BindingNode.BindingType.FUNCTION);
    }

    public BindingNode get(AlchemyParser.AssignmentContext ctx) {
        if (ctx.binding().tupleIdentifier() != null) {
            throw new UnsupportedOperationException("Cannot support multiple bindings at this time.");
        }

        return new BindingNode(ctx.binding().IDENTIFIER().getText(), get(ctx.expression()), BindingNode.BindingType.VALUE);
    }

    public Node get(AlchemyParser.StatementContext ctx) {
        if (ctx.assignment() != null) {
            return get(ctx.assignment());
        }

        if (ctx.functionDeclaration() != null) {
            return get(ctx.functionDeclaration());
        }

        if (ctx.expression() != null) {
            return get(ctx.expression());
        }

        throw new UnsupportedOperationException("Encountered unrecognized statement format.\n" + ctx.getText());
    }

    public Valued get(AlchemyParser.DictContext ctx) {
        // TODO(matthew-c21) - Determine how dictionaries are to be formed during runtime.
        return null;
    }

    public Valued get(AlchemyParser.ExpressionContext ctx) {
        if (ctx.nested != null) {
            return get(ctx.expression(0));
        }

        if (ctx.inverse != null) {
            return new LogicalInversionNode(get(ctx.expression(0)));
        }

        if (ctx.neg != null) {
            return new UnaryNegationNode(get(ctx.expression(0)));
        }

        if (ctx.op != null) {
            return BINOP_SUPPLIER.get(ctx.op.getText()).apply(get(ctx.left), get(ctx.right));
        }

        if (ctx.func != null) {
            return new FunctionApplicationNode(
                    get(ctx.func),
                    ctx.tuple().expressionList().expression()
                            .stream()
                            .map(this::get).collect(Collectors.toList()));
        }

        if (ctx.infix != null) {
            return new FunctionApplicationNode(
                    get(ctx.infix),
                    Arrays.asList(get(ctx.left), get(ctx.right)));
        }

        if (ctx.lambda() != null) {
            return get(ctx.lambda());
        }

        if (ctx.tuple() != null) {
            return get(ctx.tuple());
        }

        if (ctx.list() != null) {
            return get(ctx.list());
        }

        if (ctx.range() != null) {
            return get(ctx.range());
        }

        if (ctx.dict() != null) {
            return get(ctx.dict());
        }

        if (ctx.slice() != null) {
            if (ctx.slice().index != null) {
                return new ListAccessNode(get(ctx.expression(0)), get(ctx.slice().index));
            }

            SliceNode.SliceNodeBuilder snb = new SliceNode.SliceNodeBuilder(get(ctx.expression(0)));

            if (ctx.slice().start != null) {
                snb.setStart(get(ctx.slice().start));
            }

            if (ctx.slice().end != null) {
                snb.setStop(get(ctx.slice().end));
            }

            if (ctx.slice().skip != null) {
                snb.setSkip(get(ctx.slice().skip));
            }

            return snb.build();
        }

        if (ctx.cond != null) {
            return new Conditional(get(ctx.ifTrue), get(ctx.ifFalse), get(ctx.cond));
        }

        if (ctx.terminal != null) {
            if (ctx.IDENTIFIER() != null) {
                return new IdentifierNode(ctx.IDENTIFIER().getText());
            }

            if (ctx.FLOAT() != null) {
                return new LiteralNode(AlchemyFloat.of(ctx.FLOAT().getText()));
            } else if (ctx.INT() != null) {
                return new LiteralNode(AlchemyInt.of(ctx.INT().getText()));
            } else if (ctx.BOOL() != null) {
                return new LiteralNode(AlchemyBoolean.of(ctx.BOOL().getText()));
            } else if (ctx.CHAR() != null) {
                TerminalNode n = ctx.CHAR();
                return new LiteralNode(AlchemyCharacter.of(n.getText().substring(1, n.getText().length() - 1)));
            } else if (ctx.STRING() != null) {
                TerminalNode n = ctx.STRING();
                return new LiteralNode(AlchemyString.of(n.getText().substring(1, n.getText().length() - 1)));
            } else {
                throw new IllegalStateException("Could not resolve \"" + ctx.terminal.getText() + "\" to a value.");
            }
        }

        throw new UnsupportedOperationException("Unable to parse \"" + ctx.getText() + "\" to an expression.");
    }

    public Valued get(AlchemyParser.RangeContext ctx) {
        Valued first = ctx.start == null ? null : get(ctx.start);
        Valued second = ctx.second == null ? null : get(ctx.second);
        Valued last = ctx.end == null ? null : get(ctx.end);

        return new RangeCreationNode(first, second, last);
    }

    public Valued get(AlchemyParser.LambdaContext ctx) {
        // TODO(matthew-c21) - Add an injectible set of bindings utilizing the with block.
        return new FunctionLiteralNode(
                get(ctx.statementBody()),
                ctx.IDENTIFIER().stream().map(ParseTree::getText).collect(Collectors.toList()),
                ctx.withBlock() == null ?
                        Collections.emptyList()
                        : ctx.withBlock().assignment().stream().map(this::get).collect(Collectors.toList()));
    }

    public Valued get(AlchemyParser.StatementBodyContext ctx) {
        return new StatementListNode(ctx.statement().stream().map(this::get).collect(Collectors.toList()));
    }

    public Valued get(AlchemyParser.ListContext ctx) {
        return new ListLiteralNode(ctx.expressionList().expression().stream().map(this::get).collect(Collectors.toList()));
    }

    public Valued get(AlchemyParser.TupleContext ctx) {
        return new TupleLiteralNode(ctx.expressionList().expression().stream().map(this::get).collect(Collectors.toList()));
    }
}
