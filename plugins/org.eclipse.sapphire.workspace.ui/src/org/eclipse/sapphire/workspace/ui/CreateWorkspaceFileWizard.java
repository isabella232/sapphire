/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.workspace.ui;

import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sapphire.Context;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.osgi.BundleBasedContext;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.forms.WizardDef;
import org.eclipse.sapphire.ui.forms.swt.SapphireWizard;
import org.eclipse.sapphire.workspace.CreateWorkspaceFileOp;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class CreateWorkspaceFileWizard<M extends CreateWorkspaceFileOp> 

    extends SapphireWizard<M>
    implements IWorkbenchWizard, IExecutableExtension
    
{
    private String editor;
    
    public CreateWorkspaceFileWizard( final ElementType type,
                                      final DefinitionLoader.Reference<WizardDef> definition )
    {
        super( type, definition );
    }
    
    public CreateWorkspaceFileWizard( final M element,
                                      final DefinitionLoader.Reference<WizardDef> definition )
    {
        super( element, definition );
    }
    
    public CreateWorkspaceFileWizard()
    {
        super();
    }
    
    @Override
    protected void init( final ElementType type,
                         final DefinitionLoader.Reference<WizardDef> definition )
    {
        if( type == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( ! CreateWorkspaceFileOp.class.isAssignableFrom( type.getModelElementClass() ) )
        {
            throw new IllegalArgumentException();
        }
        
        super.init( type, definition );
    }
    
    public void init( final IWorkbench workbench,
                      final IStructuredSelection selection )
    {
        if( ! selection.isEmpty() )
        {
            final Object obj = selection.getFirstElement();
            IResource resource = null;
            
            if( obj instanceof IResource )
            {
                resource = (IResource) obj;
            }
            else
            {
                resource = (IResource) Platform.getAdapterManager().getAdapter( obj, IResource.class );
            }
            
            if( resource != null )
            {
                final CreateWorkspaceFileOp op = element();
                op.setContext( resource );
                op.initialize();
            }
        }
    }
    
    public void setInitializationData( final IConfigurationElement config,
                                       final String propertyName,
                                       final Object data )
    {
        if( definition() == null )
        {
            final String bundleId = config.getContributor().getName();
            final Context context = BundleBasedContext.adapt( bundleId );
            final Map<?,?> properties = (Map<?,?>) data;
    
            final String sdef = (String) properties.get( "sdef" );
            final DefinitionLoader.Reference<WizardDef> definition = DefinitionLoader.context( context ).sdef( sdef ).wizard();
            
            final JavaType operationJavaType = definition.resolve().getElementType().target();
            final ElementType operationElementType = ElementType.read( (Class<?>) operationJavaType.artifact(), true );
    
            init( operationElementType, definition );
            
            this.editor = (String) properties.get( "editor" );
        }
    }

    @Override
    protected void performPostFinish() 
    {
        openFileEditor( element().getFile().target(), editor() );
    }
    
    protected String editor()
    {
        return this.editor;
    }
    
}
