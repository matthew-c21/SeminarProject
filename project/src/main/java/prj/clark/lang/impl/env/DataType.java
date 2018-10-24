package prj.clark.lang.impl.env;

/**
 * This interface is used for type checking of {@link Data} objects at runtime.
 */
public interface DataType {
    boolean ofType(DataType dt);
}