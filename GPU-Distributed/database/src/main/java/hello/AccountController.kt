package hello

import hello.data.Deposit
import hello.data.DepositStatus
import hello.data.account.Account
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@RestController
class AccountController(val accountManagerService: AccountManagerService) {
	
	@PostMapping("/deposit")
	fun deposit(@RequestBody deposit: Deposit): DepositStatus {
		if (deposit.deposit <= 0) return DepositStatus(0.0, "$deposit was added to your user")
		
		return accountManagerService.deposit(deposit.deposit, deposit.user)
	}
	
	@PostMapping("/createAccount")
	fun deposit(@RequestBody account: Account): ResponseEntity<Account> {
		if (account.name.isBlank() or account.password.isBlank()) return ResponseEntity.badRequest().body(account)
		
		val status = accountManagerService.createAccount(account)
		if (status == null)
			return ResponseEntity.unprocessableEntity().body(account)
		else
			return ResponseEntity.ok(status)
	}
}