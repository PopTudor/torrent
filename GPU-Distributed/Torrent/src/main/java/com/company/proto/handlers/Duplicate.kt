package com.company.proto.handlers

import com.company.proto.torrent.Torrent
import com.google.protobuf.ByteString

data class Duplicate(var filename: Torrent.FileInfo, var data: ByteString?)
