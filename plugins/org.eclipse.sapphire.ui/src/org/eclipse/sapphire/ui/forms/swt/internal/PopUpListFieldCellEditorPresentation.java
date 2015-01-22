/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - Bug 328777 Table cell editor overlaps neighboring field
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt.internal;

import java.util.List;

import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.services.ValueNormalizationService;
import org.eclipse.sapphire.ui.forms.swt.TablePropertyEditorPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PopUpListFieldCellEditorPresentation extends ComboBoxCellEditor
{
    private final StructuredViewer viewer;
    private final TablePropertyEditorPresentation.SelectionProvider selectionProvider;
    private final Property property;
    private List<PossibleValue> possibleValues;
    private boolean isDefaultValue;
    private CCombo combo;
    private boolean disableFocusLostHandler;
    private ISelection selectionPriorToActivation;
    
    public PopUpListFieldCellEditorPresentation( final StructuredViewer parent,
                                                 final TablePropertyEditorPresentation.SelectionProvider selectionProvider,
                                                 final Property property,
                                                 final PopUpListFieldStyle popUpListFieldStyle,
                                                 final int style )
    {
        super();
        
        this.viewer = parent;
        this.selectionProvider = selectionProvider;
        this.property = property;
        
        setStyle( style | ( popUpListFieldStyle == PopUpListFieldStyle.STRICT ? SWT.READ_ONLY : SWT.NONE ) );
        create( (Composite) parent.getControl() );
        
        this.combo = (CCombo) getControl();
        
        this.combo.addSelectionListener
        (
             new SelectionAdapter()
             {
                @Override
                public void widgetSelected( final SelectionEvent event )
                {
                    PopUpListFieldCellEditorPresentation.this.isDefaultValue = false;
                }
             }
        );
        
        this.combo.addModifyListener
        (
            new ModifyListener()
            {
                public void modifyText( final ModifyEvent event )
                {
                    PopUpListFieldCellEditorPresentation.this.isDefaultValue = false;
                }
            }
        );
        
        this.possibleValues = PossibleValue.factory( property ).entries();
        final String[] contentForCombo = new String[ this.possibleValues.size() ];
        
        for( int i = 0, n = this.possibleValues.size(); i < n; i++ )
        {
            contentForCombo[ i ] = this.possibleValues.get( i ).label();
        }
        
        setItems( contentForCombo );
    }
    
    @Override
    protected Object doGetValue()
    {
        if( this.isDefaultValue )
        {
            return null;
        }
        else
        {
            final int index = this.combo.getSelectionIndex();
            
            if( index == -1 )
            {
                final ValueNormalizationService valueNormalizationService = this.property.service( ValueNormalizationService.class );
                final String value = valueNormalizationService.normalize( this.combo.getText() );
                
                if( value.length() > 0 )
                {
                    return value;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                return this.possibleValues.get( index ).value();
            }
        }
    }

    @Override
    protected void doSetValue( final Object value )
    {
        final Value<?> val = (Value<?>) value;
        
        final ValueNormalizationService valueNormalizationService = this.property.service( ValueNormalizationService.class );
        
        final String text = valueNormalizationService.normalize( val.text() );
        int index = -1;
        
        for( int i = 0, n = this.possibleValues.size(); index == -1 && i < n; i++ )
        {
            if( this.possibleValues.get( i ).value().equals( text ) )
            {
                index = i;
            }
        }
        
        if( index == -1 )
        {
            if( text != null )
            {
                this.combo.setText( text );
            }
        }
        else
        {
            this.combo.select( index );
        }
        
        if( val.text( false ) == null && val.getDefaultContent() != null )
        {
            this.isDefaultValue = true;
        }
        else
        {
            this.isDefaultValue = false;
        }
    }
    
    @Override
    public void activate()
    {
        this.selectionPriorToActivation = this.viewer.getSelection();
        
        if( this.selectionProvider != null )
        {
            this.selectionProvider.setFakeSelection( this.selectionPriorToActivation );
        }
        
        this.viewer.setSelection( StructuredSelection.EMPTY );
        
        super.activate();
    }

    @Override
    protected void focusLost()
    {
        if( ! this.disableFocusLostHandler )
        {
            this.disableFocusLostHandler = true;
            super.focusLost();
            this.disableFocusLostHandler = false;
            
            this.viewer.setSelection( this.selectionPriorToActivation );

            if( this.selectionProvider != null )
            {
                this.selectionProvider.setFakeSelection( null );
            }
            
            this.selectionPriorToActivation = null;
        }
    }
    
}

