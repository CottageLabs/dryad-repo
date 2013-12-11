package org.dspace.curate;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.DCDate;
import org.dspace.content.DCValue;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.embargo.EmbargoManager;
import org.dspace.handle.HandleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Curation task to report embargoed items without publication date
 * 
 * @author pmidford
 * created Dec 9, 2011
 *
 */

@Suspendable
public class EmbargoedWithoutPubDate extends AbstractCurationTask {


    private int total;
    private int unpublishedCount;
    private List<DatedEmbargo> embargoes;
    private DatedEmbargo[] dummy = new DatedEmbargo[1];
    private Context thisContext = null;
    private DocumentBuilderFactory dbf = null;
    private DocumentBuilder docb = null;

    private Context dspaceContext;
    private static Logger LOGGER = LoggerFactory.getLogger(EmbargoedWithoutPubDate.class);


    @Override
    public void init(Curator curator, String taskID) throws IOException{
        super.init(curator, taskID);
        // init dspace context to allow access to database
    }

    @Override
    public int perform(DSpaceObject dso) throws IOException {
        if (dso instanceof Collection){
            total = 0;
            unpublishedCount = 0;
            embargoes = new ArrayList<DatedEmbargo>();
            distribute(dso);
            if (!embargoes.isEmpty()){
                DatedEmbargo[] s = embargoes.toArray(dummy);
                Arrays.sort(s);
                this.report("Collection: " + dso.getName() + "; Total items = " + total + "; unpublished items = " + unpublishedCount); 
                for(DatedEmbargo d : s)
                    this.report(d.toString());
            }
            else if (total > 0)
                this.report("Collection: " + dso.getName() + "; Total items = " + total + "; no unpublished items"); 
        }
        return Curator.CURATE_SUCCESS;
    }

    @Override
    protected void performItem(Item item){
        final String handle = item.getHandle();
        DCValue partof[] = item.getMetadata("dc.relation.ispartof");
        if (handle != null)  //ignore items in workflow
            if (partof != null && partof.length==1){  //and articles
                total++;
                //first find the article this data is associated with
                DCValue partofArticle = partof[0];
                String articleID = partofArticle.value;  //most likely a handle, but probably ought to be a doi, so try looking both ways
                String shortHandle = "";
                if(articleID.startsWith("http://hdl.handle.net/10255/")) {   //modified from the DataPackageStats tool
                    shortHandle = articleID.substring("http://hdl.handle.net/".length());
                } else if (articleID.startsWith("http://datadryad.org/handle/")) {
                    shortHandle = articleID.substring("http://datadryad.org/handle/".length());
                } else if (articleID.startsWith("doi:10.5061/")) {
                    try{
                        URL doiLookupURL = new URL("http://datadryad.org/doi?lookup=" + articleID);
                        String doiLookup = (new BufferedReader(new InputStreamReader(doiLookupURL.openStream()))).readLine();
                        shortHandle = doiLookup.substring("http://datadryad.org/handle/".length());
                    }
                    catch(Exception e){
                        this.report("Exception encountered while looking handle for " + articleID + " found in " + item.getName() + " (" + item.getHandle() + ")");
                        LOGGER.error("Exception was " + e);
                    }
                } else {
                    this.report("Bad partof ID: " + articleID + " found in " + item.getName() + " (" + item.getHandle() + ")");
                }
                Item parentItem = null;
                try{
                    dspaceContext = new Context();
                    parentItem = (Item)HandleManager.resolveToObject(dspaceContext, shortHandle);
                    if (parentItem != null){
                        DCValue[] citationValues = parentItem.getDC("identifier", "citation", null);
                        DCDate itemEmbargoDate = null;
                        if (citationValues == null || citationValues.length == 0){  //no citation; treat as unpublished
                            unpublishedCount++;
                            itemEmbargoDate = EmbargoManager.getEmbargoDate(null, item);
                            if (itemEmbargoDate != null){
                                DatedEmbargo de = new DatedEmbargo(itemEmbargoDate.toDate(),item);
                                embargoes.add(de);
                            }
                        }
                    }
                } catch (SQLException e){
                    LOGGER.error("Unable to get DSpace Item for " + shortHandle);
                    LOGGER.error("Exception was " + e);
                } catch (AuthorizeException e) {
                    LOGGER.error("EmbargoManager unable to authorize access to embargo data for this item");
                } catch (IOException e) {
                    LOGGER.error("EmbargoManager generated an IOException when accessing embargo data for this item");
                } finally {
                    if(dspaceContext != null)
                    {
                        dspaceContext.abort();
                    }
                }
                        
            }
    }

    private static class DatedEmbargo implements Comparable<DatedEmbargo>{

        private Date embargoDate;
        private Item embargoedItem;

        public DatedEmbargo(Date date, Item item) {
            embargoDate = date;
            embargoedItem = item;
        }

        @Override
        public int compareTo(DatedEmbargo o) {
            return embargoDate.compareTo(o.embargoDate);
        }

        @Override
        public String toString(){
            return embargoedItem.getName() + " " + embargoDate.toString();
        }
    }
}
