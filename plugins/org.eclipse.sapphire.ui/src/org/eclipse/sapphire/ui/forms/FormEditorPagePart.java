/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms;

import java.util.List;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.PartValidationEvent;
import org.eclipse.sapphire.ui.SapphireEditorPagePart;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.def.PartDef;
import org.eclipse.sapphire.util.ListFactory;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a> 
 */

public final class FormEditorPagePart extends SapphireEditorPagePart
{
    private List<FormComponentPart> childParts;
    
    @Override
    public FormEditorPageDef definition()
    {
        return (FormEditorPageDef) super.definition();
    }

    @Override
    protected void init()
    {
        super.init();

        final Element element = getLocalModelElement();
        final ListFactory<FormComponentPart> childPartsListFactory = ListFactory.start();
        
        final Listener childPartListener = new FilteredListener<PartValidationEvent>()
        {
            @Override
            protected void handleTypedEvent( PartValidationEvent event )
            {
                refreshValidation();
            }
        };
        
        for( final PartDef childPartDef : definition().getContent() )
        {
            final FormComponentPart part = (FormComponentPart) create( this, element, childPartDef, this.params );
            part.attach( childPartListener );
            childPartsListFactory.add( part );
        }
        
        this.childParts = childPartsListFactory.result();
   }
    
    public List<FormComponentPart> getChildParts()
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
