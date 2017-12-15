/******************************************************************************
 * Copyright (c) 2013 Oracle and Other Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Greg Amerson - [342771] Support "image+label" hint for when actions are presented in a toolbar
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.renderer;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.SubToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireToolBarManagerActionPresentation extends SapphireHotSpotsActionPresentation
{
    private IToolBarManager toolBarManager;
    private ToolBarContribution toolBarContribution;
    private SapphireToolBarActionPresentation toolBarActionPresentation;
    
    public SapphireToolBarManagerActionPresentation( final SapphireActionPresentationManager manager )
    {
        super( manager );
        
        this.toolBarActionPresentation = new SapphireToolBarActionPresentation( manager );
    }

    public SapphireToolBarManagerActionPresentation( final ISapphirePart part,
                                                     final Shell shell,
                                                     final SapphireActionGroup actions )
    {
        this( new SapphireActionPresentationManager( new SapphireRenderingContext( part, shell ), actions ) );
    }
    
    public IToolBarManager getToolBarManager()
    {
        return this.toolBarManager;
    }
    
    public void setToolBarManager( final IToolBarManager toolBarManager )
    {
        this.toolBarManager = toolBarManager;
        this.toolBarContribution = new ToolBarContribution();
    }
    
    public void render()
    {
        setCursor( this.toolBarManager, null );
        
        this.toolBarManager.add( this.toolBarContribution );        
        this.toolBarManager.update( true ); // call update so our embedded toolbar will get created
    }
    
    public ToolBar getToolBar()
    {
        return this.toolBarActionPresentation.getToolBar();
    }
    
    private static void setCursor( final IToolBarManager toolBarManager,
                                   final Cursor cursor )
    {
        if( toolBarManager instanceof ToolBarManager )
        {
            ( (ToolBarManager) toolBarManager ).getControl().setCursor( null );
        }
        else if( toolBarManager instanceof SubToolBarManager )
        {
            setCursor( (IToolBarManager) ( (SubToolBarManager) toolBarManager ).getParent(), cursor );
        }
    }
    
    private final class ToolBarContribution extends ControlContribution
    {
        private ToolBar toolBar;
        
        protected ToolBarContribution()
        {
            super( "sapphire" );
        }
        
        @Override
        public boolean isDynamic() 
        {
            return true;
        }
        
        @Override
        protected Control createControl( final Composite parent )
        {
            this.toolBar = new ToolBar( parent, SWT.FLAT | SWT.RIGHT );
            this.toolBar.setBackground( null );
            this.toolBar.setForeground( parent.getForeground() );
            
            SapphireToolBarManagerActionPresentation.this.toolBarActionPresentation.setToolBar( this.toolBar );
            SapphireToolBarManagerActionPresentation.this.toolBarActionPresentation.render();
            
            return this.toolBar;
        }
    }
    
}
