package hello.data.account

import java.io.Serializable
import javax.persistence.*


@Entity
@Embeddable
@Table(name = "account")
data class Account(
		var name: String = "",
		var password: String = "",
		var balance: Double = 0.0,
		@javax.persistence.Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		var id: Long = 0
) : Serializable