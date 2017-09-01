/*
 * Copyright (c) 2017 by Walter Stroebel and InfComTec.
 * Commissioned by VANAD Group.
 * Released in the public domain as open source under the Apache license 2.0.
 */

import java.awt.Color;
import java.awt.Graphics;
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
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
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
     * The URL to Aloha
     */
    static String urlBase = "";
    static String keyAPI = "";

    static void usage() {
        JOptionPane.showMessageDialog(null, "Usage: java SelectToDial url-to-Aloha api-key [select]");
        System.exit(0);
    }

    /**
     * Create the tray icon and set up the menus. Then loop until terminated, waiting for numbers to dial.
     *
     * @param args There are two required arguments and one optional argument.
     * <ol>
     * <li>The URL to your instance of Aloha. This should be something like
     * 'https://api.REGIONX.vanadaloha.net/v2/telephony/clicktodial'.</li>
     * <li>The API key of the user. This will look like 'ba4d7f5724419878afc46a04022ed7d869f78c51'.</li>
     * <li>Optionally the word 'select'. By default, this utility will only pick up items copied to the clipboard. With this
     * option, any selection that is a valid phone number will pop up the "Dial this number" box. Most users will find this too
     * intrusive.</li>
     * </ol>
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            String u = args[0];
            if (!u.endsWith("/")) {
                u += "/";
            }
            urlBase = u;
        } else {
            usage();
        }
        if (args.length > 1) {
            keyAPI = args[1];
        } else {
            usage();
        }
        if (args.length > 2) {
            copyMode = !"select".equalsIgnoreCase(args[2]);
        }
        try {
            if (!SystemTray.isSupported()) {
                System.out.println("SystemTray is not supported");
                return;
            }
            final Toolkit toolkit = Toolkit.getDefaultToolkit();
            final PopupMenu popup = new PopupMenu();
            final BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
            {
                Graphics g = image.getGraphics();
                g.clearRect(0, 0, 32, 32);
                g.setColor(Color.red);
                g.drawString("VD", 4, 17);
            }
            final TrayIcon trayIcon = new TrayIcon(image, "SelectToDial", popup);
            final SystemTray tray = SystemTray.getSystemTray();
            MenuItem exit = new MenuItem("Exit");
            popup.add(exit);
            if (copyMode) {
                holder.clipboard = toolkit.getSystemClipboard();
            } else {
                holder.clipboard = toolkit.getSystemSelection();
            }
            exit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
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
                        //System.out.println("String from Clipboard: [" + result + ']');
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
     * If the latest selection is a valid phone number, ask the user if we should dial it. Note that this method is hard-coded to
     * only accept and/or reformat Dutch phone numbers.
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
        }
        if (yes == JOptionPane.YES_OPTION) {
            try {
                final URL url = new URL(urlBase + sb.toString() + ".json?apikey=" + keyAPI);
                new Thread() {
                    @Override
                    public void run() {
                        System.out.println(url);
                        try (InputStream consumer = url.openStream()) {
                            for (int ch = consumer.read(); ch >= 0; ch = consumer.read()) {
                                System.out.write(ch);
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null, "Failed to call using Aloha: " + ex.getLocalizedMessage());
                        }
                    }
                }.start();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Failed to call using Aloha: " + ex.getLocalizedMessage());
            }
        }
    }

}
