/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [337232] Certain schema causes elements to be out of order in corresponding xml files
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.xsd;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.sapphire.tests.modeling.xml.xsd.t0001.TestXmlXsd0001;
import org.eclipse.sapphire.tests.modeling.xml.xsd.t0002.TestXmlXsd0002;
import org.eclipse.sapphire.tests.modeling.xml.xsd.t0003.TestXmlXsd0003;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class XmlXsdTestSuite

    extends TestCase
    
{
    private XmlXsdTestSuite( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "XmlXsd" );

        suite.addTest( TestXmlXsd0001.suite() );
        suite.addTest( TestXmlXsd0002.suite() );
        suite.addTest( TestXmlXsd0003.suite() );
        
        return suite;
    }
    
}
