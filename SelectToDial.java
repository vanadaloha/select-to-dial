/*
 * Copyright (c) 2017 by Walter Stroebel and InfComTec.
 * Commissioned by VANAD Group.
 * Released in the public domain as open source under the Apache license 2.0.
 */

import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author walter
 */
public class SelectToDial {

    /**
     * We need clipboard access to be final, this wrapper serves that purpose.
     *
     */
    static class Holder {

        Clipboard clipboard;
    }
    /**
     * We need clipboard access to be final, this wrapper serves that purpose.
     *
     */
    static final Holder holder = new Holder();
    /**
     * Whether to use any selection or only copied selections, a user preference.
     *
     */
    static boolean copyMode = true;

    /**
     * Create the tray icon and set up the menus. Then loop until terminated, waiting for numbers to dial.
     *
     * @param args Ignored, there are no command line arguments.
     */
    public static void main(String[] args) {

        try {
            if (!SystemTray.isSupported()) {
                System.out.println("SystemTray is not supported");
                return;
            }
            final Toolkit toolkit = Toolkit.getDefaultToolkit();
            final PopupMenu popup = new PopupMenu();
            final BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
            image.getGraphics().clearRect(0, 0, 32, 32);
            image.getGraphics().setColor(Color.red);
            image.getGraphics().drawString("VD", 4, 20);
            final TrayIcon trayIcon = new TrayIcon(image, "SelectToDial", popup);
            final SystemTray tray = SystemTray.getSystemTray();
            MenuItem config = new MenuItem("Config");
            MenuItem exit = new MenuItem("Exit");
            final CheckboxMenuItem copyToDial = new CheckboxMenuItem("Copy to dial", copyMode);
            popup.add(config);
            popup.add(copyToDial);
            popup.add(exit);
            holder.clipboard = toolkit.getSystemClipboard();
            config.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JOptionPane.showMessageDialog(null, "This would be a configuration dialog for API key, user, password");
                }
            });
            exit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
            copyToDial.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    copyMode = !copyMode;
                    if (copyMode) {
                        holder.clipboard = toolkit.getSystemClipboard();
                        System.out.println("Copy mode");
                    } else {
                        holder.clipboard = toolkit.getSystemSelection();
                        System.out.println("Select mode");
                    }
                    copyToDial.setState(copyMode);

                }
            });
            tray.add(trayIcon);
            String result = "";
            try {
                result = (String) holder.clipboard.getData(DataFlavor.stringFlavor);
            } catch (Exception ignore) {
                // we might not be able to get a string from the clipboard but we don't care why this is
            }
            // System.out.println("String from Clipboard: [" + result + ']');
            showDial(result);
            while (true) {
                Thread.sleep(500);
                try {
                    String newClip = (String) holder.clipboard.getData(DataFlavor.stringFlavor);
                    if (!newClip.equals(result)) {
                        result = newClip;
                        System.out.println("String from Clipboard: [" + result + ']');
                        showDial(result);
                    }
                } catch (Exception ignore) {
                    // we might not be able to get a string from the clipboard but we don't care why this is
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(SelectToDial.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * If the latest selection is a valid phone number, ask the user if we should dial it.
     *
     * @param possiblePhoneNumber The string from the clipboard that might be a phone number.
     * @throws HeadlessException This should not happen, if it does, terminate the program.
     */
    public static void showDial(String possiblePhoneNumber) throws HeadlessException {
        StringBuilder sb = new StringBuilder();
        for (char c : possiblePhoneNumber.toCharArray()) {
            if (Character.isDigit(c)) {
                sb.append(c);
            } else if (c != '(' && c != ')' && c != '+' && c != ' ' && c != '-' && c != '.') {
                return;
            }
        }
        int yes = JOptionPane.NO_OPTION;
        if (sb.length() == 10 && sb.charAt(0) == '0') {
            sb.delete(0, 1);
            sb.insert(0, "31");
            yes = JOptionPane.showConfirmDialog(null, "Dial: " + sb.toString());
        } else if (sb.length() == 11 && sb.charAt(0) == '3' && sb.charAt(1) == '1') {
            yes = JOptionPane.showConfirmDialog(null, "Dial: " + sb.toString());
        } else {
            System.out.println("NaN: " + sb.toString());
        }
        if (yes == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(null, "Calling URL with number set to " + sb.toString());
        }
    }

}
