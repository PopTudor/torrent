package com.company.proto.handlers;

import com.company.proto.NodeConfig;
import com.company.proto.UtilsKt;
import com.company.proto.torrent.Torrent;
import com.google.protobuf.ByteString;
import io.reactivex.schedulers.Timed;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.company.proto.UtilsIO.readMessageFrom;
import static com.company.proto.UtilsIO.writeMessageTo;

public class SearchHandler implements Handler {
	private Torrent.Node currentNode;
	private LocalSearchHandler localSearchHandler;
	
	public SearchHandler(Map<Torrent.FileInfo, ByteString> storage, Torrent.Node currentNode) {
		this.currentNode = currentNode;
		this.localSearchHandler = new LocalSearchHandler(storage);
	}
	
	@Override
	public Torrent.Message handle(Torrent.Message message) {
		String regex = message.getSearchRequest().getRegex();
		
		if (!UtilsKt.isValidRegex(regex)) return messageError(regex);
		
		Torrent.LocalSearchRequest build = Torrent.LocalSearchRequest.newBuilder()
				.setRegex(regex)
				.build();
		
		Torrent.Message localSearchRequest = Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.LOCAL_SEARCH_REQUEST)
				.setLocalSearchRequest(build)
				.build();
		
		Torrent.Message localResponse = localSearchHandler.handle(localSearchRequest);
		Torrent.LocalSearchResponse localSearchResponse = localResponse.getLocalSearchResponse();
		Torrent.NodeSearchResult nodeSearchResult = Torrent.NodeSearchResult.newBuilder()
				.setNode(currentNode)
				.setStatus(localSearchResponse.getStatus())
				.setErrorMessage(localSearchResponse.getErrorMessage())
				.addAllFiles(localSearchResponse.getFileInfoList())
				.build();
		
		return askOtherNodesFor(nodeSearchResult, regex);
	}
	
	private Torrent.Message askOtherNodesFor(Torrent.NodeSearchResult currentNodeSearchResult, String regex) {
		Torrent.SearchResponse.Builder searchResponse = Torrent.SearchResponse.newBuilder()
				.setStatus(Torrent.Status.SUCCESS)
				.setErrorMessage("SUCCESS");
		searchResponse.addResults(currentNodeSearchResult);
		NodeConfig.INSTANCE.nodeObservable(currentNode)
//				.timeInterval(TimeUnit.MILLISECONDS)
//				.map(Timed::value)
				.subscribe(node -> {
					try (Socket socket = new Socket(node.getHost(), node.getPort())) {
						DataOutputStream output = new DataOutputStream(socket.getOutputStream());
						DataInputStream input = new DataInputStream(socket.getInputStream());
						
						Torrent.Message reqMessage = createLocalSearchRequest(regex);
						writeMessageTo(reqMessage, output);
						
						Torrent.Message resMessage = readMessageFrom(input);
						Torrent.LocalSearchResponse localSearchResponse = resMessage.getLocalSearchResponse();
						
						Torrent.NodeSearchResult nodeSearchResult = Torrent.NodeSearchResult.newBuilder()
								.setNode(node)
								.setStatus(localSearchResponse.getStatus())
								.setErrorMessage(localSearchResponse.getErrorMessage())
								.addAllFiles(localSearchResponse.getFileInfoList())
								.build();
						searchResponse.addResults(nodeSearchResult);
					} catch (IOException error) {
						Torrent.FileInfo fileInfo = Torrent.FileInfo.newBuilder().setFilename(regex).build();
						Torrent.NodeSearchResult.Builder nodeSearchResult = Torrent.NodeSearchResult.newBuilder()
								.addFiles(fileInfo)
								.setNode(node);
						if (error instanceof ConnectException) {
							nodeSearchResult.setStatus(Torrent.Status.NETWORK_ERROR);
							nodeSearchResult.setErrorMessage("NETWORK_ERROR");
						} else {
							nodeSearchResult.setStatus(Torrent.Status.PROCESSING_ERROR);
							nodeSearchResult.setErrorMessage("PROCESSING_ERROR");
						}
						searchResponse.addResults(nodeSearchResult.build());
						System.out.println("node: " + node.toString()+"\n"+error.toString());
					}
				}, Throwable::printStackTrace);
		
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.SEARCH_RESPONSE)
				.setSearchResponse(searchResponse.build())
				.build();
	}
	
	private Torrent.Message createLocalSearchRequest(String regex) {
		Torrent.LocalSearchRequest localSearchRequest = Torrent.LocalSearchRequest.newBuilder()
				.setRegex(regex)
				.build();
		return Torrent.Message.newBuilder()
				.setLocalSearchRequest(localSearchRequest)
				.setType(Torrent.Message.Type.LOCAL_SEARCH_REQUEST)
				.build();
	}
	
	private Torrent.Message messageError(String filename) {
		Torrent.FileInfo fileInfo = Torrent.FileInfo.newBuilder()
				.setFilename(filename)
				.build();
		Torrent.NodeSearchResult nodeSearchResult = Torrent.NodeSearchResult
				.newBuilder()
				.setNode(currentNode)
				.setStatus(Torrent.Status.MESSAGE_ERROR)
				.setErrorMessage("MESSAGE_ERROR")
				.addFiles(fileInfo)
				.build();
		Torrent.SearchResponse searchResponse = Torrent.SearchResponse
				.newBuilder()
				.setStatus(Torrent.Status.MESSAGE_ERROR)
				.setErrorMessage("MESSAGE_ERROR")
				.addResults(nodeSearchResult)
				.build();
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.SEARCH_RESPONSE)
				.setSearchResponse(searchResponse)
				.build();
	}
}
