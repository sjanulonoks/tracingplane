package edu.brown.cs.systems.baggage.context_layer.types;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.Test;

import edu.brown.cs.systems.baggage.context_layer.DataLayerException;
import edu.brown.cs.systems.baggage.context_layer.Utils;
import edu.brown.cs.systems.baggage.context_layer.types.Lexicographic;
import edu.brown.cs.systems.baggage.context_layer.types.UnsignedLexVarInt;
import junit.framework.TestCase;

public class TestXUnsignedVarint32 extends TestCase {
	
	@Test
	public void testLexVarInt32() throws DataLayerException {
		ByteBuffer b = ByteBuffer.allocate(1);
		for (int i = 0; i < 128; i++) {
			b.rewind();
			b.put((byte) i);
			b.rewind();

			assertEquals(i, UnsignedLexVarInt.readLexVarUInt32(b));
			b.rewind();
			assertEquals(i, UnsignedLexVarInt.readLexVarUInt64(b));
			
			assertEquals(1, UnsignedLexVarInt.encodedLength(i));
		}
		
		for (int i = 128; i < 256; i++) {
			b.rewind();
			b.put((byte) i);
			b.rewind();
			try {
				UnsignedLexVarInt.readLexVarUInt32(b);
				fail("Should not be able to read lexvarint " + Utils.toBinaryString((byte) i) + " due to insufficient bytes");
			} catch (BufferUnderflowException e) {
				// success
			}
		}
	}
	
	@Test
	public void testLexVarInt32_2() throws DataLayerException {
		ByteBuffer b = ByteBuffer.allocate(2);
		
		int expect = 0;
		for (int b0 = 128; b0 < 192; b0++) {
			for (int b1 = 0; b1 < 256; b1++) {
				b.rewind();
				b.put((byte) b0);
				b.put((byte) b1);
				b.rewind();
				
				if (b0 > 128) {
					assertEquals(2, UnsignedLexVarInt.encodedLength(expect));
				}
				assertEquals(expect, UnsignedLexVarInt.readLexVarUInt32(b));
				b.rewind();
				assertEquals(expect++, UnsignedLexVarInt.readLexVarUInt64(b));
			}
		}
	}
	
	@Test
	public void testLexVarInt32_3() throws DataLayerException {
		ByteBuffer b = ByteBuffer.allocate(3);
		
		int expect = 0;
		for (int b0 = 192; b0 < 224; b0++) {
			for (int b1 = 0; b1 < 256; b1++) {
				for (int b2 = 0; b2 < 256; b2++) {
					b.rewind();
					b.put((byte) b0);
					b.put((byte) b1);
					b.put((byte) b2);
					b.rewind();

					if (b0 > 192) {
						assertEquals(3, UnsignedLexVarInt.encodedLength(expect));
					}
					assertEquals(expect, UnsignedLexVarInt.readLexVarUInt32(b));
					b.rewind();
					assertEquals(expect++, UnsignedLexVarInt.readLexVarUInt64(b));
				}
			}
		}
	}
	
	@Test
	public void testLexVarInt32_4() throws DataLayerException {
		ByteBuffer b = ByteBuffer.allocate(4);
		
		int expect = 0;
		for (int b0 = 224; b0 < 240; b0++) {
			for (int b1 = 0; b1 < 256; b1++) {
				for (int b2 = 0; b2 < 256; b2++) {
					for (int b3 = 0; b3 < 256; b3++) {
						b.rewind();
						b.put((byte) b0);
						b.put((byte) b1);
						b.put((byte) b2);
						b.put((byte) b3);
						b.rewind();

						if (b0 > 224) {
							assertEquals(4, UnsignedLexVarInt.encodedLength(expect));
						}
						assertEquals(expect, UnsignedLexVarInt.readLexVarUInt32(b));
						b.rewind();
						assertEquals(expect++, UnsignedLexVarInt.readLexVarUInt64(b));
					}
				}
			}
		}
	}
	
	@Test
	public void testBigLexVarInt32() throws DataLayerException {
		ByteBuffer b = ByteBuffer.allocate(5);
		b.put((byte) -16);
		b.putInt(Integer.MAX_VALUE);
		b.rewind();
		assertEquals(Integer.MAX_VALUE, UnsignedLexVarInt.readLexVarUInt32(b));

		b.rewind();
		b.put((byte) -16);
		b.putInt(Integer.MIN_VALUE);
		b.rewind();
		assertEquals(Integer.MIN_VALUE, UnsignedLexVarInt.readLexVarUInt32(b));
		
		int[] bad_prefixes = { -8, -4, -2, -1 };
		for (int bad : bad_prefixes) {
			b.rewind();
			b.put((byte) bad);
			b.putInt(Integer.MAX_VALUE);
			b.rewind();
			try {
				UnsignedLexVarInt.readLexVarUInt32(b);
				fail("Expected buffer underflow due to insufficient bits");
			} catch (BufferUnderflowException e) {
			}
		}
		
	}
	
