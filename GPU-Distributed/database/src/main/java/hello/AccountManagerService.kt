package hello

import hello.business.*
import hello.business.command.CreateAccount
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
			twoPhaseScheduler.writeLock(transaction, account)
			
			val createUser = CreateAccount(accountRepository, account)
			transactionManager.addCommands(transaction, createUser)
			transactionManager.commit(transaction)
			
			twoPhaseScheduler.releaseLocks(transaction)
			transaction.status = TransactionStatus.COMMIT
		} catch (abortException: AbortException) {
			transactionManager.rollback(transaction)
			twoPhaseScheduler.releaseLocks(transaction)
			transaction.status = TransactionStatus.ABORT
		}
		when {
			transaction.status == TransactionStatus.COMMIT -> return account
			transaction.status == TransactionStatus.ABORT -> return null
			else -> throw RuntimeException("this should not happen")
		}
	}
	
	fun deposit(deposit: Double, user: String): DepositStatus {
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		twoPhaseScheduler.readLock(transaction, user)
		
		var account = retrieveUser(user)
		if (account == null) {
			twoPhaseScheduler.releaseLocks(transaction)
			return DepositStatus(0.0, "User not found")
		}
		TransactionHistory += transaction
		
		account.balance += deposit

//		twoPhaseScheduler.writeLock(transaction, account)
		saveAccount(account)
//		updateAccountHistory(account)
//
		account = retrieveUser(user) ?: return DepositStatus(0.0, "User not found")
		
		
		return DepositStatus(deposit, account.toString())
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
	
	private fun retrieveUser(user: String): Account? {
		return accountRepository.findByName(user).firstOrNull()
	}
}