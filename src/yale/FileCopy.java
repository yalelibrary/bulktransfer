package yale;

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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import javax.swing.JProgressBar;
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
 * Simple GUI for copying files
 *
 * @author Osman Din
 */
public class FileCopy extends JFrame implements ActionListener, PropertyChangeListener {

    private static Logger logger = Logger.getLogger("yale.FileCopy");

    private static final long serialVersionUID = 1L;

    private JTextField txtSource;

    private static Handler logFileHandler;

    private JTextField txtTarget;

    private JProgressBar progressAll;

    private JTextArea detailsBox;

    private JTextArea txtIdentifiers;

    private JButton btnCopy;

    private CopyTask task;

    private String source = "";

    private String target = "";

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
            logFileHandler = new FileHandler("log-" + logfile + ".log");
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
        menu.setMnemonic(KeyEvent.VK_H); //?
        menuBar.add(menu);

        // Create first menu item:
        final JMenuItem menuItem = new JMenuItem("About",
                KeyEvent.VK_A);
        menuItem.addActionListener(new AboutDialogAction());
        menu.add(menuItem);


        // Create second menu item:
        final JMenuItem menuItem2 = new JMenuItem("Instructions",
                KeyEvent.VK_I);
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

        progressAll = new JProgressBar(0, 100);
        progressAll.setStringPainted(true);
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
        final JPanel panProgressBars = new JPanel(new BorderLayout(0, 5));

