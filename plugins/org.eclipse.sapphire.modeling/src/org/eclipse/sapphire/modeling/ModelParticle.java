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

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ModelParticle

    implements IModelParticle
    
{
    protected final IModel model;
    private final IModelParticle parent;
    
    public ModelParticle( final IModelParticle parent )
    {
        this.parent = parent;

        if( this.parent == null )
        {
            if( this instanceof IModel )
            {
                this.model = (IModel) this;
            }
            else
            {
                throw new IllegalArgumentException();
            }
        }
        else
        {
            this.model = this.parent.getModel();
        }
    }
    
    public final IModel getModel()
    {
        return this.model;
    }

    public IModelParticle getParent()
    {
        return this.parent;
    }
    
    @SuppressWarnings( "unchecked" )
    public final <T> T findNearestParticle( final Class<T> particleType )
    {
        if( particleType.isAssignableFrom( getClass() ) )
        {
            return (T) this;
        }
        else
        {
            if( this.parent != null )
            {
                return this.parent.findNearestParticle( particleType );
            }
            else
            {
                return null;
            }
        }
    }

}
