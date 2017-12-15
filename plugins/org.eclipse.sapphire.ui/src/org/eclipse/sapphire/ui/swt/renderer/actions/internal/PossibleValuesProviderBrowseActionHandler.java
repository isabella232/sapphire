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

import java.util.Collection;

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.PossibleValuesService;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.ui.SapphireBrowseActionHandler;
import org.eclipse.sapphire.ui.SapphirePropertyEditor;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.renderers.swt.ValueLabelProvider;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PossibleValuesProviderBrowseActionHandler 

    extends SapphireBrowseActionHandler
    
{
    public static final String ID = "Sapphire.Browse.Possible";
    
    public PossibleValuesProviderBrowseActionHandler()
    {
        setId( ID );
    }
    
    @Override
    protected String browse( final SapphireRenderingContext context )
    {
        final ValueProperty property = getProperty();
        final PossibleValuesService valuesProvider = getModelElement().service( property, PossibleValuesService.class );

        if( valuesProvider != null )
        {
            final Collection<String> valuesList = valuesProvider.values();
            final String[] valuesArray = valuesList.toArray( new String[ valuesList.size() ] );
            
            final ValueLabelProvider labelProvider = new ValueLabelProvider( (SapphirePropertyEditor) getPart(), property );
            
            final ElementListSelectionDialog dialog = new ElementListSelectionDialog( context.getShell(), labelProvider );
            
            dialog.setElements( valuesArray );
            dialog.setMultipleSelection( false );
            dialog.setHelpAvailable( false );
            dialog.setTitle( property.getLabel( false, CapitalizationType.TITLE_STYLE, false ) );
            dialog.setMessage( createBrowseDialogMessage( property.getLabel( true, CapitalizationType.NO_CAPS, false ) ) );
            
            dialog.open();
            
            final Object[] result = dialog.getResult();
            
            if( result != null && result.length == 1 )
            {
                return (String) result[ 0 ];
            }
        }
        
        return null;
    }
}