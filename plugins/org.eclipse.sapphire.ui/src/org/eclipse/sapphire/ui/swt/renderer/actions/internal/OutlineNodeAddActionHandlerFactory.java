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

package org.eclipse.sapphire.ui.swt.renderer.actions.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.EditFailedException;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionHandlerFactory;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerDef;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsContentNode;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsContentTree;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsPage;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class OutlineNodeAddActionHandlerFactory

    extends SapphireActionHandlerFactory
    
{
    public static final String ID_BASE = "Sapphire.Add.";
    
    @Override
    public List<SapphireActionHandler> create()
    {
        final List<SapphireActionHandler> handlers = new ArrayList<SapphireActionHandler>();
        
        final MasterDetailsContentNode node = (MasterDetailsContentNode) getPart();
        final List<ListProperty> listProperties = node.getChildListProperties();
        
        for( final ListProperty listProperty : listProperties )
        {
            for( final ModelElementType memberType : listProperty.getAllPossibleTypes() )
            {
                final AddActionHandler handler = new AddActionHandler( listProperty, memberType );
                handlers.add( handler );
            }
        }
        
        return handlers;
    }

    private static final class AddActionHandler
    
        extends SapphireActionHandler
        
    {
        private final ListProperty listProperty;
        private final ModelElementType type;
        private MasterDetailsContentTree contentTree;
        private MasterDetailsContentTree.Listener contentTreeListener;
        
        public AddActionHandler( final ListProperty listProperty,
                                 final ModelElementType type )
        {
            this.listProperty = listProperty;
            this.type = type;
            
            setId( ID_BASE + type.getSimpleName() );
            setLabel( type.getLabel( true, CapitalizationType.NO_CAPS, false ) );
        }

        @Override
        public void init( final SapphireAction action,
                          final ISapphireActionHandlerDef def )
        {
            super.init( action, def );
            
            ImageDescriptor typeSpecificAddImage = getPart().getImageCache().getImageDescriptor( this.type );
            
            if( typeSpecificAddImage != null )
            {
                addImage( typeSpecificAddImage );
            }
            
            this.contentTree = ( (MasterDetailsContentNode) getPart() ).getContentTree();
            
            this.contentTreeListener = new MasterDetailsContentTree.Listener()
            {
                @Override
                public void handleFilterChange( String newFilterText )
                {
                    refreshEnabledState();
                }
            };
            
            this.contentTree.addListener( this.contentTreeListener );
            
            refreshEnabledState();
        }

        @Override
        protected Object run( final SapphireRenderingContext context )
        {
            final MasterDetailsContentNode node = (MasterDetailsContentNode) getPart();
            final IModelElement element = node.getLocalModelElement();
            final ModelElementList<?> list = element.read( this.listProperty );
            
            IModelElement newModelElement = null;
            
            try
            {
                newModelElement = list.addNewElement( this.type );
            }
            catch( Exception e )
            {
                // Log this exception unless the cause is EditFailedException. These exception
                // are the result of the user declining a particular action that is necessary
                // before the edit can happen (such as making a file writable).
                
                final EditFailedException editFailedException = EditFailedException.findAsCause( e );
                
                if( editFailedException == null )
                {
                    SapphireUiFrameworkPlugin.log( e );
                }
            }

            if( newModelElement != null )
            {
                node.getContentTree().notifyOfNodeStructureChange( node );
                
                for( MasterDetailsContentNode n : node.getChildNodes() )
                {
                    if( n.getModelElement() == newModelElement )
                    {
                        n.select();
                        getPart().getNearestPart( MasterDetailsPage.class ).setFocusOnDetails();
                        break;
                    }
                }
            }
            
            return newModelElement;
        }
        
        private void refreshEnabledState()
        {
            setEnabled( this.contentTree != null && this.contentTree.getFilterText().length() == 0 );
        }
        
        @Override
        public void dispose()
        {
            super.dispose();
            
            if( this.contentTree != null )
            {
                this.contentTree.removeListener( this.contentTreeListener );
            }
        }
    }

}

