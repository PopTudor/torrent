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
					val transHasLock = locksTable[lock][0].transaction
					if (waitForGraphTable.isDeadlock()) {
						transaction.status = TransactionStatus.ABORT
						releaseLocks(transaction)
					}
					waitForGraphTable += WaitFor(lock, transHasLock, transaction)
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
					if (waitForGraphTable.isDeadlock()) {
						transactionHasLock.status = TransactionStatus.ABORT
						releaseLocks(transactionHasLock)
					}
					
					waitForGraphTable += WaitFor(lock, transactionHasLock, transaction)
					Thread.sleep(100)
				}
				locksTable += lock
				lock
			}
			else -> throw IllegalArgumentException("Something is wrong")
		}
	}
	
	fun releaseLocks(transaction: Transaction) {
		locksTable.release(transaction)
		waitForGraphTable.release(transaction)
	}
	
	fun release(lock: Lock) {
		locksTable -= lock
	}
	
}

enum class LockStatus { SUCCESS, FAIL }