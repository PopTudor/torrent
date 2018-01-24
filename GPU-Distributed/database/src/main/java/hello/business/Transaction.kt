package hello.business

import java.util.*


data class Transaction(val id: Int = randInt(), val timestamp: Date = Date(), val status: TransactionStatus)

enum class TransactionStatus { ACTIVE, ABORT, COMMIT }

fun randInt() = (Math.random() * 100000).toInt()
