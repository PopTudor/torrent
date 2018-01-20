package com.company.proto.handlers;

import com.company.proto.torrent.Torrent;

public class DownloadHandler implements Handler {
	@Override
	public Torrent.Message handle(Torrent.Message message) {
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.DOWNLOAD_RESPONSE)
				.setDownloadResponse(Torrent.DownloadResponse.newBuilder()
						.setStatus(Torrent.Status.PROCESSING_ERROR)
						.build())
				.build();
	}
}
