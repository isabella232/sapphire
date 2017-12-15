/******************************************************************************
 * Copyright (c) 2013 Oracle and Liferay
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

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.IModelParticle;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.PropertyContentEvent;
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
    private Listener listener;
    
    @Override
    protected void init()
    {
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
                    throw new RuntimeException( NLS.bind( Resources.invalidPathMsg, pathStringSubstituted ) );
                }
            }
            else
            {
                throw new RuntimeException( NLS.bind( Resources.invalidPathMsg, pathStringSubstituted ) );
            }
        }
        
        this.listener = new FilteredListener<PropertyContentEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyContentEvent event )
            {
                updateCurrentPage();
            }
        };
        
        this.element.attach( this.listener, this.property );
        
        super.init();
        
        setExposePageValidationState( true );
        updateCurrentPage();
    }

    @Override
    protected Object parsePageKey( final String panelKeyString )
    {
        final Class<?> enumType = this.property.getTypeClass();
        final int lastDot = panelKeyString.lastIndexOf( '.' );
        final String enumItemName;
        
        if( lastDot == -1 )
        {
            enumItemName = panelKeyString;
        }
        else
        {
            final String className = panelKeyString.substring( 0, lastDot );
            enumItemName = panelKeyString.substring( lastDot + 1 );
            
            final ISapphireUiDef rootdef = this.definition.nearest( ISapphireUiDef.class );
            final Class<?> specifiedEnumType = rootdef.resolveClass( className );
            
            if( specifiedEnumType == null )
            {
                throw new RuntimeException( NLS.bind( Resources.invalidPageKeyClassNotResolvedMsg, panelKeyString, className ) );
            }
            
            if( specifiedEnumType != enumType )
            {
                throw new RuntimeException( NLS.bind( Resources.invalidPageKeyClassNotMatchedMsg, panelKeyString, className ) );
            }
        }
        
        Field field = null;
        
        for( Field f : enumType.getFields() )
        {
            if( f.isEnumConstant() && f.getName().equalsIgnoreCase( enumItemName ) )
            {
                field = f;
                break;
            }
        }
        
        if( field == null )
        {
            throw new RuntimeException( NLS.bind( Resources.invalidPageKeyEnumItemMsg, panelKeyString, enumType.getSimpleName(), enumItemName ) );
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
            this.element.detach( this.listener, this.property );
        }
    }

    private static final class Resources extends NLS
    {
        public static String invalidPathMsg;
        public static String invalidPageKeyClassNotResolvedMsg;
        public static String invalidPageKeyClassNotMatchedMsg;
        public static String invalidPageKeyEnumItemMsg;

        static
        {
            initializeMessages( SapphireEnumControlledPageBook.class.getName(), Resources.class );
        }
    }
    
}
