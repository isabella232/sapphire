/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt.internal;

import static org.eclipse.sapphire.modeling.util.MiscUtil.EMPTY_STRING;
import static org.eclipse.sapphire.modeling.util.MiscUtil.equal;

import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.services.ValueNormalizationService;
import org.eclipse.sapphire.ui.DelayedTasksExecutor;
import org.eclipse.sapphire.ui.forms.swt.AbstractBinding;
import org.eclipse.sapphire.ui.forms.swt.PropertyEditorPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class TextFieldBinding extends AbstractBinding
{
    private Text text;
    private String textContent;
    private ValueNormalizationService valueNormalizationService;
    private DelayedTasksExecutor.Task onTextContentModifyTask;
    
    public TextFieldBinding( final PropertyEditorPresentation propertyEditorPresentation,
                             final Text text )
    {
        super( propertyEditorPresentation, text );
    }

    @Override
    protected void initialize( final PropertyEditorPresentation propertyEditorPresentation,
                               final Control control )
    {
        super.initialize( propertyEditorPresentation, control );

        this.onTextContentModifyTask = new DelayedTasksExecutor.Task()
        {
            public int getPriority()
            {
                return 100;
            }
            
            public void run()
            {
                updateModel();
                updateTargetAttributes();
            }
        };
        
        this.text = (Text) control;
        
        this.text.addModifyListener
        (
            new ModifyListener()
            {
                public void modifyText( final ModifyEvent event )
                {
                    updateTextContent( TextFieldBinding.this.text.getText() );
                }
            }
        );
        
        this.valueNormalizationService = property().service( ValueNormalizationService.class );
    }
    
    @Override
    public Value<?> property()
    {
        return (Value<?>) super.property();
    }
    
    protected void updateTextContent( final String textContent )
    {
        this.textContent = textContent;
        DelayedTasksExecutor.schedule( this.onTextContentModifyTask );
    }

    @Override
    protected void doUpdateModel()
    {
        if( ! this.text.isDisposed() && ( this.text.getStyle() & SWT.READ_ONLY ) == 0 ) 
        {
            property().write( this.textContent );
        }
    }
    
    @Override
    protected void doUpdateTarget()
    {
        final String oldValue = this.valueNormalizationService.normalize( this.text.getText() );
        final String newValue = this.valueNormalizationService.normalize( ( (Value<?>) property() ).text( false ) );
        
        if( ! equal( oldValue, newValue ) )
        {
            this.text.setText( newValue == null ? EMPTY_STRING : newValue );
        }
    }
    
}
