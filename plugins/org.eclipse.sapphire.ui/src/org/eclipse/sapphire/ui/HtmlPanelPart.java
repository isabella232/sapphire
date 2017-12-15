/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [329102] excess scroll space in editor sections
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.modeling.util.MiscUtil.readTextResource;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhhint;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdwhint;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.el.ConcatFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.ui.def.HtmlContentSourceType;
import org.eclipse.sapphire.ui.def.HtmlPanelDef;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.sapphire.ui.swt.renderer.internal.formtext.SapphireFormText;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class HtmlPanelPart extends FormPart
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
        
        final IModelElement element = getModelElement();
        this.def = (HtmlPanelDef) this.definition;

        if( this.def.getContentSourceType().getContent() == HtmlContentSourceType.EMBEDDED )
        {
            Function contentFunction = this.def.getContent().getContent();
            
            if( this.def.getFragment().getContent() == true )
            {
                contentFunction = ConcatFunction.create( FRAGMENT_HEADER, contentFunction );
                contentFunction = ConcatFunction.create( contentFunction, FRAGMENT_FOOTER );
            }
    
            this.contentFunctionResult = initExpression
            (
                element,
                contentFunction, 
                String.class,
                null,
                new Runnable()
                {
                    public void run()
                    {
                        notifyContentChangeEventListeners();
                    }
                }
            );
        }
        else
        {
            this.urlFunctionResult = initExpression
            (
                element,
                this.def.getContentUrl().getContent(), 
                String.class,
                null,
                new Runnable()
                {
                    public void run()
                    {
                        notifyContentChangeEventListeners();
                    }
                }
            );
        }
    }
    
    public String getContent()
    {
        if( this.contentFunctionResult == null )
        {
            return null;
        }
        
        return (String) this.contentFunctionResult.value();
    }
    
    public String getContentUrl()
    {
        if( this.urlFunctionResult == null )
        {
            return null;
        }
        
        return (String) this.urlFunctionResult.value();
    }
    
    public boolean getShowBorder()
    {
        return this.def.getShowBorder().getContent();
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
    
    private void notifyContentChangeEventListeners()
    {
        final SapphirePartEvent event = new SapphirePartEvent( this );
        
        for( SapphirePartListener listener : getListeners() )
        {
            if( listener instanceof Listener )
            {
                try
                {
                    ( (Listener) listener ).handleContentChangeEvent( event );
                }
                catch( Exception e )
                {
                    SapphireUiFrameworkPlugin.log( e );
                }
            }
        }
    }
    
    public static abstract class Listener extends SapphirePartListener
    {
        public void handleContentChangeEvent( final SapphirePartEvent event )
        {
            // The default implementation doesn't do anything.
        }
    }
    
    @Override
    public void render( final SapphireRenderingContext context )
    {
        final boolean scaleVertically = getScaleVertically();
        GridData gd = gdhindent( gdwhint( gdhspan( ( scaleVertically ? gdfill() : gdhfill() ), 2 ), 100 ), 9 );
        
        if( ! scaleVertically )
        {
            gd = gdhhint( gd, this.def.getHeight().getContent() );
        }
        
        final int style = ( getShowBorder() == true ? SWT.BORDER : SWT.NONE );
        
        try
        {
            final Browser browser = new Browser( context.getComposite(), style );
            browser.setLayoutData( gd );
            
            final Listener listener;
            
            if( this.def.getContentSourceType().getContent() == HtmlContentSourceType.EMBEDDED )
            {
                browser.setText( getContent() );
                
                listener = new Listener()
                {
                    @Override
                    public void handleContentChangeEvent( final SapphirePartEvent event )
                    {
                        browser.setText( getContent() );
                    }
                };
            }
            else
            {
                browser.setUrl( getContentUrl() );
                
                listener = new Listener()
                {
                    @Override
                    public void handleContentChangeEvent( final SapphirePartEvent event )
                    {
                        browser.setUrl( getContentUrl() );
                    }
                };
            }
            
            addListener( listener );
            
            browser.addDisposeListener
            (
                new DisposeListener()
                {
                    public void widgetDisposed( final DisposeEvent event )
                    {
                        removeListener( listener );
                    }
                }
            );
        }
        catch( SWTError e )
        {
            final SapphireFormText text = new SapphireFormText( context.getComposite(), style );
            text.setText( Resources.couldNotInitializeBrowserMessage, false, false );
            text.setLayoutData( gd );
        }
    }
    
    private static final class Resources extends NLS 
    {
        public static String couldNotInitializeBrowserMessage;

        static 
        {
            initializeMessages( HtmlPanelPart.class.getName(), Resources.class );
        }
    }
    
}
