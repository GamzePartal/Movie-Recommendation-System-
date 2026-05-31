import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame {

    // ── Renkler ──────────────────────────────────────────
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

    // ── Fontlar ───────────────────────────────────────────
    static final Font FONT_BOLD_22  = new Font("SansSerif", Font.BOLD, 22);
    static final Font FONT_BOLD_13  = new Font("SansSerif", Font.BOLD, 13);
    static final Font FONT_BOLD_12  = new Font("SansSerif", Font.BOLD, 12);
    static final Font FONT_BOLD_11  = new Font("SansSerif", Font.BOLD, 11);
    static final Font FONT_PLAIN_13 = new Font("SansSerif", Font.PLAIN, 13);
    static final Font FONT_PLAIN_12 = new Font("SansSerif", Font.PLAIN, 12);
    static final Font FONT_PLAIN_11 = new Font("SansSerif", Font.PLAIN, 11);
    static final Font FONT_PLAIN_10 = new Font("SansSerif", Font.PLAIN, 10);

    // ── Veri ─────────────────────────────────────────────
    private List<User>          allUsers;
    private List<User>          targetUsers;
    private Map<Integer, Movie> movies;
    private Recommender         recommender;
    private List<Movie>         randomMovies;

    // ── Ekran bileşenleri ─────────────────────────────────
    private JComboBox<String>   targetCombo;
    private JTextField          kField1, xField1;
    private JPanel              resultPanel1;

    private JComboBox<String>[] movieCombos  = new JComboBox[5];
    private JTextField[]        ratingFields = new JTextField[5];
    private int[]               movieIdMap;
    private JTextField          kField2, xField2;
    private JPanel              resultPanel2;

    private JPanel   contentArea;
    private JButton[] navBtns   = new JButton[2];
    private int       activeTab = 0;

    // ─────────────────────────────────────────────────────
    public MainFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 700);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        loadData();
        buildUI();
        setVisible(true);
    }

    // ── CSV yolu ──────────────────────────────────────────
    private String csvPath(String name) {
        for (String path : new String[]{
                name,
                "src" + File.separator + "CSV" + File.separator + name,
                "CSV" + File.separator + name
        }) {
            if (new File(path).exists()) return path;
        }
        return "src" + File.separator + "CSV" + File.separator + name;
    }

    // ── Veri yükleme ──────────────────────────────────────
    private void loadData() {
        allUsers    = CSVReader.readMainData(csvPath("main_data.csv"));
        targetUsers = CSVReader.readTargetUsers(csvPath("target_user.csv"));
        movies      = CSVReader.readMovies(csvPath("movies.csv"));
        recommender = new Recommender(allUsers, movies);

        Set<Integer> validMovieIds = new HashSet<>();
        for (User u : allUsers) {
            validMovieIds.addAll(u.ratings.keySet());
        }

        randomMovies = new ArrayList<>();
        List<Movie> filteredMovies = new ArrayList<>();
        for (int id : validMovieIds) {
            if (movies.containsKey(id)) {
                filteredMovies.add(movies.get(id));
            }
        }
        Collections.shuffle(filteredMovies);
        for (Movie m : filteredMovies) {
            randomMovies.add(m);
            if (randomMovies.size() == 10) break;
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

    // ── Sidebar ───────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(SURFACE);
        sb.setPreferredSize(new Dimension(210, 0));
        sb.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));

        sb.add(Box.createVerticalStrut(52));
        sb.add(divider());
        sb.add(Box.createVerticalStrut(12));

        String[] labels = {"Kullanıcıya Göre", "Filme Göre"};
        String[] icons  = {"◉", "★"};
        for (int i = 0; i < 2; i++) {
            final int idx = i;
            navBtns[i] = navButton(icons[i] + "  " + labels[i], i);
            navBtns[i].addActionListener(e -> switchTab(idx));
            sb.add(navBtns[i]);
            sb.add(Box.createVerticalStrut(4));
        }

        sb.add(Box.createVerticalStrut(24));
        sb.add(divider());
        sb.add(Box.createVerticalStrut(16));
        sb.add(statRow("Kullanıcı", String.valueOf(allUsers.size())));
        sb.add(Box.createVerticalStrut(6));
        sb.add(statRow("Film",      String.valueOf(movies.size())));
        sb.add(Box.createVerticalStrut(6));
        sb.add(statRow("Hedef",     String.valueOf(targetUsers.size())));
        sb.add(Box.createVerticalGlue());
        sb.add(styledLabel("  Collaborative Filtering", FONT_PLAIN_10, TEXT_MUTED));
        sb.add(Box.createVerticalStrut(12));
        return sb;
    }

    // Sidebar nav butonu — paintComponent yok, UIManager ile renklendirildi
    private JButton navButton(String text, int tabIndex) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_PLAIN_13);
        btn.setMaximumSize(new Dimension(210, 40));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(activeTab == tabIndex ? GOLD_DIM : SURFACE);
        btn.setForeground(activeTab == tabIndex ? GOLD : TEXT_DIM);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        return btn;
    }

    private void switchTab(int index) {
        activeTab = index;
        for (int i = 0; i < navBtns.length; i++) {
            navBtns[i].setBackground(activeTab == i ? GOLD_DIM : SURFACE);
            navBtns[i].setForeground(activeTab == i ? GOLD    : TEXT_DIM);
        }
        ((CardLayout) contentArea.getLayout()).show(contentArea, index == 0 ? "s1" : "s2");
    }

    // ── Ekran 1 ───────────────────────────────────────────
    private JPanel buildScreen1() {
        JPanel screen = new JPanel(new BorderLayout());
        screen.setBackground(BG);
        screen.add(topBar("Kullanıcıya Göre Öneri",
                        "Hedef kullanıcı seç  →  K benzer kullanıcı (heap)  →  X film  →  X×K öneri"),
                BorderLayout.NORTH);

        kField1 = numField("3");
        xField1 = numField("5");

        JPanel inner = vBox(BG, new EmptyBorder(20, 24, 16, 24));
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
        JPanel row = flowRow(BG, FlowLayout.LEFT, 10);
        targetCombo = styledCombo();
        for (User u : targetUsers) targetCombo.addItem("Kullanıcı " + u.userId);
        targetCombo.setPreferredSize(new Dimension(170, 36));

        JButton btn = goldButton("Önerileri Getir");
        btn.addActionListener(e -> runScreen1());

        row.add(styledLabel("Hedef Kullanıcı", FONT_PLAIN_12, TEXT_DIM));
        row.add(targetCombo);
        row.add(Box.createHorizontalStrut(16));
        row.add(btn);
        return row;
    }

    private void runScreen1() {
        int idx = targetCombo.getSelectedIndex();
        if (idx < 0 || idx >= targetUsers.size()) return;

        int k = parseField(kField1, "K — benzer kullanıcı sayısı", allUsers.size());
        int x = parseField(xField1, "X — film sayısı");
        if (k < 0 || x < 0) return;

        User target = targetUsers.get(idx);
        showLoading(resultPanel1);

        new Thread(() -> {
            List<String> results = recommender.recommend(target, k, x);
            SwingUtilities.invokeLater(() -> displayResults(resultPanel1, results, k, x));
        }).start();
    }

    // ── Ekran 2 ───────────────────────────────────────────
    private JPanel buildScreen2() {
        JPanel screen = new JPanel(new BorderLayout());
        screen.setBackground(BG);
        screen.add(topBar("Filme Göre Öneri",
                        "5 film puan ver  →  K benzer kullanıcı (heap)  →  X film  →  X×K öneri"),
                BorderLayout.NORTH);

        kField2 = numField("3");
        xField2 = numField("5");

        JPanel inner = vBox(BG, new EmptyBorder(20, 24, 16, 24));
        inner.add(paramRow(kField2, xField2));
        inner.add(Box.createVerticalStrut(14));
        inner.add(movieGrid());
        inner.add(Box.createVerticalStrut(12));
        inner.add(controlRow2());
        inner.add(Box.createVerticalStrut(14));

        resultPanel2 = resultContainer();
        resultPanel2.add(placeholder("5 film seçin, puan girin ve 'Önerileri Getir' butonuna basın"));

        screen.add(inner, BorderLayout.NORTH);
        screen.add(scrollWrap(resultPanel2), BorderLayout.CENTER);
        return screen;
    }

    private JPanel movieGrid() {
        JPanel card = vBox(CARD, new EmptyBorder(14, 16, 14, 16));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel header = styledLabel("5 Film Seç ve Puan Ver  (1–5 tam sayı)", FONT_BOLD_13, GOLD);
        card.add(header);
        card.add(Box.createVerticalStrut(10));

        // Sütun başlığı
        JPanel colHeader = new JPanel(new BorderLayout(8, 0));
        colHeader.setBackground(CARD);
        colHeader.setAlignmentX(LEFT_ALIGNMENT);
        colHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        colHeader.add(styledLabel("Film Adı", FONT_PLAIN_11, TEXT_MUTED), BorderLayout.CENTER);
        JLabel ph = styledLabel("Puan", FONT_PLAIN_11, TEXT_MUTED);
        ph.setPreferredSize(new Dimension(70, 16));
        colHeader.add(ph, BorderLayout.EAST);
        card.add(colHeader);
        card.add(Box.createVerticalStrut(6));

        if (randomMovies == null || randomMovies.isEmpty()) {
            card.add(styledLabel("Film listesi bulunamadı. movies.csv dosyasını kontrol edin.",
                    FONT_PLAIN_12, RED_SOFT));
            movieIdMap = new int[0];
            return card;
        }

        String[] titles = new String[randomMovies.size()];
        movieIdMap      = new int[randomMovies.size()];
        for (int j = 0; j < randomMovies.size(); j++) {
            titles[j]    = randomMovies.get(j).title;
            movieIdMap[j] = randomMovies.get(j).movieId;
        }

        for (int i = 0; i < 5; i++) {
            movieCombos[i] = styledCombo();
            for (String t : titles) movieCombos[i].addItem(t);
            if (!randomMovies.isEmpty()) movieCombos[i].setSelectedIndex(i % randomMovies.size());

            ratingFields[i] = numField("3");
            ratingFields[i].setPreferredSize(new Dimension(60, 32));
            ratingFields[i].setHorizontalAlignment(JTextField.CENTER);

            JLabel numLabel = styledLabel((i + 1) + ".", FONT_BOLD_12, TEXT_MUTED);
            numLabel.setPreferredSize(new Dimension(22, 32));

            JPanel left = new JPanel(new BorderLayout(6, 0));
            left.setBackground(CARD);
            left.add(numLabel, BorderLayout.WEST);
            left.add(movieCombos[i], BorderLayout.CENTER);

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBackground(CARD);
            row.setAlignmentX(LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            row.setBorder(new EmptyBorder(2, 0, 2, 0));
            row.add(left, BorderLayout.CENTER);
            row.add(ratingFields[i], BorderLayout.EAST);

            card.add(row);
            if (i < 4) card.add(Box.createVerticalStrut(4));
        }
        return card;
    }

    private JPanel controlRow2() {
        JPanel row = flowRow(BG, FlowLayout.LEFT, 10);
        JButton btn = goldButton("Önerileri Getir");
        btn.addActionListener(e -> runScreen2());
        row.add(btn);
        return row;
    }

    private void runScreen2() {
        int k = parseField(kField2, "K — benzer kullanıcı sayısı", allUsers.size());
        int x = parseField(xField2, "X — film / kullanıcı");
        if (k < 0 || x < 0) return;

        if (movieIdMap == null || movieIdMap.length == 0) {
            showErr("Film listesi boş. movies.csv dosyasını kontrol edin.");
            return;
        }

        Map<Integer, Integer> ratings         = new LinkedHashMap<>();
        Set<Integer>          selectedMovieIds = new HashSet<>();

        for (int i = 0; i < 5; i++) {
            int comboIdx = movieCombos[i].getSelectedIndex();
            if (comboIdx < 0 || comboIdx >= movieIdMap.length) {
                showErr((i + 1) + ". satırda film seçili değil.");
                return;
            }

            int movieId = movieIdMap[comboIdx];

            int rating;
            try {
                rating = Integer.parseInt(ratingFields[i].getText().trim());
            } catch (NumberFormatException e) {
                showErr((i + 1) + ". puan tam sayı olmalı.");
                return;
            }

            if (rating < 1 || rating > 5) {
                showErr((i + 1) + ". puan 1 ile 5 arasında olmalı.");
                return;
            }

            if (selectedMovieIds.contains(movieId)) {
                showErr((i + 1) + ". satırda aynı film başka bir satırda zaten seçili.\nLütfen 5 farklı film seçin.");
                return;
            }
            selectedMovieIds.add(movieId);
            ratings.put(movieId, rating);
        }

        showLoading(resultPanel2);
        final int finalK = k, finalX = x;
        final Map<Integer, Integer> finalRatings = new LinkedHashMap<>(ratings);

        new Thread(() -> {
            List<String> results = recommender.recommendFromRatings(finalRatings, finalK, finalX);
            SwingUtilities.invokeLater(() -> displayResults(resultPanel2, results, finalK, finalX));
        }).start();
    }

    // ── Sonuç gösterimi ───────────────────────────────────
    private void displayResults(JPanel panel, List<String> results, int k, int x) {
        panel.removeAll();
        int target = k * x;

        if (results == null || results.isEmpty()) {
            panel.add(placeholder("Öneri bulunamadı. X veya K değerini küçültün."));
            panel.revalidate();
            panel.repaint();
            return;
        }

        String summary = results.size() + " öneri  (K=" + k + " kullanıcı × X=" + x
                + " film = hedef " + target + ")"
                + (results.size() < target ? "  — bazı kullanıcıların uygun filmi yetersiz" : "");

        JLabel header = styledLabel("  " + summary, FONT_BOLD_12, GOLD);
        header.setBorder(new EmptyBorder(10, 0, 10, 0));
        header.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(header);

        for (int i = 0; i < results.size(); i++) {
            if (i % x == 0) {
                JLabel group = styledLabel("   ▸  Benzer Kullanıcı " + (i / x + 1),
                        FONT_BOLD_11, TEXT_MUTED);
                group.setBorder(new EmptyBorder(i == 0 ? 0 : 12, 0, 4, 0));
                group.setAlignmentX(LEFT_ALIGNMENT);
                panel.add(group);
            }
            panel.add(movieCard(i + 1, results.get(i)));
            panel.add(Box.createVerticalStrut(3));
        }

        panel.add(Box.createVerticalStrut(20));
        panel.revalidate();
        panel.repaint();
    }

    private JPanel movieCard(int rank, String title) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(CARD);
        row.setBorder(compoundBorder(BORDER, 9, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel badge = styledLabel(String.valueOf(rank), FONT_BOLD_11, GOLD);
        badge.setOpaque(true);
        badge.setBackground(GOLD_DIM);
        badge.setPreferredSize(new Dimension(28, 28));
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setBorder(BorderFactory.createLineBorder(new Color(90, 70, 20), 1, true));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setBackground(CARD);
        left.add(badge);
        left.add(styledLabel(title, FONT_PLAIN_13, TEXT));

        MouseAdapter hover = new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                row.setBackground(CARD_HOVER); left.setBackground(CARD_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                row.setBackground(CARD); left.setBackground(CARD);
            }
        };
        row.addMouseListener(hover);
        left.addMouseListener(hover);

        row.add(left, BorderLayout.CENTER);
        return row;
    }

    // ── Parametre satırı ──────────────────────────────────
    private JPanel paramRow(JTextField kField, JTextField xField) {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setBackground(BG);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));

        JLabel totalLabel = styledLabel("15", FONT_BOLD_22, GOLD);
        updateTotal(kField, xField, totalLabel);

        row.add(paramCard("K  —  Benzer Kullanıcı Sayısı", kField, "Heap'ten kaç benzer kullanıcı çekilsin"));
        row.add(paramCard("X  —  Film / Kullanıcı",        xField, "Her kullanıcıdan kaç film alınsın"));
        row.add(staticCard("Toplam Öneri  (X × K)",         totalLabel));

        KeyAdapter ka = new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                updateTotal(kField, xField, totalLabel);
            }
        };
        kField.addKeyListener(ka);
        xField.addKeyListener(ka);
        return row;
    }

    private JPanel paramCard(String label, JTextField field, String hint) {
        JPanel card = vBox(CARD, new EmptyBorder(10, 14, 10, 14));
        card.setBorder(compoundBorder(BORDER, 10, 14));
        field.setFont(FONT_BOLD_22);
        field.setForeground(GOLD);
        field.setBackground(CARD);
        field.setBorder(null);
        field.setCaretColor(GOLD);
        field.setAlignmentX(LEFT_ALIGNMENT);
        card.add(styledLabel(label, FONT_PLAIN_10, TEXT_MUTED));
        card.add(Box.createVerticalStrut(2));
        card.add(field);
        card.add(Box.createVerticalStrut(2));
        card.add(styledLabel(hint, FONT_PLAIN_10, TEXT_MUTED));
        return card;
    }

    private JPanel staticCard(String label, JLabel valueLabel) {
        JPanel card = vBox(CARD, new EmptyBorder(10, 14, 10, 14));
        card.setBorder(compoundBorder(BORDER, 10, 14));
        valueLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(styledLabel(label, FONT_PLAIN_10, TEXT_MUTED));
        card.add(Box.createVerticalStrut(2));
        card.add(valueLabel);
        return card;
    }

    private void updateTotal(JTextField kField, JTextField xField, JLabel label) {
        try {
            int k = Integer.parseInt(kField.getText().trim());
            int x = Integer.parseInt(xField.getText().trim());
            label.setForeground(GOLD);
            label.setText(String.valueOf(k * x));
        } catch (NumberFormatException e) {
            label.setForeground(RED_SOFT);
            label.setText("?");
        }
    }

    // ── Küçük GUI yardımcıları ────────────────────────────

    /** Dikey BoxLayout panel */
    private JPanel vBox(Color bg, EmptyBorder padding) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(bg);
        if (padding != null) p.setBorder(padding);
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }

    /** FlowLayout panel */
    private JPanel flowRow(Color bg, int align, int gap) {
        JPanel p = new JPanel(new FlowLayout(align, gap, 0));
        p.setBackground(bg);
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }

    /** Tek satır JLabel */
    private JLabel styledLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    /** Compound border kısayolu */
    private Border compoundBorder(Color lineColor, int vPad, int hPad) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(lineColor, 1, true),
                new EmptyBorder(vPad, hPad, vPad, hPad));
    }

    private JPanel topBar(String title, String subtitle) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(SURFACE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(16, 24, 16, 24)));

        JPanel texts = vBox(SURFACE, null);
        JLabel t = styledLabel(title, new Font("Serif", Font.BOLD, 17), TEXT);
        texts.add(t);
        texts.add(Box.createVerticalStrut(3));
        texts.add(styledLabel(subtitle, FONT_PLAIN_11, TEXT_MUTED));
        bar.add(texts, BorderLayout.WEST);
        return bar;
    }

    private JComboBox<String> styledCombo() {
        JComboBox<String> cb = new JComboBox<>();
        cb.setBackground(CARD);
        cb.setForeground(TEXT);
        cb.setFont(FONT_PLAIN_12);
        cb.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                l.setBackground(isSelected ? GOLD_DIM : CARD);
                l.setForeground(isSelected ? GOLD     : TEXT);
                l.setBorder(new EmptyBorder(5, 10, 5, 10));
                return l;
            }
        });
        return cb;
    }

    private JButton goldButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD_13);
        btn.setPreferredSize(new Dimension(155, 36));
        btn.setBackground(GOLD);
        btn.setForeground(new Color(20, 15, 5));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JTextField numField(String value) {
        JTextField f = new JTextField(value, 4);
        f.setBackground(CARD);
        f.setForeground(GOLD);
        f.setCaretColor(GOLD);
        f.setFont(FONT_BOLD_13);
        f.setBorder(compoundBorder(BORDER, 6, 10));
        return f;
    }

    private JPanel resultContainer() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG);
        return p;
    }

    private JScrollPane scrollWrap(JPanel panel) {
        JScrollPane sp = new JScrollPane(panel);
        sp.setBackground(BG);
        sp.getViewport().setBackground(BG);
        sp.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return sp;
    }

    private JLabel placeholder(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.ITALIC, 13));
        l.setForeground(TEXT_MUTED);
        l.setBorder(new EmptyBorder(20, 0, 0, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private void showLoading(JPanel panel) {
        panel.removeAll();
        panel.add(placeholder("Hesaplanıyor..."));
        panel.revalidate();
        panel.repaint();
    }

    private int parseField(JTextField field, String name) {
        return parseField(field, name, Integer.MAX_VALUE);
    }

    private int parseField(JTextField field, String name, int max) {
        try {
            int v = Integer.parseInt(field.getText().trim());
            if (v <= 0) { showErr(name + " 0'dan büyük olmalı."); return -1; }
            if (v > max) { showErr(name + " en fazla " + max + " olabilir."); return -1; }
            return v;
        } catch (NumberFormatException e) {
            showErr(name + " tam sayı olmalı.");
            return -1;
        }
    }

    private void showErr(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Hata", JOptionPane.ERROR_MESSAGE);
    }

    private JSeparator divider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        sep.setMaximumSize(new Dimension(210, 1));
        sep.setAlignmentX(LEFT_ALIGNMENT);
        return sep;
    }

    private JPanel statRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(SURFACE);
        row.setMaximumSize(new Dimension(210, 26));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setBorder(new EmptyBorder(0, 18, 0, 14));
        row.add(styledLabel(label, FONT_PLAIN_12, TEXT_DIM),  BorderLayout.WEST);
        row.add(styledLabel(value, FONT_BOLD_12,  GOLD),      BorderLayout.EAST);
        return row;
    }

    // ── Main ─────────────────────────────────────────────
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(MainFrame::new);
    }
}