package data

import org.springframework.data.annotation.Id
import javax.persistence.Entity
import javax.persistence.GenerationType
import javax.persistence.GeneratedValue


@Entity
class Account constructor() {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private val id: Long? = null
	var name: String? = null
	var password: String? = null
	
	
	constructor(name: String, password: String) : this() {
		this.name = name
		this.password = password
	}
	
	
}