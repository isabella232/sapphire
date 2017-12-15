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

package org.eclipse.sapphire.ui.renderers.swt;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.LabelTransformer;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.assist.BrowseHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class BrowseCommandHandler 

    extends AbstractHandler
    
{
    private final SapphireRenderingContext context;
    private final List<BrowseHandler> browseHandlers;
    
    public BrowseCommandHandler( final SapphireRenderingContext context,
                                 final List<BrowseHandler> browseHandlers )
    {
        this.context = context;
        this.browseHandlers = browseHandlers;
    }
    
    public Object execute( final ExecutionEvent event )
    {
        if( this.browseHandlers.size() == 1 )
        {
            invokeBrowseHandler( this.browseHandlers.get( 0 ) );
        }
        else
        {
            final Menu menu = new Menu( this.context.getShell(), SWT.POP_UP );
            final boolean[] menuItemSelected = new boolean[] { false };
            
            for( final BrowseHandler browseHandler : this.browseHandlers )
            {
                final MenuItem menuItem = new MenuItem( menu, SWT.PUSH );
                
                final String label = LabelTransformer.transform( browseHandler.getLabel(), CapitalizationType.FIRST_WORD_ONLY, true ) + "...";
                menuItem.setText( label );
                
                final Image image = this.context.getImageCache().getImage( browseHandler.getImageDescriptor() );
                menuItem.setImage( image );
                
                menuItem.addSelectionListener
                (
                    new SelectionAdapter()
                    {
                        @Override
                        public void widgetSelected( final SelectionEvent event )
                        {
                            menuItemSelected[ 0 ] = true;
                            invokeBrowseHandler( browseHandler );
                            menu.dispose();
                        }
                    }
                );
            }
            
            menu.addMenuListener
            (
                new MenuAdapter()
                {
                    @Override
                    public void menuHidden( final MenuEvent event )
                    {
                        // This very hacky, but I have not been able to find other solutions. At least on some platforms,
                        // the menu hide event comes before the item selection event. 

                        final Runnable op = new Runnable()
                        {
                            public void run()
                            {
                                if( menuItemSelected[ 0 ] == false )
                                {
                                    handleBrowseCanceled();
                                    menu.dispose();
                                }
                            }
                        };
                        
                        menu.getDisplay().asyncExec( op );
                    }
                }
            );
            
            final Rectangle rect = getInvokerBounds();
            final Point pt = new Point( rect.x, rect.y + rect.height );
            menu.setLocation( pt );
            
            menu.setVisible( true );
        }

        return null;
    }
    
    protected abstract Rectangle getInvokerBounds();
    
    protected void handleBrowseCompleted( final String text )
    {
        // The default implementation doesn't do anything.
    }
    
    protected void handleBrowseCanceled()
    {
        // The default implementation doesn't do anything.
    }
    
    private void invokeBrowseHandler( final BrowseHandler browseHandler )
    {
        final String text = browseHandler.browse( this.context );
        
        if( text != null )
        {
            handleBrowseCompleted( text );
        }
        else
        {
            handleBrowseCanceled();
        }
    }

}
