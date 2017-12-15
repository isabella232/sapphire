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

package org.eclipse.sapphire.tests.java.jdt.t0004;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.java.jdt.JavaJdtTestHelper;
import org.eclipse.sapphire.tests.java.t0004.ITestElement;
import org.eclipse.sapphire.tests.java.t0004.TestJava0004;
import org.eclipse.sapphire.workspace.WorkspaceFileResourceStore;

/**
 * Tests JavaTypeValidationService in context of JdtJavaTypeReferenceService.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestJavaJdt0004

    extends TestJava0004
    
{
    private final JavaJdtTestHelper helper;
    
    private TestJavaJdt0004( final String name )
    {
        super( name );
        this.helper = new JavaJdtTestHelper( getClass(), getName() );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "JavaJdt0004" );

        suite.addTest( new TestJavaJdt0004( "testOptionalAnyType" ) );
        suite.addTest( new TestJavaJdt0004( "testRequiredClass1" ) );
        suite.addTest( new TestJavaJdt0004( "testRequiredClass2" ) );
        suite.addTest( new TestJavaJdt0004( "testRequiredClass3" ) );
        suite.addTest( new TestJavaJdt0004( "testRequiredClass4" ) );
        suite.addTest( new TestJavaJdt0004( "testRequiredClass5" ) );
        suite.addTest( new TestJavaJdt0004( "testRequiredClass6" ) );
        suite.addTest( new TestJavaJdt0004( "testRequiredInterface1" ) );
        suite.addTest( new TestJavaJdt0004( "testRequiredInterface2" ) );
        suite.addTest( new TestJavaJdt0004( "testRequiredInterface3" ) );
        suite.addTest( new TestJavaJdt0004( "testRequiredAnnotation1" ) );
        suite.addTest( new TestJavaJdt0004( "testRequiredEnum1" ) );
        suite.addTest( new TestJavaJdt0004( "testRequiredMixedType1" ) );
        suite.addTest( new TestJavaJdt0004( "testRequiredMixedType2" ) );
        
        return suite;
    }
    
    protected ITestElement createTestElement() throws Exception
    {
        final IJavaProject project = this.helper.getJavaProject();
        final IFile file = project.getProject().getFile( "foobar.xml" );
        return ITestElement.TYPE.instantiate( new RootXmlResource( new XmlResourceStore( new WorkspaceFileResourceStore( file ) ) ) );
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        this.helper.dispose();
    }
    
}
