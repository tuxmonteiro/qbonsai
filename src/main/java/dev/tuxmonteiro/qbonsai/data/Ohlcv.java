package dev.tuxmonteiro.qbonsai.data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

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

    public Ohlcv update(Map<String, Object> value) {
        var amount = new BigDecimal(value.get("amount_str").toString());
        var price = new BigDecimal(value.get("price_str").toString());
        Timestamp microtimestamp = Timestamp.from(Instant.ofEpochMilli(Long.getLong(value.get("microtimestamp").toString())));

        return updateOpen(price, microtimestamp)
                .updateHigh(price)
                .updateLow(price)
                .updateClose(price)
                .updateVolume(amount);
    }

    private Ohlcv updateOpen(BigDecimal price, Timestamp microtimestamp) {
        if (Objects.isNull(this.open)) {
            this.open = price;
            this.high = price;
            this.low = price;
            this.close = price;
            this.informedTimestamp = microtimestamp;
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
        this.volume.add(amount);
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
