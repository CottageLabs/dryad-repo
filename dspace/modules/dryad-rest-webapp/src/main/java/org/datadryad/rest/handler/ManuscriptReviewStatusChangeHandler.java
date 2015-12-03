/*
 */
package org.datadryad.rest.handler;

import org.apache.log4j.Logger;
import org.datadryad.rest.models.Manuscript;
import org.datadryad.rest.storage.StoragePath;
import org.dspace.core.ConfigurationManager;
import org.dspace.servicemanager.DSpaceKernelImpl;
import org.dspace.servicemanager.DSpaceKernelInit;
import org.dspace.workflow.ApproveRejectReviewItem;
import org.dspace.workflow.ApproveRejectReviewItemException;

/**
 * Processes accept/reject status and moves items in review.
 * @author Dan Leehr <dan.leehr@nescent.org>
 */
public class ManuscriptReviewStatusChangeHandler implements HandlerInterface<Manuscript> {
    private static final Logger log = Logger.getLogger(ManuscriptReviewStatusChangeHandler.class);
    private DSpaceKernelImpl kernelImpl;
    public ManuscriptReviewStatusChangeHandler() {
        // Requires DSpace kernel to be running
        try {
            kernelImpl = DSpaceKernelInit.getKernel(null);
            if (!kernelImpl.isRunning())
            {
                kernelImpl.start(ConfigurationManager.getProperty("dspace.dir"));
            }
        } catch (Exception ex) {
            // Failed to start so destroy it and log and throw an exception
            try {
                if(kernelImpl != null) {
                    kernelImpl.destroy();
                }
            } catch (Exception e1) {
                // Nothing to do
            }
            log.error("Error Initializing DSpace kernel in ManuscriptReviewStatusChangeHandler", ex);
        }
    }

    @Override
    public void handleCreate(StoragePath path, Manuscript manuscript) throws HandlerException {
        processChange(manuscript);
    }

    @Override
    public void handleUpdate(StoragePath path, Manuscript manuscript) throws HandlerException {
        processChange(manuscript);
    }

    @Override
    public void handleDelete(StoragePath path, Manuscript object) throws HandlerException {
        // Do nothing
    }

    private void processChange(Manuscript manuscript) throws HandlerException {
        if(kernelImpl == null) {
            throw new HandlerException("Cannot process change, DSpace Kernel is not initialized");
        } else if(!kernelImpl.isRunning()) {
            throw new HandlerException("Cannot process change, DSpace Kernel is not running");
        }

        if (manuscript.isSubmitted()) {
            // Do nothing for submitted
        } else if (manuscript.isAccepted() || manuscript.isPublished()) {
            // accept for accepted or published
            accept(manuscript);
        } else if (manuscript.isRejected() || manuscript.isNeedsRevision()) {
            // reject for rejected or needs revision
            reject(manuscript);
        }
    }

    private void accept(Manuscript manuscript) throws HandlerException {
        // dspace review-item -a true
        try {
            ApproveRejectReviewItem.reviewManuscript(manuscript);
        } catch (ApproveRejectReviewItemException ex) {
            throw new HandlerException("Exception handling acceptance notice for manuscript " + manuscript.manuscriptId, ex);
        }
    }
}
