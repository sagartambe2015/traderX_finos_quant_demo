//package com.example;
//
//
//import javafx.application.Application;
//import javafx.application.Platform;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.layout.*;
//import javafx.stage.Stage;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.scene.control.cell.PropertyValueFactory;
//import javafx.concurrent.Task;
//
//public class TradingJavaFXApp extends Application {
//    private TextArea outputArea;
//    private TableView<OrderEntry> orderEntryTable;
//    private ObservableList<OrderEntry> orderEntries;
//
//    @Override
//    public void start(Stage primaryStage) {
//        primaryStage.setTitle("Trading Automation - JavaFX");
//
//        // Create main layout
//        BorderPane mainLayout = new BorderPane();
//
//        // Create top control panel
//        HBox controlPanel = createControlPanel();
//        mainLayout.setTop(controlPanel);
//
//        // Create center content with grid layout
//        GridPane centerGrid = createCenterGrid();
//        mainLayout.setCenter(centerGrid);
//
//        // Create scene
//        Scene scene = new Scene(mainLayout, 1000, 700);
//        primaryStage.setScene(scene);
//        primaryStage.show();
//    }
//
//    private HBox createControlPanel() {
//        HBox controlPanel = new HBox(10);
//        controlPanel.setPadding(new Insets(10));
//        controlPanel.setAlignment(Pos.CENTER_LEFT);
//        controlPanel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc;");
//
//        Button btnProfile = new Button("Get Profile");
//        Button btnFunds = new Button("Get Funds");
//        Button btnHoldings = new Button("Get Holdings");
//        Button btnOptionCalc = new Button("Run Option Calculation");
//
//        // Style buttons
//        String buttonStyle = "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;";
//        btnProfile.setStyle(buttonStyle);
//        btnFunds.setStyle(buttonStyle);
//        btnHoldings.setStyle(buttonStyle);
//        btnOptionCalc.setStyle(buttonStyle);
//
//        // Add event handlers
//        btnProfile.setOnAction(e -> runBackendOperation("profile"));
//        btnFunds.setOnAction(e -> runBackendOperation("funds"));
//        btnHoldings.setOnAction(e -> runBackendOperation("holdings"));
//        btnOptionCalc.setOnAction(e -> runBackendOperation("option"));
//
//        controlPanel.getChildren().addAll(btnProfile, btnFunds, btnHoldings, btnOptionCalc);
//        return controlPanel;
//    }
//
//    private GridPane createCenterGrid() {
//        GridPane grid = new GridPane();
//        grid.setHgap(10);
//        grid.setVgap(10);
//        grid.setPadding(new Insets(10));
//
//        // Panel 1: Order Entry Table (Top Left)
//        VBox tablePanel = createOrderEntryTable();
//        grid.add(tablePanel, 0, 0);
//
//        // Panel 2: Output Area (Top Right)
//        VBox outputPanel = createOutputPanel();
//        grid.add(outputPanel, 1, 0);
//
//        // Panel 3: Status Panel (Bottom Left)
//        VBox statusPanel = createStatusPanel();
//        grid.add(statusPanel, 0, 1);
//
//        // Panel 4: Additional Info (Bottom Right)
//        VBox infoPanel = createInfoPanel();
//        grid.add(infoPanel, 1, 1);
//
//        return grid;
//    }
//
//    private VBox createOrderEntryTable() {
//        VBox panel = new VBox(10);
//        panel.setPadding(new Insets(10));
//        panel.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5;");
//
//        Label titleLabel = new Label("Order Entry Results");
//        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
//
//        // Create table
//        orderEntryTable = new TableView<>();
//        orderEntries = FXCollections.observableArrayList();
//        orderEntryTable.setItems(orderEntries);
//
//        // Create columns
//        TableColumn<OrderEntry, String> strikeCol = new TableColumn<>("Strike");
//        strikeCol.setCellValueFactory(new PropertyValueFactory<>("strike"));
//        strikeCol.setPrefWidth(150);
//
//        TableColumn<OrderEntry, Double> entryPriceCol = new TableColumn<>("Entry Price");
//        entryPriceCol.setCellValueFactory(new PropertyValueFactory<>("entryPrice"));
//        entryPriceCol.setPrefWidth(100);
//
//        TableColumn<OrderEntry, Double> targetCol = new TableColumn<>("Target");
//        targetCol.setCellValueFactory(new PropertyValueFactory<>("target"));
//        targetCol.setPrefWidth(100);
//
//        TableColumn<OrderEntry, Double> stopLossCol = new TableColumn<>("Stop Loss");
//        stopLossCol.setCellValueFactory(new PropertyValueFactory<>("stopLoss"));
//        stopLossCol.setPrefWidth(100);
//
//        TableColumn<OrderEntry, Double> trailingSLCol = new TableColumn<>("Trailing SL");
//        trailingSLCol.setCellValueFactory(new PropertyValueFactory<>("trailingSL"));
//        trailingSLCol.setPrefWidth(100);
//
//        orderEntryTable.getColumns().addAll(strikeCol, entryPriceCol, targetCol, stopLossCol, trailingSLCol);
//        orderEntryTable.setPrefHeight(200);
//
//        panel.getChildren().addAll(titleLabel, orderEntryTable);
//        return panel;
//    }
//
//    private VBox createOutputPanel() {
//        VBox panel = new VBox(10);
//        panel.setPadding(new Insets(10));
//        panel.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5;");
//
//        Label titleLabel = new Label("Console Output");
//        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
//
//        outputArea = new TextArea();
//        outputArea.setEditable(false);
//        outputArea.setPrefHeight(200);
//        outputArea.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 12;");
//
//        panel.getChildren().addAll(titleLabel, outputArea);
//        return panel;
//    }
//
//    private VBox createStatusPanel() {
//        VBox panel = new VBox(10);
//        panel.setPadding(new Insets(10));
//        panel.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5;");
//
//        Label titleLabel = new Label("Status");
//        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
//
//        Label statusLabel = new Label("Ready");
//        statusLabel.setStyle("-fx-font-size: 14; -fx-text-fill: green;");
//
//        ProgressIndicator progressIndicator = new ProgressIndicator();
//        progressIndicator.setVisible(false);
//
//        panel.getChildren().addAll(titleLabel, statusLabel, progressIndicator);
//        return panel;
//    }
//
//    private VBox createInfoPanel() {
//        VBox panel = new VBox(10);
//        panel.setPadding(new Insets(10));
//        panel.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5;");
//
//        Label titleLabel = new Label("Additional Information");
//        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
//
//        TextArea infoArea = new TextArea();
//        infoArea.setEditable(false);
//        infoArea.setPrefHeight(100);
//        infoArea.setText("Additional trading information will appear here...");
//
//        panel.getChildren().addAll(titleLabel, infoArea);
//        return panel;
//    }
//
//    private void runBackendOperation(String operation) {
//        Task<Void> task = new Task<>() {
//            @Override
//            protected Void call() throws Exception {
//                try {
//                    switch (operation) {
//                        case "profile":
//                            String profileResult = FyersTraderApi.getInstance().GetProfile();
//                            Platform.runLater(() -> outputArea.appendText("Profile fetched successfully.\n" + profileResult + "\n"));
//                            break;
//                        case "funds":
//                            String fundsResult = FyersTraderApi.getInstance().GetFunds();
//                            Platform.runLater(() -> outputArea.appendText("Funds fetched successfully.\n" + fundsResult + "\n"));
//                            break;
//                        case "holdings":
//                            String holdingsResult = FyersTraderApi.getInstance().GetHoldings();
//                            Platform.runLater(() -> outputArea.appendText("Holdings fetched successfully.\n" + holdingsResult + "\n"));
//                            break;
//                        case "option":
//                            Platform.runLater(() -> {
//                                orderEntries.clear();
//                                addSampleOrderEntries();
//                                outputArea.appendText("Option calculation completed. Check Order Entry table.\n");
//                            });
//                            break;
//                    }
//                } catch (Exception ex) {
//                    Platform.runLater(() -> outputArea.appendText("Error: " + ex.getMessage() + "\n"));
//                }
//                return null;
//            }
//        };
//
//        // Show progress indicator
//        ProgressIndicator progressIndicator = (ProgressIndicator) ((VBox) ((GridPane) ((BorderPane) getScene().getRoot()).getCenter()).getChildren().get(2)).getChildren().get(2);
//        progressIndicator.setVisible(true);
//
//        task.setOnSucceeded(e -> progressIndicator.setVisible(false));
//        task.setOnFailed(e -> progressIndicator.setVisible(false));
//
//        new Thread(task).start();
//    }
//
//    private void addSampleOrderEntries() {
//        orderEntries.add(new OrderEntry("NIFTY50 19000 CE", 150.50, 255.85, 45.15, 45.15));
//        orderEntries.add(new OrderEntry("NIFTY50 19000 PE", 120.75, 205.28, 36.23, 36.23));
//        orderEntries.add(new OrderEntry("NIFTY50 19100 CE", 95.25, 161.93, 28.58, 28.58));
//    }
//
//    // Data model class for table
//    public static class OrderEntry {
//        private final String strike;
//        private final double entryPrice;
//        private final double target;
//        private final double stopLoss;
//        private final double trailingSL;
//
//        public OrderEntry(String strike, double entryPrice, double target, double stopLoss, double trailingSL) {
//            this.strike = strike;
//            this.entryPrice = entryPrice;
//            this.target = target;
//            this.stopLoss = stopLoss;
//            this.trailingSL = trailingSL;
//        }
//
//        // Getters
//        public String getStrike() { return strike; }
//        public double getEntryPrice() { return entryPrice; }
//        public double getTarget() { return target; }
//        public double getStopLoss() { return stopLoss; }
//        public double getTrailingSL() { return trailingSL; }
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//}