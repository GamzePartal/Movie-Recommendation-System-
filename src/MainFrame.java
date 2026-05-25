import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame {

    // ── Renk Paleti (Dark Theme) ─────────────────────────────────────────
    static final Color BG_DEEP    = new Color(15,  15,  20);
    static final Color BG_PANEL   = new Color(23,  23,  31);
    static final Color BG_CARD    = new Color(26,  26,  36);
    static final Color BG_HOVER   = new Color(34,  34,  46);
    static final Color ACCENT     = new Color(127, 119, 221);
    static final Color ACCENT_DIM = new Color(30,  30,  54);
    static final Color BORDER     = new Color(30,  30,  40);
    static final Color TEXT_PRI   = new Color(224, 224, 240);
    static final Color TEXT_SEC   = new Color(100, 100, 130);
    static final Color TEXT_MUTED = new Color(60,  60,  80);
    static final Color BLUE_DIM   = new Color(30,  40,  54);
    static final Color BLUE_TEXT  = new Color(55,  138, 221);

    // ── Veri ─────────────────────────────────────────────────────────────
    private List<User>          allUsers;
    private List<User>          targetUsers;
    private Map<Integer, Movie> movies;
    private Recommender         recommender;
    private List<Movie>         randomMovies;

    // ── Ekran 1 bileşenleri ───────────────────────────────────────────────
    private JComboBox<String> targetCombo;
    private JTextField        xField1, kField1;
    private JLabel            totalLabel1;
    private JPanel            resultPanel1;

    // ── Ekran 2 bileşenleri ───────────────────────────────────────────────
    private JComboBox<String>[] movieCombos  = new JComboBox[5];
    private JTextField[]        ratingFields = new JTextField[5];
    private JTextField          xField2, kField2;
    private JLabel              totalLabel2;
    private JPanel              resultPanel2;

    // ── Navigasyon ────────────────────────────────────────────────────────
    private int       activeTab  = 0;
    private JPanel[]  navItems   = new JPanel[2];   // nav panel referansları
    private JPanel    contentArea;

    // ════════════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ════════════════════════════════════════════════════════════════════
    public MainFrame() {
        super("CineMatch — Film Öneri Sistemi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 680);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DEEP);

        loadData();
        buildUI();
        setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════
    // CSV YOLU: src/CSV/ klasörüne bak (IntelliJ yapısı)
    // ════════════════════════════════════════════════════════════════════
    private String csvPath(String filename) {
        // Önce proje root'unu dene, sonra src/CSV altını
        String[] candidates = {
                filename,
                "src" + File.separator + "CSV" + File.separator + filename,
                "CSV" + File.separator + filename
        };
        for (String path : candidates) {
            if (new File(path).exists()) return path;
        }
        // Bulunamazsa src/CSV ile dön (hata mesajı anlamlı olsun)
        return "src" + File.separator + "CSV" + File.separator + filename;
    }

    // ════════════════════════════════════════════════════════════════════
    // VERİ YÜKLEME
    // ════════════════════════════════════════════════════════════════════
    private void loadData() {
        allUsers    = CSVReader.readMainData(csvPath("main_data.csv"));
        targetUsers = CSVReader.readTargetUsers(csvPath("target_user.csv"));
        movies      = CSVReader.readMovies(csvPath("movies.csv"));
        recommender = new Recommender(allUsers, movies);

        List<Movie> movieList = new ArrayList<>(movies.values());
        Collections.shuffle(movieList);
        randomMovies = movieList.subList(0, Math.min(10, movieList.size()));
    }

    // ════════════════════════════════════════════════════════════════════
    // ANA YAPI: Sidebar (sol) + İçerik (sağ)
    // ════════════════════════════════════════════════════════════════════
    private void buildUI() {
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);

        contentArea = new JPanel(new CardLayout());
        contentArea.setBackground(BG_DEEP);
        contentArea.add(buildScreen1(), "screen1");
        contentArea.add(buildScreen2(), "screen2");
        add(contentArea, BorderLayout.CENTER);
    }

    // ════════════════════════════════════════════════════════════════════
    // SIDEBAR
    // ════════════════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_PANEL);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));

        // Logo satırı
        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 18));
        logoRow.setBackground(BG_PANEL);
        logoRow.setAlignmentX(LEFT_ALIGNMENT);
        logoRow.setMaximumSize(new Dimension(220, 64));
        JLabel logoIcon = new JLabel("🎬");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        JLabel logoText = new JLabel("CineMatch");
        logoText.setFont(new Font("SansSerif", Font.BOLD, 15));
        logoText.setForeground(TEXT_PRI);
        logoRow.add(logoIcon);
        logoRow.add(logoText);
        sidebar.add(logoRow);

        // Çizgi
        sidebar.add(makeSep());

        // Navigasyon öğeleri — referansları navItems[]'a kaydediyoruz
        String[] titles = {"Kullanıcıya Göre", "Puana Göre"};
        String[] icons  = {"👤", "⭐"};
        for (int i = 0; i < 2; i++) {
            final int idx = i;
            navItems[i] = buildNavItem(icons[i], titles[i], i == 0);
            navItems[i].addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) { switchTab(idx); }
                public void mouseEntered(MouseEvent e) {
                    if (activeTab != idx) navItems[idx].setBackground(BG_HOVER);
                }
                public void mouseExited(MouseEvent e) {
                    if (activeTab != idx) navItems[idx].setBackground(BG_PANEL);
                }
            });
            sidebar.add(navItems[i]);
        }

        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(makeSep());
        sidebar.add(Box.createVerticalStrut(10));

        // Alt istatistikler
        JLabel statsLbl = new JLabel("   VERİ KAYNAĞI");
        statsLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        statsLbl.setForeground(TEXT_MUTED);
        statsLbl.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(statsLbl);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(buildStatItem("📊", allUsers.size() + " Kullanıcı"));
        sidebar.add(buildStatItem("🎥", movies.size() + " Film"));
        sidebar.add(buildStatItem("🎯", targetUsers.size() + " Hedef"));

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JPanel buildNavItem(String icon, String title, boolean active) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 11));
        item.setBackground(active ? BG_HOVER : BG_PANEL);
        item.setAlignmentX(LEFT_ALIGNMENT);
        item.setMaximumSize(new Dimension(220, 44));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (active) {
            JPanel accent = new JPanel();
            accent.setBackground(ACCENT);
            accent.setPreferredSize(new Dimension(3, 18));
            item.add(accent);
        }
        JLabel ico = new JLabel(icon);
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lbl.setForeground(active ? TEXT_PRI : TEXT_SEC);
        item.add(ico);
        item.add(lbl);
        return item;
    }

    private JPanel buildStatItem(String icon, String text) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        item.setBackground(BG_PANEL);
        item.setAlignmentX(LEFT_ALIGNMENT);
        item.setMaximumSize(new Dimension(220, 32));
        JLabel ico = new JLabel(icon);
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(TEXT_SEC);
        item.add(ico);
        item.add(lbl);
        return item;
    }

    // Sekme geçişi: navItems[] referansları ile renk güncelle
    private void switchTab(int idx) {
        activeTab = idx;
        for (int i = 0; i < 2; i++) {
            navItems[i].setBackground(i == idx ? BG_HOVER : BG_PANEL);
            // İçindeki JLabel rengini güncelle (index: accent varsa +2, yoksa +1)
            for (Component c : navItems[i].getComponents()) {
                if (c instanceof JLabel) {
                    String txt = ((JLabel) c).getText();
                    if (txt != null && !txt.isEmpty() && txt.length() > 2) {
                        ((JLabel) c).setForeground(i == idx ? TEXT_PRI : TEXT_SEC);
                    }
                }
            }
            navItems[i].repaint();
        }
        ((CardLayout) contentArea.getLayout()).show(contentArea, idx == 0 ? "screen1" : "screen2");
    }

    private JSeparator makeSep() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        sep.setBackground(BORDER);
        sep.setMaximumSize(new Dimension(220, 1));
        sep.setAlignmentX(LEFT_ALIGNMENT);
        return sep;
    }

    // ════════════════════════════════════════════════════════════════════
    // EKRAN 1: Hedef Kullanıcıya Göre Öneri
    // ════════════════════════════════════════════════════════════════════
    private JPanel buildScreen1() {
        JPanel screen = new JPanel(new BorderLayout());
        screen.setBackground(BG_DEEP);
        screen.add(makeTopBar("Hedef Kullanıcıya Göre Öneri",
                "Cosine similarity  →  MaxHeap  →  Top X×K film"), BorderLayout.NORTH);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(BG_DEEP);
        inner.setBorder(new EmptyBorder(20, 24, 20, 24));

        xField1    = makeNumField("3");
        kField1    = makeNumField("5");
        totalLabel1 = makeTotalLabel();
        inner.add(makeMetricRow(xField1, kField1, totalLabel1, "Kullanıcıya Göre"));
        inner.add(Box.createVerticalStrut(16));
        inner.add(makeControlRow1());
        inner.add(Box.createVerticalStrut(20));

        resultPanel1 = new JPanel();
        resultPanel1.setLayout(new BoxLayout(resultPanel1, BoxLayout.Y_AXIS));
        resultPanel1.setBackground(BG_DEEP);
        resultPanel1.add(placeholder("Kullanıcı seçip 'Önerileri Getir' butonuna basın."));

        JScrollPane scroll = makeScroll(resultPanel1);
        inner.add(scroll);
        screen.add(inner, BorderLayout.CENTER);
        return screen;
    }

    private JPanel makeControlRow1() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setBackground(BG_DEEP);
        row.setAlignmentX(LEFT_ALIGNMENT);

        targetCombo = new JComboBox<>();
        styleCombo(targetCombo);
        for (User u : targetUsers) targetCombo.addItem("Kullanıcı " + u.userId);
        targetCombo.setPreferredSize(new Dimension(180, 36));

        xField1.setPreferredSize(new Dimension(60, 36));
        kField1.setPreferredSize(new Dimension(60, 36));
        attachTotalUpdater(xField1, kField1, totalLabel1);

        JButton btn = makeAccentButton("Önerileri Getir");
        btn.addActionListener(e -> runRecommend1());

        row.add(targetCombo);
        row.add(lbl("X  :", TEXT_SEC, 12)); row.add(xField1);
        row.add(lbl("K  :", TEXT_SEC, 12)); row.add(kField1);
        row.add(Box.createHorizontalStrut(6));
        row.add(btn);
        return row;
    }

    private void runRecommend1() {
        int idx = targetCombo.getSelectedIndex();
        if (idx < 0 || idx >= targetUsers.size()) return;
        User target = targetUsers.get(idx);
        int X = parseField(xField1), K = parseField(kField1);
        if (X <= 0 || K <= 0) { showErr("X ve K pozitif tam sayı olmalı!"); return; }
        showLoading(resultPanel1);
        new Thread(() -> {
            List<String> res = recommender.recommend(target, X, K);
            SwingUtilities.invokeLater(() -> showResults(resultPanel1, res, X, K));
        }).start();
    }

    // ════════════════════════════════════════════════════════════════════
    // EKRAN 2: Film Puanına Göre Öneri
    // ════════════════════════════════════════════════════════════════════
    private JPanel buildScreen2() {
        JPanel screen = new JPanel(new BorderLayout());
        screen.setBackground(BG_DEEP);
        screen.add(makeTopBar("Film Puanına Göre Öneri",
                "5 film seç + puan ver  →  Vektör  →  Benzer kullanıcılar"), BorderLayout.NORTH);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(BG_DEEP);
        inner.setBorder(new EmptyBorder(20, 24, 20, 24));

        xField2    = makeNumField("3");
        kField2    = makeNumField("5");
        totalLabel2 = makeTotalLabel();
        inner.add(makeMetricRow(xField2, kField2, totalLabel2, "Puana Göre"));
        inner.add(Box.createVerticalStrut(16));
        inner.add(buildMovieGrid());
        inner.add(Box.createVerticalStrut(12));
        inner.add(makeControlRow2());
        inner.add(Box.createVerticalStrut(20));

        resultPanel2 = new JPanel();
        resultPanel2.setLayout(new BoxLayout(resultPanel2, BoxLayout.Y_AXIS));
        resultPanel2.setBackground(BG_DEEP);
        resultPanel2.add(placeholder("Film seçip puan girdikten sonra 'Önerileri Getir' butonuna basın."));

        inner.add(makeScroll(resultPanel2));
        screen.add(inner, BorderLayout.CENTER);
        return screen;
    }

    private JPanel buildMovieGrid() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JLabel hdr = lbl("Film Secimi ve Puanlama  (5 farkli film secin, 1-5 puan verin)", TEXT_PRI, 13);
        hdr.setFont(new Font("SansSerif", Font.BOLD, 13));
        hdr.setAlignmentX(LEFT_ALIGNMENT);
        card.add(hdr);
        card.add(Box.createVerticalStrut(10));

        // Sutun basliklari
        JPanel colHdr = new JPanel(new BorderLayout(8, 0));
        colHdr.setBackground(BG_CARD);
        colHdr.setAlignmentX(LEFT_ALIGNMENT);
        colHdr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        JPanel colLeft = new JPanel(new BorderLayout(8, 0));
        colLeft.setBackground(BG_CARD);
        JLabel numHdr = lbl("#", TEXT_MUTED, 11);
        numHdr.setPreferredSize(new Dimension(20, 16));
        colLeft.add(numHdr, BorderLayout.WEST);
        colLeft.add(lbl("Film Adi", TEXT_MUTED, 11), BorderLayout.CENTER);
        colHdr.add(colLeft, BorderLayout.CENTER);
        JLabel ratingHdr = lbl("Puan", TEXT_MUTED, 11);
        ratingHdr.setPreferredSize(new Dimension(70, 16));
        colHdr.add(ratingHdr, BorderLayout.EAST);
        card.add(colHdr);
        card.add(Box.createVerticalStrut(6));

        // Film listesini String dizisi olarak hazirla
        String[] items = new String[randomMovies.size()];
        for (int j = 0; j < randomMovies.size(); j++) {
            Movie m = randomMovies.get(j);
            items[j] = m.movieId + " | " + m.title;
        }

        for (int i = 0; i < 5; i++) {
            // Her combo icin bagimsiz DefaultComboBoxModel
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(items);
            movieCombos[i] = new JComboBox<>(model);
            movieCombos[i].setSelectedIndex(i < items.length ? i : 0);
            styleCombo(movieCombos[i]); // model set edildikten SONRA

            ratingFields[i] = makeNumField("3");
            ratingFields[i].setPreferredSize(new Dimension(60, 34));
            ratingFields[i].setMaximumSize(new Dimension(60, 34));

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBackground(BG_CARD);
            row.setAlignmentX(LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            row.setBorder(new EmptyBorder(2, 0, 2, 0));

            JLabel numLbl = lbl(String.valueOf(i + 1), TEXT_MUTED, 12);
            numLbl.setPreferredSize(new Dimension(20, 34));
            numLbl.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel leftPart = new JPanel(new BorderLayout(6, 0));
            leftPart.setBackground(BG_CARD);
            leftPart.add(numLbl, BorderLayout.WEST);
            leftPart.add(movieCombos[i], BorderLayout.CENTER);

            row.add(leftPart, BorderLayout.CENTER);
            row.add(ratingFields[i], BorderLayout.EAST);

            card.add(row);
            if (i < 4) card.add(Box.createVerticalStrut(4));
        }
        return card;
    }

    private JPanel makeControlRow2() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setBackground(BG_DEEP);
        row.setAlignmentX(LEFT_ALIGNMENT);

        xField2.setPreferredSize(new Dimension(60, 36));
        kField2.setPreferredSize(new Dimension(60, 36));
        attachTotalUpdater(xField2, kField2, totalLabel2);

        JButton btn = makeAccentButton("Önerileri Getir");
        btn.addActionListener(e -> runRecommend2());

        row.add(lbl("X  :", TEXT_SEC, 12)); row.add(xField2);
        row.add(lbl("K  :", TEXT_SEC, 12)); row.add(kField2);
        row.add(Box.createHorizontalStrut(6));
        row.add(btn);
        return row;
    }

    private void runRecommend2() {
        // 5 combo'dan secilen film-puan ciflerini topla
        // Ayni film birden fazla secilmisse UYAR (hoca 5 farkli film istiyor)
        Map<Integer, Integer> ratings = new LinkedHashMap<>();
        Set<Integer> seen = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            String sel = (String) movieCombos[i].getSelectedItem();
            if (sel == null || !sel.contains("|")) {
                showErr((i + 1) + ". satirda film secili degil!"); return;
            }
            int movieId;
            try { movieId = Integer.parseInt(sel.split("\\|")[0].trim()); }
            catch (NumberFormatException ex) { showErr((i+1) + ". satirda film ID hatasi!"); return; }

            int r = parseField(ratingFields[i]);
            if (r < 1 || r > 5) {
                showErr((i + 1) + ". satir icin 1-5 arasi tam sayi girin!"); return;
            }
            if (seen.contains(movieId)) {
                showErr("Ayni filmi birden fazla secmeyin!\n" + (i+1) + ". satirda tekrar var."); return;
            }
            seen.add(movieId);
            ratings.put(movieId, r);
        }
        int X = parseField(xField2), K = parseField(kField2);
        if (X <= 0 || K <= 0) { showErr("X ve K pozitif tam sayi olmali!"); return; }
        showLoading(resultPanel2);
        final int fX = X, fK = K;
        final Map<Integer, Integer> finalRatings = new LinkedHashMap<>(ratings);
        new Thread(() -> {
            List<String> res = recommender.recommendFromRatings(finalRatings, fX, fK);
            SwingUtilities.invokeLater(() -> showResults(resultPanel2, res, fX, fK));
        }).start();
    }

    // ════════════════════════════════════════════════════════════════════
    // SONUÇ GÖSTERİMİ
    // ════════════════════════════════════════════════════════════════════
    private void showResults(JPanel panel, List<String> results, int X, int K) {
        panel.removeAll();
        if (results.isEmpty()) {
            panel.add(placeholder("Öneri bulunamadı. X ve K değerlerini küçültün."));
            panel.revalidate(); panel.repaint(); return;
        }
        JLabel hdr = lbl("  " + results.size() + " öneri  ·  X=" + X + "  K=" + K, TEXT_SEC, 12);
        hdr.setBorder(new EmptyBorder(10, 0, 8, 0));
        hdr.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(hdr);

        for (int i = 0; i < results.size(); i++) {
            if (i % K == 0) {
                JLabel grp = lbl("   Benzer Kullanıcı " + (i / K + 1), TEXT_MUTED, 11);
                grp.setFont(new Font("SansSerif", Font.BOLD, 11));
                grp.setBorder(new EmptyBorder(i == 0 ? 0 : 10, 0, 4, 0));
                grp.setAlignmentX(LEFT_ALIGNMENT);
                panel.add(grp);
            }
            panel.add(buildMovieRow(i + 1, results.get(i), i / K + 1));
            panel.add(Box.createVerticalStrut(4));
        }
        panel.add(Box.createVerticalStrut(20));
        panel.revalidate(); panel.repaint();
    }

    private JPanel buildMovieRow(int rank, String title, int userNum) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(BG_CARD);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        row.setAlignmentX(LEFT_ALIGNMENT);

        // Sol: rank + film adı
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setBackground(BG_CARD);

        JLabel badge = new JLabel(String.valueOf(rank), SwingConstants.CENTER);
        badge.setFont(new Font("SansSerif", Font.BOLD, 11));
        badge.setForeground(ACCENT);
        badge.setOpaque(true);
        badge.setBackground(ACCENT_DIM);
        badge.setPreferredSize(new Dimension(26, 26));
        badge.setBorder(BorderFactory.createLineBorder(new Color(60, 58, 110), 1, true));

        JLabel titleLbl = lbl(title, TEXT_PRI, 13);
        left.add(badge);
        left.add(titleLbl);

        // Sağ: kullanıcı rozeti
        JLabel userBadge = new JLabel("User " + userNum);
        userBadge.setFont(new Font("SansSerif", Font.PLAIN, 11));
        userBadge.setForeground(BLUE_TEXT);
        userBadge.setOpaque(true);
        userBadge.setBackground(BLUE_DIM);
        userBadge.setBorder(new EmptyBorder(3, 8, 3, 8));

        // Hover
        MouseAdapter hover = new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { row.setBackground(BG_HOVER); left.setBackground(BG_HOVER); }
            public void mouseExited(MouseEvent e)  { row.setBackground(BG_CARD);  left.setBackground(BG_CARD);  }
        };
        row.addMouseListener(hover);
        left.addMouseListener(hover);

        row.add(left, BorderLayout.CENTER);
        row.add(userBadge, BorderLayout.EAST);
        return row;
    }

    // ════════════════════════════════════════════════════════════════════
    // YARDIMCI BİLEŞENLER
    // ════════════════════════════════════════════════════════════════════
    private JPanel makeTopBar(String title, String sub) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(14, 24, 14, 24)
        ));
        JLabel t = lbl(title, TEXT_PRI, 16);
        t.setFont(new Font("SansSerif", Font.BOLD, 16));
        JLabel s = lbl(sub, TEXT_SEC, 12);
        JPanel texts = new JPanel();
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.setBackground(BG_PANEL);
        texts.add(t); texts.add(Box.createVerticalStrut(3)); texts.add(s);
        bar.add(texts, BorderLayout.WEST);
        return bar;
    }

    private JPanel makeMetricRow(JTextField xF, JTextField kF, JLabel totLbl, String tag) {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setBackground(BG_DEEP);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        row.add(makeMetricCard("Benzer Kullanıcı (X)", xF, "Heap'ten çekilecek"));
        row.add(makeMetricCard("Film / Kullanıcı (K)", kF, "En yüksek puanlı"));
        row.add(makeStaticCard("Toplam Öneri", totLbl, "X × K"));
        updateTotal(xF, kF, totLbl);
        return row;
    }

    private JPanel makeMetricCard(String label, JTextField field, String hint) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true), new EmptyBorder(12, 14, 12, 14)));
        JLabel l = lbl(label, TEXT_SEC, 11); l.setAlignmentX(LEFT_ALIGNMENT);
        field.setFont(new Font("SansSerif", Font.BOLD, 22));
        field.setForeground(TEXT_PRI); field.setBackground(BG_CARD);
        field.setBorder(null); field.setCaretColor(ACCENT); field.setAlignmentX(LEFT_ALIGNMENT);
        JLabel h = lbl(hint, new Color(127, 119, 221, 150), 11); h.setAlignmentX(LEFT_ALIGNMENT);
        card.add(l); card.add(Box.createVerticalStrut(4)); card.add(field);
        card.add(Box.createVerticalStrut(2)); card.add(h);
        return card;
    }

    private JPanel makeStaticCard(String label, JLabel val, String hint) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true), new EmptyBorder(12, 14, 12, 14)));
        JLabel l = lbl(label, TEXT_SEC, 11); l.setAlignmentX(LEFT_ALIGNMENT);
        val.setFont(new Font("SansSerif", Font.BOLD, 22));
        val.setForeground(TEXT_PRI); val.setAlignmentX(LEFT_ALIGNMENT);
        JLabel h = lbl(hint, new Color(127, 119, 221, 150), 11); h.setAlignmentX(LEFT_ALIGNMENT);
        card.add(l); card.add(Box.createVerticalStrut(4)); card.add(val);
        card.add(Box.createVerticalStrut(2)); card.add(h);
        return card;
    }

    private JScrollPane makeScroll(JPanel p) {
        JScrollPane s = new JScrollPane(p);
        s.setBackground(BG_DEEP);
        s.getViewport().setBackground(BG_DEEP);
        s.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        s.setAlignmentX(LEFT_ALIGNMENT);
        return s;
    }

    private JLabel placeholder(String text) {
        JLabel l = lbl(text, TEXT_MUTED, 13);
        l.setBorder(new EmptyBorder(20, 0, 0, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private void showLoading(JPanel panel) {
        panel.removeAll();
        panel.add(placeholder("Hesaplanıyor..."));
        panel.revalidate(); panel.repaint();
    }

    private JTextField makeNumField(String val) {
        JTextField f = new JTextField(val, 4);
        f.setBackground(BG_CARD); f.setForeground(TEXT_PRI);
        f.setCaretColor(ACCENT);
        f.setFont(new Font("SansSerif", Font.BOLD, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true), new EmptyBorder(6, 10, 6, 10)));
        return f;
    }

    private JLabel makeTotalLabel() {
        JLabel l = new JLabel("0");
        l.setFont(new Font("SansSerif", Font.BOLD, 22));
        l.setForeground(TEXT_PRI);
        return l;
    }

    private void attachTotalUpdater(JTextField xF, JTextField kF, JLabel tot) {
        ActionListener al = e -> updateTotal(xF, kF, tot);
        FocusAdapter fa   = new FocusAdapter() { public void focusLost(FocusEvent e) { updateTotal(xF, kF, tot); } };
        xF.addActionListener(al); kF.addActionListener(al);
        xF.addFocusListener(fa);  kF.addFocusListener(fa);
    }

    private void updateTotal(JTextField xF, JTextField kF, JLabel tot) {
        try { tot.setText(String.valueOf(Integer.parseInt(xF.getText().trim()) * Integer.parseInt(kF.getText().trim()))); }
        catch (NumberFormatException ex) { tot.setText("?"); }
    }

    private int parseField(JTextField f) {
        try { return Integer.parseInt(f.getText().trim()); }
        catch (NumberFormatException ex) { return -1; }
    }

    private JButton makeAccentButton(String text) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? ACCENT.darker() :
                        getModel().isRollover() ? ACCENT.brighter() : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(160, 36));
        btn.setBorderPainted(false); btn.setContentAreaFilled(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setBackground(BG_CARD);
        combo.setForeground(TEXT_PRI);
        combo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        combo.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        combo.setOpaque(true);

        // Renderer: hem acilan liste hem de kapali haldeki secili item icin
        combo.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean hasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
                // index == -1: kapali haldeki secili item gosterimi
                if (index == -1) {
                    l.setBackground(BG_CARD);
                    l.setForeground(TEXT_PRI);  // ACIK renk — koyu zemin uzerinde okunur
                } else {
                    l.setBackground(isSelected ? ACCENT_DIM : BG_CARD);
                    l.setForeground(isSelected ? TEXT_PRI : TEXT_PRI);
                }
                l.setOpaque(true);
                l.setBorder(new EmptyBorder(5, 10, 5, 10));
                return l;
            }
        });

        combo.setUI(new BasicComboBoxUI() {
            // Ok dugmesi
            protected JButton createArrowButton() {
                JButton b = new JButton("▾");
                b.setBackground(BG_CARD);
                b.setForeground(TEXT_SEC);
                b.setBorder(null);
                b.setOpaque(true);
                b.setFont(new Font("SansSerif", Font.PLAIN, 11));
                return b;
            }

            // Kapali haldeki alan (selected item gosterimi) — arka plan BG_CARD
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(BG_CARD);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });
    }

    private JLabel lbl(String text, Color color, int size) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, size));
        l.setForeground(color);
        return l;
    }

    private void showErr(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Hata", JOptionPane.ERROR_MESSAGE);
    }

    // ════════════════════════════════════════════════════════════════════
    // MAIN
    // ════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(MainFrame::new);
    }
}