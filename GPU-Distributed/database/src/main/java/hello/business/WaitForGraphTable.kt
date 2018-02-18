package hello.business

import org.springframework.stereotype.Service
import java.util.*

@Service
class WaitForGraphTable(
		val transactionsTable: TransactionsTable
) {
	private val waitForList = Collections.synchronizedSet(mutableSetOf<WaitFor>())
	
	operator fun plusAssign(waitFor: WaitFor) {
		waitForList += waitFor
	}
	
	fun isDeadlock(transactionHasLock: Transaction, transactionWaitsLock: Transaction): Boolean {
		return waitForList.any { transactionHasLock.id == it.transWaitsLock.id && transactionWaitsLock.id == it.transHasLock.id }
	}
	
	fun release(transaction: Transaction) {
		waitForList.removeIf { it.transWaitsLock.id == transaction.id }
		transactionsTable -= transaction
	}
}