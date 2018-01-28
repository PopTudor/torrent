package com.company.proto.handlers

import com.company.proto.*
import com.company.proto.torrent.Torrent
import com.google.protobuf.ByteString


class ChunkHandler(private val storage: Map<Torrent.FileInfo, ByteString>)
	: Handler {
	
	override fun handle(message: Torrent.Message): Torrent.Message {
		val hash = message.chunkRequest.fileHash
		val chunkIndex = message.chunkRequest.chunkIndex
		
		if (hash.size() != 16 || chunkIndex < 0) return messageError()
		// if don't have file, return error
		val fileInfo = storage.keys.find { it.hash == hash } ?: return unableToComplete()
		
		val bytes = storage[fileInfo] ?: return processingError()
		return chunkResponse(bytes, fileInfo.getChunks(chunkIndex))
	}
	
	private fun chunkResponse(bytes: ByteString, chunkInfo: Torrent.ChunkInfo): Torrent.Message {
		val data = bytes.toChunkAt(chunkInfo) ?: return processingError()
		println("chunk response $chunkInfo $data")
		val build = Torrent.ChunkResponse.newBuilder()
				.setStatus(Torrent.Status.SUCCESS)
				.setData(data)
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.CHUNK_RESPONSE)
				.setChunkResponse(build)
				.build()
	}
	
	private fun processingError(): Torrent.Message {
		val build = Torrent.ChunkResponse
				.newBuilder()
				.setStatus(Torrent.Status.PROCESSING_ERROR)
				.setErrorMessage("Chunk not found")
				.setData(ByteString.EMPTY)
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