package smlauncher.starmade;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Version implements Comparable<Version>, SerializationInterface, XMLSerializationInterface {
	public int top;
	public int major;
	public int minor;

	public Version() {

	}

	public Version(int top, int major, int minor) {
		this.top = top;
		this.major = major;
		this.minor = minor;
	}

	public static Version parseFrom(String vString) {
		Version v = new Version();
		v.parse(vString);
		return v;
	}

	public static Version deserializeStatic(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		Version v = new Version();
		v.deserialize(b, updateSenderStateId, isOnServer);
		return v;
	}

	public String toString() {
		return top + "." + major + "." + minor;
	}

	public void parse(String vString) {
		String[] split = vString.trim().split("\\.", 3);
		top = Integer.parseInt(split[0]);
		major = Integer.parseInt(split[1]);
		minor = Integer.parseInt(split[2]);
	}

	@Override
	public int compareTo(Version o) {
		int topComp = Integer.compare(top, o.top);
		int majorComp = Integer.compare(major, o.major);
		int minorComp = Integer.compare(minor, o.minor);

		return topComp != 0 ? topComp : (majorComp != 0 ? majorComp : (minorComp));
	}

	public boolean isCompatible(Version o) {
		return top == o.top && major == o.major;
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeInt(top);
		b.writeInt(major);
		b.writeInt(minor);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		top = b.readInt();
		major = b.readInt();
		minor = b.readInt();
	}

	@Override
	public void parseXML(Node node) {
		parse(node.getTextContent());
	}

	@Override
	public Node writeXML(Document doc, Node parent) {
		Element createElement = doc.createElement("Version");
		createElement.setTextContent(toString());
		parent.appendChild(createElement);
		return createElement;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + major;
		result = prime * result + minor;
		result = prime * result + top;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		Version other = (Version) obj;
		if(major != other.major)
			return false;
		if(minor != other.minor)
			return false;
		return top == other.top;
	}

	public boolean isEmpty() {
		return top == 0 && major == 0 && minor == 0;
	}

}
