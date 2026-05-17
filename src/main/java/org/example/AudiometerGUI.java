package org.example;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import org.example.fp.AudiometryTestState;
import org.example.fp.Ear;
import org.example.fp.HughsonWestlakeAlgorithm;
import org.example.fp.PatientResponse;
import org.example.fp.ResponseParser;
import org.example.fp.ThresholdResult;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class AudiometerGUI extends JFrame {

    private AudiogramPanel graphPanel;
    private JComboBox<String> freqBox, dbBox, earBox, portBox;
    private JButton connectButton, sendButton, heardButton, notHeardButton, saveButton;
    private JLabel algorithmStatusLabel;
    private SerialPort activePort;
    private AudiometryTestState currentTestState;

    public AudiometerGUI() {
        setTitle("Audiometer Clinical Software");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setupControlPanel();

        graphPanel = new AudiogramPanel();
        add(graphPanel, BorderLayout.CENTER);
    }

    private void setupControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setPreferredSize(new Dimension(250, 600));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Test Kontrolleri"));
        controlPanel.setLayout(new GridLayout(14, 1, 8, 8));

        controlPanel.add(new JLabel("Donanım Portu:"));
        portBox = new JComboBox<>();
        for (SerialPort port : SerialPort.getCommPorts()) {
            portBox.addItem(port.getSystemPortName());
        }
        controlPanel.add(portBox);

        connectButton = new JButton("Bağlan");
        connectButton.addActionListener(e -> connectToPort());
        controlPanel.add(connectButton);

        controlPanel.add(new JLabel("Test Edilen Kulak:"));
        earBox = new JComboBox<>(new String[]{"Sağ Kulak", "Sol Kulak"});
        controlPanel.add(earBox);

        controlPanel.add(new JLabel("Frekans (Hz):"));
        freqBox = new JComboBox<>(new String[]{"250", "500", "1000", "2000", "4000", "8000"});
        controlPanel.add(freqBox);

        controlPanel.add(new JLabel("Şiddet (dB):"));
        String[] decibels = {
                "-10", "-5", "0", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50",
                "55", "60", "65", "70", "75", "80", "85", "90", "95", "100", "105", "110", "115", "120"
        };
        dbBox = new JComboBox<>(decibels);
        dbBox.setSelectedItem("40");
        controlPanel.add(dbBox);

        sendButton = new JButton("Sinyal Gönder");
        sendButton.setEnabled(false);
        sendButton.addActionListener(e -> sendSignal());
        controlPanel.add(sendButton);

        heardButton = new JButton("Duydu (Algoritma)");
        heardButton.addActionListener(e -> handlePatientResponse(PatientResponse.HEARD));
        controlPanel.add(heardButton);

        notHeardButton = new JButton("Duymadı (Algoritma)");
        notHeardButton.addActionListener(e -> handlePatientResponse(PatientResponse.NOT_HEARD));
        controlPanel.add(notHeardButton);

        algorithmStatusLabel = new JLabel("Algoritma hazır.");
        controlPanel.add(algorithmStatusLabel);

        saveButton = new JButton("Grafiği Kaydet");
        saveButton.addActionListener(e -> saveGraph());
        controlPanel.add(saveButton);

        add(controlPanel, BorderLayout.WEST);
    }

    private void connectToPort() {
        if (activePort != null && activePort.isOpen()) {
            activePort.closePort();
            connectButton.setText("Bağlan");
            sendButton.setEnabled(false);
            return;
        }

        String portName = (String) portBox.getSelectedItem();
        if (portName == null) return;

        activePort = SerialPort.getCommPort(portName);
        activePort.setBaudRate(9600);

        if (activePort.openPort()) {
            connectButton.setText("Bağlantıyı Kes");
            sendButton.setEnabled(true);
            JOptionPane.showMessageDialog(this, "Bağlantı Başarılı!");
            listenForResponse();
        } else {
            JOptionPane.showMessageDialog(this, "Port açılamadı!", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendSignal() {
        ensureCurrentTestState();

        String freq = (String) freqBox.getSelectedItem();
        String db = (String) dbBox.getSelectedItem();
        String command = freq + "," + db + "\n";

        byte[] bytes = command.getBytes();
        activePort.writeBytes(bytes, bytes.length);
        algorithmStatusLabel.setText(freq + " Hz, " + db + " dB gönderildi.");
    }

    private void ensureCurrentTestState() {
        int frequency = Integer.parseInt((String) freqBox.getSelectedItem());
        int intensity = Integer.parseInt((String) dbBox.getSelectedItem());
        Ear ear = selectedEar();

        if (currentTestState == null
                || currentTestState.isCompleted()
                || currentTestState.frequencyHz() != frequency
                || currentTestState.ear() != ear) {
            currentTestState = HughsonWestlakeAlgorithm.initialState(ear, frequency, intensity);
        }
    }

    private Ear selectedEar() {
        String earLabel = (String) earBox.getSelectedItem();
        return Ear.fromLabel(earLabel)
                .orElseThrow(() -> new IllegalStateException("Geçersiz kulak seçimi: " + earLabel));
    }

    private void handlePatientResponse(PatientResponse response) {
        ensureCurrentTestState();
        currentTestState = HughsonWestlakeAlgorithm.applyResponse(currentTestState, response);

        if (currentTestState.threshold().isPresent()) {
            ThresholdResult threshold = currentTestState.threshold().orElseThrow();
            graphPanel.addThreshold(threshold.frequencyHz(), threshold.thresholdDb(), (String) earBox.getSelectedItem());
            dbBox.setSelectedItem(String.valueOf(threshold.thresholdDb()));
            algorithmStatusLabel.setText("Eşik bulundu: " + threshold.thresholdDb() + " dB");
            JOptionPane.showMessageDialog(this, "Eşik bulundu: " + threshold.thresholdDb() + " dB");
            return;
        }

        dbBox.setSelectedItem(String.valueOf(currentTestState.currentIntensityDb()));
        algorithmStatusLabel.setText("Sonraki seviye: " + currentTestState.currentIntensityDb() + " dB");
    }

    private void listenForResponse() {
        activePort.addDataListener(new SerialPortMessageListener() {
            @Override
            public byte[] getMessageDelimiter() { return new byte[]{'\n'}; }

            @Override
            public boolean delimiterIndicatesEndOfMessage() { return true; }

            @Override
            public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_RECEIVED; }

            @Override
            public void serialEvent(SerialPortEvent event) {
                String message = new String(event.getReceivedData()).trim();

                SwingUtilities.invokeLater(() -> {
                    ResponseParser.parse(message)
                            .ifPresent(this::handleSerialResponse);
                });
            }

            private void handleSerialResponse(PatientResponse response) {
                handlePatientResponse(response);
            }
        });
    }

    private void saveGraph() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Grafiği Kaydet");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getAbsolutePath().endsWith(".png")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".png");
            }

            BufferedImage image = new BufferedImage(graphPanel.getWidth(), graphPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            graphPanel.paint(g2d);
            g2d.dispose();

            try {
                ImageIO.write(image, "png", fileToSave);
                JOptionPane.showMessageDialog(this, "Grafik başarıyla kaydedildi!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Kaydetme hatası!", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AudiometerGUI gui = new AudiometerGUI();
            gui.setVisible(true);
        });
    }
}