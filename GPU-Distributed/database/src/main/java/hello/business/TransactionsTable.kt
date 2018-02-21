package hello.business

import org.springframework.stereotype.Service
import java.util.*

@Service
open class TransactionsTable {
	private val transactions = Collections.synchronizedSet(mutableSetOf<Transaction>())
	
	
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
	
	fun size() = transactions.size
	
	fun contains(transaction: Transaction) = transactions.contains(transaction)
}
