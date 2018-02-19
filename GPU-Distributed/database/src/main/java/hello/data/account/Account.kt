package hello.data.account

import java.io.Serializable
import java.sql.Timestamp
import javax.persistence.*


@Entity
@Embeddable
@Table(name="account")
data class Account(
		val name: String = "",
		val password: String = "",
		var balance: Double = 0.0,
		val timestamp: Timestamp = Timestamp(System.currentTimeMillis())
) : Serializable {
	@javax.persistence.Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	var id: Long? = null
	
}