package edu.brown.cs.systems.tracingplane.atom_layer.types;

import static org.junit.Assert.assertEquals;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Random;
import org.junit.Test;
import edu.brown.cs.systems.tracingplane.atom_layer.types.ProtobufVarint.MalformedVarintException;

public class TestProtobufVarint {

    @Test
    public void testVarintSize() {
        int numtests = 1000;
        Random r = new Random(0);
        for (int i = 0; i < 5; i++) {
            int min = (int) Math.pow(2, 7 * i);
            int max = (int) Math.min(Integer.MAX_VALUE, Math.pow(2, 7 * (i + 1)));

            for (int j = 0; j < numtests; j++) {
                int value = r.nextInt(max - min) + min;
                assertEquals(i + 1, ProtobufVarint.sizeOf(value));
            }
        }
    }

    @Test
    public void testVarintCorrect() throws MalformedVarintException, BufferUnderflowException {
        int numtests = 1000;
        Random r = new Random(0);
        for (int i = 0; i < 5; i++) {
            int min = (int) Math.pow(2, 7 * i);
            int max = (int) Math.min(Integer.MAX_VALUE, Math.pow(2, 7 * (i + 1)));

            for (int j = 0; j < numtests; j++) {
                int value = r.nextInt(max - min) + min;

                ByteBuffer b = ByteBuffer.allocate(i + 1);
                ProtobufVarint.writeRawVarint32(b, value);
                b.rewind();

                assertEquals(value, ProtobufVarint.readRawVarint32(b));
            }
        }
    }

}
