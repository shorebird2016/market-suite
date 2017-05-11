package org.marketsuite.component.xml;

import java.util.List;

import org.jdom.Element;

public class Path extends Element {
	public Path(){
		super(Response.PATH);
	}

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < getSize(); i++)
            sb.append(getValueAt(i));
        return sb.toString();
    }

	public Path(Element pathElement) {
		if(pathElement != null){
			List list = pathElement.getChildren();
			for (int i = 0; i < list.size(); i++) {
				Element ele = (Element) list.get(i);
				String id = ele.getAttributeValue(Response.ID);
				//Some time controller returns attribute value equal to "null"
				if (!id .equals("null"))
					addPathElement(ele.getName(), id);
			}
		}
	}

	public void addPathElement(String name1, String value){
		Element pathElement = new Element(name1);
		pathElement.setAttribute(Response.ID, value);
		addContent(pathElement);
	}

	public String getValueAt(int i){
		List list = this.getChildren();
		Element element = (Element)list.get(i);
		return element.getAttributeValue(Response.ID);
	}

	public String getNameAt(int i){
		List list = this.getChildren();
		Element element = (Element)list.get(i);
		return element.getName();
	}

	public Path removeLeaf(){
		Path path = new Path();
		List list = getChildren();
		int count = list.size() - 1;
		for (int i = 0; i < count;i++)
			path.addPathElement(getNameAt(i), getValueAt(i));
		return path;
	}

	public void addLast(String name1, String value){
		this.addPathElement(name1, value);
	}

	public Path removeFirst(){
		Path path = new Path();
		List list = getChildren();
		int count = list.size();
		for (int i = 1; i < count;i++)
			path.addPathElement(getNameAt(i), getValueAt(i));
		return path;
	}

	public int getSize(){
		List list = getChildren();
		if (list != null)
			return list.size();
		return 0;
	}

	public Element getLastPathElement(){
		int len = getSize();
		if (len > 0)
			return (Element)getChildren().get(len-1);
		return null;
	}

	public boolean isEqual(Path p) {
		if (p != null && p.getSize() == getSize()) {
			for (int i = 0; i < p.getSize(); i++) {
				if (!getValueAt(i).equals(p.getValueAt(i)))
					return false;
			}
			return true;
		}
		return false;
	}

	public boolean includes(Path p) {
		if (p != null && p.getSize() <= getSize()) {
			for (int i = 0; i < p.getSize(); i++) {
				if (!getValueAt(i).equals(p.getValueAt(i)))
					return false;
			}
			return true;
		}
		return false;
	}

	public Element createElement() {
		Element p = new Element(Response.PATH);
		List list = getChildren();
		for (int i = 0; i < list.size(); i++) {
			Element ele = (Element) list.get(i);
			Element pathElement = new Element(ele.getName());
			pathElement.setAttribute(Response.ID, ele.getAttributeValue(Response.ID));
			p.addContent(pathElement);
		}
		return p;
	}

	public Path getPathTill(int index) {
		Path path = new Path();
		for (int i = 0; i <= index; i++)
			path.addPathElement(getNameAt(i), getValueAt(i));
		return path;
	}
}
