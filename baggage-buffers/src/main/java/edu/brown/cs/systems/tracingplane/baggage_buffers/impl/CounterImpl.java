package edu.brown.cs.systems.tracingplane.baggage_buffers.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;
import edu.brown.cs.systems.tracingplane.atom_layer.types.AtomLayerException;
import edu.brown.cs.systems.tracingplane.atom_layer.types.UnsignedLexVarint;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Bag;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.BaggageHandler;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.SpecialTypes.Counter;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageReader;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageWriter;

public class CounterImpl implements Counter {

    private static final Random r = new Random(System.currentTimeMillis()); // TODO: properly seed rng uniquely

    private BagKey componentId = null;
    private Map<BagKey, Long> componentValues = null;

    @Override
    public BaggageHandler<?> handler() {
        return Handler.instance;
    }

    private BagKey newComponentId() {
        while (true) {
            ByteBuffer id = ByteBuffer.allocate(4);
            r.nextBytes(id.array());
            BagKey key = BagKey.keyed(id);
            if (!componentValues.containsKey(key)) {
                return key;
            }
        }
    }

    @Override
    public void increment() {
        if (componentValues == null) {
            componentValues = new TreeMap<>();
        }
        if (componentId == null) {
            componentValues.put(componentId = newComponentId(), 1L);
        } else {
            componentValues.put(componentId, componentValues.get(componentId) + 1);
        }
    }

    private void putMax(BagKey key, Long value) {
        if (componentValues == null) {
            componentValues = new TreeMap<>();
        }
        Long existingValue = componentValues.get(key);
        if (existingValue == null || existingValue < value) {
            componentValues.put(key, value);
        }
    }

    @Override
    public long getValue() {
        return componentValues == null ? 0 : componentValues.values().stream().reduce(0L, (l, r) -> l + r);
    }

    private void serialize(BaggageWriter writer) {
        if (componentValues == null) {
            return;
        }

        for (Entry<BagKey, Long> entry : componentValues.entrySet()) {
            writer.enter(entry.getKey());
            UnsignedLexVarint.writeReverseLexVarUInt64(writer.newDataAtom(9), entry.getValue());
            writer.exit();
        }
    }

    private CounterImpl mergeWith(CounterImpl other) {
        if (other == null || other.componentValues == null) {
            return this;
        }

        for (Entry<BagKey, Long> entry : other.componentValues.entrySet()) {
            Long existingValue = componentValues.get(entry.getKey());
            Long newValue = existingValue == null ? entry.getValue() : Math.max(entry.getValue(), existingValue);
            componentValues.put(entry.getKey(), newValue);
        }
        return this;
    }

    private CounterImpl branch() {
        if (componentValues == null) {
            return null;
        }
        CounterImpl other = new CounterImpl();
        other.componentValues = new TreeMap<>(componentValues);
        return other;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(getValue());
        if (componentValues != null && componentValues.size() > 1) {
            b.append(" (");
            List<String> entries = new ArrayList<>();
            componentValues.entrySet().forEach(e -> {
                entries.add(e.getKey() + " = " + e.getValue());
            });
            b.append(StringUtils.join(entries, ", "));
            b.append(")");
        }
        return b.toString();
    }

    public static class Handler implements BaggageHandler<CounterImpl> {

        public static final Handler instance = new Handler();

        @Override
        public CounterImpl parse(BaggageReader reader) {
            CounterImpl counter = null;

            BagKey nextComponentKey = null;
            while ((nextComponentKey = reader.enter()) != null) {
                try {
                    ByteBuffer data = reader.nextData();
                    if (data == null) {
                        continue;
                    }

                    Long value;
                    try {
                        value = UnsignedLexVarint.readReverseLexVarUInt64(data, data.position());
                    } catch (AtomLayerException e) {
                        continue; // invalid lexvarint
                    }

                    if (counter == null) {
                        counter = new CounterImpl();
                    }
                    counter.putMax(nextComponentKey, value);
                } finally {
                    reader.exit();
                }
            }

            return counter;
        }

        @Override
        public void serialize(BaggageWriter writer, CounterImpl instance) {
            if (instance != null) {
                instance.serialize(writer);
            }
        }

        @Override
        public CounterImpl join(CounterImpl first, CounterImpl second) {
            if (first == null) {
                return second;
            } else {
                return first.mergeWith(second);
            }
        }

        @Override
        public CounterImpl branch(CounterImpl from) {
            return from == null ? null : from.branch();
        }

        @Override
        public boolean isInstance(Bag bag) {
            return bag == null || bag instanceof CounterImpl;
        }

    }

}
