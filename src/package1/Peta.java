package package1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class Peta extends JFrame implements ActionListener {
    private final int CELL_SIZE = 35;
    private final int ROWS = 15;
    private final int COLS = 15;

    private JPanel panel;

    private int[][] matrix = new int[ROWS][COLS];

    private JButton resetButton;
    private JButton resetRedDroidButton;
    private JButton resetGreenDroidButton;
    private JButton startButton;
    private JButton pauseButton;
    private JButton addRedDroid;
    private JButton removeRedDroid;
    private JButton redDroidView;
    private JButton greenDroidView;
    private JSlider visibilitySlider;
    private JLabel visibilityLabel;
    
    private Droid redDroid;
    private Droid greenDroid;
    
    private boolean searching;
    
    private final int MOVE_DELAY = 300; // Jeda antara setiap pergerakan (dalam milidetik)
    private Timer timer;
    
    private java.util.List<Droid> redDroids = new ArrayList<>();;
    private boolean greenDroidViewEnabled = false;
    private int greenDroidVisibility = 1;
    
    private boolean greenDroidVisible = true;

    public Peta() {
        setTitle("Hide and Seek - Ahmad Malik Baehaqi");
        setSize(CELL_SIZE * COLS + 280, CELL_SIZE * ROWS + 90);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new JPanel() {
            
            private void drawDroid(Graphics g, Droid droid) {
                g.setColor(droid.getColor());
                g.fillOval(droid.getCol() * CELL_SIZE, droid.getRow() * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
            
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Set the background color to white
                setBackground(Color.WHITE);

                // Draw black borders around each cell
                g.setColor(Color.BLACK);
                for (int i = 0; i < ROWS; i++) {
                    for (int j = 0; j < COLS; j++) {
                        g.drawRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    }
                }

                // Fill in cells that are set to 1
                g.setColor(Color.BLACK);
                for (int i = 0; i < ROWS; i++) {
                    for (int j = 0; j < COLS; j++) {
                        if (matrix[i][j] == 1) {
                            g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                        }
                    }
                }
                
                if (greenDroidViewEnabled) {
                    paintGreenDroidView(g);
                    for (Droid redDroid : redDroids) {
                        drawDroid(g, redDroid);
                    }
                    if (greenDroidVisible) {
                        drawDroid(g, greenDroid);
                    }
                } else {
                    // Paint all the droids normally
                    for (Droid redDroid : redDroids) {
                        drawDroid(g, redDroid);
                    }
                    if (greenDroidVisible) {
                        drawDroid(g, greenDroid);
                    }
                }
                
            }
        };
        
        add(panel, BorderLayout.CENTER);

        // Tambahkan JPanel kosong di sebelah kanan peta
        JPanel rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(230, 0)); // atur lebar panel
        add(rightPanel, BorderLayout.WEST);

        // Tambahkan tombol reset di JPanel yang baru saja ditambahkan
        
        resetButton = new JButton("Acak Peta");
        resetButton.addActionListener(this);
        resetButton.setPreferredSize(new Dimension(180, 30));
        rightPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // atur layout manager
        rightPanel.add(resetButton);
        
        resetRedDroidButton = new JButton("Acak Droid Merah");
        resetRedDroidButton.addActionListener(this);
        resetRedDroidButton.setPreferredSize(new Dimension(180, 30));
        rightPanel.add(resetRedDroidButton);
        
        resetGreenDroidButton = new JButton("Acak Droid Hijau");
        resetGreenDroidButton.addActionListener(this);
        resetGreenDroidButton.setPreferredSize(new Dimension(180, 30));
        rightPanel.add(resetGreenDroidButton);
        
        startButton = new JButton("Mulai");
        startButton.addActionListener(this);
        startButton.setPreferredSize(new Dimension(180, 30));
        rightPanel.add(startButton);
        
        pauseButton = new JButton("Berhenti");
        pauseButton.addActionListener(this);
        pauseButton.setPreferredSize(new Dimension(180, 30));
        rightPanel.add(pauseButton);
        
        addRedDroid = new JButton("Tambah Droid Merah");
        addRedDroid.addActionListener(this);
        addRedDroid.setPreferredSize(new Dimension(180, 30));
        rightPanel.add(addRedDroid);
        
        removeRedDroid = new JButton("Kurangi Droid Merah");
        removeRedDroid.addActionListener(this);
        removeRedDroid.setPreferredSize(new Dimension(180, 30));
        rightPanel.add(removeRedDroid);
        
        redDroidView = new JButton("Pandangan Droid Merah");
        redDroidView.addActionListener(this);
        redDroidView.setPreferredSize(new Dimension(180, 30));
        rightPanel.add(redDroidView);
        
        greenDroidView = new JButton("Pandangan Droid Hijau");
        greenDroidView.addActionListener(this);
        greenDroidView.setPreferredSize(new Dimension(180, 30));
        rightPanel.add(greenDroidView);
        
        visibilityLabel = new JLabel("Visibilitas Droid Hijau");
        visibilitySlider = new JSlider(1, 14, greenDroidVisibility);
        visibilitySlider.setPaintTicks(true);
        visibilitySlider.setMajorTickSpacing(5);
        visibilitySlider.setMinorTickSpacing(1);
        visibilitySlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                greenDroidVisibility = source.getValue();
                panel.repaint();
            }
        });
        
        rightPanel.add(visibilityLabel);
        rightPanel.add(visibilitySlider);
        visibilityLabel.setVisible(false);
        visibilitySlider.setVisible(false);
        
        rightPanel.setBackground(Color.WHITE);

        // Set random cells to 1 to create walls
        generateMaze();

        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private java.util.List<int[]> findShortestPath() {
        int startRow = redDroid.getRow();
        int startCol = redDroid.getCol();
        int targetRow = greenDroid.getRow();
        int targetCol = greenDroid.getCol();

        BFS bfs = new BFS(matrix, startRow, startCol, targetRow, targetCol);
        return bfs.findPath();
    }
    
    private int currentIndex; // Variabel anggota kelas

    private void moveRedDroidAutomatically(java.util.List<int[]> shortestPath) {
        currentIndex = 0; // Mengatur currentIndex ke 0 saat memulai pergerakan

        timer = new Timer(MOVE_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentIndex >= shortestPath.size()) {
                        timer.stop();
                        searching = false;
                        startButton.doClick(); // Panggil startButton()
                    }
                boolean reachedTarget = false; // Variabel untuk menandai apakah salah satu droid merah mencapai tujuan

                for (Droid redDroid : redDroids) {
                    int[] position = shortestPath.get(currentIndex);
                    int nextRow = position[0];
                    int nextCol = position[1];

                    int currentRow = redDroid.getRow();
                    int currentCol = redDroid.getCol();

                    // Cek apakah droid merah sudah mencapai tujuan akhir
                    if (currentRow == nextRow && currentCol == nextCol) {
                        continue; // Langsung ke droid merah berikutnya jika sudah mencapai tujuan
                    }

                    BFS bfs = new BFS(matrix, currentRow, currentCol, nextRow, nextCol);
                    java.util.List<int[]> droidPath = bfs.findPath();

                    if (!droidPath.isEmpty()) {
                        int[] nextPosition = droidPath.get(1); // Ambil posisi selanjutnya setelah posisi saat ini
                        int newRow = nextPosition[0];
                        int newCol = nextPosition[1];

                        redDroid.setRow(newRow);
                        redDroid.setCol(newCol);

                        // Cek apakah droid merah telah mencapai tujuan akhir atau bertabrakan dengan droid hijau
                        if (newRow == greenDroid.getRow() && newCol == greenDroid.getCol()) {
                            reachedTarget = true;
                            break;
                        }
                    }
                }

                panel.repaint();
                currentIndex++; // Pindah ke indeks berikutnya setelah memperbarui posisi droid

                if (reachedTarget || currentIndex >= shortestPath.size()) {
                    timer.stop();
                    searching = false;

                    if (reachedTarget) {
                        JOptionPane.showMessageDialog(null, "Permainan Selesai!");
                    } else {
                        startButton.doClick(); // Panggil startButton()
                    }
                }
                moveGreenDroid();
            }
        });

        timer.start();
    }

    
    private void checkPath() {
        for (int i = 1; i < ROWS - 1; i++) {
            for (int j = 1; j < COLS - 1; j++) {
                if (matrix[i][j] == 0) {
                    // Cek kondisi 1
                    if (matrix[i][j - 1] == 1 && matrix[i - 1][j] == 1 && matrix[i][j + 1] == 1 && matrix[i + 1][j] == 0) {
                        matrix[i - 1][j] = 0; // Ubah atas menjadi jalan
                    }
                    // Cek kondisi 2
                    if (matrix[i - 1][j] == 1 && matrix[i][j + 1] == 1 && matrix[i + 1][j] == 1 && matrix[i][j - 1] == 0) {
                        matrix[i][j + 1] = 0; // Ubah kanan menjadi jalan
                    }
                    // Cek kondisi 3
                    if (matrix[i][j + 1] == 1 && matrix[i + 1][j] == 1 && matrix[i][j - 1] == 1 && matrix[i - 1][j] == 0) {
                        matrix[i + 1][j] = 0; // Ubah bawah menjadi jalan
                    }
                    // Cek kondisi 4
                    if (matrix[i + 1][j] == 1 && matrix[i][j - 1] == 1 && matrix[i - 1][j] == 1 && matrix[i][j + 1] == 0) {
                        matrix[i][j - 1] = 0; // Ubah kiri menjadi jalan
                    }
                }
            }
        }
    }
    
    private void createRandomRedDroid() {
        Random random = new Random();
        int redDroidRow;
        int redDroidCol;

        do {
            redDroidRow = random.nextInt(ROWS);
            redDroidCol = random.nextInt(COLS);
        } while (matrix[redDroidRow][redDroidCol] != 0 || (greenDroid != null && redDroidRow == greenDroid.getRow() && redDroidCol == greenDroid.getCol()));

        Color redDroidColor = Color.RED;
        redDroid = new Droid(redDroidRow, redDroidCol, redDroidColor);
        redDroids.add(redDroid);
    }
    
    private void createRandomGreenDroid() {
        Random random = new Random();
        int greenDroidRow;
        int greenDroidCol;

        do {
            greenDroidRow = random.nextInt(ROWS);
            greenDroidCol = random.nextInt(COLS);
        } while (matrix[greenDroidRow][greenDroidCol] != 0 || (greenDroidRow == redDroid.getRow() && greenDroidCol == redDroid.getCol()));

        Color greenDroidColor = Color.GREEN;
        greenDroid = new Droid(greenDroidRow, greenDroidCol, greenDroidColor);
    }
    
    private void resetDroidPosition() {
        Random random = new Random();
    
        for (int i = 0; i < redDroids.size(); i++) {
            int redDroidRow;
            int redDroidCol;

            do {
                redDroidRow = random.nextInt(ROWS);
                redDroidCol = random.nextInt(COLS);
            } while (matrix[redDroidRow][redDroidCol] != 0 || (greenDroid != null && redDroidRow == greenDroid.getRow() && redDroidCol == greenDroid.getCol()));

            Droid redDroid = redDroids.get(i);
            redDroid.setRow(redDroidRow);
            redDroid.setCol(redDroidCol);
        }
    }
    
    private void resetGreenDroidPosition() {
        createRandomGreenDroid();
    }

    
    private void generateMaze() {
        // Inisialisasi peta dengan seluruh elemen bernilai 1 (tembok)
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                matrix[i][j] = 1;
            }
        }

        // Panggil metode recursiveBacktracking untuk membuat jalur
        recursiveBacktracking(0, 0);
        
        if (redDroids.size() < 1) {
            createRandomRedDroid();
        }
        
        createRandomGreenDroid();
        checkPath();

        panel.repaint();
    }

    private void recursiveBacktracking(int row, int col) {
        // Setel elemen pada posisi saat ini menjadi 0 (jalur)
        matrix[row][col] = 0;

        // Buat array untuk menyimpan urutan acak arah pergerakan
        int[] directions = {0, 1, 2, 3};
        shuffleArray(directions); // Acak urutan arah

        // Coba semua arah secara acak
        for (int direction : directions) {
            int newRow = row;
            int newCol = col;

            if (direction == 0 && newRow > 1) {
                newRow -= 2; // Atas
            } else if (direction == 1 && newCol < COLS - 2) {
                newCol += 2; // Kanan
            } else if (direction == 2 && newRow < ROWS - 2) {
                newRow += 2; // Bawah
            } else if (direction == 3 && newCol > 1) {
                newCol -= 2; // Kiri
            }

            if (matrix[newRow][newCol] == 1) {
                // Cek apakah sel yang dituju adalah tembok
                matrix[(newRow + row) / 2][(newCol + col) / 2] = 0; // Hapus tembok di antara sel saat ini dan sel tujuan
                recursiveBacktracking(newRow, newCol); // Pindah ke selanjutnya secara rekursif
            }
        }
    }

    private void shuffleArray(int[] array) {
        Random rand = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int index = rand.nextInt(i + 1);
            int temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }

    private boolean isValidMove(int row, int col) {
        // Memastikan langkah berada di dalam batas peta
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) {
            return false;
        }

        // Memastikan langkah tidak bertabrakan dengan tembok
        if (matrix[row][col] == 1) {
            return false;
        }

        // Memastikan langkah tidak bertabrakan dengan droid merah
        if (row == redDroid.getRow() && col == redDroid.getCol()) {
            return false;
        }

        return true;
    }
    
    private int prevGreenRow;
    private int prevGreenCol;

    private void moveGreenDroid() {
        int redRow = redDroid.getRow();
        int redCol = redDroid.getCol();
        int greenRow = greenDroid.getRow();
        int greenCol = greenDroid.getCol();
        
        if (greenRow == redRow && greenCol == redCol) {
            timer.stop();
            searching = false;
            // Tambahkan kode di sini untuk tindakan yang ingin Anda lakukan saat game berhenti
        } else {
            int newRow = greenRow;
            int newCol = greenCol;

            // Menghindari droid merah dengan memilih arah yang menjauh
            if (redRow < greenRow) {
                newRow++;
            } else if (redRow > greenRow) {
                newRow--;
            }

            if (redCol < greenCol) {
                newCol++;
            } else if (redCol > greenCol) {
                newCol--;
            }

            // Cek apakah langkah berikutnya valid (tidak bertabrakan dengan tembok atau droid lain) dan hanya bergerak secara horizontal atau vertikal
            if (isValidMove(newRow, newCol) && (newRow == greenRow || newCol == greenCol)) {
                greenDroid.setRow(newRow);
                greenDroid.setCol(newCol);
                panel.repaint();
            } else {
                // Jika langkah selanjutnya tidak valid, cari langkah alternatif secara acak
                int[] directions = {0, 1, 2, 3};
                shuffleArray(directions);

                for (int direction : directions) {
                    newRow = greenRow;
                    newCol = greenCol;

                    if (direction == 0) {
                        newRow++;
                    } else if (direction == 1) {
                        newCol++;
                    } else if (direction == 2) {
                        newRow--;
                    } else if (direction == 3) {
                        newCol--;
                    }

                    // Cek apakah langkah alternatif valid (tidak bertabrakan dengan tembok atau droid lain) dan hanya bergerak secara horizontal atau vertikal
                    if (isValidMove(newRow, newCol) && (newRow == greenRow || newCol == greenCol) && (newRow != prevGreenRow || newCol != prevGreenCol)) {
                        greenDroid.setRow(newRow);
                        greenDroid.setCol(newCol);
                        panel.repaint();
                        break;
                    }
                }
            }

            // Update posisi sebelumnya
            prevGreenRow = greenDroid.getRow();
            prevGreenCol = greenDroid.getCol();
        }
    }

    private void pauseDroid() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
            searching = false;
        }
    }
    
    private void paintGreenDroidView(Graphics g) {
        int greenRow = greenDroid.getRow();
        int greenCol = greenDroid.getCol();

        g.setColor(Color.BLUE);
        // Paint all the cells with original matrix colors except the visible ones
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (Math.abs(i - greenRow) > 1 || Math.abs(j - greenCol) > 1) {
                    g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        // Paint the visible cells around the green droid
        for (int i = greenRow - greenDroidVisibility; i <= greenRow + greenDroidVisibility; i++) {
            for (int j = greenCol - greenDroidVisibility; j <= greenCol + greenDroidVisibility; j++) {
                // Skip the current cell of the green droid
                if (i == greenRow && j == greenCol) {
                    continue;
                }

                // Check if the cell is within the bounds of the matrix
                if (i >= 0 && i < ROWS && j >= 0 && j < COLS) {
                    if (matrix[i][j] == 0) {
                        g.setColor(Color.WHITE); // Set color for empty cell
                    } else {
                        g.setColor(Color.BLACK); // Set color for wall cell
                    }
                    g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        // Set the color for the red droids
        g.setColor(Color.RED);

        // Paint the red droids on the visible cells
        for (Droid redDroid : redDroids) {
            int redRow = redDroid.getRow();
            int redCol = redDroid.getCol();

            // Check if the red droid is within the visible cells
            if (Math.abs(redRow - greenRow) <= 1 && Math.abs(redCol - greenCol) <= 1) {
                // Paint the red droid with red color
                g.fillOval(redCol * CELL_SIZE, redRow * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }
    
    private boolean isGreenDroidVisible() {
        int redRow = redDroid.getRow();
        int redCol = redDroid.getCol();
        int greenRow = greenDroid.getRow();
        int greenCol = greenDroid.getCol();

        // Cek apakah droid hijau terlihat secara horizontal atau vertikal
        if (redRow == greenRow || redCol == greenCol) {
            int startRow, endRow, startCol, endCol;

            // Tentukan titik awal dan akhir untuk cek penghalang
            if (redRow < greenRow) {
                startRow = redRow;
                endRow = greenRow;
            } else {
                startRow = greenRow;
                endRow = redRow;
            }

            if (redCol < greenCol) {
                startCol = redCol;
                endCol = greenCol;
            } else {
                startCol = greenCol;
                endCol = redCol;
            }

            // Periksa setiap sel antara droid merah dan hijau
            for (int row = startRow + 1; row < endRow; row++) {
                if (matrix[row][redCol] == 1) {
                    return false; // Ada tembok yang menghalangi
                }
            }

            for (int col = startCol + 1; col < endCol; col++) {
                if (matrix[redRow][col] == 1) {
                    return false; // Ada tembok yang menghalangi
                }
            }

            return true; // Tidak ada tembok yang menghalangi
        }

        return false; // Droid merah dan hijau tidak berada pada posisi yang sama secara horizontal atau vertikal
    }


    private void toggleGreenDroidVisibility() {
        greenDroidVisible = !greenDroidVisible;
        panel.repaint();
        
    }
    
    private void removeRedDroid() {
        if (!redDroids.isEmpty()) {
            redDroids.remove(redDroids.size() - 1);
            panel.repaint();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == resetButton) {
            generateMaze();
            resetDroidPosition();
        }
        if (e.getSource() == resetRedDroidButton) {
            resetDroidPosition();
            panel.repaint();
        }
        if (e.getSource() == resetGreenDroidButton) {
            resetGreenDroidPosition();
            panel.repaint();
        }
        if (e.getSource() == pauseButton) {
            pauseDroid();
            panel.repaint();
        }
        if (e.getSource() == startButton) {
            if (!searching) {
                searching = true;
                // Mulai pencarian
                java.util.List<int[]> shortestPath = findShortestPath();
                // Menggunakan jalur terpendek yang ditemukan, lakukan pergerakan droid merah
                moveRedDroidAutomatically(shortestPath);
            }
        }
        if (e.getSource() == addRedDroid) {
            createRandomRedDroid();
            panel.repaint();
        }
        if (e.getSource() == removeRedDroid) {
            removeRedDroid();
        }
        if (e.getSource() == redDroidView) {
            toggleGreenDroidVisibility();
        }
        if (e.getSource() == greenDroidView) {
            greenDroidViewEnabled = !greenDroidViewEnabled;
            panel.repaint();
            visibilityLabel.setVisible(greenDroidViewEnabled);
            visibilitySlider.setVisible(greenDroidViewEnabled);
        }
    }
}