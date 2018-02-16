package hello.business

import org.springframework.stereotype.Service
import java.util.*

@Service
class WaitForGraphTable {
	private val waitForList = Collections.synchronizedList(mutableListOf<WaitFor>())
	
	operator fun plusAssign(waitFor: WaitFor) {
		waitForList += waitFor
	}
	
	
}