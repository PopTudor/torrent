package hello.business.command

import hello.data.account.Account
import hello.data.account.AccountRepository

class UpdateAccount(
		val accountRepository: AccountRepository,
		val account: Account
) : Command {
	override var reverseCommand: Command? = null
	
	override fun execute() {
		accountRepository.save(account)
	}
}