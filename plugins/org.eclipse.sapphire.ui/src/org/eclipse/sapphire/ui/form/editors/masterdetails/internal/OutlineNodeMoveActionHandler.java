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

package org.eclipse.sapphire.ui.form.editors.masterdetails.internal;

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
    private ModelElementListener listPropertyListener = null;
    private MasterDetailsContentOutline contentTree = null;
    private MasterDetailsContentOutline.Listener contentTreeListener = null;
    
    @Override
    public void init( final SapphireAction action,
                      final ISapphireActionHandlerDef def )
    {
        super.init( action, def );

        this.contentTree = ( (MasterDetailsContentNode) getPart() ).getContentTree();
        
        this.contentTreeListener = new MasterDetailsContentOutline.Listener()
        {
            @Override
            public void handleFilterChange( String newFilterText )
            {
                refreshEnabledState();
            }
        };
        
        this.contentTree.addListener( this.contentTreeListener );
        
        final Runnable op = new Runnable()
        {
            public void run()
            {
                refreshEnabledState();
            }
        };
        
        final ListProperty property = getList().getParentProperty();
        
        this.listPropertyListener = new ModelElementListener()
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
        
        final MasterDetailsContentNode node = (MasterDetailsContentNode) getPart().getParentPart();
        node.getLocalModelElement().addListener( this.listPropertyListener );
        
        refreshEnabledState();
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

    @Override
    public void dispose()
    {
        super.dispose();
        
        final MasterDetailsContentNode node = (MasterDetailsContentNode) getPart().getParentPart();
        node.getLocalModelElement().removeListener( this.listPropertyListener );
        
        if( this.contentTree != null )
        {
            this.contentTree.removeListener( this.contentTreeListener );
        }
    }

}
