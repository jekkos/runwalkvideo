package com.runwalk.video.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table(name="ospos_attribute_values")
public class AttributeValue implements Serializable {

	@Id
	@Column(name="attribute_id")
	private Long id;
	
	@Column(name="attribute_value")
	private String value;

	public Long getId() {
		return id;
	}

	public String getValue() {
		return value;
	}
	
}
