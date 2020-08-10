package com.runwalk.video.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDao<E> implements Dao<E> {

	private final Class<E> typeParameter;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public AbstractDao(Class<E> typeParameter) {
		this.typeParameter = typeParameter;
	}

	public Class<E> getTypeParameter() {
		return typeParameter;
	}
	
	protected Logger getLogger() {
		return logger;
	}
	
}
