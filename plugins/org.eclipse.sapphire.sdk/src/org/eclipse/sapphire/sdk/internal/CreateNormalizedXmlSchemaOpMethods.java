/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.sdk.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.sapphire.modeling.ProgressMonitor;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.modeling.xml.XmlElement;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.platform.PathBridge;
import org.eclipse.sapphire.platform.StatusBridge;
import org.eclipse.sapphire.sdk.CreateNormalizedXmlSchemaOp;
import org.eclipse.sapphire.workspace.WorkspaceFileResourceStore;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class CreateNormalizedXmlSchemaOpMethods
{
    public static Status execute( final CreateNormalizedXmlSchemaOp operation,
                                  ProgressMonitor monitor )
    {
        if( monitor == null )
        {
            monitor = new ProgressMonitor();
        }
        
        monitor.beginTask( Resources.executeTaskName, 2 );
        
        try
        {
            final IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();
            final IFile sourceSchemaFile = wsroot.getFile( PathBridge.create( operation.getSourceFile().getContent() ) );
            final IFile targetSchemaFile = operation.getFileHandle();
            
            try
            {
                final InputStream sourceSchemaFileInputStream = sourceSchemaFile.getContents();
                
                try
                {
                    targetSchemaFile.refreshLocal( IResource.DEPTH_ZERO, new NullProgressMonitor() );
                    
                    create( targetSchemaFile.getParent() );
                    
                    if( targetSchemaFile.exists() )
                    {
                        targetSchemaFile.setContents( sourceSchemaFileInputStream, IFile.FORCE, null );
                    }
                    else
                    {
                        targetSchemaFile.create( sourceSchemaFileInputStream, IFile.FORCE, null );
                    }
                }
                finally
                {
                    try
                    {
                        sourceSchemaFileInputStream.close();
                    }
                    catch( IOException e ) {}
                }
            }
            catch( CoreException e )
            {
                return StatusBridge.create( e.getStatus() );
            }
            
            monitor.worked( 1 );
            
            try
            {
                final WorkspaceFileResourceStore fileResourceStore = new WorkspaceFileResourceStore( targetSchemaFile );
                final XmlResourceStore xmlResourceStore = new XmlResourceStore( fileResourceStore );
                final XmlElement root = new XmlElement( xmlResourceStore, xmlResourceStore.getDomDocument().getDocumentElement() );
    
                removeAnnotations( root );
                sort( root );
                root.format();
                
                xmlResourceStore.save();
            }
            catch( ResourceStoreException e )
            {
                return Status.createErrorStatus( e );
            }
        }
        finally
        {
            monitor.done();
        }
        
        return Status.createOkStatus();
    }
    
    private static void create( final IContainer container ) throws CoreException
    {
        if( ! container.exists() )
        {
            create( container.getParent() );
            
            final IFolder iFolder = (IFolder) container;
            iFolder.create( true, true, null );
        }
    }
    
    private static void removeAnnotations( final XmlElement element )
    {
        for( XmlElement x : element.getChildElements( "annotation" ) )
        {
            x.remove();
        }
        
        for( XmlElement x : element.getChildElements() )
        {
            removeAnnotations( x );
        }
    }
    
    private static void sort( final XmlElement root )
    {
        final List<XmlElement> elements = new ArrayList<XmlElement>();
        
        for( XmlElement x : root.getChildElements() )
        {
            elements.add( x );
            x.remove();
        }
        
        Collections.sort( elements, new SchemaElementComparator() );
        
        final Element rootDomElement = root.getDomNode();
        
        for( XmlElement x : elements )
        {
            rootDomElement.insertBefore( x.getDomNode(), null );
        }
    }
    
    private static final class SchemaElementComparator implements Comparator<XmlElement>
    {
        public int compare( final XmlElement x, final XmlElement y )
        {
            int result = x.getLocalName().compareTo( y.getLocalName() );
            
            if( result == 0 )
            {
                result = x.getAttributeText( "name" ).compareTo( y.getAttributeText( "name" ) );
            }
            
            return result;
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String executeTaskName;
        
        static
        {
            initializeMessages( CreateNormalizedXmlSchemaOpMethods.class.getName(), Resources.class );
        }
    }
    
}
