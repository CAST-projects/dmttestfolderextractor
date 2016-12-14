package com.test.dmt.extractor.filesystem;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.castsoftware.dmt.engine.extraction.AbstractBlankInitialRootExtractor;
import com.castsoftware.util.logger.Logging;
import com.castsoftware.util.logger.exceptions.LogException;

/**
 * The {@link FileSystemExtractor} class is the class responsible for extracting sources from file system. <BR>
 */
public class FileSystemExtractor extends AbstractBlankInitialRootExtractor
{

	/**
	 * Folder extractor constructor
	 */
    public FileSystemExtractor()
    {
        // NOP
    }

    @Override
    public void connect(String url, String user, String pswd, IConfiguration configuration) throws LogException
    {
        File rootFile = new File(url);
        if (!rootFile.isAbsolute())
            throw Logging.error("test.dmt.extractor.filesystem.extractionURLNotAbsoluteFailure", "FILE", rootFile);
        if (!rootFile.exists())
            throw Logging.error("test.dmt.extractor.filesystem.extractionURLNotFoundFailure", "FILE", rootFile);
    }

    @Override
    public void disconnect(IConfiguration configuration) throws LogException
    {
        // Nothing
    }

    @Override
    public void getChildren(Map<String, Iterable<BrowseEntry>> pathsChildren, IConfiguration configuration) throws LogException
    {
        File rootFile = new File(configuration.getURL());

        for (Entry<String, Iterable<BrowseEntry>> entry : pathsChildren.entrySet())
        {
            File dir = new File(rootFile, entry.getKey());
            if (!dir.isDirectory())
            {
                continue;
            }

            File[] subDirs = dir.listFiles(new FileFilter()
            {
                @Override
                public boolean accept(File file)
                {
                    return file.isDirectory();
                }
            });
            if (subDirs == null)
            {
                Logging.warn("test.dmt.extractor.filesystem.directoryListingFailure", "DIR", dir);
                continue;
            }

            List<BrowseEntry> list = new ArrayList<BrowseEntry>();
            entry.setValue(list);
            for (File file : subDirs)
            {
                list.add(BrowseEntry.create(file.getName(), BrowseEntryType.NODE));
            }
        }
    }

    @Override
    public void extract(List<? extends ISourceRoot> initialRoots, IRootFactory rootFactory, IConfiguration configuration)
        throws LogException
    {
        File globalRootFile = new File(configuration.getURL());

        for (ISourceRoot root : initialRoots)
        {
            File rootFile = new File(globalRootFile, root.getPath());

            if (rootFile.isDirectory())
                root.addDirectoryOrFile(null, rootFile);
            else
                root.addDirectoryOrFile(rootFile.getName(), rootFile);
        }
    }

}
