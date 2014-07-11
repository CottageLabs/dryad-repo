/*
 */
package org.datadryad.journalstatistics.extractor;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Set;
import org.apache.log4j.Logger;
import org.datadryad.api.DryadDataPackage;
import org.datadryad.api.DryadObject;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.ItemIterator;
import org.dspace.core.Context;

/**
 * Counts items in a DSpace Collection, such as Dryad Data Packages or files
 * @author Dan Leehr <dan.leehr@nescent.org>
 */
public abstract class DataItemCount extends DatabaseExtractor<Integer> {

    private static Logger log = Logger.getLogger(DataItemCount.class);

    public DataItemCount(Context context) {
        super(context);
    }

    protected abstract Collection getCollection() throws SQLException;
    protected abstract DryadObject makeDryadObject(Item item);

    @Override
    public Integer extract(final String journalName) {
        Context context = this.getContext();
        Collection collection;
        Integer count = 0;
        try {
            /*
             * Initial implementation is to fetch items by metadata field ID
             * then filter on collection. This may be too slow since it will
             * result in a lot of queries and could be done with a single join
             *
             * Note: findByMetadataField only returns items in archive
             */

            // Only data packages have publicationName metadata
            ItemIterator itemsByJournal = Item.findByMetadataField(this.getContext(), JOURNAL_SCHEMA, JOURNAL_ELEMENT, JOURNAL_QUALIFIER, journalName);

            collection = getCollection();
            Collection dataPackageCollection = DryadDataPackage.getCollection(context);
            while (itemsByJournal.hasNext()) {
                Item packageItem = itemsByJournal.next();
                // Skip if the item is not a data package
                if(!packageItem.isOwningCollection(dataPackageCollection)) {
                    continue;
                }
                DryadDataPackage dataPackage = new DryadDataPackage(packageItem);
                Set<DryadObject> objectsToConsider = dataPackage.getRelatedObjectsInCollection(context, collection);
                for(DryadObject dryadObject : objectsToConsider) {
                    Item item = dryadObject.getItem();
                    if(item.isOwningCollection(collection)) {
                        if(passesDateFilter(dryadObject.getDateAccessioned())) {
                            count++;
                        }
                    }
                }
            }
        } catch (AuthorizeException ex) {
            log.error("AuthorizeException getting item count per journal", ex);
        } catch (IOException ex) {
            log.error("IOException getting item count per journal", ex);
        } catch (SQLException ex) {
            log.error("SQLException getting item count per journal", ex);
        }
        return count;
    }

}
