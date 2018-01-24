package hello

import hello.data.account.Account
import org.springframework.stereotype.Service
import java.util.*

@Service
class SessionManagementService {
	private val map = mutableMapOf<String, Account>()
	
	fun isUserLoggedIn(token: String): Boolean {
		return map.contains(token)
	}
	
	fun loginUser(account: Account): String {
		val token = UUID.randomUUID().toString()
		map[token] = account
		return token
	}
}