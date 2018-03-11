package app;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.jfoenix.controls.JFXColorPicker;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;

import enums.Inertia;
import enums.Integration;
import enums.Interpolation;
import enums.Style;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class MainController {
	//// FXML fields
	// ROOT 
    @FXML private VBox rootNode;

    // CHART
    @FXML private StackPane chartPane;
    private LineChart<Number, Number> lineChart;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    // Chart properties
    @FXML private JFXTextField chartTitle;
    @FXML private JFXTextField chartWidth;
    @FXML private JFXTextField chartHeight;
    // X-Axis properties
    @FXML private JFXTextField xAxisName;
    @FXML private JFXTextField xAxisTickSize;
    @FXML private JFXTextField xAxisMinRange;
    @FXML private JFXTextField xAxisMaxRange;
    // Y-Axis properties
    @FXML private JFXTextField yAxisName;
    @FXML private JFXTextField yAxisTickSize;
    @FXML private JFXTextField yAxisMinRange;
    @FXML private JFXTextField yAxisMaxRange;
    
    // TRACES
    @FXML private JFXListView<Trace> traceListView;
    @FXML private JFXTabPane traceTabPane;
    // Trace properties
    @FXML private JFXTextField traceName;
    @FXML private JFXComboBox<File> traceFile;
    @FXML private JFXComboBox<Integration> traceIntegration;
    @FXML private JFXComboBox<Interpolation> traceInterpolation;
    @FXML private JFXComboBox<Inertia> traceInertia;
    @FXML private JFXTextField traceMass;
    @FXML private JFXTextField traceMinX;
    @FXML private JFXTextField traceMaxX;
    @FXML private JFXTextField traceInitV;
    @FXML private JFXTextField traceStep;
    // Trace details
    @FXML private Label funcTypeLabel;
    @FXML private Label integrationTypeLabel;
    @FXML private Label stepSizeLabel;
    @FXML private Label iterationsLabel;
    @FXML private Label totalTimeLabel;
    @FXML private Label computationTimeLabel;
    @FXML private Label energyDifferenceLabel;
    
   // GRAPHS
    @FXML private JFXListView<Graph> graphListView;
    // Graph properties
    @FXML private JFXTextField graphName;
    @FXML private JFXComboBox<String> graphXData;
    @FXML private JFXComboBox<String> graphYData;
    @FXML private JFXComboBox<Trace> graphTrace;
    @FXML private JFXTextField graphMinX;
    @FXML private JFXTextField graphMaxX;
    // Graph layout
    @FXML private JFXColorPicker graphColor;
    @FXML private JFXSlider graphDetail;
    @FXML private JFXComboBox<Style> graphStyle;
    @FXML private JFXSlider graphWidth;
    @FXML private JFXToggleButton graphVisible; 
    
    //// NON-FXML FIELDS
    /**
     * Used in Bidirectional bindings to convert String <-> Double.
     * <dt>String to Double:</dt>
     * <li>Allows both ',' and '.' as decimal separator.
     * <li>Returns null if String is empty ("").
     * <br></br>
     * <dt>Double to String:</dt>
     * <li>Returns null if Double is null.
     */
    private StringConverter<Double> customStringConverter;
    
    /*
     * Cached traces and graphs, used to manage property bindings.
     */
    private Trace selectedTrace;
    private Trace prevTrace;
    private Graph selectedGraph;
    private Graph prevGraph;
    
    /*
     * Observable lists used in ListViews and ChoiceBoxes
     */
    private ObservableList<Trace> traceList;
    private ObservableList<Graph> graphList;
    private ObservableList<File> fileList;
    private ObservableList<String> dataList;
    
    /*
     * Listener list for cells within choiceBox
     */
    private List<ListCell<Trace>> listenerList = new ArrayList<>();
    
    // Initialization
    /**
     * Initializes application, called after FXML fields has been invoked.
     */
	@FXML private void initialize() {    
    	// Initialize converter (used for trace bindings)
    	customStringConverter = new StringConverter<>() {
			@Override
			public Double fromString(String arg0) {
				return (arg0.equals("")) ? null : Double.valueOf(arg0.replace(',', '.'));}
			
			@Override
			public String toString(Double arg0) {
				return (arg0 == null) ? null : String.valueOf(arg0);}
		};

    	initializeLists();
    	initializeTraceView();
    	initializeGraphView();
    	
    }

	/**
	 * Initializes trace view and adds default trace to listView. 
	 */
	private void initializeTraceView() {
		// Set default trace
    	traceList.add(new Trace());
    	traceListView.getSelectionModel().selectFirst();
    	
    	// Add name listener
		traceName.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				// Update ListView entries
				traceListView.refresh();
				
				// Update choicebox entries
				listenerList.stream()
					.filter(cell -> cell != null)
					.filter(cell -> cell.getItem() != null)
					.forEach(cell -> cell.setText(cell.getItem().getName()));
				graphTrace.getButtonCell().setText(selectedGraph.getTrace().getName());
			}
		});
    	
    	// Updaters
    	updateTraceView();
	}

	/**
	 * Initializes chart, sets default chart and graph properties.
	 */
	private void initializeGraphView() {
		// Initialize chart
		xAxis = new NumberAxis();
		yAxis = new NumberAxis();
		lineChart = new LineChart<>(xAxis, yAxis);
		
		// Set chart properties
		xAxis.setLabel("xAxis");
		yAxis.setLabel("yAxis");
		lineChart.setTitle("My Chart");
		lineChart.setCreateSymbols(true);
		lineChart.setAnimated(false);
		
		// Add chart to GUI
		chartPane.getChildren().setAll(lineChart);
		
		// Set default graph
		graphList.add(new Graph(selectedTrace));
		graphListView.getSelectionModel().selectFirst();
		
		// Set graph trace cell factory
		graphTrace.setCellFactory(new Callback<ListView<Trace>, ListCell<Trace>>(){
			@Override
            public ListCell<Trace> call(ListView<Trace> arg) {
                ListCell<Trace> cell = new JFXListCell<Trace>(); 
                listenerList.add(cell);
                return cell;
            }
		});

		// Add name listener
		graphName.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				graphListView.refresh();
			}
		});
		
		// Updaters
		updateGraphView();
	}
	
	/**
     * Initializes and binds ListViews, loads imported files and initializes ChoiceBoxes.
     */
    private void initializeLists() {
    	// Initialize observable lists
    	traceList = FXCollections.observableArrayList();
    	graphList = FXCollections.observableArrayList();
    	fileList = FXCollections.observableArrayList();
    	dataList = FXCollections.observableList(Arrays.asList(Trace.MAP_KEYS));
    	
    	// Import tracker files from 'import'
    	File importFolder = new File(getClass().getResource("../imports").getPath());
    	FilenameFilter fileFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".txt");
			}
		};
    	for (File dataFile : importFolder.listFiles(fileFilter))
    		fileList.add(dataFile);
    	
    	// Bind observable lists
    	traceListView.setItems(traceList);
    	graphListView.setItems(graphList);
    	traceFile.setItems(fileList);
    	
    	// Fill trace choiceBoxes
    	traceIntegration.setItems(FXCollections.observableList(Integration.getElements()));
        traceInterpolation.setItems(FXCollections.observableList(Interpolation.getElements()));
        traceInertia.setItems(FXCollections.observableList(Inertia.getElements()));
        
        // Fill graph choiceBoxes
        graphTrace.setItems(traceList);
        graphXData.setItems(dataList);
        graphYData.setItems(dataList);
        graphStyle.setItems(FXCollections.observableList(Style.getElements()));
    }
    
    
    // GUI Update
    /**
     * Manages trace bindings and updates trace details.
     * Called when selected trace has been changed.
     */
	private void updateTraceView() {
		// Retrive selected list entry
		selectedTrace = traceListView.getSelectionModel().getSelectedItem();
		
		// Unbind previous trace
		if (prevTrace != null)
			unbindTrace();
		
		// Cache selected trace
		prevTrace = selectedTrace;
		
		// Bind selected trace
		bindTrace();
    }
    
	/**
     * Manages graph bindings and updates graph details.
     * Called when selected graph has been changed.
     */
	private void updateGraphView() {
		// Retrive selected list entry
		selectedGraph = graphListView.getSelectionModel().getSelectedItem();
		
		System.out.println("Selected item: " + selectedGraph);
		
		// Unbind previous graph
		if (prevGraph != null)
			unbindGraph();
		
		// Cache selected graph
		prevGraph = selectedGraph;
		
		// If there is no selected graph, break
		if (selectedGraph == null) {
			clearGraphView();
			return;
		}
		
		// Bind selected graph
		bindGraph();
		
//		// Prevent duplicate graphs
//		if (!lineChart.getData().contains(selectedGraph.getSeries()))
//			lineChart.getData().add(selectedGraph.getSeries());
		
//		// Fix graph order
//		graphList.forEach(g -> g.getSeries().getNode().setViewOrder(graphList.indexOf(g)));
    	
    	// Update chart
    	lineChart.getData().setAll(graphList.stream().map(graph -> graph.getSeries()).collect(Collectors.toList()));
    	
    	// Update graphs
    	graphList.forEach(graph -> graph.updateGraph());
    	
    	// Update chart layout
    	updateChartStyles();
    }

	
	/**
	 * Clears graph view if there are no graphs to display.
	 */
	private void clearGraphView() {
		// Clear graph properties
		graphName.setText(null);
		graphXData.setValue(null);
		graphYData.setValue(null);
		graphTrace.setValue(null);
		graphMinX.setText(null);
		graphMaxX.setText(null);
		
		// Clear graph layout properties
		graphStyle.setValue(null);
		graphColor.setValue(Color.valueOf("#FFFFFF"));
		graphDetail.setValue(50);
		graphWidth.setValue(50);
		graphVisible.setSelected(false);
	}
	
	// Manage bindings
	/**
	 * Bind TraceView inputs to selected trace.
	 */
	private void bindTrace() {
		// Bind trace properties
		traceName					.textProperty().bindBidirectional(selectedTrace.getNameProperty());
	    traceFile					.valueProperty().bindBidirectional(selectedTrace.getFileProperty());
	    traceIntegration			.valueProperty().bindBidirectional(selectedTrace.getIntegrationProperty());
	    traceInterpolation			.valueProperty().bindBidirectional(selectedTrace.getInterpolationProperty());
	    traceInertia				.valueProperty().bindBidirectional(selectedTrace.getInertiaProperty());
	    traceMass					.textProperty().bindBidirectional(selectedTrace.getMassProperty(), customStringConverter);
	    traceMinX					.textProperty().bindBidirectional(selectedTrace.getMinXProperty(), customStringConverter);
	    traceMaxX					.textProperty().bindBidirectional(selectedTrace.getMaxXProperty(), customStringConverter);
	    traceInitV					.textProperty().bindBidirectional(selectedTrace.getInitVProperty(), customStringConverter);
	    traceStep					.textProperty().bindBidirectional(selectedTrace.getStepProperty(), customStringConverter);
		
		// Set trace details
	    funcTypeLabel.textProperty().bind(selectedTrace.getInterpolationTypeProperty());
	    integrationTypeLabel.textProperty().bind(selectedTrace.getIntegrationTypeProperty());
	    stepSizeLabel.textProperty().bind(selectedTrace.getStepSizeProperty());
	    iterationsLabel.textProperty().bind(selectedTrace.getIterationsProperty());
	    totalTimeLabel.textProperty().bind(selectedTrace.getTotalTimeProperty());
	    computationTimeLabel.textProperty().bind(selectedTrace.getComputationTimeProperty());
	    energyDifferenceLabel.textProperty().bind(selectedTrace.getEnergyDifferenceProperty());
	}
	
	/**
	 * Bind GraphView inputs to selected graph.
	 */
	private void bindGraph() {
		// Bind graph properties
		graphName					.textProperty().bindBidirectional(selectedGraph.getNameProperty());
		graphXData					.valueProperty().bindBidirectional(selectedGraph.getXDataProperty());
		graphYData					.valueProperty().bindBidirectional(selectedGraph.getYDataProperty());
		graphTrace					.valueProperty().bindBidirectional(selectedGraph.getTraceProperty());
		graphMinX					.textProperty().bindBidirectional(selectedGraph.getMinXProperty(), customStringConverter);
		graphMaxX					.textProperty().bindBidirectional(selectedGraph.getMaxXProperty(), customStringConverter);
		

		
		// Bind graph layout properties
		graphStyle					.valueProperty().bindBidirectional(selectedGraph.getStyleProperty());
		graphColor					.valueProperty().bindBidirectional(selectedGraph.getColorProperty());
		graphDetail					.valueProperty().bindBidirectional(selectedGraph.getDetailProperty());
		graphWidth					.valueProperty().bindBidirectional(selectedGraph.getWidthProperty());
		graphVisible				.selectedProperty().bindBidirectional(selectedGraph.getVisibleProperty());
	}
	
	/**
	 * Unbinds TraceView inputs from previously selected trace.
	 */
	private void unbindTrace() {
		// Unbind trace properties
		traceName				.textProperty().unbindBidirectional(prevTrace.getNameProperty());
		traceFile				.valueProperty().unbindBidirectional(prevTrace.getFileProperty());
		traceIntegration		.valueProperty().unbindBidirectional(prevTrace.getIntegrationProperty());
		traceInterpolation		.valueProperty().unbindBidirectional(prevTrace.getInterpolationProperty());
		traceInertia			.valueProperty().unbindBidirectional(prevTrace.getInertiaProperty());
		traceMass				.textProperty().unbindBidirectional(prevTrace.getMassProperty());
		traceMinX				.textProperty().unbindBidirectional(prevTrace.getMinXProperty());
		traceMaxX				.textProperty().unbindBidirectional(prevTrace.getMaxXProperty());
		traceInitV				.textProperty().unbindBidirectional(prevTrace.getInitVProperty());
		traceStep				.textProperty().unbindBidirectional(prevTrace.getStepProperty());
	}
	
	/**
	 * Unbinds GraphView inputs from previously selected graph.
	 */
	private void unbindGraph() {
		// Unbind graph properties
		graphName				.textProperty().unbindBidirectional(prevGraph.getNameProperty());
		graphXData				.valueProperty().unbindBidirectional(prevGraph.getXDataProperty());
		graphYData				.valueProperty().unbindBidirectional(prevGraph.getYDataProperty());
		graphTrace				.valueProperty().unbindBidirectional(prevGraph.getTraceProperty());
		graphMinX				.textProperty().unbindBidirectional(prevGraph.getMinXProperty());
		graphMaxX				.textProperty().unbindBidirectional(prevGraph.getMaxXProperty());
		
		// Unbind graph layout properties
		graphStyle				.valueProperty().unbindBidirectional(prevGraph.getStyleProperty());
		graphColor				.valueProperty().unbindBidirectional(prevGraph.getColorProperty());
		graphDetail				.valueProperty().unbindBidirectional(prevGraph.getDetailProperty());
		graphWidth				.valueProperty().unbindBidirectional(prevGraph.getWidthProperty());
		graphVisible			.selectedProperty().unbindBidirectional(prevGraph.getVisibleProperty());
	}
    
	/**
	 * Update chart layout in order to maintain dot colors
	 */
	private void updateChartStyles() {
//		String nodeStyle = String.format("-fx-background-color: #%s, #FFFFFF; -fx-background-insets: 0, 2;", "ff0000");
//		lineChart.lookupAll(".chart-legend-item").stream()
		// chart-legend-item-symbol chart-line-symbol series0 default-color0
//		.map(elem -> ((Labeled) elem).getGraphic());
//		.forEach(elem -> ((Labeled) elem).getGraphic().getStyleClass().get(3));
		
		for (Node node : lineChart.lookupAll(".chart-legend-item")) {
			Labeled labeledNode = (Labeled) node;
			Node graphicNode = labeledNode.getGraphic();
			String nodeSeries = graphicNode.getStyleClass().get(2);
			int seriesIndex = Character.getNumericValue(nodeSeries.charAt(nodeSeries.length() - 1));
			
			String graphStyle =  String.format("-fx-background-color: #%s, #FFFFFF; -fx-background-insets: 0, 2;", graphList.get(seriesIndex).getHexColor());
			
			graphicNode.setStyle(graphStyle);
		}
		
//		.forEach(elem -> ((Labeled) elem).getGraphic().setStyle(nodeStyle));
//		String dotStyle = String.format("-fx-background-color: #%s;", graphList.get(i).getHexColor());
//		System.out.println(nodeSet);
//		nodeSet.forEach(item -> item.setStyle(String.format("-fx-background-color: #000000;")));
//		List<Node> nodeList = nodeSet.stream()
//				.filter(test -> ((Parent) test).getChildrenUnmodifiable().size() != 0)
//				.map(parent -> ((Parent) parent).getChildrenUnmodifiable().get(0)).collect(Collectors.toList());
		
//		System.out.println("Series are: " + lineChart.getData() + " Size: " + lineChart.getData().size());
//		System.out.println("Hei");
//		ObservableList<Node> nodeList = treeTraversal(lineChart, 2).getChildrenUnmodifiable();
////		Set<Node> nodelist = lineChart.lookupAll(".default-color0");
////		System.out.println("Nodes found: " + nodelist + " Size: " + nodelist.size());
//		System.out.println("Node found: " + nodeList);
//		if (nodeList.size() == 2)
//			System.out.println();;
//			for (int i = 0; i < nodeList.size(); i++) {
//				if (nodeList.get(i).lookup(".default-color0") != null)
//					nodeList.get(i).lookup(".default-color0").setStyle("-fx-background-color: #000000;");
//				System.out.println("Children: " + ((Parent) nodeList.get(i)).getChildrenUnmodifiable());
//				
//			}
		
//						.stream()
////						.filter(node -> node instanceof Label)
//						.forEach(node -> node.setStyle("-fx-background-color: #000000"));
//			.forEach(elem -> System.out.println("Hei"));
	}
	private Parent treeTraversal(Parent parent, int index) {
		return (Parent) parent.getChildrenUnmodifiable().get(index);
	}
    //// Button handlers
    // Main menu button handlers
    @FXML private void handleNewProjectClick(ActionEvent event) {}
    
    @FXML private void handleSaveClick(ActionEvent event) {}
    
    @FXML private void handleLoadClick(ActionEvent event) {}
    
    @FXML private void handleImportClick(ActionEvent event) {}
    
    @FXML private void handleExportClick(ActionEvent event) {}
    
    
    // Trace button handlers
    @FXML private void handleNewTraceClick(ActionEvent event) {
    	// Add ned trace
    	traceList.add(new Trace());
    	
    	// Select new trace
    	traceListView.getSelectionModel().selectLast();
    	
    	// Update
    	updateTraceView();
    }
    
    @FXML private void handleDeleteTraceClick(ActionEvent event) {
    	// Update
    	updateGraphView();
    }
    
    @FXML private void handleComputeClick(ActionEvent event) {
    	// Select trace details view
    	traceTabPane.getSelectionModel().selectLast();
    	
    	// Run trace in parallel
    	selectedTrace.parallelTrace();
    	
    	// Update
    	updateTraceView();
    }
    
    @FXML private void handleComputeAllClick(ActionEvent event) {
    	// Select trace details view
    	traceTabPane.getSelectionModel().selectLast();
    	
    	// Run all traces in parallel
    	traceList.forEach(trace -> trace.parallelTrace());
    	
    	// Update
    	updateTraceView();
    }
    
    @FXML private void handleTraceListClick(Event event) {
    	// Update
    	updateTraceView();
    }
    
    
    // Graph button handlers
    @FXML private void handleNewGraphClick(ActionEvent event) {
    	// Add new graph
    	graphList.add(new Graph(selectedTrace));
    	
    	// Select new graph
    	graphListView.getSelectionModel().selectLast();
    	
    	// Update
    	updateGraphView();
    }

    @FXML private void handleDeleteGraphClick(ActionEvent event) {
    	// If there is no selected graph, break
    	if (selectedGraph == null) return;
    	
    	// Remove property bindings
    	selectedGraph.removeTraceLink();
    	
    	// Remove graph from series
    	if (lineChart.getData().contains(selectedGraph.getSeries()))
    		lineChart.getData().remove(selectedGraph.getSeries());
    	
    	// Remove graph
    	graphList.remove(selectedGraph);
    	
    	// Update
    	updateGraphView();
    }
    
    @FXML private void handleGraphUpClick(ActionEvent event) {
    	// Assign local variable
    	Graph graph;
    	int graphIndex;
    	
    	// If there is no selected graph, break
    	if (selectedGraph == null) return;
    	
    	// Retrive graph index
    	graphIndex = graphList.indexOf(selectedGraph);
    	
    	// If graph already has highest priority, break
    	if (graphIndex == 0) return;
    	
    	// Remove graph from list
    	graph = graphList.remove(graphIndex);
    	
    	// Add graph at new index
    	graphList.add(graphIndex - 1, graph);
    	
    	// Reselect moved graph
    	graphListView.getSelectionModel().select(graph);
    	
    	// Update
    	updateGraphView();
    }
    
    @FXML private void handleGraphDownClick(ActionEvent event) {
    	// Assign local variable
    	Graph graph;
    	int graphIndex;
    	
    	// If there is no selected graph, break
    	if (selectedGraph == null) return;
    	
    	// Retrive graph index
    	graphIndex = graphList.indexOf(selectedGraph);
    	
    	// If graph already has lowest priority, break
    	if (graphIndex == graphList.size() - 1) return;
    	
    	// Remove graph from list
    	graph = graphList.remove(graphIndex);
    	
    	// Add graph at new index
    	graphList.add(graphIndex + 1, graph);
    	
    	// Reselect moved graph
    	graphListView.getSelectionModel().select(graph);
    	
    	// Update
    	updateGraphView();
    }
    
    @FXML private void handleGraphListClick(Event event) {
    	// Update
    	updateGraphView();
    }
    
    
    // Other
    // Trace file opener
    @FXML private void handleFileOpenClick(ActionEvent event) {
    	// Retrive parent for file chooser
    	Stage mainStage = (Stage) rootNode.getScene().getWindow();
    	
    	// Construct file chooser
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Open Data File");
    	fileChooser.getExtensionFilters().addAll(
    	         new ExtensionFilter("Tracker file (*.txt)", "*.txt"),
    	         new ExtensionFilter("All Files", "*.*"));
    	
    	// Launch file chooser and retrive selected file
    	File selectedFile = fileChooser.showOpenDialog(mainStage);
    	
    	// Add file to fileList and update trace file
    	fileList.add(selectedFile);
    	selectedTrace.setFile(selectedFile);
    }
    
    
    
    
}
