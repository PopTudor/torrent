package com.company.proto.handlers

import com.company.proto.*
import com.company.proto.torrent.Torrent
import com.google.protobuf.ByteString

class LocalSearchHandler(private val storage: Map<Torrent.FileInfo, ByteString>) : Handler {
	
	override fun handle(message: Torrent.Message): Torrent.Message {
		val regex = message.localSearchRequest.regex
		
		if (!regex.isValidRegex()) return messageError()
		
		val fileInfo = storage.keys.find { regex.toRegex().matches(it.filename) }
				?: return successNoResult()
		return successWithResult(fileInfo)
	}
	
	private fun successWithResult(fileInfo: Torrent.FileInfo): Torrent.Message {
		val searchResponse = Torrent.LocalSearchResponse.newBuilder()
				.setStatus(Torrent.Status.SUCCESS)
				.addFileInfo(fileInfo)
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.LOCAL_SEARCH_RESPONSE)
				.setLocalSearchResponse(searchResponse)
				.build()
	}
	
	private fun successNoResult(): Torrent.Message {
		val searchResponse = Torrent.LocalSearchResponse
				.newBuilder()
				.setStatus(Torrent.Status.SUCCESS)
				.setErrorMessage(Torrent.Status.SUCCESS.toString())
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.LOCAL_SEARCH_RESPONSE)
				.setLocalSearchResponse(searchResponse)
				.build()
	}
	
	private fun messageError(): Torrent.Message {
		val searchResponse = Torrent.LocalSearchResponse
				.newBuilder()
				.setStatus(Torrent.Status.MESSAGE_ERROR)
				.setErrorMessage("MESSAGE_ERROR")
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.LOCAL_SEARCH_RESPONSE)
				.setLocalSearchResponse(searchResponse)
				.build()
	}
}

