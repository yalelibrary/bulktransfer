package yale;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileCopyTest {

    @Test
    public void test() {
        final FileCopy f = new FileCopy();
        // path given by service (e.g., on win)
        final String servicePath = "\\storage.yale.edu\\home\\fc_Beinecke-8017001-YUL\\DL_images\\images_derivative_tiff\\rip\\1.tiff";
        // path specified by client (e.g., on a mac)
        final String clientSourcePath = "/Volumes/fc_Beinecke-8017001-YUL/DL_images/images_derivative_tiff";
        //desired path on mac(a combination of service and client path):
        final String desired = "/Volumes/fc_Beinecke-8017001-YUL/DL_images/images_derivative_tiff/rip/1.tiff";
        final String converted = f.convertSharePath(FilenameUtils.separatorsToUnix(servicePath), clientSourcePath, f.getLast(clientSourcePath));
        assertTrue (converted.equals(desired));
    }

    @Test
    public void test2() {
        final FileCopy f2 = new FileCopy();
        // path given by service (e.g., on win)
        final String servicePath = "\\storage.yale.edu\\home\\fc_Beinecke-8017001-YUL\\DL_images\\images_derivative_tiff\\rip\\1.tiff";
        // path specified by client (e.g., on a mac)
        final String clientSourcePath = "/Volumes/home/fc_Beinecke-8017001-YUL/DL_images/images_derivative_tiff";
        //desired path on mac(a combination of service and client path):
        final String desired = "/Volumes/home/fc_Beinecke-8017001-YUL/DL_images/images_derivative_tiff/rip/1.tiff";
        final String converted = f2.convertSharePath(FilenameUtils.separatorsToUnix(servicePath), clientSourcePath, f2.getLast(clientSourcePath));
        assertTrue (converted.equals(desired));
    }

    @Test
    public void test3() {
        final FileCopy f2 = new FileCopy();
        // path given by service (e.g., on win)
        final String servicePath = "\\storage.yale.edu\\home\\fc_Beinecke-8017001-YUL\\DL_images\\images_derivative_tiff\\rip\\1.tiff";
        // path specified by client (e.g., on a mac)
        final String clientSourcePath = "/Volumes/DL_images/images_derivative_tiff";
        //desired path on mac(a combination of service and client path):
        final String desired = "/Volumes/DL_images/images_derivative_tiff/rip/1.tiff";
        final String converted = f2.convertSharePath(FilenameUtils.separatorsToUnix(servicePath), clientSourcePath, f2.getLast(clientSourcePath));
        assertTrue (converted.equals(desired));
    }

    @Test
    public void testLastFolder() {
        final FileCopy f3 = new FileCopy();
        final String servicePath = "storage.yale.edu/home/images";
        assertEquals(f3.getLast(servicePath), "images");
    }

    @Test
    public void testLastFolderWin() {
        final FileCopy f3 = new FileCopy();
        final String servicePath = "\\storage.yale.edu\\home\\images";
        assertEquals(f3.getLast(servicePath), "images");
    }

    @Test
    public void testOnpath() {
        final FileCopy f3 = new FileCopy();
        final String servicePath = "\\storage.yale.edu\\home\\images\\1.tif";
        assertTrue(f3.foundOnPath(servicePath, "images"));
    }
}
