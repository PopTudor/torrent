package hello.business

import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class TwoPhaseScheduler(
		private val locksTable: LocksTable
) {
	private val transactions = mutableListOf<Transaction>()
	
	@Synchronized
	fun readLock(transaction: Transaction, resource: Any) = runBlocking {
			val table = resource.javaClass.simpleName
			when {
				locksTable.isUnlocked(transaction, table, resource) -> {
					val lock = Lock(LockType.READ, resource, table, transaction)
					locksTable.lock(lock, resource)
					lock.readLock().lock()
				}
				locksTable.hasReadLock(transaction, table, resource) -> locksTable.getLock(resource).readLock().lock()
				else -> {
					val waitingForReadLock = launch {
						while (locksTable.isLocked(transaction, table, resource))
							delay(5, TimeUnit.MILLISECONDS)
					}
					waitingForReadLock.join()
				}
			}
		
	}
	
	fun writeLock(it: Transaction, user: String): WaitForData {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	fun schedule(transaction: Transaction) {
		transactions += transaction
	}
	
}

enum class LockStatus { SUCCESS, FAIL }