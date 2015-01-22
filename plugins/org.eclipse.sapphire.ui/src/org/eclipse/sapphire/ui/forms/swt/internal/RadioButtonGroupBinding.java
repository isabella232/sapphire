/******************************************************************************
 * Copyright (c) 2015 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [418602] Radio buttons property editor should show images
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt.internal;

import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.MasterConversionService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.EnumValueType;
import org.eclipse.sapphire.modeling.util.MiscUtil;
import org.eclipse.sapphire.services.ValueImageService;
import org.eclipse.sapphire.ui.forms.PropertyEditorDef;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.swt.AbstractBinding;
import org.eclipse.sapphire.ui.forms.swt.PropertyEditorPresentation;
import org.eclipse.sapphire.ui.forms.swt.RadioButtonsGroup;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public final class RadioButtonGroupBinding extends AbstractBinding
{
    @Text( "<value not set>" )
    private static LocalizableText nullValueLabel;
    
    static
    {
        LocalizableText.init( RadioButtonGroupBinding.class );
    }

    private RadioButtonsGroup buttonsGroup;
    private Button badValueButton;
    private Enum<?>[] enumValues;
    
    public RadioButtonGroupBinding( final PropertyEditorPresentation propertyEditorPresentation,
                                     final RadioButtonsGroup buttonsGroup )
    {
        super( propertyEditorPresentation, buttonsGroup );
    }
    
    @Override
    
    protected void initialize( final PropertyEditorPresentation propertyEditorPresentation,
                               final Control control )
    {
        super.initialize( propertyEditorPresentation, control );
        
        this.enumValues = (Enum<?>[]) property().definition().getTypeClass().getEnumConstants();

        this.buttonsGroup = (RadioButtonsGroup) control;
        
        final PropertyEditorPart part = propertyEditorPresentation.part();
        final Property property = part.property();
        final EnumValueType enumValueType = new EnumValueType( this.enumValues[ 0 ].getDeclaringClass() );

        for( final Enum<?> enumItem : this.enumValues )
        {
            final String enumItemStr = property.service( MasterConversionService.class ).convert( enumItem, String.class );
            final String auxText = part.getRenderingHint( PropertyEditorDef.HINT_AUX_TEXT + "." + enumItemStr, null );
            final ValueImageService imageService = property.service( ValueImageService.class );
            final ImageData imageData = imageService.provide( enumItemStr );
            final Image image = propertyEditorPresentation.resources().image( imageData );
            final Button button = this.buttonsGroup.addRadioButton( enumValueType.getLabel( enumItem, false, CapitalizationType.FIRST_WORD_ONLY, true ), auxText, image );
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
    
    private int getSelectionIndex()
    {
        return this.buttonsGroup.getSelectionIndex();
    }

    private void setSelectionIndex( final int index )
    {
        this.buttonsGroup.setSelectionIndex( index );
    }
    
    private void createMalformedItem( String label )
    {
        if( this.badValueButton == null )
        {
            this.badValueButton = this.buttonsGroup.addRadioButton( MiscUtil.EMPTY_STRING );
        }
        
        this.badValueButton.setText( label );
        presentation().layout();
    }

    private void removeMalformedItem()
    {
        if( ! this.buttonsGroup.isDisposed() )
        {
            if( this.badValueButton != null )
            {
                this.badValueButton.dispose();
                this.badValueButton = null;
                presentation().layout();
            }
        }
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Value<Enum<?>> property()
    {
        return (Value<Enum<?>>) super.property();
    }

    @Override
    protected final void doUpdateModel()
    {
        final int index = getSelectionIndex();
        
        if( index >= 0 && index < this.enumValues.length )
        {
            property().write( this.enumValues[ index ], true );
            removeMalformedItem();
        }
    }

    @Override
    protected final void doUpdateTarget()
    {
        final int existingSelection = getSelectionIndex();
        final Value<Enum<?>> value = property();
    
        int newSelection = this.enumValues.length;
        
        if( ! value.malformed() )
        {
            final Enum<?> newValueEnum = value.content( true );
            
            for( int i = 0, n = this.enumValues.length; i < n; i++ )
            {
                if( this.enumValues[ i ] == newValueEnum )
                {
                    newSelection = i;
                    break;
                }
            }
        }
        
        if( newSelection == this.enumValues.length )
        {
            final String newValueString = value.text( true );
            final String label = ( newValueString == null ? nullValueLabel.text() : newValueString );
    
            createMalformedItem( label );
        }
        else
        {
            removeMalformedItem();
        }
        
        if( existingSelection != newSelection )
        {
            setSelectionIndex( newSelection );
        }
    }

}
