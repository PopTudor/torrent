import hello.AbortException
import hello.AccountManagerService
import hello.business.*
import hello.data.account.Account
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.concurrent.thread

class AccountManagerServiceTest {
	lateinit var accountManagerService: AccountManagerService
	lateinit var accountRepository: AccountRepositoryMock
	lateinit var twoPhaseScheduler: TwoPhaseScheduler
	lateinit var transactionManager: TransactionManager
	lateinit var transactionsTable: TransactionsTable
	lateinit var locksTable: LocksTable
	lateinit var waitForGraphTable: WaitForGraphTable
	
	@Before
	fun init() {
		transactionsTable = TransactionsTable()
		locksTable = LocksTable(transactionsTable)
		waitForGraphTable = WaitForGraphTable(transactionsTable)
		accountRepository = AccountRepositoryMock()
		twoPhaseScheduler = TwoPhaseScheduler(locksTable, waitForGraphTable)
		transactionManager = TransactionManager()
		accountManagerService = AccountManagerService(accountRepository, twoPhaseScheduler, transactionManager)
	}
	
	@Test
	fun createAccount_noConflict_Commit() {
		val account = createAccount("test")
		val result = accountManagerService.createAccount(account)
		val dbResult = accountRepository.findOne(account.id)
		
		Assert.assertEquals(account, result)
		Assert.assertEquals(account, dbResult)
		// check transaction has cleaned after itself
		Assert.assertTrue(locksTable.isEmpty())
		Assert.assertTrue(transactionsTable.isEmpty())
		Assert.assertTrue(waitForGraphTable.isEmpty())
	}
	
	@Test
	fun createAccount_Conflict_Rollback() {
		val account = createAccount("test")
		val result1 = accountManagerService.createAccount(account)
		val dbResult1 = accountRepository.findOne(account.id)
		try {
			val thread = thread {
				accountManagerService.createAccount(account)
				accountRepository.findOne(account.id)
			}
			Thread.sleep(100)
			
			thread.join()
		} catch (abortException: AbortException) {
			Assert.assertEquals(account, result1)
			Assert.assertEquals(account, dbResult1)
			Assert.assertTrue(accountRepository.size() == 1)
		}
		// check transaction has cleaned after itself
		Assert.assertTrue(locksTable.isEmpty())
		Assert.assertTrue(transactionsTable.isEmpty())
		Assert.assertTrue(waitForGraphTable.isEmpty())
	}
	
	
	fun createAccount(name: String): Account {
		val account = Account(name, name)
		account.id = randInt().toLong()
		return account
	}
}