package app.gotway.euc;

import org.junit.Test;

import app.gotway.euc.ble.DataParser;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void testFloatToStr() throws Exception {
        assertEquals("30", DataParser.floatToStr(30.000f));
    }
}