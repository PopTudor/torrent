package hello.business

import org.springframework.stereotype.Service

@Service
class Transactions(val locks: Locks) {
	private val transactions = mutableListOf<Transaction>()
	
	@Synchronized
	fun start(block: () -> Unit) {
		transactions += Transaction(status = TransactionStatus.ACTIVE)
		
		
		
	}
}