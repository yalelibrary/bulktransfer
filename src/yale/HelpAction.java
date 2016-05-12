package yale;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

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
        JOptionPane.showMessageDialog(null, "Instructions (Mac): \n\n sudo mount -t smbfs //DOMAIN\\;netid@url/share dir \n sudo java -cp . yale.FileCopy \n\n" +
                "Instructions (Linux -- Cent OS) \n sudo /sbin/mount.cifs //url/share dir -o user=netid, domain=");
    }
}
