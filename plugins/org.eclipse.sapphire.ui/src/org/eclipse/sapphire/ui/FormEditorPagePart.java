/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.def.FormEditorPageDef;
import org.eclipse.sapphire.ui.def.ISapphirePartDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a> 
 */

public final class FormEditorPagePart extends SapphireEditorPagePart
{
    private List<SapphirePart> childParts;
    
    @Override
    public FormEditorPageDef getDefinition()
    {
        return (FormEditorPageDef) super.getDefinition();
    }

    @Override
    protected void init()
    {
        super.init();

        final IModelElement element = getLocalModelElement();

        final SapphirePartListener childPartListener = new SapphirePartListener()
        {
            @Override
            public void handleValidateStateChange( final Status oldValidateState,
                                                   final Status newValidationState )
            {
                updateValidationState();
            }
        };
        
        this.childParts = new ArrayList<SapphirePart>();
        
        for( ISapphirePartDef childPartDef : getDefinition().getContent() )
        {
            final SapphirePart childPart = create( this, element, childPartDef, this.params );
            childPart.addListener( childPartListener );
            this.childParts.add( childPart );
        }
        
        this.childParts = Collections.unmodifiableList( this.childParts );
        
        updateValidationState();
    }
    
    public List<? extends SapphirePart> getChildParts()
    {
        return this.childParts;
    }
    
    @Override
    protected Status computeValidationState()
    {
        final Status.CompositeStatusFactory factory = Status.factoryForComposite();

        for( SapphirePart child : getChildParts() )
        {
            factory.add( child.getValidationState() );
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
