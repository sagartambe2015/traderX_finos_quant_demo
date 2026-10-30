package com.example;

import kotlin.Pair;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;

public class TradingSwingApp extends JFrame {
    private JTextArea outputArea;
    private JTable orderEntryTable;
    private DefaultTableModel tableModel;
    JSpinner dateSpinnerExpiry = new JSpinner(new SpinnerDateModel());
    JSpinner dateSpinnerDateToCalculate = new JSpinner(new SpinnerDateModel());

    public TradingSwingApp() {
        setTitle("Trading Automation");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 2, 10, 10));

        // Panel 1: Control Buttons (Top Left)
        JPanel controlPanel = new JPanel();
        controlPanel.setBorder(BorderFactory.createTitledBorder("Controls"));
        controlPanel.setLayout(new GridBagLayout());

        JLabel lblExpiry = new JLabel();
        lblExpiry.setText("Select Expiry Date:");



        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinnerExpiry, "yyyy-MM-dd");
        dateSpinnerExpiry.setEditor(dateEditor);


        controlPanel.add(lblExpiry);
        controlPanel.add(dateSpinnerExpiry); // Add at the top of the panel

        JLabel lblDate = new JLabel();
        lblDate.setText("Select Date:");


        JSpinner.DateEditor dateEditorDateSpinnerDateToCalculate = new JSpinner.DateEditor(dateSpinnerDateToCalculate, "yyyy-MM-dd");
        dateSpinnerDateToCalculate.setEditor(dateEditorDateSpinnerDateToCalculate);

        controlPanel.add(lblDate);
        controlPanel.add(dateSpinnerDateToCalculate);


        JButton btnProfile = new JButton("Get Profile");
        JButton btnFunds = new JButton("Get Funds");
        JButton btnHoldings = new JButton("Get Holdings");
        JButton btnOptionCalc = new JButton("Run Option Calculation");
        JButton btnOptionSellMonthlyCalc = new JButton("Run Option Sell Monthly Calculation");

        controlPanel.add(btnProfile);
        controlPanel.add(btnFunds);
        controlPanel.add(btnHoldings);
        controlPanel.add(btnOptionCalc);
        controlPanel.add(btnOptionSellMonthlyCalc);

        add(controlPanel);

        // Panel 2: Order Entry Table (Top Right)
        JPanel tablePanel = new JPanel();
        tablePanel.setBorder(BorderFactory.createTitledBorder("Order Entry Results"));
        tablePanel.setLayout(new BorderLayout());

        // Create table model with columns
        String[] columnNames = {"Strike", "Entry Price", "Target", "Stop Loss", "Trailing SL", "Final Stop Loss"};
        tableModel = new DefaultTableModel(columnNames, 0);
        orderEntryTable = new JTable(tableModel);

        JScrollPane tableScrollPane = new JScrollPane(orderEntryTable);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);

        add(tablePanel);

        // Panel 3: Output Area (Bottom Left)
        JPanel outputPanel = new JPanel();
        outputPanel.setBorder(BorderFactory.createTitledBorder("Console Output"));
        outputPanel.setLayout(new BorderLayout());

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        outputPanel.add(outputScrollPane, BorderLayout.CENTER);




        add(outputPanel);

        // Panel 4: Status/Info (Bottom Right)
        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(BorderFactory.createTitledBorder("Status"));
        statusPanel.setLayout(new BorderLayout());

        JLabel statusLabel = new JLabel("Ready");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusPanel.add(statusLabel, BorderLayout.CENTER);

        add(statusPanel);

        // Event handlers
        btnProfile.addActionListener(e -> runBackendOperation("profile"));
        btnFunds.addActionListener(e -> runBackendOperation("funds"));
        btnHoldings.addActionListener(e -> runBackendOperation("holdings"));
        btnOptionCalc.addActionListener(e -> runBackendOperation("option_buy_expiry"));
        btnOptionSellMonthlyCalc.addActionListener(e -> runBackendOperation("option_sell_monthly"));

        setVisible(true);
    }

    private void runBackendOperation(String operation) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {

                    switch (operation) {
                        case "profile":

                            outputArea.append("Profile fetched successfully.\n"+ FyersTraderApi.getInstance().GetProfile());
                            break;
                        case "funds":

                            outputArea.append("Funds fetched successfully.\n"+ FyersTraderApi.getInstance().GetFunds());
                            break;
                        case "holdings":

                            outputArea.append("Holdings fetched successfully.\n"+ FyersTraderApi.getInstance().GetHoldings());
                            break;
                        case "option_buy_expiry":
                            run_calculation_for_option_buy_expiry();
                            break;
                        case "option_sell_monthly":
                            // Clear existing table data
                            run_calculation_for_option_sell_monthly();
                            break;

                    }
                } catch (Exception ex) {
                    outputArea.append("Error: " + ex.getMessage() + "\n");
                }
                return null;
            }

            private void run_calculation_for_option_sell_monthly() {
                tableModel.setRowCount(tableModel.getRowCount()+1);

                Date dateForCalculation=(Date) dateSpinnerDateToCalculate.getValue();
                String date =  AppUtils.getFormattedDate(dateForCalculation,"yyyy-MM-dd");

                outputArea.append("Running option calculation for Expiry: and Date: " + date + "...\n");
                outputArea.append("Option calculation completed. Check Order Entry table.\n");
            }

            private void run_calculation_for_option_buy_expiry() {
                // Clear existing table data
                tableModel.setRowCount(0);

                Date dateForExpiry = (Date)dateSpinnerExpiry.getValue();
                Date dateForCalculation=(Date) dateSpinnerDateToCalculate.getValue();

                String expiryDate =  AppUtils.getFormattedDate(dateForExpiry,"yyMdd");

                String date =  AppUtils.getFormattedDate(dateForCalculation,"yyyy-MM-dd");

                outputArea.append("Running option calculation for Expiry: " + expiryDate +
                        " and Date: " + date + "...\n");



                            /*Pair<Pojo.OrderEntry, Pojo.OrderEntry> entry =
                                    FyersTraderApi
                                            .getInstance()
                                            .GetNifty5pIndexHistory(date,expiryDate );*/
                // Run option calculation

                //NSE:NIFTY25SEP24800PE

                //TODO:
                //1. Monthly expiry date calculation need to be done in "YYMON" format
                //2. Weekly expiry date calculation need to be done in "YYMMDD" format

//                           Pair<Pojo.OrderEntry, Pojo.OrderEntry> entry =
//                                    FyersTraderApi
//                                            .getInstance()
//                                            .GetNifty5pIndexHistory("2025-09-29","25SEP" );


                Pair<Pojo.OrderEntry, Pojo.OrderEntry> entry =
                        FyersTraderApi
                                .getInstance()
                                .GetNifty5pIndexHistoryForExpiryOptionBuy(date    ,expiryDate, "25O07" );

                // Add sample order entry data to table
                addSampleOrderEntries(entry);

                outputArea.append("Option calculation completed. Check Order Entry table.\n");
            }
        };
        worker.execute();
    }

    private void addSampleOrderEntries(Pair<Pojo.OrderEntry, Pojo.OrderEntry> entry) {
        // Add sample data to the table
        // In real implementation, you would get this from your OrderEntryCalculation

        Pojo.OrderEntry callEntry = entry.component1();
        Pojo.OrderEntry putEntry  = entry.component2();

        System.out.println("Call Entry: " + callEntry);
        System.out.println("Put Entry: " + putEntry);
        Object[] row1 = {callEntry.strike(),callEntry.entryPrice(), callEntry.target(),callEntry.sl(), callEntry.tsl(), Math.max(callEntry.tsl(),callEntry.sl() )};
        Object[] row2 = {putEntry.strike(),putEntry.entryPrice(),putEntry.target(),putEntry.sl(),putEntry.tsl(),  Math.max(putEntry.tsl(),putEntry.sl())};


        tableModel.addRow(row1);
        tableModel.addRow(row2);


    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TradingSwingApp::new);
    }
}