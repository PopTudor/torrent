package hello

import hello.business.Transaction
import hello.business.TransactionManager
import hello.business.TransactionStatus
import hello.business.TwoPhaseScheduler
import hello.business.command.CreateAccount
import hello.business.command.GetAccount
import hello.data.DepositStatus
import hello.data.account.Account
import hello.data.account.AccountRepository
import org.springframework.stereotype.Service

@Service
class AccountManagerService(
		private val accountRepository: AccountRepository,
		private val twoPhaseScheduler: TwoPhaseScheduler,
		private val transactionManager: TransactionManager
) {
	init {
//		accountRepository.save(Account("tudor","parola",100.0))
	}
	
	
	fun createAccount(account: Account): Account? {
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		try {
			val createUser = CreateAccount(accountRepository, account)
			
			twoPhaseScheduler.writeLock(transaction, account)
			transactionManager.addCommands(transaction, createUser)
			transactionManager.commit(transaction)
			
			twoPhaseScheduler.releaseLocks(transaction) // comment to create deadlock by not releasing lock
			transaction.status = TransactionStatus.COMMIT
		} catch (abortException: AbortException) {
			println("rollback create: ${abortException.transaction}")
			transactionManager.rollback(transaction)
			twoPhaseScheduler.releaseLocks(transaction)
			transaction.status = TransactionStatus.ABORT
		}
		when {
			transaction.status == TransactionStatus.COMMIT -> return account
			transaction.status == TransactionStatus.ABORT -> return null
			else -> throw RuntimeException("When statement must have an else branch")
		}
	}
	
	fun deposit(deposit: Double, user: String): DepositStatus {
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		try {
			twoPhaseScheduler.readLock(transaction, user)
			val getUser = GetAccount(accountRepository, user) {
				if (it == null) throw AbortException(transaction)
			}
			
			transactionManager.addCommands(transaction, getUser)

//			TransactionHistory += transaction
//
//			account.balance += deposit
//
//			twoPhaseScheduler.writeLock(transaction, account)
//			saveAccount(account)
//			updateAccountHistory(account)

//			account = retrieveUser(user) ?: return DepositStatus(0.0, "User not found")
			
			
		} catch (abortException: AbortException) {
		
		}
		
		return DepositStatus(deposit, user)
	}
	
	private fun updateLog(account: Account) {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	private fun updateAccountHistory(account: Account) {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	private fun saveAccount(account: Account) {
		accountRepository.save(account)
	}
}