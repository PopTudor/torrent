package hello.data.payment

import hello.data.account.Account
import java.sql.Timestamp
import javax.persistence.*

@Entity
data class Payment(
		val senderId: Int? = null,
		val receiverId: Int? = null
) {
	@javax.persistence.Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	var id: Long? = null
	val timestamp = Timestamp(System.currentTimeMillis())
	
}