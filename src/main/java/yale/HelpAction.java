package yale;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Help menu helper
 */
class HelpAction implements Action {

    @Override
    public Object getValue(String key) {
        return null;
    }

    @Override
    public void putValue(String key, Object value) {

    }

    @Override
    public void setEnabled(boolean b) {

    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {

    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        /*JOptionPane.showMessageDialog(null, "Instructions (Mac): \n\n sudo mount -t smbfs //DOMAIN\\;netid@url/share dir \n sudo java -cp . yale.FileCopy \n\n" +
                "Instructions (Linux -- Cent OS) \n sudo /sbin/mount.cifs //url/share dir -o user=netid, domain="); */
        JOptionPane.showMessageDialog(null, getText());
    }

    private String getText() {
        final StringBuffer sb = new StringBuffer("Please consult project documentation.");
        try {
            final URL oracle = getClass().getResource("file:///instructions.txt");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(oracle.openStream()));


            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
                sb.append(System.getProperty("line.separator"));
            }

            in.close();
        } catch (Exception e) {
            // ignore it
        }

        return sb.toString();
    }


}
