/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.binding.t0004;

import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests reporting of unresolvable namespace usage in value property binding.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestXmlBinding0004 extends SapphireTestCase
{
    @Test
    
    public void test() throws Exception
    {
        final XmlResourceStore xmlResourceStore = new XmlResourceStore();
        final TestElement element = TestElement.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        
        try
        {
            element.setTestProperty( "abc" );
            fail( "Did not catch the expected exception." );
        }
        catch( Exception e )
        {
            assertEquals( e.getMessage(), "TestElement.TestProperty : Could not resolve namespace for foo:abc node name." );
        }
    }

}
