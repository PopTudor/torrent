package hello

import hello.data.DepositStatus
import hello.data.account.Account
import hello.data.account.AccountRepository
import hello.data.payment.PaymentRepository
import org.springframework.stereotype.Service

@Service
class AccountManagementService(val accountRepository: AccountRepository,
							   val paymentRepository: PaymentRepository) {
	fun deposit(deposit: Double, user: String): DepositStatus {
		val account = retrieveUser(user) ?: return DepositStatus(0.0, "User not found")
		updateAccount(account, deposit)
		saveAccount(account)
		updateAccountHistory(account)
		updateLog(account)
		
		
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
	private fun updateAccount(account: Account, deposit: Double) {
		account.balance += deposit
	}
	
	private fun retrieveUser(user: String): Account? {
		return accountRepository.findByName(user).first()
	}
}