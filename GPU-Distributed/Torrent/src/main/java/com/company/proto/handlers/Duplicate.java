package com.company.proto.handlers;

import com.google.protobuf.ByteString;

public class Duplicate {
	private String filename;
	private ByteString data;
	
	public Duplicate(String filename, ByteString data) {
		this.filename = filename;
		this.data = data;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public ByteString getData() {
		return data;
	}
	
	public void setData(ByteString data) {
		this.data = data;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		
		Duplicate duplicate = (Duplicate) o;
		
		if (filename != null ? !filename.equals(duplicate.filename) : duplicate.filename != null) return false;
		return data != null ? data.equals(duplicate.data) : duplicate.data == null;
	}
	
	@Override
	public int hashCode() {
		int result = filename != null ? filename.hashCode() : 0;
		result = 31 * result + (data != null ? data.hashCode() : 0);
		return result;
	}
}
