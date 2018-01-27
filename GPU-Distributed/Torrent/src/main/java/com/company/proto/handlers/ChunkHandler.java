package com.company.proto.handlers;

import com.company.proto.Constants;
import com.company.proto.UtilsKt;
import com.company.proto.torrent.Torrent;
import com.google.protobuf.ByteString;

import java.util.HashMap;
import java.util.Map;

import static com.company.proto.UtilsKt.toMD5Hash;

public class ChunkHandler implements Handler {
	private Map<Torrent.FileInfo, ByteString> storage;
	private Map<ByteString, String> filenameHashes;
	
	public ChunkHandler(Map<Torrent.FileInfo, ByteString> storage) {
		this.storage = storage;
		filenameHashes = new HashMap<>(storage.size());
		storage.forEach((key, value) -> filenameHashes.put(UtilsKt.toMD5Hash(value.toByteArray()), key.getFilename()));
	}
	
	@Override
	public Torrent.Message handle(Torrent.Message message) {
		ByteString hash = message.getChunkRequest().getFileHash();
		int chunkIndex = message.getChunkRequest().getChunkIndex();
		
		if (hash.size() != 16 || chunkIndex < 0) return messageError();
		// if don't have file, return error
		boolean hasHash = filenameHashes.containsKey(hash);
		if (hasHash) {
			String filename = filenameHashes.get(hash);
			ByteString bytes = ByteString.EMPTY;
			for (Map.Entry<Torrent.FileInfo, ByteString> it : storage.entrySet()) {
				if (it.getKey().getFilename().equals(filename)) {
					bytes = it.getValue();
					break;
				}
			}
			
			return chunkData(bytes, chunkIndex);
		} else return unableToComplete();
	}
	
	private Torrent.Message chunkData(ByteString bytes, int chunkIndex) {
		int i = chunkIndex * Constants.CHUNK_SIZE;
		ByteString data = ByteString.copyFrom(bytes.toByteArray(), i, Math.min(bytes.size(), i + Constants.CHUNK_SIZE));
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
