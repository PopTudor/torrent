package hello

import hello.business.Transaction
import hello.business.TransactionStatus
import hello.business.TwoPhaseScheduler
import hello.business.command.*
import hello.data.account.Account
import hello.data.account.AccountRepository
import hello.data.account.AccountStatus
import hello.data.account.DepositStatus
import hello.data.history.HistoryRepository
import hello.data.order.Order
import hello.data.order.OrderStatus
import hello.data.order.OrderType
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
			transactionManagerCommands.addCommands(transaction, HistoryCommand(historyRepository, transaction, account))
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
	
	fun deleteAccount(account: Account): Account? {
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		try {
			val createUser = DeleteAccount(accountRepository, account)
			
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
			val dbUser = GetAccount(accountRepository, user) { account ->
				if (account == null) throw AbortException(transaction)
				val backupAccount = account.copy()
				
				account.balance += deposit
				val depositAcc = UpdateAccount(accountRepository, account)
				depositAcc.reverseCommand = UndoUpdateAccount(accountRepository, backupAccount)
				
				twoPhaseScheduler.writeLock(transaction, account)
				transactionManagerCommands.addCommands(transaction, depositAcc)
				transactionManagerCommands.addCommands(transaction, HistoryCommand(historyRepository, transaction, depositAcc))
				transactionManagerCommands.commit(transaction)
			}
			transactionManagerCommands.addCommands(transaction, dbUser)
			transactionManagerCommands.addCommands(transaction, HistoryCommand(historyRepository, transaction, dbUser))
			transactionManagerCommands.commit(transaction)
			
			twoPhaseScheduler.releaseLocks(transaction)
			transaction.status = TransactionStatus.COMMIT
		} catch (abortException: AbortException) {
			abort(abortException.transaction)
		}
		when {
			transaction.status == TransactionStatus.COMMIT -> return DepositStatus(deposit, user)
			transaction.status == TransactionStatus.ABORT -> return DepositStatus(deposit, "Could not be deposited")
			else -> throw RuntimeException("When statement must have an else branch")
		}
	}
	
	fun withdraw(deposit: Double, user: String): DepositStatus {
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		try {
			if (deposit < 0 || user.isBlank()) return DepositStatus(deposit, "Could not be withdrawn from $user")
			
			twoPhaseScheduler.readLock(transaction, user)
			val getUser = GetAccount(accountRepository, user) { account ->
				if (account == null) throw AbortException(transaction)
				val backupAccount = account.copy()
				
				account.balance -= deposit
				val withdrawnAcc = UpdateAccount(accountRepository, account)
				withdrawnAcc.reverseCommand = UndoUpdateAccount(accountRepository, backupAccount)
				
				twoPhaseScheduler.writeLock(transaction, account)
				transactionManagerCommands.addCommands(transaction, withdrawnAcc)
				transactionManagerCommands.addCommands(transaction, HistoryCommand(historyRepository, transaction, withdrawnAcc))
				transactionManagerCommands.commit(transaction)
			}
			transactionManagerCommands.addCommands(transaction, getUser)
			transactionManagerCommands.addCommands(transaction, HistoryCommand(historyRepository, transaction, getUser))
			transactionManagerCommands.commit(transaction)
			
			twoPhaseScheduler.releaseLocks(transaction)
			transaction.status = TransactionStatus.COMMIT
		} catch (abortException: AbortException) {
			abort(abortException.transaction)
		}
		when {
			transaction.status == TransactionStatus.COMMIT -> return DepositStatus(deposit, user)
			transaction.status == TransactionStatus.ABORT -> return DepositStatus(0.0, "Could not be withdrawn")
			else -> throw RuntimeException("When statement must have an else branch")
		}
	}
	
	fun createOrder(account: Account, amount: Double): OrderStatus {
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		val order = Order(amount, OrderType.BUY, account)
		try {
			if (amount < 0) return OrderStatus("Could not buy $amount")
			
			twoPhaseScheduler.readLock(transaction, account)
			val getUser = GetAccount(accountRepository, account.name) { account ->
				if (account == null) throw AbortException(transaction)
				val backupAccount = account.copy()
				
				account.balance -= amount
				val withdrawnAcc = UpdateAccount(accountRepository, account)
				withdrawnAcc.reverseCommand = UndoUpdateAccount(accountRepository, backupAccount)
				
				twoPhaseScheduler.writeLock(transaction, account)
				transactionManagerCommands.addCommands(transaction, withdrawnAcc)
				transactionManagerCommands.addCommands(transaction, HistoryCommand(historyRepository, transaction, withdrawnAcc))
				transactionManagerCommands.commit(transaction)
			}
			transactionManagerCommands.addCommands(transaction, getUser)
			transactionManagerCommands.addCommands(transaction, HistoryCommand(historyRepository, transaction, getUser))
			transactionManagerCommands.commit(transaction)
			
			twoPhaseScheduler.releaseLocks(transaction)
			transaction.status = TransactionStatus.COMMIT
		} catch (abortException: AbortException) {
			abort(abortException.transaction)
		}
		when {
			transaction.status == TransactionStatus.COMMIT -> return OrderStatus("$order created")
			transaction.status == TransactionStatus.ABORT -> return OrderStatus("could not create order")
			else -> throw RuntimeException("When statement must have an else branch")
		}
	}
	
	fun changeAccountPassword(account: Account, password: String): AccountStatus {
		if (password.isBlank()) return AccountStatus("$account could not be changed")
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		try {
			twoPhaseScheduler.readLock(transaction, account)
			val dbUser = GetAccount(accountRepository, account.name) { account1 ->
				if (account1 == null) throw AbortException(transaction)
				val backupAccount = account1.copy()
				
				account1.password = password
				val accToSave = UpdateAccount(accountRepository, account1)
				accToSave.reverseCommand = UndoUpdateAccount(accountRepository, backupAccount)
				
				twoPhaseScheduler.writeLock(transaction, account1)
				transactionManagerCommands.addCommands(transaction, accToSave)
				transactionManagerCommands.addCommands(transaction, HistoryCommand(historyRepository, transaction, account1))
				transactionManagerCommands.commit(transaction)
			}
			transactionManagerCommands.addCommands(transaction, dbUser)
			transactionManagerCommands.addCommands(transaction, HistoryCommand(historyRepository, transaction, dbUser))
			transactionManagerCommands.commit(transaction)
			
			twoPhaseScheduler.releaseLocks(transaction)
			transaction.status = TransactionStatus.COMMIT
		} catch (abortException: AbortException) {
			abort(abortException.transaction)
		}
		when {
			transaction.status == TransactionStatus.COMMIT -> return AccountStatus("Changed password for $account")
			transaction.status == TransactionStatus.ABORT -> return AccountStatus("$account could not be changed")
			else -> throw RuntimeException("When statement must have an else branch")
		}
	}
	
	fun transfer(sender: Account, receiver: Account, amount: Double): AccountStatus {
		if (amount <= 0.0001) return AccountStatus("could transfer $amount from $sender to $receiver")
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		try {
			twoPhaseScheduler.readLock(transaction, sender)
			twoPhaseScheduler.readLock(transaction, receiver)
			val senderDb = GetAccount(accountRepository, sender.name) { account1 ->
				if (account1 == null) throw AbortException(transaction)
				val backupAccount = account1.copy()
				
				account1.balance -= amount
				val senderDbResult = UpdateAccount(accountRepository, account1)
				senderDbResult.reverseCommand = UndoUpdateAccount(accountRepository, backupAccount)
				
				val receiverDb = GetAccount(accountRepository, receiver.name) { account2 ->
					if (account2 == null) throw AbortException(transaction)
					val backupAccount2 = account2.copy()
					
					account2.balance += amount
					val receiverDbResult = UpdateAccount(accountRepository, account2)
					receiverDbResult.reverseCommand = UndoUpdateAccount(accountRepository, backupAccount2)
					
					twoPhaseScheduler.writeLock(transaction, account1)
					twoPhaseScheduler.writeLock(transaction, account2)
					transactionManagerCommands.addCommands(transaction, senderDbResult)
					transactionManagerCommands.addCommands(transaction, HistoryCommand(historyRepository, transaction, account1))
					transactionManagerCommands.addCommands(transaction, HistoryCommand(historyRepository, transaction, account2))
					transactionManagerCommands.commit(transaction)
				}
			}
			transactionManagerCommands.addCommands(transaction, senderDb)
			transactionManagerCommands.addCommands(transaction, HistoryCommand(historyRepository, transaction, senderDb))
			transactionManagerCommands.commit(transaction)
			
			twoPhaseScheduler.releaseLocks(transaction)
			transaction.status = TransactionStatus.COMMIT
		} catch (abortException: AbortException) {
			abort(abortException.transaction)
		}
		when {
			transaction.status == TransactionStatus.COMMIT -> return AccountStatus("Sent $amount from $sender to $receiver")
			transaction.status == TransactionStatus.ABORT -> return AccountStatus("Could not send $amount from $sender to $receiver")
			else -> throw RuntimeException("When statement must have an else branch")
		}
	}
	
	private fun abort(transaction: Transaction) {
		transactionManagerCommands.rollback(transaction)
		twoPhaseScheduler.releaseLocks(transaction)
		transaction.status = TransactionStatus.ABORT
	}
}