        panInputLabels.add(lblSource, BorderLayout.NORTH);
        panInputLabels.add(lblTarget, BorderLayout.CENTER);
        panInputFields.add(txtSource, BorderLayout.NORTH);
        panInputFields.add(txtTarget, BorderLayout.CENTER);
        panProgressBars.add(progressAll, BorderLayout.NORTH);

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
        infoPanel.add(panProgressBars, BorderLayout.SOUTH);
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
            }

            else { // customize as necessary
                final int option = JOptionPane.showConfirmDialog(this,
                 "The target file or directory already exists. Do you want to overwrite it?",
                 "Overwrite the target", JOptionPane.YES_NO_OPTION);
                if (option != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            task = this.new CopyTask(source, target);
            task.addPropertyChangeListener(this);
            task.execute();
            btnCopy.setText("Cancel");
        } else if ("Cancel".equals(btnCopy.getText())) {
            synchronized (this)  {
                stop = true;
            }
            task.cancel(true);
            btnCopy.setText("Copy");
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equals(evt.getPropertyName())) {
            int progress = (Integer) evt.getNewValue();
            progressAll.setValue(progress);
        }
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
    class CopyTask extends SwingWorker<Void, Integer> {

        private File source;

        private File target;

        private long totalBytes = 0L;

        private long copiedBytes = 0L;

        private final int MAX_THREADS = 10;

        private Map<File, File> filesToCopy = new HashMap<File, File>();

        private List<String> identifiers = new ArrayList<String>();

        public CopyTask(File source, File target) {
            this.source = source;
            this.target = target;
            progressAll.setValue(0);
        }

        /**
         * Main method
         */
        @Override
        public Void doInBackground() throws Exception {
            synchronized (this) {
                stop = false;
            }
            logger.info("Started background task");
            detailsBox.append("\n");
            retrieveTotalBytes(source); // used to calculate progress

            logger.log(Level.INFO, "Total bytes:{0}", new Object[]{totalBytes});

            try {
                if (suspiciousTarget(target.getAbsolutePath())) {
                    detailsBox.append("Forbidden target folder");
                    return null;
                }

                // filter in
                //final String s[] = txtIdentifiers.getText().split("\\r?\\n");
                identifiers = Arrays.asList(txtIdentifiers.getText().split("\\s*,\\s*"));
                //identifiers = new ArrayList<String>(Arrays.asList(s)) ;
                gather(source, target);
                copyFiles();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Internal error:", e);
                detailsBox.append("\n Error in copying one or more files: \n");
                detailsBox.append(e.getCause().toString());
            }
            detailsBox.append("\nOK\n");
            return null;
        }

        @Override
        public void done() {
            setProgress(100);
            btnCopy.setText("Copy");
        }

        // Copy files stored in java.util.Map
        private void copyFiles() {
            final ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS);
            final Set<File> files = filesToCopy.keySet();

            for (File source : files) {
                final File target = filesToCopy.get(source);
                pool.submit(new DownloadTask(source, target));
            }

            pool.shutdown(); //TODO check. why is this shutdown here?
            logger.info("Pool shutdown OK");

            try {
                pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
               logger.info("Pool termination OK.");
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "Internal error:", e);
            }

            filesToCopy.clear();
            filesToCopy = new HashMap<File, File>();
            identifiers = new ArrayList<String>();
        }

        /**
         * Computes total number of bytes
         * Possibly time intensive for large directories
         */
        private void retrieveTotalBytes(final File sourceFile) {
            final File[] files = sourceFile.listFiles();

            if (files == null) {
                return;
            }

            for (final File file : files) {
                if (file.isDirectory()) {
                    retrieveTotalBytes(file);
                } else {
                    totalBytes += file.length();
                }
            }
        }

        /**
         * Makes directories (if necessary) and populates a map (for later retrieval)
         */
        private void gather(final File sourceFile, final File targetFile) throws IOException {
            if (sourceFile.isDirectory()) {

                if (!targetFile.exists()) {
                    boolean success = targetFile.mkdirs();
                    logger.log(Level.INFO, "Dir created:{0}", new Object[]{success});
                }

                final String[] paths = sourceFile.list();

                for (final String filePath : paths) {
                    final File src = new File(sourceFile, filePath);
                    final File dest = new File(targetFile, filePath);

                    if (src.isDirectory()) {
                        if (!browse(src)) {
                            detailsBox.append("Skipped directory:" + src.getName() + "\n");
                            continue;  //skip if we don't want to browse the folder
                        }
                    }

                    gather(src, dest);
                }
            } else { // add file object to map for later multithreaded retrieval
                if (downloadFile(sourceFile)) {
                    filesToCopy.put(sourceFile, targetFile);
                } else {
                    System.out.println("Skipped:" + sourceFile.getAbsolutePath());
                }
            }
        }

        private boolean downloadFile(final File f) {

            final String fullFileName = f.getName();

            // remove the file extension

            int count = fullFileName.length() - fullFileName.replace(".", "").length();

            if (count > 1) {
                detailsBox.append("Unexpected file name:" + f.getAbsolutePath());
                return false;
            }


            final String fileName = fullFileName.substring(0, fullFileName.lastIndexOf('.'));

            // System.out.println("Will evaluate:" + fileName);

            for (final String s : identifiers) {

                if (s.equals(fileName)) {
                    return true;
                }

                if (s.contains("-")) {
                    final String[] sp = s.split("-");

                    if (sp.length == 2) {

                        //strip digits

                        final String fileA = sp[0].replaceAll("[^\\d.]", "");

                        if (!fileA.matches("[0-9]+")) {
                            continue;
                        }


                        final int fileANum = Integer.parseInt(fileA);
                        final String fileB = sp[1].replaceAll("[^\\d.]", "");

                        if (!fileB.matches("[0-9]+")) {
                            continue;
                        }

                        final int fileBNum = Integer.parseInt(fileB);
                        final String prefixA = sp[0].replace(sp[0], "");
                        final String prefixB = sp[1].replace(sp[1], "");

                        if (prefixA.equals(prefixB) && fileName.startsWith(prefixA)) {
                            final String fi = fileName.replaceAll("[^\\d.]", "");

                            if (!fi.isEmpty()) {
                                if (!fi.matches("[0-9]+")) {
                                    continue;
                                }

                                final int fileNum = Integer.parseInt(fi);

                                if (fileNum >= fileANum && fileNum <= fileBNum) {
                                    return true;
                                }
                            }
                        }
                    } // end of sp.length == 2
                }
            }

            return false;
        }

        /**
         * Decides whether to download a folder or not. A type of a filter, hence
         * should be replaced by a FileFilter
         */
        private boolean browse(final File dir) {

            return true;  // replace as necessary
        }

        /**
         * Copying of one file (not folder)
         */
        private class DownloadTask implements Runnable {

            private File name;

            private final File dest;

            public DownloadTask(final File name, final File toPath) {
                this.name = name;
                this.dest = toPath;
            }

            @Override
            public void run() {
                try {
                    fileCopy(name, dest);
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Error:", e);
                }
            }

            private void fileCopy(final File sourceFile, final File targetFile) throws IOException {

                if (stop) {
                    return;
                }

                // if it already exists skip it if the user wants
                if (!checkBox.isSelected() && targetFile.exists()) {
                    logger.log(Level.INFO, "Skipped file:{0}", new Object[]{sourceFile.getAbsolutePath()});
                    detailsBox.append("Skipped file: " + sourceFile.getAbsolutePath() + "\n");
                    return;
                }

                logger.log(Level.INFO, "Copying file:{0}", new Object[]{sourceFile.getAbsolutePath()});
                detailsBox.append("Copying file " + sourceFile.getAbsolutePath() + " ... " + "\n");

                final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile));
                final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(targetFile));
                int readByte;

                while ((readByte = bis.read()) != -1) {
                    bos.write(readByte);
                    setProgress((int) (copiedBytes++ * 100 / totalBytes));
                }

                try {
                    bis.close();
                    bos.close();
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Internal error:", e);
                }

                logger.log(Level.INFO, "Copied file:{0}", new Object[]{sourceFile.getAbsolutePath()});
            }
        }
    }

    // TODO Filters file names
    private boolean suspiciousTarget(final String filename) {

        if (filename.contains("storage.yale.edu") || filename.contains("fc_Beinecke-807001-YUL")) {
            return true;
        }

        if (filename.contains("Volume")) {
            return true;
        }

        if (filename.contains("ladybird")) {
            return true;
        }

        return false;
    }
}