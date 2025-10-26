/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package interfaz;

import javax.swing.*;
import java.awt.*;

/**
 * GraphicsCharts - componente Swing simple para mostrar 4 gráficas en tiempo real:
 *  - Throughput (procesos completados por unidad de tiempo)
 *  - Utilización CPU (fracción de ticks ocupados)
 *  - Equidad (Jain's fairness index sobre tiempos de servicio)
 *  - Tiempo de respuesta promedio (aproximación por tiempo total de servicio)
 *
 * No usa java.util.ArrayList ni otras colecciones de java.util para almacenar
 * historiales: utiliza arrays circulares de tamaño fijo.
 *
 * Uso:
 *  - Añadir una instancia al graphics_panel (por ejemplo BorderLayout.CENTER)
 *  - Llamar addSample(...) periódicamente para insertar una nueva muestra
 *
 * Nota: el método addSample es thread-safe y usa SwingUtilities.invokeLater para
 * actualizar la UI si se llama desde hilos de fondo.
 */
public class GraphicsCharts extends JPanel {

    private static final int DEFAULT_HISTORY = 120; // número de muestras a mostrar

    private final int capacity;
    private final double[] historyThroughput;       // valores enteros guardados como double
    private final double[] historyCpuUtil;
    private final double[] historyFairness;
    private final double[] historyResponse;

    private int head = 0;    // posición de inserción siguiente
    private int size = 0;    // número de muestras almacenadas (<= capacity)

    public GraphicsCharts() {
        this(DEFAULT_HISTORY);
    }

    public GraphicsCharts(int historySize) {
        this.capacity = Math.max(8, historySize);
        historyThroughput = new double[capacity];
        historyCpuUtil = new double[capacity];
        historyFairness = new double[capacity];
        historyResponse = new double[capacity];

        setPreferredSize(new Dimension(800, 480));
        setBackground(Color.WHITE);
        setDoubleBuffered(true);
        setBorder(BorderFactory.createLineBorder(new Color(153, 153, 153)));
    }

    /**
     * Añade una muestra con las cuatro métricas.
     * Puede llamarse desde cualquier hilo (es thread-safe).
     *
     * @param throughput número de procesos completados en la unidad de muestreo (por ejemplo, por segundo)
     * @param cpuUtil    fracción [0..1] de ocupación del procesador en el periodo
     * @param fairness   índice de equidad (Jain's index), rango ideal [0..1] (1 = perfectamente justo)
     * @param response   tiempo de respuesta promedio (en la misma unidad de tiempo que use el muestreo)
     */
    public void addSample(final int throughput, final double cpuUtil, final double fairness, final double response) {
        if (SwingUtilities.isEventDispatchThread()) {
            pushSample(throughput, cpuUtil, fairness, response);
        } else {
            SwingUtilities.invokeLater(() -> pushSample(throughput, cpuUtil, fairness, response));
        }
    }

    private void pushSample(int throughput, double cpuUtil, double fairness, double response) {
        historyThroughput[head] = throughput;
        historyCpuUtil[head] = clamp(cpuUtil, 0.0, 1.0);
        historyFairness[head] = clamp(fairness, 0.0, 1.0);
        historyResponse[head] = Math.max(0.0, response);

        head = (head + 1) % capacity;
        if (size < capacity) size++;

        repaint();
    }

    private static double clamp(double v, double lo, double hi) {
        if (v < lo) return lo;
        if (v > hi) return hi;
        return v;
    }

