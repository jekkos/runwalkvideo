package com.runwalk.video.dao.jpa;

import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import com.runwalk.video.entities.Item;

public class ItemDao extends JpaDao<Item> {
	
	public ItemDao(EntityManagerFactory entityManagerFactory) {
		super(Item.class, entityManagerFactory);
	}

	public Item getItemByItemNumber(String itemNumber) {
		TypedQuery<Item> query = createEntityManager().createQuery(
				"SELECT DISTINCT item from " + getTypeParameter().getSimpleName() + " item LEFT JOIN FETCH item.attributeLinks WHERE item.itemNumber = :itemNumber", Item.class);
		query.setParameter("itemNumber", itemNumber);
		return query.getSingleResult();
	}
	
}
