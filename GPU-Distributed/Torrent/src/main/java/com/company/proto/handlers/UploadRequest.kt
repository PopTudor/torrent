package com.company.proto.handlers

import com.company.proto.CHUNK_SIZE
import com.company.proto.hashToReadableMD5
import com.company.proto.toChunkedArray
import com.company.proto.toMD5Hash
import com.company.proto.torrent.Torrent
import com.google.protobuf.ByteString


class UploadRequest(
		private var storage: MutableMap<Torrent.FileInfo, ByteString>
) : Handler {
	
	override fun handle(message: Torrent.Message): Torrent.Message {
		val filename = message.uploadRequest.filename
		val data = message.uploadRequest.data
		
		if (filename.isNullOrBlank()) return errorFilenameEmptyResponse()
		
		val fileInfo = Torrent.FileInfo.newBuilder()
				.setHash(data.toMD5Hash())
				.setSize(data.size())
				.setFilename(filename)
				.addAllChunks(data.toChunkedArray(CHUNK_SIZE))
				.build()
		if (fileInfo in storage.keys) return successFileUploadResponse(fileInfo)
		
		storage[fileInfo] = data
		return successFileUploadResponse(fileInfo)
	}
	
	private fun successFileUploadResponse(fileInfo: Torrent.FileInfo): Torrent.Message {
		println("Upload file ${fileInfo.hash.hashToReadableMD5()}")
		val uploadResponse = Torrent.UploadResponse.newBuilder()
				.setStatus(Torrent.Status.SUCCESS)
				.setFileInfo(fileInfo)
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.UPLOAD_RESPONSE)
				.setUploadResponse(uploadResponse)
				.build()
	}
	
	private fun errorFilenameEmptyResponse(): Torrent.Message {
		val uploadResponse = Torrent.UploadResponse.newBuilder()
				.setStatus(Torrent.Status.MESSAGE_ERROR)
				.setErrorMessage("Filename is empty")
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.UPLOAD_RESPONSE)
				.setUploadResponse(uploadResponse)
				.build()
	}
	
	private fun errorProcessing(fileInfo: Torrent.FileInfo): Torrent.Message {
		val uploadResponse = Torrent.UploadResponse.newBuilder()
				.setStatus(Torrent.Status.PROCESSING_ERROR)
				.setErrorMessage("File could not be stored")
				.setFileInfo(fileInfo)
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.UPLOAD_RESPONSE)
				.setUploadResponse(uploadResponse)
				.build()
	}
	
}