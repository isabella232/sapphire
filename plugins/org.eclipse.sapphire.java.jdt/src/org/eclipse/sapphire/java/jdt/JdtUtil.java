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

package org.eclipse.sapphire.java.jdt;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.sapphire.util.ListFactory;

/**
 * Collection of utility functions for Java Developer Tools.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class JdtUtil
{
    private JdtUtil() {}
    
    /**
     * Finds the nearest Java source folder for the specified resource. 
     * 
     * <p>For a resource contained by a source folder, the containing source folder is returned.</p>
     * 
     * <p>For a resource in a Java project, but outside a source folder, the first non-derived source 
     * folder is returned.</p>
     * 
     * <p>For a resource in a non-Java project or in a Java project without a non-derived source 
     * folder, null is returned.</p>
     * 
     * @param resource the resource to use for search context
     * @return the nearest Java source folder or null
     */
    
    public static IContainer findSourceFolder( final IResource resource )
    {
        if( resource == null )
        {
            throw new IllegalArgumentException();
        }
        
        final IProject project = resource.getProject();
        
        if( project == null )
        {
            throw new IllegalArgumentException();
        }
        
        final List<IContainer> sourceFolders = findSourceFolders( project );
        final IPath resourceFullPath = resource.getFullPath();
        
        for( IContainer sourceFolder : sourceFolders )
        {
            if( sourceFolder.getFullPath().isPrefixOf( resourceFullPath ) )
            {
                return sourceFolder;
            }
        }
        
        for( IContainer sourceFolder : sourceFolders )
        {
            if( ! sourceFolder.isDerived() )
            {
                return sourceFolder;
            }
        }
        
        return null;
    }
    
    /**
     * Finds the Java source folders of the project containing the specified resource. If the
     * project is not a Java project, an empty list is returned.
     * 
     * @param resource the resource to use for locating the project 
     * @return the source folders of the project containing the specified resource
     * @throws IllegalArgumentException if resource is null or if resource doesn't
     *   belong to a project (such as IWorkspaceRoot)
     */
    
    public static List<IContainer> findSourceFolders( final IResource resource )
    {
        if( resource == null )
        {
            throw new IllegalArgumentException();
        }
        
        final IProject project = resource.getProject();
        
        if( project == null )
        {
            throw new IllegalArgumentException();
        }
        
        return findSourceFolders( project );
    }
    
    /**
     * Finds the Java source folders of the specified project. If the project is not 
     * a Java project, an empty list is returned.
     * 
     * @param project the project for which source folders are requested 
     * @return the source folders of the specified project
     * @throws IllegalArgumentException if project is null
     */
    
    public static List<IContainer> findSourceFolders( final IProject project )
    {
        if( project == null )
        {
            throw new IllegalArgumentException();
        }
        
        return findSourceFolders( JavaCore.create( project ) );
    }
    
    /**
     * Finds the Java source folders of the specified project. If the project is not 
     * a Java project, an empty list is returned.
     * 
     * @param project the project for which source folders are requested 
     * @return the source folders of the specified project
     * @throws IllegalArgumentException if project is null
     */
    
    public static List<IContainer> findSourceFolders( final IJavaProject project )
    {
        if( project == null )
        {
            throw new IllegalArgumentException();
        }
        
        final ListFactory<IContainer> sourceFolders = ListFactory.start();
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        
        try
        {
            for( IClasspathEntry cpe : project.getRawClasspath() )
            {
                if( cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE )
                {
                    final IResource sourceFolderResource = root.findMember( cpe.getPath() );
                    
                    if( sourceFolderResource instanceof IContainer )
                    {
                        sourceFolders.add( (IContainer) sourceFolderResource );
                    }
                }
            }
        }
        catch( JavaModelException e )
        {
            // Ignore the exception and return an empty list.
            
            return ListFactory.empty();
        }
        
        return sourceFolders.result();
    }

}
