package com.company.proto.handlers

import com.company.proto.*
import com.company.proto.torrent.Torrent
import com.google.protobuf.ByteString
import kotlinx.coroutines.experimental.delay

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.ConnectException
import java.net.Socket


class SearchHandler(
		private val storage: Map<Torrent.FileInfo, ByteString>,
		private val currentNode: Torrent.Node
) : Handler {
	private val localSearchHandler: LocalSearchHandler = LocalSearchHandler(storage)
	
	override fun handle(message: Torrent.Message): Torrent.Message {
		val regex = message.searchRequest.regex
		
		if (!regex.isValidRegex()) return messageError(regex)
		
		val localResponse = localSearchHandler.handle(localSearchRequest(regex))
		val nodeSearchResult = Torrent.NodeSearchResult.newBuilder()
				.setNode(currentNode)
				.setStatus(localResponse.localSearchResponse.status)
				.setErrorMessage(localResponse.localSearchResponse.errorMessage)
				.addAllFiles(localResponse.localSearchResponse.fileInfoList)
				.build()
		
		return askOtherNodesFor(nodeSearchResult, regex)
	}
	
	private fun localSearchRequest(regex: String): Torrent.Message {
		val build = Torrent.LocalSearchRequest.newBuilder()
				.setRegex(regex)
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.LOCAL_SEARCH_REQUEST)
				.setLocalSearchRequest(build)
				.build()
	}
	
	private fun askOtherNodesFor(currentNodeSearchResult: Torrent.NodeSearchResult, regex: String): Torrent.Message {
		println("SearchHandler - ask nodes:${currentNode.host}:${currentNode.port}")
		val searchResponse = Torrent.SearchResponse.newBuilder()
				.setStatus(Torrent.Status.SUCCESS)
				.addResults(currentNodeSearchResult)
		nodeList.filter { it != currentNode }.forEach { node ->
			try {
				Socket(node.host, node.port).use { socket ->
					val output = socket.getDataOutputStream()
					val input = socket.getDataInputStream()
					
					val reqMessage = createLocalSearchRequest(regex)
					output.writeMessage(reqMessage)
					
					val resMessage = input.readMessage()
					val localSearchResponse = resMessage.localSearchResponse
					
					val nodeSearchResult = Torrent.NodeSearchResult.newBuilder()
							.setNode(node)
							.setStatus(localSearchResponse.status)
							.setErrorMessage(localSearchResponse.errorMessage)
							.addAllFiles(localSearchResponse.fileInfoList)
							.build()
					searchResponse.addResults(nodeSearchResult)
				}
			} catch (error: IOException) {
				val fileInfo = Torrent.FileInfo.newBuilder().setFilename(regex).build()
				val nodeSearchResult = Torrent.NodeSearchResult.newBuilder()
						.addFiles(fileInfo)
						.setNode(node)
				if (error is ConnectException) {
					nodeSearchResult.status = Torrent.Status.NETWORK_ERROR
					nodeSearchResult.errorMessage = "NETWORK_ERROR"
				} else {
					nodeSearchResult.status = Torrent.Status.PROCESSING_ERROR
					nodeSearchResult.errorMessage = "PROCESSING_ERROR"
				}
				searchResponse.addResults(nodeSearchResult.build())
			}
		}
		
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.SEARCH_RESPONSE)
				.setSearchResponse(searchResponse.build())
				.build()
	}
	
	private fun createLocalSearchRequest(regex: String): Torrent.Message {
		val localSearchRequest = Torrent.LocalSearchRequest.newBuilder()
				.setRegex(regex)
				.build()
		return Torrent.Message.newBuilder()
				.setLocalSearchRequest(localSearchRequest)
				.setType(Torrent.Message.Type.LOCAL_SEARCH_REQUEST)
				.build()
	}
	
	private fun messageError(filename: String): Torrent.Message {
		val fileInfo = Torrent.FileInfo.newBuilder()
				.setFilename(filename)
				.build()
		val nodeSearchResult = Torrent.NodeSearchResult
				.newBuilder()
				.setNode(currentNode)
				.setStatus(Torrent.Status.MESSAGE_ERROR)
				.setErrorMessage("Regex $filename is invalid")
				.addFiles(fileInfo)
				.build()
		val searchResponse = Torrent.SearchResponse
				.newBuilder()
				.setStatus(Torrent.Status.MESSAGE_ERROR)
				.setErrorMessage("MESSAGE_ERROR")
				.addResults(nodeSearchResult)
				.build()
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.SEARCH_RESPONSE)
				.setSearchResponse(searchResponse)
				.build()
	}
}
