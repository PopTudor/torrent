package com.company.proto.handlers;

import com.company.proto.torrent.Torrent;
import com.google.protobuf.ByteString;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

public class ReplicateHandler implements Handler {
	private final Map<String, ByteString> storage;
	private final List<Duplicate> duplicates;
	
	public ReplicateHandler(Map<String, ByteString> storage, List<Duplicate> duplicates) {
		this.storage = storage;
		this.duplicates = duplicates;
	}
	
	@Override
	public Torrent.Message handle(Torrent.Message message) {
		String filename = message.getReplicateRequest().getFileInfo().getFilename();
		ByteString hash = message.getReplicateRequest().getFileInfo().getHash();
		
		if (StringUtils.isEmpty(filename)) return messageError();
		
		boolean contains = duplicates.stream().anyMatch(duplicate -> duplicate.getFilename().equals(filename));
		if (contains) return successFileReplicated();
		else return replicate();
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
	
	private Torrent.Message replicate() {
		
		return null;
	}
	
	private Torrent.Message successFileReplicated() {
		Torrent.ReplicateResponse build = Torrent.ReplicateResponse.newBuilder()
				.setErrorMessage("SUCCESS")
				.setStatus(Torrent.Status.SUCCESS)
				.build();
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.REPLICATE_RESPONSE)
				.setReplicateResponse(build)
				.build();
	}
}
