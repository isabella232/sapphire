/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.java;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.sapphire.tests.java.jdt.JavaJdtTestSuite;
import org.eclipse.sapphire.tests.java.t0001.TestJava0001;
import org.eclipse.sapphire.tests.java.t0002.TestJava0002;
import org.eclipse.sapphire.tests.java.t0003.TestJava0003;
import org.eclipse.sapphire.tests.java.t0004.TestJava0004;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class JavaTestSuite

    extends TestCase
    
{
    private JavaTestSuite( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "Java" );

        suite.addTest( TestJava0001.suite() );
        suite.addTest( TestJava0002.suite() );
        suite.addTest( TestJava0003.suite() );
        suite.addTest( TestJava0004.suite() );
        
        suite.addTest( JavaJdtTestSuite.suite() );
        
        return suite;
    }
    
}
