public class EduLearnerMindmap {
    public static void main(String[] args) throws Exception {
        javax.swing.SwingUtilities.invokeLater(() -> new AppFrame().setVisible(true));
    }
}

class AppFrame extends javax.swing.JFrame {
    private final javax.swing.JTextField courseField = new javax.swing.JTextField();
    private final javax.swing.JButton generateBtn = new javax.swing.JButton("Generate");
    private final javax.swing.JButton exportBtn = new javax.swing.JButton("Export PNG");
    private final javax.swing.JPanel rightPanel = new javax.swing.JPanel();
    private final MindmapPanel mindmapPanel = new MindmapPanel();
    private final javax.swing.JTextArea infoArea = new javax.swing.JTextArea();

    public AppFrame() {
        setTitle("EduLearner — Mindmap & YouTube References");
        setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        initLayout();
        attachActions();
    }

    private void initLayout() {
        setLayout(new java.awt.BorderLayout());
        javax.swing.JPanel top = new javax.swing.JPanel(new java.awt.BorderLayout(8, 8));
        top.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        top.add(new javax.swing.JLabel("Course name:"), java.awt.BorderLayout.WEST);
        top.add(courseField, java.awt.BorderLayout.CENTER);
        javax.swing.JPanel topRight = new javax.swing.JPanel();
        topRight.add(generateBtn);
        topRight.add(exportBtn);
        top.add(topRight, java.awt.BorderLayout.EAST);
        add(top, java.awt.BorderLayout.NORTH);

        add(mindmapPanel, java.awt.BorderLayout.CENTER);

        rightPanel.setLayout(new java.awt.BorderLayout());
        rightPanel.setPreferredSize(new java.awt.Dimension(360, 10));
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        javax.swing.JScrollPane sp = new javax.swing.JScrollPane(infoArea);
        rightPanel.add(sp, java.awt.BorderLayout.CENTER);
        add(rightPanel, java.awt.BorderLayout.EAST);
    }

    private void attachActions() {
        generateBtn.addActionListener(e -> onGenerate());
        exportBtn.addActionListener(e -> onExport());
        mindmapPanel.setNodeClickListener(node -> showNodeInfo(node));
    }

    private void onGenerate() {
        String course = courseField.getText().trim();
        if (course.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Please enter a course name.");
            return;
        }
        java.util.List<MindNode> nodes = TopicGenerator.generateFor(course);
        mindmapPanel.setNodes(nodes);
        String ytQuery = buildYouTubeSearchLink(course);
        StringBuilder sb = new StringBuilder();
        sb.append("Course: ").append(course).append('\n');
        sb.append("YouTube search link (clickable):\n");
        sb.append(ytQuery).append('\n').append('\n');
        sb.append("Suggested resources:\n");
        for (String s : TopicGenerator.suggestResources(course)) {
            sb.append("- ").append(s).append('\n');
        }
        sb.append('\n');
        sb.append("Tip: Click a node on the mindmap to get focused YouTube search link for that topic.");
        infoArea.setText(sb.toString());
    }

    private void showNodeInfo(MindNode node) {
        String base = buildYouTubeSearchLink(node.label);
        StringBuilder sb = new StringBuilder();
        sb.append("Topic: ").append(node.label).append('\n');
        sb.append("YouTube search link:\n");
        sb.append(base).append('\n').append('\n');
        sb.append("Related keywords:\n");
        for (String k : node.related) sb.append("- ").append(k).append('\n');
        sb.append('\n');
        sb.append("Actions:\n");
        sb.append("- Double-click a node to open the YouTube search in your browser.\n");
        sb.append("- Use Export PNG to save the mindmap image.\n");
        infoArea.setText(sb.toString());
    }

    private String buildYouTubeSearchLink(String query) {
        try {
            return "https://www.youtube.com/results?search_query=" + java.net.URLEncoder.encode(query + " tutorial", "UTF-8");
        } catch (java.io.UnsupportedEncodingException ex) {
            return "https://www.youtube.com/results?search_query=" + query.replace(" ", "+");
        }
    }

