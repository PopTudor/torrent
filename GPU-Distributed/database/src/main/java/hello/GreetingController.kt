package hello

import hello.data.Deposit
import hello.data.DepositStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@RestController
class GreetingController(private val accountManagementService: AccountManagementService) {
	
	@PostMapping("/deposit")
	fun deposit(@RequestBody deposit: Deposit): DepositStatus {
		if (deposit.deposit <= 0) return DepositStatus(0.0, "$deposit was added to your user")
		
		return accountManagementService.deposit(deposit.deposit, deposit.user)
	}
}