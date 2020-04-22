package newpackage;

import Parsing.*;
import DataBase.DataBaseManger;

import Models.NodeCounter;
import com.sun.org.apache.xml.internal.serializer.OutputPropertiesFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import Main.FXMLDocumentController;
import java.util.Arrays;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

// labelling XML nodes using interval encoding scheme
public class IntervalLabelling extends DefaultHandler {

    Stack<String> elementStack = new Stack<String>();
    Stack<Integer> attStack = new Stack<Integer>();
    Stack<Object> objectStack = new Stack<Object>();
    public FileWriter fstream;
    public String xmlString;
    //public FileWriter fstream2;
    public ArrayList<BufferedWriter> out = new ArrayList<BufferedWriter>();
    public ArrayList<Models.NodeCounter> nodeCount = new ArrayList<>();
    BufferedWriter[] partitioning;
    Map<Integer, String> map = new TreeMap<Integer, String>();

    Map<Integer, String> mapValue = new TreeMap<Integer, String>();
    Map<Integer, String> mapValueAtt = new TreeMap<Integer, String>();
    public Hashtable<String, Integer> tags = new Hashtable<String, Integer>();
    public Hashtable<String, Integer> tagsAttribute = new Hashtable<String, Integer>();
    public DataBaseManger dataBaseManger = new DataBaseManger();
    public int totalElement = 0;
    public int totalatt = 0;

    public ArrayList<String> uniqueNode = new ArrayList<>();
    public Stack<Long> childStack = new Stack<Long>(); //to follow element's child count
    public Stack<Integer> OrderStack = new Stack<Integer>(); // element's doc order num
    public Stack<Integer> LevelStack = new Stack<Integer>(); //hold level of element
    public ArrayList<String> UniPaths = new ArrayList();

    public Stack<Integer> flags = new Stack<Integer>(); //hold level of element
    public Stack<String> E = new Stack<String>();
    public int leaf = 0;
    public int internal = 0;
    public int maxDepth = 0;
    public int pLevel = 0; //previous level
    public int cLevel = 0; //current level
    public int childCount = 0; // compute number of children per node
    public int order = 0;

    //String E;
    /* list of prime numbers starting from 3 to assign them to
	 * distinct elements or attributes
	 * They are stored up-side down in stack data structure
     */
    public Stack<Integer> pNum; // prime number to hold for elements
    public Stack<Integer> pNumAtt; // prime number to hold for attributes
    public ArrayList<Integer> PrimeList = new ArrayList<Integer>();
    public int totalprime;
    public String FileName;
    public static final String comma = ",";
    public ArrayList<String> values = new ArrayList<String>();
    public static String loc; // location of the datasets
    public Document doc;

    public IntervalLabelling(String fileName) throws IOException {
        System.setProperty("entityExpansionLimit", "0");
        PrimenumberList();
        pNum = new Stack<Integer>();
        assignToStack(pNum);
        pNumAtt = new Stack<Integer>();
        assignToStack(pNumAtt);
        totalprime = pNum.size();

        FileName = fileName;
        // pNum = PrimenumberList();
        // place and hold file name for indexing info
        //
        loc = FXMLDocumentController.AnswerPath;
        File dir = new File(loc + File.separator + fileName);
        if (!dir.exists()) {
            dir.mkdir();
        }

        //FileOutputStream output = new FileOutputStream(loc+fileName+"_interval.gzip");
        //Writer writer = new OutputStreamWriter(new GZIPOutputStream(output), "UTF-8");
        fstream = new FileWriter(loc + File.separator + "interval.txt"); // element
        out.add(new BufferedWriter(fstream)); // 0 interval

        //	output = new FileOutputStream(loc+fileName+"_tags.gzip");
        //   writer = new OutputStreamWriter(new GZIPOutputStream(output), "UTF-8");
        fstream = new FileWriter(loc + File.separator + "tags.txt");
        out.add(new BufferedWriter(fstream)); // 1 tags

        //	 output = new FileOutputStream(loc+fileName+"_values.gzip");
        //	 writer = new OutputStreamWriter(new GZIPOutputStream(output), "UTF-8");
        fstream = new FileWriter(loc + File.separator + "values.txt");
        out.add(new BufferedWriter(fstream)); // 2 text values

    }

