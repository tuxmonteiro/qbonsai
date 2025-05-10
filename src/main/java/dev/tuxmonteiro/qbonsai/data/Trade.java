package dev.tuxmonteiro.qbonsai.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import dev.tuxmonteiro.jccxt.base.types.Fee;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "amount",
        "cost",
        "datetime",
        "fee",
        "id",
        "info",
        "order",
        "price",
        "side",
        "symbol",
        "takerOrMaker",
        "timestamp",
        "type"
})
public class Trade implements Serializable {

    private final static long serialVersionUID = -4926463071797815072L;

    @JsonProperty("amount")
    private BigDecimal amount;
    
    @JsonProperty("cost")
    private BigDecimal cost;
    
    @JsonProperty("datetime")
    private String datetime;
    
    @JsonProperty("fee")
    private Fee fee;
    
    @JsonProperty("id")
    private String id;

    @JsonProperty("info")
    private Object info;
    
    @JsonProperty("order")
    private String order;
    
    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("side")
    private Object side;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("takerOrMaker")
    private Object takerOrMaker;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("type")
    private String type;

    public BigDecimal getAmount() {
        return amount;
    }

    public Trade setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public Trade setCost(BigDecimal cost) {
        this.cost = cost;
        return this;
    }

    public String getDatetime() {
        return datetime;
    }

    public Trade setDatetime(String datetime) {
        this.datetime = datetime;
        return this;
    }

    public Fee getFee() {
        return fee;
    }

    public Trade setFee(Fee fee) {
        this.fee = fee;
        return this;
    }

    public String getId() {
        return id;
    }

    public Trade setId(String id) {
        this.id = id;
        return this;
    }

    public Object getInfo() {
        return info;
    }

    public Trade setInfo(Object info) {
        this.info = info;
        return this;
    }

    public String getOrder() {
        return order;
    }

    public Trade setOrder(String order) {
        this.order = order;
        return this;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Trade setPrice(BigDecimal price) {
        this.price = price;
        return this;
    }

    public Object getSide() {
        return side;
    }

    public Trade setSide(Object side) {
        this.side = side;
        return this;
    }

    public String getSymbol() {
        return symbol;
    }

    public Trade setSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public Object getTakerOrMaker() {
        return takerOrMaker;
    }

    public Trade setTakerOrMaker(Object takerOrMaker) {
        this.takerOrMaker = takerOrMaker;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Trade setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getType() {
        return type;
    }

    public Trade setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "amount=" + amount +
                ", cost=" + cost +
                ", datetime='" + datetime + '\'' +
                ", fee=" + fee +
                ", id='" + id + '\'' +
                ", info=" + info +
                ", order='" + order + '\'' +
                ", price=" + price +
                ", side=" + side +
                ", symbol='" + symbol + '\'' +
                ", takerOrMaker=" + takerOrMaker +
                ", timestamp=" + timestamp +
                ", type='" + type + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Trade trade = (Trade) o;
        return Objects.equals(amount, trade.amount) && Objects.equals(cost, trade.cost) && Objects.equals(datetime, trade.datetime) && Objects.equals(fee, trade.fee) && Objects.equals(id, trade.id) && Objects.equals(info, trade.info) && Objects.equals(order, trade.order) && Objects.equals(price, trade.price) && Objects.equals(side, trade.side) && Objects.equals(symbol, trade.symbol) && Objects.equals(takerOrMaker, trade.takerOrMaker) && Objects.equals(timestamp, trade.timestamp) && Objects.equals(type, trade.type);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(amount);
        result = 31 * result + Objects.hashCode(cost);
        result = 31 * result + Objects.hashCode(datetime);
        result = 31 * result + Objects.hashCode(fee);
        result = 31 * result + Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(info);
        result = 31 * result + Objects.hashCode(order);
        result = 31 * result + Objects.hashCode(price);
        result = 31 * result + Objects.hashCode(side);
        result = 31 * result + Objects.hashCode(symbol);
        result = 31 * result + Objects.hashCode(takerOrMaker);
        result = 31 * result + Objects.hashCode(timestamp);
        result = 31 * result + Objects.hashCode(type);
        return result;
    }
}
