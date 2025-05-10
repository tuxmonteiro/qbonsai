package dev.tuxmonteiro.qbonsai.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "cost",
    "currency",
    "rate"
})
public class Fee implements Serializable
{

    private final static long serialVersionUID = 5786725895174532869L;

    @JsonProperty("cost")
    private BigDecimal cost;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("rate")
    private BigDecimal rate;

    public BigDecimal cost() {
        return cost;
    }

    public Fee setCost(BigDecimal cost) {
        this.cost = cost;
        return this;
    }

    public String currency() {
        return currency;
    }

    public Fee setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public BigDecimal rate() {
        return rate;
    }

    public Fee setRate(BigDecimal rate) {
        this.rate = rate;
        return this;
    }

    @Override
    public String toString() {
        return "Fee{" +
                "cost=" + cost +
                ", currency='" + currency + '\'' +
                ", rate=" + rate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Fee fee = (Fee) o;
        return Objects.equals(cost, fee.cost) && Objects.equals(currency, fee.currency) && Objects.equals(rate, fee.rate);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(cost);
        result = 31 * result + Objects.hashCode(currency);
        result = 31 * result + Objects.hashCode(rate);
        return result;
    }
}
