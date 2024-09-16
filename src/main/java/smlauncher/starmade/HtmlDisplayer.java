package smlauncher.starmade;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

/**
 * A complete Java class that demonstrates how to create an HTML viewer with styles,
 * using the JEditorPane, HTMLEditorKit, StyleSheet, and JFrame.
 *
 * @author alvin alexander, devdaily.com.
 */
public class HtmlDisplayer extends JEditorPane {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */

	private final HTMLEditorKit kit;

	public HtmlDisplayer() {
		// create jeditorpane

		// make it read-only
		setEditable(false);

		// add an html editor kit
		kit = new HTMLEditorKit();
		setEditorKit(kit);

		// add some styles to the html
		StyleSheet styleSheet = kit.getStyleSheet();
		styleSheet.addRule("body {color:#bfbfbf; font-family:Verdana,Arial,sans-serif; margin: 4px; background-color : #292929; }");
		styleSheet.addRule("h1 {color: #fffeff;}");
		styleSheet.addRule("h2 {color: #fffeff;}");
		styleSheet.addRule("pre {font : 10px monaco; color : #3b3b3b; background-color : black; }");

		// create a document, set it on the jeditorpane, then add the html
		Document doc = kit.createDefaultDocument();
		setDocument(doc);

		DefaultCaret caret = (DefaultCaret) getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		// create a scrollpane; modify its attributes as desired

		//        addComponentListener(new ComponentAdapter() {
		//    		@Override
		//    		public void componentResized(ComponentEvent e) {
		//    			jEditorPane.setPreferredSize(new Dimension(
		//    					HtmlDisplayer.this.getWidth()-20,
		//    					HtmlDisplayer.this.getHeight()-20));
		//    			jEditorPane.setSize(new Dimension(
		//    					HtmlDisplayer.this.getWidth()-20,
		//    					HtmlDisplayer.this.getHeight()-20));
		//    		}
		//    	});

	}

}
