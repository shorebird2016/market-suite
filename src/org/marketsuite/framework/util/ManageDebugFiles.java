package org.marketsuite.framework.util;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;

// after construction
public class ManageDebugFiles extends PrintStream {

    // We don't want the end-user creating the OutputStream
    // so the constuctors are private
    private ManageDebugFiles(OutputStream os, boolean b) {
        super(os, b);
    }

    private static ManageDebugFiles Create(boolean bIsOutput) {
        // Since we derive from PrintStream we need
        // to create an OuputStream -- even
        // if it is not used (which this won't be)
        ManageDebugFiles retVal =
            new ManageDebugFiles(new ByteArrayOutputStream(),
                true);
        retVal._bIsOutput = bIsOutput;
        return retVal;
    }

    public boolean checkError() // no need to synchronize since
    {                           // _bIsOuput can't change
        return globalCheckError(_bIsOutput);
    }

    private static synchronized boolean globalCheckError(boolean bIsOutput) {
        if (bIsOutput) {
            return _output.checkError();
        }
        return _err.checkError();
    }

    public void close() {
        globalClose(_bIsOutput);
    }

    private static synchronized void globalClose(boolean bIsOutput) {
        if (bIsOutput) {
            _output.close();
        }
        else {
            _err.close();
        }
    }

    public void flush() {
        globalFlush(_bIsOutput);
    }

    private synchronized void globalFlush(boolean bIsOutput) {
        if (bIsOutput) {
            _output.flush();
        }
        else {
            _err.flush();
        }
    }

    public void print(boolean b) {
        globalPrint(_bIsOutput, String.valueOf(b));
    }

    public void print(char c) {
        globalPrint(_bIsOutput, String.valueOf(c));
    }

    public void print(char[] s) {
        globalPrint(_bIsOutput, String.valueOf(s));
    }

    public void print(double d) {
        globalPrint(_bIsOutput, String.valueOf(d));
    }

    public void print(float f) {
        globalPrint(_bIsOutput, String.valueOf(f));
    }

    public void print(int i) {
        globalPrint(_bIsOutput, String.valueOf(i));
    }

    public void print(long l) {
        globalPrint(_bIsOutput, String.valueOf(l));
    }

    public void print(Object obj) {
        globalPrint(_bIsOutput, String.valueOf(obj));
    }

    public void print(String s) {
        globalPrint(_bIsOutput, s);
    }

    public void println() {
        globalPrint(_bIsOutput, _lineSeparator);
    }

    public void println(boolean b) {
        globalPrint(_bIsOutput,
            String.valueOf(b) + _lineSeparator);
    }

    public void println(char c) {
        globalPrint(_bIsOutput,
            String.valueOf(c) + _lineSeparator);
    }

    public void println(char[] s) {
        globalPrint(_bIsOutput,
            String.valueOf(s) + _lineSeparator);
    }

    public void println(double d) {
        globalPrint(_bIsOutput,
            String.valueOf(d) + _lineSeparator);
    }

    public void println(float f) {
        globalPrint(_bIsOutput,
            String.valueOf(f) + _lineSeparator);
    }

    public void println(int i) {
        globalPrint(_bIsOutput,
            String.valueOf(i) + _lineSeparator);
    }

    public void println(long l) {
        globalPrint(_bIsOutput,
            String.valueOf(l) + _lineSeparator);
    }

    public void println(Object obj) {
        globalPrint(_bIsOutput,
            String.valueOf(obj) + _lineSeparator);
    }

    public void println(String s) {
        globalPrint(_bIsOutput,
            s + _lineSeparator);
    }

    private static void printOut(final String s) {
        stdOut.append(s);
        if (scrollOut)
            stdOut.setCaretPosition(stdOut.getDocument().getLength());
    }

    private static void printErr(final String s) {
        stdErr.append(s);
        if (scrollErr)
            stdErr.setCaretPosition(stdErr.getDocument().getLength());
    }

