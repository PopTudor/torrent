package com.company.proto.handlers

import com.company.proto.*
import com.company.proto.exceptions.MessageErrorException
import com.company.proto.exceptions.ProcessingErrorException
import com.company.proto.exceptions.UnableToCompleteException
import com.company.proto.torrent.Torrent
import com.google.protobuf.ByteString
import java.io.IOException
import java.net.ConnectException
import java.net.Socket

class ReplicateHandler(
		private val storage: MutableMap<Torrent.FileInfo, ByteString>,
		private val duplicates: MutableList<Duplicate>,
		private val currentNode: Torrent.Node
) : Handler {
	
	override fun handle(message: Torrent.Message): Torrent.Message {
		val filename = message.replicateRequest.fileInfo.filename
		val hash = message.replicateRequest.fileInfo.hash
		if (filename.isNullOrBlank()) return messageError()
		
		val fileInfoDup = storage.keys.find { it.hash == hash } ?: return replicate(message.replicateRequest.fileInfo)
		
		duplicates.add(Duplicate(fileInfoDup, storage[fileInfoDup]))
		
		return successFileReplicated(message.replicateRequest.fileInfo)
	}
	
	private fun replicate(fileinfo: Torrent.FileInfo): Torrent.Message {
		println("Replicate: ${fileinfo.hash.hashToReadableMD5()}")
		val replicateResponse = Torrent.ReplicateResponse.newBuilder()
		val chunkData = mutableMapOf<Torrent.ChunkInfo, ByteString>()
		val response = Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.REPLICATE_RESPONSE)
		
		for (chunkInfo in fileinfo.chunksList) {
			// if got the chunk from other node, move to the next on
			if (chunkInfo in chunkData.keys) continue
			for (node in nodeList) {
				try {
					if (node == currentNode) continue
					Socket(node.host, node.port).use { socket ->
						val output = socket.getDataOutputStream()
						val input = socket.getDataInputStream()
						
						val chunkRequest = createChunkRequest(fileinfo.hash, chunkInfo.index)
						output.writeMessage(chunkRequest)
						
						val chunkResponse = input.readMessage().chunkResponse
						val chunkInfoResponse = Torrent.ChunkInfo.newBuilder()
								.setHash(chunkResponse.data.toMD5Hash())
								.setIndex(chunkInfo.index)
								.setSize(chunkResponse.data.size())
								.build()
						chunkData[chunkInfoResponse] = chunkResponse.data
						val replicationStatus = Torrent.NodeReplicationStatus.newBuilder()
								.setNode(node)
								.setChunkIndex(chunkInfo.index)
								.setStatus(chunkResponse.status)
						replicateResponse.addNodeStatusList(replicationStatus.build())
					}
				} catch (error: IOException) {
					val replicationStatus = Torrent.NodeReplicationStatus.newBuilder()
							.setNode(node)
							.setChunkIndex(chunkInfo.index)
							.setStatus(Torrent.Status.NETWORK_ERROR)
					replicateResponse.addNodeStatusList(replicationStatus.build())
				}
			}
			if (chunkInfo !in chunkData.keys) {
				replicateResponse.status = Torrent.Status.UNABLE_TO_COMPLETE
				replicateResponse.errorMessage = "Did not receive the chunk ${chunkInfo.hash.hashToReadableMD5()} from any node"
				replicateResponse.clearNodeStatusList()
				response.replicateResponse = replicateResponse.build()
				return response.build()
			}
		}
		val finalData = ByteString.copyFrom(chunkData.values)
		when {
			finalData.toMD5Hash() == fileinfo.hash -> {
				storage[fileinfo] = finalData
				replicateResponse.status = Torrent.Status.SUCCESS
				response.replicateResponse = replicateResponse.build()
			}
			else -> response.replicateResponse = Torrent.ReplicateResponse.newBuilder()
					.setStatus(Torrent.Status.PROCESSING_ERROR)
					.setErrorMessage("The received chunks do not add up to file size")
					.build()
		}
		return response.build()
	}
	
	
	private fun successFileReplicated(fileInfo: Torrent.FileInfo): Torrent.Message {
		println("Succes replicate: ${fileInfo.hash.hashToReadableMD5()}")
		val replicateResponse = Torrent.ReplicateResponse.newBuilder()
				.setStatus(Torrent.Status.SUCCESS)
		fileInfo.chunksList.forEach {
			val nodeReplicationStatus = Torrent.NodeReplicationStatus.newBuilder()
					.setStatus(Torrent.Status.SUCCESS)
					.setNode(currentNode)
					.setChunkIndex(it.index)
			replicateResponse.addNodeStatusList(nodeReplicationStatus)
		}
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.REPLICATE_RESPONSE)
				.setReplicateResponse(replicateResponse)
				.build()
	}
	
	private fun createChunkRequest(fileHash: ByteString, chunkIndex: Int): Torrent.Message {
		val build = Torrent.ChunkRequest.newBuilder()
				.setFileHash(fileHash)
				.setChunkIndex(chunkIndex)
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.CHUNK_REQUEST)
				.setChunkRequest(build)
				.build()
	}
	
	private fun messageError(): Torrent.Message {
		val build = Torrent.ReplicateResponse.newBuilder()
				.setStatus(Torrent.Status.MESSAGE_ERROR)
				.setErrorMessage("The filename is empty")
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.REPLICATE_RESPONSE)
				.setReplicateResponse(build)
				.build()
	}
}
