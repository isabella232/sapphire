/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms;

import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.ui.forms.swt.internal.TextPresentation;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TextPart extends FormComponentPart
{
    private FunctionResult contentFunctionResult;
    
    @Override
    protected void init()
    {
        this.contentFunctionResult = initExpression
        (
            definition().getText().content(), 
            String.class,
            null,
            new Runnable()
            {
                public void run()
                {
                    broadcast( new ContentEvent() );
                }
            }
        );
    }
    
    @Override
    public TextDef definition()
    {
        return (TextDef) super.definition();
    }
    
    public String content()
    {
        return (String) this.contentFunctionResult.value();
    }

    public FormComponentPresentation createPresentation( final SwtPresentation parent, final Composite composite )
    {
        return new TextPresentation( this, parent, composite );
    }
    
    public final class ContentEvent extends PartEvent
    {
        public ContentEvent()
        {
            super( TextPart.this );
        }

        @Override
        public TextPart part()
        {
            return (TextPart) super.part();
        }
    }

}
