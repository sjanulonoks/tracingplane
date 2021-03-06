package edu.brown.cs.systems.tracingplane.baggage_layer;

import java.nio.ByteBuffer;
import edu.brown.cs.systems.tracingplane.atom_layer.BaggageAtoms;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.DataPrefix;

/**
 * <p>
 * An instance of {@link Baggage} with meaningful, tree-multimap structured data. If you are using BaggageBuffers, you
 * should access data through the APIs that are generated by BaggageBuffers. If you are not using BaggageBuffers and
 * instead are using the default {@link GenericBaggageContents}, access data directly by casting BaggageContents
 * instances to GenericBaggageContents.
 * </p>
 */
public interface BaggageContents extends BaggageAtoms {

    public static final ByteBuffer TRIMMARKER_ATOM = ByteBuffer.wrap(new byte[] { DataPrefix.prefix });
    public static final ByteBuffer TRIMMARKER_CONTENTS = (ByteBuffer) TRIMMARKER_ATOM.duplicate().position(1);

    public static final BaggageLayer<?> baggageLayer = BaggageLayerConfig.defaultBaggageLayer();

}
