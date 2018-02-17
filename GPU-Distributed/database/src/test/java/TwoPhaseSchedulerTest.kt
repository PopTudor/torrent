import hello.business.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

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
	fun oneTransaction_OneResource_Unlocked_Acquires_ReadLock() {
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		val readlock = twoPhaseScheduler.readLock(transaction, "test")
		val storedLock = locksTable[readlock]
		
		Assert.assertNotNull(readlock)
		Assert.assertTrue(readlock.type == LockType.READ)
		Assert.assertTrue(storedLock.isNotEmpty())
		Assert.assertEquals(readlock, storedLock[0])
	}
	
	@Test
	fun oneTransaction_OneResource_Unlocked_StoresTransaction() {
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		val readlock = twoPhaseScheduler.readLock(transaction, "test")
		val acquiredTransaction = transactionTable[readlock.transaction]
		
		Assert.assertNotNull(acquiredTransaction)
		Assert.assertEquals(transaction, acquiredTransaction)
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
		
		Assert.assertTrue(readLock1.type == LockType.READ)
		Assert.assertTrue(readLock2.type == LockType.READ)
		
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
	
	@Test
	fun oneTransaction_OneResource_ReadLock_WriteLock_AcquiresLocks() {
		val transaction1 = Transaction(status = TransactionStatus.ACTIVE)
		val readLock = twoPhaseScheduler.readLock(transaction1, "test")
		val writeLock = twoPhaseScheduler.writeLock(transaction1, "test")
		
		Assert.assertNotNull(readLock)
		Assert.assertNotNull(writeLock)
		
		Assert.assertTrue(readLock.type == LockType.READ)
		Assert.assertTrue(writeLock.type == LockType.WRITE)
		
		Assert.assertNotEquals(readLock, writeLock)
	}
	
	@Test
	fun oneTransaction_OneResource_ReadLock_WriteLock_StoresTransaction() {
		val transaction1 = Transaction(status = TransactionStatus.ACTIVE)
		val readLock = twoPhaseScheduler.readLock(transaction1, "test")
		val writeLock = twoPhaseScheduler.writeLock(transaction1, "test")
		val storedTransaction = transactionTable[readLock.transaction]
		val storedTransaction1 = transactionTable[writeLock.transaction]
		
		Assert.assertNotNull(storedTransaction)
		Assert.assertNotNull(storedTransaction1)
	}
	
	@Test
	fun oneTransaction_OneResource_ReadLock_WriteLock_StoresSameTransaction() {
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		val readLock = twoPhaseScheduler.readLock(transaction, "test")
		val writeLock = twoPhaseScheduler.writeLock(transaction, "test")
		val storedTransaction1 = transactionTable[readLock.transaction]
		val storedTransaction2 = transactionTable[writeLock.transaction]
		
		Assert.assertEquals(transaction, storedTransaction1)
		Assert.assertEquals(transaction, storedTransaction2)
		Assert.assertEquals(storedTransaction1, storedTransaction2)
	}
	
	@Test
	fun oneTransaction_OneResource_ReadLock_WriteLock_ReadLock() {
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		val readLock1 = twoPhaseScheduler.readLock(transaction, "test")
		val writeLock = twoPhaseScheduler.writeLock(transaction, "test")
		val readLock2 = twoPhaseScheduler.readLock(transaction, "test")
		
		Assert.assertNotNull(readLock1)
		Assert.assertNotNull(readLock2)
		Assert.assertNotNull(writeLock)
		Assert.assertEquals(readLock1, readLock2)
		
		Assert.assertTrue(readLock1.isReadLock())
		Assert.assertTrue(readLock2.isReadLock())
		Assert.assertTrue(writeLock.isWriteLock())
	}
	
	@Test
	fun oneTransaction_OneDifferentResource_ReadLock_WriteLock_ReadLock() {
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		val readLock1 = twoPhaseScheduler.readLock(transaction, "test")
		val writeLock = twoPhaseScheduler.writeLock(transaction, "test")
		val readLock2 = twoPhaseScheduler.readLock(transaction, "test2")
		
		Assert.assertNotNull(readLock1)
		Assert.assertNotNull(readLock2)
		Assert.assertNotNull(writeLock)
		Assert.assertNotEquals(readLock1, readLock2)
		
		Assert.assertTrue(readLock1.isReadLock())
		Assert.assertTrue(readLock2.isReadLock())
		Assert.assertTrue(writeLock.isWriteLock())
	}
	
	@Test
	fun oneTransaction_OneResource_WriteLock_ReadLock() {
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		val writeLock = twoPhaseScheduler.writeLock(transaction, "test")
		val readLock1 = twoPhaseScheduler.readLock(transaction, "test")
		
		Assert.assertNotNull(readLock1)
		Assert.assertNotNull(writeLock)
		
		Assert.assertTrue(readLock1.isReadLock())
		Assert.assertTrue(writeLock.isWriteLock())
	}
	
	@Test
	fun oneTransaction_OneResource_ReleaseLocks() {
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		val writeLock = twoPhaseScheduler.readLock(transaction, "test")
		val readLock1 = twoPhaseScheduler.writeLock(transaction, "test")
		
		twoPhaseScheduler.releaseLocks(transaction)
		
		val numOfAliveLocks = locksTable[readLock1].size
		val numOfAliveTransactions = locksTable[readLock1].map { it.transaction }.size
		
		Assert.assertTrue(numOfAliveLocks == 0)
		Assert.assertTrue(numOfAliveTransactions == 0)
		
	}
	
	@Test
	fun twoTransaction_OneResource_WriteLock_ReadLock() {
		val countDownLatch = CountDownLatch(1)
		
		val transaction1 = Transaction(status = TransactionStatus.ACTIVE)
		twoPhaseScheduler.writeLock(transaction1, "test")
		
		val thread = thread {
			val transaction2 = Transaction(status = TransactionStatus.ACTIVE)
			println("blocked $transaction2")
			twoPhaseScheduler.readLock(transaction2, "test")
			println("finished: $transaction2")
		}
		
		println("holding R: $transaction1")
		countDownLatch.await(2, TimeUnit.SECONDS)
		twoPhaseScheduler.releaseLocks(transaction1)
		println("finished: $transaction1")
		thread.join()
	}

//	@Test
//	fun twoTransaction_OneResource_WriteLock_WriteLock() {
//		val countDownLatch = CountDownLatch(1)
//
//		val transaction1 = Transaction(status = TransactionStatus.ACTIVE)
//		twoPhaseScheduler.writeLock(transaction1, "test")
//
//		val thread = thread {
//			val transaction2 = Transaction(status = TransactionStatus.ACTIVE)
//			println("blocked $transaction2")
//			twoPhaseScheduler.readLock(transaction2, "test")
//			println("finished: $transaction2")
//		}
//
//		println("holding R: $transaction1")
//		countDownLatch.await(2, TimeUnit.SECONDS)
//		twoPhaseScheduler.releaseLocks(transaction1)
//		println("finished: $transaction1")
//		thread.join()
//	}
	
	
}