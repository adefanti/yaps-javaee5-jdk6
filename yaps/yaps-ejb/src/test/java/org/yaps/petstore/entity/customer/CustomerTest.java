package org.yaps.petstore.entity.customer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.hsqldb.HsqldbConnection;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.hibernate.ejb.EntityManagerImpl;
import org.hibernate.jdbc.Work;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CustomerTest {
	private static EntityManagerFactory emf;
	private static EntityManager em;
	private static EntityTransaction tx;
	private static IDatabaseConnection connection;
	private static IDataSet dataset;

	@BeforeClass
	public static void initEntityManager() throws Exception {
		emf = Persistence.createEntityManagerFactory("floydPU");
		em = emf.createEntityManager();

		// Initializes DBUnit
		Session session = ((EntityManagerImpl) em).getSession();
		session.doWork(new Work() {
			@Override
			public void execute(Connection hibernateConnection)
					throws SQLException {
				try {
					// Creates HSQLDB connection
					connection = new HsqldbConnection(hibernateConnection,
							"public");
				} catch (DatabaseUnitException e) {
					throw new RuntimeException(e);
				}
			}
		});

		dataset = (new FlatXmlDataSetBuilder()).build(Thread.currentThread()
				.getContextClassLoader()
				.getResourceAsStream("customer-dataset.xml"));
	}

	@AfterClass
	public static void closeEntityManager() throws SQLException {
		em.close();
		emf.close();
		connection.close();
	}

	@Before
	public void cleanDB() throws Exception {
		// Cleans the database with DbUnit
		DatabaseOperation.CLEAN_INSERT.execute(connection, dataset);
	}

	@Before
	public void initTransaction() {
		tx = em.getTransaction();
	}

	@Test
	public void createCustomer() throws Exception {

		Customer customer = new Customer("Richard", "Wright");
		tx.begin();
		em.persist(customer);
		tx.commit();
		assertNotNull("ID should not be null", customer.getId());
	}

	@Test
	public void findAll() throws Exception {

		// Gets all the objects from the database
		Query query = em.createNamedQuery(Customer.FIND_ALL);
		assertEquals("Should have 5 customers", query.getResultList().size(), 5);

		// Creates a new object and persists it
		Customer customer = new Customer("Richard", "Wright");
		tx.begin();
		em.persist(customer);
		tx.commit();

		// Gets all the objects from the database
		assertEquals("Should have 6 customers", query.getResultList().size(), 6);

		// Removes the object from the database
		tx.begin();
		em.remove(customer);
		tx.commit();

		// Gets all the objects from the database
		assertEquals("Should have 5 customers", query.getResultList().size(), 5);
	}

	@Test
	public void testVersion() {
		// Creates a new object and persists it
		Customer customer = new Customer("Richard", "Wright");
		tx.begin();
		em.persist(customer);
		tx.commit();

		// Updates previous object and persists it
		customer.setLastname("Right");
		tx.begin();
		em.merge(customer);
		tx.commit();

		assertEquals(customer.getVersion(), Integer.valueOf(1));
	}
}
