/*******************************************************************************
 * Copyright (c) 2012 Accenture Services Pvt Ltd. and Oracle
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
import java.util.SortedSet;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.annotations.PossibleValues;
import org.eclipse.sapphire.services.ContentProposal;
import org.eclipse.sapphire.services.ContentProposalService;
import org.eclipse.sapphire.services.PossibleValuesService;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests PossibleValuesContentProposalService.
 * 
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestServices0004 extends SapphireTestCase
{
    private TestServices0004( final String name ) 
    {
        super( name );
    }

    public static Test suite() 
    {
        final TestSuite suite = new TestSuite();

        suite.setName( "TestServices0004" );
        
        suite.addTest( new TestServices0004( "testFromPossibleValuesService" ) );
        suite.addTest( new TestServices0004( "testFromPossibleValuesAnnotation" ) );

        return suite;
    }

    public void testFromPossibleValuesService()
    {
        final TestElement element = TestElement.TYPE.instantiate();

        final PossibleValuesService possibleValuesService = element.service( TestElement.PROP_SHAPES, PossibleValuesService.class );
        assertNotNull( possibleValuesService );

        final SortedSet<String> values = possibleValuesService.values();
        assertNotNull( values );

        final ContentProposalService contentProposalService = element.service( TestElement.PROP_SHAPES, ContentProposalService.class );
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

    public void testFromPossibleValuesAnnotation()
    {
        final TestElement item = TestElement.TYPE.instantiate();

        final PossibleValues possibleValuesAnnotation = TestElement.PROP_COLORS.getAnnotation( PossibleValues.class );
        assertNotNull( possibleValuesAnnotation );

        final String[] values = possibleValuesAnnotation.values();
        assertNotNull( values );

        final ContentProposalService contentProposalService = item.service( TestElement.PROP_COLORS, ContentProposalService.class );
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