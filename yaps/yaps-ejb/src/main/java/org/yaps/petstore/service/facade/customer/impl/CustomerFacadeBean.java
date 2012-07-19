package org.yaps.petstore.service.facade.customer.impl;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.yaps.petstore.domain.model.customer.Customer;
import org.yaps.petstore.service.common.AbstractFacade;
import org.yaps.petstore.service.facade.customer.CustomerFacade;

@Stateless
@Local(CustomerFacade.class)
@Remote(CustomerFacade.class)
public class CustomerFacadeBean extends AbstractFacade<Customer> implements
		CustomerFacade {

	@PersistenceContext(unitName = "petstorePU")
	private EntityManager em;

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	public CustomerFacadeBean() {
		super(Customer.class);
	}
}
