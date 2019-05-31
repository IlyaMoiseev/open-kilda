/* Copyright 2018 Telstra Open Source
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.openkilda.wfm.share.bolt;

import org.openkilda.wfm.AbstractBolt;
import org.openkilda.wfm.CommandContext;

import com.google.common.base.Preconditions;
import lombok.Value;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.TupleUtils;

import java.io.Serializable;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MonotonicClock<T extends Serializable> extends AbstractBolt {
    public static final String FIELD_ID_TIME_MILLIS = "time";
    public static final String FIELD_ID_TICK_NUMBER = "tick";
    public static final String FIELD_ID_TICK_IDENTIFIER = "identifier";
    public static final Fields STREAM_FIELDS = new Fields(
            FIELD_ID_TIME_MILLIS, FIELD_ID_TICK_NUMBER, FIELD_ID_TICK_IDENTIFIER, FIELD_ID_CONTEXT);

    private transient Clock baseClock;

    private Map<T, Long> clocksConfig = new HashMap<>();
    private transient Map<T, ClockAdapter> clocks;

    private final Integer interval;

    public MonotonicClock() {
        this(1);
    }

    public MonotonicClock(Integer intervalSeconds) {
        this(intervalSeconds, null);
    }

    MonotonicClock(Integer interval, Clock baseClock) {
        this.interval = interval;
        this.baseClock = baseClock;
    }

    public MonotonicClock<T> addTickInterval(T identifier, long durationSeconds) {
        Preconditions.checkArgument(
                1 <= durationSeconds, String.format("Invalid time duration \"%d\" < 1", durationSeconds));
        clocksConfig.put(identifier, durationSeconds);
        return this;
    }

    @Override
    protected void handleInput(Tuple input) {
        if (!TupleUtils.isTick(input)) {
            return;
        }

        processClocks(input);
    }

    @Override
    protected void init() {
        if (baseClock == null) {
            baseClock = Clock.systemDefaultZone();
        }

        clocks = new HashMap<>();
        clocks.put(null, new ClockAdapter(baseClock));
        for (Map.Entry<T, Long> entry : clocksConfig.entrySet()) {
            clocks.put(entry.getKey(), new ClockAdapter(baseClock, entry.getValue()));
        }
    }

    @Override
    protected CommandContext setupCommandContext() {
        return new CommandContext();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputManager) {
        outputManager.declare(STREAM_FIELDS);
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return TupleUtils.putTickFrequencyIntoComponentConfig(null, interval);
    }

    private void processClocks(Tuple input) {
        for (Map.Entry<T, ClockAdapter> entry : clocks.entrySet()) {
            ClockAdapter adapter = entry.getValue();
            adapter.nextTick()
                    .ifPresent(tick -> produceTick(input, entry.getKey(), tick));
        }
    }

    private void produceTick(Tuple input, T identifier, Tick tick) {
        getOutput().emit(input, makeDefaultTuple(identifier, tick));
    }

    private Values makeDefaultTuple(T identifier, Tick tick) {
        return new Values(tick.getValue().toEpochMilli(), tick.getNumber(), identifier, getCommandContext());
    }

    private static class ClockAdapter {
        private final Clock clock;
        private long tickNumber = 0;
        Instant lastTick = null;

        ClockAdapter(Clock baseClock) {
            clock = baseClock;
        }

        ClockAdapter(Clock baseClock, long durationSeconds) {
            Duration duration = Duration.ofSeconds(durationSeconds);
            clock = Clock.tick(baseClock, duration);
        }

        private Optional<Tick> nextTick() {
            Instant previous = lastTick;
            lastTick = clock.instant();

            Optional<Tick> tick = Optional.empty();
            if (!lastTick.equals(previous)) {
                tick = Optional.of(new Tick(tickNumber, lastTick));
                tickNumber += 1;
            }
            return tick;
        }
    }

    @Value
    private static class Tick {
        private final long number;
        private final Instant value;
    }

    public static class Match<T extends Serializable> implements Serializable {
        String sourceComponent;
        T identifier;

        public Match(String sourceComponent, T identifier) {
            this.sourceComponent = sourceComponent;
            this.identifier = identifier;
        }

        public boolean isTick(Tuple input) {
            if (!sourceComponent.equals(input.getSourceComponent())) {
                return false;
            }
            return Objects.equals(identifier, input.getValueByField(FIELD_ID_TICK_IDENTIFIER));
        }
    }
}