    private static synchronized void globalPrint(boolean bIsOutput, final String s) {
        globalVerifySize(bIsOutput, s.length());
        //String str = "[" + (new java.util.Date()).toString() + "]";
        String str = "[" + (new Date()).toString() + "]";
        if (bIsOutput) {
            _output.print(str + s);
            if (stdioFrame != null && stdioFrame.isShowing() && stdioFrame.getState() == Frame.NORMAL && outOK) {
                if (SwingUtilities.isEventDispatchThread()) {
                    printOut(s);
                }
                else {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            printOut(s);
                        }
                    });
                }
            }
        }
        else {
            _err.print(str + s);
            if (stdioFrame != null && stdioFrame.isShowing() && stdioFrame.getState() == Frame.NORMAL && errOK) {
                if (SwingUtilities.isEventDispatchThread()) {
                    printErr(s);
                }
                else {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            printErr(s);
                        }
                    });
                }
            }
        }
    }

    // We will never be a base class so no need to implement setError
    // protected void setError()
    public void write(byte[] buf, int off, int len) {
        globalWrite(_bIsOutput, buf, off, len);
    }

    public void write(int b) {
        globalWrite(_bIsOutput, b);
    }

    public void write(byte[] buf) {
        globalWrite(_bIsOutput, buf, 0, buf.length);
    }

    private static synchronized void globalWrite(boolean bIsOutput,
                                                 byte[] buf,
                                                 int off,
                                                 int len) {
        globalVerifySize(bIsOutput, len);
        if (bIsOutput) {
            _output.write(buf, off, len);
        }
        else {
            _err.write(buf, off, len);
        }
    }

    private static synchronized void globalWrite(boolean bIsOutput,
                                                 int b) {
        // int b is the "byte" written hence a 1 for a size
        globalVerifySize(bIsOutput, 1);
        if (bIsOutput) {
            _output.write(b);
        }
        else {
            _err.write(b);
        }
    }

    private static void globalVerifySize(boolean bIsOutput,
                                         int length) {
        if (bIsOutput) {
            _outputSize += length;
            // file length exceeded
            if (_outputSize > _maxDebugFileSize) {
                _outFileV1IsCurrentVersion =
                    TruncateAndSwap(_outBaseFileV1, _outBaseFileV2, _outFileV1IsCurrentVersion, true); // indicates is stdout
            }
        }
        else {
            //need to increment this variable too.
            //punitma - fix for bug #169.
            _errSize += length;
            if (_errSize > _maxDebugFileSize) {
                _errFileV1IsCurrentVersion =
                    TruncateAndSwap(_errBaseFileV1,
                        _errBaseFileV2,
                        _errFileV1IsCurrentVersion,
                        false); // indicates is stderr
            }
        }
    }

    // Value will not change so no need to synchronize before
    // accessing
    private static String _lineSeparator; // CR/LF or whatever
    // system dictates
    private static int _outputSize = 0;
    private static int _errSize = 0;
    private static PrintStream _output;
    private static PrintStream _err;
    private static boolean _wasInited = false;
    private static Class RSTA;
    private static Object rsta;
    // when a debug file (FarmerOut.* or FarmeErr.*) reaches
    // >= _maxDebugFileSize it is truncated. The new version
    // is set to _truncatedDebugFileSize;
    public static final int _maxDebugFileSize = 0x100000; // 1 MB
    public static final int _truncatedDebugFileSize =
        _maxDebugFileSize / 2;
    private static boolean _errFileV1IsCurrentVersion = false;
    private static final String userHome = System.getProperty("user.home");
    private static final String _errBaseFileV1 = "sim.err.v1";
    private static final String _errBaseFileV2 = "sim.err.v2";
    private static boolean _outFileV1IsCurrentVersion = false;
    private static final String _outBaseFileV1 = "sim.out.v1";
    private static final String _outBaseFileV2 = "sim.out.v2";
    private static File _debugDir = null;

    // we take a directory and a fileName
    // make a File,
    // make a FileOutputStream,
    // make a BufferedOutputStream,
    // make a PrintStream a bunch of times so put it in one place
    private static PrintStream MakePrintStream(String dir, String file)
        throws IOException {
        // The true paramter to PrintStream means:
        // output buffer will be flushed whenever
        //   1) a byte array is written,
        //   2) one of the println methods is invoked,
        //   3) newline character or byte ('\n') is written
        // This is fine since we are writing to the RAM disk and
        // not the Flash
        File tempFile = new File(dir, file);
        // An attempt is made to delete the file before this but it
        // fails likely do to a non-cleanedup reference to the file.
        // This extra delete is to prevent the PrintStream from attaching
        // to an existing file marked for delete. Some odd behavior was
        // being experienced before this explicit delete was added.
        tempFile.delete();
        return new PrintStream(
            new BufferedOutputStream(
                new FileOutputStream(
                    tempFile)),
            true);
    }

    // parentPath is the parent of the location where the Controller's
    // modules are found
    synchronized public static boolean init() throws Exception {
        if (_wasInited) // programmer error
        {
            String error = "PersistFile.Init() already called";
            System.err.println(error);
            throw new Exception(error);
        }
        _wasInited = true;
        _lineSeparator = System.getProperty("line.separator");
        try {
            File tempFile = null;
            _debugDir = new File(userHome);
            if (!_debugDir.exists()) {
                _debugDir.mkdir();
            }
            tempFile = new File(_debugDir.getAbsolutePath(), _outBaseFileV2);
            if (tempFile.exists()) // remove #2 copy of out file
            {
                tempFile.delete();
            }
            tempFile = new File(_debugDir.getAbsolutePath(), _errBaseFileV2);
            if (tempFile.exists()) // remove #2 copy of error file
            {
                tempFile.delete();
            }
            _output = MakePrintStream(_debugDir.getAbsolutePath(), _outBaseFileV1);
            _err = MakePrintStream(_debugDir.getAbsolutePath(), _errBaseFileV1);
            _outFileV1IsCurrentVersion = true;
            _errFileV1IsCurrentVersion = true;
            // Now that setup is complete actually associated
            // stdout/stderror with our toggling files
            System.setOut(ManageDebugFiles.Create(true));
            System.setErr(ManageDebugFiles.Create(false));
        } catch (IOException ex) {
            // if we end up here we have no way to display an error
            // so we just quit because there is no way to identify
            // what is taking place in the Controller
            return false;
        }
        return true;
    }

    static private boolean TruncateAndSwap(String fileName1,
                                           String fileName2,
                                           boolean v1IsCurent,
                                           boolean bIsStdOut) {
        File sourceFile = null;
        String sourceFileName = null;
        String destFileName = null;
        if (v1IsCurent) {
            sourceFileName = fileName1;
            destFileName = fileName2;
        }
        else {
            sourceFileName = fileName2;
            destFileName = fileName1;
        }
        sourceFile = new File(_debugDir.getAbsolutePath(),
            sourceFileName);
        byte data[] = new byte[_truncatedDebugFileSize];
        FileInputStream fileInputStream = null;
        PrintStream destStream = null;
        try {
            fileInputStream = new FileInputStream(sourceFile);
            destStream =
                MakePrintStream(_debugDir.getAbsolutePath(),
                    destFileName);
            // import to use _outputSize here and not sourceFile.length()
            // It seems Java is a bit slow in determining file sizes
            // so _outputSize is accurate
            fileInputStream.skip(bIsStdOut ? _outputSize : _errSize - data.length);
            fileInputStream.read(data, // buffer
                0,
                data.length); // length to read
            destStream.write(data, 0, data.length);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (bIsStdOut) {
                _output.close();
                _outputSize = data.length;
                _output = destStream;
            }
            else {
                _err.close();
                _errSize = data.length;
                _err = destStream;
            }
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // delete previous version of output stream used
            sourceFile.delete();
        }
        // Toggle the current version
        return !v1IsCurent;
    }

    // Display error and output in a gui frame
    private static JFrame stdioFrame;
    private static JTextArea stdOut;
    private static JTextArea stdErr;
    private static boolean errOK = true;
    private static boolean outOK = true;
    private static boolean scrollOut = true;
    private static boolean scrollErr = true;
    private static JTextField outSearch;
    private static JTextField errSearch;
    private static JCheckBox outCI, errCI;
    private static JLabel errLabel, outLabel;
    private static JButton outForward, outBackward;
    private static JButton errForward, errBackward;
    private static JButton outExclude;
    private boolean _bIsOutput;
    private static JTabbedPane tabbedPane;

    static boolean search(String searchString, JTextArea ta, boolean CI, boolean back) {
        String txt;
        Caret caret = ta.getCaret();
        int pos = ta.getCaretPosition();
        if (CI) {
            txt = ta.getText().toLowerCase();
            searchString = searchString.toLowerCase();
        }
        else
            txt = ta.getText();
        int idx;
        if (back) {
            idx = txt.lastIndexOf(searchString, pos - searchString.length() - 1);
            if (idx < 0) return false;
            caret.setDot(idx + searchString.length());
            caret.moveDot(idx);
            idx += searchString.length();
        }
        else {
            idx = txt.indexOf(searchString, pos + 1);
            if (idx < 0) return false;
            caret.setDot(idx);
            caret.moveDot(idx + searchString.length());
        }


        caret.setSelectionVisible(true);
        caret.setVisible(true);

        try { // make sure the search string is visible
            ta.scrollRectToVisible(ta.modelToView(idx));
        } catch (BadLocationException ex) {
            //
        }

        return true;
    }

    public static final void initF4(JFrame f) {
        f.addWindowListener(new WindowAdapter() {
            public void windowIconified(WindowEvent ev) {
                if (null != stdioFrame) {
                    stdioFrame.setState(Frame.ICONIFIED);
                }
            }

            public void windowDeiconified(WindowEvent ev) {
                if (null != stdioFrame) {
                    stdioFrame.setState(Frame.NORMAL);
                }
            }
        });
    }
}