import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.File;
import java.util.*;
import java.util.List;

/*
 * -------------------------------------------------------
 *  MainFrame — Sinema temalı karanlık GUI
 * -------------------------------------------------------
 *  Ekran 1: Hedef kullanıcı seç → K benzer kullanıcı → X film → X×K öneri
 *  Ekran 2: 5 film puanla → K benzer kullanıcı → X film → X×K öneri
 *
 *  K = heap'ten çekilecek BENZER KULLANICI sayısı
 *  X = her kullanıcıdan alınacak FİLM sayısı
 */
public class MainFrame extends JFrame {

    // ── Renk paleti ──────────────────────────────────────
    static final Color BG         = new Color(12, 12, 18);
    static final Color SURFACE    = new Color(20, 20, 30);
    static final Color CARD       = new Color(26, 26, 40);
    static final Color CARD_HOVER = new Color(34, 34, 52);
    static final Color BORDER     = new Color(45, 45, 65);
    static final Color GOLD       = new Color(212, 175, 95);
    static final Color GOLD_DIM   = new Color(60, 48, 20);
    static final Color TEXT       = new Color(230, 230, 245);
    static final Color TEXT_DIM   = new Color(130, 130, 160);
    static final Color TEXT_MUTED = new Color(65, 65, 90);
    static final Color RED_SOFT   = new Color(200, 80, 80);

    // ── Veri ─────────────────────────────────────────────
    private List<User> allUsers;
    private List<User> targetUsers;
    private Map<Integer, Movie> movies;
    private Recommender recommender;
    private List<Movie> randomMovies;

    // ── Ekran 1 bileşenleri ───────────────────────────────
    private JComboBox<String> targetCombo;
    private JTextField kField1;
    private JTextField xField1;
    private JPanel resultPanel1;

    // ── Ekran 2 bileşenleri ───────────────────────────────
    @SuppressWarnings("unchecked")
    private JComboBox<String>[] movieCombos = new JComboBox[5];

    private JTextField[] ratingFields = new JTextField[5];
    private int[] movieIdMap;

    private JTextField kField2;
    private JTextField xField2;
    private JPanel resultPanel2;

    // ── Navigasyon ────────────────────────────────────────
    private JPanel contentArea;
    private JButton[] navBtns = new JButton[2];
    private int activeTab = 0;

