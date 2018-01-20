package com.company.proto.handlers;

import com.company.proto.Utils;
import com.company.proto.torrent.Torrent;
import com.google.protobuf.ByteString;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class LocalSearchHandler implements Handler {
	private Map<String, ByteString> storage;
	
	public LocalSearchHandler(Map<String, ByteString> storage) {
		this.storage = storage;
	}
	
	private static boolean isValid(String regex) {
		try {
			Pattern.compile(regex);
			return true;
		} catch (PatternSyntaxException exception) {
			return false;
		}
	}
	
	@Override
	public Torrent.Message handle(Torrent.Message message) {
		String regex = message.getLocalSearchRequest().getRegex();
		
		if (!isValid(regex)) return messageError(regex);
		
		String resultFilename = storage.keySet().stream()
				.filter(filename -> filename.matches(regex))
				.findAny()
				.orElse("");
		if (resultFilename.isEmpty()) return successNoResult(regex);
		else return successWithResult(resultFilename);
	}
	
	private Torrent.Message successWithResult(String resultFilename) {
		byte[] fileData = storage.get(resultFilename).toByteArray();
		Torrent.FileInfo fileInfo = Torrent.FileInfo.newBuilder()
				.setFilename(resultFilename)
				.setHash(Utils.hashToMD5(fileData))
				.setSize(fileData.length)
				.addAllChunks(Utils.toList(fileData))
				.build();
		Torrent.LocalSearchResponse searchResponse = Torrent.LocalSearchResponse
				.newBuilder()
				.setStatus(Torrent.Status.SUCCESS)
				.setErrorMessage("SUCCESS")
				.addFileInfo(fileInfo)
				.build();
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.LOCAL_SEARCH_RESPONSE)
				.setLocalSearchResponse(searchResponse)
				.build();
	}
	
	private Torrent.Message successNoResult(String filename) {
		Torrent.FileInfo fileInfo = Torrent.FileInfo.newBuilder()
				.setFilename(filename)
				.build();
		Torrent.LocalSearchResponse searchResponse = Torrent.LocalSearchResponse
				.newBuilder()
				.setStatus(Torrent.Status.SUCCESS)
				.setErrorMessage("SUCCESS")
				.addFileInfo(fileInfo)
				.build();
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.LOCAL_SEARCH_RESPONSE)
				.setLocalSearchResponse(searchResponse)
				.build();
	}
	
	private Torrent.Message messageError(String filename) {
		Torrent.FileInfo fileInfo = Torrent.FileInfo.newBuilder()
				.setFilename(filename).build();
		Torrent.LocalSearchResponse searchResponse = Torrent.LocalSearchResponse
				.newBuilder()
				.setStatus(Torrent.Status.MESSAGE_ERROR)
				.setErrorMessage("MESSAGE_ERROR")
				.addFileInfo(fileInfo)
				.build();
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.LOCAL_SEARCH_RESPONSE)
				.setLocalSearchResponse(searchResponse)
				.build();
	}
}
