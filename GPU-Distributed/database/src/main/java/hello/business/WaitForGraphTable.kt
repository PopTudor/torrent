package hello.business

import org.springframework.stereotype.Service
import java.util.*


@Service
class WaitForGraphTable(
		val transactionsTable: TransactionsTable
) {
	private val graph = Collections.synchronizedSet(mutableSetOf<Node>())
	
	operator fun plusAssign(node: Node) {
		graph += node
	}
	
	/**
	 * 1  2
	 * 2  1
	 * 1  3
	 * 1 = [2,3]
	 * 2 = [1]
	 * ---------
	 * 1  2
	 * 2
	 * 1 = [2]
	 * 2 = []
	 */
	@Synchronized
	fun isDeadlock(waitingTrans: Transaction, transactionHasLock: Transaction): Boolean {
		val transaction1WaitFor = graph.filter { it.transWaitsLock.id == waitingTrans.id }.map { it.transHasLock }
		val transaction2WaitFor = graph.filter { it.transHasLock.id == transactionHasLock.id }.map { it.transWaitsLock }
		return transaction1WaitFor.contains(transactionHasLock) && transaction2WaitFor.contains(waitingTrans)
	}
	
	@Synchronized
	fun release(transaction: Transaction) {
		graph.removeIf { it.transWaitsLock.id == transaction.id }
		transactionsTable -= transaction
	}
}