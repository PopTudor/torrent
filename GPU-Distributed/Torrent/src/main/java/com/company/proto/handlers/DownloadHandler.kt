package com.company.proto.handlers

import com.company.proto.toMD5Hash
import com.company.proto.torrent.Torrent
import com.google.protobuf.ByteString

class DownloadHandler(private val storage: Map<Torrent.FileInfo, ByteString>) : Handler {
	override fun handle(message: Torrent.Message): Torrent.Message {
		val filehash = message.downloadRequest.fileHash
		
		if (filehash.size() != 16) return errorMessage()

		val (fileKey, filedata) = storage.entries.find { (fileInfo, _) -> fileInfo.hash == filehash }
				?: return unableToComplete()
		
		val downloadResponse = Torrent.DownloadResponse.newBuilder()
				.setStatus(Torrent.Status.SUCCESS)
				.setErrorMessage(Torrent.Status.SUCCESS.toString())
				.setData(filedata)
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.DOWNLOAD_RESPONSE)
				.setDownloadResponse(downloadResponse)
				.build()
	}
	
	private fun unableToComplete(): Torrent.Message {
		val build = Torrent.DownloadResponse.newBuilder()
				.setStatus(Torrent.Status.UNABLE_TO_COMPLETE)
				.setErrorMessage("ce plms")
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.DOWNLOAD_RESPONSE)
				.setDownloadResponse(build)
				.build()
	}
	
	private fun errorMessage(): Torrent.Message {
		val build = Torrent.DownloadResponse.newBuilder()
				.setStatus(Torrent.Status.MESSAGE_ERROR)
				.setErrorMessage(Torrent.Status.MESSAGE_ERROR.toString())
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.DOWNLOAD_RESPONSE)
				.setDownloadResponse(build)
				.build()
		
	}
	
}
