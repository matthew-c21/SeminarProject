package prj.clark.alchemy.data;

import org.junit.Test;
import prj.clark.alchemy.err.StringFormatException;

import java.util.Iterator;

import static org.junit.Assert.*;

public class AlchemyStringTest {
    @Test
    public void toStringReturnsCorrectStringValue() {
        Data s = AlchemyString.of("asdf");
        Data t = AlchemyString.of("lkjlkhkj");

        assertEquals("asdf", s.toString());
        assertEquals("lkjlkhkj", t.toString());
    }

    @Test
    public void toStringBehavesCorrectly() {
        Data s = AlchemyString.of("123");
        assertEquals("123", s.toString());
    }

    @Test
    public void ensurePrintAndToStringSame() {
        AlchemyString s = AlchemyString.of("asdf");
        AlchemyString t = AlchemyString.of("jlk;");
        assertEquals(s.toString(), s.print());
        assertEquals(t.toString(), t.print());
    }

    @Test
    public void equalValuesEqual() {
        Data s = AlchemyString.of("hello");
        // While the call to new is redundant, it does allow for me to ensure that there isn't an underlying bug being
        // hidden by the caching of the string literal.
        Data t = AlchemyString.of(new String("hello"));
        assertEquals(s, t);
    }

    @Test
    public void unequalValuesNotEqual() {
        Data s = AlchemyString.of("hello");
        Data t = AlchemyString.of("goodbye");
        assertNotEquals(s, t);
    }

    @Test
    public void equalValuesShareHash() {
        assertEquals(AlchemyString.of("hello").hashCode(), AlchemyString.of("hello").hashCode());
    }

    @Test
    public void unqualValuesDoNotShareHash() {
        assertNotEquals(AlchemyString.of("abc").hashCode(), AlchemyString.of("xyzasdf").hashCode());
    }

    @Test
    public void truthinessIsCorrect() {
        assertTrue(AlchemyString.of("a").toBoolean());
        assertFalse(AlchemyString.of("").toBoolean());
    }

    @Test
    public void stringEscapedCorrectly() {
        Data s1 = AlchemyString.of("\\t \\n \\\\ \\r \\f \\\" \\'");
        assertEquals("\t \n \\ \r \f \" '", s1.toString());

        Data s2 = AlchemyString.of("\\\\n");
        assertEquals("\\n", s2.toString());
    }

    @Test
    public void unicodeEscapedCorrectly() {
        // For whatever reason, identical string representations aren't being recognized as equal, and that's really
        // getting on my nerves. The strings are of length 1, so just checking the lead character will work.
        Data s = AlchemyString.of("\\u263a");
        assertEquals(1, s.toString().length());
        assertEquals("\u263a️".charAt(0), s.toString().charAt(0));

        Data s2 = AlchemyString.of("\\u26D4");
        assertEquals(1, s2.toString().length());
        assertEquals("⛔️".charAt(0), s2.toString().charAt(0));
    }

    @Test(expected = StringFormatException.class)
    public void incompleteUnicodeEscapeThrowsException() {
        Data s = AlchemyString.of("\\u1fd");
    }

    @Test(expected = StringFormatException.class)
    public void nonHexUnicodeEscapeThrowsException() {
        Data s = AlchemyString.of("\\uabc-");
    }

    @Test(expected = StringFormatException.class)
    public void invalidEscapesFail() {
        AlchemyString.of("\\k");
    }

    @Test(expected = StringFormatException.class)
    public void incompleteEscapesFail() {
        AlchemyString.of("This string continues onto the next\\nline\\");
    }

    @Test
    public void iterationOccursCorrectly() {
        String expected = "This is a long sentence featuring\n" +
                "\tmultiple lines\n" +
                "\tdifferent escapes\n" +
                "\t\"embedded 'quotes'\"";

        Indexed actual = AlchemyString.of("This is a long sentence featuring\\n" +
                "\\tmultiple lines\\n" +
                "\\tdifferent escapes\\n" +
                "\\t\\\"embedded \\'quotes\\'\\\"");

        for (int i = 0; i < expected.length(); ++i) {
            assertEquals(AlchemyCharacter.of(expected.charAt(i)), actual.getIndex(AlchemyInt.of(i)).get());
        }
    }
}