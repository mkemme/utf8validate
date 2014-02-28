package org.mke.utf8validate;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

/**
 * This class checks input stream and verifies if it is UTF-8 encoded If the
 * input stream is UTF-8 compliant and without ByteOrderMark (BOM), then this
 * app exits with exit code = 0. If it is UTF-8 file with BOM, then exit code is
 * 1. For plain ASCII files exit code is 2 If it is not UTF-8 rule compliant
 * file, then exit code is 10
 * 
 * @author Martins Kemme
 * @version $Id: Utf8Validate.java 591 2009-03-20 08:03:01Z mkemme $
 */
public class Utf8Validate {

	/**
	 * Program exit codes
	 * */
	public static final int EXIT_IS_VALID_UTF8_FILE_NOBOM = 0;
	public static final int EXIT_IS_VALID_UTF8_FILE_WITHBOM = 1;
	public static final int EXIT_IS_PLAIN_ASCII = 2;
	public static final int EXIT_NOT_VALID_UTF8_FILE = 10;
	public static final int EXIT_INPUT_STREAM_WAS_EMPTY = 20;

	/**
	 * To get debug messages change variable cVerbosityLevel value to 2
	 * 
	 * @TODO put this parameter as program parameter
	 */
	public static int cVerbosityLevel = 1;

	/**
	 * @param No
	 *            parameters are expected. This class just reads input stream.
	 */
	public static void main(String[] args) {
		DataInputStream is = new DataInputStream(System.in);
		writeLog("Validating UTF-8 compliance for input stream...", 2, true);

		// Do the actual validation
		int returnCode = validateInputStream(is);
		try {
			is.close();
		} catch (IOException ignored) {
		}

		// Display result message
		switch (returnCode) {
		case EXIT_IS_VALID_UTF8_FILE_NOBOM:
			writeLog("Valid UTF-8 file.", 1, true);
			break;
		case EXIT_IS_VALID_UTF8_FILE_WITHBOM:
			writeLog("Valid UTF-8 file with BOM.", 1, true);
			break;
		case EXIT_IS_PLAIN_ASCII:
			writeLog("Plain ASCII file. Valid UTF-8 file.", 1, true);
			break;
		default:
			writeLog("NOT a valid UTF-8 file.", 1, true);
		}

		// Exit with proper exit code
		System.exit(returnCode);
	}

	/**
	 * This is the method that checks if input stream is UTF-8 compliant.
	 * 
	 * @return Returns int exit code (refer to exit code global variables)
	 * */
	public static int validateInputStream(DataInputStream dis) {
		int isValidUtf8File = EXIT_IS_VALID_UTF8_FILE_NOBOM;
		byte ch;
		int multibyteCounter = 0;
		int byteRead;
		long byteCounter = 1;
		int BOMFlag = 0;
		boolean isPlainASCIIFlag = true;
		try {
			/** Perform test while stream seems is valid */
			while (isValidUtf8File < EXIT_NOT_VALID_UTF8_FILE) {
				byteRead = dis.readUnsignedByte();
				ch = (byte) byteRead;

				/**
				 * Set up isPlainASCIIFlag. By default this flag is true. In
				 * case we see a byte with eighth bit set then set this flag to
				 * false
				 * 
				 * */
				if ((ch & 0x80) == 0x80)
					isPlainASCIIFlag = false;

				/**
				 * Check if first three bytes match Byte Oder Mark EF,BB,BF Do
				 * not perform any checks starting from byte No.4
				 */
				if (byteCounter < 4) {
					if ((byteCounter == 1) && ((ch & 0xEF) == 0xEF))
						BOMFlag++;
					if ((byteCounter == 2) && ((ch & 0xBB) == 0xBB))
						BOMFlag++;
					if ((byteCounter == 3) && ((ch & 0xBF) == 0xBF))
						BOMFlag++;
					byteCounter++;
				}

				if (multibyteCounter == 0) {
					/**
					 * There has been no escape byte before, i.e. we are not in
					 * the middle of the escape sequence. Now we have to
					 * calculate how many special 10xxxxxx bytes will follow it
					 */
					if ((ch & 0x80) == 0) {
					} else if ((ch & 0xFE) == 0xFC) {
						multibyteCounter = 5;
					} else if ((ch & 0xFC) == 0xF8) {
						multibyteCounter = 4;
					} else if ((ch & 0xF8) == 0xF0) {
						multibyteCounter = 3;
					} else if ((ch & 0xF0) == 0xE0) {
						multibyteCounter = 2;
					} else if ((ch & 0xE0) == 0xC0) {
						multibyteCounter = 1;
					} else
						isValidUtf8File = EXIT_NOT_VALID_UTF8_FILE;
				} else {
					/**
					 * We have seen escape byte beforehand now we have to
					 * validate that this byte is matching binary pattern
					 * 10xxxxxx because only such bytes are allowed to follow
					 * escape bytes. We will reduce multibyte counter as we see
					 * these bytes.
					 */
					if ((ch & 0xC0) == 0x80) {
						multibyteCounter--;
					} else
						isValidUtf8File = EXIT_NOT_VALID_UTF8_FILE;
				}

				/**
				 * Check if there are not too many special bytes following
				 * escape byte
				 */
				if (multibyteCounter < 0)
					isValidUtf8File = EXIT_NOT_VALID_UTF8_FILE;

				// Write some debugging output
				writeLog(">> Char: " + (char) ch + " Hex: "
						+ Integer.toHexString(byteRead) + " Integer: "
						+ byteRead + "\tMultibyte=" + multibyteCounter
						+ "  IsValid=" + isValidUtf8File + "  BOM flag="
						+ BOMFlag + "  ByteCounter=" + byteCounter, 2, true);
			}
		} catch (EOFException eof) {
			writeLog(" >> EOF reached. Normal program termination.", 2, true);
		} catch (IOException io) {
			writeLog("I/O error occurred: " + io, 1, false);
		} catch (Throwable anything) {
			writeLog("Abnormal exception caught !: " + anything, 1, false);
		} finally {
		}

		/**
		 * if we are in the middle of the escape sequence but we have reached
		 * EOF, then this is not a valid UTF-8 file
		 */
		if (multibyteCounter > 0)
			isValidUtf8File = EXIT_NOT_VALID_UTF8_FILE;

		/**
		 * Now process results and generate exit code
		 */
		if (byteCounter == 1) {
			// no bytes seen - this must be empty string
			return EXIT_INPUT_STREAM_WAS_EMPTY;
		}

		if (isPlainASCIIFlag) {
			// Many bytes, but only 7 bits seen - this must be ASCII
			return EXIT_IS_PLAIN_ASCII;
		}

		if ((isValidUtf8File == EXIT_IS_VALID_UTF8_FILE_NOBOM)
				&& (BOMFlag == 3))
			// if this is a valid UTF-8 file and we have seen 3 BOM bytes, then
			// this is "valid utf-8 file with BOM"
			return EXIT_IS_VALID_UTF8_FILE_WITHBOM;

		// This is not a special case and this must be an UTF-8 file
		return isValidUtf8File;
	}

	/**
	 * Display all messages in a consistent way and enable logging level control
	 * 
	 * @param level
	 *            = 0: normal msg level = 1: verbose level = 2: max verbosity
	 *            and debug
	 * 
	 *            infoMessage = true: informative message infoMessage = false:
	 *            error message
	 */
	private static void writeLog(String msg, int level, boolean infoMessage) {
		if (level <= cVerbosityLevel) {
			if (infoMessage) {
				System.out.println(msg);
				System.out.flush();
			} else {
				System.err.println(msg);
				System.err.flush();
			}
		}
	}
}