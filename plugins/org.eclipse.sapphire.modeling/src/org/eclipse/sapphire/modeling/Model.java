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

package org.eclipse.sapphire.modeling;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class Model

    extends ModelElement
    implements IModel
    
{
    private ModelStore modelStore;
    private CorruptedModelStoreExceptionInterceptor corruptedModelStoreExceptionInterceptor;
    private ValidateEditPolicy validateEditPolicy;
    private Set<ValidateEditListener> validateEditListeners;
    
    public Model( final ModelElementType type,
                  final ModelStore modelStore )
    {
        super( type, null, null );
        
        this.modelStore = modelStore;
        this.corruptedModelStoreExceptionInterceptor = null;
        this.validateEditPolicy = ValidateEditPolicy.ON_EVERY_CHANGE;
        this.validateEditListeners = null;
        
        try
        {
            this.modelStore.open();
        }
        catch( IOException e )
        {
            SapphireModelingFrameworkPlugin.log( e );
        }
    }
    
    public ModelStore getModelStore()
    {
        return this.modelStore;
    }
    
    public File getFile()
    {
        if( this.modelStore instanceof IFileModelStore )
        {
            return ( (IFileModelStore) this.modelStore ).getFile();
        }
        
        return null;
    }
    
    public IFile getEclipseFile()
    {
        if( this.modelStore instanceof IEclipseFileModelStore )
        {
            return ( (IEclipseFileModelStore) this.modelStore ).getEclipseFile();
        }
        
        return null;
    }
    
    public IProject getEclipseProject()
    {
        final IFile file = getEclipseFile();
        return ( file == null ? null : file.getProject() );
    }

    @Override
    public final void validateEdit()
    {
        validateEdit( false );
    }

    private void validateEdit( final boolean saveMethodContext )
    {
        synchronized( this )
        {
            if( this.validateEditPolicy == ValidateEditPolicy.ON_EVERY_CHANGE ||
                ( this.validateEditPolicy == ValidateEditPolicy.ON_SAVE && saveMethodContext == true ) )
            {
                if( this.validateEditListeners != null )
                {
                    for( ValidateEditListener listener : this.validateEditListeners )
                    {
                        if( listener.validateEdit( this ) == false )
                        {
                            throw new ValidateEditException();
                        }
                    }
                }
                
                if( this.modelStore.validateEdit() == false )
                {
                    throw new ValidateEditException();
                }
            }
        }
    }
    
    public final void addValidateEditListener( final ValidateEditListener listener )
    {
        synchronized( this )
        {
            if( this.validateEditListeners == null )
            {
                this.validateEditListeners = new CopyOnWriteArraySet<ValidateEditListener>();
            }
            
            this.validateEditListeners.add( listener );
        }
    }

    public final void removeValidateEditListener( final ValidateEditListener listener )
    {
        synchronized( this )
        {
            if( this.validateEditListeners != null )
            {
                this.validateEditListeners.remove( listener );
            }
        }
    }
    
    public final ValidateEditPolicy getValidateEditPolicy()
    {
        synchronized( this )
        {
            return this.validateEditPolicy;
        }
    }
    
    public final void setValidateEditPolicy( final ValidateEditPolicy policy )
    {
        synchronized( this )
        {
            this.validateEditPolicy = policy;
        }
    }

    public boolean isCorrupted()
    {
        return false;
    }
    
    protected final CorruptedModelStoreExceptionInterceptor getCorruptedModelStoreExceptionInterceptor()
    {
        return this.corruptedModelStoreExceptionInterceptor;
    }

    public final void setCorruptedModelStoreExceptionInterceptor( final CorruptedModelStoreExceptionInterceptor interceptor )
    {
        this.corruptedModelStoreExceptionInterceptor = interceptor;
    }
    
    public void save()
    
        throws IOException
        
    {
        validateEdit( true );
        this.modelStore.save();
    }
    
}
