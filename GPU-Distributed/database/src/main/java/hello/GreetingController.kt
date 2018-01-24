package hello

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.util.concurrent.atomic.AtomicLong


@RestController
class GreetingController(val accountManagementService: AccountManagementService) {
	
	
	@GetMapping("/deposit")
	fun deposit(@RequestParam(value = "deposit", defaultValue = "0") deposit: Double): ResponseEntity<String> {
		if (deposit <= 0) return ResponseEntity.ok("$deposit was added to your account")

		val response = accountManagementService.deposit(deposit,"tudor")
		return ResponseEntity.ok(response.toString())
	}
}