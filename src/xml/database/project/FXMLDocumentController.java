/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xml.database.project;

import DataBase.DataBaseManger;
import Models.Node;
import Parsing.IntervalLabelling;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.StageStyle;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 *
 * @author SAlbr
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private TextField path1;
    @FXML
    private TextField path2;

    @FXML
    private CheckBox check1;
    @FXML
    private CheckBox check2;
    @FXML
    private Label d1;

    @FXML
    private Label d2;

    @FXML
    private Label d3;

    @FXML
    private Label d4;

    @FXML
    private Label d5;
    @FXML
    private Label time;

    @FXML
    private Tab StatisticsTab;

    @FXML
    private Tab barchartTab;
    @FXML
    private PieChart piechart;
    @FXML
    private TextArea uniqueNodeText;
    @FXML
    private TextArea uniquePathText;
    @FXML
    private TextArea queryArea;

    @FXML
    private TextArea resultArea;
    @FXML
    private Tab Querytab;

    public static String DatabasePath = "";
    public static File DatabaseFile;
    public static String AnswerPath = "";
    public Document Uniquedoc;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        path1.setEditable(false);
        path2.setEditable(false);

    }

    @FXML
    void UploadDatabase(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML", "*.xml");
        fileChooser.setTitle("Upload Database");
        fileChooser.getExtensionFilters().add(extFilter);
        File selectedFile = fileChooser.showOpenDialog(XMLDatabaseProject.getStage());
        if (selectedFile != null) {
            DatabaseFile = selectedFile;
            DatabasePath = selectedFile.getAbsolutePath();
            path1.setText(DatabasePath);
            check1.setDisable(false);
            check1.setSelected(true);
            check1.setDisable(true);

        }

    }

    @FXML
    void Outputfolder(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Output Folder");
        File selectedFile = chooser.showDialog(XMLDatabaseProject.getStage());
        if (selectedFile != null) {
            AnswerPath = selectedFile.getAbsolutePath();
            path2.setText(AnswerPath);
            check2.setDisable(false);
            check2.setSelected(true);
            check2.setDisable(true);

        }

    }

    @FXML
    void StartTheProgram(ActionEvent event) {
        try {
            long start = System.currentTimeMillis();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser sAXParser = factory.newSAXParser();
            IntervalLabelling handler = new IntervalLabelling("IntervalLabelling");

            sAXParser.parse(DatabasePath, handler);
            long end = System.currentTimeMillis();

            NumberFormat formatter = new DecimalFormat("#0.00000");
            time.setText("Execution time is : " + formatter.format((end - start) / 1000d) + " seconds ");

            d1.setText("Number of  elements : " + handler.detali[0]);
            d2.setText("Numberof  attributes : " + handler.detali[1]);
            d3.setText("Number of inverted list : " + handler.detali[2]);
            d4.setText("Number of internal nodes : " + handler.internal);
            d5.setText("Number of leaf nodes :  " + handler.leaf);
            Object arr[] = handler.UniSet.toArray();
            long total = handler.internal + handler.leaf;
            String[] stringArray = Arrays.copyOf(arr, arr.length, String[].class);

            String path = "";

            for (int i = 0; i < stringArray.length; i++) {

                path = path + stringArray[i] + "\n\n";

            }
            uniquePathText.setText(path);
            Uniquedoc = handler.doc;
            ArrayList<Node> count = handler.nodeCount;
            drawBarchart(count);
            drawPiechart(count, total);

            StatisticsTab.setDisable(false);
            Querytab.setDisable(false);
            uniqueNodeText.setText(handler.xmlString);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void drawBarchart(ArrayList<Models.Node> nodeCount) {
        SwingNode swingNode = new SwingNode();
        SwingUtilities.invokeLater(() -> {

            DefaultCategoryDataset d = new DefaultCategoryDataset();
            for (int i = 0; i < nodeCount.size(); i++) {
                d.setValue(nodeCount.get(i).getNum(), nodeCount.get(i).getName(), nodeCount.get(i).getName());

            }

            JFreeChart jchChart = ChartFactory.createBarChart3D("", "Nodes Name", "Counts", d, PlotOrientation.VERTICAL, true, true, true);

            CategoryPlot plot = jchChart.getCategoryPlot();
            plot.setRangeGridlinePaint(Color.WHITE);

//            ChartFrame chrtFrame = new ChartFrame("Saleh", jchChart, true);
//            chrtFrame.setVisible(true);
//            chrtFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            // jchChart.getPlot().setBackgroundPaint(Color.WHITE);
            ChartPanel chartPanel = new ChartPanel(jchChart);

            JPanel p = new JPanel();
            p.setPreferredSize(new Dimension(1161, 870));
            chartPanel.setPreferredSize(new Dimension(p.getPreferredSize()));
            p.removeAll();
            p.add(chartPanel);
            p.updateUI();
            swingNode.setContent(p);

        });
        barchartTab.setContent(swingNode);
    }

    private void drawPiechart(ArrayList<Node> count, long total) {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        for (int i = 0; i < count.size(); i++) {
            data.add(new PieChart.Data(count.get(i).getName(), count.get(i).getNum()));

        }
        piechart.setData(data);
        piechart.getData().stream().forEach(d -> {
            Tooltip tooltip = new Tooltip();

            tooltip.setText(String.format("%.2f", (double) (d.getPieValue() * 100) / total) + "%");

            Tooltip.install(d.getNode(), tooltip);
//            d.pieValueProperty().addListener((observable, oldValue, newValue)
//                    -> System.out.println("123"));
        });
    }

    @FXML
    void queryRunMethod(ActionEvent event) {
        String query = queryArea.getText();
        NodeList nodes = XPath(query);
        if (nodes == null) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("ERROR");
            alert.setHeaderText(null);
            alert.setContentText("ERROR IN THE QUERY");
            alert.showAndWait();
            return;
        }
        if (nodes.getLength() == 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.UTILITY);
            alert.setHeaderText(null);
            alert.setTitle("ERROR");
            alert.setContentText("ERROR IN THE SCHEMA");

            alert.showAndWait();
            return;
        }
//        DataBase.DataBaseManger db = new DataBaseManger();
//        db.query(query);

    }

    public NodeList XPath(String query) {
        try {
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile(query);
            NodeList nodes = (NodeList) expr.evaluate(Uniquedoc, XPathConstants.NODESET);
            return nodes;
        } catch (XPathExpressionException ex) {

        }
        return null;
    }

}
