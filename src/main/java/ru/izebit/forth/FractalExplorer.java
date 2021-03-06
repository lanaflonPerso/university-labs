package ru.izebit.forth;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:izebit@gmail.com">Artem Konovalov</a> <br/>
 * Date: 23.10.2019
 */
public class FractalExplorer extends JFrame {
    private static final int width = 500;
    private static final int height = 500;

    private final JImageDisplay imageDisplay;
    private FractalGenerator generator;
    private Rectangle2D.Double range;

    public FractalExplorer(int width, int height) {
        this.imageDisplay = new JImageDisplay(width, height);
        initFractal("maldebrot");
    }

    private void createAndShowGUI() {
        this.setLayout(new BorderLayout());

        String[] items = {"maldebrot", "tricorn", "burning ship"};
        final JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.addActionListener(e -> {
            @SuppressWarnings("unchecked")
            JComboBox<String> cb = (JComboBox<String>) e.getSource();
            String fractalType = (String) cb.getSelectedItem();
            if (fractalType != null) {
                initFractal(fractalType);
                drawFractal();
            }

        });
        this.add(comboBox, BorderLayout.NORTH);

        imageDisplay.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                double xCoord = FractalGenerator.getCoord(range.x, range.x + range.getWidth(), imageDisplay.getWidth(), e.getX());
                double yCoord = FractalGenerator.getCoord(range.y, range.y + range.getHeight(), imageDisplay.getHeight(), e.getY());
                generator.recenterAndZoomRange(range, xCoord, yCoord, 0.5);
                drawFractal();
            }
        });
        this.add(imageDisplay, BorderLayout.CENTER);

        JButton saveButton = new JButton("save image");
        saveButton.addActionListener(event -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Specify a file to save");

            int userSelection = fileChooser.showSaveDialog(FractalExplorer.this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try {
                    BufferedImage bi = imageDisplay.getBufferedImage();  // retrieve image
                    ImageIO.write(bi, "png", fileToSave);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        JButton resetButton = new JButton("reset");
        resetButton.addActionListener(event -> {
            range = generator.getInitialRange(new Rectangle2D.Double());
            imageDisplay.clearImage();
            repaint();
            drawFractal();
        });
        JPanel jPanel = new JPanel();
        jPanel.add(saveButton);
        jPanel.add(resetButton);
        this.add(jPanel, BorderLayout.SOUTH);

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setPreferredSize(new Dimension(width, height + 100));
        this.setTitle("this is a fractal demo");
        this.pack();
        this.setVisible(true);
        this.setResizable(false);
    }

    private void initFractal(final String fractalType) {
        switch (fractalType) {
            case "maldebrot":
                generator = new Mandelbrot();
                range = generator.getInitialRange(new Rectangle2D.Double());
                break;
            case "tricorn":
                generator = new Tricorn();
                range = generator.getInitialRange(new Rectangle2D.Double());
                break;
            case "burning ship":
                generator = new BurningShip();
                range = generator.getInitialRange(new Rectangle2D.Double());
                break;
        }
    }

    private void drawFractal() {
        for (int x = 0; x < imageDisplay.getImageWidth(); x++) {
            double xCoord = FractalGenerator.getCoord(range.x, range.x + range.getWidth(), imageDisplay.getImageWidth(), x);
            for (int y = 0; y < imageDisplay.getImageHeight(); y++) {
                double yCoord = FractalGenerator.getCoord(range.y, range.y + range.getHeight(), imageDisplay.getImageHeight(), y);
                int numIterations = generator.numIterations(xCoord, yCoord);
                imageDisplay.drawPixel(x, y, getColor(numIterations));
            }
        }

        repaint();
    }

    private static int getColor(int numIters) {
        if (numIters < 0)
            return 0;

        float hue = 0.7f + (float) numIters / 200f;
        return Color.HSBtoRGB(hue, 1f, 1f);
    }

    public static void main(String[] args) {
        final FractalExplorer fractalExplorer = new FractalExplorer(width, height);
        fractalExplorer.createAndShowGUI();
        fractalExplorer.drawFractal();
    }
}
