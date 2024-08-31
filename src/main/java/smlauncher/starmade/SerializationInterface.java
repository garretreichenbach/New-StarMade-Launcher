package smlauncher.starmade;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;

import java.io.*;

public interface SerializationInterface {
	static void set(SerializationInterface from, SerializationInterface to) throws IOException {
		FastByteArrayOutputStream outStream = new FastByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(outStream);
		from.serialize(out, false);
		FastByteArrayInputStream inStream = new FastByteArrayInputStream(outStream.array, 0, outStream.length);
		DataInputStream in = new DataInputStream(inStream);
		to.deserialize(in, 0, false);
	}

	void serialize(DataOutput b, boolean isOnServer) throws IOException;

	void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException;
}