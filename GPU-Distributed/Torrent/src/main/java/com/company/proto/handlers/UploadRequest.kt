package com.company.proto.handlers

import com.company.proto.Constants
import com.company.proto.*
import com.company.proto.torrent.Torrent
import com.google.common.base.Strings
import com.google.protobuf.ByteString

import java.util.Arrays

class UploadRequest(internal var storage: MutableMap<Torrent.FileInfo, ByteString>) : Handler {
	
	override fun handle(message: Torrent.Message): Torrent.Message {
		val filename = message.uploadRequest.filename
		val data = message.uploadRequest.data
		val dataBytes = data.toByteArray()
		val hash = dataBytes.toMD5Hash()
		
		val fileInfo = Torrent.FileInfo.newBuilder()
				.setHash(hash)
				.setSize(data.size())
				.setFilename(filename)
		
		if (filename.isNullOrBlank()) return errorFilenameEmptyResponse(fileInfo)
		
		var i = 0
		var j = 0
		while (i < data.size()) {
			val chunk = Arrays.copyOfRange(dataBytes, i, Math.min(data.size(), i + Constants.CHUNK_SIZE))
			
			val chunkInfo = Torrent.ChunkInfo
					.newBuilder()
					.setIndex(j)
					.setSize(chunk.size)
					.setHash(chunk.toMD5Hash())
					.build()
			
			fileInfo.addChunks(j, chunkInfo)
			i += Constants.CHUNK_SIZE
			j++
		}
		val info = fileInfo.build()
		
		for (it in storage.keys) {
			if (it.filename == filename)
				return successFileUploadResponse(info)
		}
		val fileInfo1 = Torrent.FileInfo.newBuilder()
				.setHash(hash)
				.setSize(data.size())
				.setFilename(filename)
				.addAllChunks(data.toList())
				.build()
		storage[fileInfo1] = data
		
		return successFileUploadResponse(info)
	}
	
	private fun errorFilenameEmptyResponse(fileInfo: Torrent.FileInfo.Builder): Torrent.Message {
		val uploadResponse = Torrent.UploadResponse
				.newBuilder()
				.setStatus(Torrent.Status.SUCCESS)
				.setErrorMessage("Filename must not be empty")
				.setFileInfo(fileInfo)
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.UPLOAD_RESPONSE)
				.setUploadResponse(uploadResponse)
				.build()
	}
	
	private fun successFileUploadResponse(fileInfo: Torrent.FileInfo): Torrent.Message {
		val uploadResponse = Torrent.UploadResponse
				.newBuilder()
				.setStatus(Torrent.Status.SUCCESS)
				.setErrorMessage("upload success")
				.setFileInfo(fileInfo)
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
