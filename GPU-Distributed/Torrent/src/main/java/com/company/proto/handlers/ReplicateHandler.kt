package com.company.proto.handlers

import com.company.proto.*
import com.company.proto.torrent.Torrent
import com.google.protobuf.ByteString
import org.apache.commons.lang.StringUtils

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.ConnectException
import java.net.Socket

import com.company.proto.UtilsIO.readMessageFrom
import com.company.proto.UtilsIO.writeMessageTo

class ReplicateHandler(private val storage: Map<Torrent.FileInfo, ByteString>, private val duplicates: MutableList<Duplicate>, private val currentNode: Torrent.Node) : Handler {
	
	override fun handle(message: Torrent.Message): Torrent.Message {
		val filename = message.replicateRequest.fileInfo.filename
		val hash = message.replicateRequest.fileInfo.hash
		val chunkInfos = message.replicateRequest.fileInfo.chunksList
		if (StringUtils.isEmpty(filename)) return messageError()
		
		storage.forEach { fileInfo, bytes ->
			if (fileInfo.hash == hash)
				duplicates.add(Duplicate(filename, bytes))
		}
		duplicates.find { duplicate -> duplicate.filename == filename } ?: return replicate(hash, chunkInfos)
		return successFileReplicated(message.chunkRequest)
	}
	
	private fun messageError(): Torrent.Message {
		val build = Torrent.ReplicateResponse.newBuilder()
				.setErrorMessage("MESSAGE_ERROR")
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.REPLICATE_RESPONSE)
				.setReplicateResponse(build)
				.build()
	}
	
	private fun replicate(fileHash: ByteString, chunkInfos: List<Torrent.ChunkInfo>): Torrent.Message {
		val replicateResponse = Torrent.ReplicateResponse.newBuilder()
		val message = Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.REPLICATE_RESPONSE)
		chunkInfos.forEach { chunkInfo ->
			nodeObservable(currentNode).forEach { node ->
				// foreach node
				try {
					Socket(node.host, node.port).use { socket ->
						val output = DataOutputStream(socket.getOutputStream())
						val input = DataInputStream(socket.getInputStream())
						
						val reqMessage = createChunkRequest(fileHash, chunkInfo.index)
						writeMessageTo(reqMessage, output)
						
						val resMessage = readMessageFrom(input)
						val chunkResponse = resMessage.chunkResponse
						
						val replicationStatus = Torrent.NodeReplicationStatus
								.newBuilder()
								.setNode(node)
								.setChunkIndex(chunkInfo.index)
								.setStatus(chunkResponse.status)
								.build()
						replicateResponse.addNodeStatusList(replicationStatus)
					}
				} catch (error: IOException) {
					val replicationStatus = Torrent.NodeReplicationStatus
							.newBuilder()
							.setNode(node)
							.setChunkIndex(chunkInfo.index)
					if (error is ConnectException) {
						replicateResponse.errorMessage = "NETWORK_ERROR"
						replicationStatus.status = Torrent.Status.NETWORK_ERROR
					} else {
						replicationStatus.status = Torrent.Status.PROCESSING_ERROR
						replicateResponse.errorMessage = "PROCESSING_ERROR"
					}
					replicateResponse.addNodeStatusList(replicationStatus.build())
					error.printStackTrace()
				}
				
			}
		}
		
		message.replicateResponse = replicateResponse.build()
		return message.build()
	}
	
	private fun nodeIsCurrent(node: Torrent.Node): Boolean {
		return node.host == currentNode.host && node.port == currentNode.port
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
	
	private fun successFileReplicated(chunkRequest: Torrent.ChunkRequest): Torrent.Message {
		val nodeReplicationStatus = Torrent.NodeReplicationStatus
				.newBuilder()
				.setStatus(Torrent.Status.SUCCESS)
				.setNode(currentNode)
				.setChunkIndex(chunkRequest.chunkIndex)
				.setErrorMessage("SUCCESS")
				.build()
		val build = Torrent.ReplicateResponse.newBuilder()
				.setErrorMessage("SUCCESS")
				.addNodeStatusList(nodeReplicationStatus)
				.setStatus(Torrent.Status.SUCCESS)
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.REPLICATE_RESPONSE)
				.setReplicateResponse(build)
				.build()
	}
	
}
