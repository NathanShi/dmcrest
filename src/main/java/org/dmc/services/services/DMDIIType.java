package org.dmc.services.services;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "organization_dmdii_type")
public class DMDIIType {
	
	@Id
	@GeneratedValue(generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	private Integer id;
	
	@Column(name = "dmdii_member_desc")
	private String memberDescription;
	
	public Integer getId() {
		return this.id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getMemberDescription() {
		return this.memberDescription;
	}
	
	public void setMemberDescription(String memberDescription) {
		this.memberDescription = memberDescription;
	}
	
}
