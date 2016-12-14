package com.test.dmt.extractor.filesystem;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.eq;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.castsoftware.dmt.engine.extraction.ExtractionEngine;
import com.castsoftware.dmt.engine.extraction.IExtractor.BrowseEntry;
import com.castsoftware.dmt.engine.extraction.IExtractor.BrowseEntryType;
import com.castsoftware.dmt.engine.foldertree.FolderTreeZIPReader;
import com.castsoftware.dmt.engine.foldertree.IFolderTreeInterpreter;
import com.castsoftware.junit.TmpFolder;
import com.castsoftware.util.logger.exceptions.LogException;

/**
 * Tests of {@link FileSystemExtractor} class
 */
public class FileSystemExtractorTest
{
    /** Temporary directory created per test and clean-up */
    @Rule
    public final TmpFolder tmpRoot = new TmpFolder();

    private File tmpRootFile;

    /**
     * Useless constructor
     */
    public FileSystemExtractorTest()
    {
        // NOP
    }

    /**
     * Setup test class members
     *
     * @throws FileNotFoundException
     *             exception
     */
    @Before
    public void setupTest() throws FileNotFoundException
    {
        tmpRootFile = tmpRoot.getRoot();
    }

    /**
     * Tests of {@link FileSystemExtractor#disconnect(com.castsoftware.dmt.engine.extraction.IExtractor.IConfiguration)} method
     *
     * @throws IOException
     *             - exception
     * @throws LogException
     *             - error log exception
     */
    @Test
    public void testProbeExistingFolder() throws LogException, IOException
    {
        File emptyDir = tmpRoot.newFolder("data");

        FileSystemExtractor extractor = new FileSystemExtractor();

        ExtractionEngine engine = new ExtractionEngine(extractor, "myType", "myPckId", "myPckType",
            emptyDir.getAbsolutePath(), null, null, null, null, tmpRootFile);

        Map<String, ? extends Iterable<BrowseEntry>> result = engine.discover(Arrays.asList(new String[] { "" }), 1);

        com.castsoftware.junit.Assert.assertUnorderedEquals(Arrays.asList(new String[] { "" }),
            result.keySet());
        com.castsoftware.junit.Assert.assertUnorderedEquals(Arrays.asList(new BrowseEntry[] {}),
            result.get(""));
    }

    /**
     * Tests of {@link FileSystemExtractor#disconnect(com.castsoftware.dmt.engine.extraction.IExtractor.IConfiguration)} method
     *
     * @throws FileNotFoundException
     *             - exception
     * @throws LogException
     *             - error log exception
     */
    @Test(expected = LogException.class)
    public void testProbeUnexistingFolder() throws FileNotFoundException, LogException
    {
        FileSystemExtractor extractor = new FileSystemExtractor();

        ExtractionEngine engine = new ExtractionEngine(extractor, "myType", "myPckId", "myPckType", "q:\\nounours",
            null, null, null, null, tmpRootFile);
        engine.discover(Arrays.asList(new String[] { "" }), 1).get("");
    }

    /**
     * Tests of {@link FileSystemExtractor#disconnect(com.castsoftware.dmt.engine.extraction.IExtractor.IConfiguration)} method
     *
     * @throws FileNotFoundException
     *             - exception
     * @throws LogException
     *             - error log exception
     */
    @Test(expected = LogException.class)
    public void testProbeUnexistingNetFolder() throws FileNotFoundException, LogException
    {
        FileSystemExtractor extractor = new FileSystemExtractor();

        ExtractionEngine engine = new ExtractionEngine(extractor, "myType", "myPckId", "myPckType",
            "\\\\nounours\\QualityAssurance\\COMMON", null, null, null, null, tmpRootFile);
        engine.discover(Arrays.asList(new String[] { "" }), 1).get("");
    }

    /**
     * Tests file system extract
     *
     * @throws Exception
     *             exception
     */
    @Test
    public void testExtract1IncludedPath() throws Exception
    {
        File rootPath = new File(tmpRootFile, "pathToExtract");
        tmpRoot.newFile("pathToExtract/dir1/file1.txt", 10);
        tmpRoot.newFile("pathToExtract/dir1/dir2/file2.txt", 5);

        List<String> includedPaths = new ArrayList<String>();
        includedPaths.add("dir1");
        FileSystemExtractor fsExtractor = new FileSystemExtractor();
        File zipFile = new File(tmpRootFile, "extract.zip");

        ExtractionEngine engine = new ExtractionEngine(fsExtractor, "myType", "myPack", "myPckType",
            rootPath.getAbsolutePath(), "", "", includedPaths, null, tmpRootFile);
        engine.extract(zipFile);

        // Check the content of the source package
        IMocksControl mockCtrl = createStrictControl();
        IFolderTreeInterpreter interpreter = mockCtrl.createMock(IFolderTreeInterpreter.class);
        interpreter.init();
        interpreter.open("");
        interpreter.startTree(anyObject(String.class), anyObject(String.class), anyObject(String.class), anyObject(String.class),
            anyObject(String.class));
        interpreter.startRoot(anyObject(String.class), eq("dir1"), anyObject(String.class), eq("myType"));
        interpreter.startDirectory(anyObject(String.class), eq("dir2"), anyObject(String.class));
        interpreter.startFile(anyObject(String.class), eq("file2.txt"), eq(5L), anyObject(String.class));
        interpreter.endFile();
        interpreter.endDirectory();
        interpreter.startFile(anyObject(String.class), eq("file1.txt"), eq(10L), anyObject(String.class));
        interpreter.endFile();
        interpreter.endRoot();
        interpreter.endTree();
        interpreter.close();
        interpreter.done();
        mockCtrl.replay();

        FolderTreeZIPReader reader = new FolderTreeZIPReader(zipFile.getAbsolutePath());
        reader.process(false, interpreter);

        mockCtrl.verify();
    }

