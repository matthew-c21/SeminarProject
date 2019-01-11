package prj.clark.alchemy.env;

/**
 * This is a very general top-level case for data. The given type should be
 * used for casting to an appropriate subclass. This approach is used to to the
 * fact that underlying data doesn't map very cleanly to how Java handles data.
 */
// TODO(matthew-c21) - Consider whether adding a type parameter from the PrimitiveData class would be appropriate here.
public interface Data {
    DataType getType();
}