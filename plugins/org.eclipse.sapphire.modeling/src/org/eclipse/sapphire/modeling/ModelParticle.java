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

package org.eclipse.sapphire.modeling;

import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.localization.SourceLanguageLocalizationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ModelParticle

    implements IModelParticle
    
{
    private final IModelParticle parent;
    private final Resource resource;
    
    public ModelParticle( final IModelParticle parent,
                          final Resource resource )
    {
        this.parent = parent;
        this.resource = resource;
    }
    
    public final Resource resource()
    {
        return this.resource;
    }
    
    public final IModelParticle root()
    {
        if( this.parent == null )
        {
            return this;
        }
        
        return this.parent.root();
    }
    
    public IModelParticle parent()
    {
        return this.parent;
    }
    
    public final <T> T nearest( final Class<T> particleType )
    {
        if( particleType.isAssignableFrom( getClass() ) )
        {
            return particleType.cast( this );
        }
        else
        {
            if( this.parent != null )
            {
                return this.parent.nearest( particleType );
            }
            else
            {
                return null;
            }
        }
    }
    
    public <A> A adapt( final Class<A> adapterType )
    {
        A result = null;
        
        if( this.resource != null )
        {
            result = this.resource.adapt( adapterType );
        }

        if( result == null && this.parent != null )
        {
            result = this.parent.adapt( adapterType );
        }
        
        if( result == null && adapterType == LocalizationService.class )
        {
            result = adapterType.cast( SourceLanguageLocalizationService.INSTANCE );
        }
        
        return result;
    }

}
