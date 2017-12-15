/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.binding;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.sapphire.tests.modeling.xml.binding.t0001.TestXmlBinding0001;
import org.eclipse.sapphire.tests.modeling.xml.binding.t0002.TestXmlBinding0002;
import org.eclipse.sapphire.tests.modeling.xml.binding.t0003.TestXmlBinding0003;
import org.eclipse.sapphire.tests.modeling.xml.binding.t0004.TestXmlBinding0004;
import org.eclipse.sapphire.tests.modeling.xml.binding.t0005.TestXmlBinding0005;
import org.eclipse.sapphire.tests.modeling.xml.binding.t0006.TestXmlBinding0006;
import org.eclipse.sapphire.tests.modeling.xml.binding.t0007.TestXmlBinding0007;
import org.eclipse.sapphire.tests.modeling.xml.binding.t0008.TestXmlBinding0008;
import org.eclipse.sapphire.tests.modeling.xml.binding.t0009.TestXmlBinding0009;
import org.eclipse.sapphire.tests.modeling.xml.binding.t0010.TestXmlBinding0010;
import org.eclipse.sapphire.tests.modeling.xml.binding.t0011a.TestXmlBinding0011a;
import org.eclipse.sapphire.tests.modeling.xml.binding.t0011b.TestXmlBinding0011b;
import org.eclipse.sapphire.tests.modeling.xml.binding.t0011c.TestXmlBinding0011c;
import org.eclipse.sapphire.tests.modeling.xml.binding.t0011d.TestXmlBinding0011d;
import org.eclipse.sapphire.tests.modeling.xml.binding.t0011e.TestXmlBinding0011e;
import org.eclipse.sapphire.tests.modeling.xml.binding.t0011f.TestXmlBinding0011f;
import org.eclipse.sapphire.tests.modeling.xml.binding.t0011g.TestXmlBinding0011g;
import org.eclipse.sapphire.tests.modeling.xml.binding.t0011h.TestXmlBinding0011h;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class XmlBindingTestSuite

    extends TestCase
    
{
    private XmlBindingTestSuite( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "XmlBinding" );

        suite.addTest( TestXmlBinding0001.suite() );
        suite.addTest( TestXmlBinding0002.suite() );
        suite.addTest( TestXmlBinding0003.suite() );
        suite.addTest( TestXmlBinding0004.suite() );
        suite.addTest( TestXmlBinding0005.suite() );
        suite.addTest( TestXmlBinding0006.suite() );
        suite.addTest( TestXmlBinding0007.suite() );
        suite.addTest( TestXmlBinding0008.suite() );
        suite.addTest( TestXmlBinding0009.suite() );
        suite.addTest( TestXmlBinding0010.suite() );
        suite.addTest( TestXmlBinding0011a.suite() );
        suite.addTest( TestXmlBinding0011b.suite() );
        suite.addTest( TestXmlBinding0011c.suite() );
        suite.addTest( TestXmlBinding0011d.suite() );
        suite.addTest( TestXmlBinding0011e.suite() );
        suite.addTest( TestXmlBinding0011f.suite() );
        suite.addTest( TestXmlBinding0011g.suite() );
        suite.addTest( TestXmlBinding0011h.suite() );
        
        return suite;
    }
    
}
