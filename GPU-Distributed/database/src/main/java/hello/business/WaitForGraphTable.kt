package hello.business

import org.springframework.stereotype.Service
import java.util.*


@Service
open class WaitForGraphTable(
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
	fun isDeadlock(waitForTrans: Transaction, hasLockTrans: Transaction): Boolean {
		if (waitForTrans.id == hasLockTrans.id) return false
		val transaction1WaitFor = graph.filter { it.lock.transaction.id == waitForTrans.id }.map { it.transHasLock }
		val transaction2WaitFor = graph.filter { it.lock.transaction.id == hasLockTrans.id }.map { it.transHasLock }
		return transaction1WaitFor.contains(hasLockTrans) && transaction2WaitFor.contains(waitForTrans)
	}
	
	@Synchronized
	fun release(transaction: Transaction) {
		graph.removeIf { it.transWaitsLock.id == transaction.id }
		transactionsTable -= transaction
	}
	
	fun isEmpty(): Boolean {
		return graph.isEmpty()
	}
}