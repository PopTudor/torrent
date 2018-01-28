package com.company.proto.handlers

import com.company.proto.*
import com.company.proto.torrent.Torrent
import com.google.protobuf.ByteString

class LocalSearchHandler(private val storage: Map<Torrent.FileInfo, ByteString>) : Handler {
	
	override fun handle(message: Torrent.Message): Torrent.Message {
		val regex = message.localSearchRequest.regex
		
		if (!regex.isValidRegex()) return messageError(regex)
		
		val filesInfo = storage.keys.filter { regex.toRegex().matches(it.filename) }
		println("Files found $filesInfo")
		if (filesInfo.isEmpty()) return successNoResult()
		
		return successWithResult(filesInfo)
	}
	
	private fun successWithResult(fileInfo: List<Torrent.FileInfo>): Torrent.Message {
		val localSearchResponse = Torrent.LocalSearchResponse.newBuilder()
				.setStatus(Torrent.Status.SUCCESS)
				.addAllFileInfo(fileInfo)
				.build()
		return Torrent.Message.newBuilder()
				.setLocalSearchResponse(localSearchResponse)
				.setType(Torrent.Message.Type.LOCAL_SEARCH_RESPONSE)
				.build()
	}
	
	private fun successNoResult(): Torrent.Message {
		val searchResponse = Torrent.LocalSearchResponse.newBuilder()
				.setStatus(Torrent.Status.SUCCESS)
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.LOCAL_SEARCH_RESPONSE)
				.setLocalSearchResponse(searchResponse)
				.build()
	}
	
	private fun messageError(regex: String): Torrent.Message {
		val searchResponse = Torrent.LocalSearchResponse.newBuilder()
				.setStatus(Torrent.Status.MESSAGE_ERROR)
				.setErrorMessage("Regex $regex is invalid")
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.LOCAL_SEARCH_RESPONSE)
				.setLocalSearchResponse(searchResponse)
				.build()
	}
}

