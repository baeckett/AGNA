package com.bentza.sna.gui;

import com.bentza.sna.io.IOUtils;
import com.bentza.sna.Environment;
import com.bentza.sna.net.AgnaLib;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTML.Tag;

public class AgnaTextPane extends JTextPane
    {
    public boolean changed;

    private String file_name;

    private String folder_name;

    private int external_elements;

    // sets horizontal scrollbar off:
    public boolean getScrollableTracksViewportWidth()
        {
        Component parent = this.getParent();
        ComponentUI ui = this.getUI();
        return (ui.getPreferredSize(this).width <= parent.getSize().width);
        }

    private CaretListener list_caret = new CaretListener()
        {
            public void caretUpdate(CaretEvent e)
                {
                setChanged(true);
                }
        };

    AgnaTextPane()
        {
        // RTFEditorKit rtf_edit = new RTFEditorKit();
        // output_edit.setEditorKit(rtf_edit);
        // StyleContext edit_context = new StyleContext();
        // DefaultStyledDocument edit_doc = new
        // DefaultStyledDocument(edit_context);
        // output_edit.setDocument(edit_doc);

        HTMLEditorKit kit = new HTMLEditorKit();
        // StyleSheet css = new StyleSheet();
        HTMLDocument doc = (HTMLDocument) (kit.createDefaultDocument());
        this.setToolTipText("Output area");
        this.setEditorKit(kit);
        this.setDocument(doc);
        this.setContentType("text/html");
        this.setEditable(true);

        // output_edit.setText("<font face ='Verdana'>"
        // + "<b> blue text</b></font><br> Test"
        // + "<font face ='Trebuchet MS'color='red'>"
        // + "<b> red text</b>");

        // output_edit.setCaretPosition(output_edit.getDocument().getLength());
        // doAppendParagraphToOutput("test ok");

        /*
         * try { //kit.insertHTML(doc, doc.getLength(), "<IMG
         * SRC='file:Agna_icon.GIF'>", 0, 0, HTML.Tag.IMG);
         * //kit.insertHTML(doc, doc.getLength(), "<table border='1'
         * width='54%'><tr><td width='25%'>&nbsp;</td><td width='25%'>&nbsp;</td><td width='25%'>&nbsp;</td></tr><tr><td width='25%'>&nbsp;</td><td width='25%'>&nbsp;</td><td width='25%'>&nbsp;</td></tr></table>",
         * 0, 0, HTML.Tag.TABLE); } catch(Exception e) {}
         */

        // output_edit.setText("");
        changed = false;
        file_name = "";
        folder_name = "";
        // if (folder not exist) create folder(folder_name);
        // folder_name = MainFrame.getPathWithoutExtension(file_name) +
        // "_files";
        external_elements = 0;
        addCaretListener(list_caret);
        }

    public void clearAll()
        {
        String cont = this.getContentType();
        if (cont.equals("text/plain") || cont.equals("text"))
            {
            this.setText("");
            }

        if (cont.equals("text/html"))
            {
            this.setText("<html><head><body><p></p></body></head></html>");
            }

        external_elements = 0;
        this.setChanged(true);
        }

    public void setChanged(boolean tmp_changed)
        {
        changed = tmp_changed;
        }

    public boolean getChanged()
        {
        return changed;
        }

    public void setFileName(String tmp_name)
        {
        // setting file name:
        file_name = tmp_name;
        // setting folder name:
        folder_name = IOUtils.getPathWithoutExtension(file_name) + " files";
        // creating folder:
        }

    public String getFileName()
        {
        return file_name;
        }

    public void setFolder(String tmp_name)
        {
        folder_name = tmp_name;
        }

    // probably never used:
    public void setFolderName(String tmp_name)
        {
        folder_name = tmp_name;
        }

    public String getFolderName()
        {
        return folder_name;
        }

    // appends a string and inserts a horizontal rule before it
    public void appendBlock(String str)
        {
        String lb = System.getProperty("line.separator");
        StringBuffer newstr = new StringBuffer(str);
        String cont = this.getContentType();
        HTMLEditorKit kit = null;
        HTMLDocument doc = null;
        int tmp_position = 0;

        if (cont.equals("text/plain") || cont.equals("text"))
            {
            tmp_position = this.getDocument().getLength() - 1;
            if (tmp_position <= 0)
                tmp_position = 0;
            this.setCaretPosition(tmp_position);
            this.replaceSelection(lb
                    + "*****************************************" + lb + str);
            this.setChanged(true);
            tmp_position = this.getDocument().getLength() - 1;
            if (tmp_position <= 0)
                tmp_position = 0;
            this.setCaretPosition(tmp_position);
            }

        if (cont.equals("text/html"))
            {
            kit = ((HTMLEditorKit) this.getEditorKit());
            doc = ((HTMLDocument) this.getDocument());
            try
                {
                kit.insertHTML(doc, doc.getLength(), "<hr>", 0, 0, HTML.Tag.HR);
                // kit.insertHTML(doc, doc.getLength(), "<p>" +
                // newstr.toString() + "", 1, 0, HTML.Tag.P);
                kit.insertHTML(doc, doc.getLength(), newstr.toString() + "", 0,
                        0, null);
                this.setChanged(true);
                // this.setCaretPosition(this.getText().length());
                this.setCaretPosition(doc.getLength());
                } catch (Exception e)
                {
                }
            }
        }

    public void appendParagraph(String str)
        {
        StringBuffer newstr = new StringBuffer(str);
        String cont = this.getContentType();
        HTMLEditorKit kit = null;
        HTMLDocument doc = null;
        int tmp_position = 0;

        if (cont.equals("text/plain") || cont.equals("text"))
            {
            tmp_position = this.getDocument().getLength() - 1;
            if (tmp_position <= 0)
                tmp_position = 0;
            this.setCaretPosition(tmp_position);

            this.replaceSelection(str);
            this.setChanged(true);
            tmp_position = this.getDocument().getLength() - 1;
            if (tmp_position <= 0)
                tmp_position = 0;
            this.setCaretPosition(tmp_position);
            }

        if (cont.equals("text/html"))
            {
            kit = ((HTMLEditorKit) this.getEditorKit());
            doc = ((HTMLDocument) this.getDocument());
            try
                {
                // kit.insertHTML(doc, doc.getLength(), "<br>" +
                // newstr.toString() + "", 0, 0, HTML.Tag.BR);
                kit.insertHTML(doc, doc.getLength(), "<p>" + newstr.toString()
                        + "", 1, 0, HTML.Tag.P);
                this.setChanged(true);
                this.setCaretPosition(this.getText().length());
                } catch (Exception e)
                {
                }
            }
        }

    public void appendTable()
        {
        String cont = this.getContentType();
        HTMLEditorKit kit = null;
        HTMLDocument doc = null;
        int tmp_position = 0;

        if (cont.equals("text/plain") || cont.equals("text"))
            {
            tmp_position = this.getDocument().getLength() - 1;
            if (tmp_position <= 0)
                tmp_position = 0;
            this.setCaretPosition(tmp_position);

            this.replaceSelection("\n");
            this.setChanged(true);
            tmp_position = this.getDocument().getLength() - 1;
            if (tmp_position <= 0)
                tmp_position = 0;
            this.setCaretPosition(tmp_position);

            }

        if (cont.equals("text/html"))
            {
            kit = ((HTMLEditorKit) this.getEditorKit());
            doc = ((HTMLDocument) this.getDocument());
            try
                {
                // kit.insertHTML(doc, doc.getLength(), "<br>" +
                // newstr.toString() + "", 0, 0, HTML.Tag.BR);
                kit.insertHTML(doc, doc.getLength(), "<table border='1'>", 0,
                        0, HTML.Tag.TABLE);
                this.setChanged(true);
                this.setCaretPosition(this.getText().length());
                } catch (Exception e)
                {
                }
            }
        }

    public void appendRow()
        {
        String cont = this.getContentType();
        HTMLEditorKit kit = null;
        HTMLDocument doc = null;
        int tmp_position = 0;

        if (cont.equals("text/plain") || cont.equals("text"))
            {
            tmp_position = this.getDocument().getLength() - 1;
            if (tmp_position <= 0)
                tmp_position = 0;
            this.setCaretPosition(tmp_position);

            this.replaceSelection("\n");
            this.setChanged(true);
            tmp_position = this.getDocument().getLength() - 1;
            if (tmp_position <= 0)
                tmp_position = 0;
            this.setCaretPosition(tmp_position);

            }

        if (cont.equals("text/html"))
            {
            kit = ((HTMLEditorKit) this.getEditorKit());
            doc = ((HTMLDocument) this.getDocument());
            try
                {
                kit.insertHTML(doc, doc.getLength(), "<tr>", 1, 0, HTML.Tag.TR);
                this.setChanged(true);
                this.setCaretPosition(this.getText().length());
                } catch (Exception e)
                {
                }
            }
        }

    public void appendCell(String str)
        {
        StringBuffer newstr = new StringBuffer(str);
        String cont = this.getContentType();
        HTMLEditorKit kit = null;
        HTMLDocument doc = null;
        int tmp_position = 0;

        if (cont.equals("text/plain") || cont.equals("text"))
            {
            tmp_position = this.getDocument().getLength() - 1;
            if (tmp_position <= 0)
                tmp_position = 0;
            this.setCaretPosition(tmp_position);

            this.replaceSelection(str + "\t");
            this.setChanged(true);
            tmp_position = this.getDocument().getLength() - 1;
            if (tmp_position <= 0)
                tmp_position = 0;
            this.setCaretPosition(tmp_position);

            }

        if (cont.equals("text/html"))
            {
            kit = ((HTMLEditorKit) this.getEditorKit());
            doc = ((HTMLDocument) this.getDocument());
            try
                {
                kit.insertHTML(doc, doc.getLength(), "<td>" + str, 1, 0,
                        HTML.Tag.TD);
                this.setChanged(true);
                this.setCaretPosition(this.getText().length());
                } catch (Exception e)
                {
                }
            }
        }

    public void appendString(String str)
        {
        StringBuffer newstr = new StringBuffer(str);
        String cont = this.getContentType();
        HTMLEditorKit kit = null;
        HTMLDocument doc = null;
        int tmp_position = 0;

        if (cont.equals("text/plain") || cont.equals("text"))
            {
            tmp_position = this.getDocument().getLength() - 1;
            if (tmp_position <= 0)
                tmp_position = 0;
            this.setCaretPosition(tmp_position);

            this.replaceSelection(str);
            this.setChanged(true);
            tmp_position = this.getDocument().getLength() - 1;
            if (tmp_position <= 0)
                tmp_position = 0;
            this.setCaretPosition(tmp_position);

            }

        if (cont.equals("text/html"))
            {
            kit = ((HTMLEditorKit) this.getEditorKit());
            doc = ((HTMLDocument) this.getDocument());
            try
                {
                kit.insertHTML(doc, doc.getLength(), newstr.toString() + "", 0,
                        0, null);
                this.setChanged(true);
                this.setCaretPosition(this.getText().length());
                } catch (Exception e)
                {
                }
            }
        }

    public void setFont(String fontname, int start, int end)
        {
        String cont = this.getContentType();
        HTMLEditorKit kit = null;
        HTMLDocument doc = null;

        if (cont.equals("text/html"))
            {
            kit = ((HTMLEditorKit) this.getEditorKit());
            doc = ((HTMLDocument) this.getDocument());
            try
                {
                kit.insertHTML(doc, start, "<font face='" + "kuku" + "'>", 0,
                        0, HTML.Tag.FONT);
                kit.insertHTML(doc, end, "</font>", 0, 0, HTML.Tag.FONT);
                } catch (Exception e)
                {
                }
            }
        }

    public void appendBold(String str)
        {
        StringBuffer newstr = new StringBuffer(str);
        String cont = this.getContentType();
        HTMLEditorKit kit = null;
        HTMLDocument doc = null;
        int tmp_position = 0;

        if (cont.equals("text/plain") || cont.equals("text"))
            {
            tmp_position = this.getDocument().getLength() - 1;
            if (tmp_position <= 0)
                tmp_position = 0;
            this.setCaretPosition(tmp_position);

            this.replaceSelection(str);
            this.setChanged(true);
            tmp_position = this.getDocument().getLength() - 1;
            if (tmp_position <= 0)
                tmp_position = 0;
            this.setCaretPosition(tmp_position);

            }

        if (cont.equals("text/html"))
            {
            kit = ((HTMLEditorKit) this.getEditorKit());
            doc = ((HTMLDocument) this.getDocument());
            try
                {
                kit.insertHTML(doc, doc.getLength(), "<p><b>"
                        + newstr.toString() + "</b>", 1, 0, HTML.Tag.P);
                this.setChanged(true);
                this.setCaretPosition(this.getText().length());
                } catch (Exception e)
                {
                }
            }
        }

    // inserts in output an image read from a file
    public void appendExistingImage(String imag_path)
        {
        String cont = this.getContentType();
        HTMLEditorKit kit = null;
        HTMLDocument doc = null;
        if (cont.equals("text/html"))
            {
            kit = ((HTMLEditorKit) this.getEditorKit());
            doc = ((HTMLDocument) this.getDocument());
            try
                {
                kit.insertHTML(doc, doc.getLength(), "<br>", 0, 0, HTML.Tag.BR);
                kit.insertHTML(doc, doc.getLength(), "<IMG SRC='file:"
                        + imag_path + "'>", 0, 0, HTML.Tag.IMG);
                kit.insertHTML(doc, doc.getLength(), "<br>", 0, 0, HTML.Tag.BR);
                kit.insertHTML(doc, doc.getLength(), "<br>", 0, 0, HTML.Tag.BR);
                this.setChanged(true);
                this.setCaretPosition(this.getText().length());
                } catch (Exception e)
                {
                }
            }
        }

    private String makeGoodFileName(String filename)
        {
        String temp = "";

        for (int i = 0; i < 3 - String.valueOf(external_elements).length(); i++)
            {
            temp += "0";
            }
        temp += String.valueOf(external_elements);
        File tmpfile = new File(filename + temp + ".jpg");
        while (tmpfile.exists())
            {
            external_elements++;
            temp = "";
            for (int i = 0; i < 3 - String.valueOf(external_elements).length(); i++)
                {
                temp += "0";
                }
            temp += String.valueOf(external_elements);
            tmpfile = new File(filename + temp + ".jpg");
            }
        return filename + temp + ".jpg";
        }

    // inserts in output a buffered image
    // and saves it with a code name
    public void appendBufferedImage(BufferedImage tmp_img)
        {
        // saving image
        String fs = System.getProperty("file.separator");
        File tmpfile = null;
        try
            {
            tmpfile = new File(MainFrame.getWorkingDirectory());
            } catch (Exception e1)
            {
            tmpfile = new File(".");
            }

        boolean save_now = false;
        String filename = null;
        while (!IOUtils.getExtension(file_name).equals("html")
                && !IOUtils.getExtension(file_name).equals("htm"))
            {
            int confirm = JOptionPane
                    .showOptionDialog(
                            GrNet.gr_frame,
                            "Current output has never been saved as web page.\nYou must save file in HTML format before inserting any image.\nWould you like to save current output now?",
                            "Agna 2 Output Message", JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE, null, null, null);
            if (confirm != 0)
                {
                return;
                } else
                {
                save_now = true;
                // exit method if file wasn't saved
                if (!GrNet.saveOutput())
                    return;
                }
            }
        try
            {
            filename = folder_name
                    + fs
                    + IOUtils.getPathWithoutExtension(MainFrame
                            .getTopFolder(file_name)) + "_img";
            } catch (Exception e)
            {
            }

        filename = makeGoodFileName(filename);
        // creating folder if needed:
        File folder = new File(folder_name);
        if (!folder.exists())
            {
            folder.mkdir();
            }
        external_elements++;
        GrNet.saveImageAsJPG(tmp_img, filename);

        // filename = MainFrame.getTopFolder(filename); // file without folder
        // filename = MainFrame.getTopFolder(folder_name) + fs +filename; //
        // folder + file

        // AgnaLib.AgnaLib();
        String appendstr = AgnaLib.it + AgnaLib.bold + "Visual representation"
                + AgnaLib.unbold + " of " + AgnaLib.unit
                + MainFrame.getCurrentNetwork().getName();
        appendstr += AgnaLib.lb;
        // appendstr += AgnaLib.it + "Source file: " + AgnaLib.unit + filename;
        appendBlock(appendstr);
        appendstr = null;
        // appendString(AgnaLib.it + "Source file: " + AgnaLib.unit + filename);
        filename = filename.replace(fs.charAt(0), '/');
        appendString("");
        appendExistingImage(filename);
        if (save_now)
            {
            MainFrame.doSimplySaveOutput();
            }
        }

    /*
     * private String getContextLineBreak() { String cont =
     * this.getContentType(); if (cont.equals("text/plain") ||
     * cont.equals("text")) { return "\n"; } if (cont.equals("text/html")) {
     * return "<br>"; } return null; }
     */

    // End of class
    }