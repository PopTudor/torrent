package hello

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.util.concurrent.atomic.AtomicLong


@RestController
class GreetingController(val accountManagementService: AccountManagementService) {
	
	@GetMapping("/deposit")
	fun greeting(@RequestParam(value = "deposit", defaultValue = "0") deposit: Int): Greeting {
		return accountManagementService.deposit(deposit)
	}
}