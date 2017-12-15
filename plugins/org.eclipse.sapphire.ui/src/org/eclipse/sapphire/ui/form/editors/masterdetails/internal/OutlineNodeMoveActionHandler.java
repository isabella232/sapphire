/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.form.editors.masterdetails.internal;

import org.eclipse.sapphire.DisposeEvent;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementListener;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerDef;
import org.eclipse.sapphire.ui.form.editors.masterdetails.MasterDetailsContentNode;
import org.eclipse.sapphire.ui.form.editors.masterdetails.MasterDetailsContentOutline;
import org.eclipse.swt.widgets.Display;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class OutlineNodeMoveActionHandler extends SapphireActionHandler
{
    private MasterDetailsContentOutline contentTree = null;
    
    @Override
    public void init( final SapphireAction action,
                      final ISapphireActionHandlerDef def )
    {
        super.init( action, def );

        this.contentTree = ( (MasterDetailsContentNode) getPart() ).getContentTree();
        
        final MasterDetailsContentOutline.Listener contentTreeListener = new MasterDetailsContentOutline.Listener()
        {
            @Override
            public void handleFilterChange( String newFilterText )
            {
                refreshEnabledState();
            }
        };
        
        this.contentTree.addListener( contentTreeListener );
        
        final Runnable op = new Runnable()
        {
            public void run()
            {
                refreshEnabledState();
            }
        };
        
        final IModelElement parent = ( (MasterDetailsContentNode) getPart().getParentPart() ).getLocalModelElement();
        final ListProperty property = getList().getParentProperty();
        
        final ModelElementListener listPropertyListener = new ModelElementListener()
        {
            @Override
            public void propertyChanged( final ModelPropertyChangeEvent event )
            {
                if( event.getProperty() == property )
                {
                    Display.getDefault().asyncExec( op );
                }
            }
        };
        
        parent.addListener( listPropertyListener );
        
        refreshEnabledState();
        
        attach
        (
            new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof DisposeEvent )
                    {
                        parent.removeListener( listPropertyListener );
                        OutlineNodeMoveActionHandler.this.contentTree.removeListener( contentTreeListener );
                    }
                }
            }
        );
    }
    
    protected final ModelElementList<?> getList()
    {
        return (ModelElementList<?>) getModelElement().parent();
    }
    
    private void refreshEnabledState()
    {
        setEnabled( computeEnabledState() );
    }
    
    protected boolean computeEnabledState()
    {
        return ( this.contentTree != null && this.contentTree.getFilterText().length() == 0 );
    }

}
