import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame {

    // Veri
    private List<User> allUsers;
    private List<User> targetUsers;
    private Map<Integer, Movie> movies;
    private Recommender recommender;

    // Ekran 1 bileşenleri
    private JComboBox<String> targetUserCombo;
    private JTextField xField1, kField1;
    private JTextArea resultArea1;

    // Ekran 2 bileşenleri
    private JComboBox<String>[] movieCombos;
    private JTextField[] ratingFields;
    private JTextField xField2, kField2;
    private JTextArea resultArea2;

    // 10 rastgele film (Ekran 2 için)
    private List<Movie> randomMovies;

    public MainFrame() {
        setTitle("Film Öneri Sistemi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null);

        // Veriyi yükle
        loadData();

        // Sekmeleri oluştur
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Hedef Kullanıcıya Göre", buildPanel1());
        tabbedPane.addTab("Film Puanına Göre", buildPanel2());
        add(tabbedPane);

        setVisible(true);
    }

    // ─────────────────────────────────────────
    // CSV dosyalarını oku, Recommender'ı hazırla
    // ─────────────────────────────────────────
    private void loadData() {
        // CSV dosyaları .jar ile aynı klasörde olmalı
        allUsers    = CSVReader.readMainData("main_data.csv");
        targetUsers = CSVReader.readTargetUsers("target_user.csv");
        movies      = CSVReader.readMovies("movies.csv");
        recommender = new Recommender(allUsers, movies);

        // Ekran 2 için rastgele 10 film seç
        List<Movie> movieList = new ArrayList<>(movies.values());
        Collections.shuffle(movieList);
        randomMovies = movieList.subList(0, Math.min(10, movieList.size()));
    }

    // ─────────────────────────────────────────
    // EKRAN 1: Hedef kullanıcı seçerek öneri
    // ─────────────────────────────────────────
    private JPanel buildPanel1() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Üst: kontroller ---
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        top.add(new JLabel("Hedef Kullanıcı:"));
        targetUserCombo = new JComboBox<>();
        for (User u : targetUsers) {
            targetUserCombo.addItem("Kullanıcı " + u.userId);
        }
        top.add(targetUserCombo);

        top.add(new JLabel("X (kullanıcı sayısı):"));
        xField1 = new JTextField("3", 4);
        top.add(xField1);

        top.add(new JLabel("K (film/kullanıcı):"));
        kField1 = new JTextField("5", 4);
        top.add(kField1);

        JButton btn = new JButton("Önerileri Getir");
        btn.addActionListener(e -> runRecommendation1());
        top.add(btn);

        panel.add(top, BorderLayout.NORTH);

        // --- Orta: sonuçlar ---
        resultArea1 = new JTextArea();
        resultArea1.setEditable(false);
        resultArea1.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultArea1.setBorder(new EmptyBorder(8, 8, 8, 8));
        resultArea1.setText("Kullanıcı seçip 'Önerileri Getir' butonuna basın.");
        JScrollPane scroll = new JScrollPane(resultArea1);
        scroll.setBorder(BorderFactory.createTitledBorder("Önerilen Filmler"));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // Ekran 1 buton tıklandığında
    private void runRecommendation1() {
        int idx = targetUserCombo.getSelectedIndex();
        if (idx < 0 || idx >= targetUsers.size()) return;

        User targetUser = targetUsers.get(idx);

        int X, K;
        try {
            X = Integer.parseInt(xField1.getText().trim());
            K = Integer.parseInt(kField1.getText().trim());
            if (X <= 0 || K <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "X ve K pozitif tam sayı olmalıdır!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Hesaplama biraz sürebilir, thread'de çalıştır
        resultArea1.setText("Hesaplanıyor...");
        new Thread(() -> {
            List<String> results = recommender.recommend(targetUser, X, K);
            SwingUtilities.invokeLater(() -> displayResults(resultArea1, results, X, K));
        }).start();
    }

    // ─────────────────────────────────────────
    // EKRAN 2: Elle puan girerek öneri
    // ─────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private JPanel buildPanel2() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Üst: 5 satır film seçimi ---
        JPanel gridPanel = new JPanel(new GridLayout(5, 2, 8, 8));
        gridPanel.setBorder(BorderFactory.createTitledBorder("5 Film Seç ve Puan Ver (1-5)"));

        movieCombos = new JComboBox[5];
        ratingFields = new JTextField[5];

        for (int i = 0; i < 5; i++) {
            movieCombos[i] = new JComboBox<>();
            for (Movie m : randomMovies) {
                movieCombos[i].addItem(m.movieId + " | " + m.title);
            }
            // Varsayılan olarak farklı filmler seçili gelsin
            if (i < randomMovies.size()) {
                movieCombos[i].setSelectedIndex(i);
            }

            ratingFields[i] = new JTextField("3", 5);

            JPanel row = new JPanel(new BorderLayout(5, 0));
            row.add(movieCombos[i], BorderLayout.CENTER);
            row.add(ratingFields[i], BorderLayout.EAST);
            gridPanel.add(row);
        }

        // X, K ve buton
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        controls.add(new JLabel("X:"));
        xField2 = new JTextField("3", 4);
        controls.add(xField2);
        controls.add(new JLabel("K:"));
        kField2 = new JTextField("5", 4);
        controls.add(kField2);

        JButton btn = new JButton("Önerileri Getir");
        btn.addActionListener(e -> runRecommendation2());
        controls.add(btn);

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.add(gridPanel, BorderLayout.CENTER);
        topSection.add(controls, BorderLayout.SOUTH);
        panel.add(topSection, BorderLayout.NORTH);

        // --- Alt: sonuçlar ---
        resultArea2 = new JTextArea();
        resultArea2.setEditable(false);
        resultArea2.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultArea2.setBorder(new EmptyBorder(8, 8, 8, 8));
        resultArea2.setText("Film ve puanları girdikten sonra 'Önerileri Getir' butonuna basın.");
        JScrollPane scroll = new JScrollPane(resultArea2);
        scroll.setBorder(BorderFactory.createTitledBorder("Önerilen Filmler"));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // Ekran 2 buton tıklandığında
    private void runRecommendation2() {
        // Girilen puanları doğrula ve topla
        Map<Integer, Integer> movieRatings = new LinkedHashMap<>();

        for (int i = 0; i < 5; i++) {
            // Seçilen combo'dan movieId'yi al
            String selected = (String) movieCombos[i].getSelectedItem();
            if (selected == null) continue;
            int movieId = Integer.parseInt(selected.split("\\|")[0].trim());

            // Puanı parse et
            int rating;
            try {
                rating = Integer.parseInt(ratingFields[i].getText().trim());
                if (rating < 1 || rating > 5) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                    (i + 1) + ". satır için geçerli bir puan girin (1-5)!",
                    "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Aynı film iki kez seçilmişse uyar
            if (movieRatings.containsKey(movieId)) {
                JOptionPane.showMessageDialog(this,
                    "Aynı filmi birden fazla seçmeyin!", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }

            movieRatings.put(movieId, rating);
        }

        int X, K;
        try {
            X = Integer.parseInt(xField2.getText().trim());
            K = Integer.parseInt(kField2.getText().trim());
            if (X <= 0 || K <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "X ve K pozitif tam sayı olmalıdır!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final int finalX = X, finalK = K;
        resultArea2.setText("Hesaplanıyor...");
        new Thread(() -> {
            List<String> results = recommender.recommendFromRatings(movieRatings, finalX, finalK);
            SwingUtilities.invokeLater(() -> displayResults(resultArea2, results, finalX, finalK));
        }).start();
    }

    // ─────────────────────────────────────────
    // Sonuçları text area'ya yaz
    // ─────────────────────────────────────────
    private void displayResults(JTextArea area, List<String> results, int X, int K) {
        if (results.isEmpty()) {
            area.setText("Öneri bulunamadı. X ve K değerlerini küçültmeyi deneyin.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Top ").append(X).append(" × ").append(K)
          .append(" = ").append(X * K).append(" Öneri:\n");
        sb.append("─".repeat(50)).append("\n");

        int userIdx = 1;
        for (int i = 0; i < results.size(); i++) {
            if (i > 0 && i % K == 0) {
                sb.append("\n");
                userIdx++;
            }
            if (i % K == 0) {
                sb.append("[ Kullanıcı ").append(userIdx).append(" önerileri ]\n");
            }
            sb.append("  ").append(i % K + 1).append(". ").append(results.get(i)).append("\n");
        }

        area.setText(sb.toString());
        area.setCaretPosition(0);
    }

    // ─────────────────────────────────────────
    // MAIN
    // ─────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}