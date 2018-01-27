package com.company.proto.handlers

import com.company.proto.Constants
import com.company.proto.toMD5Hash
import com.company.proto.torrent.Torrent
import com.google.protobuf.ByteString


class UploadRequest(private var storage: MutableMap<Torrent.FileInfo, ByteString>) : Handler {
	
	override fun handle(message: Torrent.Message): Torrent.Message {
		val filename = message.uploadRequest.filename
		val data = message.uploadRequest.data
		
		val fileInfo = Torrent.FileInfo.newBuilder()
				.setHash(data.toMD5Hash())
				.setSize(data.size())
				.setFilename(filename)
				.addAllChunks(chunkArray(data.toByteArray(), Constants.CHUNK_SIZE))
				.build()
		
		if (filename.isNullOrBlank()) return errorFilenameEmptyResponse(fileInfo)
		
		storage[fileInfo] = data
		return successFileUploadResponse(fileInfo)
	}
	
	private fun errorFilenameEmptyResponse(fileInfo: Torrent.FileInfo): Torrent.Message {
		val uploadResponse = Torrent.UploadResponse
				.newBuilder()
				.setStatus(Torrent.Status.MESSAGE_ERROR)
				.setErrorMessage("Filename must not be empty")
				.setFileInfo(fileInfo)
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.UPLOAD_RESPONSE)
				.setUploadResponse(uploadResponse)
				.build()
	}
	
	private fun successFileUploadResponse(fileInfo: Torrent.FileInfo): Torrent.Message {
		val uploadResponse = Torrent.UploadResponse.newBuilder()
				.setStatus(Torrent.Status.SUCCESS)
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

fun chunkArray(array: ByteArray, chunkSize: Int): List<Torrent.ChunkInfo> {
	val numOfChunks = Math.ceil(array.size.toDouble() / chunkSize).toInt()
	val chunksInfo = mutableListOf<Torrent.ChunkInfo>()
	var j = 0
	for (i in 0 until numOfChunks) {
		val start = i * chunkSize
		val length = Math.min(array.size - start, chunkSize)
		
		val temp = ByteArray(length)
		System.arraycopy(array, start, temp, 0, length)
		
		val chunkInfo = Torrent.ChunkInfo
				.newBuilder()
				.setIndex(j)
				.setSize(temp.size)
				.setHash(temp.toMD5Hash())
				.build()
		chunksInfo.add(chunkInfo)
		j++
	}
	return chunksInfo
}

fun chunkAtIntex(array: ByteArray, chunkSize: Int, chunkInfo: Torrent.ChunkInfo): ByteArray {
	val numOfChunks = Math.ceil(array.size.toDouble() / chunkSize).toInt()
	var j = 0
	for (i in 0 until numOfChunks) {
		val start = i * chunkSize
		val length = Math.min(array.size - start, chunkSize)
		
		val temp = ByteArray(length)
		System.arraycopy(array, start, temp, 0, length)
		
		val chunkInfotmp = Torrent.ChunkInfo
				.newBuilder()
				.setIndex(j)
				.setSize(temp.size)
				.setHash(temp.toMD5Hash())
				.build()
		if (chunkInfotmp == chunkInfo)
			return temp
		if (j == chunkInfo.index)
			break
		j++
	}
	return ByteArray(0)
}
