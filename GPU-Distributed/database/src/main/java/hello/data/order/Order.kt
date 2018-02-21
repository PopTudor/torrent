package hello.data.order

import hello.data.account.Account
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Order(
		val amount: Double,
		val orderType: OrderType,
		val account: Account
) {
	@javax.persistence.Id
	var id: String? = null
	
}

enum class OrderType {
	BUY, SELL
}