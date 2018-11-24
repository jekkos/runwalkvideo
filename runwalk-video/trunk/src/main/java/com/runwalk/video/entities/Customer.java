package com.runwalk.video.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@SuppressWarnings("serial")
@DiscriminatorValue(Customer.PERSON_TYPE)
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name = "ospos_customers")
public class Customer extends Person {
	
	// discriminator value for customer
	public static final String PERSON_TYPE = "0";
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "customer")
	private List<Analysis> analyses = new ArrayList<Analysis>();
	
	@Column(name = "account_number")
	private String accountNumber;

	@Column(name = "company_name")
	private String companyName;

	@Column(name = "consent")
	private boolean inMailingList = true;

	public Customer() { }
	
	public Customer(String name, String firstName) {
		setFirstname(firstName);
		setName(name);
	}
	
	public String getAccountNumbe() {
		return this.accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	
	public List<Analysis> getAnalyses() {
		return analyses;
	}
	
	public int getAnalysesCount() {
		return getAnalyses().size();
	}
	
	public boolean addAnalysis(Analysis analysis) {
		return getAnalyses().add(analysis);
	}
	
	public boolean removeAnalysis(Analysis analysis) {
		return getAnalyses().remove(analysis);
	}
	
	public void setAnalyses(List<Analysis> analyses) {
		this.analyses = analyses;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public boolean isInMailingList() {
		return inMailingList;
	}

	public void setInMailingList(boolean inMailingList) {
		this.inMailingList = inMailingList;
	}
}
