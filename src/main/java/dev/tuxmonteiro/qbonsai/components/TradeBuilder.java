package dev.tuxmonteiro.qbonsai.components;

import dev.tuxmonteiro.jccxt.base.types.Trade;
import dev.tuxmonteiro.qbonsai.utils.ConvertUtils;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class TradeBuilder {

    public static final Trade NULL_TRADE = new Trade();

    public static Trade build(Map<String, Object> map) {
        var amount = getAmount(baseData(map));
        var price = getPrice(baseData(map));
        long datetimeMicrosecs = getDatetimeMicrosecs(baseData(map));

        if (Objects.isNull(amount) || Objects.isNull(price)) {
            throw new IllegalArgumentException("map not processed: " + map);
        }

        Instant instant = ConvertUtils.getInstantFromLong(datetimeMicrosecs);

        Trade trade = new Trade();
        trade.setAmount(amount.doubleValue());
        trade.setCost(price.doubleValue());
        trade.setDatetime(instant.toString());

        return trade;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> baseData(Map<String, Object> map) {
        if (map.containsKey("data")) {
        return (Map<String, Object>) map.get("data");
        } else {
            throw new IllegalArgumentException("not 'data' in map");
        }
    }

    // TODO: Implements more generic code
    private static BigDecimal getPrice(Map<String, Object> map) {
        return map.containsKey("price_str") ?
                new BigDecimal(map.get("price_str").toString()) : null;
    }

    // TODO: Implements more generic code
    private static BigDecimal getAmount(Map<String, Object> map) {
        return map.containsKey("amount_str") ?
                new BigDecimal(map.get("amount_str").toString()) : null;
    }

    // TODO: Implements more generic code
    private static long getDatetimeMicrosecs(Map<String, Object> map) {
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

    public static boolean isTrade(Map<String, Object> map) {
        return "trade".equals(map.get("event"));
    }
}
