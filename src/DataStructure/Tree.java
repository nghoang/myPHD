/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import utility.Utilities;

/**
 * 
 * @author c1038943
 */
public class Tree<T> {

	private Node<T> rootElement;

	public static Node<String> AddNodeToTree(Node<String> curNode,ArrayList<String> lines, int level) {
	
		while(lines.size() > 0)
		{
			String line = lines.get(0);
			lines.remove(0);
			String prefix = Utilities.SimpleRegexSingle("(^[\\-]*)", line, 1);
			line = line.replaceAll("(^[\\-]*)", "");
			int lineLevel = prefix.length();
			if (lineLevel == 0)
			{
				curNode.setData(line);
			}
			else if (level == lineLevel)
			{
				Node<String> n = new Node<String>();
				n.setData(line);
				curNode.addChild(n);
			}
			else if (level > lineLevel)
			{
				String startWithStr = "";
				for (int i=0;i<lineLevel;i++)
				{
					startWithStr += "-";
				}
				lines.add(0, startWithStr + line);
				return curNode;
			}
			else if (level < lineLevel)
			{
				Node<String> n = new Node<String>();
				n.setData(line);
				n = AddNodeToTree(n, lines, lineLevel + 1);
				curNode.addChild(n);
			}
		}
		
		return curNode;
	}
	
	public static Tree<String> ReadTreeFromFlatFile(String filename)
	{
		Tree<String> tr = new Tree<String>();
		String content;
			content = Utilities.readFileAsString(filename);
		String[] lines = content.split("\n");
		ArrayList<String> arraylines = new ArrayList<String>();
		for (String line : lines)
		{
			arraylines.add(line.trim());
		}
		Node<String> root = new Node<String>();
		root = AddNodeToTree(root,arraylines,0);
		tr.setRootElement(root);
		return tr;
	}

	/**
	 * Default ctor.
	 */
	public Tree() {
		super();
	}

	/**
	 * Return the root Node of the tree.
	 * 
	 * @return the root element.
	 */
	public Node<T> getRootElement() {
		return this.rootElement;
	}

	/**
	 * Set the root Element for the tree.
	 * 
	 * @param rootElement
	 *            the root element to set.
	 */
	public void setRootElement(Node<T> rootElement) {
		this.rootElement = rootElement;
	}

	/**
	 * Returns the Tree<T> as a List of Node<T> objects. The elements of the
	 * List are generated from a pre-order traversal of the tree.
	 * 
	 * @return a List<Node<T>>.
	 */
	public List<Node<T>> toList() {
		List<Node<T>> list = new ArrayList<Node<T>>();
		walk(rootElement, list);
		return list;
	}

	/**
	 * Returns a String representation of the Tree. The elements are generated
	 * from a pre-order traversal of the Tree.
	 * 
	 * @return the String representation of the Tree.
	 */
	public String toString() {
		return toList().toString();
	}

	/**
	 * Walks the Tree in pre-order style. This is a recursive method, and is
	 * called from the toList() method with the root element as the first
	 * argument. It appends to the second argument, which is passed by reference
	 * * as it recurses down the tree.
	 * 
	 * @param element
	 *            the starting element.
	 * @param list
	 *            the output of the walk.
	 */
	private void walk(Node<T> element, List<Node<T>> list) {
		list.add(element);
		for (Node<T> data : element.getChildren()) {
			walk(data, list);
		}
	}
}
