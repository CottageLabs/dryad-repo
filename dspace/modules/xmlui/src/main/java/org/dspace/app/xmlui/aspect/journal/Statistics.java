/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dspace.app.xmlui.aspect.journal;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.Para;
import org.dspace.authorize.AuthorizeException;
import org.xml.sax.SAXException;

/**
 *
 * @author rnathanday
 */
public class Statistics extends AbstractDSpaceTransformer {
    
    // TODO: localize
    
    @Override
    public void addBody(Body body) throws SAXException, WingException,
            UIException, SQLException, IOException, AuthorizeException
    {
        
        Request request = ObjectModelHelper.getRequest(this.objectModel);
        String journalAbbr = request.getContextPath();

        // redirect to /journal if not found
        Division div = body.addDivision("journal-landing");
        div.addPara().addContent("getSitemapPath "  + request.getSitemapPath());
        div.addPara().addContent("getSitemapURI "   + request.getSitemapURI());
        div.addPara().addContent("getSitemapURIPrefix " + request.getSitemapURIPrefix());
        div.addPara().addContent("getRequestURI "   + request.getRequestURI());        
        div.addPara().addContent("getContextPath "  + request.getContextPath());        
        div.addPara().addContent("getQueryString "  + request.getQueryString());
        div.addPara().addContent("getPathInfo "     + request.getPathInfo());
        div.addPara().addContent("getServletPath "  + request.getServletPath());
                
        // ------------------
        // Journal X
        // 1 sentence scope
        // Publisher:
        // Society: 
        // Editorial review:
        // ------------------
        
        // ------------------
        // Search data in Dryad associated with Journal X
        // 
        // ------------------

        // ------------------
        // Top 10 downloads
        // 
        // ------------------

        // ------------------
        // Top 10 viewed files
        // 
        // ------------------

        // ------------------
        // Most recent deposits
        // 
        // ------------------

        // ------------------
        // Geographic breakdown of users
        // 
        // ------------------
        
    }

    
}
