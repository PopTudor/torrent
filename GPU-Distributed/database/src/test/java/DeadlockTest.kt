
import hello.AbortException
import hello.business.*
import hello.printAcquired
import hello.printBlocked
import hello.printFinish
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class DeadlockTest {
	lateinit var locksTable: LocksTable;
	lateinit var transactionTable: TransactionsTable
	lateinit var waitForGraphTable: WaitForGraphTable
	lateinit var twoPhaseScheduler: TwoPhaseScheduler
	
	@Before
	fun init() {
		transactionTable = TransactionsTable()
		locksTable = LocksTable(transactionTable)
		waitForGraphTable = WaitForGraphTable(transactionTable)
		twoPhaseScheduler = TwoPhaseScheduler(locksTable, waitForGraphTable)
	}
	
	@Ignore("blocks all the tests")
	@Test(expected = AbortException::class)
	fun twoTransaction_TwoResource_Deadlock() {
		val A = "test"
		val B = "test1"
		
		val transaction1 = Transaction(status = TransactionStatus.ACTIVE)
		twoPhaseScheduler.writeLock(transaction1, A)
		transaction1.printAcquired(A)
		
		
		val thread = thread {
			try {
				val transaction2 = Transaction(status = TransactionStatus.ACTIVE)
				twoPhaseScheduler.writeLock(transaction2, B)
				transaction2.printAcquired(B)
				
				transaction2.printBlocked()
				twoPhaseScheduler.writeLock(transaction2, A)
				twoPhaseScheduler.releaseLocks(transaction2)
				transaction2.printFinish()
			} catch (exception: AbortException) {
				println(exception.transaction)
				twoPhaseScheduler.releaseLocks(exception.transaction)
			}
		}
		Thread.sleep(10)
		transaction1.printBlocked()
		twoPhaseScheduler.writeLock(transaction1, B)
		twoPhaseScheduler.releaseLocks(transaction1)
		transaction1.printFinish()
		thread.join()
		
	}
	
	/**
	 * 1  2
	 * 2
	 * 1 = [2]
	 * 2 = []
	 */
	@Ignore("blocks all the tests")
	@Test
	fun twoTransaction_TwoResource_NotDeadlock() {
		val A = "test"
		val B = "test1"
		
		val transaction1 = Transaction(status = TransactionStatus.ACTIVE)
		twoPhaseScheduler.writeLock(transaction1, A)
		transaction1.printAcquired(A)
		
		val thread = thread {
			try {
				val transaction2 = Transaction(status = TransactionStatus.ACTIVE)
				twoPhaseScheduler.writeLock(transaction2, B)
				transaction2.printAcquired(B)
				println("trans2 finished")
				twoPhaseScheduler.releaseLocks(transaction2)
			} catch (exception: AbortException) {
				println("trans2 abort" + exception.transaction)
			}
		}
		
		Thread.sleep(30)
		
		transaction1.printBlocked()
		twoPhaseScheduler.writeLock(transaction1, B)
		transaction1.printFinish()
		twoPhaseScheduler.releaseLocks(transaction1)
		thread.join()
	}
	
	/**
	 * 1  2
	 * 2
	 * 1 = [2]
	 * 2 = []
	 */
	@Ignore("blocks all the tests")
	@Test
	fun twoTransaction_twoResource_WaitFor_T1_toFinish() {
		val A = "test"
		
		val transaction1 = Transaction(status = TransactionStatus.ACTIVE)
		twoPhaseScheduler.writeLock(transaction1, A)
		transaction1.printAcquired(A)
		
		val thread = thread {
			try {
				val transaction2 = Transaction(status = TransactionStatus.ACTIVE)
				transaction2.printBlocked()
				twoPhaseScheduler.writeLock(transaction2, A)
				transaction2.printAcquired(A)
				println("trans2 finished")
				twoPhaseScheduler.releaseLocks(transaction2)
			} catch (exception: AbortException) {
				println("trans2 abort" + exception.transaction)
			}
		}
		Thread.sleep(300)
		transaction1.printFinish()
		twoPhaseScheduler.releaseLocks(transaction1)
		thread.join()
	}
	
	@Ignore("blocks all the tests")
	@Test
	fun threeTransaction_ThreeResource_Deadlock() {
		val resource1 = "test1"
		val resource2 = "test2"
		val resource3 = "test3"
		
		val transaction1 = Transaction(status = TransactionStatus.ACTIVE)
		twoPhaseScheduler.writeLock(transaction1, resource1)
		transaction1.printAcquired(resource1)
		
		val pool = Executors.newFixedThreadPool(3)
		pool.submit {
			try {
				println(Thread.currentThread().name + " ")
				
				val transaction = Transaction(status = TransactionStatus.ACTIVE)
				twoPhaseScheduler.writeLock(transaction, resource2)
				transaction.printAcquired(resource2)
				twoPhaseScheduler.writeLock(transaction, resource3)
				transaction.printAcquired(resource3)
				twoPhaseScheduler.writeLock(transaction, resource1)
				transaction.printAcquired(resource1)
				transaction.printFinish()
				twoPhaseScheduler.releaseLocks(transaction)
				
			} catch (e: AbortException) {
				println("abort: ${e.transaction}")
			}
		}
		pool.submit {
			try {
				println(Thread.currentThread().name + " ")
				
				val transaction = Transaction(status = TransactionStatus.ACTIVE)
				twoPhaseScheduler.writeLock(transaction, resource3)
				transaction.printAcquired(resource3)
				twoPhaseScheduler.writeLock(transaction, resource2)
				transaction.printAcquired(resource2)
				twoPhaseScheduler.writeLock(transaction, resource1)
				transaction.printAcquired(resource1)
				transaction.printFinish()
				twoPhaseScheduler.releaseLocks(transaction)
			} catch (e: AbortException) {
				println("abort: ${e.transaction}")
			}
		}
		pool.submit {
			// no deadlock
			try {
				println(Thread.currentThread().name + " transaction4")
				val res4 = "test4"
				val transaction = Transaction(status = TransactionStatus.ACTIVE)
				twoPhaseScheduler.writeLock(transaction, res4)
				transaction.printAcquired(res4)
				transaction.printFinish()
				println("end tran4: ${transaction}")
			} catch (e: AbortException) {
				println("abort tran4: ${e.transaction}")
			}
		}
		
		Thread.sleep(30)
		transaction1.printBlocked()
		try {
			twoPhaseScheduler.writeLock(transaction1, resource2)
			twoPhaseScheduler.writeLock(transaction1, resource3)
			Thread.sleep(1000)
			twoPhaseScheduler.releaseLocks(transaction1)
			pool.shutdown()
		} catch (e: AbortException) {
			println("abort: ${e.transaction}")
			pool.shutdown()
		}
		
		transaction1.printFinish()
		pool.awaitTermination(10, TimeUnit.SECONDS)
		
	}
}