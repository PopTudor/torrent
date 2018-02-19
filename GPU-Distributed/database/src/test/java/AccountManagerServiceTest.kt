import hello.AccountManagerService
import hello.business.*
import hello.data.account.Account
import hello.data.account.AccountRepository
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AccountManagerServiceTest {
	lateinit var accountManagerService: AccountManagerService
	lateinit var accountRepository: AccountRepository
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
		val account = Account("test", "test")
		val result = accountManagerService.createAccount(account)
		val dbResult = accountRepository.findOne(account.id)
		
		Assert.assertEquals(account, result)
		Assert.assertEquals(account, dbResult)
		// check transaction has cleaned after itself
		Assert.assertTrue(locksTable.isEmpty())
		Assert.assertTrue(transactionsTable.isEmpty())
		Assert.assertTrue(waitForGraphTable.isEmpty())
	}
	
	
	fun createAccount(name: String): Account {
		val account = Account(name)
		account.id = randInt().toLong()
		return account
	}
}