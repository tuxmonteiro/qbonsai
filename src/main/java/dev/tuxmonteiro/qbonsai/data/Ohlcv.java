package dev.tuxmonteiro.qbonsai.data;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class Ohlcv {

    private final Timestamp localTimestamp;

    private Timestamp informedTimestamp = Timestamp.from(Instant.now());
    private BigDecimal open = null;
    private BigDecimal high = null;
    private BigDecimal low = null;
    private BigDecimal close = null;
    private BigDecimal volume = BigDecimal.ZERO;

    public Ohlcv() {
        this.localTimestamp = Timestamp.from(Instant.now());
    }

    public Flux<Ohlcv> update(Flux<Map<String, Object>> flux) {
        return flux.map(map -> {
            var amount = map.containsKey("amount_str") ? new BigDecimal(map.get("amount_str").toString()) : null;
            var price = map.containsKey("price_str") ? new BigDecimal(map.get("price_str").toString()) : null;
            Timestamp microtimestamp = null;

            long epoch;
            if (map.containsKey("microtimestamp")) {
                String microtimestampStr = Optional.ofNullable(map.get("microtimestamp")).orElse("0").toString();
                epoch = Long.parseLong(microtimestampStr);
            } else {
                epoch = (System.currentTimeMillis() * 1000L) + (System.nanoTime() % 1000_000_000L);
            }
            long epochMilli = epoch;
            long epochNano = 0L;
            if (epoch > 9999999999999L) {
                epochMilli = epoch / 1000L;
                epochNano = epoch % 1000L;
            }
            Instant instant = Instant.ofEpochMilli(epochMilli).plusNanos(epochNano);
            microtimestamp = Timestamp.from(instant);

            updateOpen(price, microtimestamp)
                    .updateHigh(price)
                    .updateLow(price)
                    .updateClose(price)
                    .updateVolume(amount);
            return this;
        });
    }

    private Ohlcv updateOpen(BigDecimal price, Timestamp microtimestamp) {
        if (Objects.isNull(this.open) && Objects.nonNull(price) && Objects.nonNull(microtimestamp)) {
            this.open = price;
            this.high = price;
            this.low = price;
            this.close = price;
            this.informedTimestamp = microtimestamp;
        }
        return this;
    }

    private Ohlcv updateHigh(BigDecimal price) {
        if (Objects.nonNull(price) && this.high.compareTo(price) < 0) {
            this.high = price;
        }
        return this;
    }

    private Ohlcv updateLow(BigDecimal price) {
        if (Objects.nonNull(price) && this.low.compareTo(price) > 0) {
            this.low = price;
        }
        return this;
    }

    private Ohlcv updateClose(BigDecimal price) {
        if (Objects.nonNull(price)) {
            this.close = price;
        }
        return this;
    }

    private Ohlcv updateVolume(BigDecimal amount) {
        if (Objects.nonNull(amount)) {
            this.volume = this.volume.add(amount);
        }
        return this;
    }

    public Timestamp getLocalTimestamp() {
        return localTimestamp;
    }

    public Timestamp getInformedTimestamp() {
        return informedTimestamp;
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

    @Override
    public String toString() {
        return "Ohlcv{" +
                "localTimestamp=" + localTimestamp +
                ", informedTimestamp=" + informedTimestamp +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", volume=" + volume +
                '}';
    }
}
