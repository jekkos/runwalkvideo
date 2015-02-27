package com.runwalk.video.dao.jpa;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Customer;

public class AnalysisDao extends JpaDao<Analysis> {

	public AnalysisDao(EntityManagerFactory entityManagerFactory) {
		super(Analysis.class, entityManagerFactory);
	}
	
	public List<Analysis> getAnalysesByCustomer(Customer customer) {
		TypedQuery<Analysis> query = createEntityManager().createQuery(
				"SELECT analysis FROM " + getTypeParameter().getSimpleName() + " analysis "
					+ "LEFT JOIN FETCH analysis.recordings WHERE analysis.customer = :customer ", Analysis.class);
		query.setParameter("customer", customer);
		return query.getResultList();
	}
	
	public List<Analysis> getAnalysesAfterStartDate(Date startDate) {
		TypedQuery<Analysis> query = createEntityManager().createQuery(
				"SELECT analysis FROM " + getTypeParameter().getSimpleName() + " analysis "
					+ "WHERE analysis.startDate >= :startDate", Analysis.class);
		query.setParameter("startDate", startDate);
		return query.getResultList();
	}

}
