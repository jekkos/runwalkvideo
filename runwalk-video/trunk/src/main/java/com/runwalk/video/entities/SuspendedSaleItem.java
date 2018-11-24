package com.runwalk.video.entities;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

@SuppressWarnings("serial")
@Entity
@Table(name="ospos_sales_items")
public class SuspendedSaleItem implements Serializable {
	
	public static final BigDecimal DEFAULT_DISCOUNT = BigDecimal.valueOf(10.0d);
	
	public static final int DEFAULT_QUANTITY = 1;

	@EmbeddedId
	private SuspendedSaleItemKey id;
	
	@Column(name="quantity_purchased")
	private int quantity;
	
	@Column(name="description")
	private String description;
	
	@Column(name="item_unit_price")
	private BigDecimal unitPrice;
	
	@Column(name="item_cost_price")
	private BigDecimal costPrice;
	
	@Column(name="discount")
	private BigDecimal discountPercent = DEFAULT_DISCOUNT;
	
	@Column(name="discount_type")
	private DiscountType discountType;

	@Column(name="item_location")
	private Long locationId;

	public SuspendedSaleItem() { }

	public SuspendedSaleItem(SuspendedSale suspendedSale, Item item, Customer customer, Long locationId) {
		this(suspendedSale, item);
		quantity = DEFAULT_QUANTITY;
		discountType = DiscountType.PERCENT;
		costPrice = item.getCostPrice();
		unitPrice = item.getUnitPrice();
		this.locationId = locationId;
	}

	public SuspendedSaleItem(SuspendedSale suspendedSale, Item item) {
		id = new SuspendedSaleItemKey(suspendedSale, item);
	}

	public SuspendedSaleItemKey getId() {
		return id;
	}

	public void setId(SuspendedSaleItemKey id) {
		this.id = id;
	}
	
	public Long getSaleId() {
		return id.getSaleId();
	}
	
	public DiscountType getDiscountType() {
		return discountType;
	}
	
	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	public BigDecimal getCostPrice() {
		return costPrice;
	}

	public void setCostPrice(BigDecimal costPrice) {
		this.costPrice = costPrice;
	}

	public BigDecimal getDiscountPercent() {
		return discountPercent;
	}

	public void setDiscountPercent(BigDecimal discountPercent) {
		this.discountPercent = discountPercent;
	}
	
	public Long getItemId() {
		return id.getItemId();
	}
	
	public enum DiscountType {
		PERCENT, FIXED		
	}
	
}
