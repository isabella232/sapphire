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

package org.eclipse.sapphire.modeling.el;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ModelPropertyListener;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.localization.SourceLanguageLocalizationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class ModelElementFunctionContext

    extends FunctionContext
    
{
    private final IModelElement element;
    private final LocalizationService localizationService;
    private final ModelPropertyListener listener;
    
    public ModelElementFunctionContext( final IModelElement element )
    {
        this( element, SourceLanguageLocalizationService.INSTANCE );
    }
    
    public ModelElementFunctionContext( final IModelElement element,
                                        final LocalizationService localizationService )
    {
        this.element = element;
        this.localizationService = localizationService;
        
        this.listener = new ModelPropertyListener()
        {
            @Override
            public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
            {
                if( event != null )
                {
                    final ModelProperty property = event.getProperty();
                    
                    if( property != null )
                    {
                        notifyListeners( property.getName() );
                    }
                }
            }
        };
        
        this.element.addListener( this.listener, "*" );
    }
    
    public final IModelElement element()
    {
        return this.element;
    }
    
    @Override
    public Object property( final String name )
    {
        final ModelElementType type = this.element.getModelElementType();
        final ModelProperty property = type.getProperty( name );
        
        if( property != null )
        {
            return this.element.read( property );
        }
        
        return super.property( name );
    }

    @Override
    public LocalizationService getLocalizationService()
    {
        return this.localizationService;
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        this.element.removeListener( this.listener, "*" );
    }
    
}
