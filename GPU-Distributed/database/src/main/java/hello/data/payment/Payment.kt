package hello.data.payment

import java.sql.Timestamp
import javax.persistence.*

@Entity
class Payment(
		var sender: String = "",
		var receiver: String = ""
) {
	@Id
	@SequenceGenerator(name = "pk_sequence", sequenceName = "entity_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pk_sequence")
	@Column(name = "id", unique = true, nullable = false)
	val id = 0
	var timestamp = Timestamp(System.currentTimeMillis())
	
}