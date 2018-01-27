package com.company.proto.handlers

import com.company.proto.Constants
import com.company.proto.toMD5Hash
import com.company.proto.torrent.Torrent
import com.google.protobuf.ByteString


class ChunkHandler(private val storage: Map<Torrent.FileInfo, ByteString>)
//		filenameHashes = new HashMap<>(storage.size());
//		storage.forEach((key, value) -> filenameHashes.put(UtilsKt.toMD5Hash(value.toByteArray()), key.getFilename()));
	: Handler {
//	private val filenameHashes: Map<ByteString, String>? = null
	
	override fun handle(message: Torrent.Message): Torrent.Message {
		val hash = message.chunkRequest.fileHash
		val chunkIndex = message.chunkRequest.chunkIndex
		
		if (hash.size() != 16 || chunkIndex < 0) return messageError()
		// if don't have file, return error
		val fileInfo = storage.keys.find { it.hash == hash } ?: return unableToComplete()
		
		return chunkResponse(storage[fileInfo] ?: ByteString.EMPTY, fileInfo.getChunks(chunkIndex))
	}
	
	private fun chunkResponse(bytes: ByteString, chunkInfo: Torrent.ChunkInfo): Torrent.Message {
		val dataArray = chunkAtIntex(bytes.toByteArray(), Constants.CHUNK_SIZE, chunkInfo)
		val data = ByteString.copyFrom(dataArray)
		
		if (data.toMD5Hash() != chunkInfo.hash)
			throw RuntimeException("Requested chunk index hash does not match with retrieved data hash")
		
		val build = Torrent.ChunkResponse
				.newBuilder()
				.setStatus(Torrent.Status.SUCCESS)
				.setData(data)
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.CHUNK_RESPONSE)
				.setChunkResponse(build)
				.build()
	}
	
	private fun messageError(): Torrent.Message {
		val build = Torrent.ChunkResponse
				.newBuilder()
				.setStatus(Torrent.Status.MESSAGE_ERROR)
				.setErrorMessage("MESSAGE_ERROR")
				.setData(ByteString.EMPTY)
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.CHUNK_RESPONSE)
				.setChunkResponse(build)
				.build()
	}
	
	private fun unableToComplete(): Torrent.Message {
		val build = Torrent.ChunkResponse.newBuilder()
				.setStatus(Torrent.Status.UNABLE_TO_COMPLETE)
				.setErrorMessage("UNABLE_TO_COMPLETE")
				.setData(ByteString.EMPTY)
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.CHUNK_RESPONSE)
				.setChunkResponse(build)
				.build()
	}
}