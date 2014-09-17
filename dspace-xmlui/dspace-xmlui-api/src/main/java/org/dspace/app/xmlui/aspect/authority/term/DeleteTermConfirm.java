/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.authority.term;

import java.sql.SQLException;
import java.util.ArrayList;

import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.app.xmlui.wing.element.Para;
import org.dspace.app.xmlui.wing.element.Row;
import org.dspace.app.xmlui.wing.element.Table;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.authority.Term;

/**
 * Present the user with a list of not-yet-but-soon-to-be-deleted-epeople.
 *
 * @author Alexey Maslov
 */
public class DeleteTermConfirm extends AbstractDSpaceTransformer
{
    /** Language Strings */
    private static final Message T_dspace_home =
            message("xmlui.general.dspace_home");

    private static final Message T_eperson_trail =
            message("xmlui.administrative.eperson.general.epeople_trail");

    private static final Message T_title =
            message("xmlui.administrative.eperson.DeleteEPeopleConfirm.title");

    private static final Message T_trail =
            message("xmlui.administrative.eperson.DeleteEPeopleConfirm.trail");

    private static final Message T_confirm_head =
            message("xmlui.administrative.eperson.DeleteEPeopleConfirm.confirm_head");

    private static final Message T_confirm_para =
            message("xmlui.administrative.eperson.DeleteEPeopleConfirm.confirm_para");

    private static final Message T_head_id =
            message("xmlui.administrative.eperson.DeleteEPeopleConfirm.head_id");

    private static final Message T_head_name =
            message("xmlui.administrative.eperson.DeleteEPeopleConfirm.head_name");

    private static final Message T_head_email =
            message("xmlui.administrative.eperson.DeleteEPeopleConfirm.head_email");

    private static final Message T_submit_confirm =
            message("xmlui.administrative.eperson.DeleteEPeopleConfirm.submit_confirm");

    private static final Message T_submit_cancel =
            message("xmlui.general.cancel");


    public void addPageMeta(PageMeta pageMeta) throws WingException
    {
        pageMeta.addMetadata("title").addContent(T_title);
        pageMeta.addTrailLink(contextPath + "/", T_dspace_home);
        pageMeta.addTrailLink(contextPath + "/admin/term",T_eperson_trail);
        pageMeta.addTrail().addContent(T_trail);
    }

    public void addBody(Body body) throws WingException, SQLException, AuthorizeException
    {
        // Get all our parameters
        String idsString = parameters.getParameter("termIds", null);

        ArrayList<Term> terms = new ArrayList<Term>();
        for (String id : idsString.split(","))
        {
            Term term = Term.find(context,Integer.valueOf(id));
            terms.add(term);
        }

        // DIVISION: epeople-confirm-delete
        Division deleted = body.addInteractiveDivision("term-confirm-delete",contextPath+"/admin/term",Division.METHOD_POST,"primary administrative term");
        deleted.setHead(T_confirm_head);
        deleted.addPara(T_confirm_para);

        Table table = deleted.addTable("term-confirm-delete",terms.size() + 1, 1);
        Row header = table.addRow(Row.ROLE_HEADER);
        header.addCell().addContent(T_head_id);
        header.addCell().addContent("Literal Form");
        header.addCell().addContent("identifier");

        for (Term t : terms)
        {
            Row row = table.addRow();
            row.addCell().addContent(t.getID());
            row.addCell().addContent(t.getLiteralForm());
            row.addCell().addContent(t.getIdentifier());
        }
        Para buttons = deleted.addPara();
        buttons.addButton("submit_confirm").setValue(T_submit_confirm);
        buttons.addButton("submit_cancel").setValue(T_submit_cancel);

        deleted.addHidden("administrative-continue").setValue(knot.getId());
    }
}
