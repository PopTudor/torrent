package hello.business

import org.springframework.stereotype.Service
import java.util.*

@Service
class WaitForGraphTable(
		val transactionsTable: TransactionsTable
) {
	private val waitForList = Collections.synchronizedList(mutableListOf<WaitFor>())
	
	operator fun plusAssign(waitFor: WaitFor) {
		waitForList += waitFor
	}
	
	fun isDeadlock(transactionWaitsLock: Transaction, transactionHasLock: Transaction): Boolean {
		for (wait in waitForList) {
			if (wait.transHasLock.id != transactionWaitsLock.id && transactionHasLock.id != wait.transWaitsLock.id) {
				return true
			}
		}
		return false;
	}
	
	fun release(transaction: Transaction) {
		waitForList.removeIf { it.transWaitsLock.id == transaction.id }
		transactionsTable -= transaction
	}
}