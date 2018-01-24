package hello.business

import org.springframework.stereotype.Service

@Service
class TwoPhaseLock(val locks: Locks, val transactions: Transactions) {

}