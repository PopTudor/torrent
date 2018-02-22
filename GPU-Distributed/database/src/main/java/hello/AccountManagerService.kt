package hello

import hello.business.Transaction
import hello.business.TransactionStatus
import hello.business.TwoPhaseScheduler
import hello.business.command.*
import hello.data.account.Account
import hello.data.account.AccountRepository
import hello.data.account.DepositStatus
import hello.data.history.HistoryRepository
import hello.data.order.Order
import hello.data.order.OrderStatus
import hello.data.order.OrdersRepository
import org.springframework.stereotype.Service

@Service
class AccountManagerService(
		private val accountRepository: AccountRepository,
		private val historyRepository: HistoryRepository,
		private val orderRepository: OrdersRepository,
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
			transactionManagerCommands.addCommands(transaction, CreateHistoryCommand(historyRepository, transaction, account))
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
			else -> return null
		}
	}
	
	fun deposit(deposit: Double, user: String): DepositStatus? {
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		
		try {
			twoPhaseScheduler.readLock(transaction, user)
			val dbUser = GetAccountCommand(accountRepository, user)
			transactionManagerCommands.addCommands(transaction, dbUser)
			transactionManagerCommands.addCommands(transaction, CreateHistoryCommand(historyRepository, transaction, dbUser))
			transactionManagerCommands.commit(transaction)
			
			val dbAccount = dbUser.resultAccount ?: throw AbortException(transaction)
			val backupAccount = dbAccount.copy()
			dbAccount.balance += deposit
			
			val depositAcc = UpdateAccount(accountRepository, dbAccount)
			depositAcc.reverseCommand = UndoUpdateAccount(accountRepository, backupAccount)
			
			twoPhaseScheduler.writeLock(transaction, dbAccount)
			transactionManagerCommands.addCommands(transaction, depositAcc)
			transactionManagerCommands.addCommands(transaction, CreateHistoryCommand(historyRepository, transaction, depositAcc))
			transactionManagerCommands.commit(transaction)
			
			twoPhaseScheduler.releaseLocks(transaction)
			transaction.status = TransactionStatus.COMMIT
		} catch (abortException: AbortException) {
			abort(abortException.transaction)
		}
		when {
			transaction.status == TransactionStatus.COMMIT -> return DepositStatus(deposit, user)
			transaction.status == TransactionStatus.ABORT -> return DepositStatus(deposit, "Could not be deposited")
			else -> return null
		}
	}
	
	fun withdraw(withdraw: Double, user: String): DepositStatus? {
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		try {
			if (withdraw < 0 || user.isBlank()) return DepositStatus(withdraw, "Could not be withdrawn from $user")
			
			twoPhaseScheduler.readLock(transaction, user)
			val getAccountCommand = GetAccountCommand(accountRepository, user)
			transactionManagerCommands.addCommands(transaction, getAccountCommand)
			transactionManagerCommands.addCommands(transaction, CreateHistoryCommand(historyRepository, transaction, getAccountCommand))
			transactionManagerCommands.commit(transaction)
			
			val dbAccount = getAccountCommand.resultAccount ?: throw AbortException(transaction)
			val backupAccount = dbAccount.copy()
			
			dbAccount.balance -= withdraw
			
			val withdrawnAcc = UpdateAccount(accountRepository, dbAccount)
			withdrawnAcc.reverseCommand = UndoUpdateAccount(accountRepository, backupAccount)
			
			twoPhaseScheduler.writeLock(transaction, dbAccount)
			transactionManagerCommands.addCommands(transaction, withdrawnAcc)
			transactionManagerCommands.addCommands(transaction, CreateHistoryCommand(historyRepository, transaction, withdrawnAcc))
			transactionManagerCommands.commit(transaction)
			
			if (dbAccount.balance < 0) throw AbortException(transaction)
			//rollback when negative
			val overwithdrawn = getAccountCommand.resultAccount ?: throw AbortException(transaction)
			if (overwithdrawn.balance < 0) {
				throw AbortException(transaction)
			}
			
			twoPhaseScheduler.releaseLocks(transaction)
			transaction.status = TransactionStatus.COMMIT
		} catch (abortException: AbortException) {
			abort(abortException.transaction)
		}
		when {
			transaction.status == TransactionStatus.COMMIT -> return DepositStatus(withdraw, user)
			transaction.status == TransactionStatus.ABORT -> return DepositStatus(0.0, "Could not be withdrawn")
			else -> return null
		}
	}
	
	fun createSellOrder(order: Order): OrderStatus? {
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		try {
			if (order.amount < 0) return OrderStatus("Could not create $order")
			
			twoPhaseScheduler.readLock(transaction, order.account)
			val getAccountCommand = GetAccountCommand(accountRepository, order.account)
			transactionManagerCommands.addCommands(transaction, getAccountCommand)
			transactionManagerCommands.addCommands(transaction, CreateHistoryCommand(historyRepository, transaction, getAccountCommand))
			transactionManagerCommands.commit(transaction)
			
			val dbAccount = getAccountCommand.resultAccount ?: throw AbortException(transaction)
			val backupAccount = dbAccount.copy()
			
			dbAccount.balance -= order.amount
			
			val withdrawnAcc = UpdateAccount(accountRepository, dbAccount)
			withdrawnAcc.reverseCommand = UndoUpdateAccount(accountRepository, backupAccount)
			
			order.account = dbAccount.name
			val createOrder = CreateOrderCommand(orderRepository, order)
			createOrder.reverseCommand = DeleteOrder(orderRepository, order)
			
			twoPhaseScheduler.writeLock(transaction, dbAccount)
			transactionManagerCommands.addCommands(transaction, withdrawnAcc)
			transactionManagerCommands.addCommands(transaction, CreateHistoryCommand(historyRepository, transaction, withdrawnAcc))
			transactionManagerCommands.addCommands(transaction, createOrder)
			transactionManagerCommands.addCommands(transaction, CreateHistoryCommand(historyRepository, transaction, order))
			transactionManagerCommands.commit(transaction)
			
			if (dbAccount.balance < 0) throw AbortException(transaction)
			//rollback when negative
			val overwithdrawn = getAccountCommand.resultAccount ?: throw AbortException(transaction)
			if (overwithdrawn.balance < 0) {
				throw AbortException(transaction)
			}
			
			twoPhaseScheduler.releaseLocks(transaction)
			transaction.status = TransactionStatus.COMMIT
		} catch (abortException: AbortException) {
			abort(abortException.transaction)
		}
		when {
			transaction.status == TransactionStatus.COMMIT -> return OrderStatus("$order created")
			transaction.status == TransactionStatus.ABORT -> return OrderStatus("could not create order")
			else -> return null
		}
	}
	
	private fun abort(transaction: Transaction) {
		transactionManagerCommands.rollback(transaction)
		twoPhaseScheduler.releaseLocks(transaction)
		transaction.status = TransactionStatus.ABORT
	}
}