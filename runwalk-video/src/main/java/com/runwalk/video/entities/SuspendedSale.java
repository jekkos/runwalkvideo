package com.runwalk.video.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

@Entity
@SuppressWarnings("serial")
@Table(name="ospos_sales")
public class SuspendedSale implements Serializable {
	
	@Id
	@Column(name="sale_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long saleId;

	@Column(name="sale_time")
	@Temporal(value=TemporalType.TIMESTAMP)
	private Date saleTime = new Date();
	
	@ManyToOne
	@JoinColumn(name="customer_id", nullable=false )
	private Customer customer;
	
	@Column(name="employee_id")
	private Long employeeId;
	
	@Column(name="sale_status")
    @Enumerated
	private SaleStatus status;
	
	@Column(name="sale_type")
    @Enumerated
	private SaleType type;
	
	@OneToMany
	@JoinColumn(name="sale_id")
	private List<SuspendedSaleItem> saleItems;
	
	@OneToMany
	@JoinColumn(name="sale_id")
	private List<SuspendedSaleItemTax> saleItemTaxes;
	
	@Column(name="comment")
	private String comment = "";
	
	public SuspendedSale() {}

	public SuspendedSale(Customer customer, Long employeeId) {
		this.customer = customer;
		this.employeeId = employeeId;
		this.status = SaleStatus.SUSPENDED;
		this.type = SaleType.POS;
	}
	
	public Long getId() {
		return saleId;
	}
	
	public Long getEmployeeId() {
		return employeeId;
	}

	public Date getSaleTime() {
		return saleTime;
	}

	public void setSaleTime(Date saleTime) {
		this.saleTime = saleTime;
	}
	
	public SaleStatus getStatus() {
		return status;
	}
	
	public SaleType getType() {
		return type;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public List<SuspendedSaleItem> getSaleItems() {
		return saleItems;
	}

	public void setSaleItems(List<SuspendedSaleItem> saleItems) {
		this.saleItems = saleItems;
	}
	
	public List<SuspendedSaleItemTax> getSaleItemTaxes() {
		return saleItemTaxes;
	}

	public void setSaleItemTaxes(List<SuspendedSaleItemTax> saleItemTaxes) {
		this.saleItemTaxes = saleItemTaxes;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public enum SaleType {
		POS, INVOICE, WORK_ORDER, QUOTE, RETURN
	}

	public enum SaleStatus {
		COMPLETED, SUSPENDED, CANCELLED
	}
	
}
