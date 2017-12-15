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

package org.eclipse.sapphire.ui.internal.binding;

import static org.eclipse.sapphire.modeling.util.MiscUtil.EMPTY_STRING;
import static org.eclipse.sapphire.modeling.util.MiscUtil.equal;

import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.services.ValueNormalizationService;
import org.eclipse.sapphire.ui.DelayedTasksExecutor;
import org.eclipse.sapphire.ui.PropertyEditorPart;
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
    private ValueNormalizationService valueNormalizationService;
    private DelayedTasksExecutor.Task onTextContentModifyTask;
    
    public TextFieldBinding( final PropertyEditorPart editor,
                             final SapphireRenderingContext context,
                             final Text text )
    {
        super( editor, context, text );
    }

    @Override
    protected void initialize( final PropertyEditorPart editor,
                               final SapphireRenderingContext context,
                               final Control control )
    {
        super.initialize( editor, context, control );

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
        
        this.valueNormalizationService = editor.getLocalModelElement().service( editor.getProperty(), ValueNormalizationService.class );
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
            getModelElement().write( getProperty(), this.textContent );
        }
    }
    
    @Override
    protected void doUpdateTarget()
    {
        final String oldValue = this.valueNormalizationService.normalize( this.text.getText() );
        final String newValue = this.valueNormalizationService.normalize( ( (Value<?>) getPropertyValue() ).getText( false ) );
        
        if( ! equal( oldValue, newValue ) )
        {
            this.text.setText( newValue == null ? EMPTY_STRING : newValue );
        }
    }
    
}
