package com.company.proto.handlers

import com.company.proto.*
import com.company.proto.exceptions.MessageErrorException
import com.company.proto.exceptions.ProcessingErrorException
import com.company.proto.exceptions.UnableToCompleteException
import com.company.proto.torrent.Torrent
import com.google.protobuf.ByteString
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.async
import java.io.IOException
import java.net.ConnectException
import java.net.Socket
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class ReplicateHandler(
		private val storage: MutableMap<Torrent.FileInfo, ByteString>,
		private val duplicates: MutableList<Duplicate>,
		private val currentNode: Torrent.Node
) : Handler {
	val threadPool = Executors.newFixedThreadPool(5)
	
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
		val chunkData = Collections.synchronizedMap(mutableMapOf<Torrent.ChunkInfo, ByteString>())
		
		val response = Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.REPLICATE_RESPONSE)
		
		for (chunkInfo in fileinfo.chunksList) {
			// sources will execute in parallel
			val sources = mutableListOf<Observable<Torrent.NodeReplicationStatus>>()
			for (node in nodeList.shuffled()) { // shuffle nodes before iteration
				sources += Observable.just(node)
						.subscribeOn(Schedulers.from(threadPool)) // ask for chunks on different threads
						.filter { it != currentNode }
						.filter { chunkInfo !in chunkData.keys }
						.map {
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
								replicationStatus.build()
							}
						}
						.onErrorReturn {
							Torrent.NodeReplicationStatus.newBuilder()
									.setNode(node)
									.setChunkIndex(chunkInfo.index)
									.setStatus(Torrent.Status.NETWORK_ERROR)
									.build()
						}
			}
			// merge will run the sources in parallel. Observable.concat will run them sequentially
			val list = Observable.merge(sources).toList()
					.blockingGet() // block current thread until we get all the responses from all nodes for chunk i
			replicateResponse.addAllNodeStatusList(list)
			
			if (chunkInfo !in chunkData.keys) {
				replicateResponse.status = Torrent.Status.UNABLE_TO_COMPLETE
				replicateResponse.errorMessage = "Chunk ${chunkInfo.hash.hashToReadableMD5()} was not found on any node"
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
