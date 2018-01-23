package com.company.proto.handlers;

import com.company.proto.Utils;
import com.company.proto.torrent.Torrent;
import com.google.protobuf.ByteString;
import org.apache.commons.lang.StringUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import static com.company.proto.UtilsIO.readMessageFrom;
import static com.company.proto.UtilsIO.writeMessageTo;

public class ReplicateHandler implements Handler {
	private final Map<String, ByteString> storage;
	private final List<Duplicate> duplicates;
	private Torrent.Node currentNode;
	
	public ReplicateHandler(Map<String, ByteString> storage, List<Duplicate> duplicates, Torrent.Node node) {
		this.storage = storage;
		this.duplicates = duplicates;
		currentNode = node;
	}
	
	@Override
	public Torrent.Message handle(Torrent.Message message) {
		String filename = message.getReplicateRequest().getFileInfo().getFilename();
		ByteString hash = message.getReplicateRequest().getFileInfo().getHash();
		List<Torrent.ChunkInfo> chunkInfos = message.getReplicateRequest().getFileInfo().getChunksList();
		if (StringUtils.isEmpty(filename)) return messageError();
		
		storage.forEach((s, bytes) -> {
			if (haveFileLocally(hash, bytes))
				duplicates.add(new Duplicate(filename, bytes));
		});
		boolean contains = duplicates.stream().anyMatch(duplicate -> duplicate.getFilename().equals(filename));
		if (contains) return successFileReplicated(message.getChunkRequest());
		else return replicate(hash, chunkInfos);
	}
	
	private boolean haveFileLocally(ByteString hash, ByteString bytes) {
		return Utils.hashToMD5(bytes.toByteArray()).equals(hash);
	}
	
	private Torrent.Message messageError() {
		Torrent.ReplicateResponse build = Torrent.ReplicateResponse.newBuilder()
				.setErrorMessage("MESSAGE_ERROR")
				.build();
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.REPLICATE_RESPONSE)
				.setReplicateResponse(build)
				.build();
	}
	
	private Torrent.Message replicate(ByteString fileHash, List<Torrent.ChunkInfo> chunkInfos) {
		Torrent.ReplicateResponse.Builder replicateResponse = Torrent.ReplicateResponse.newBuilder();
		Torrent.Message.Builder message = Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.REPLICATE_RESPONSE);
		chunkInfos.forEach(chunkInfo -> {
			for (int i = 5001; i <= 5005; i++) { // foreach node
				Torrent.Node node = Torrent.Node.newBuilder()
						.setHost("127.0.0.1")
						.setPort(i)
						.build();
				if (nodeIsCurrent(node)) continue;
				try (Socket socket = new Socket(node.getHost(), node.getPort())) {
					DataOutputStream output = new DataOutputStream(socket.getOutputStream());
					DataInputStream input = new DataInputStream(socket.getInputStream());
					
					Torrent.Message reqMessage = createChunkRequest(fileHash, chunkInfo.getIndex());
					writeMessageTo(reqMessage, output);
					
					Torrent.Message resMessage = readMessageFrom(input);
					Torrent.ChunkResponse chunkResponse = resMessage.getChunkResponse();
					
					Torrent.NodeReplicationStatus replicationStatus = Torrent.NodeReplicationStatus
							.newBuilder()
							.setNode(node)
							.setChunkIndex(chunkInfo.getIndex())
							.setStatus(chunkResponse.getStatus())
							.setErrorMessageBytes(ByteString.EMPTY)
							.build();
					replicateResponse.addNodeStatusList(replicationStatus);
				} catch (IOException error) {
					Torrent.NodeReplicationStatus.Builder replicationStatus = Torrent.NodeReplicationStatus
							.newBuilder()
							.setNode(node)
							.setChunkIndex(chunkInfo.getIndex())
							;
					if (error instanceof ConnectException) {
						replicateResponse.setErrorMessage("NETWORK_ERROR");
						replicationStatus.setStatus(Torrent.Status.NETWORK_ERROR);
					} else
						replicationStatus.setStatus(Torrent.Status.PROCESSING_ERROR);
						replicateResponse.setErrorMessage("PROCESSING_ERROR");
					replicateResponse.addNodeStatusList(replicationStatus.build());
					error.printStackTrace();
				}
			}
		});
		message.setReplicateResponse(replicateResponse.build());
		return message.build();
	}
	
	private boolean nodeIsCurrent(Torrent.Node node) {
		return node.getHost().equals(currentNode.getHost()) && node.getPort() == currentNode.getPort();
	}
	
	private Torrent.Message createChunkRequest(ByteString fileHash, int chunkIndex) {
		Torrent.ChunkRequest build = Torrent.ChunkRequest.newBuilder()
				.setFileHash(fileHash)
				.setChunkIndex(chunkIndex)
				.build();
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.CHUNK_REQUEST)
				.setChunkRequest(build)
				.build();
	}
	
	private Torrent.Message successFileReplicated(Torrent.ChunkRequest chunkRequest) {
		Torrent.NodeReplicationStatus nodeReplicationStatus = Torrent.NodeReplicationStatus
				.newBuilder()
				.setStatus(Torrent.Status.SUCCESS)
				.setNode(currentNode)
				.setChunkIndex(chunkRequest.getChunkIndex())
				.setErrorMessage("SUCCESS")
				.build();
		Torrent.ReplicateResponse build = Torrent.ReplicateResponse.newBuilder()
				.setErrorMessage("SUCCESS")
				.addNodeStatusList(nodeReplicationStatus)
				.setStatus(Torrent.Status.SUCCESS)
				.build();
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.REPLICATE_RESPONSE)
				.setReplicateResponse(build)
				.build();
	}
	
}
