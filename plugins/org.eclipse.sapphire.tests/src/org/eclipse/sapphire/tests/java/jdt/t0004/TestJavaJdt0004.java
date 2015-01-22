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

package org.eclipse.sapphire.tests.java.jdt.t0004;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.java.jdt.JavaJdtTestHelper;
import org.eclipse.sapphire.tests.java.t0004.TestElement;
import org.eclipse.sapphire.tests.java.t0004.TestJava0004;
import org.eclipse.sapphire.workspace.WorkspaceFileResourceStore;
import org.junit.After;

/**
 * Tests JavaTypeValidationService in context of JdtJavaTypeReferenceService.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestJavaJdt0004 extends TestJava0004
{
    private final JavaJdtTestHelper helper;
    
    public TestJavaJdt0004()
    {
        this.helper = new JavaJdtTestHelper( getClass() );
    }
    
    @Override
    
    protected TestElement createTestElement() throws Exception
    {
        final IJavaProject project = this.helper.getJavaProject();
        final IFile file = project.getProject().getFile( "foobar.xml" );
        return TestElement.TYPE.instantiate( new RootXmlResource( new XmlResourceStore( new WorkspaceFileResourceStore( file ) ) ) );
    }
    
    @After
    
    public void disposeTestHelper() throws Exception
    {
        this.helper.dispose();
    }
    
}
