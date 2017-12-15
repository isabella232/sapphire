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

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.util.internal.MiscUtil;
import org.eclipse.sapphire.ui.DelayedTasksExecutor;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class TextFieldBinding 

    extends AbstractBinding
    
{
    private Text text;
    private String textContent;
    private DelayedTasksExecutor.Task onTextContentModifyTask;
    
    public TextFieldBinding( final IModelElement modelElement,
                             final ValueProperty property,
                             final SapphireRenderingContext context,
                             final Text text )
    {
        super( modelElement, property, context, text );
    }

    @Override
    protected void initialize( final IModelElement modelElement,
                               final ModelProperty property,
                               final SapphireRenderingContext context,
                               final Control control )
    {
        super.initialize( modelElement, property, context, control );

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
            ( (ValueProperty) getProperty() ).invokeSetterMethod( getModelElement(), this.textContent );
        }
    }
    
    @Override
    protected void doUpdateTarget()
    {
        final Value<?> value = (Value<?>) getPropertyValue();
        final String existingValue = this.text.getText();
        
        if( value == null )
        {
            if( ! existingValue.equals( MiscUtil.EMPTY_STRING ) )
            {
                this.text.setText( MiscUtil.EMPTY_STRING );
            }
        }
        else
        {
            final String newValue = value.getText( false );
            
            if( ! existingValue.equals( newValue ) )
            {
                this.text.setText( newValue == null ? MiscUtil.EMPTY_STRING : newValue );
            }
        }
    }
    
}
