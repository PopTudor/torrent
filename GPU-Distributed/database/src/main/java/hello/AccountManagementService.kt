package hello

import org.springframework.stereotype.Service

@Service
class AccountManagementService {
	fun deposit(deposit: Int): Greeting {
		return Greeting(deposit.toLong(), deposit.toString())
	}
	
}