package com.company.proto.handlers;

import com.company.proto.Constants;
import com.company.proto.Utils;
import com.company.proto.torrent.Torrent;
import com.google.protobuf.ByteString;

import java.util.HashMap;
import java.util.Map;

public class ChunkHandler implements Handler {
	private Map<String, ByteString> storage;
	private Map<String, String> filenameHashes;
	
	public ChunkHandler(Map<String, ByteString> storage) {
		this.storage = storage;
		filenameHashes = new HashMap<>(storage.size());
		storage.forEach((key, value) -> {
			String fileHash = Utils.hashToMD5(key.getBytes()).toStringUtf8();
			filenameHashes.put(fileHash, key);
		});
	}
	
	@Override
	public Torrent.Message handle(Torrent.Message message) {
		ByteString hash = message.getChunkRequest().getFileHash();
		int chunkIndex = message.getChunkRequest().getChunkIndex();
		
		if (hash.size() != 16 || chunkIndex < 0) return messageError();
		// if don't have file, return error
		boolean hasHash = filenameHashes.containsKey(hash.toStringUtf8());
		if (hasHash) {
			String filename = filenameHashes.get(hash);
			return chunkData(storage.get(filename), chunkIndex);
		} else return unableToComplete();
	}
	
	private Torrent.Message chunkData(ByteString bytes, int chunkIndex) {
		int i = chunkIndex * 1024;
		ByteString data = ByteString.copyFrom(bytes.toByteArray(), i, Math.min(bytes.size(), i + Constants.CHUNK_SIZE));
//		Arrays.copyOfRange(bytes.toByteArray(), i,Math.min(bytes.size(), i + Constants.CHUNK_SIZE) );
		Torrent.ChunkResponse build = Torrent.ChunkResponse
				.newBuilder()
				.setStatus(Torrent.Status.SUCCESS)
				.setErrorMessage("SUCCESS")
				.setData(data)
				.build();
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.CHUNK_RESPONSE)
				.setChunkResponse(build)
				.build();
	}
	
	private Torrent.Message messageError() {
		Torrent.ChunkResponse build = Torrent.ChunkResponse
				.newBuilder()
				.setStatus(Torrent.Status.MESSAGE_ERROR)
				.setErrorMessage("MESSAGE_ERROR")
				.setData(ByteString.EMPTY)
				.build();
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.CHUNK_RESPONSE)
				.setChunkResponse(build)
				.build();
	}
	
	private Torrent.Message unableToComplete() {
		Torrent.ChunkResponse build = Torrent.ChunkResponse.newBuilder()
				.setStatus(Torrent.Status.UNABLE_TO_COMPLETE)
				.setErrorMessage("UNABLE_TO_COMPLETE")
				.setData(ByteString.EMPTY)
				.build();
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.CHUNK_RESPONSE)
				.setChunkResponse(build)
				.build();
	}
}
