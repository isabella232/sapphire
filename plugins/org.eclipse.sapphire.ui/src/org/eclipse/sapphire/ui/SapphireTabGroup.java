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

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.ui.util.SwtUtil.gdfill;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdhfill;
import static org.eclipse.sapphire.ui.util.SwtUtil.glayout;
import static org.eclipse.sapphire.ui.util.SwtUtil.hspan;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.modeling.SapphireMultiStatus;
import org.eclipse.sapphire.ui.def.ISapphirePartDef;
import org.eclipse.sapphire.ui.def.ISapphireTabGroupDef;
import org.eclipse.sapphire.ui.def.ISapphireTabGroupItemDef;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class SapphireTabGroup

    extends SapphirePart
    
{
    private List<SapphirePart> tabParts;
    
    @Override
    protected void init()
    {
        super.init();

        this.tabParts = new ArrayList<SapphirePart>();
        
        for( ISapphireTabGroupItemDef tabDef : ( (ISapphireTabGroupDef) this.definition ).getTabs() )
        {
            final SapphirePart tabPart = create( this, getModelElement(), tabDef, null );
            this.tabParts.add( tabPart );

            final SapphirePartListener tabPartListener = new SapphirePartListener()
            {
                @Override
                public void handleValidateStateChange( final IStatus oldValidateState,
                                                       final IStatus newValidationState )
                {
                    updateValidationState();
                }
            };
            
            tabPart.addListener( tabPartListener );
        }
    }

    @Override
    public void render( final SapphireRenderingContext context )
    {
        final boolean expandVertically 
            = ( Boolean.valueOf( this.definition.getHint( ISapphirePartDef.HINT_EXPAND_VERTICALLY ) ) == true );
    
        final TabFolder tabGroup = new TabFolder( context.getComposite(), SWT.TOP );
        tabGroup.setLayoutData( hspan( ( expandVertically ? gdfill() : gdhfill() ), 2 ) );
        context.adapt( tabGroup );
        
        for( final SapphirePart tabPart : this.tabParts )
        {
            final Composite tabControl = new Composite( tabGroup, SWT.NONE );
            //tabControl.setLayout( glayout( 2, 5, 10, 10, 10 ) );
            tabControl.setLayout( glayout( 2, 0, 0 ) );

            final TabItem tab = new TabItem( tabGroup, SWT.NONE );
            tab.setText( ( (ISapphireTabGroupItemDef) tabPart.getDefinition() ).getLabel().getLocalizedText() );
            tab.setControl( tabControl );
            
            final SapphirePartListener tabPartListener = new SapphirePartListener()
            {
                @Override
                public void handleValidateStateChange( final IStatus oldValidateState,
                                                       final IStatus newValidationState )
                {
                    updateTabImage( tab, tabPart, newValidationState );
                }
            };
            
            tabPart.addListener( tabPartListener );
            updateTabImage( tab, tabPart, tabPart.getValidationState() );
            
            tabPart.render( new SapphireRenderingContext( tabPart, context, tabControl ) );
        }
    }
    
    private void updateTabImage( final TabItem tab,
                                 final SapphirePart tabPart,
                                 final IStatus newValidationState )
    {
        final int severity = newValidationState.getSeverity();
        
        ImageDescriptor imageDescriptor = ( (ISapphireTabGroupItemDef) tabPart.getDefinition() ).getImagePath().resolve();
        
        if( imageDescriptor == null )
        {
            imageDescriptor = SapphireImageCache.OBJECT_LEAF_NODE; 
        }
        
        final Image image = getImageCache().getImage( imageDescriptor, severity );            
        
        tab.setImage( image );
    }
    
    @Override
    protected IStatus computeValidationState()
    {
        final SapphireMultiStatus st = new SapphireMultiStatus();

        for( SapphirePart tabPart : this.tabParts )
        {
            st.add( tabPart.getValidationState() );
        }
        
        return st;
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        for( SapphirePart tabPart : this.tabParts )
        {
            tabPart.dispose();
        }
    }
    
}
