package com.bentza.sna;

import com.bentza.sna.gui.MainFrame;
import javax.swing.JOptionPane;
import java.awt.Cursor;

        public class AgnaThread // extends Thread
// Thread object to be used in thread-specific methods
// used in conjunction with MainFrame and ProgressDialog
// Subsequently modified because of thread-unsafe risk!
    {

    public void decorate(String new_status)
    // modifies the main frame (status bar, mouse cursor)
        {
        MainFrame.getCurrentFrame().setCursor(
                Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (new_status != null)
            MainFrame.setCurrentStatus(new_status);
        }

    // sets the main frame to its default state
    public void undecorate()
        {
        MainFrame.progress_dialog.setPercent(-1);
        MainFrame.getCurrentFrame().setCursor(
                Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        MainFrame.setCurrentStatus(MainFrame.default_status);
        }

    public void run()
        {
        }

   
        public void go()
        {
        if (!MainFrame.progress_dialog.getStart())
            return; // most probably there is another thread running
        MainFrame.progress_dialog.startPane(Environment.getApplicationFullName()
                + " process.", "");
        // this.start();
        try
            {
            this.run();
            } catch (Exception thread_failure)
            {
            // 2.1.3: an exception inside an operation used to kill the
            // wait-state silently, leaving "no output" with no message;
            // surface it and always restore the UI state
            AgnaLog.error("operation failed", thread_failure);
            JOptionPane.showMessageDialog(MainFrame.getCurrentFrame(),
                    "Agna operation failed:\n" + thread_failure
                            + "\nSee the console for details.", "Agna",
                    JOptionPane.ERROR_MESSAGE);
            } finally
            {
            this.undecorate();
            }
        }

    public void finish() // called inside thread before the end of run()
        {
        this.undecorate();
        }

    public void nap()
        {
        /*
         * try { Thread.sleep(250); } catch(java.lang.InterruptedException e) {}
         */
        }

    }