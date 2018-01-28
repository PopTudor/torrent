package com.company.proto.handlers

import com.company.proto.hashToReadableMD5
import com.company.proto.torrent.Torrent
import com.google.protobuf.ByteString
import sun.security.krb5.KrbException.errorMessage

class DownloadHandler(private val storage: Map<Torrent.FileInfo, ByteString>) : Handler {
	override fun handle(message: Torrent.Message): Torrent.Message {
		val filehash = message.downloadRequest.fileHash
		println("Download: " + filehash.hashToReadableMD5())
		print("Local: ")
		storage.keys.forEach { print(it.hash.hashToReadableMD5()+" ") }
		if (filehash.size() != 16) return errorMessage(filehash)
		
		val fileKey = storage.keys.find { it.hash == filehash }
				?: return unableToComplete(filehash)
		
		return successFileFound(fileKey)
	}
	
	private fun successFileFound(fileKey: Torrent.FileInfo): Torrent.Message {
		println("Actual: $fileKey")
		val downloadResponse = Torrent.DownloadResponse.newBuilder()
				.setStatus(Torrent.Status.SUCCESS)
				.setErrorMessage(Torrent.Status.SUCCESS.toString())
				.setData(storage[fileKey])
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.DOWNLOAD_RESPONSE)
				.setDownloadResponse(downloadResponse)
				.build()
	}
	
	private fun unableToComplete(filehash: ByteString): Torrent.Message {
		println("Download: unable to complete $filehash not found")
		val build = Torrent.DownloadResponse.newBuilder()
				.setStatus(Torrent.Status.UNABLE_TO_COMPLETE)
				.setErrorMessage("File with hash: $filehash not found")
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.DOWNLOAD_RESPONSE)
				.setDownloadResponse(build)
				.build()
	}
	
	private fun errorMessage(filehash: ByteString): Torrent.Message {
		val build = Torrent.DownloadResponse.newBuilder()
				.setStatus(Torrent.Status.MESSAGE_ERROR)
				.setErrorMessage("Filehash $filehash is not 16 Bytes")
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.DOWNLOAD_RESPONSE)
				.setDownloadResponse(build)
				.build()
		
	}
	
}
