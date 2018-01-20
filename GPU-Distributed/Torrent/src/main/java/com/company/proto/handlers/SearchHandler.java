package com.company.proto.handlers;

import com.company.proto.torrent.Torrent;

public class SearchHandler implements Handler {
	@Override
	public Torrent.Message handle(Torrent.Message message) {
		return Torrent.Message.newBuilder()
				.setType(Torrent.Message.Type.SEARCH_RESPONSE)
				.setSearchResponse(Torrent.SearchResponse.newBuilder()
						.setStatus(Torrent.Status.PROCESSING_ERROR)
						.build())
				.build();
	}
}
