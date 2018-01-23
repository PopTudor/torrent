package com.company.proto;

import com.company.proto.torrent.Torrent;
import com.google.common.collect.Streams;
import com.google.protobuf.ByteString;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.company.proto.UtilsIO.readMessageFrom;
import static com.company.proto.UtilsIO.writeMessageTo;

public class NodeConfig {
	
	public static List<Torrent.Node> nodeList() {
		return IntStream.rangeClosed(5001, 5005)
				.mapToObj(index ->
						Torrent.Node.newBuilder()
								.setHost("127.0.0.1")
								.setPort(index)
								.build()
				).collect(Collectors.toList());
	}
	
	public static void askOtherNodes(Torrent.Node currentNode, Consumer<Torrent.Node> action) {
		nodeList().stream()
				.filter(node -> !node.equals(currentNode))
				.forEach(action);
	}
}
