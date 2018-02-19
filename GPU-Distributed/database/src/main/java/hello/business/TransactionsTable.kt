package hello.business

import org.springframework.stereotype.Service
import java.util.*

@Service
class TransactionsTable {
	private val transactions = Collections.synchronizedList(mutableListOf<Transaction>())
	
	
	operator fun plusAssign(transaction: Transaction) {
		transactions += transaction
	}
	
	operator fun minusAssign(transaction: Transaction) {
		transactions -= transaction
	}
	
	operator fun get(transaction: Transaction): Transaction? {
		return transactions.find { it == transaction }
	}
	
	fun isEmpty() = transactions.isEmpty()
}