    public MainFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 700);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);
        setTitle("CineMatch — Film Öneri Sistemi");
        getContentPane().setBackground(BG);

        loadData();
        buildUI();

        setVisible(true);
    }

    // CSV dosya yolunu bulur
    private String csvPath(String name) {
        for (String path : new String[]{
                name,
                "src" + File.separator + "CSV" + File.separator + name,
                "CSV" + File.separator + name
        }) {
            if (new File(path).exists()) {
                return path;
            }
        }

        return "src" + File.separator + "CSV" + File.separator + name;
    }

    private void loadData() {
        allUsers    = CSVReader.readMainData(csvPath("main_data.csv"));
        targetUsers = CSVReader.readTargetUsers(csvPath("target_user.csv"));
        movies      = CSVReader.readMovies(csvPath("movies.csv"));
        recommender = new Recommender(allUsers, movies);

        // Ekran 2 için movies.csv içinden rastgele 10 film seçilir.
        // Proje dokümanında combo box'ın tüm filmleri değil,
        // rastgele seçilmiş 10 filmi göstermesi isteniyor.
        randomMovies = new ArrayList<>();

        List<Movie> allMovieList = new ArrayList<>(movies.values());
        Collections.shuffle(allMovieList);

        for (Movie movie : allMovieList) {
            randomMovies.add(movie);

            if (randomMovies.size() == 10) {
                break;
            }
        }
    }

    // ── Ana layout ────────────────────────────────────────
    private void buildUI() {
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);

        contentArea = new JPanel(new CardLayout());
        contentArea.setBackground(BG);

        contentArea.add(buildScreen1(), "s1");
        contentArea.add(buildScreen2(), "s2");

        add(contentArea, BorderLayout.CENTER);
    }

    // ── Sol sidebar ───────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SURFACE);
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));

        sidebar.add(Box.createVerticalStrut(24));
        sidebar.add(logoLabel());
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(subLabel("Film Öneri Sistemi"));
        sidebar.add(Box.createVerticalStrut(28));
        sidebar.add(divider());
        sidebar.add(Box.createVerticalStrut(12));

        String[] labels = {"Kullanıcıya Göre", "Filme Göre"};
        String[] icons  = {"◉", "★"};

        for (int i = 0; i < 2; i++) {
            final int index = i;

            navBtns[i] = buildNavBtn(icons[i], labels[i]);
            navBtns[i].addActionListener(e -> switchTab(index));

            sidebar.add(navBtns[i]);
            sidebar.add(Box.createVerticalStrut(4));
        }

        sidebar.add(Box.createVerticalStrut(24));
        sidebar.add(divider());
        sidebar.add(Box.createVerticalStrut(16));

        sidebar.add(statRow("Kullanıcı", String.valueOf(allUsers.size())));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(statRow("Film", String.valueOf(movies.size())));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(statRow("Hedef", String.valueOf(targetUsers.size())));

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(footerLabel());
        sidebar.add(Box.createVerticalStrut(12));

        return sidebar;
    }

    private JLabel logoLabel() {
        JLabel label = new JLabel("  🎬 CineMatch");
        label.setFont(new Font("Serif", Font.BOLD, 20));
        label.setForeground(GOLD);
        label.setAlignmentX(LEFT_ALIGNMENT);

        return label;
    }

    private JLabel subLabel(String text) {
        JLabel label = new JLabel("  " + text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        label.setForeground(TEXT_MUTED);
        label.setAlignmentX(LEFT_ALIGNMENT);

        return label;
    }

    private JButton buildNavBtn(String icon, String label) {
        JButton button = new JButton(icon + "  " + label) {
            @Override
            protected void paintComponent(Graphics g) {
                boolean selected;

                if (label.equals("Kullanıcıya Göre")) {
                    selected = activeTab == 0;
                } else {
                    selected = activeTab == 1;
                }

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                if (selected) {
                    g2.setColor(GOLD_DIM);
                } else if (getModel().isRollover()) {
                    g2.setColor(CARD);
                } else {
                    g2.setColor(SURFACE);
                }

                g2.fillRoundRect(8, 2, getWidth() - 16, getHeight() - 4, 8, 8);

                if (selected) {
                    g2.setColor(GOLD);
                    g2.fillRoundRect(8, 2, 3, getHeight() - 4, 3, 3);
                }

                if (selected) {
                    g2.setColor(GOLD);
                } else if (getModel().isRollover()) {
                    g2.setColor(TEXT);
                } else {
                    g2.setColor(TEXT_DIM);
                }

                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();

                g2.drawString(
                        getText(),
                        20,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2
                );

                g2.dispose();
            }
        };

        button.setFont(new Font("SansSerif", Font.PLAIN, 13));
        button.setMaximumSize(new Dimension(210, 40));
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return button;
    }

    private JPanel statRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(SURFACE);
        row.setMaximumSize(new Dimension(210, 26));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setBorder(new EmptyBorder(0, 18, 0, 14));

        JLabel keyLabel = new JLabel(label);
        keyLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        keyLabel.setForeground(TEXT_DIM);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        valueLabel.setForeground(GOLD);

        row.add(keyLabel, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);

        return row;
    }

    private JLabel footerLabel() {
        JLabel label = new JLabel("  Collaborative Filtering");
        label.setFont(new Font("SansSerif", Font.PLAIN, 10));
        label.setForeground(TEXT_MUTED);
        label.setAlignmentX(LEFT_ALIGNMENT);

        return label;
    }

    private JSeparator divider() {
        JSeparator separator = new JSeparator();
        separator.setForeground(BORDER);
        separator.setMaximumSize(new Dimension(210, 1));
        separator.setAlignmentX(LEFT_ALIGNMENT);

        return separator;
    }

    private void switchTab(int index) {
        activeTab = index;

        for (JButton button : navBtns) {
            button.repaint();
        }

        CardLayout cardLayout = (CardLayout) contentArea.getLayout();

        if (index == 0) {
            cardLayout.show(contentArea, "s1");
        } else {
            cardLayout.show(contentArea, "s2");
        }
    }

    // ─────────────────────────────────────────────────────
    //  EKRAN 1: Hedef Kullanıcıya Göre Öneri
    //
    //  K = heap'ten kaç benzer kullanıcı çekileceği
    //  X = her kullanıcıdan kaç film alınacağı
    // ─────────────────────────────────────────────────────
    private JPanel buildScreen1() {
        JPanel screen = new JPanel(new BorderLayout());
        screen.setBackground(BG);

        screen.add(
                topBar(
                        "Kullanıcıya Göre Öneri",
                        "Hedef kullanıcı seç  →  K benzer kullanıcı (heap)  →  X film  →  X×K öneri"
                ),
                BorderLayout.NORTH
        );

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(BG);
        inner.setBorder(new EmptyBorder(20, 24, 16, 24));

        kField1 = numField("3");
        xField1 = numField("5");

        inner.add(paramRow(kField1, xField1));
        inner.add(Box.createVerticalStrut(14));

        inner.add(controlRow1());
        inner.add(Box.createVerticalStrut(14));

        resultPanel1 = resultContainer();
        resultPanel1.add(placeholder("Kullanıcı seçin ve 'Önerileri Getir' butonuna basın."));

        screen.add(inner, BorderLayout.NORTH);
        screen.add(scrollWrap(resultPanel1), BorderLayout.CENTER);

        return screen;
    }

    private JPanel controlRow1() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setBackground(BG);
        row.setAlignmentX(LEFT_ALIGNMENT);

        targetCombo = styledCombo();

        for (User user : targetUsers) {
            targetCombo.addItem("Kullanıcı " + user.userId);
        }

        targetCombo.setPreferredSize(new Dimension(170, 36));

        JButton button = goldButton("Önerileri Getir");
        button.addActionListener(e -> runScreen1());

        row.add(fieldLabel("Hedef Kullanıcı"));
        row.add(targetCombo);
        row.add(Box.createHorizontalStrut(16));
        row.add(button);

        return row;
    }

    private void runScreen1() {
        int selectedIndex = targetCombo.getSelectedIndex();

        if (selectedIndex < 0 || selectedIndex >= targetUsers.size()) {
            return;
        }

        // K = benzer kullanıcı sayısı
        // X = her kullanıcıdan alınacak film sayısı
        int similarUsers = parseField(kField1, "K — benzer kullanıcı sayısı");
        int moviesPerUser = parseField(xField1, "X — film / kullanıcı");

        if (similarUsers < 0 || moviesPerUser < 0) {
            return;
        }

        User targetUser = targetUsers.get(selectedIndex);
        showLoading(resultPanel1);

        final int finalSimilarUsers = similarUsers;
        final int finalMoviesPerUser = moviesPerUser;

        new Thread(() -> {
            List<String> results = recommender.recommend(
                    targetUser,
                    finalSimilarUsers,
                    finalMoviesPerUser
            );

            SwingUtilities.invokeLater(() ->
                    displayResults(
                            resultPanel1,
                            results,
                            finalSimilarUsers,
                            finalMoviesPerUser
                    )
            );
        }).start();
    }

    // ─────────────────────────────────────────────────────
    //  EKRAN 2: Film Puanına Göre Öneri
    //
    //  K = heap'ten kaç benzer kullanıcı çekileceği
    //  X = her kullanıcıdan kaç film alınacağı
    // ─────────────────────────────────────────────────────
    private JPanel buildScreen2() {
        JPanel screen = new JPanel(new BorderLayout());
        screen.setBackground(BG);

        screen.add(
                topBar(
                        "Filme Göre Öneri",
                        "5 film puan ver  →  K benzer kullanıcı (heap)  →  X film  →  X×K öneri"
                ),
                BorderLayout.NORTH
        );

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(BG);
        inner.setBorder(new EmptyBorder(20, 24, 16, 24));

        kField2 = numField("3");
        xField2 = numField("5");

        inner.add(paramRow(kField2, xField2));
        inner.add(Box.createVerticalStrut(14));

        inner.add(movieGrid());
        inner.add(Box.createVerticalStrut(12));

        inner.add(controlRow2());
        inner.add(Box.createVerticalStrut(14));

        resultPanel2 = resultContainer();
        resultPanel2.add(placeholder("5 film seçin, puan girin ve 'Önerileri Getir' butonuna basın."));

        screen.add(inner, BorderLayout.NORTH);
        screen.add(scrollWrap(resultPanel2), BorderLayout.CENTER);

        return screen;
    }

    private JPanel movieGrid() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD);
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER, 1, true),
                        new EmptyBorder(14, 16, 14, 16)
                )
        );
        card.setAlignmentX(LEFT_ALIGNMENT);

        JLabel header = new JLabel("5 Film Seç ve Puan Ver  (1–5 tam sayı)");
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setForeground(GOLD);
        header.setAlignmentX(LEFT_ALIGNMENT);

        card.add(header);
        card.add(Box.createVerticalStrut(10));

        JPanel columnHeader = new JPanel(new BorderLayout(8, 0));
        columnHeader.setBackground(CARD);
        columnHeader.setAlignmentX(LEFT_ALIGNMENT);
        columnHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel filmHeader = new JLabel("Film Adı");
        filmHeader.setFont(new Font("SansSerif", Font.PLAIN, 11));
        filmHeader.setForeground(TEXT_MUTED);

        JLabel ratingHeader = new JLabel("Puan");
        ratingHeader.setFont(new Font("SansSerif", Font.PLAIN, 11));
        ratingHeader.setForeground(TEXT_MUTED);
        ratingHeader.setPreferredSize(new Dimension(70, 16));

        columnHeader.add(filmHeader, BorderLayout.CENTER);
        columnHeader.add(ratingHeader, BorderLayout.EAST);

        card.add(columnHeader);
        card.add(Box.createVerticalStrut(6));

        if (randomMovies == null || randomMovies.isEmpty()) {
            JLabel emptyLabel = new JLabel("Film listesi bulunamadı. movies.csv dosyasını kontrol edin.");
            emptyLabel.setForeground(RED_SOFT);
            emptyLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            card.add(emptyLabel);

            movieIdMap = new int[0];
            return card;
        }

        String[] titles = new String[randomMovies.size()];
        movieIdMap = new int[randomMovies.size()];

        for (int j = 0; j < randomMovies.size(); j++) {
            titles[j] = randomMovies.get(j).title;
            movieIdMap[j] = randomMovies.get(j).movieId;
        }

        for (int i = 0; i < 5; i++) {
            movieCombos[i] = styledCombo();

            for (String title : titles) {
                movieCombos[i].addItem(title);
            }

            if (!randomMovies.isEmpty()) {
                movieCombos[i].setSelectedIndex(i % randomMovies.size());
            }

            ratingFields[i] = new JTextField("3");
            ratingFields[i].setPreferredSize(new Dimension(60, 32));
            ratingFields[i].setBackground(CARD);
            ratingFields[i].setForeground(GOLD);
            ratingFields[i].setCaretColor(GOLD);
            ratingFields[i].setFont(new Font("SansSerif", Font.BOLD, 14));
            ratingFields[i].setHorizontalAlignment(JTextField.CENTER);
            ratingFields[i].setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(BORDER, 1, true),
                            new EmptyBorder(4, 6, 4, 6)
                    )
            );

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBackground(CARD);
            row.setAlignmentX(LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            row.setBorder(new EmptyBorder(2, 0, 2, 0));

            JLabel numberLabel = new JLabel((i + 1) + ".");
            numberLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            numberLabel.setForeground(TEXT_MUTED);
            numberLabel.setPreferredSize(new Dimension(22, 32));

            JPanel left = new JPanel(new BorderLayout(6, 0));
            left.setBackground(CARD);
            left.add(numberLabel, BorderLayout.WEST);
            left.add(movieCombos[i], BorderLayout.CENTER);

            row.add(left, BorderLayout.CENTER);
            row.add(ratingFields[i], BorderLayout.EAST);

            card.add(row);

            if (i < 4) {
                card.add(Box.createVerticalStrut(4));
            }
        }

        return card;
    }

    private JPanel controlRow2() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setBackground(BG);
        row.setAlignmentX(LEFT_ALIGNMENT);

        JButton button = goldButton("Önerileri Getir");
        button.addActionListener(e -> runScreen2());

        row.add(button);

        return row;
    }

    private void runScreen2() {
        // K = benzer kullanıcı sayısı
        // X = her kullanıcıdan alınacak film sayısı
        int similarUsers = parseField(kField2, "K — benzer kullanıcı sayısı");
        int moviesPerUser = parseField(xField2, "X — film / kullanıcı");

        if (similarUsers < 0 || moviesPerUser < 0) {
            return;
        }

        if (movieIdMap == null || movieIdMap.length == 0) {
            showErr("Film listesi boş. movies.csv dosyasını kontrol edin.");
            return;
        }

        Map<Integer, Integer> ratings = new LinkedHashMap<>();
        Set<Integer> selectedMovieIds = new HashSet<>();

        for (int i = 0; i < 5; i++) {
            int comboIndex = movieCombos[i].getSelectedIndex();

            if (comboIndex < 0 || comboIndex >= movieIdMap.length) {
                showErr((i + 1) + ". satırda film seçili değil.");
                return;
            }

            int movieId = movieIdMap[comboIndex];

            if (selectedMovieIds.contains(movieId)) {
                showErr((i + 1) + ". satırda tekrar eden film var. Farklı filmler seçin.");
                return;
            }

            String ratingText = ratingFields[i].getText().trim();

            int rating;

            try {
                rating = Integer.parseInt(ratingText);
            } catch (NumberFormatException exception) {
                showErr((i + 1) + ". puan tam sayı olmalı.");
                return;
            }

            if (rating < 1 || rating > 5) {
                showErr((i + 1) + ". puan 1 ile 5 arasında olmalı.");
                return;
            }

            selectedMovieIds.add(movieId);
            ratings.put(movieId, rating);
        }

        System.out.println("=== Ekran 2 Girdi ===");

        for (Map.Entry<Integer, Integer> entry : ratings.entrySet()) {
            int movieId = entry.getKey();
            int rating = entry.getValue();

            Movie movie = movies.get(movieId);

            System.out.println(
                    "Film ID: " + movieId +
                            " | Puan: " + rating +
                            " | Ad: " + (movie != null ? movie.title : "?")
            );
        }

        showLoading(resultPanel2);

        final int finalSimilarUsers = similarUsers;
        final int finalMoviesPerUser = moviesPerUser;
        final Map<Integer, Integer> finalRatings = new LinkedHashMap<>(ratings);

        new Thread(() -> {
            List<String> results = recommender.recommendFromRatings(
                    finalRatings,
                    finalSimilarUsers,
                    finalMoviesPerUser
            );

            SwingUtilities.invokeLater(() ->
                    displayResults(
                            resultPanel2,
                            results,
                            finalSimilarUsers,
                            finalMoviesPerUser
                    )
            );
        }).start();
    }

    // ── Sonuçları göster ─────────────────────────────────
    private void displayResults(JPanel panel,
                                List<String> results,
                                int similarUsers,
                                int moviesPerUser) {

        panel.removeAll();

        int targetTotal = similarUsers * moviesPerUser;

        if (results == null || results.isEmpty()) {
            panel.add(placeholder("Öneri bulunamadı. X veya K değerini küçültün."));
            panel.revalidate();
            panel.repaint();
            return;
        }

        String summary = results.size()
                + " öneri  (K="
                + similarUsers
                + " kullanıcı × X="
                + moviesPerUser
                + " film = hedef "
                + targetTotal
                + ")";

        if (results.size() < targetTotal) {
            summary += "  — bazı kullanıcıların uygun filmi yetersiz";
        }

        JLabel header = new JLabel("  " + summary);
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setForeground(GOLD);
        header.setBorder(new EmptyBorder(10, 0, 10, 0));
        header.setAlignmentX(LEFT_ALIGNMENT);

        panel.add(header);

        for (int i = 0; i < results.size(); i++) {
            if (i % moviesPerUser == 0) {
                int userNo = i / moviesPerUser + 1;

                JLabel groupLabel = new JLabel("   ▸  Benzer Kullanıcı " + userNo);
                groupLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
                groupLabel.setForeground(TEXT_MUTED);
                groupLabel.setBorder(new EmptyBorder(i == 0 ? 0 : 12, 0, 4, 0));
                groupLabel.setAlignmentX(LEFT_ALIGNMENT);

                panel.add(groupLabel);
            }

            panel.add(movieCard(i + 1, results.get(i), i / moviesPerUser + 1));
            panel.add(Box.createVerticalStrut(3));
        }

        panel.add(Box.createVerticalStrut(20));

        panel.revalidate();
        panel.repaint();
    }

    private JPanel movieCard(int rank, String title, int userNo) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(CARD);
        row.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER, 1, true),
                        new EmptyBorder(9, 14, 9, 14)
                )
        );
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel badge = new JLabel(String.valueOf(rank), SwingConstants.CENTER);
        badge.setFont(new Font("SansSerif", Font.BOLD, 11));
        badge.setForeground(GOLD);
        badge.setOpaque(true);
        badge.setBackground(GOLD_DIM);
        badge.setPreferredSize(new Dimension(28, 28));
        badge.setBorder(BorderFactory.createLineBorder(new Color(90, 70, 20), 1, true));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        titleLabel.setForeground(TEXT);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setBackground(CARD);
        left.add(badge);
        left.add(titleLabel);

        JLabel userBadge = new JLabel(" U" + userNo + " ");
        userBadge.setFont(new Font("SansSerif", Font.PLAIN, 11));
        userBadge.setForeground(TEXT_DIM);
        userBadge.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));

        MouseAdapter hover = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                row.setBackground(CARD_HOVER);
                left.setBackground(CARD_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                row.setBackground(CARD);
                left.setBackground(CARD);
            }
        };

        row.addMouseListener(hover);
        left.addMouseListener(hover);

        row.add(left, BorderLayout.CENTER);
        row.add(userBadge, BorderLayout.EAST);

        return row;
    }

    // ── Parametre satırı ──────────────────────────────────
    private JPanel paramRow(JTextField kField, JTextField xField) {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setBackground(BG);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));

        row.add(
                paramCard(
                        "K  —  Benzer Kullanıcı Sayısı",
                        kField,
                        "Heap'ten kaç benzer kullanıcı çekilsin"
                )
        );

        row.add(
                paramCard(
                        "X  —  Film / Kullanıcı",
                        xField,
                        "Her kullanıcıdan kaç film alınsın"
                )
        );

        JLabel totalLabel = totalLabel(kField, xField);

        row.add(
                staticCard(
                        "Toplam Öneri  (X × K)",
                        totalLabel
                )
        );

        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateTotal(kField, xField, totalLabel);
            }
        };

        kField.addKeyListener(keyAdapter);
        xField.addKeyListener(keyAdapter);

        return row;
    }

    private JPanel paramCard(String label, JTextField field, String hint) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD);
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER, 1, true),
                        new EmptyBorder(10, 14, 10, 14)
                )
        );

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("SansSerif", Font.PLAIN, 10));
        labelComponent.setForeground(TEXT_MUTED);
        labelComponent.setAlignmentX(LEFT_ALIGNMENT);

        field.setFont(new Font("SansSerif", Font.BOLD, 22));
        field.setForeground(GOLD);
        field.setBackground(CARD);
        field.setBorder(null);
        field.setCaretColor(GOLD);
        field.setAlignmentX(LEFT_ALIGNMENT);

        JLabel hintLabel = new JLabel(hint);
        hintLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        hintLabel.setForeground(TEXT_MUTED);
        hintLabel.setAlignmentX(LEFT_ALIGNMENT);

        card.add(labelComponent);
        card.add(Box.createVerticalStrut(2));
        card.add(field);
        card.add(Box.createVerticalStrut(2));
        card.add(hintLabel);

        return card;
    }

    private JPanel staticCard(String label, JLabel valueLabel) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD);
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER, 1, true),
                        new EmptyBorder(10, 14, 10, 14)
                )
        );

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("SansSerif", Font.PLAIN, 10));
        labelComponent.setForeground(TEXT_MUTED);
        labelComponent.setAlignmentX(LEFT_ALIGNMENT);

        valueLabel.setAlignmentX(LEFT_ALIGNMENT);

        card.add(labelComponent);
        card.add(Box.createVerticalStrut(2));
        card.add(valueLabel);

        return card;
    }

    private JLabel totalLabel(JTextField kField, JTextField xField) {
        JLabel label = new JLabel("15");
        label.setFont(new Font("SansSerif", Font.BOLD, 22));
        label.setForeground(GOLD);

        updateTotal(kField, xField, label);

        return label;
    }

    private void updateTotal(JTextField kField, JTextField xField, JLabel label) {
        try {
            int k = Integer.parseInt(kField.getText().trim());
            int x = Integer.parseInt(xField.getText().trim());

            label.setForeground(GOLD);
            label.setText(String.valueOf(k * x));
        } catch (NumberFormatException exception) {
            label.setForeground(RED_SOFT);
            label.setText("?");
        }
    }

    // ── Yardımcı GUI bileşenleri ──────────────────────────
    private JPanel topBar(String title, String subtitle) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(SURFACE);
        bar.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                        new EmptyBorder(16, 24, 16, 24)
                )
        );

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 17));
        titleLabel.setForeground(TEXT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subtitleLabel.setForeground(TEXT_MUTED);

        JPanel texts = new JPanel();
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.setBackground(SURFACE);
        texts.add(titleLabel);
        texts.add(Box.createVerticalStrut(3));
        texts.add(subtitleLabel);

        bar.add(texts, BorderLayout.WEST);

        return bar;
    }

    private JComboBox<String> styledCombo() {
        JComboBox<String> comboBox = new JComboBox<>();

        comboBox.setBackground(CARD);
        comboBox.setForeground(TEXT);
        comboBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        comboBox.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));

        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                                                          Object value,
                                                          int index,
                                                          boolean isSelected,
                                                          boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list,
                        value,
                        index,
                        isSelected,
                        cellHasFocus
                );

                if (isSelected) {
                    label.setBackground(GOLD_DIM);
                    label.setForeground(GOLD);
                } else {
                    label.setBackground(CARD);
                    label.setForeground(TEXT);
                }

                label.setBorder(new EmptyBorder(5, 10, 5, 10));

                return label;
            }
        });

        return comboBox;
    }

    private JButton goldButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                Color backgroundColor;

                if (getModel().isPressed()) {
                    backgroundColor = GOLD.darker();
                } else if (getModel().isRollover()) {
                    backgroundColor = GOLD.brighter();
                } else {
                    backgroundColor = GOLD;
                }

                g2.setColor(backgroundColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                g2.setColor(new Color(20, 15, 5));
                g2.setFont(getFont());

                FontMetrics fm = g2.getFontMetrics();

                g2.drawString(
                        getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2
                );

                g2.dispose();
            }
        };

        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(155, 36));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return button;
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        label.setForeground(TEXT_DIM);

        return label;
    }

    private JLabel placeholder(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.ITALIC, 13));
        label.setForeground(TEXT_MUTED);
        label.setBorder(new EmptyBorder(20, 0, 0, 0));
        label.setAlignmentX(LEFT_ALIGNMENT);

        return label;
    }

    private JTextField numField(String value) {
        JTextField field = new JTextField(value, 4);

        field.setBackground(CARD);
        field.setForeground(GOLD);
        field.setCaretColor(GOLD);
        field.setFont(new Font("SansSerif", Font.BOLD, 14));
        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER, 1, true),
                        new EmptyBorder(6, 10, 6, 10)
                )
        );

        return field;
    }

    private JPanel resultContainer() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG);

        return panel;
    }

    private JScrollPane scrollWrap(JPanel panel) {
        JScrollPane scrollPane = new JScrollPane(panel);

        scrollPane.setBackground(BG);
        scrollPane.getViewport().setBackground(BG);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        return scrollPane;
    }

    private void showLoading(JPanel panel) {
        panel.removeAll();
        panel.add(placeholder("Hesaplanıyor..."));
        panel.revalidate();
        panel.repaint();
    }

    private int parseField(JTextField field, String name) {
        try {
            int value = Integer.parseInt(field.getText().trim());

            if (value <= 0) {
                showErr(name + " 0'dan büyük olmalı.");
                return -1;
            }

            return value;
        } catch (NumberFormatException exception) {
            showErr(name + " tam sayı olmalı.");
            return -1;
        }
    }

    private void showErr(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Hata",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName()
            );
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(MainFrame::new);
    }
}