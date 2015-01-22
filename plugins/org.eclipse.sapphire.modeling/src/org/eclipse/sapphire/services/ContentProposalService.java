/*******************************************************************************
 * Copyright (c) 2015 Oracle and Accenture Services Pvt Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 *    Kamesh Sampath - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.services;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 */

public abstract class ContentProposalService extends Service
{
    public abstract Session session();

    public abstract class Session
    {
        private String filter = "";
        private List<ContentProposal> proposals;
        private List<ContentProposal> proposalsReadOnly;
        
        public Session()
        {
            this.filter = "";
            this.proposals = compute();
            this.proposalsReadOnly = Collections.unmodifiableList( this.proposals );
        }
        
        /**
         * Returns the current filter.
         * 
         * @return the current filter
         */
        
        public final String filter() 
        {
            return this.filter;
        }
        
        /**
         * Returns the list of proposals corresponding to the current filter.
         * 
         * @return the list of proposals corresponding to the current filter
         */

        public final List<ContentProposal> proposals() 
        {
            return this.proposalsReadOnly;
        }
        
        /**
         * Advances the content proposal session by the specified characters and re-computes the proposals.
         * After calling this method, filter() and proposals() methods will return updated results.
         * 
         * @param delta the characters that should be appended to the current filter
         */

        public final void advance( final String delta ) 
        {
            if( delta == null || delta.length() == 0 )
            {
                throw new IllegalArgumentException();
            }
            
            this.filter = this.filter + delta;
            this.proposals = compute();
            this.proposalsReadOnly = Collections.unmodifiableList( this.proposals );
        }
        
        /**
         * Computes the list of proposals based on the current state of the filter. The implementation
         * should not attempt to hold on to the returned list or attempt to modify it after completion
         * of this method. 
         * 
         * @return the list of proposals
         */
        
        protected abstract List<ContentProposal> compute();
        
    }

}
