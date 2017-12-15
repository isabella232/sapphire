/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.dtd.t0004;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.xml.dtd.DtdParser;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests parsing of a DTD with an entity ref in ATTLIST.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestEntityRefInAttList extends SapphireTestCase
{
    private TestEntityRefInAttList( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestEntityRefInAttList" );

        suite.addTest( new TestEntityRefInAttList( "testEntityRefInAttList" ) );
        
        return suite;
    }
    
    public void testEntityRefInAttList() throws Exception
    {
        DtdParser.parse( loadResource( "TestCase.dtd" ) );
    }

}
