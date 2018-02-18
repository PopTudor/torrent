package hello

import hello.business.Transaction

class AbortException(
		val transaction: Transaction
) : RuntimeException() {
}