    private void onExport() {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(mindmapPanel.getWidth(), mindmapPanel.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2 = img.createGraphics();
        mindmapPanel.paint(g2);
        g2.dispose();
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setDialogTitle("Save mindmap as PNG");
        if (chooser.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File f = chooser.getSelectedFile();
            if (!f.getName().toLowerCase().endsWith(".png")) f = new java.io.File(f.getParentFile(), f.getName() + ".png");
            try {
                javax.imageio.ImageIO.write(img, "png", f);
                javax.swing.JOptionPane.showMessageDialog(this, "Saved to: " + f.getAbsolutePath());
            } catch (java.io.IOException ex) {
                javax.swing.JOptionPane.showMessageDialog(this, "Failed to save: " + ex.getMessage());
            }
        }
    }
}

class MindmapPanel extends javax.swing.JPanel {
    private java.util.List<MindNode> nodes = new java.util.ArrayList<>();
    private java.util.Random rnd = new java.util.Random();
    private java.util.function.Consumer<MindNode> nodeClickListener = n -> {};

    public MindmapPanel() {
        setBackground(java.awt.Color.WHITE);
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                MindNode hit = findNodeAt(e.getX(), e.getY());
                if (hit != null) {
                    if (e.getClickCount() == 2) {
                        try { java.awt.Desktop.getDesktop().browse(new java.net.URI(buildYTSearch(hit.label))); } catch (Exception ex) { }
                    }
                    nodeClickListener.accept(hit);
                }
            }
        });
    }

    public void setNodeClickListener(java.util.function.Consumer<MindNode> c) { this.nodeClickListener = c; }

    public void setNodes(java.util.List<MindNode> newNodes) {
        this.nodes = new java.util.ArrayList<>(newNodes);
        layoutNodes();
        repaint();
    }

    private String buildYTSearch(String q) {
        try { return "https://www.youtube.com/results?search_query=" + java.net.URLEncoder.encode(q + " tutorial", "UTF-8"); } catch (Exception ex) { return "https://www.youtube.com/results?search_query=" + q.replace(" ", "+"); }
    }

    private void layoutNodes() {
        int w = getWidth() <= 0 ? 700 : getWidth();
        int h = getHeight() <= 0 ? 500 : getHeight();
        int cx = w/2, cy = h/2;
        int r = Math.min(w, h)/4;
        if (nodes.isEmpty()) return;
        nodes.get(0).x = cx; nodes.get(0).y = cy; // center as root
        int n = nodes.size()-1;
        for (int i=1;i<nodes.size();i++) {
            double ang = 2*Math.PI*(i-1)/Math.max(1, n);
            int dist = r + rnd.nextInt(r/3);
            nodes.get(i).x = (int)(cx + Math.cos(ang)*dist);
            nodes.get(i).y = (int)(cy + Math.sin(ang)*dist);
        }
    }

    private MindNode findNodeAt(int mx, int my) {
        for (MindNode node : nodes) {
            int dx = mx - node.x, dy = my - node.y;
            if (dx*dx + dy*dy <= node.radius*node.radius) return node;
        }
        return null;
    }

    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        if (nodes.isEmpty()) return;
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        for (int i=1;i<nodes.size();i++) {
            drawEdge(g2, nodes.get(0), nodes.get(i));
        }
        for (MindNode node : nodes) drawNode(g2, node);
        g2.dispose();
    }

    private void drawEdge(java.awt.Graphics2D g2, MindNode a, MindNode b) {
        java.awt.Stroke prev = g2.getStroke();
        g2.setStroke(new java.awt.BasicStroke(2));
        g2.setColor(java.awt.Color.LIGHT_GRAY);
        g2.drawLine(a.x, a.y, b.x, b.y);
        g2.setStroke(prev);
    }

    private void drawNode(java.awt.Graphics2D g2, MindNode n) {
        java.awt.Color fill = new java.awt.Color(160, 200, 255);
        g2.setColor(fill);
        g2.fillOval(n.x - n.radius, n.y - n.radius, n.radius*2, n.radius*2);
        g2.setColor(java.awt.Color.DARK_GRAY);
        g2.setStroke(new java.awt.BasicStroke(2));
        g2.drawOval(n.x - n.radius, n.y - n.radius, n.radius*2, n.radius*2);
        java.awt.FontMetrics fm = g2.getFontMetrics();
        String text = n.label;
        int tw = fm.stringWidth(text);
        int th = fm.getHeight();
        g2.setColor(java.awt.Color.BLACK);
        g2.drawString(text, n.x - tw/2, n.y + th/4);
    }

    public java.awt.Dimension getPreferredSize() { return new java.awt.Dimension(740, 560); }

    public void doLayout() { super.doLayout(); layoutNodes(); }
}

