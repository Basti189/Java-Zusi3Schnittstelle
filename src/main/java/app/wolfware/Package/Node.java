package app.wolfware.Package;

import java.util.ArrayList;
import java.util.List;

public class Node {

	private final byte[] PACKET_LENGTH = new byte[] {0x00, 0x00, 0x00, 0x00};
	private final byte[] ID;
	private final List<Attribute> listAttribute = new ArrayList<Attribute>();
	private final List<Node> listNodes = new ArrayList<Node>();
	private final byte[] END = new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
	
	public Node(int ID) {
		this.ID = intToByteArray2Bytes(ID);
	}
	
	public Node(byte[] ID) {
		this.ID = ID;
	}

	public void addNode(Node nodes) {
		listNodes.add(nodes);
	}
	
	public void addAttribute(Attribute attribute) {
		listAttribute.add(attribute);
	}
	
	public byte[] get() {
		byte[] result = attachByteArrays(PACKET_LENGTH, ID);
		for (Attribute attribute : listAttribute) {
			result = attachByteArrays(result, attribute.get());
		}
		for (Node nodes : listNodes) {
			result = attachByteArrays(result, nodes.get());
		}
		result = attachByteArrays(result, END);
		return result;
	}
	
	public String toString() {
		StringBuilder output = new StringBuilder("\nKnoten\nID: ");
		for (byte b : ID) {
			output.append("0x").append(String.format("%02x", b)).append(",");
		}
		for (Node nodes : listNodes) {
			output.append(nodes.toString());
		}
		for (Attribute attribute : listAttribute) {
			output.append("\nAttribute\n").append(attribute.toString());
		}
		output.append("\nKnotenende");
		return output.toString();
	}
	
	public byte[] getID() {
		return ID;
	}
	
	public int getIDAsInt() {
		return (ID[0] & 0xFF) | (ID[1] & 0xFF) << 8;
	}
	
	public List<Node> getNodes() {
		return listNodes;
	}

	public Node getNodeByID(int ID) {
		for (Node node : listNodes) {
			if (node.getIDAsInt() == ID) {
				return node;
			}
		}
		return null;
	}
	
	public List<Attribute> getAttribute() {
		return listAttribute;
	}
	
	public Attribute getAttributeByID(int ID) {
		for (Attribute attribute : listAttribute) {
			if (attribute.getIDAsInt() == ID) {
				return attribute;
			}
		}
		return null;
	}
	
	public static byte[] intToByteArray2Bytes(int value) {
		return new byte[] {
				(byte)value,
				(byte)(value >>> 8)};     
	}
	
	private static byte[] attachByteArrays(byte[] target, byte[] source) {
	    byte[] result = new byte[target.length + source.length]; 
	    System.arraycopy(target, 0, result, 0, target.length); 
	    System.arraycopy(source, 0, result, target.length, source.length); 
	    return result;
	}
}
