package org.yaps.petstore.service.customer;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.junit.BeforeClass;
import org.junit.Test;
import org.yaps.petstore.domain.model.customer.Customer;
import org.yaps.petstore.service.facade.customer.CustomerFacade;

public class CustomerFacadeBeanTest {

	/** Class to test */
	private static CustomerFacade service;

	/**
	 * initialize Session Ejb 
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpClass() throws Exception {
		Properties properties = new Properties();
		properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
		properties.setProperty("openejb.jndiname.format", "{ejbName}/{interfaceType.annotationName}");

		InitialContext initialContext = new InitialContext(properties);

		service = (CustomerFacade) initialContext.lookup("CustomerFacadeBean/Local");
	}

	/**
	 * Test create method
	 */
	@Test
	public void testCreateNewCustomer() {
		// given
		Customer customer = new Customer("Richard", "Wright");
		
		// when
		service.create(customer);
		
		// then
		assertThat(customer).isNotNull();
		assertThat(customer.getId()).isNotNull().isGreaterThan(0);
		assertThat(customer.getFirstname()).isEqualTo("Richard");
	}
	
	/**
	 * Test find method
	 */
	@Test
	public void testFindCustomer() {
		// given
		Customer customer = new Customer("Richard", "Wright");
		service.create(customer);
		
		// when
		Customer foundCustomer = service.find(customer.getId());
		
		// then
		assertThat(foundCustomer).isNotNull();
		assertThat(foundCustomer.getId()).isNotNull().isEqualTo(customer.getId());
		assertThat(foundCustomer.getFirstname()).isEqualTo("Richard");
	}
	
	/**
	 * Test update method
	 */
	@Test
	public void testUpdateCustomer() {
		// given
		Customer customer = new Customer("Gillian", "Manson");
		service.create(customer);
		
		// when
		customer.setFirstname("Jude");
		Customer updatedCustomer = service.update(customer);
		
		// then
		assertThat(updatedCustomer).isNotNull();
		assertThat(updatedCustomer.getId()).isNotNull().isEqualTo(customer.getId());
		assertThat(updatedCustomer.getFirstname()).isEqualTo("Jude");
		assertThat(updatedCustomer.getVersion()).isEqualTo(1);
	}
}
