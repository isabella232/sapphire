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
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.util.internal.MiscUtil;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class RadioButtonsGroupBinding 

    extends AbstractEnumBinding
    
{
    private RadioButtonsGroup buttonsGroup;
    private Button badValueButton;
    
    public RadioButtonsGroupBinding( final IModelElement modelElement,
                                     final ValueProperty property,
                                     final SapphireRenderingContext context,
                                     final RadioButtonsGroup buttonsGroup )
    {
        super( modelElement, property, context, buttonsGroup );
    }
    
    @Override
    
    protected void initialize( final IModelElement modelElement,
                               final ModelProperty property,
                               final SapphireRenderingContext context,
                               final Control control )
    {
        super.initialize( modelElement, property, context, control );
        
        this.buttonsGroup = (RadioButtonsGroup) control;
        
        final EnumValueType enumValueType = new EnumValueType( this.enumValues[ 0 ].getDeclaringClass() );

        for( Enum<?> enumItem : this.enumValues )
        {
            final Button button = this.buttonsGroup.addRadioButton( enumValueType.getLabel( enumItem, false, CapitalizationType.FIRST_WORD_ONLY, true ) );
            button.setData( enumItem );
        }
        
        this.buttonsGroup.addSelectionListener
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
        return this.buttonsGroup.getSelectionIndex();
    }

    @Override
    
    protected void setSelectionIndex( final int index )
    {
        this.buttonsGroup.setSelectionIndex( index );
    }
    
    @Override
    
    protected void createMalformedItem( String label )
    {
        if( this.badValueButton == null )
        {
            this.badValueButton = this.buttonsGroup.addRadioButton( MiscUtil.EMPTY_STRING );
        }
        
        this.badValueButton.setText( label );
        getContext().layout();
    }

    @Override
    
    protected void removeMalformedItem()
    {
        if( this.badValueButton != null )
        {
            this.badValueButton.dispose();
            this.badValueButton = null;
            getContext().layout();
        }
    }

}
