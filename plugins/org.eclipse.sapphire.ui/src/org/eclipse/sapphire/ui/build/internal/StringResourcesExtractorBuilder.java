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

package org.eclipse.sapphire.ui.build.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class StringResourcesExtractorBuilder

    extends IncrementalProjectBuilder
    
{

    @Override
    @SuppressWarnings( "unchecked" )
    
    protected IProject[] build( final int kind,
                                final Map args,
                                final IProgressMonitor monitor )
    
        throws CoreException
        
    {
        monitor.beginTask( "Extracting localizable resources...", 100 );
        
        try
        {
            final IProject project = getProject();
            final ICommand command = getCommand();
            final Map<String,String> arguments = command.getArguments();
            
            final String input = arguments.get( "input" );
            
            if( input == null )
            {
                throw new IllegalStateException();
            }
            
            final IFolder inputFolder = project.getFolder( input );
            
            if( ! inputFolder.exists() )
            {
                throw new IllegalStateException();
            }
            
            final String output = arguments.get( "output" );
            
            if( output == null )
            {
                throw new IllegalStateException();
            }
            
            final IFolder outputFolder = project.getFolder( output );
            
            IResourceDelta delta = getDelta( project );
            
            final Set<IFile> inputFilesAddedOrModified = new HashSet<IFile>();
            final Set<IFile> inputFilesRemoved = new HashSet<IFile>();
            
            if( delta == null )
            {
                inputFolder.accept
                (
                    new IResourceVisitor()
                    {
                        public boolean visit( final IResource resource )
                        {
                            if( resource.getType() == IResource.FOLDER )
                            {
                                return true;
                            }
                            else
                            {
                                if( resource.getFileExtension().toLowerCase().equals( "sdef" ) )
                                {
                                    inputFilesAddedOrModified.add( (IFile) resource );
                                }
                                
                                return false;
                            }
                        }
                    }
                );
            }
            else
            {
                delta = delta.findMember( inputFolder.getProjectRelativePath() );
                
                if( delta != null )
                {
                    delta.accept
                    (
                        new IResourceDeltaVisitor()
                        {
                            public boolean visit( final IResourceDelta delta ) throws CoreException
                            {
                                final IResource resource = delta.getResource();
                                
                                if( resource.getType() == IResource.FOLDER )
                                {
                                    return true;
                                }
                                else
                                {
                                    if( resource.getFileExtension().toLowerCase().equals( "sdef" ) )
                                    {
                                        if( delta.getKind() == IResourceDelta.REMOVED )
                                        {
                                            inputFilesRemoved.add( (IFile) resource );
                                        }
                                        else
                                        {
                                            inputFilesAddedOrModified.add( (IFile) resource );
                                        }
                                    }
                                    
                                    return false;
                                }
                            }
                        }
                    );
                }
            }
            
            monitor.worked( 10 );
            
            if( monitor.isCanceled() )
            {
                throw new OperationCanceledException();
            }
            
            final int totalFilesToProcess = inputFilesRemoved.size() + inputFilesAddedOrModified.size();
            
            if( totalFilesToProcess == 0 )
            {
                monitor.worked( 90 );
            }
            else
            {
                final IProgressMonitor processingProgressMonitor = new SubProgressMonitor( monitor, 90 );
                processingProgressMonitor.beginTask( "", totalFilesToProcess );
                
                for( IFile file : inputFilesRemoved )
                {
                    final IFile resourcesFile = getResourceFile( inputFolder, file, outputFolder );
                    
                    if( resourcesFile.exists() )
                    {
                        resourcesFile.delete( true, null );
                    }
                    
                    processingProgressMonitor.worked( 1 );
                    
                    if( monitor.isCanceled() )
                    {
                        throw new OperationCanceledException();
                    }
                }
                
                for( IFile file : inputFilesAddedOrModified )
                {
                    try
                    {
                        final InputStream in = file.getContents();
                        final String resourcesFileContent;
                        
                        try
                        {
                            final Reader r = new InputStreamReader( in );
                            resourcesFileContent = StringResourcesExtractor.extract( r );
                        }
                        finally
                        {
                            try
                            {
                                in.close();
                            }
                            catch( IOException e ) {}
                        }
                        
                        if( resourcesFileContent != null )
                        {
                            final byte[] bytes = resourcesFileContent.getBytes();
                            final InputStream stream = new ByteArrayInputStream( bytes );
                            
                            final IFile resourcesFile = getResourceFile( inputFolder, file, outputFolder );
                            
                            if( resourcesFile.exists() )
                            {
                                resourcesFile.setContents( stream, IResource.FORCE, null );
                            }
                            else
                            {
                                create( resourcesFile.getParent() );
                                resourcesFile.create( stream, IResource.FORCE, null );
                            }
                            
                            resourcesFile.setDerived( true, new NullProgressMonitor() );
                        }
                    }
                    catch( Exception e )
                    {
                        SapphireUiFrameworkPlugin.log( e );
                    }
                    
                    processingProgressMonitor.worked( 1 );
                    
                    if( monitor.isCanceled() )
                    {
                        throw new OperationCanceledException();
                    }
                }
            }
            
            return new IProject[] { project };
        }
        finally
        {
            monitor.done();
        }
    }
    
    private static IFile getResourceFile( final IFolder inputFolder,
                                          final IFile inputFile,
                                          final IFolder outputFolder )
    {
        final IPath relativePath = inputFile.getParent().getFullPath().makeRelativeTo( inputFolder.getFullPath() );
        final IFolder parentFolder = outputFolder.getFolder( relativePath );
        return parentFolder.getFile( getNameWithoutExtension( inputFile ) + ".properties" );
    }
    
    private static String getNameWithoutExtension( final IFile f )
    {
        final String fname = f.getName();
        final int lastdot = fname.lastIndexOf( '.' );
        
        if( lastdot == -1 )
        {
            return fname;
        }
        else
        {
            return fname.substring( 0, lastdot );
        }
    }
    
    private static void create( final IContainer container )
    
        throws CoreException
        
    {
        if( container.getType() == IResource.FOLDER )
        {
            create( container.getParent() );
            
            if( ! container.exists() )
            {
                final IFolder folder = (IFolder) container;
                folder.create( true, true, null );
                folder.setDerived( true, new NullProgressMonitor() );
            }
        }
    }
    
}
