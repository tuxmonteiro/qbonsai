package dev.tuxmonteiro.qbonsai.data;

import dev.tuxmonteiro.jccxt.base.types.Trade;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.*;

@Slf4j
public class Ohlcv {

    private final Instant createAt = Instant.now();
    private final Map<Instant, Trade> trades = Collections.synchronizedMap(new HashMap<>());

    private Instant informedInstant = Instant.now();
    private BigDecimal open = BigDecimal.ONE.negate();
    private BigDecimal high = BigDecimal.ONE.negate();
    private BigDecimal low = BigDecimal.valueOf(Double.MAX_VALUE);
    private BigDecimal close = BigDecimal.ZERO;
    private BigDecimal volume = BigDecimal.ZERO;

    public Mono<Ohlcv> update(Flux<Map<String, Object>> flux) {
        return flux.map(map -> {
            var amount = getAmount(map);
            var price = getPrice(map);
            long datetimeMicrosecs = getDatetimeMicrosecs(map);

            if (Objects.isNull(amount) || Objects.isNull(price)) {
                throw new IllegalArgumentException("map not processed: " + map);
            }

            long datetimeNano = 0L;
            if (datetimeMicrosecs > 9999999999999L) {
                datetimeMicrosecs = datetimeMicrosecs / 1000L;
                datetimeNano = datetimeMicrosecs % 1000L;
            }
            Instant instant = getInstantMicrosecs(datetimeMicrosecs, datetimeNano);

            Trade trade = new Trade();
            trade.setAmount(amount.doubleValue());
            trade.setCost(price.doubleValue());
            trade.setDatetime(instant.toString());

            trades.put(instant, trade);

            return updateOpen(price, instant)
                    .updateHigh(price)
                    .updateLow(price)
                    .updateClose(price)
                    .updateVolume(amount);
        })
        .onErrorResume(t -> {
            log.error(t.getMessage(), t);
            return Flux.empty();
        })
        .then(Mono.just(this));
    }

    private Instant getInstantMicrosecs(long datetimeMicrosecs, long datetimeNano) {
        return Instant.ofEpochMilli(datetimeMicrosecs).plusNanos(datetimeNano);
    }

    // TODO: Implements more generic code
    private BigDecimal getPrice(Map<String, Object> map) {
        return map.containsKey("price_str") ?
                new BigDecimal(map.get("price_str").toString()) : null;
    }

    // TODO: Implements more generic code
    private BigDecimal getAmount(Map<String, Object> map) {
        return map.containsKey("amount_str") ?
                new BigDecimal(map.get("amount_str").toString()) : null;
    }

    // TODO: Implements more generic code
    private long getDatetimeMicrosecs(Map<String, Object> map) {
        long datetimeMicrosecs;
        if (map.containsKey("microtimestamp")) {
            String microtimestampStr = Optional.ofNullable(map.get("microtimestamp")).orElse("0").toString();
            datetimeMicrosecs = Long.parseLong(microtimestampStr);
        } else {
            log.error("microtimestamp not found. Using Instant.now()");
            Instant now = Instant.now();
            datetimeMicrosecs = (now.toEpochMilli() * 1000L) + (now.getLong(ChronoField.NANO_OF_SECOND) % 1000L);
        }
        return datetimeMicrosecs;
    }

    private Ohlcv updateOpen(BigDecimal price, Instant instant) {
        if (this.open.signum() < 0 && Objects.nonNull(price) && Objects.nonNull(instant)) {
            this.open = price;
            this.high = price;
            this.low = price;
            this.close = price;
            this.informedInstant = instant;
        }
        return this;
    }

    private Ohlcv updateHigh(BigDecimal price) {
        if (this.high.compareTo(price) < 0) {
            this.high = price;
        }
        return this;
    }

    private Ohlcv updateLow(BigDecimal price) {
        if (this.low.compareTo(price) > 0) {
            this.low = price;
        }
        return this;
    }

    private Ohlcv updateClose(BigDecimal price) {
        this.close = price;
        return this;
    }

    private Ohlcv updateVolume(BigDecimal amount) {
        this.volume = this.volume.add(amount);
        return this;
    }

    public Instant getCreateAt() {
        return createAt;
    }

    public Instant getInformedInstant() {
        return informedInstant;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public BigDecimal getClose() {
        return close;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public Map<Instant, Trade> getTrades() {
        return trades;
    }

    @Override
    public String toString() {
        return """
                {
                    'createAt': '%s',
                    'informedTimestamp': '%s',
                    'open': '%s',
                    'high': '%s',
                    'low': '%s',
                    'close': '%s',
                    'volume': '%s',
                    'trades': '%s'
                }
                """.formatted(createAt, informedInstant, open, high, low, close, volume, trades.size());
    }
}