    public void clear() {
        if (SwingUtilities.isEventDispatchThread()) {
            head = 0;
            size = 0;
            for (int i = 0; i < capacity; i++) {
                historyThroughput[i] = 0;
                historyCpuUtil[i] = 0;
                historyFairness[i] = 0;
                historyResponse[i] = 0;
            }
            repaint();
        } else {
            SwingUtilities.invokeLater(this::clear);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // zona de dibujo para cuatro gráficas apiladas
        int w = getWidth();
        int h = getHeight();

        int padding = 12;
        int chartGap = 8;
        int chartHeight = (h - 2 * padding - 3 * chartGap) / 4;

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Títulos y bounds
            int y0 = padding;

            drawSingleChart(g2, "Throughput (completed / tick)", historyThroughput, 0, w, y0, chartHeight);
            y0 += chartHeight + chartGap;

            drawSingleChart(g2, "CPU Utilization (fraction)", historyCpuUtil, 0, w, y0, chartHeight);
            y0 += chartHeight + chartGap;

            drawSingleChart(g2, "Fairness (Jain's index)", historyFairness, 0, w, y0, chartHeight);
            y0 += chartHeight + chartGap;

            drawSingleChart(g2, "Avg Response Time", historyResponse, 0, w, y0, chartHeight);

        } finally {
            g2.dispose();
        }
    }

    /**
     * Dibuja una sola gráfica de línea con ejes simples.
     *
     * @param g2      Graphics2D
     * @param title   título del panel
     * @param series  array circular con datos
     * @param xmin    no usado (reserva futura)
     * @param w       ancho total del componente
     * @param y       coordenada y superior del chart
     * @param height  alto del chart
     */
    private void drawSingleChart(Graphics2D g2, String title, double[] series, int xmin, int w, int y, int height) {
        // background del chart
        int left = 8, right = 8, top = y + 18, bottom = y + height - 8;
        int chartW = w - left - right;
        int chartH = bottom - top;

        // recuadro y título
        g2.setColor(new Color(245, 245, 245));
        g2.fillRect(left, y + 2, chartW, height - 4);
        g2.setColor(Color.DARK_GRAY);
        g2.drawString(title, left + 6, y + 14);

        // eje (simple)
        g2.setColor(new Color(200, 200, 200));
        g2.drawRect(left, top, chartW - 1, chartH - 1);

        if (size == 0) return;

        // Copia los valores en orden cronológico a un array temporal
        double[] data = new double[size];
        int idx = (head - size + capacity) % capacity;
        for (int i = 0; i < size; i++) {
            data[i] = series[idx];
            idx = (idx + 1) % capacity;
        }

        // calcular min / max (para escala). Si todos iguales, ajustar rango
        double min = data[0], max = data[0];
        for (int i = 1; i < data.length; i++) {
            if (data[i] < min) min = data[i];
            if (data[i] > max) max = data[i];
        }
        if (max == min) {
            // expandir rango ligeramente para que la línea se vea
            max = min + 1.0;
            min = Math.max(0, min - 1.0);
        }

        // dibujar línea
        g2.setColor(new Color(0, 120, 215));
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(2f));

        int points = data.length;
        for (int i = 0; i < points - 1; i++) {
            int x1 = left + (int) ((double) i / (points - 1) * (chartW - 2)) + 1;
            int x2 = left + (int) ((double) (i + 1) / (points - 1) * (chartW - 2)) + 1;
            int y1 = top + chartH - 2 - (int) ((data[i] - min) / (max - min) * (chartH - 4));
            int y2 = top + chartH - 2 - (int) ((data[i + 1] - min) / (max - min) * (chartH - 4));
            g2.drawLine(x1, y1, x2, y2);
        }

        g2.setStroke(oldStroke);

        // dibujar puntos
        g2.setColor(new Color(0, 100, 180));
        for (int i = 0; i < points; i++) {
            int xi = left + (int) ((double) i / (points - 1) * (chartW - 2)) + 1;
            int yi = top + chartH - 2 - (int) ((data[i] - min) / (max - min) * (chartH - 4));
            g2.fillOval(xi - 3, yi - 3, 6, 6);
        }

        // leyenda de min/max en la esquina derecha
        g2.setColor(Color.BLACK);
        String rtext = String.format("min: %.2f  max: %.2f", min, max);
        g2.drawString(rtext, left + chartW - g2.getFontMetrics().stringWidth(rtext) - 6, y + 14);
    }
}
