package yale;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.SwingWorker;


/**
 * GUI for file search and transfer
 *
 * @author Osman Din
 */
public class FileCopy extends JFrame implements ActionListener, PropertyChangeListener {

    private static Logger logger = Logger.getLogger("yale.FileCopy");

    private static final long serialVersionUID = 1L;

    private JTextField txtSource;

    private static Handler logFileHandler;

    private JTextField txtTarget;

    private JTextArea detailsBox;

    private JTextArea txtIdentifiers;

    private JButton btnCopy;

    private CopyTask task;

    private String source = "";

    private String target = "";

    private static String LINE_SEPARTOR = System.getProperty("line.separator");

    private boolean stop = false;

    private JCheckBox checkBox;

    public FileCopy() {
        buildGUI();
    }

    private void buildGUI() {

        // Build log file:

        final Date date = Calendar.getInstance().getTime();
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.ss.SSSZ");
        final String logfile = formatter.format(date);

        try {
            logFileHandler = new FileHandler("ft-" + logfile + ".log");
            logFileHandler.setFormatter(new SimpleFormatter());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Internal error:", e);
        }

        logger.addHandler(logFileHandler);
        logger.setLevel(Level.ALL);
        logger.info("Initiated GUI");

        // Populate the GUI:

        setTitle("File Transfer Utility");

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (task != null) task.cancel(true);
                dispose();
                System.exit(0);
            }
        });

        //Create the menu bar:
        final JMenuBar menuBar = new JMenuBar();

        //Build the first menu:
        final JMenu menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(menu);

        // Create first menu item:
        final JMenuItem menuItem = new JMenuItem("About", KeyEvent.VK_A);
        menuItem.addActionListener(new AboutDialogAction());
        menu.add(menuItem);


        // Create second menu item:
        final JMenuItem menuItem2 = new JMenuItem("Instructions", KeyEvent.VK_I);
        menuItem2.addActionListener(new HelpAction());
        menu.add(menuItem2);

        menuBar.add(menu);
        setJMenuBar(menuBar);

        // Add source and target labels
        final JLabel lblSource = new JLabel("Source: ");
        final JLabel lblTarget = new JLabel("Target: ");
        txtSource = new JTextField(50);
        txtSource.setText("");
        txtTarget = new JTextField(50);
        txtTarget.setText("");

        final JPanel buttonsPanel = new JPanel();

        // Select source button
        final JButton selectSourceButton = new JButton("Browse Source Folder");
        selectSourceButton.addActionListener(getActListener1());

        // Select target button:
        final JButton selectTargetButton = new JButton("Browse Target Folder");
        selectTargetButton.addActionListener(getActionListener2());

        buttonsPanel.add(selectSourceButton, BorderLayout.WEST);
        buttonsPanel.add(selectTargetButton, BorderLayout.EAST);

        detailsBox = new JTextArea(5, 50);

        final Color c = Color.WHITE;
        detailsBox.setBackground(c);

        detailsBox.setEditable(false);
        final DefaultCaret caret = (DefaultCaret) detailsBox.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane scrollPane = new JScrollPane(detailsBox, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        txtIdentifiers = new JTextArea(5, 50);
        txtIdentifiers.setEditable(true);
        final DefaultCaret caret2 = (DefaultCaret) txtIdentifiers.getCaret();
        caret2.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        final JScrollPane scrollPane2 = new JScrollPane(txtIdentifiers);

        btnCopy = new JButton("Copy");
        btnCopy.setFocusPainted(false);
        btnCopy.setEnabled(false);
        btnCopy.addActionListener(this);

        final DocumentListener listener = new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                boolean bEnabled = txtSource.getText().length() > 0 && txtTarget.getText().length() > 0;
                btnCopy.setEnabled(bEnabled);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                boolean bEnabled = txtSource.getText().length() > 0 && txtTarget.getText().length() > 0;
                btnCopy.setEnabled(bEnabled);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        };

        txtSource.getDocument().addDocumentListener(listener);
        txtTarget.getDocument().addDocumentListener(listener);

        final JPanel contentPane = (JPanel) getContentPane();
        contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        final JPanel panInputLabels = new JPanel(new BorderLayout(0, 5));
        final JPanel panInputFields = new JPanel(new BorderLayout(0, 5));

        panInputLabels.add(lblSource, BorderLayout.NORTH);
        panInputLabels.add(lblTarget, BorderLayout.CENTER);
        panInputFields.add(txtSource, BorderLayout.NORTH);
        panInputFields.add(txtTarget, BorderLayout.CENTER);

        final JPanel panInput = new JPanel(new BorderLayout(0, 5));
        panInput.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Path"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        final JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Progress"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        //infoPanel.setBackground(Color.gray);

        final JPanel panIds = new JPanel(new BorderLayout());
        panIds.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Identifiers"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        final JPanel panOptions = new JPanel(new BorderLayout());
        panOptions.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Options"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        final JPanel panControls = new JPanel(new BorderLayout());
        panControls.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        panInput.add(panInputLabels, BorderLayout.LINE_START);
        panInput.add(panInputFields, BorderLayout.CENTER);
        panIds.add(scrollPane2, BorderLayout.CENTER);
        infoPanel.add(scrollPane, BorderLayout.CENTER);
        panControls.add(btnCopy, BorderLayout.CENTER);

        // Add the checkbox:
        JPanel checkboxPanel = new JPanel(new BorderLayout(0, 5));
        checkBox = new JCheckBox("Overwrite");
        checkboxPanel.add(checkBox);
        panOptions.add(checkboxPanel, BorderLayout.LINE_START);

        // Add panels to upper pane:
        final JPanel panUpper = new JPanel(new BorderLayout());
        panUpper.add(buttonsPanel, BorderLayout.NORTH);
        panUpper.add(panInput, BorderLayout.CENTER);

        // Add to content pane:
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
        contentPane.add(panUpper, BorderLayout.NORTH);
        contentPane.add(panIds, BorderLayout.CENTER);
        contentPane.add(panOptions, BorderLayout.CENTER);
        contentPane.add(infoPanel, BorderLayout.CENTER);
        contentPane.add(panControls, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private ActionListener getActionListener2() {
        return new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int val = fileChooser.showOpenDialog(null);
                if (val == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    target = selectedFile.getAbsolutePath();
                    txtTarget.setText(target);
                }
            }
        };
    }

    private ActionListener getActListener1() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                final JFileChooser chooser = new JFileChooser();
                int x = 0;
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                final int returnValue = chooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    final File selectedFile = chooser.getSelectedFile();
                    source = selectedFile.getAbsolutePath();
                    txtSource.setText(source);

                }
            }
        };
    }

    /**
     * Cleans any state.
     */
    @Override
    public synchronized void addWindowListener(final WindowListener l) {
        super.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent windowEvent) {
                try {
                    if (logFileHandler != null) {
                        logFileHandler.close();
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Internal error:", e);
                }

                //TODO check any missing steps that java.awt does
                System.exit(0);
            }
        });
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if ("Copy".equals(btnCopy.getText())) {
            final File source = new File(txtSource.getText());
            final File target = new File(txtTarget.getText());

            if (!source.exists()) {
                JOptionPane.showMessageDialog(this, "The source file/directory does not exist!",
                        "ERROR", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!target.exists() && source.isDirectory()) {
                boolean op = target.mkdirs();
                if (!op) {
                    detailsBox.append("Error creating target directory. Check input and please try again.");
                    return;
                }
            }

            /*else { // customize as necessary
                final int option = JOptionPane.showConfirmDialog(this,
                 "The target file or directory already exists. Do you want to overwrite it?",
                 "Overwrite the target", JOptionPane.YES_NO_OPTION);
                if (option != JOptionPane.YES_OPTION) {
                    return;
                }
            } */

            task = this.new CopyTask(source, target);
            task.addPropertyChangeListener(this);
            task.execute();
            //btnCopy.setText("Cancel");
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FileCopy().setVisible(true);
            }
        });
    }

    /**
     * Actual copying
     */
    private class CopyTask extends SwingWorker<Void, Integer> {

        private File source;

        private File target;

        private final int MAX_THREADS = 10;

        public CopyTask(File source, File target) {
            this.source = source;
            this.target = target;
        }

        /**
         * Main method
         */
        @Override
        public Void doInBackground() throws Exception {

            synchronized (this) {
                stop = false;
            }

            if (invalidTarget(target.getAbsolutePath())) {
                detailsBox.append("Forbidden target folder " + LINE_SEPARTOR);
                return null;
            }

            logger.log(Level.INFO, "Started processing:{0}", source.getAbsolutePath());
            detailsBox.append("\nStarted processing: " + new Date().toString() + LINE_SEPARTOR);

            try {
                final List<String> fileNames = expandNumbers(getIdentifiers(txtIdentifiers.getText()));
                final Map<File, File> filesToCopy = getPaths(fileNames);
                createCopyThreads(filesToCopy);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Internal error:", e);
                detailsBox.append("\n Error in copying one or more files: \n" + e.getCause());
            }

            detailsBox.append("End: " + new Date().toString() + LINE_SEPARTOR);
            logger.log(Level.INFO, "Ended processing:{0}", source.getAbsolutePath());
            return null;
        }

        // Copy files stored in java.util.Map
        private void createCopyThreads(final Map<File, File> filesToCopy) {
            final ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS);
            final Set<File> files = filesToCopy.keySet();

            for (final File source : files) {
                final File target = filesToCopy.get(source);
                pool.submit(new DownloadTask(source, target));
            }

            pool.shutdown(); // Note

            try {
                pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
                logger.info("Pool termination OK.");
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "Internal error:", e);
            }
        }

        // Gets path from the REMOTE crawler web service
        private Map<File, File> getPaths(List<String> identifiers) {
            final Map<File, File> paths = new HashMap<>();
            try {
                for (final String id : identifiers) { //batch should be ok. TODO check
                    detailsBox.append("Searching for:" + id + System.getProperty("line.separator"));
                    final String response = doGET(id);
                    final List<String> filePaths = extract(response);

                    //logger.info("File paths from service:" + filePaths);

                    for (final String src : filePaths) {
                        final File srcFile = new File(src);

                        if (!srcFile.exists()) {
                            logger.log(Level.INFO, "File does not exist: {0}", new String[]{src});
                            continue;
                        }
                        final String d = src.replace(source.getAbsolutePath(), "");
                        final String destFile = target.getAbsolutePath() + File.separator + d;

                        final File f = new File(destFile);

                        paths.put(srcFile, f);
                        logger.log(Level.INFO, "Populated map entry:{0}{1}", new String[]{src, destFile});
                    }
                }
            } catch (Exception e) {
                logger.log(Level.INFO, "Error looking up file path for:", e);
            }
            return paths;
        }

        private List<String> getIdentifiers(final String text) {
            final List<String> lines = Arrays.asList(text.split("\\r?\\n")); //split by line
            final List<String> identifiers = new ArrayList<>();

            for (final String s : lines) {
                final String lineIdentifiers[] = s.split("\\s*,\\s*");      // split commas

                for (final String pStr : lineIdentifiers) {
                    identifiers.add(pStr.trim());
                }
            }

            //logger.log(Level.INFO, "regexed identifiers are:{0}", identifiers);
            return identifiers;
        }

        // Simple method to expand ranges, e.g., 3-5
        private List<String> expandNumbers(final List<String> identifiers) {

            final List<String> expandedIdentifiers = new ArrayList<>();

            for (final String s : identifiers) {

                if (s.contains("-")) {
                    // remove space?
                    final String[] sp = s.split("-");

                    if (sp.length == 2 && valid(sp[0]) && valid(sp[1])) {

                        int i = Integer.parseInt(sp[0]);
                        final int j = Integer.parseInt(sp[1]);

                        while (i <= j) {
                            expandedIdentifiers.add(String.valueOf(i));
                            i++;
                        }
                    } else {
                        logger.log(Level.INFO, "Unexpected token. Ignoring :{}", s);
                    }
                } else {
                    if (valid(s)) {
                        expandedIdentifiers.add(s);
                    } else {
                        logger.log(Level.INFO, "Unexpected token. Ignoring :{}", s);
                    }
                }
            }

            //logger.log(Level.INFO, "The modified identifiers are:{0}", identifiers);
            return expandedIdentifiers;
        }

        private boolean valid(String s) {
            return s.matches("^[0-9]+$"); // only numbers allowed //TODO might have to accommodate others
        }


        @Override
        public void done() {
            setProgress(100);
            btnCopy.setText("Copy");
        }

        public String doGET(final String s) throws Exception {
            final HttpClientManager httpClientManager = new HttpClientManager();
            final HttpGet getMethod0 = httpClientManager.doGET(s);
            final HttpResponse httpResponse;
            try {
                httpResponse = httpClientManager.httpClient.execute(getMethod0);
                final HttpEntity e = httpResponse.getEntity();
                final String response = EntityUtils.toString(e);
                logger.log(Level.INFO, "Content from ws:{0}", response);
                return response;
            } catch (IOException e) {
                detailsBox.append("Error searching." + System.getProperty("line.separator"));
                logger.log(Level.WARNING, "Error:", e);
            }
            return "";
        }

        private List<String> extract(final String s) {
            if ( s== null || s.isEmpty()) {
                return Collections.emptyList();
            }
            final String tmp = s.replace("[", "");
            final String tmp2 = tmp.replace("]","");
            final String[] arrs = tmp2.split("\\s*,\\s*");
            return Arrays.asList(arrs);
        }

        private boolean invalidTarget(final String filename) { //TODO expand (?)
            return filename.contains("storage.yale.edu") || filename.contains("fc_Beinecke-807001-YUL")
                    || filename.contains("Volume");
        }

        /**
         * Copying of one file (not folder)
         */
        private class DownloadTask implements Runnable {

            private final File name;

            private final File dest;

            public DownloadTask(final File name, final File toPath) {
                this.name = name;
                this.dest = toPath;
            }

            @Override
            public void run() {
                try {
                    copy(name, dest);
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Error:", e);
                }
            }

            private void copy(final File sourceFile, final File targetFile) throws IOException {

                if (stop) {
                    return;
                }

                // if it already exists skip it if the user wants
                if (!checkBox.isSelected() && targetFile.exists()) {
                    logger.log(Level.INFO, "Skipped file:{0}", new Object[]{sourceFile.getAbsolutePath()});
                    detailsBox.append("Skipped file: " + sourceFile.getAbsolutePath() + LINE_SEPARTOR);
                    return;
                }

                String filePath = sourceFile.getCanonicalPath();

                logger.log(Level.INFO, "Copying file: {0} to path: {1}", new Object[]{sourceFile.getAbsolutePath(),
                        targetFile.getAbsolutePath()});
                detailsBox.append("Copying: " + filePath + LINE_SEPARTOR);

                try {
                    FileUtils.copyFile(name, targetFile);
                } catch (IOException e) {
                    detailsBox.append("Error in copying:" + filePath + " : " + e.getMessage() +LINE_SEPARTOR);
                    throw e;
                }
                logger.log(Level.INFO, "Copied file:{0}", new Object[]{sourceFile.getAbsolutePath()});
                detailsBox.append("Done: " + filePath + LINE_SEPARTOR);
            }
        }
    }
}