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

import java.lang.reflect.Field;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.ui.def.ISapphirePageBookExtDef;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireEnumControlledPageBook

    extends SapphirePageBook
    
{
    private ValueProperty property;
    
    @Override
    protected void init()
    {
        super.init();
        
        this.property = (ValueProperty) resolve( ( (ISapphirePageBookExtDef) this.definition ).getControlProperty().getText() );
        
        setExposePageValidationState( true );
        updateCurrentPage();
    }

    @Override
    protected Object parsePageKey( final String panelKeyString )
    {
        final int lastDot = panelKeyString.lastIndexOf( '.' );
        final String className = panelKeyString.substring( 0, lastDot );
        final String enumItemName = panelKeyString.substring( lastDot + 1 );
        
        final ISapphireUiDef rootdef = (ISapphireUiDef) this.definition.getModel();
        final Class<?> classObject = rootdef.resolveClass( className );
        final Field field;
        
        try
        {
            field = classObject.getField( enumItemName );
        }
        catch( NoSuchFieldException e )
        {
            throw new RuntimeException( e );
        }
        
        try
        {
            return field.get( null );
        }
        catch( IllegalAccessException e )
        {
            throw new RuntimeException( e );
        }
    }
    
    @Override
    protected void handleModelElementChange( final ModelPropertyChangeEvent event )
    {
        super.handleModelElementChange( event );
        
        if( event.getProperty() == this.property )
        {
            updateCurrentPage();
        }
    }
    
    private void updateCurrentPage()
    {
        final IModelElement modelElement = getModelElement();
        final Value<?> newEnumItemValue = (Value<?>) this.property.invokeGetterMethod( modelElement );
        final Enum<?> newEnumItem = (Enum<?>) newEnumItemValue.getContent( true );

        changePage( modelElement, newEnumItem );
    }
    
}
