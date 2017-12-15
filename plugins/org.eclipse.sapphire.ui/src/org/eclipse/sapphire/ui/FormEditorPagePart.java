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

package org.eclipse.sapphire.ui;

import java.util.List;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.def.FormEditorPageDef;
import org.eclipse.sapphire.ui.def.PartDef;
import org.eclipse.sapphire.util.ListFactory;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a> 
 */

public final class FormEditorPagePart extends SapphireEditorPagePart
{
    private List<SapphirePart> childParts;
    
    @Override
    public FormEditorPageDef definition()
    {
        return (FormEditorPageDef) super.definition();
    }

    @Override
    protected void init()
    {
        super.init();

        final IModelElement element = getLocalModelElement();
        final ListFactory<SapphirePart> childPartsListFactory = ListFactory.start();
        
        final Listener childPartListener = new FilteredListener<PartValidationEvent>()
        {
            @Override
            protected void handleTypedEvent( PartValidationEvent event )
            {
                refreshValidation();
            }
        };
        
        for( PartDef childPartDef : definition().getContent() )
        {
            final SapphirePart part = create( this, element, childPartDef, this.params );
            part.attach( childPartListener );
            childPartsListFactory.add( part );
        }
        
        this.childParts = childPartsListFactory.result();
   }
    
    public List<? extends SapphirePart> getChildParts()
    {
        return this.childParts;
    }
    
    @Override
    protected Status computeValidation()
    {
        final Status.CompositeStatusFactory factory = Status.factoryForComposite();

        for( SapphirePart child : getChildParts() )
        {
            factory.merge( child.validation() );
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
