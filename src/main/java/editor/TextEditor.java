package editor;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class TextEditor extends JFrame {

    private final JTextArea textArea;
    private final JTextField searchField;
    private final JFileChooser fileChooser;
    private final JCheckBox regexCheckBox;
    private List<MatchResult> searchResults;
    private int index;

    public TextEditor() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setTitle("Text Editor");
        setLayout(new GridBagLayout());

        textArea = new JTextArea();
        searchField = new JTextField();
        fileChooser = new JFileChooser();
        regexCheckBox = new JCheckBox();

        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        GridBagConstraints c = new GridBagConstraints();
        Dimension buttonSize = new Dimension(32, 32);

        // File Chooser
        fileChooser.setName("FileChooser");

        // Open Button
        JButton loadButton = new JButton();
        loadButton.setIcon(loadIcon("load.png"));
        loadButton.setName("OpenButton");
        loadButton.setPreferredSize(buttonSize);
        loadButton.addActionListener(e -> loadFile());
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(10,10,10,10);
        add(loadButton, c);

        // Save Button
        JButton saveButton = new JButton();
        saveButton.setIcon(loadIcon("save.png"));
        saveButton.setName("SaveButton");
        saveButton.setPreferredSize(buttonSize);
        saveButton.addActionListener(e -> saveFile());
        c.gridx = 1;
        c.gridy = 0;
        add(saveButton, c);

        // Search Field
        searchField.setName("SearchField");
        searchField.setPreferredSize(buttonSize);
        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(searchField, c);

        // Search Button
        JButton searchButton = new JButton();
        searchButton.setName("StartSearchButton");
        searchButton.setIcon(loadIcon("search.png"));
        searchButton.setPreferredSize(buttonSize);
        searchButton.addActionListener(e -> search());
        c.gridx = 3;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        add(searchButton, c);

        // Previous Match Button
        JButton previousButton = new JButton();
        previousButton.setName("PreviousMatchButton");
        previousButton.setIcon(loadIcon("previous.png"));
        previousButton.setPreferredSize(buttonSize);
        previousButton.addActionListener(e -> previousSearch());
        c.gridx = 4;
        add(previousButton, c);

        // Next Match Button
        JButton nextButton = new JButton();
        nextButton.setName("NextMatchButton");
        nextButton.setIcon(loadIcon("next.png"));
        nextButton.setPreferredSize(buttonSize);
        nextButton.addActionListener(e -> nextSearch());
        c.gridx = 5;
        add(nextButton, c);

        // Regex Check Box
        regexCheckBox.setText("Use Regex");
        regexCheckBox.setName("UseRegExCheckbox");
        c.gridx = 6;
        add(regexCheckBox, c);

        // Text Area
        textArea.setName("TextArea");
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setName("ScrollPane");
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 7;
        c.fill = GridBagConstraints.BOTH;
        add(scrollPane, c);

        // JMenu
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setName("MenuFile");

        JMenuItem saveMenu = new JMenuItem("Save");
        saveMenu.setName("MenuSave");
        saveMenu.addActionListener(e -> saveFile());
        fileMenu.add(saveMenu);

        JMenuItem loadMenu = new JMenuItem("Load");
        loadMenu.setName("MenuOpen");
        loadMenu.addActionListener(e -> loadFile());
        fileMenu.add(loadMenu);
        fileMenu.addSeparator();

        JMenuItem exitMenu = new JMenuItem("Exit");
        exitMenu.setName("MenuExit");
        exitMenu.addActionListener(e -> this.dispose());
        fileMenu.add(exitMenu);

        // Search Menu
        JMenu searchMenu = new JMenu("Search");
        searchMenu.setName("MenuSearch");

        JMenuItem startSearchMenu = new JMenuItem("Start Search");
        startSearchMenu.setName("MenuStartSearch");
        startSearchMenu.addActionListener(e -> search());
        searchMenu.add(startSearchMenu);

        JMenuItem previousMatchMenu = new JMenuItem("Previous Search");
        previousMatchMenu.setName("MenuPreviousMatch");
        previousMatchMenu.addActionListener(e -> previousSearch());
        searchMenu.add(previousMatchMenu);

        JMenuItem nextMatchMenu = new JMenuItem("Next Match");
        nextMatchMenu.setName("MenuNextMatch");
        nextMatchMenu.addActionListener(e -> nextSearch());
        searchMenu.add(nextMatchMenu);

        JMenuItem regexMenu = new JMenuItem("Use regular Expressions");
        regexMenu.setName("MenuUseRegExp");
        regexMenu.addActionListener(e -> regexCheckBox.setSelected(true));
        searchMenu.add(regexMenu);

        menuBar.add(fileMenu);
        menuBar.add(searchMenu);
        setJMenuBar(menuBar);

    }

    private void search() {
        Thread searchThread = new Thread(() -> {
            searchResults = null;
            index = 0;
            String text = textArea.getText();
            String search = searchField.getText();
            int flags = regexCheckBox.isSelected() ? Pattern.CASE_INSENSITIVE : Pattern.LITERAL | Pattern.CASE_INSENSITIVE;
            try {
                Pattern pattern = Pattern.compile(search, flags);
                Matcher matcher = pattern.matcher(text);
                searchResults = matcher.results().collect(Collectors.toList());
                if (!searchResults.isEmpty()) {
                    highlightSearch(searchResults.get(0));
                }
            } catch (PatternSyntaxException exception) {
                System.out.println(exception.getMessage());
            }
        });
        searchThread.start();
    }

    private void saveFile() {
        int r = fileChooser.showSaveDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            try (FileOutputStream fos = new FileOutputStream(fileChooser.getSelectedFile())) {
                byte[] data = textArea.getText().getBytes(StandardCharsets.UTF_8);
                fos.write(data);
                fos.flush();
            } catch (IOException exception) {
                System.out.println(exception.getMessage());
            }
        }
    }

    private void loadFile() {
        int r = fileChooser.showOpenDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            try (FileInputStream fis = new FileInputStream(fileChooser.getSelectedFile())) {
                byte[] data = fis.readAllBytes();
                textArea.setText(new String(data, StandardCharsets.UTF_8));
            } catch (IOException exception) {
                textArea.setText("");
                System.out.println(exception.getMessage());
            }
        }
    }

    private Icon loadIcon(String fileName){
        int size = 24;
        try {
            return new ImageIcon(ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(fileName))).getScaledInstance(size, size, BufferedImage.SCALE_SMOOTH));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return new ImageIcon();
        }
    }

    private void nextSearch() {
        if (searchResults != null && !searchResults.isEmpty()) {
            index++;
            if (index == searchResults.size()) {
                index = 0;
            }
            highlightSearch(searchResults.get(index));
        }
    }

    private void previousSearch() {
        if (searchResults != null && !searchResults.isEmpty()) {
            index--;
            if (index < 0) {
                index = searchResults.size() - 1;
            }
            highlightSearch(searchResults.get(index));
        }
    }

    private void highlightSearch(MatchResult r) {
        textArea.setCaretPosition(r.start() + r.group().length());
        textArea.select(r.start(), r.start() + r.group().length());
        textArea.grabFocus();
    }
}
