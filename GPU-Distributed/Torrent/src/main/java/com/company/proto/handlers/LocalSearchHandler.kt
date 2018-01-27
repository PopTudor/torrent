package com.company.proto.handlers

import com.company.proto.*
import com.company.proto.torrent.Torrent
import com.google.protobuf.ByteString

class LocalSearchHandler(private val storage: Map<Torrent.FileInfo, ByteString>) : Handler {
	
	override fun handle(message: Torrent.Message): Torrent.Message {
		val regex = message.localSearchRequest.regex
		
		if (!regex.isValidRegex()) return messageError(regex);
		
		val resultFilename = storage.keys.stream()
				.map { t -> t.filename }
				.filter { filename -> filename.matches(regex.toRegex()) }
				.findAny()
				.orElse("")
		return if (resultFilename.isEmpty())
			successNoResult(regex)
		else
			successWithResult(regex)
	}
	
	private fun successWithResult(resultFilename: String): Torrent.Message {
//		val fileData = storage[resultFilename]?.toByteArray()
		val fileInfo = Torrent.FileInfo.newBuilder()
				.setFilename(resultFilename)
				//				.setHash(Utils.toMD5Hash(fileData))
				//				.setSize(fileData.length)
				//				.addAllChunks(Utils.toList(fileData))
				.build()
		val searchResponse = Torrent.LocalSearchResponse
				.newBuilder()
				.setStatus(Torrent.Status.SUCCESS)
				.setErrorMessage("SUCCESS")
				.addFileInfo(fileInfo)
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.LOCAL_SEARCH_RESPONSE)
				.setLocalSearchResponse(searchResponse)
				.build()
	}
	
	private fun successNoResult(filename: String): Torrent.Message {
		val fileInfo = Torrent.FileInfo.newBuilder()
				.setFilename(filename)
				.build()
		val searchResponse = Torrent.LocalSearchResponse
				.newBuilder()
				.setStatus(Torrent.Status.SUCCESS)
				.setErrorMessage("SUCCESS")
				.addFileInfo(fileInfo)
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.LOCAL_SEARCH_RESPONSE)
				.setLocalSearchResponse(searchResponse)
				.build()
	}
	
	private fun messageError(filename: String): Torrent.Message {
		val fileInfo = Torrent.FileInfo.newBuilder()
				.setFilename(filename)
				.build()
		val searchResponse = Torrent.LocalSearchResponse
				.newBuilder()
				.setStatus(Torrent.Status.MESSAGE_ERROR)
				.setErrorMessage("MESSAGE_ERROR")
				.addFileInfo(fileInfo)
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.LOCAL_SEARCH_RESPONSE)
				.setLocalSearchResponse(searchResponse)
				.build()
	}
}
