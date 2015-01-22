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

package org.eclipse.sapphire.tests.java.jdt.t0002;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.jdt.internal.JdtJavaTypeReferenceService;
import org.eclipse.sapphire.tests.java.jdt.JavaJdtTestCase;
import org.junit.Test;

/**
 * Tests correctness of type hierarchy reporting of JdtJavaTypeReferenceService.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestJavaJdt0002 extends JavaJdtTestCase
{
    @Test
    
    public void test() throws Exception
    {
        final IJavaProject project = getJavaProject();
        
        writeJavaSourceFile( "foo.bar", "TestClassA", "public class TestClassA extends TestClassAA implements TestInterfaceA {}" );
        writeJavaSourceFile( "foo.bar", "TestClassAA", "public class TestClassAA extends TestClassAAA {}" );
        writeJavaSourceFile( "foo.bar", "TestClassAAA", "public class TestClassAAA implements TestInterfaceB, TestInterfaceC {}" );
        writeJavaSourceFile( "foo.bar", "TestInterfaceA", "public interface TestInterfaceA {}" );
        writeJavaSourceFile( "foo.bar", "TestInterfaceB", "public interface TestInterfaceB {}" );
        writeJavaSourceFile( "foo.bar", "TestInterfaceC", "public interface TestInterfaceC {}" );
        
        final JdtJavaTypeReferenceService service = new JdtJavaTypeReferenceService( project );
        
        final JavaType type = service.resolve( "foo.bar.TestClassA" );
        
        assertNotNull( type );

        assertTrue( type.isOfType( "java.lang.Object" ) );
        assertTrue( type.isOfType( "foo.bar.TestClassA" ) );
        assertTrue( type.isOfType( "foo.bar.TestClassAA" ) );
        assertTrue( type.isOfType( "foo.bar.TestClassAAA" ) );
        assertTrue( type.isOfType( "foo.bar.TestInterfaceA" ) );
        assertTrue( type.isOfType( "foo.bar.TestInterfaceB" ) );
        assertTrue( type.isOfType( "foo.bar.TestInterfaceC" ) );
        
        assertFalse( type.isOfType( "java.util.List" ) );
        assertFalse( type.isOfType( "java.util.ArrayList" ) );
        assertFalse( type.isOfType( "foo.bar.FooBar" ) );
    }

}