    public void startDocument() throws SAXException {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.newDocument();

        } catch (Exception e) {

        }
    }

    public void endDocument() throws SAXException {

        try {

            ArrayList<String> eNames = new ArrayList<>(tags.keySet());
            ArrayList<String> aNames = new ArrayList<>(tagsAttribute.keySet());
            Hashtable<String, Integer> h = new Hashtable<>();
            Map<Integer, String> fis = new TreeMap<>();

            for (int i = 0; i < eNames.size(); i++) {
                h.put(eNames.get(i), i);
                fis.put(i, eNames.get(i));
            }
            int j = h.size();;
            // number of elements
            for (int i = 0; i < aNames.size(); i++) {
                if (!h.containsKey(aNames.get(i))) {
                    h.put(aNames.get(i), j);
                    fis.put(j, aNames.get(i));
                    j++;
                }
            }

            partitioning = new BufferedWriter[h.size()];
            Set<String> up = new HashSet<String>();
            for (int i = 0; i < partitioning.length; i++) {
                //FileOutputStream output = new FileOutputStream(loc+FileName+"_" + fis.get(i)+".gzip");
                //Writer writer = new OutputStreamWriter(new GZIPOutputStream(output), "UTF-8");
                fstream = new FileWriter(loc + "\\IntervalLabelling\\" + File.separator + fis.get(i) + ".txt");
                partitioning[i] = new BufferedWriter(fstream);

            }

            for (String val : map.values()) {

                out.get(0).write(val);
                out.get(0).newLine();
                String[] label = val.split(",");
                String newlabel = label[1];

                for (int i = 2; i < label.length; i++) {
                    newlabel = newlabel + comma + label[i];

                }

                partitioning[h.get(val.split(",")[0])].write(newlabel);
                partitioning[h.get(val.split(",")[0])].newLine();

//                if (mapValue.containsKey(Integer.parseInt(label[1]))) {
////                    fstream = new FileWriter(loc + File.separator + val.split(",")[0] + "_value.txt", true);
////                    BufferedWriter vout = new BufferedWriter(fstream);
////                    if (up.isEmpty() || !up.contains(val.split(",")[0] + "_value")) {
////                        up.add(val.split(",")[0] + "_value");
////                    }
////                    vout.write(mapValue.get(Integer.parseInt(label[1])));
////                    vout.newLine();
////                    vout.close();
//
//                    String data = mapValue.get(Integer.parseInt(label[1]));
//
//
//                } else {
//
//                }
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "15");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);

            xmlString = result.getWriter().toString();

            File f = new File(FXMLDocumentController.AnswerPath + "\\UniqueNodes.xml");
            FileWriter output = null;
            f.createNewFile();
            output = new FileWriter(f.getAbsoluteFile(), false);
            output.write(xmlString);
            output.close();
            out.get(0).close();
            out.get(1).close();
            out.get(2).close();

            for (int i = 0; i < partitioning.length; i++) {
                partitioning[i].close();
            }
            dataBaseManger.st.close();
            dataBaseManger.con.close();

            detali[0] = totalElement;
            detali[1] = totalatt;
            detali[2] = (partitioning.length + up.size());
        } catch (Exception e) {

            e.printStackTrace();
        }

