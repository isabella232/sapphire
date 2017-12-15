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

package org.eclipse.sapphire.tests.java.jdt;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.sapphire.tests.java.jdt.t0001.TestJavaJdt0001;
import org.eclipse.sapphire.tests.java.jdt.t0002.TestJavaJdt0002;
import org.eclipse.sapphire.tests.java.jdt.t0003.TestJavaJdt0003;
import org.eclipse.sapphire.tests.java.jdt.t0004.TestJavaJdt0004;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class JavaJdtTestSuite

    extends TestCase
    
{
    private JavaJdtTestSuite( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "JavaJdt" );

        suite.addTest( TestJavaJdt0001.suite() );
        suite.addTest( TestJavaJdt0002.suite() );
        suite.addTest( TestJavaJdt0003.suite() );
        suite.addTest( TestJavaJdt0004.suite() );
        
        return suite;
    }
    
}
