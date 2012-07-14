package org.yaps.petstore.entity.customer;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

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
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.jdbc.Work;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CustomerTest {
	private static EntityManagerFactory emf;
	private static EntityManager entityManager;
	private static EntityTransaction tx;
	private static IDatabaseConnection connection;
	private static IDataSet dataset;

	@BeforeClass
	public static void initEntityManager() throws Exception {
		emf = Persistence.createEntityManagerFactory("petstorePU");
		entityManager = emf.createEntityManager();

		// Initializes DBUnit
		Session session = ((EntityManagerImpl) entityManager).getSession();
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
		entityManager.close();
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
		tx = entityManager.getTransaction();
	}

	@Test
	public void createCustomer() throws Exception {
		// given
		Customer customer = new Customer("Richard", "Wright");

		// when
		tx.begin();
		entityManager.persist(customer);
		tx.commit();

		// then
		assertThat(customer.getId()).overridingErrorMessage(
				"ID should not be null").isNotNull();
	}

	@Test
	public void findAll() throws Exception {

		// Gets all the objects from the database
		Query query = entityManager.createNamedQuery(Customer.FIND_ALL);
		assertEquals("Should have 5 customers", query.getResultList().size(), 5);

		// Creates a new object and persists it
		Customer customer = new Customer("Richard", "Wright");
		tx.begin();
		entityManager.persist(customer);
		tx.commit();

		// Gets all the objects from the database
		assertEquals("Should have 6 customers", query.getResultList().size(), 6);

		// Removes the object from the database
		tx.begin();
		entityManager.remove(customer);
		tx.commit();

		// when
		// Gets all the objects from the database
		List<?> allCustomers = query.getResultList();

		// then
		assertThat(allCustomers).overridingErrorMessage(
				"Should have 5 customers").hasSize(5);
	}

	@Test
	public void testVersion() {
		// given
		// Creates a new object and persists it
		Customer customer = new Customer("Richard", "Wright");
		tx.begin();
		entityManager.persist(customer);
		tx.commit();
		// Updates previous object and persists it
		customer.setLastname("Right");

		// when
		tx.begin();
		entityManager.merge(customer);
		tx.commit();

		// then
		assertThat(customer.getVersion()).isEqualTo(1);
	}

	@Test
	public void testAudit() {
		// given
		// Creates a new object and persists it
		Customer customer = new Customer("Richard", "Wright");
		tx.begin();
		entityManager.persist(customer);
		tx.commit();

		// Updates previous object and persists it
		customer.setLastname("Right");

		// when
		tx.begin();
		entityManager.merge(customer);
		tx.commit();

		// then
		List<Object[]> customerRevisions = getCustomerRevisions(customer);
		assertThat(customerRevisions).isNotNull().hasSize(2);
		assertEquals("Wright",
				((Customer) customerRevisions.get(0)[0]).getLastname());
	}

	@SuppressWarnings("unchecked")
	private List<Object[]> getCustomerRevisions(Customer customer) {
		AuditReader auditReader = AuditReaderFactory.get(entityManager);
		AuditQuery auditQuery = auditReader.createQuery()
				.forRevisionsOfEntity(Customer.class, false, true)
				.add(AuditEntity.id().eq(customer.getId()));
		return auditQuery.getResultList();
	}
}
