package com.runwalk.video.entities;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SuspendedSaleItemKey implements Serializable {

    public SuspendedSaleItemKey() {	}

    public SuspendedSaleItemKey(SuspendedSale suspendedSale, Item item) {
        saleId = suspendedSale.getId();
        itemId = item.getId();
        line = suspendedSale.getSaleItems().size();
    }

    @Column(name="sale_id")
    private Long saleId;

    @Column(name="item_id")
    private Long itemId;

    @Column(name = "line")
    private int line;

    public Long getSaleId() {
        return saleId;
    }

    public void setSaleId(Long saleId) {
        this.saleId = saleId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SuspendedSaleItemKey that = (SuspendedSaleItemKey) o;
        return line == that.line &&
                Objects.equals(saleId, that.saleId) &&
                Objects.equals(itemId, that.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(saleId, itemId, line);
    }
}