	@Test
	public void testEstimatedSize() {
		assertEquals(1, UnsignedLexVarInt.encodedLength(0));
		assertEquals(2, UnsignedLexVarInt.encodedLength(128));
		assertEquals(3, UnsignedLexVarInt.encodedLength(128*256));
		assertEquals(4, UnsignedLexVarInt.encodedLength(128*256*256));
		assertEquals(5, UnsignedLexVarInt.encodedLength(128*256*256*256));
		assertEquals(5, UnsignedLexVarInt.encodedLength(Integer.MAX_VALUE));
		assertEquals(5, UnsignedLexVarInt.encodedLength(Integer.MIN_VALUE));
		assertEquals(5, UnsignedLexVarInt.encodedLength(-1));
	}
	
	
	@Test
	public void testLexVarPrefixSize() throws DataLayerException {
		for (int i = 0; i < 128; i++) {
			assertEquals(1, UnsignedLexVarInt.interpretSize((byte) i));
		}
		for (int i = 128; i < 192; i++) {
			assertEquals(2, UnsignedLexVarInt.interpretSize((byte) i));
		}
		for (int i = 192; i < 224; i++) {
			assertEquals(3, UnsignedLexVarInt.interpretSize((byte) i));
		}
		for (int i = 224; i < 240; i++) {
			assertEquals(4, UnsignedLexVarInt.interpretSize((byte) i));
		}
		for (int i = 240; i < 248; i++) {
			assertEquals(5, UnsignedLexVarInt.interpretSize((byte) i));
		}
		for (int i = 248; i < 252; i++) {
			assertEquals(6, UnsignedLexVarInt.interpretSize((byte) i));
		}
		assertEquals(7, UnsignedLexVarInt.interpretSize((byte) 252));
		assertEquals(7, UnsignedLexVarInt.interpretSize((byte) 253));
		assertEquals(8, UnsignedLexVarInt.interpretSize((byte) 254));
		assertEquals(9, UnsignedLexVarInt.interpretSize((byte) 255));
	}
	
	
	@Test
	public void testWriteReadLexVarUInt32() throws DataLayerException {
		Random r = new Random(0);
		int numtests = 1000;

		int min = 0;
		int max = 128;
		for (int size = 1; size < 5; size++) {
			ByteBuffer b = ByteBuffer.allocate(size);
			for (int i = 0; i < numtests; i++) {
				int value = r.nextInt(max-min) + min;
				assertTrue(value >= min);
				assertTrue(value < max);
				
				b.rewind();
				int sizeWritten = UnsignedLexVarInt.writeLexVarUInt32(b, value);
				assertEquals(size, sizeWritten);

				b.rewind();
				int valueRead = UnsignedLexVarInt.readLexVarUInt32(b);
				assertEquals(value, valueRead);
			}
			
			min = max;
			max = min * 128;
		}
		
		ByteBuffer b = ByteBuffer.allocate(5);
		for (int i = 0; i < numtests; i++) {
			int value = -r.nextInt(Integer.MAX_VALUE)-1;
			assertTrue(value < 0);
			
			b.rewind();
			int sizeWritten = UnsignedLexVarInt.writeLexVarUInt32(b, value);
			assertEquals(5, sizeWritten);

			b.rewind();
			long valueRead = UnsignedLexVarInt.readLexVarUInt32(b);
			assertEquals(value, valueRead);
			
		}
	}

	
	private static Random r = new Random(10);
	private static int generate(int size) {
		long min = 0;
		int max = 128;
		for (int i = 1; i < size; i++) {
			min = max;
			max *= 128;
		}
		
		long value;
		do {
			value = r.nextLong() % (max-min);
		} while (value < 0);
		value += min;
		return (int) value;
	}
	
	@Test
	public void testUnsignedVarint32Comparison() {
		byte[] imax = Lexicographic.writeVarUInt32(Integer.MAX_VALUE);
		byte[] imax2 = Lexicographic.writeVarUInt32(-1);
		
		assertTrue(Lexicographic.compare(imax, imax2) < 0);
		

		int numtests = 100;
		for (int sizea = 1; sizea <= 5; sizea++) {
			ByteBuffer bufa = ByteBuffer.allocate(sizea);
			for (int sizeb = sizea; sizeb <= 5; sizeb++) {
				ByteBuffer bufb = ByteBuffer.allocate(sizeb);
				for (int i = 0; i < numtests; i++) {
					int a = generate(sizea);
					int b = generate(sizeb);
					
					bufa.rewind();
					assertEquals(sizea, UnsignedLexVarInt.writeLexVarUInt32(bufa, a));
					
					bufb.rewind();
					assertEquals(sizeb, UnsignedLexVarInt.writeLexVarUInt32(bufb, b));

					boolean a_smaller = a >= 0 ? (b < 0 || a < b) : (b < 0 && a < b);
					
					assertEquals(a==b, Lexicographic.compare(bufa.array(), bufb.array()) == 0);
					assertEquals(a_smaller, Lexicographic.compare(bufa.array(), bufb.array()) < 0);
					assertEquals(!a_smaller, Lexicographic.compare(bufb.array(), bufa.array()) < 0);
				}
			}
		}
	}
}