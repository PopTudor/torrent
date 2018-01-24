package hello.business

import org.springframework.stereotype.Service

@Service
class Locks {
	private val locks = mutableListOf<Lock>()
}