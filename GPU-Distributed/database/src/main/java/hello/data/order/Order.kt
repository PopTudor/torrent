package hello.data.order

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Order(
		var amount: Double,
		var orderType: String,
		var account: String,
		@Id
		var id: String? = null
) {
	constructor() : this(0.0, "", "", null)
}