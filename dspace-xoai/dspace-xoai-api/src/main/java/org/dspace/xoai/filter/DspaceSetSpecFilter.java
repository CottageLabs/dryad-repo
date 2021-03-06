/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.xoai.filter;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;

import java.sql.SQLException;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.handle.HandleManager;
import org.dspace.xoai.data.DSpaceDatabaseItem;
import org.dspace.xoai.util.XOAIDatabaseManager;

/**
 * 
 * @author Lyncode Development Team <dspace@lyncode.com>
 */
public class DspaceSetSpecFilter extends DSpaceFilter
{
    private static Logger log = LogManager.getLogger(DspaceSetSpecFilter.class);

    private String _setSpec;

    public DspaceSetSpecFilter(String spec)
    {
        _setSpec = spec;
    }

    private static final String pfxCom = ConfigurationManager.getProperty("xoai", "set.community.prefix");
    private static final String pfxCol = ConfigurationManager.getProperty("xoai", "set.collection.prefix");

    @Override
    public DatabaseFilterResult getWhere(Context context)
    {
        if (_setSpec.startsWith(pfxCol))
        {
            try
            {
                DSpaceObject dso = HandleManager.resolveToObject(context,
                        _setSpec.replace(pfxCol, ""));
                return new DatabaseFilterResult(
                        "EXISTS (SELECT tmp.* FROM collection2item tmp WHERE tmp.item_id=i.item_id AND collection_id = ?)",
                        dso.getID());
            }
            catch (Exception ex)
            {
                log.error(ex.getMessage(), ex);
            }
        }
        else if (_setSpec.startsWith(pfxCom))
        {
            try
            {
                DSpaceObject dso = HandleManager.resolveToObject(context,
                        _setSpec.replace(pfxCom, ""));
                List<Integer> list = XOAIDatabaseManager.getAllSubCollections(
                        context, dso.getID());
                String subCollections = StringUtils.join(list.iterator(), ",");
                return new DatabaseFilterResult(
                        "EXISTS (SELECT tmp.* FROM collection2item tmp WHERE tmp.item_id=i.item_id AND collection_id IN ("
                                + subCollections + "))");
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
        return new DatabaseFilterResult();
    }

    @Override
    public boolean isShown(DSpaceDatabaseItem item)
    {
        try
        {
            Item dsitem = item.getItem();
            if (_setSpec.startsWith(pfxCol))
            {
                String handle = _setSpec.replace(pfxCol, "");
                for (Collection c : dsitem.getCollections())
                    if (c.getHandle().replace('/', '_').equals(handle))
                        return true;
                return false;
            }
            else if (_setSpec.startsWith(pfxCom))
            {
                String handle = _setSpec.replace(pfxCom, "");
                for (Community c : XOAIDatabaseManager
                        .flatParentCommunities(dsitem))
                    if (c.getHandle().replace('/', '_').equals(handle))
                        return true;
                return false;
            }
        }
        catch (SQLException ex)
        {
            log.error(ex.getMessage(), ex);
        }

        return false;
    }

    @Override
    public SolrFilterResult getQuery()
    {
        if (_setSpec.startsWith(pfxCol))
        {
            try
            {
                return new SolrFilterResult("item.collections:"
                        + ClientUtils.escapeQueryChars(_setSpec));
            }
            catch (Exception ex)
            {
                log.error(ex.getMessage(), ex);
            }
        }
        else if (_setSpec.startsWith(pfxCom))
        {
            try
            {
                return new SolrFilterResult("item.communities:"
                        + ClientUtils.escapeQueryChars(_setSpec));
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
        return new SolrFilterResult();
    }

}
