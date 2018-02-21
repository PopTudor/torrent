package hello

import hello.business.Transaction
import hello.business.TransactionStatus
import hello.business.TwoPhaseScheduler
import hello.business.command.*
import hello.data.DepositStatus
import hello.data.account.Account
import hello.data.account.AccountRepository
import hello.data.history.HistoryRepository
import org.springframework.stereotype.Service

@Service
class AccountManagerService(
		private val accountRepository: AccountRepository,
		private val historyRepository: HistoryRepository,
		private val twoPhaseScheduler: TwoPhaseScheduler,
		private val transactionManagerCommands: TransactionManagerCommands
) {
	init {
//		accountRepository.save(Account("tudor","parola",100.0))
	}
	
	
	fun createAccount(account: Account): Account? {
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		try {
			val createUser = CreateAccount(accountRepository, account)
			
			twoPhaseScheduler.writeLock(transaction, account)
			transactionManagerCommands.addCommands(transaction, createUser)
			transactionManagerCommands.commit(transaction)
			
			twoPhaseScheduler.releaseLocks(transaction) // comment to create deadlock by not releasing lock
			transaction.status = TransactionStatus.COMMIT
		} catch (abortException: AbortException) {
			abort(abortException.transaction)
		} catch (exception: Exception) {
			abort(transaction)
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
			val getUser = GetAccount(accountRepository, user) { account ->
				if (account == null) throw AbortException(transaction)
				val backupAccount = account.copy()
				
				account.balance += deposit
				val accToSave = UpdateAccount(accountRepository, account)
				accToSave.reverseCommand = UndoUpdateAccount(accountRepository, backupAccount)
				
				twoPhaseScheduler.writeLock(transaction, account)
				transactionManagerCommands.addCommands(transaction, accToSave)
			}
			transactionManagerCommands.addCommands(transaction, getUser)
			transactionManagerCommands.addCommands(transaction, History(historyRepository, transaction))
			transactionManagerCommands.commit(transaction)
			
			twoPhaseScheduler.releaseLocks(transaction)
			transaction.status = TransactionStatus.COMMIT
		} catch (abortException: AbortException) {
			abort(abortException.transaction)
		}
		when {
			transaction.status == TransactionStatus.COMMIT -> return DepositStatus(deposit, user)
			transaction.status == TransactionStatus.ABORT -> return DepositStatus(0.0, "Could not be deposited")
			else -> throw RuntimeException("When statement must have an else branch")
		}
	}


//	fun withdraw(deposit: Double, user: String): DepositStatus {
//		val transaction = Transaction(status = TransactionStatus.ACTIVE)
//		try {
//			twoPhaseScheduler.readLock(transaction, user)
//			val getUser = GetAccount(accountRepository, user) { account ->
//				if (account == null) throw AbortException(transaction)
//				val backupAccount = account.copy()
//
//				account.balance += deposit
//				val accToSave = UpdateAccount(accountRepository, account)
//				accToSave.reverseCommand = UndoUpdateAccount(accountRepository, backupAccount)
//
//				twoPhaseScheduler.writeLock(transaction, account)
//				transactionManagerCommands.addCommands(transaction, accToSave)
//			}
//			transactionManagerCommands.addCommands(transaction, getUser)
//			transactionManagerCommands.addCommands(transaction, History(historyRepository, transaction))
//			transactionManagerCommands.commit(transaction)
//
//			twoPhaseScheduler.releaseLocks(transaction)
//			transaction.status = TransactionStatus.COMMIT
//		} catch (abortException: AbortException) {
//			abort(abortException.transaction)
//		}
//		when {
//			transaction.status == TransactionStatus.COMMIT -> return DepositStatus(deposit, user)
//			transaction.status == TransactionStatus.ABORT -> return DepositStatus(0.0, "Could not be deposited")
//			else -> throw RuntimeException("When statement must have an else branch")
//		}
//	}
	
	
	private fun abort(transaction: Transaction) {
		transactionManagerCommands.rollback(transaction)
		twoPhaseScheduler.releaseLocks(transaction)
		transaction.status = TransactionStatus.ABORT
	}
}