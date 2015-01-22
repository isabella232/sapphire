/*******************************************************************************
 * Copyright (c) 2015 Accenture Services Pvt Ltd. and Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kamesh Sampath - initial implementation
 *    Konstantin Komissarchik - initial implementation    
 ******************************************************************************/

package org.eclipse.sapphire.tests.services.t0004;

import java.util.Collection;
import java.util.Set;

import org.eclipse.sapphire.PossibleValues;
import org.eclipse.sapphire.PossibleValuesService;
import org.eclipse.sapphire.services.ContentProposal;
import org.eclipse.sapphire.services.ContentProposalService;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests PossibleValuesContentProposalService.
 * 
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestServices0004 extends SapphireTestCase
{
    @Test
    
    public void testFromPossibleValuesService()
    {
        final TestElement element = TestElement.TYPE.instantiate();

        final PossibleValuesService possibleValuesService = element.property( TestElement.PROP_SHAPES ).service( PossibleValuesService.class );
        assertNotNull( possibleValuesService );

        final Set<String> values = possibleValuesService.values();
        assertNotNull( values );

        final ContentProposalService contentProposalService = element.property( TestElement.PROP_SHAPES ).service( ContentProposalService.class );
        assertNotNull( contentProposalService );

        final ContentProposalService.Session session = contentProposalService.session();
        assertNotNull( session );

        Collection<ContentProposal> proposals;
        
        proposals = session.proposals();
        assertNotNull( proposals );
        assertEquals( values.size(), proposals.size() );

        session.advance( "Circ" );
        
        proposals = session.proposals();
        assertNotNull( proposals );
        assertEquals( 3, proposals.size() );
    }

    @Test
    
    public void testFromPossibleValuesAnnotation()
    {
        final TestElement item = TestElement.TYPE.instantiate();

        final PossibleValues possibleValuesAnnotation = TestElement.PROP_COLORS.getAnnotation( PossibleValues.class );
        assertNotNull( possibleValuesAnnotation );

        final String[] values = possibleValuesAnnotation.values();
        assertNotNull( values );

        final ContentProposalService contentProposalService = item.property( TestElement.PROP_COLORS ).service( ContentProposalService.class );
        assertNotNull( contentProposalService );

        final ContentProposalService.Session session = contentProposalService.session();
        assertNotNull( session );

        Collection<ContentProposal> proposals;
        
        proposals = session.proposals();
        assertNotNull( proposals );
        assertEquals( values.length, proposals.size() );

        session.advance( "R" );

        proposals = session.proposals();
        assertNotNull( proposals );
        assertEquals( 1, proposals.size() );
    }

}