class MindNode {
    String label;
    java.util.List<String> related = new java.util.ArrayList<>();
    int x, y; int radius = 60;
    MindNode(String l) { this.label = l; }
}

class TopicGenerator {
    public static java.util.List<MindNode> generateFor(String course) {
        String key = course.trim().toLowerCase();
        java.util.List<MindNode> out = new java.util.ArrayList<>();
        MindNode root = new MindNode(capitalize(course));
        out.add(root);
        java.util.List<String> topics = lookupTopics(key);
        for (String t : topics) {
            MindNode n = new MindNode(t);
            n.related.addAll(suggestSubtopics(t));
            out.add(n);
        }
        return out;
    }

    public static java.util.List<String> suggestResources(String course) {
        java.util.List<String> res = new java.util.ArrayList<>();
        res.add("Official docs and tutorials for " + course);
        res.add("YouTube search: https://www.youtube.com/results?search_query=" + encode(course + " tutorial"));
        res.add("FreeCodeCamp, Coursera, edX, Khan Academy depending on topic");
        return res;
    }

    private static java.util.List<String> lookupTopics(String key) {
        java.util.List<String> t = new java.util.ArrayList<>();
        if (key.contains("java")) {
            t.add("Java Basics"); t.add("OOP Concepts"); t.add("Collections"); t.add("Streams & Lambdas"); t.add("Concurrency");
        } else if (key.contains("data") && key.contains("structure")) {
            t.add("Arrays & Lists"); t.add("Stacks & Queues"); t.add("Trees"); t.add("Graphs"); t.add("Hashing");
        } else if (key.contains("machine") || key.contains("learning") || key.contains("ml")) {
            t.add("Linear Algebra Basics"); t.add("Statistics & Probability"); t.add("Supervised Learning"); t.add("Neural Networks"); t.add("Model Evaluation");
        } else if (key.contains("web") || key.contains("frontend") || key.contains("web development")) {
            t.add("HTML & CSS"); t.add("JavaScript Basics"); t.add("Frontend Frameworks"); t.add("APIs & AJAX"); t.add("Build Tools");
        } else if (key.contains("algorith")) {
            t.add("Searching & Sorting"); t.add("Dynamic Programming"); t.add("Greedy Algorithms"); t.add("Graph Algorithms"); t.add("Complexity Analysis");
        } else {
            t.add("Overview"); t.add("Fundamentals"); t.add("Tools & Setup"); t.add("Core Topics"); t.add("Projects & Practice");
        }
        return t;
    }

    private static java.util.List<String> suggestSubtopics(String topic) {
        java.util.List<String> s = new java.util.ArrayList<>();
        if (topic.toLowerCase().contains("java basics")) { s.add("Syntax"); s.add("Variables"); s.add("Control Flow"); }
        else if (topic.toLowerCase().contains("oop")) { s.add("Classes"); s.add("Objects"); s.add("Inheritance"); s.add("Polymorphism"); }
        else if (topic.toLowerCase().contains("neural")) { s.add("Perceptron"); s.add("Backpropagation"); s.add("Activation Functions"); }
        else { s.add("Key concept 1"); s.add("Key concept 2"); s.add("Practice exercises"); }
        return s;
    }

    private static String encode(String q) {
        try { return java.net.URLEncoder.encode(q, "UTF-8"); } catch (Exception e) { return q.replace(" ", "+"); }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}