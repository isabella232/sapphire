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

package org.eclipse.sapphire.tests.java.jdt;

import static org.eclipse.sapphire.util.StringUtil.UTF8;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class JavaJdtTestHelper
{
    private final Class<?> testClass;
    private IJavaProject project;
    
    public JavaJdtTestHelper( final Class<?> testClass )
    {
        this.testClass = testClass;
    }
    
    public IJavaProject getJavaProject() throws Exception
    {
        if( this.project == null )
        {
            final IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject( this.testClass.getName() );
            p.create( null );
            p.open( null );
            
            final IProjectDescription desc = p.getDescription();
            desc.setNatureIds( new String[] { JavaCore.NATURE_ID } );
            p.setDescription( desc, null );
            
            this.project = JavaCore.create( p );
            
            final IFolder srcFolder = p.getFolder( "src" );
            create( srcFolder );
            
            final IFolder binFolder = p.getFolder( "bin" );
            create( binFolder );
            
            final IClasspathEntry[] cp =
            {
                JavaRuntime.getDefaultJREContainerEntry(),
                JavaCore.newSourceEntry( srcFolder.getFullPath() )
            };
            
            this.project.setRawClasspath( cp, binFolder.getFullPath(), null );
        }
        
        return this.project;
    }
    
    public void writeJavaSourceFile( final String packageName,
                                     final String className,
                                     final String content )
    
        throws Exception
        
    {
        final IProject project = getJavaProject().getProject();
        IFolder folder = project.getFolder( "src" );
        
        final StringBuilder buf = new StringBuilder();
        
        if( packageName != null )
        {
            folder = folder.getFolder( packageName.replace( '.', '/' ) );
            
            if( ! folder.exists() )
            {
                create( folder );
            }
            
            buf.append( "import " + packageName + ";\n\n" );
        }
        
        buf.append( content );
        
        final byte[] bytes = buf.toString().getBytes( UTF8 );
        
        final IFile file = folder.getFile( className + ".java" );
        file.create( new ByteArrayInputStream( bytes ), true, null );
    }
    
    public void create( final IFolder folder ) throws Exception
    {
        if( ! folder.exists() )
        {
            final IContainer parent = folder.getParent();
            
            if( parent instanceof IFolder )
            {
                create( (IFolder) parent );
            }
            
            folder.create( true, true, null );
        }
    }

    public void dispose() throws Exception
    {
        if( this.project != null )
        {
            this.project.getProject().delete( true, null );
        }
    }
    
}
