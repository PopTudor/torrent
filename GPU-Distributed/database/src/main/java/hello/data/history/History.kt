package hello.data.history

import hello.business.Transaction
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class History(
		val transaction: Transaction?,
		@Id
		var id: String? = null
)