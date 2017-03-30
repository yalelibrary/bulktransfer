# File Transfer Utility

Swing application aka TiffFinder to allow file copy from remote shares.

# Dependency:

The app relies on an indexing/crawling service running on smdr01. The service repo is:

https://github.com/yalelibrary/fileservice

See instructions there on how to build and deploy the project.

## Mac Installation/Run Instructions

1. Open Finder / select "Go" / select "Connect to Server...".
2. Enter server address: "smb://storage.yale.edu/home/fc_Beinecke_807001-YUL".This will mount the share. Make sure
that you can see the folder you want to copy in Finder. Sometimes, Finder freezes due a bug and this would prevent the application from seeing the folder in the first place.
If you experience this, just un-mount/disconnect the share by clicking on the eject icon,
and try a minute or so later.
3. (Now start the program by clicking on the jar file or by typing in Terminal "java -jar bulk.jar"). Click on "Browse Source Folder".
4. Browse to "Volumes" (typically under "Macintosh HD") and select the relevant directory within the share (typically labelled 'home').
5. Select the target folder.
6. Select Overwrite if you want your target files to be replaced.
7. Specify folders of interest in the identifiers box.
8. Once you click "Copy", the Info box and progress bar will update.

When done, unmount the share via Finder.

## Packaging Instructions

Create the jar file by using the manifest file Mainfest.txt:

```
jar cfm filetransferutility.jar Manifest.txt yale/*.class
```

You must be in production/out directory for this to happen. Run javac it if the directory does not exist.

## Author

Osman Din
