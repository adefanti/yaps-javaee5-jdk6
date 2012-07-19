package org.yaps.petstore.domain.model.customer;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
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

	private static final long CUTOMER_DATASET_ID = 1001;

	@BeforeClass
	public static void initEntityManager() throws Exception {
		emf = Persistence.createEntityManagerFactory("petstorePU-junit");
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
	public void createCustomerWithValidValues() throws Exception {
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
	public void createCustomerWithInvalidValues() {
		// given
		Customer customer = new Customer(null, "Wright");
		try {
			// when
			tx.begin();
			entityManager.persist(customer);
			tx.commit();
			fail("Object with null values should not be created");
		} catch (Exception e) {
			// then
			assertThat(e).isInstanceOf(PersistenceException.class);
			tx.rollback();
		}
	}

	@Test
	public void updateCustomerWithValidValues() throws Exception {
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
		customer = entityManager.find(Customer.class, customer.getId());
		assertThat(customer).isNotNull();
		assertThat(customer.getLastname()).isEqualTo("Right");
		// check version
		assertThat(customer.getVersion()).isEqualTo(1);
		// check audit datas
		List<Object[]> customerRevisions = getCustomerRevisions(customer);
		assertThat(customerRevisions).isNotNull().hasSize(2);
		assertThat(((Customer) customerRevisions.get(0)[0]).getLastname())
				.isEqualTo("Wright");
	}

	@Test
	public void updateCustomerWithInvalidValues() throws Exception {
		// given
		// Find customer with id number 1001, updates and persists it
		Customer customer = entityManager.find(Customer.class,
				CUTOMER_DATASET_ID);
		customer.setLastname(null);

		try {
			// when
			tx.begin();
			entityManager.merge(customer);
			tx.commit();
			fail("Object with null values should not be updated");
		} catch (Exception e) {
			// then
			assertThat(e).isInstanceOf(PersistenceException.class);
			assertThat(tx.isActive()).isFalse();
			// rollback update
			tx.begin();
			tx.rollback();
		}
	}
	
	@Test
	public void find() throws Exception {
		// when
		Customer customer = entityManager.find(Customer.class,
				CUTOMER_DATASET_ID);

		// then
		assertThat(customer).isNotNull();
		assertThat(customer.getFirstname()).isEqualTo("Roger");
		assertThat(customer.getLastname()).isEqualTo("Waters");
		assertThat(customer.getVersion()).isEqualTo(0);
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

	@SuppressWarnings("unchecked")
	private List<Object[]> getCustomerRevisions(Customer customer) {
		AuditReader auditReader = AuditReaderFactory.get(entityManager);
		AuditQuery auditQuery = auditReader.createQuery()
				.forRevisionsOfEntity(Customer.class, false, true)
				.add(AuditEntity.id().eq(customer.getId()));
		return auditQuery.getResultList();
	}
}
