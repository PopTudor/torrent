package hello.business

import org.springframework.stereotype.Service

@Service
class TransactionsTable {
	private val transactions = mutableListOf<Transaction>()
	
	operator fun plusAssign(transaction: Transaction) {
		transactions += transaction
	}
	
	operator fun minusAssign(transaction: Transaction) {
		transactions -= transaction
	}
}