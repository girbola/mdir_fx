package common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConversionTest {

	@Test
	void testStringWithDigits() {
		assertEquals("02", Conversion.stringWithDigits(2, 2));
		assertEquals("002", Conversion.stringWithDigits(2, 3));
		assertEquals("0002", Conversion.stringWithDigits(2, 4));
		
	}

}
