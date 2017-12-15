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

package org.eclipse.sapphire.ui.internal.binding;

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.EnumValueType;
import org.eclipse.sapphire.ui.SapphirePropertyEditor;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class ComboBinding 

    extends AbstractEnumBinding
    
{
    private Combo combo;
    
    public ComboBinding( final SapphirePropertyEditor editor,
                         final SapphireRenderingContext context,
                         final Combo combo )
    {
        super( editor, context, combo );
    }
    
    @Override
    protected void initialize( final SapphirePropertyEditor editor,
                               final SapphireRenderingContext context,
                               final Control control )
    {
        super.initialize( editor, context, control );

        this.combo = (Combo) control;
        
        final EnumValueType enumValueType = new EnumValueType( this.enumValues[ 0 ].getDeclaringClass() );
        
        for( Enum<?> enumItem : this.enumValues )
        {
            final String enumItemLabel = enumValueType.getLabel( enumItem, false, CapitalizationType.FIRST_WORD_ONLY, false );
            this.combo.add( enumItemLabel );
        }

        this.combo.addSelectionListener
        (
            new SelectionAdapter()
            {
                public void widgetSelected( final SelectionEvent event )
                {
                    updateModel();
                    updateTargetAttributes();
                }
            }
        );
    }
    
    @Override
    
    protected int getSelectionIndex()
    {
        return this.combo.getSelectionIndex();
    }

    @Override
    
    protected void setSelectionIndex( final int selection )
    {
        this.combo.select( selection );
    }
    
    @Override
    
    protected void createMalformedItem( final String label )
    {
        if( this.combo.getItemCount() > this.enumValues.length )
        {
            this.combo.setItem( this.enumValues.length, label );
        }
        else
        {
            this.combo.add( label );
        }
    }

    @Override
    
    protected void removeMalformedItem()
    {
        if( this.combo.getItemCount() > this.enumValues.length )
        {
            this.combo.remove( this.enumValues.length );
        }
    }
    
}
