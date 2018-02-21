package hello.business.command

import hello.data.account.Account
import hello.data.account.AccountRepository
import javax.transaction.RollbackException

class CreateAccount(
		val accountRepository: AccountRepository,
		val account: Account
) : Command {
	
	override val reverseCommand: Command?
		get() = DeleteAccount(accountRepository, account)
	
	override fun execute() {
		if (account.balance.toInt() != 0) throw RollbackException("Balance must be 0 for new accounts")
		accountRepository.save(account)
	}
}