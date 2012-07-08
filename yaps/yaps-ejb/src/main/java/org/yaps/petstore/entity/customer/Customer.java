package org.yaps.petstore.entity.customer;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "t_customer")
@NamedQueries({ @NamedQuery(name = Customer.FIND_ALL, query = "SELECT c FROM Customer c") })
public class Customer implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String FIND_ALL = "findAllCustomers";

	@Id
	@GeneratedValue
	private Long id;
	@Column(length = 100, nullable = false)
	private String firstname;
	@Column(length = 100, nullable = false)
	private String lastname;

	public Customer() {
	}

	public Customer(String firstname, String lastname) {
		this.firstname = firstname;
		this.lastname = lastname;
	}

	public Long getId() {
		return id;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
}
