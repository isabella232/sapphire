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

package org.eclipse.sapphire.tests.workspace.t0001;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.workspace.TestWorkspace;
import org.eclipse.sapphire.workspace.WorkspaceFileResourceStore;

/**
 * Tests validation of Eclipse workspace and project relative paths.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestWorkspace0001

    extends TestWorkspace
    
{
    private IProject a;
    private IFile aa;
    private IFolder ab;
    private IFile aba;
    private IProject b;
    private IFile baaaaa;
    
    private TestWorkspace0001( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "Workspace0001" );

        suite.addTest( new TestWorkspace0001( "testWorkspaceRelativePath" ) );
        suite.addTest( new TestWorkspace0001( "testProjectRelativePath" ) );
        
        return suite;
    }
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.a = createProject( "a" );
        this.aa = createFile( this.a, "a.txt" );
        this.ab = createFolder( this.a, "b" );
        this.aba = createFile( this.a, "b/a.txt" );
        
        this.b = createProject( "b" );
        this.baaaaa = createFile( this.b, "a/a/a/a.txt" );
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        deleteProject( "a" );
        deleteProject( "b" );
    }
    
    public void testWorkspaceRelativePath() throws Exception
    {
        final ValueProperty property = TestElement.PROP_WORKSPACE_RELATIVE_PATH;
        final TestElement element = TestElement.TYPE.instantiate( new RootXmlResource( new XmlResourceStore( new WorkspaceFileResourceStore( this.aa ) ) ) );
        
        testValidationOk( element, property, this.a.getFullPath() );
        testValidationOk( element, property, this.aa.getFullPath() );
        testValidationOk( element, property, this.ab.getFullPath() );
        testValidationOk( element, property, this.aba.getFullPath() );
        testValidationOk( element, property, this.b.getFullPath() );
        testValidationOk( element, property, this.baaaaa.getFullPath() );
        
        testValidationError( element, property, this.ab.getFile( "b.txt" ).getFullPath() );
        testValidationError( element, property, this.b.getLocation() );
    }
    
    public void testProjectRelativePath() throws Exception
    {
        final ValueProperty property = TestElement.PROP_PROJECT_RELATIVE_PATH;
        
        TestElement element = TestElement.TYPE.instantiate( new RootXmlResource( new XmlResourceStore( new WorkspaceFileResourceStore( this.aa ) ) ) );
        
        testValidationOk( element, property, this.aa.getProjectRelativePath() );
        testValidationOk( element, property, this.ab.getProjectRelativePath() );
        testValidationOk( element, property, this.aba.getProjectRelativePath() );

        testValidationError( element, property, this.ab.getFile( "b.txt" ).getFullPath() );
        testValidationError( element, property, this.b.getLocation() );
        testValidationError( element, property, this.a.getFullPath() );
        testValidationError( element, property, this.aa.getFullPath() );
        testValidationError( element, property, this.ab.getFullPath() );
        testValidationError( element, property, this.baaaaa.getProjectRelativePath() );
        
        element = TestElement.TYPE.instantiate();
        
        testValidationError( element, property, this.aa.getProjectRelativePath(), "No context project found." );
    }
    
    private void testValidationOk( final TestElement element,
                                   final ValueProperty property,
                                   final IPath path )
    {
        element.write( property, new Path( path.toPortableString() ) );
        assertValidationOk( element.read( property ) );
    }
    
    private void testValidationError( final TestElement element,
                                      final ValueProperty property,
                                      final IPath path )
    {
        testValidationError( element, property, path, "File or folder \"" + path.toPortableString() + "\" does not exist." );
    }

    private void testValidationError( final TestElement element,
                                      final ValueProperty property,
                                      final IPath path,
                                      final String expectedErrorMessage )
    {
        element.write( property, new Path( path.toPortableString() ) );
        assertValidationError( element.read( property ), expectedErrorMessage );
    }

}
