package prj.clark.alchemy.data;

import org.junit.After;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.junit.Assert.*;

public class EagerAlchemyListTest {
    private EagerAlchemyList list;

    // Easy use factory methods.
    private static Data f64(double d) {
        return AlchemyFloat.of(d);
    }

    private static Data i64(long l) {
        return AlchemyInt.of(l);
    }

    private static Data str(String s) {
        return AlchemyString.of(s);
    }

    private static Data ch(int cp) {
        return AlchemyCharacter.of(cp);
    }

    @After
    public void tearDown() {
        list = null;
    }

    private void init(Data... ds) {
        list = new EagerAlchemyList(Arrays.asList(ds));
    }

    @Test
    public void iteratorContainsCorrectElements() {
        Data[] data = new Data[]{i64(12), str("asfd")};
        init(data);

        Iterator i = list.iterator();
        for (Data d : data) {
            assertEquals(d, i.next());
        }

        assertFalse(i.hasNext());
    }

    @Test
    public void equalListsShareHashCodes() {
        Data[] data = new Data[]{i64(1), i64(2), i64(3)};
        init(data);

        EagerAlchemyList list2 = new EagerAlchemyList(Arrays.asList(i64(1), i64(2), i64(3)));
        assertEquals(list.hashCode(), list2.hashCode());
    }

    @Test
    public void unequalListsDoNotShareHashCodes() {
        Data[] data = new Data[]{i64(1), i64(2), i64(3)};
        init(data);

        EagerAlchemyList list2 = new EagerAlchemyList(Arrays.asList(i64(2), i64(2), i64(3)));
        assertNotEquals(list.hashCode(), list2.hashCode());
    }

    @Test
    public void toStringBehavesCorrectly() {
        Data[] data = new Data[]{i64(1), i64(2), i64(3), i64(4), i64(5)};
        init(data);
        assertEquals("[1, 2, 3, 4, 5]", list.toString());
    }

    @Test
    public void toStringAndPrintIdentical() {
        Data[] data = new Data[]{i64(1), i64(2), i64(3), i64(4), i64(5)};
        init(data);
        assertEquals(list.toString(), list.print());
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeIndexThrowsException() {
        Data[] data = new Data[]{i64(1), i64(2), i64(3), i64(4), i64(5)};
        init(data);
        list.getIndex(AlchemyInt.of(-1));
    }
}