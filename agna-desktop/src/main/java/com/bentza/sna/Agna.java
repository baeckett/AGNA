package com.bentza.sna;

import com.bentza.sna.AgnaLog;
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
        // 2.1.3: the main frame paints over the splash; keep the splash
        // above everything so it stays visible for its full wait time
        splash_screen.setAlwaysOnTop(true);
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
                        } catch (Exception e0) {
      AgnaLog.warn("suppressed exception", e0);
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
                            } catch (Exception e2) {
      AgnaLog.warn("suppressed exception", e2);
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
// Historical developer notes from the original 2001-2005 codebase were
// removed during the 2.1.3 revival; see docs/ for the current manual, CLI
// reference, and contribution guidance.
