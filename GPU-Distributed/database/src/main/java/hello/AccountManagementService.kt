package hello

import hello.data.account.Account
import hello.data.account.AccountRepository
import hello.data.DepositStatus
import hello.data.payment.Payment
import hello.data.payment.PaymentRepository
import org.springframework.stereotype.Service
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

@Service
class AccountManagementService(val accountRepository: AccountRepository,
							   val paymentRepository: PaymentRepository) {
	val lock: Lock = ReentrantLock()
	
	init {
		accountRepository.save(Account("tudor", "parola"))
		accountRepository.save(Account("tudor2", "parola3"))
		accountRepository.save(Account("tudor3", "parola2"))
		accountRepository.save(Account("tudor1", "parola1"))
	}
	
	fun deposit(deposit: Double, user: String): DepositStatus {
		paymentRepository.save(Payment(1,2))
		val account = retrieveUser(user) ?: return DepositStatus(0.0, "User not found")
		updateAccount(account, deposit)
		saveAccount(account)
		updateAccountHistory(account)
		updateLog(account)
		
		
		return DepositStatus(deposit, account.toString())
	}
	
	private fun lock(function: () -> Unit) {
		lock.lock()
		function()
		lock.unlock()
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
	
	private fun updateAccount(account: Account, deposit: Double) {
		account.balance += deposit
	}
	
	private fun retrieveUser(user: String): Account? {
		return accountRepository.findByName(user).first()
	}
}