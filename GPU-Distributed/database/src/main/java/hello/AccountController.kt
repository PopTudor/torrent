package hello

import hello.data.account.Account
import hello.data.account.Deposit
import hello.data.account.DepositStatus
import hello.data.account.Withdraw
import hello.data.order.Order
import hello.data.order.OrderStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@RestController
class AccountController(val accountManagerService: AccountManagerService) {
	
	@PostMapping("/deposit")
	fun deposit(@RequestBody deposit: Deposit): DepositStatus? {
		if (deposit.deposit <= 0) return DepositStatus(0.0, "$deposit was added to your user")
		
		return accountManagerService.deposit(deposit.deposit, deposit.user)
	}
	
	@PostMapping("/withdraw")
	fun withdraw(@RequestBody withdraw: Withdraw): DepositStatus? {
		if (withdraw.amount <= 0) return DepositStatus(0.0, "could not withdraw: $withdraw")
		
		return accountManagerService.withdraw(withdraw.amount, withdraw.user)
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
	
	@PostMapping("/createSellOrder")
	fun createOrder(@RequestBody order: Order): ResponseEntity<OrderStatus> {
		val status = accountManagerService.createSellOrder(order)
		if (status == null)
			return ResponseEntity.unprocessableEntity().body(OrderStatus("Could not create order"))
		else
			return ResponseEntity.ok(status)
	}
}