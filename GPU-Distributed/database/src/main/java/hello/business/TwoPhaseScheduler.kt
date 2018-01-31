package hello.business

import org.springframework.stereotype.Service

@Service
class TwoPhaseScheduler(
		private val locksTable: LocksTable,
		private val transactionTable: TransactionsTable
) {
	
	@Synchronized
	fun readLock(transaction: Transaction, resource: Any): Lock {
		val table = resource.javaClass.simpleName
		val lock = Lock(LockType.READ, resource, table, transaction)
		return when {
			locksTable.isUnlocked(lock) -> locksTable.lock(lock)
			locksTable.hasReadLock(lock) -> locksTable[lock]
			else -> lock
		}
	}
	
	fun writeLock(transaction: Transaction, resource: Any): WaitForData {
		val table = resource.javaClass.simpleName
		val lock = Lock(LockType.READ, resource, table, transaction)
		when {
			locksTable.isUnlocked(lock) -> locksTable.lock(lock)
			else -> TODO("wait for unlock")
		}
	}
	
	fun schedule(transaction: Transaction) {
		transactionTable += transaction
	}
	
	fun release(lock: Lock) {
		transactionTable -= lock.transaction
		locksTable -= lock
	}
	
}

enum class LockStatus { SUCCESS, FAIL }