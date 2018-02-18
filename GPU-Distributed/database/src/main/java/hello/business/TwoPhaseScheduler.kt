package hello.business

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
				if (locksTable.hasWriteLockForTransaction(transaction)) {
					return lock
				}
				
				while (locksTable.hasWriteLock(lock)) {
					val transactionHasLock = locksTable[lock][0].transaction
					if (waitForGraphTable.isDeadlock(transaction, transactionHasLock)) {
						transaction.status = TransactionStatus.ABORT
						releaseLocks(transaction)
					}
					waitForGraphTable += Node(lock, transactionHasLock, transaction)
					Thread.sleep(100)
				}
				locksTable += lock
				lock
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
				while (locksTable.hasWriteLock(lock)) {
					val transactionHasLock = locksTable[lock].firstOrNull()?.transaction ?: return lock
					waitForGraphTable += Node(lock, transaction, transactionHasLock)
					
					if (waitForGraphTable.isDeadlock(transaction, transactionHasLock)) {
						transaction.status = TransactionStatus.ABORT
						releaseLocks(transaction)
						println("Deadlock !!")
						throw hello.AbortException(transaction)
					}
					
					Thread.yield()
				}
				locksTable += lock
				lock
			}
			else -> throw IllegalArgumentException("Something is wrong")
		}
	}
	
	fun releaseLocks(transaction: Transaction) {
		waitForGraphTable.release(transaction)
		locksTable.release(transaction)
	}
	
	fun release(lock: Lock) {
		locksTable -= lock
	}
	
}

enum class LockStatus { SUCCESS, FAIL }