package hello

import hello.data.account.AccountRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
class GreetingController(val accountManagementService: AccountManagementService, val accountRepository: AccountRepository) {
	
	
	@GetMapping("/deposit")
	fun deposit(
			@RequestParam(value = "deposit", defaultValue = "0") deposit: Double,
			@RequestParam(value = "user") user: String
	): ResponseEntity<String> {
		if (deposit <= 0) return ResponseEntity.ok("$deposit was added to your account")
		
		val response = accountManagementService.deposit(deposit, user)
		return ResponseEntity.ok(response.toString())
	}
}