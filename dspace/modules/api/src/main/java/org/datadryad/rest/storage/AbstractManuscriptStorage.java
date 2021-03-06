/*
 */
package org.datadryad.rest.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.datadryad.rest.models.Manuscript;
import org.datadryad.rest.models.Journal;

/**
 *
 * @author Dan Leehr <dan.leehr@nescent.org>
 */
public abstract class AbstractManuscriptStorage extends AbstractStorage<Manuscript> {

    @Override
    public final void checkCollectionPath(StoragePath path) throws StorageException {
        final List<String> expectedKeyPath = Arrays.asList(Journal.JOURNAL_CODE);
        checkPath(path, expectedKeyPath);

    }

    @Override
    public final void checkObjectPath(StoragePath path) throws StorageException {
        final List<String> expectedKeyPath = Arrays.asList(Journal.JOURNAL_CODE, Manuscript.MANUSCRIPT_ID);
        checkPath(path, expectedKeyPath);
    }

    public List<Manuscript> getManuscriptsMatchingPath(StoragePath path, int limit) throws StorageException {
        return new ArrayList<Manuscript>();
    }
}
