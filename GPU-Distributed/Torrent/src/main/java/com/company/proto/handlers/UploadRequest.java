package com.company.proto.handlers;

import com.company.proto.Constants;
import com.company.proto.UtilsKt;
import com.company.proto.torrent.Torrent;
import com.google.common.base.Strings;
import com.google.protobuf.ByteString;

import java.util.Arrays;
import java.util.Map;

public class UploadRequest implements Handler {
	Map<String, ByteString> storage;
	
	public UploadRequest(Map<String, ByteString> storage) {
		this.storage = storage;
	}
	
	@Override
	public Torrent.Message handle(Torrent.Message message) {
		String filename = message.getUploadRequest().getFilename();
		ByteString data = message.getUploadRequest().getData();
		byte[] dataBytes = data.toByteArray();
		ByteString hash = UtilsKt.toMD5Hash(dataBytes);
		
		Torrent.FileInfo.Builder fileInfo = Torrent.FileInfo
				.newBuilder()
				.setHash(hash)
				.setSize(dataBytes.length)
				.setFilename(filename);
		
		if (Strings.isNullOrEmpty(filename)) return errorFilenameEmptyResponse(fileInfo);
		
		for (int i = 0, j = 0; i < data.size(); i += Constants.CHUNK_SIZE, j++) {
			byte[] chunk = Arrays.copyOfRange(dataBytes, i, Math.min(data.size(), i + Constants.CHUNK_SIZE));
			
			Torrent.ChunkInfo chunkInfo = Torrent.ChunkInfo
					.newBuilder()
					.setIndex(j)
					.setSize(chunk.length)
					.setHash(UtilsKt.toMD5Hash(chunk))
					.build();
			
			fileInfo.addChunks(j, chunkInfo);
		}
		Torrent.FileInfo info = fileInfo.build();
		
		if (storage.containsKey(filename)) return successFileUploadResponse(info);
		
		storage.put(filename, data);
		
		return successFileUploadResponse(info);
	}
	
	private Torrent.Message errorFilenameEmptyResponse(Torrent.FileInfo.Builder fileInfo) {
		Torrent.UploadResponse uploadResponse = Torrent.UploadResponse
				.newBuilder()
				.setStatus(Torrent.Status.SUCCESS)
				.setErrorMessage("Filename must not be empty")
				.setFileInfo(fileInfo)
				.build();
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.UPLOAD_RESPONSE)
				.setUploadResponse(uploadResponse)
				.build();
	}
	
	private Torrent.Message successFileUploadResponse(Torrent.FileInfo fileInfo) {
		Torrent.UploadResponse uploadResponse = Torrent.UploadResponse
				.newBuilder()
				.setStatus(Torrent.Status.SUCCESS)
				.setErrorMessage("")
				.setFileInfo(fileInfo)
				.build();
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.UPLOAD_RESPONSE)
				.setUploadResponse(uploadResponse)
				.build();
	}
	
	private Torrent.Message errorProcessing(Torrent.FileInfo fileInfo) {
		Torrent.UploadResponse uploadResponse = Torrent.UploadResponse.newBuilder()
				.setStatus(Torrent.Status.PROCESSING_ERROR)
				.setErrorMessage("File could not be stored")
				.setFileInfo(fileInfo)
				.build();
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.UPLOAD_RESPONSE)
				.setUploadResponse(uploadResponse)
				.build();
	}
}
