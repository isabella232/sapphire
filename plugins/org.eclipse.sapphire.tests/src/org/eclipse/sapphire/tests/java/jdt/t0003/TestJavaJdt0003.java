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

package org.eclipse.sapphire.tests.java.jdt.t0003;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.java.jdt.JavaJdtTestCase;
import org.eclipse.sapphire.workspace.WorkspaceFileResourceStore;

/**
 * Tests resolution of Java type references in the model via JdtJavaTypeReferenceService.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestJavaJdt0003

    extends JavaJdtTestCase
    
{
    private TestJavaJdt0003( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "JavaJdt0003" );

        suite.addTest( new TestJavaJdt0003( "testTopLevel" ) );
        suite.addTest( new TestJavaJdt0003( "testInner" ) );
        
        return suite;
    }
    
    public void testTopLevel() throws Exception
    {
        final IJavaProject project = getJavaProject();
        writeJavaSourceFile( "foo.bar", "TestClass", "public class TestClass {}" );
        
        final IFile file = project.getProject().getFile( "foobar.xml" );
        final TestElement element = TestElement.TYPE.instantiate( new RootXmlResource( new XmlResourceStore( new WorkspaceFileResourceStore( file ) ) ) );
        element.setSomeClass( "foo.bar.TestClass" );
        
        final JavaType type = element.getSomeClass().resolve();

        assertNotNull( type );
    }

    public void testInner() throws Exception
    {
        final IJavaProject project = getJavaProject();
        writeJavaSourceFile( "foo.bar", "TestClass", "public class TestClass { public static class Inner {} }" );
        
        final IFile file = project.getProject().getFile( "foobar.xml" );
        final TestElement element = TestElement.TYPE.instantiate( new RootXmlResource( new XmlResourceStore( new WorkspaceFileResourceStore( file ) ) ) );
        element.setSomeClass( "foo.bar.TestClass$Inner" );
        
        final JavaType type = element.getSomeClass().resolve();

        assertNotNull( type );
    }

}
