package hello

import hello.business.Transaction
import hello.business.TransactionHistory
import hello.business.TransactionStatus
import hello.business.TwoPhaseScheduler
import hello.data.DepositStatus
import hello.data.account.Account
import hello.data.account.AccountRepository
import hello.data.payment.PaymentRepository
import org.springframework.stereotype.Service

@Service
class AccountManagementService(
		val accountRepository: AccountRepository,
		val paymentRepository: PaymentRepository,
		val twoPhaseScheduler: TwoPhaseScheduler
) {
	init {
//		accountRepository.save(Account("tudor","parola",100.0))
	}
	
	fun deposit(deposit: Double, user: String): DepositStatus {
		val transaction = Transaction(status = TransactionStatus.ACTIVE)
		
		val readLock = twoPhaseScheduler.readLock(transaction, user)
		readLock.readLock()
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