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

package org.eclipse.sapphire.tests.java.t0002;

import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.internal.StandardJavaTypeReferenceService;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests correctness of type hierarchy reporting of StandardJavaTypeReferenceService.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestJava0002 extends SapphireTestCase
{
    private static final String PACKAGE_NAME = "org.eclipse.sapphire.tests.java.t0002";
    
    @Test
    
    public void test()
    {
        final StandardJavaTypeReferenceService service = new StandardJavaTypeReferenceService( TestJava0002.class.getClassLoader() );
        final JavaType type = service.resolve( PACKAGE_NAME + ".TestClassA" );
        
        assertNotNull( type );

        assertTrue( type.isOfType( "java.lang.Object" ) );
        assertTrue( type.isOfType( PACKAGE_NAME + ".TestClassA" ) );
        assertTrue( type.isOfType( PACKAGE_NAME + ".TestClassAA" ) );
        assertTrue( type.isOfType( PACKAGE_NAME + ".TestClassAAA" ) );
        assertTrue( type.isOfType( PACKAGE_NAME + ".TestInterfaceA" ) );
        assertTrue( type.isOfType( PACKAGE_NAME + ".TestInterfaceB" ) );
        assertTrue( type.isOfType( PACKAGE_NAME + ".TestInterfaceC" ) );
        
        assertFalse( type.isOfType( "java.util.List" ) );
        assertFalse( type.isOfType( "java.util.ArrayList" ) );
        assertFalse( type.isOfType( "foo.bar.FooBar" ) );
    }

}
