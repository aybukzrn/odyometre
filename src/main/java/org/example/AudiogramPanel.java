package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class AudiogramPanel extends JPanel {

    private Map<Integer, Integer> rightEarData = new HashMap<>();
    private Map<Integer, Integer> leftEarData = new HashMap<>();

    private final int[] frequencies = {250, 500, 1000, 2000, 4000, 8000};
    private final int minDb = -10;
    private final int maxDb = 120;
    private final int dbStep = 10;

    public AudiogramPanel() {
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder("Odyogram Grafiği"));
    }

    public void addThreshold(int frequency, int db, String ear) {
        if (ear.equals("Sağ Kulak")) {
            rightEarData.put(frequency, db);
        } else {
            leftEarData.put(frequency, db);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int padding = 40;

        int graphWidth = width - (2 * padding);
        int graphHeight = height - (2 * padding);

        g2d.setColor(Color.LIGHT_GRAY);

        for (int i = 0; i < frequencies.length; i++) {
            int x = padding + (i * graphWidth / (frequencies.length - 1));
            g2d.drawLine(x, padding, x, height - padding);
            g2d.setColor(Color.BLACK);
            g2d.drawString(frequencies[i] + " Hz", x - 15, padding - 10);
            g2d.setColor(Color.LIGHT_GRAY);
        }

        int numDbSteps = (maxDb - minDb) / dbStep;
        for (int i = 0; i <= numDbSteps; i++) {
            int y = padding + (i * graphHeight / numDbSteps);
            g2d.drawLine(padding, y, width - padding, y);
            g2d.setColor(Color.BLACK);
            int currentDb = minDb + (i * dbStep);
            g2d.drawString(currentDb + " dB", padding - 35, y + 5);
            g2d.setColor(Color.LIGHT_GRAY);
        }

        drawPoints(g2d, rightEarData, Color.RED, "O", padding, graphWidth, graphHeight, numDbSteps);
        drawPoints(g2d, leftEarData, Color.BLUE, "X", padding, graphWidth, graphHeight, numDbSteps);
    }

    private void drawPoints(Graphics2D g2d, Map<Integer, Integer> data, Color color, String shape,
                            int padding, int graphWidth, int graphHeight, int numDbSteps) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(2));

        for (Map.Entry<Integer, Integer> entry : data.entrySet()) {
            int freq = entry.getKey();
            int db = entry.getValue();

            int freqIndex = 0;
            for (int i = 0; i < frequencies.length; i++) {
                if (frequencies[i] == freq) freqIndex = i;
            }
            int x = padding + (freqIndex * graphWidth / (frequencies.length - 1));

            int y = padding + ((db - minDb) * graphHeight / (maxDb - minDb));

            if (shape.equals("O")) {
                g2d.drawOval(x - 5, y - 5, 10, 10);
            } else if (shape.equals("X")) {
                g2d.drawLine(x - 5, y - 5, x + 5, y + 5);
                g2d.drawLine(x - 5, y + 5, x + 5, y - 5);
            }
        }
    }
}