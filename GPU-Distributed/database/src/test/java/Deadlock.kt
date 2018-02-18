import hello.business.*
import hello.printAcquired
import hello.printBlocked
import hello.printFinish
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import kotlin.concurrent.thread

class Deadlock {
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
	@Test
	fun twoTransaction_OneResource_Deadlock() {
		val resource1 = "test"
		val resource2 = "test1"
		
		val transaction1 = Transaction(status = TransactionStatus.ACTIVE)
		twoPhaseScheduler.writeLock(transaction1, resource1)
		transaction1.printAcquired(resource1)
		
		val thread = thread {
			val transaction2 = Transaction(status = TransactionStatus.ACTIVE)
			twoPhaseScheduler.writeLock(transaction2, resource2)
			transaction2.printAcquired(resource2)
			
			transaction2.printBlocked()
			twoPhaseScheduler.writeLock(transaction2, resource1)
			transaction2.printFinish()
		}
		
		Thread.sleep(30)
		transaction1.printBlocked()
		
		twoPhaseScheduler.writeLock(transaction1, resource2)
		
		transaction1.printFinish()
		thread.join()
	}
	
	@Ignore("blocks all the tests")
	@Test
	fun threeTransaction_OneResource_Deadlock() {
		val resource1 = "test"
		val resource2 = "test1"
		val resource3 = "test2"
		
		val transaction1 = Transaction(status = TransactionStatus.ACTIVE)
		twoPhaseScheduler.writeLock(transaction1, resource1)
		transaction1.printAcquired(resource1)
		
		val thread = thread {
			val transaction2 = Transaction(status = TransactionStatus.ACTIVE)
			twoPhaseScheduler.writeLock(transaction2, resource2)
			transaction2.printAcquired(resource2)
			twoPhaseScheduler.writeLock(transaction2, resource3)
			transaction2.printAcquired(resource2)
			
			
			transaction2.printBlocked()
			twoPhaseScheduler.writeLock(transaction2, resource1)
			transaction2.printFinish()
		}
		val thread1 = thread {
			val transaction3 = Transaction(status = TransactionStatus.ACTIVE)
			twoPhaseScheduler.writeLock(transaction3, resource2)
			twoPhaseScheduler.writeLock(transaction3, resource3)
			twoPhaseScheduler.writeLock(transaction3, resource1)
			transaction3.printAcquired(resource2)
			
			transaction3.printBlocked()
			transaction3.printFinish()
		}
		val thread2 = thread {
			val transaction3 = Transaction(status = TransactionStatus.ACTIVE)
			twoPhaseScheduler.writeLock(transaction3, resource1)
			twoPhaseScheduler.writeLock(transaction3, resource2)
			twoPhaseScheduler.writeLock(transaction3, resource3)
			transaction3.printAcquired(resource2)
			
			transaction3.printBlocked()
			transaction3.printFinish()
		}
		
		Thread.sleep(30)
		transaction1.printBlocked()
		
		twoPhaseScheduler.writeLock(transaction1, resource2)
		
		transaction1.printFinish()
		thread.join()
		thread1.join()
		thread2.join()
	}
}