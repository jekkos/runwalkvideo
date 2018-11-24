package com.runwalk.video.tasks;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.NoResultException;

import com.runwalk.video.dao.Dao;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.dao.jpa.SuspendedSaleDao;
import com.runwalk.video.entities.Customer;
import com.runwalk.video.entities.Item;
import com.runwalk.video.entities.SuspendedSale;
import com.runwalk.video.entities.SuspendedSaleItem;
import com.runwalk.video.entities.SuspendedSaleItemTax;

public class CreateOrUpdateSuspendedSaleTask extends AbstractTask<Void, Void> {

	private final DaoService daoService;
	private final Item oldItem;
	private final Item newItem;
	private final Customer customer;
	private final Long employeeId;
	private final Long locationId;

	public CreateOrUpdateSuspendedSaleTask(DaoService daoService, Customer customer, Item oldItem, Item newItem, Long employeeId, Long locationId) {
		super("createOrUpdateSuspendedSale");
		this.daoService = daoService;
		this.customer = customer;
		this.oldItem = oldItem;
		this.newItem = newItem;
		this.employeeId = employeeId;
		this.locationId = locationId;
	}

	@Override
	protected Void doInBackground() throws Exception {
		message("startMessage");
		SuspendedSale suspendedSale = findOrCreateSuspendedSale(getCustomer());
		replaceSuspendedSaleItems(suspendedSale);
		if (getNewItem() != null) {
			SuspendedSaleItem suspendedSaleItem = new SuspendedSaleItem(suspendedSale, getNewItem(), getCustomer(), locationId);
			SuspendedSaleItemTax suspendedSaleItemTax = new SuspendedSaleItemTax(suspendedSale, getNewItem());
			suspendedSale.getSaleItems().add(suspendedSaleItem);
			suspendedSale.getSaleItemTaxes().add(suspendedSaleItemTax);
		}
		SuspendedSaleDao suspendedSaleDao = getDaoService().getDao(SuspendedSale.class);
		suspendedSaleDao.merge(suspendedSale);
		message("endMessage");
		return null;
	}

	private void replaceSuspendedSaleItems(SuspendedSale suspendedSale) {
		Iterator<SuspendedSaleItem> saleItemIterator = suspendedSale.getSaleItems().iterator();
		while (oldItem != null && saleItemIterator.hasNext()) {
			SuspendedSaleItem suspendedSaleItem = saleItemIterator.next();
			if (suspendedSaleItem.getItemId().equals(getOldItem().getId())) {
				Dao<SuspendedSaleItemTax> suspendedSaleItemTaxDao = getDaoService().getDao(SuspendedSaleItemTax.class);
				suspendedSale.getSaleItemTaxes().stream()
						.filter(saleItemTax -> saleItemTax.getId().equals(suspendedSaleItem.getId()))
						.forEach(saleItemTax -> suspendedSaleItemTaxDao.delete(saleItemTax));
				getDaoService().getDao(SuspendedSaleItem.class).delete(suspendedSaleItem);
				saleItemIterator.remove();
			}
		}
	}
	
	private SuspendedSale findOrCreateSuspendedSale(Customer customer) {
		SuspendedSaleDao suspendedSaleDao = getDaoService().getDao(SuspendedSale.class);
		try {
			return suspendedSaleDao.getSuspendedSaleByCustomer(customer);
		} catch (NoResultException e) {
			SuspendedSale suspendedSale = new SuspendedSale(customer, employeeId);
			suspendedSaleDao.persist(suspendedSale);
			return suspendedSale;
		}
	}

	public DaoService getDaoService() {
		return daoService;
	}

	public Customer getCustomer() {
		return customer;
	}

	public Item getNewItem() {
		return newItem;
	}

	public Item getOldItem() {
		return oldItem;
	}

	public Long getEmployeeId() {
		return employeeId;
	}
	
}
