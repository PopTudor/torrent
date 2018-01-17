package com.company.proto.errors;

import com.company.proto.torrent.Torrent;

import java.io.IOException;

public class ChunkException extends IOException {
	Torrent.Node node;
	
	public ChunkException(String message, Throwable cause, Torrent.Node node) {
		super(message, cause);
		this.node = node;
	}
}