    /**
     * Tests file system extract
     *
     * @throws Exception
     *             exception
     */
    @Test
    public void testExtract2IncludedPaths() throws Exception
    {
        File rootPath = new File(tmpRootFile, "pathToExtract");
        tmpRoot.newFile("pathToExtract/dir1/file1.txt", 5);
        tmpRoot.newFile("pathToExtract/dir1/dir2/file2.txt", 10);
        tmpRoot.newFile("pathToExtract/dir3/file3.txt", 15);
        tmpRoot.newFile("pathToExtract/dir3/dir4/file4.txt", 20);

        List<String> includedPaths = new ArrayList<String>();
        includedPaths.add("dir1");
        includedPaths.add("dir3");
        FileSystemExtractor fsExtractor = new FileSystemExtractor();
        File zipFile = new File(tmpRootFile, "extract.zip");

        ExtractionEngine engine = new ExtractionEngine(fsExtractor, "myType", "myPack", "myPckType",
            rootPath.getAbsolutePath(), "", "", includedPaths, null, tmpRootFile);
        engine.extract(zipFile);

        // Check the content of the source package
        IMocksControl mockCtrl = createStrictControl();
        IFolderTreeInterpreter interpreter = mockCtrl.createMock(IFolderTreeInterpreter.class);
        interpreter.init();
        interpreter.open("");
        interpreter.startTree(anyObject(String.class), anyObject(String.class), anyObject(String.class), anyObject(String.class),
            anyObject(String.class));
        interpreter.startRoot(anyObject(String.class), eq("dir1"), anyObject(String.class), eq("myType"));
        interpreter.startDirectory(anyObject(String.class), eq("dir2"), anyObject(String.class));
        interpreter.startFile(anyObject(String.class), eq("file2.txt"), eq(10L), anyObject(String.class));
        interpreter.endFile();
        interpreter.endDirectory();
        interpreter.startFile(anyObject(String.class), eq("file1.txt"), eq(5L), anyObject(String.class));
        interpreter.endFile();
        interpreter.endRoot();
        interpreter.startRoot(anyObject(String.class), eq("dir3"), anyObject(String.class), eq("myType"));
        interpreter.startDirectory(anyObject(String.class), eq("dir4"), anyObject(String.class));
        interpreter.startFile(anyObject(String.class), eq("file4.txt"), eq(20L), anyObject(String.class));
        interpreter.endFile();
        interpreter.endDirectory();
        interpreter.startFile(anyObject(String.class), eq("file3.txt"), eq(15L), anyObject(String.class));
        interpreter.endFile();
        interpreter.endRoot();
        interpreter.endTree();
        interpreter.close();
        interpreter.done();
        mockCtrl.replay();

        FolderTreeZIPReader reader = new FolderTreeZIPReader(zipFile.getAbsolutePath());
        reader.process(false, interpreter);

        mockCtrl.verify();
    }

    /**
     * Tests file system extract
     *
     * @throws Exception
     *             exception
     */
    @Test
    public void testExtractWithoutIncludedPath() throws Exception
    {
        File rootPath = new File(tmpRootFile, "pathToExtract");
        tmpRoot.newFile("pathToExtract/dir1/file1.txt", 5);
        tmpRoot.newFile("pathToExtract/dir1/dir2/file2.txt", 10);

        FileSystemExtractor fsExtractor = new FileSystemExtractor();
        File zipFile = new File(tmpRootFile, "extract.zip");

        ExtractionEngine engine = new ExtractionEngine(fsExtractor, "myType", "myPack", "myPckType",
            rootPath.getAbsolutePath(), "", "", null, null, tmpRootFile);
        engine.extract(zipFile);

        // Check the content of the source package
        IMocksControl mockCtrl = createStrictControl();
        IFolderTreeInterpreter interpreter = mockCtrl.createMock(IFolderTreeInterpreter.class);
        interpreter.init();
        interpreter.open("");
        interpreter.startTree(anyObject(String.class), anyObject(String.class), anyObject(String.class), anyObject(String.class),
            anyObject(String.class));
        interpreter.startRoot(anyObject(String.class), eq(""), anyObject(String.class), eq("myType"));
        interpreter.startDirectory(anyObject(String.class), eq("dir1"), anyObject(String.class));
        interpreter.startDirectory(anyObject(String.class), eq("dir2"), anyObject(String.class));
        interpreter.startFile(anyObject(String.class), eq("file2.txt"), eq(10L), anyObject(String.class));
        interpreter.endFile();
        interpreter.endDirectory();
        interpreter.startFile(anyObject(String.class), eq("file1.txt"), eq(5L), anyObject(String.class));
        interpreter.endFile();
        interpreter.endDirectory();
        interpreter.endRoot();
        interpreter.endTree();
        interpreter.close();
        interpreter.done();
        mockCtrl.replay();

        FolderTreeZIPReader reader = new FolderTreeZIPReader(zipFile.getAbsolutePath());
        reader.process(false, interpreter);

        mockCtrl.verify();
    }

}
