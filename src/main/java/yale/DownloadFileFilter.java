package yale;

import java.io.File;
import java.util.List;

public class DownloadFileFilter {

    public static boolean download(final File f, List<String> identifiers) {

        final String fullFileName = f.getName();

        // remove the file extension

        int count = fullFileName.length() - fullFileName.replace(".", "").length();

        if (count > 1) {
            //detailsBox.append("Unexpected file name:" + f.getAbsolutePath() + "\n");
            return false;
        }


        final String fileName = fullFileName.substring(0, fullFileName.lastIndexOf('.'));

        // System.out.println("Will evaluate:" + fileName);

        for (final String s : identifiers) {

            if (s.equals(fileName)) {
                return true;
            }

            if (s.contains("-")) {
                final String[] sp = s.split("-");

                if (sp.length == 2) {

                    //strip digits

                    final String fileA = sp[0].replaceAll("[^\\d.]", "");

                    if (!fileA.matches("[0-9]+")) {
                        continue;
                    }


                    final int fileANum = Integer.parseInt(fileA);
                    final String fileB = sp[1].replaceAll("[^\\d.]", "");

                    if (!fileB.matches("[0-9]+")) {
                        continue;
                    }

                    final int fileBNum = Integer.parseInt(fileB);
                    final String prefixA = sp[0].replace(sp[0], "");
                    final String prefixB = sp[1].replace(sp[1], "");

                    if (prefixA.equals(prefixB) && fileName.startsWith(prefixA)) {
                        final String fi = fileName.replaceAll("[^\\d.]", "");

                        if (!fi.isEmpty()) {
                            if (!fi.matches("[0-9]+")) {
                                continue;
                            }

                            final int fileNum = Integer.parseInt(fi);

                            if (fileNum >= fileANum && fileNum <= fileBNum) {
                                return true;
                            }
                        }
                    }
                } // end of sp.length == 2
            }
        }

        return false;
    }
}
