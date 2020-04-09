package Parsing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import Main.FXMLDocumentController;

// labelling XML nodes using interval encoding scheme
public class DeweyLabelling extends DefaultHandler {

    Stack<String> elementStack = new Stack<String>();
    Stack<Integer> attStack = new Stack<Integer>();

    public String Filename;
    public FileWriter fstream;
    public ArrayList<BufferedWriter> out = new ArrayList<BufferedWriter>();

    Hashtable<Integer, String> map = new Hashtable<Integer, String>();

    public static int totalElement = 0;
    public static int totalAtt = 0;
    public static int totaltxtNodes = 0;
    public static int totalparentNodes = 0;

    public Stack<Integer> childStack = new Stack<Integer>(); //to follow element's child count
    public Stack<Integer> OrderStack = new Stack<Integer>(); // element's doc order num
    public Stack<Integer> LevelStack = new Stack<Integer>(); //hold level of element

    public Stack<Integer> flags = new Stack<Integer>(); //hold level of element
    public Stack<String> E = new Stack<String>(); // hold type of elements

    public int maxDepth = 0;
    public int pLevel = 0; //previous level
    public int cLevel = 0; //current level
    public int childCount = 0; // compute number of children per node
    public int order = 0;

    public static final String comma = ",";
    public Stack<String> parentLabel = new Stack<String>();

    public Stack<String> values = new Stack<String>();
    public static String loc; // location of the datasets

    public Stack<Integer> pNum; // prime number to hold for elements
    public Stack<Integer> pNumAtt; // prime number to hold for attributes
    public ArrayList<Integer> PrimeList = new ArrayList<Integer>();

    public Hashtable<String, Integer> tags = new Hashtable<String, Integer>();
    public Hashtable<String, Integer> tagsAttribute = new Hashtable<String, Integer>();

    public DeweyLabelling(String fileName) throws IOException {
        Filename = fileName;
        PrimenumberList(); // generating prime numbers
        pNum = new Stack<Integer>();
        assignToStack(pNum);
        pNumAtt = new Stack<Integer>();
        assignToStack(pNumAtt);

        loc = FXMLDocumentController.AnswerPath;
        File dir = new File(loc + fileName);
        if (!dir.exists()) {
            dir.mkdir();
        }
        //FileOutputStream output = new FileOutputStream(loc+fileName+"_interval.gzip");
        //Writer writer = new OutputStreamWriter(new GZIPOutputStream(output), "UTF-8");
        fstream = new FileWriter(loc + File.separator + "Dewey.txt"); // element
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
//	System.out.println("Start Document");
    }

    public void endDocument() throws SAXException {

        try {
            out.get(0).close();
            out.get(1).close();
            out.get(2).close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //  	System.out.println("End Document");
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {

        elementStack.add(qName);
        totalElement++;
        cLevel = elementStack.size(); //current level
        LevelStack.push(cLevel);

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
            int x = childStack.get(childStack.size() - 1);
            x++;
            childStack.set(childStack.size() - 1, x);
            childStack.add(0);
            String label = parentLabel.get(parentLabel.size() - 1);
            parentLabel.add(label + "." + x);
        } else {
            childStack.add(0);
            parentLabel.add("1"); // root
        }

        try {
            out.get(0).write(parentLabel.peek());
            out.get(0).newLine();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        if (elementStack.size() > maxDepth) {
            maxDepth = elementStack.size();
        }

        ++order;
        OrderStack.push(order);

        if (attributes.getLength() > 0) {
            flags.push(LevelStack.peek()); // to indicate it has attribute
            E.push(qName);

            int i = 0;
            while (i < attributes.getLength()) {

                order++;
                totalAtt++;
                OrderStack.push(order);
                int x = childStack.get(childStack.size() - 1);
                x++;
                childStack.set(childStack.size() - 1, x);
                String label = parentLabel.get(parentLabel.size() - 1);
                try {
                    out.get(0).write(label + "." + x);
                    out.get(0).newLine();
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
                i++;
                OrderStack.pop();

                if ((elementStack.size() + 1) > maxDepth) {
                    maxDepth = elementStack.size() + 1;
                }
            }

        }

    }

    public void endElement(String uri, String localName,
            String qName) throws SAXException {

        if (!values.isEmpty() && values.peek().trim().length() > 0) {
            try {

                out.get(2).write(parentLabel.peek() + ":" + values.peek());
                out.get(2).newLine();
                values.pop();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        elementStack.pop();
        LevelStack.pop();
        OrderStack.pop();
        childStack.pop();
        parentLabel.pop();
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        // ignore white space
        String value = new String(ch, start, length).trim();
        if (value.length() == 0) {
            return;
        } else {
            values.push(value);
            // System.out.println("Value : " + new String(ch, start, length));
        }

    }

    public String currentElement() {
        return this.elementStack.peek();
    }

    public String currentElementParent() {
        if (this.elementStack.size() < 2) {
            return null;
        }
        return this.elementStack.get(this.elementStack.size() - 2);
    }

    public void PrimenumberList() {
        // ArrayList<Integer> PrimeList= new ArrayList<Integer>();
        for (int i = 3; i < 1000; i++) {
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
            temp.push((Integer) PrimeList.get(i));
        }
    }
}
