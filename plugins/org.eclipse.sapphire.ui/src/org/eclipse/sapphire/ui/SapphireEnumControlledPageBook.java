/******************************************************************************
 * Copyright (c) 2012 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 *    Gregory Amerson - [363765] Page book control property should handle model paths
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import java.lang.reflect.Field;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.IModelParticle;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ModelPropertyListener;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;
import org.eclipse.sapphire.ui.def.PageBookExtDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Greg Amerson</a>
 */

public final class SapphireEnumControlledPageBook extends PageBookPart
{
    private IModelElement element;
    private ValueProperty property;
    private ModelPropertyListener listener;
    
    @Override
    protected void init()
    {
        super.init();
        
        final String pathString = ( (PageBookExtDef) this.definition ).getControlProperty().getContent();
        final String pathStringSubstituted = substituteParams( pathString, this.params );
        final ModelPath path = new ModelPath( pathStringSubstituted );
        
        this.element = getLocalModelElement();

        for( int i = 0, n = path.length(); i < n; i++ )
        {
            final ModelPath.Segment segment = path.segment( i );

            if( segment instanceof ModelPath.ModelRootSegment )
            {
                this.element = (IModelElement) this.element.root();
            }
            else if( segment instanceof ModelPath.ParentElementSegment )
            {
                IModelParticle parent = this.element.parent();

                if ( ! ( parent instanceof IModelElement ) )
                {
                    parent = parent.parent();
                }

                this.element = (IModelElement) parent;
            }
            else if( segment instanceof ModelPath.PropertySegment )
            {
                this.property = (ValueProperty) resolve( this.element, ( (ModelPath.PropertySegment) segment ).getPropertyName() );

                if ( i + 1 != n )
                {
                    throw new RuntimeException( NLS.bind( Resources.invalidPath, pathStringSubstituted ) );
                }
            }
            else
            {
                throw new RuntimeException( NLS.bind( Resources.invalidPath, pathStringSubstituted ) );
            }
        }
        
        this.listener = new ModelPropertyListener()
        {
            @Override
            public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
            {
                if( event.getProperty() == SapphireEnumControlledPageBook.this.property )
                {
                    updateCurrentPage();
                }
            }
        };
        
        this.element.addListener( this.listener, this.property.getName() );
        
        setExposePageValidationState( true );
        updateCurrentPage();
    }

    @Override
    protected Object parsePageKey( final String panelKeyString )
    {
        final int lastDot = panelKeyString.lastIndexOf( '.' );
        final String className = panelKeyString.substring( 0, lastDot );
        final String enumItemName = panelKeyString.substring( lastDot + 1 );
        
        final ISapphireUiDef rootdef = this.definition.nearest( ISapphireUiDef.class );
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
    
    private void updateCurrentPage()
    {
        final Value<?> newEnumItemValue = this.element.read( this.property );
        final Enum<?> newEnumItem = (Enum<?>) newEnumItemValue.getContent( true );

        changePage( this.element, newEnumItem );
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.listener != null )
        {
            this.element.removeListener( this.listener, this.property.getName() );
        }
    }

    private static final class Resources extends NLS
    {
        public static String invalidPath;

        static
        {
            initializeMessages( SapphireEnumControlledPageBook.class.getName(), Resources.class );
        }
    }
    
}
