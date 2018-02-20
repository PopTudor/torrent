package hello.business

import hello.AbortException
import org.springframework.stereotype.Service

@Service
class TwoPhaseScheduler(
		private val locksTable: LocksTable,
		private val waitForGraphTable: WaitForGraphTable
) {
	
	@Synchronized
	fun readLock(transaction: Transaction, resource: Any): Lock {
		val table = resource.javaClass.simpleName
		val lock = Lock(LockType.READ, resource, table, transaction)
		return when {
			locksTable.isUnlocked(lock) -> {
				locksTable += lock
				lock
			}
			locksTable.hasReadLock(lock) -> {
				locksTable += lock
				lock
			}
			
			locksTable.hasWriteLock(lock) -> {
				if (waitForLock(lock, transaction)) return lock
				else {
					locksTable += lock
					lock
				}
			}
			else -> throw IllegalArgumentException()
		}
	}
	
	@Synchronized
	fun writeLock(transaction: Transaction, resource: Any): Lock {
		val table = resource.javaClass.simpleName
		val lock = Lock(LockType.WRITE, resource, table, transaction)
		return when {
			locksTable.isUnlocked(lock) -> {
				locksTable += (lock)
				lock
			}
			locksTable.hasReadLock(lock) -> {
				locksTable += lock
				lock
			}
			locksTable.hasWriteLock(lock) -> {
				if (waitForLock(lock, transaction)) return lock
				else {
					locksTable += lock
					lock
				}
			}
			else -> throw IllegalArgumentException("Something is wrong")
		}
	}
	
	private fun waitForLock(lock: Lock, transaction: Transaction): Boolean {
		while (locksTable.hasWriteLock(lock)) {
			val transactionHasLock = locksTable[lock].firstOrNull()?.transaction ?: return true
			if (transactionHasLock.id == transaction.id) return false
			waitForGraphTable += Node(lock, transaction, transactionHasLock)
			
			if (waitForGraphTable.isDeadlock(transaction, transactionHasLock)) {
				transaction.status = TransactionStatus.ABORT
				releaseLocks(transaction)
				println("Deadlock !!")
				throw AbortException(transaction)
			}
			
			Thread.sleep(30)
		}
		return false
	}
	
	fun releaseLocks(transaction: Transaction) {
		waitForGraphTable.release(transaction)
		locksTable.release(transaction)
	}
}