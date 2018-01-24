package hello.data.account

import org.springframework.data.annotation.Id
import java.io.Serializable
import javax.persistence.Embeddable
import javax.persistence.Entity
import javax.persistence.GenerationType
import javax.persistence.GeneratedValue


@Entity
@Embeddable
data class Account(
		val name: String = "",
		val password: String = "",
		var balance: Double = 0.0
) : Serializable {
	@javax.persistence.Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	val id: Long? = null
	
}