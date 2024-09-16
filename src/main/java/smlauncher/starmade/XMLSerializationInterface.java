package smlauncher.starmade;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public interface XMLSerializationInterface {
	void parseXML(Node node);

	Node writeXML(Document doc, Node parent);
}
