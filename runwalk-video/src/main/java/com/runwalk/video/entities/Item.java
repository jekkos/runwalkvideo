package com.runwalk.video.entities;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@SuppressWarnings("serial")
@Table(name = "ospos_items")
public class Item implements Serializable {

	public static final String FILTER_CATEGORY = "Schoenen";
	@Id
	@Column(name="item_id")
	private Long id;
	
	@Column(name="item_number")
	private String itemNumber;
	
	@Column(name="name")
	private String name;
	
	@Column(name="description")
	private String description;

	@Column(name="category")
	private String category;

	@OneToMany
	@JoinColumn(name = "item_id")
	private Set<AttributeLink> attributeLinks;
	
	@Column(name="cost_price")
	private BigDecimal costPrice;
	
	@Column(name="unit_price")
	private BigDecimal unitPrice;
	
	public Long getId() {
		return id;
	}

	public String getItemNumber() {
		return this.itemNumber;
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

	public List<String> getItemSizes() {

		List<String> attributeValues = Lists.newArrayList();
        for (AttributeLink attributeLink: attributeLinks) {
        	if (attributeLink.isCurrentValue() && attributeLink.getAttributeDefinition().getName().contains(FILTER_CATEGORY)) {
				attributeValues.add(attributeLink.getAttributeValue().getValue());
			}
		}
		return attributeValues;
    }

	private String getSizeAsString() {
		return Joiner.on(", ").join(getItemSizes());
	}

	public BigDecimal getCostPrice() {
		return costPrice;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == getClass()) {
			Item other = (Item) obj;
			return Objects.equals(getItemNumber(), other.getItemNumber())
				&& Objects.equals(getId(), other.getId());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getItemNumber(), getId());
	}

	@Override
	public String toString() {
		return getName() + " " + getSizeAsString();
	}

}