//   	System.out.println("End Document ");
    }

    public int detali[] = new int[3];

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {

        addNewNode(qName);

        if (attributes.getLength() > 0) {
            if (!uniqueNode.contains(attributes.getQName(0))) {
                uniqueNode.add(attributes.getQName(0));
                nodeCount.add(new NodeCounter(attributes.getQName(0)));
                dataBaseManger.createTables(attributes.getQName(0));
            } else {
                for (int j = 0; j < nodeCount.size(); j++) {
                    if (nodeCount.get(j).getName().equals(attributes.getQName(0))) {
                        nodeCount.get(j).Addone();
                        break;
                    }
                }
            }
        }
        elementStack.add(qName);

        totalElement++;

        cLevel = elementStack.size(); //current level
        LevelStack.push(cLevel);

        if (elementStack.size() > maxDepth) {
            maxDepth = elementStack.size();
        }

        if (!tags.containsKey(qName)) {
            tags.put(qName, (Integer) pNum.pop());

            try {
                out.get(1).write(qName + "," + tags.get(qName) + comma + "0");
                out.get(1).newLine();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (!childStack.isEmpty()) {
            Long x = childStack.get(childStack.size() - 1);
            if (x == 1) {
                x = x * tags.get(qName);
            } else {
                if ((x % (tags.get(qName)) != 0)) {
                    x = x * tags.get(qName);
                }
            }

            childStack.set(childStack.size() - 1, x);
            childStack.add((long) 1);
        } else {
            childStack.add((long) 1);
        }

        ++order; // document order
        OrderStack.push(order);

        if (attributes.getLength() > 0) {
            flags.push(LevelStack.peek()); // to indicate it has attribute
            E.push(qName);  // last element has attributes list
            int i = 0;
            while (i < attributes.getLength()) {

                order++;
                totalatt++;
                OrderStack.push(order);
                String l = OrderStack.peek() + "," + OrderStack.peek() + ","
                        + (LevelStack.peek() + 1);

                map.put(OrderStack.peek(), attributes.getQName(i) + "," + OrderStack.peek() + "," + OrderStack.peek() + ","
                        + (LevelStack.peek() + 1));
                try {
                    out.get(2).write(l + ":" + attributes.getValue(i));
                    out.get(2).newLine();
                    mapValue.put(OrderStack.peek(), l + comma + attributes.getValue(i));
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                if (!tagsAttribute.containsKey(attributes.getQName(i))) {
                    tagsAttribute.put(attributes.getQName(i), (Integer) pNumAtt.pop());
                    try {
                        out.get(1).write(attributes.getQName(i) + "," + tagsAttribute.get(attributes.getQName(i)) + comma + "1");
                        out.get(1).newLine();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                int c = 0;
                if (i == 0) // first iteration
                {
                    attStack.add(tagsAttribute.get(attributes.getQName(i)));
                } else {
                    if (!((attStack.get(attStack.size() - 1) % tagsAttribute.get(attributes.getQName(i))) == 0)) {
                        c = attStack.get(attStack.size() - 1) * tagsAttribute.get(attributes.getQName(i));
                        attStack.set(attStack.size() - 1, c);
                    }
                }
                i++;
                OrderStack.pop();
                if ((elementStack.size() + 1) > maxDepth) {
                    maxDepth = elementStack.size() + 1;
                }
            }
        }
        pLevel = cLevel; // previous level is this level

        String path = "";
        for (int i = 0; i < elementStack.size(); i++) {
            if (i < elementStack.size() - 1) {
                path = path + elementStack.get(i) + "/";
            } else {
                path = path + elementStack.get(i);
            }

        }
        //  add new unique nodes
        if (!UniPaths.contains(path)) {
            UniPaths.add(path);
            if (UniPaths.size() == 1) {
                Element e = doc.createElement(qName);
                doc.appendChild(e);
            } else {
                NodeList nList = doc.getElementsByTagName(elementStack.get(elementStack.size() - 2));
                Element Father = (Element) nList.item(nList.getLength() - 1);
                Element e = doc.createElement(qName);
                Father.appendChild(e);
            }
        }

    }
    int i = 0;

    public void endElement(String uri, String localName,
            String qName) throws SAXException {

        String record;
        String path = "";
        for (int i = 0; i < elementStack.size(); i++) {
            if (i < elementStack.size() - 1) {
                path = path + elementStack.get(i) + "/";
            } else {
                path = path + elementStack.get(i);
            }

        }
        //  add new unique nodes
        int pathID = UniPaths.indexOf(path);

        boolean hasAtt = (!E.isEmpty()) && qName.equals(E.peek()) && LevelStack.peek() == flags.peek();
        record = qName + "," + OrderStack.peek();
        if (pLevel > LevelStack.peek() || hasAtt) {
            record = record + "," + ++order;
        } else {
            record = record + "," + order;
        }

        if (!childStack.isEmpty()) {
            Long child = childStack.peek();

            if (hasAtt && !attStack.isEmpty()) {
                record = record + "," + LevelStack.peek(); // + "," + child + comma +  attStack.pop();
                flags.pop();
                E.pop();
            } else {
                if (child == 1) {
                    record = record + "," + LevelStack.peek();
                } else {
                    record = record + "," + LevelStack.peek(); // + "," + child;
                }
            }
        }
        map.put(OrderStack.peek(), record);
        String[] txtLabel = record.split(",");
        if (txtLabel[1].equals(txtLabel[2])) {
            leaf++;
        } else {
            internal++;
        }
        if (!values.isEmpty()) {
            try {
                String v = "";

                for (int j = 0; j < values.size(); j++) {
                    v = v + values.get(j) + " ";

                }
                v = v.trim();
                out.get(2).write(txtLabel[1] + comma + txtLabel[2] + comma + txtLabel[3] + ":" + v);
                out.get(2).newLine();
                mapValue.put(OrderStack.peek(), txtLabel[1] + comma + txtLabel[2] + comma + txtLabel[3] + comma + v);
                dataBaseManger.insertData(qName, txtLabel[1], txtLabel[2], txtLabel[3], v, pathID);
                values.clear();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {

            dataBaseManger.insertData(qName, txtLabel[1], txtLabel[2], txtLabel[3], "", pathID);
        }

        elementStack.pop();
        LevelStack.pop();
        OrderStack.pop();
        childStack.pop();
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        //ignore white space
        String value = new String(ch, start, length).trim();

        if (value.length() != 0) {
            values.add(value);
        }

    }

    public String currentElement() {
        return this.elementStack.peek();
    }

    public void PrimenumberList() {
        // ArrayList<Integer> PrimeList= new ArrayList<Integer>();
        for (int i = 3; i < 2000; i++) {
            boolean isPrimeNumber = true;
            // check to see if the number is prime
            for (int j = 2; j < i; j++) {
                if (i % j == 0) {
                    isPrimeNumber = false;
                    break; // exit the inner for loop
                }
            }
            // print the number if prime
            if (isPrimeNumber) {
                PrimeList.add(i);
            }
        }

    }

    public void assignToStack(Stack<Integer> temp) {
        for (int i = PrimeList.size() - 1; i >= 0; i--) {
            temp.push(PrimeList.get(i));
        }
    }

    private void addNewNode(String qName) {
        if (!uniqueNode.contains(qName)) {
            uniqueNode.add(qName);
            nodeCount.add(new NodeCounter(qName));
            dataBaseManger.createTables(qName);
        } else {
            for (int j = 0; j < nodeCount.size(); j++) {
                if (nodeCount.get(j).getName().equals(qName)) {
                    nodeCount.get(j).Addone();
                    break;
                }
            }
        }
    }

}
