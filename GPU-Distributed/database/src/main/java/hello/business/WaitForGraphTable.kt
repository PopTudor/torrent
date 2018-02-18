package hello.business

import org.springframework.stereotype.Service
import java.util.*


@Service
class WaitForGraphTable(
		val transactionsTable: TransactionsTable
) {
	private val graph = Collections.synchronizedSet(mutableSetOf<WaitFor>())
	
	operator fun plusAssign(waitFor: WaitFor) {
		graph += waitFor
	}
	
	@Synchronized
	fun isDeadlock(transaction: Transaction, transactionHasLock: Transaction): Boolean {
		val transaction1WaitFor = graph.filter { it.transWaitsLock.id == transaction.id }.map { it.transHasLock }
		val transaction2WaitFor = graph.filter { it.transHasLock.id == transactionHasLock.id }.map { it.transWaitsLock }
		val isDeadlock = transaction1WaitFor.contains(transactionHasLock) && transaction2WaitFor.contains(transaction)
		return isDeadlock
	}
	
	@Synchronized
	fun release(transaction: Transaction) {
		graph.removeIf { it.transWaitsLock.id == transaction.id }
		transactionsTable -= transaction
	}
}