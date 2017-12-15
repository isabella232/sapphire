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

package org.eclipse.sapphire.tests.modeling.xml.dtd;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.sapphire.tests.modeling.xml.dtd.t0001.TestXmlDtd0001;
import org.eclipse.sapphire.tests.modeling.xml.dtd.t0002.TestXmlDtd0002;
import org.eclipse.sapphire.tests.modeling.xml.dtd.t0003.TestXmlDtd0003;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class XmlDtdTestSuite extends TestCase
{
    private XmlDtdTestSuite( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "XmlDtdTestSuite" );

        suite.addTest( TestXmlDtd0001.suite() );
        suite.addTest( TestXmlDtd0002.suite() );
        suite.addTest( TestXmlDtd0003.suite() );
        
        return suite;
    }
    
}
