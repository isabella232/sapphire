/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [329102] excess scroll space in editor sections
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms;

import static org.eclipse.sapphire.modeling.util.MiscUtil.readTextResource;

import org.eclipse.sapphire.modeling.el.ConcatFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.def.HtmlContentSourceType;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.ui.forms.swt.internal.HtmlPanelPresentation;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class HtmlPanelPart extends FormComponentPart
{
    private static final String FRAGMENT_STYLE = readTextResource( HtmlPanelPart.class, "FragmentStyle.css" );
    private static final String FRAGMENT_HEADER = "<html><head><style>" + FRAGMENT_STYLE + "</style></head><body>";
    private static final String FRAGMENT_FOOTER = "</body></html>";
    
    private HtmlPanelDef def;
    private FunctionResult contentFunctionResult;
    private FunctionResult urlFunctionResult;
    
    @Override
    protected void init()
    {
        super.init();
        
        this.def = (HtmlPanelDef) this.definition;

        if( this.def.getContentSourceType().content() == HtmlContentSourceType.EMBEDDED )
        {
            Function contentFunction = this.def.getContent().content();
            
            if( this.def.getFragment().content() == true )
            {
                contentFunction = ConcatFunction.create( FRAGMENT_HEADER, contentFunction );
                contentFunction = ConcatFunction.create( contentFunction, FRAGMENT_FOOTER );
            }
    
            this.contentFunctionResult = initExpression
            (
                contentFunction, 
                String.class,
                null,
                new Runnable()
                {
                    public void run()
                    {
                        broadcast( new ContentEvent( HtmlPanelPart.this ) );
                    }
                }
            );
        }
        else
        {
            this.urlFunctionResult = initExpression
            (
                this.def.getContentUrl().content(), 
                String.class,
                null,
                new Runnable()
                {
                    public void run()
                    {
                        broadcast( new ContentEvent( HtmlPanelPart.this ) );
                    }
                }
            );
        }
    }
    
    @Override
    public HtmlPanelDef definition()
    {
        return (HtmlPanelDef) super.definition();
    }
    
    public String content()
    {
        if( this.contentFunctionResult == null )
        {
            return null;
        }
        
        return (String) this.contentFunctionResult.value();
    }
    
    public String url()
    {
        if( this.urlFunctionResult == null )
        {
            return null;
        }
        
        return (String) this.urlFunctionResult.value();
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.contentFunctionResult != null )
        {
            this.contentFunctionResult.dispose();
        }
        
        if( this.urlFunctionResult != null )
        {
            this.urlFunctionResult.dispose();
        }
    }
    
    @Override
    public FormComponentPresentation createPresentation( final SwtPresentation parent, final Composite composite )
    {
        return new HtmlPanelPresentation( this, parent, composite );
    }

    public static final class ContentEvent extends PartEvent
    {
        public ContentEvent( final SapphirePart part )
        {
            super( part );
        }
    }

}
