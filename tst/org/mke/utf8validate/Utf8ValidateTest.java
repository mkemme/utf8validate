package org.mke.utf8validate;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import org.junit.Before;
import org.junit.Test;

public class Utf8ValidateTest {
	Utf8Validate validator;

	@Before
	public void setUp() throws Exception {
		validator = new Utf8Validate();
	}

	@Test
	public void testValidateInputStreamForRegularUTF8WithoutBOM() {
		byte[] data_bytes = { (byte) 0xc5, (byte) 0xa0, (byte) 0x69,
				(byte) 0x73 };
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(
				data_bytes));

		int result = Utf8Validate.validateInputStream(dis);
		assertEquals(
				"One special UTF-8 encoded character and two ASCII chars.",
				Utf8Validate.EXIT_IS_VALID_UTF8_FILE_NOBOM, result);
	}

	@Test
	public void testValidateInputStreamForUTF8WithBOM() {
		byte[] data_bytes = { (byte) 0xef, (byte) 0xbb, (byte) 0xbf,
				(byte) 0xc0, (byte) 0xa0, (byte) 0x69 };
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(
				data_bytes));

		int result = Utf8Validate.validateInputStream(dis);
		assertEquals("Check input stream with BOM.",
				Utf8Validate.EXIT_IS_VALID_UTF8_FILE_WITHBOM, result);
	}

	@Test
	public void testValidateInputStreamForNonUTF8() {
		byte[] data_bytes = { (byte) 0xd0, (byte) 0x69, (byte) 0x73,
				(byte) 0x20, (byte) 0x69, (byte) 0x72 };
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(
				data_bytes));

		int result = Utf8Validate.validateInputStream(dis);
		assertEquals("Check input stream with BOM.",
				Utf8Validate.EXIT_NOT_VALID_UTF8_FILE, result);
	}

	@Test
	public void testValidateInputStreamForPoorUTF8Encoding1() {
		byte[] data_bytes = { (byte) 0xC2, (byte) 0xA9, (byte) 0xA9 };
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(
				data_bytes));

		int result = Utf8Validate.validateInputStream(dis);
		assertEquals(
				"Two special bytes following flagging byte, while flagging byte tells, that only one byte must follow.",
				Utf8Validate.EXIT_NOT_VALID_UTF8_FILE, result);
	}

	@Test
	public void testValidateInputStreamForPoorUTF8Encoding2() {
		byte[] data_bytes = { (byte) 0xC2, (byte) 0x20 };
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(
				data_bytes));

		int result = Utf8Validate.validateInputStream(dis);
		assertEquals(
				"Flag shows that one more special byte will follow, but there is 7-bit byte following it.",
				Utf8Validate.EXIT_NOT_VALID_UTF8_FILE, result);
	}

	@Test
	public void testValidateInputStreamForPoorUTF8Encoding3() {
		byte[] data_bytes = { (byte) 0xC2 };
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(
				data_bytes));

		int result = Utf8Validate.validateInputStream(dis);
		assertEquals(
				"Flag shows that one more special byte will follow, but there is EOF following it.",
				Utf8Validate.EXIT_NOT_VALID_UTF8_FILE, result);
	}

	@Test
	public void testValidateInputStreamCheckIncompleteBOM() {
		byte[] data_bytes = { (byte) 0xef, (byte) 0xbb };
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(
				data_bytes));

		int result = Utf8Validate.validateInputStream(dis);
		assertEquals(
				"Flag shows that one more special byte will follow, but there is EOF following it.",
				Utf8Validate.EXIT_NOT_VALID_UTF8_FILE, result);
	}

	@Test
	public void testValidateInputStreamCheckBannedByteFE() {
		byte[] data_bytes = { (byte) 0x20, (byte) 0xfe, (byte) 0x20,
				(byte) 0x20 };
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(
				data_bytes));

		int result = Utf8Validate.validateInputStream(dis);
		assertEquals("Byte 0xFE must never occur in UTF-8 file.",
				Utf8Validate.EXIT_NOT_VALID_UTF8_FILE, result);
	}

	@Test
	public void testValidateInputStreamCheckBannedByteFF() {
		byte[] data_bytes = { (byte) 0xFF, (byte) 0x20, (byte) 0x20 };
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(
				data_bytes));

		int result = Utf8Validate.validateInputStream(dis);
		assertEquals("Byte 0xFF must never occur in UTF-8 file.",
				Utf8Validate.EXIT_NOT_VALID_UTF8_FILE, result);
	}

	@Test
	public void testValidateInputStreamForPlainASCII() {
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(
				"Plain ASCII text - the quick brown fox jumps over a lazy dog."
						.getBytes()));

		int result = Utf8Validate.validateInputStream(dis);
		assertEquals("Plain ASCII files must be recognized as ASCII files.",
				Utf8Validate.EXIT_IS_PLAIN_ASCII, result);
	}

	@Test
	public void testValidateInputStreamForEmptyStream() {
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(""
				.getBytes()));

		int result = Utf8Validate.validateInputStream(dis);
		assertEquals("Empty stream must return its own result code",
				Utf8Validate.EXIT_INPUT_STREAM_WAS_EMPTY, result);
	}
}
