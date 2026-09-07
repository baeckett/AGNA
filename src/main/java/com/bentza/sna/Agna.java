package com.bentza.sna;

// Agna 2.1.2
// February, 2005

import com.bentza.sna.gui.AgnaSplash;
import com.bentza.sna.gui.MainFrame;
import com.bentza.sna.gui.GrNet;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

public class Agna
    {
    public Agna()
        {
        }

    // // PROGRAM PRINCIPAL: *********************

    public static void main(String s[])
        {
        final Environment env = new Environment();
        final AgnaSplash splash_screen = new AgnaSplash(null);
        System.out.println("");
        System.out.println("   ****************************************");
        System.out.println("");
        System.out.println("    " + Environment.getApplicationName()
                + " Version " + Environment.getApplicationVersion());
        System.out.println("");
        System.out.println("    " + Environment.getApplicationCopyright());
        System.out.println("");
        System.out.println("    " + Environment.getApplicationUrl());
        System.out.println("");
        System.out.println("   ****************************************");
        System.out.println("");
        System.out.println("    L O A D I N G . . .");
        final MainFrame frame = new MainFrame();

        frame.getCurrentFrame().setDefaultCloseOperation(
                WindowConstants.DO_NOTHING_ON_CLOSE);

        final WindowListener main_window_listener = new WindowAdapter()
            {
                public void windowActivated(WindowEvent e)
                    {
                    MainFrame.getCurrentFrame().repaint();
                    try
                        {
                        GrNet.update_network_needed = true;
                        } catch (Exception e0)
                        {
                        }
                    }

                // ask before close
                public void windowClosing(WindowEvent e)
                    {
                    final int confirm = JOptionPane.showOptionDialog(MainFrame
                            .getCurrentFrame(), "Quit Agna?",
                            "Exit Confirmation", JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE, null, null, null);
                    if (confirm == 0)
                        {
                        // frame.grid_model.setReady(false); // avoid annoying
                        // message 'Bad value encountered!...'
                        frame.saveChangedFiles(false);

                        MainFrame.getCurrentFrame().dispose();
                        System.out.println("");
                        System.out
                                .println("   ****************************************");
                        System.out.println("");
                        System.out.println("    Thank you for using Agna.");
                        System.out.println("");
                        System.out.println("    For any comment, question or");
                        System.out.println("    suggestion please e-mail:");
                        System.out.println("");
                        System.out.println("    "
                                + Environment.getApplicationEmail());
                        System.out.println("");
                        System.out
                                .println("   ****************************************");
                        System.exit(0);
                        }
                    };
            };

        MainFrame.getCurrentFrame().addWindowListener(main_window_listener);
        System.out.println("    COMPLETED.");
        if (s.length != 0 && s[0] != null && !s[0].equals(""))
            {// trying to automatically open network
            File new_file = null;
            boolean read_successful = false;
            try
                {
                new_file = new File(s[0]);
                if (new_file.exists() && !new_file.isDirectory())
                    {
                    frame.doSimplyOpenNetwork(new_file);
                    read_successful = true;
                    } else if (s.length != 1)
                    // file name might contain blancs
                    for (int i = 1; i < s.length; i++)
                        {
                        s[0] += " " + s[i];
                        try
                            {
                            new_file = new File(s[0]);
                            if (new_file.exists() && !new_file.isDirectory())
                                {
                                frame.doSimplyOpenNetwork(new_file);
                                read_successful = true;
                                break;
                                }
                            } catch (Exception e2)
                            {
                            }
                        }
                if (!read_successful)
                    JOptionPane
                            .showMessageDialog(
                                    MainFrame.getCurrentFrame(),
                                    "The path "
                                            + s[0]
                                            + " does not seem to point to a valid file.",
                                    "Agna Message", JOptionPane.ERROR_MESSAGE);
                } catch (Exception e1)
                {
                JOptionPane.showMessageDialog(MainFrame.getCurrentFrame(),
                        "Error opening file " + s[0], "Agna Message",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

// FRAME PRINCIPAL: ************************************************************

// *********************************************************
// probably never used:

// *********************************************************

// ********************************************************

/**
 * ExcelAdapter enables Copy-Paste Clipboard functionality on JTables. The
 * clipboard data format used by the adapter is compatible with the clipboard
 * format used by Excel. This provides for clipboard interoperability between
 * enabled JTables and Excel.
 */

// probably never used:
// ********************************************************
// abstract class performing mathematical treatment
// of Networks

// *******************************************

// ********************************************************

// ********************************************************
// COMPONENTA GRAFICA: ********************************************************

// ****************************************************

// ***************************************************************
// a pair of two float numbers - the coordinates of a node
// between 0 and 100

// ***************************************************************

// ***************************************************************
// ***************************************************************
// to be used in AgnaTableHeader:

// ***************************************************************
// Componenta buton dublu

// ***************************************************************
// file filter to be used by a JFileChooser;
// shows agna files only;

// ***************************************************************
// file filter to be used by a JFileChooser (in MainFrame's openOutput);

// ***************************************************************
// file filter to be used by a JFileChooser (in MainFrame's openOutput);
// shows html files only;

// ***************************************************************
// file filter to be used by a JFileChooser (in MainFrame's openOutput);
// shows text files only;

// ***************************************************************
// file filter to be used by a JFileChooser (in MainFrame's openOutput);
// shows text and dat files only;

// ***************************************************************
// file filter to be used by a JFileChooser;
// shows gif & jpeg files only;
// modified version of a class written by Shah Mumin (1999);
// ***************************************************************
// file filter to be used by a JFileChooser;
// shows jpeg files only;
// modified version of a class written by Shah Mumin (1999);
// ***************************************************************
// image preview component to be used by a JFileChooser
// modified version of a class written by Shah Mumin (1999);
// ***************************************************************

// ***************************************************************
// to be used in ShortestPathDialog:
// ***************************************************************
// ***************************************************************
// *************************************************
// to be used in MainFrame's OpenOutput:
/*
 * class AgnaEditorKit extends HTMLEditorKit { public Document
 * createDefaultDocument() { Document doc = super.createDefaultDocument();
 * ((HTMLDocument)doc).setAsynchronousLoadPriority(-1); return doc; } }
 */
// ***********************************************************

// ***************************************************************

// **********************

// ***************************************************************
// tips & tricks:
/*
 * jmenu created at runtime from directory list: File rootFolder = new
 * File(requestedRootFolderName); File[] childs = rootFolder.listFiles(); for
 * (int i=0; i<childs.length; i++) { if (childs[i].isDirectory()) {
 * jmenu.add(new JMenuItem(childs[i].getName()); } }
 */

/*
 * html to text convertor: static HTMLEditorKit kit = null; static HTMLEditorKit
 * getKit() { if( kit == null ) kit = new HTMLEditorKit(); return kit; } static
 * String htmlToText(String html) { HTMLDocument doc =
 * (HTMLDocument)getKit().createDefaultDocument(); try { Reader r = new
 * StringReader(html); getKit().read(r, doc, 0); } catch (IOException ioe) {
 * log.error("html parse error"); return ""; } catch (BadLocationException ble) {
 * log.error("html parse error"); return ""; } try { return doc.getText( 0,
 * doc.getLength() ); } catch (javax.swing.text.BadLocationException exc ) {
 * log.error("html parse error"); return ""; } }
 */

/*
 * // launches another application: String command="C:\\Program
 * Files\\Winamp\\winamp.exe"; try { Process p =
 * Runtime.getRuntime().exec(command); OutputStream os = p.getOutputStream();
 * BufferedInputStream is = new BufferedInputStream(p.getInputStream(), 40000);
 * byte[] b = new byte[40000]; int i; int n; do { n = is.available(); i =
 * is.read(b, 0, n); System.out.print(new String(b,0,n)); } while ((i =
 * is.read()) != -1); os.flush(); p.waitFor(); } catch (Exception ex) {
 * ex.printStackTrace(); }
 */

/*
 * String tmp = ""; // lists ascii code: for (int i = 0; i < 256; i++) { tmp += "<br>" +
 * String.valueOf(i) + " (" + (char)i + ")<br>"; }
 */

// doAppendParagraphToOutput(tmp);
/*
 * if (checkAllValues()) updateNetwork(); else return; String tmp_str = new
 * String(""); tmp_str = AgnaLib.outPrestige(my_full_net.getNetwork()); tmp_str +=
 * "Generated by: Agna 2.0\n\n";
 * //output_edit.setCaretPosition(output_edit.getText().length());
 * //output_edit.setCaretPosition(output_edit.getDocument().getLength());
 * //output_edit.replaceSelection(tmp_str); doAppendParagraphToOutput(tmp_str);
 * System.gc();
 */

// ***************************************************************
// De facut:
// - tiled background (maybe later)
// - use preview image in agn files
// - show centrality on graph
// - quick analysis
// - language support
//
// ***************************************************************
// comenzi reciclabile:
// JOptionPane.showInputDialog("Enter number of nodes");
// JOptionPane.showMessageDialog(null, "Serus!", "Test",
// JOptionPane.INFORMATION_MESSAGE);
// ||||||||||||||

/*
 * tmp_number=JOptionPane.showInputDialog("Enter number of nodes"); try {
 * nodes_count=Integer.parseInt(tmp_number); } catch (Exception e) {
 * nodes_count=2; } if (nodes_count==0) {nodes_count=2;} tmp_number=null; //
 * memory commands: Runtime r = Runtime.getRuntime() ; // amount of unallocated
 * memory long free = r.freeMemory(); // total amount of memory available
 * allocated and unallocated. long total = r.totalMemory(); // total amount of
 * allocated memory long inuse = r.totalMemory() - r.freeMemory() ; // max
 * memory JVM will attempt to use long max = r.maxMemory(); // suggest now would
 * be a good time for garbage collection System.gc() ;
 * //*************************** Reading files from jars: URL url =
 * ClassLoader.getSystemResource(name); or InputStream stream =
 * ClassLoader.getSystemResourceAsStream(name); These techniques allow you to
 * read a file out of a JAR file that is located in your class path. You don't
 * need to specify the JAR filename.
 */

// *********************************************************
// * Adrese utile
// *********************************************************
// *
// * indexul claselor java:
// * http://java.sun.com/j2se/1.4/docs/api/allclasses-noframe.html
// *
// * carti:
// * http://www.kaposnet.hu/books/
// * http://manning.spindoczine.com/sbe/
// * http://www.jalice.net/
// * http://www.anilbachi.8m.com/
// * http://mindprod.com/jgloss.html
// *
// * Java Compiling Service:
// *
// * http://www.innovation.ch/java/java_compile.html
// * http://hastu.com/indojive/compiler.html
// *
// * Java Tutorial:
// *
// * http://www.phrantic.com/scoop/toc.htm
// * http://www.ibiblio.org/javafaq/javatutorial.html#xtocid459721
// * http://www.kaposnet.hu/books/hackjava/index.htm
// * http://www.kaposnet.hu/books/javabyexample/index.htm
// * http://www.kaposnet.hu/books/javaguru/index.htm
// * http://java.sun.com/docs/books/tutorial/uiswing/components/frame.html
// * http://java.about.com/library/javanotes4/bl-index.htm
// * http://developer.java.sun.com/developer/JDCTechTips/
// *http://www.janeg.ca/case/techIndex.html
// * Swing Tutorials:
// * http://manning.spindoczine.com/sbe/
// * http://www.apl.jhu.edu/~hall/java/Swing-Tutorial/
// *
// * Resources!
// * http://www.ibiblio.org/javafaq/
// * swing examples:
// * http://www2.gol.com/users/tame/swing/examples/
// *
// * RUNNABLE JAR:
// * http://www.javaworld.com/javaworld/javatips/jw-javatip127.html?
// * JNI article:
// * http://www.javaworld.com/jw-10-1999/jw-10-jni.html
// * making a double-clickable application in mac os:
// * http://developer.apple.com/java/javatutorial/doubleclick.html
// * adding scripting facilities:
// * http://www.javaworld.com/javaworld/jw-10-1999/jw-10-script-p4.html
// * menu help tip:
// * http://www.javaworld.com/javaworld/javaqa/1999-10/01-qa-menuhelp.html?
// * browser
// * http://java.sun.com/products/jfc/tsc/articles/tictactoe/index.html
// * guru:
// * http://saloon.javaranch.com/
// *
// *********************************************************
