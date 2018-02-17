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
	fun oneTransaction_OneResource_Unlocked() {
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		val readlock = twoPhaseScheduler.readLock(transaction, "test")
		val acquiredTransaction = transactionTable[readlock.transaction]
		val storedLock = locksTable[readlock]
		
		Assert.assertNotNull(readlock)
		Assert.assertNotNull(acquiredTransaction)
		Assert.assertTrue(storedLock.isNotEmpty())
		Assert.assertEquals(readlock, storedLock[0])
		Assert.assertEquals(storedLock[0].transaction, acquiredTransaction)
	}
	
	@Test
	fun oneTransaction_TwoResource_Unlocked() {
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		val readLock1 = twoPhaseScheduler.readLock(transaction, "test")
		val readLock2 = twoPhaseScheduler.readLock(transaction, "test1")
		
		val aquiredTransaction = transactionTable[readLock1.transaction]
		val aquiredTransaction1 = transactionTable[readLock2.transaction]
		
		val storedLock1 = locksTable[readLock1]
		val storedLock2 = locksTable[readLock2]
		
		Assert.assertNotNull(readLock1)
		Assert.assertNotNull(readLock2)
		
		Assert.assertNotNull(aquiredTransaction)
		Assert.assertNotNull(aquiredTransaction1)
		
		Assert.assertTrue(storedLock1.isNotEmpty())
		Assert.assertTrue(storedLock2.isNotEmpty())
		
		Assert.assertEquals(transaction, aquiredTransaction)
		Assert.assertEquals(transaction, aquiredTransaction1)
		Assert.assertEquals(readLock1, storedLock1[0])
		Assert.assertEquals(readLock2, storedLock2[0])
		Assert.assertNotEquals(readLock1, readLock2)
	}
	
	@Test
	fun oneTransaction_SameResource_ReadLocked() {
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		val readLock1 = twoPhaseScheduler.readLock(transaction, "test")
		val readLock2 = twoPhaseScheduler.readLock(transaction, "test")
		
		val aquiredTransaction = transactionTable[readLock1.transaction]
		val aquiredTransaction1 = transactionTable[readLock2.transaction]
		
		val storedLock1 = locksTable[readLock1]
		val storedLock2 = locksTable[readLock2]
		
		Assert.assertNotNull(readLock1)
		Assert.assertNotNull(readLock2)
		Assert.assertNotNull(aquiredTransaction)
		Assert.assertNotNull(aquiredTransaction1)
		Assert.assertNotNull(storedLock1)
		Assert.assertNotNull(storedLock2)
		Assert.assertEquals(transaction, aquiredTransaction)
		Assert.assertEquals(transaction, aquiredTransaction1)
		Assert.assertEquals(readLock1, storedLock1[0])
		Assert.assertEquals(readLock2, storedLock2[0])
		Assert.assertEquals(readLock1, readLock2)
		Assert.assertEquals(storedLock1, storedLock2)
	}
	
	@Test
	fun oneTransaction_OneResource_ReadLocked() {
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		val readLock1 = twoPhaseScheduler.readLock(transaction, "test")
		val aquiredTransaction = transactionTable[readLock1.transaction]
		val storedLock1 = locksTable[readLock1]
		
		Assert.assertNotNull(readLock1)
		Assert.assertNotNull(aquiredTransaction)
		Assert.assertTrue(storedLock1.isNotEmpty())
		Assert.assertEquals(transaction, aquiredTransaction)
		Assert.assertEquals(readLock1, storedLock1[0])
	}
	
	@Test
	fun twoTransaction_OneResource_ReadLocked() {
		val transaction1 = Transaction(status = TransactionStatus.ACTIVE)
		val transaction2 = Transaction(status = TransactionStatus.ACTIVE)
		
		val readLock1 = twoPhaseScheduler.readLock(transaction1, "test")
		val readLock2 = twoPhaseScheduler.readLock(transaction2, "test")
		
		val aquiredTransaction1 = transactionTable[readLock1.transaction]
		val aquiredTransaction2 = transactionTable[readLock2.transaction]
		
		val storedLock1 = locksTable[readLock1]
		val storedLock2 = locksTable[readLock2]
		
		Assert.assertEquals(transaction1, aquiredTransaction1)
		Assert.assertEquals(transaction2, aquiredTransaction2)
		
		Assert.assertEquals(readLock1, storedLock1[0]) // lock1 is acquired
		Assert.assertEquals(readLock2, storedLock2[1]) // lock2 is acquired
		Assert.assertEquals(readLock1, storedLock2[0]) // lock1 and lock2 share same resource
	}
	
	
	
}