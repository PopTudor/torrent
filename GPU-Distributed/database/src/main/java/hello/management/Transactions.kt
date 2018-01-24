package hello.management

import org.springframework.stereotype.Service
import java.sql.Timestamp

@Service
class Transactions(val locks: Locks) {
	private val transactions = mutableListOf<Transaction>()
	
	@Synchronized
	fun start(block: () -> Unit) {
		transactions += Transaction(status = TransactionStatus.ACTIVE)
		
		
		
	}
}