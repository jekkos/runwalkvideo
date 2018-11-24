package com.runwalk.video.entities;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="ospos_sales_items_taxes")
public class SuspendedSaleItemTax {

	public static final String DEFAULT_VAT_NAME = "VAT";
	
	public static final BigDecimal DEFAULT_VAT_PCT = BigDecimal.valueOf(21d);

	@EmbeddedId
	private SuspendedSaleItemKey id;
	
	@Column(name="name")
	private String name = DEFAULT_VAT_NAME;
	
	@Column(name="percent")
	private BigDecimal percent = DEFAULT_VAT_PCT;
	
	public SuspendedSaleItemTax() {	}
	
	public SuspendedSaleItemTax(SuspendedSaleItemKey suspendedSaleItemKey) {
		id = suspendedSaleItemKey;
	}
	
	public SuspendedSaleItemTax(SuspendedSale suspendedSale, Item item) {
		this(new SuspendedSaleItemKey(suspendedSale, item));
	}	

	public SuspendedSaleItemTax(String name, BigDecimal prcent) {
		this.name = name;
		this.percent = percent;
	}

	public SuspendedSaleItemKey getId() {
		return id;
	}

	public void setId(SuspendedSaleItemKey id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getPercent() {
		return percent;
	}

	public void setPercent(BigDecimal percent) {
		this.percent = percent;
	}

}
