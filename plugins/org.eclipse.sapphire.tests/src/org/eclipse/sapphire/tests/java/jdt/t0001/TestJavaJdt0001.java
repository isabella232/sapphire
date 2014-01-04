/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.java.jdt.t0001;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeKind;
import org.eclipse.sapphire.java.jdt.internal.JdtJavaTypeReferenceService;
import org.eclipse.sapphire.tests.java.jdt.JavaJdtTestCase;
import org.junit.Test;

/**
 * Tests correctness of Java type kind determination of JdtJavaTypeReferenceService.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestJavaJdt0001 extends JavaJdtTestCase
{
    @Test
    
    public void testIsClass() throws Exception
    {
        final IJavaProject project = getJavaProject();
        
        writeJavaSourceFile( "foo.bar", "TestClass", "public class TestClass {}" );
        
        final JdtJavaTypeReferenceService service = new JdtJavaTypeReferenceService( project );
        final JavaType type = service.resolve( "foo.bar.TestClass" );
        
        assertNotNull( type );
        assertEquals( JavaTypeKind.CLASS, type.kind() );
    }
    
    @Test

    public void testIsAbstractClass() throws Exception
    {
        final IJavaProject project = getJavaProject();
        
        writeJavaSourceFile( "foo.bar", "TestAbstractClass", "public abstract class TestAbstractClass {}" );
        
        final JdtJavaTypeReferenceService service = new JdtJavaTypeReferenceService( project );
        final JavaType type = service.resolve( "foo.bar.TestAbstractClass" );
        
        assertNotNull( type );
        assertEquals( JavaTypeKind.ABSTRACT_CLASS, type.kind() );
    }
    
    @Test

    public void testIsInterface() throws Exception
    {
        final IJavaProject project = getJavaProject();
        
        writeJavaSourceFile( "foo.bar", "TestInterface", "public interface TestInterface {}" );
        
        final JdtJavaTypeReferenceService service = new JdtJavaTypeReferenceService( project );
        final JavaType type = service.resolve( "foo.bar.TestInterface" );
        
        assertNotNull( type );
        assertEquals( JavaTypeKind.INTERFACE, type.kind() );
    }
    
    @Test
    
    public void testIsAnnotation() throws Exception
    {
        final IJavaProject project = getJavaProject();
        
        writeJavaSourceFile( "foo.bar", "TestAnnotation", "public @interface TestAnnotation {}" );
        
        final JdtJavaTypeReferenceService service = new JdtJavaTypeReferenceService( project );
        final JavaType type = service.resolve( "foo.bar.TestAnnotation" );
        
        assertNotNull( type );
        assertEquals( JavaTypeKind.ANNOTATION, type.kind() );
    }
    
    @Test
    
    public void testIsEnum() throws Exception
    {
        final IJavaProject project = getJavaProject();
        
        writeJavaSourceFile( "foo.bar", "TestEnum", "public enum TestEnum { A, B, C }" );
        
        final JdtJavaTypeReferenceService service = new JdtJavaTypeReferenceService( project );
        final JavaType type = service.resolve( "foo.bar.TestEnum" );
        
        assertNotNull( type );
        assertEquals( JavaTypeKind.ENUM, type.kind() );
    }

}
