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

package org.eclipse.sapphire.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.def.FormDef;
import org.eclipse.sapphire.ui.def.PartDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class SapphirePartContainer extends FormPart
{
    private List<SapphirePart> childParts;
    
    @Override
    protected void init()
    {
        super.init();

        final List<SapphirePart> childPartsFromInit = initChildParts();
        this.childParts = Collections.unmodifiableList( new ArrayList<SapphirePart>( childPartsFromInit ) );

        final Listener childPartListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                if( event instanceof ValidationChangedEvent )
                {
                    updateValidationState();
                }
            }
        };
        
        for( SapphirePart childPart : this.childParts )
        {
            childPart.attach( childPartListener );
        }
        
        updateValidationState();
    }
    
    protected List<SapphirePart> initChildParts()
    {
        final IModelElement element = getLocalModelElement();
        final FormDef def = (FormDef) this.definition;
        final List<SapphirePart> childParts = new ArrayList<SapphirePart>();
        
        for( PartDef childPartDef : def.getContent() )
        {
            final SapphirePart childPart = create( this, element, childPartDef, this.params );
            childParts.add( childPart );
        }
        
        return childParts;
    }
    
    public List<? extends SapphirePart> getChildParts()
    {
        return this.childParts;
    }
    
    public void render( final SapphireRenderingContext context )
    {
        for( SapphirePart child : getChildParts() )
        {
            child.render( context );
        }
    }
    
    @Override
    protected Status computeValidationState()
    {
        final Status.CompositeStatusFactory factory = Status.factoryForComposite();

        for( SapphirePart child : getChildParts() )
        {
            factory.merge( child.getValidationState() );
        }
        
        return factory.create();
    }
    
    @Override
    public boolean setFocus()
    {
        for( SapphirePart child : getChildParts() )
        {
            if( child.setFocus() == true )
            {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean setFocus( final ModelPath path )
    {
        for( SapphirePart child : getChildParts() )
        {
            if( child.setFocus( path ) == true )
            {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        for( SapphirePart child : getChildParts() )
        {
            child.dispose();
        }
    }

}
