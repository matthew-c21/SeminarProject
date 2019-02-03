package prj.clark.alchemy.data;

import prj.clark.alchemy.env.Context;
import prj.clark.alchemy.err.FunctionInvocationException;
import prj.clark.alchemy.tree.Valued;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Used for functions that are created at runtime. This class should conform to the automatic currying in the language.
 * Instances are assumed to be raw, untyped functions.
 * @see RawFunction
 */
public class GeneratedFunction implements Invokable {
    private final Valued functionBody;
    private final Context enclosingContext;
    private final int argCount;
    private final List<String> arguments;

    // TODO(matthew-c21) - Consider removing this to an external class.
    private static class PartiallyAppliedFunction implements Invokable {
        private final List<Data> suppliedArguments;
        private final Invokable baseFunction;

        public PartiallyAppliedFunction(List<Data> suppliedArguments, Invokable baseFunction) {
            this.suppliedArguments = suppliedArguments;
            this.baseFunction = baseFunction;
        }

        @Override
        public Data invoke(List<Data> args) {
            // TODO(matthew-c21) - Determine whether stream concatenation or the creation of a new list is faster.
            return baseFunction.invoke(Stream.concat(suppliedArguments.stream(), args.stream()).collect(Collectors.toList()));
        }

    }

    /**
     * Create a new GeneratedFunction that may be utilized.
     * @param functionBody the actual body of the function, given as a single node.
     * @param enclosingContext the surrounding context of the function.
     * @param arguments the names of all bound parameters. The length of this List should be the same as argCount.
     */
    public GeneratedFunction(Valued functionBody, Context enclosingContext, List<String> arguments) {
        // TODO(matthew-c21) - Consider taking copies rather than pointers.
        this.functionBody = functionBody;
        this.enclosingContext = enclosingContext;
        this.arguments = arguments;
        this.argCount = arguments.size();
    }

    @Override
    public Data invoke(List<Data> args) {
        if (args.size() < argCount) {
            // return a partial function.
            return new PartiallyAppliedFunction(args, this);
        } else if (args.size() > argCount) {
            throw new FunctionInvocationException("Too many arguments supplied to function.");
        } else {
            // Apply the function using the given function body.
            // TODO(matthew-c21) - Create the new scoped context here so that it can be discarded after use.
            for (int i = 0; i < args.size(); ++i) {
                enclosingContext.bindMutably(arguments.get(i), args.get(i));
            }

            return functionBody.evaluate(enclosingContext);
        }
    }

}