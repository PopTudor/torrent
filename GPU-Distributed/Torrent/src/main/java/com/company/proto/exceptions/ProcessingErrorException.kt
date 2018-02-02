package com.company.proto.exceptions

class ProcessingErrorException(message: String = "Processing error") : RuntimeException(message)
class MessageErrorException(message: String = "Message error") : RuntimeException()
class UnableToCompleteException(message: String = "Message error") : RuntimeException()
