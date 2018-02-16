import hello.business.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TwoPhaseSchedulerTest {
	lateinit var locksTable: LocksTable;
	lateinit var transactionTable: TransactionsTable
	lateinit var waitForGraphTable: WaitForGraphTable
	lateinit var twoPhaseScheduler: TwoPhaseScheduler
	
	@Before
	fun init() {
		transactionTable = TransactionsTable()
		locksTable = LocksTable(transactionTable)
		waitForGraphTable = WaitForGraphTable()
		twoPhaseScheduler = TwoPhaseScheduler(locksTable, waitForGraphTable)
	}
	
	@Test
	fun oneTransactionOneResourceUnlocked() {
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		val acquiredLock = twoPhaseScheduler.readLock(transaction, "test")
		val acquiredTransaction = transactionTable[acquiredLock.transaction]
		val storedLock = locksTable[acquiredLock]
		
		Assert.assertNotNull(acquiredLock)
		Assert.assertNotNull(acquiredTransaction)
		Assert.assertNotNull(storedLock)
		Assert.assertEquals(acquiredLock, storedLock)
		Assert.assertEquals(storedLock!!.transaction, acquiredTransaction)
	}
	
	@Test
	fun oneTransactionTwoResourceUnlocked() {
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		val readLock1 = twoPhaseScheduler.readLock(transaction, "test")
		val readLock2 = twoPhaseScheduler.readLock(transaction, "test1")
		Assert.assertNotNull(readLock1)
		Assert.assertNotNull(readLock2)
	}
	
}