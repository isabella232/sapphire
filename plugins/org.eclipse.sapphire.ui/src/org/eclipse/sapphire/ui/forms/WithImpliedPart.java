/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.ui.forms.swt.internal.FormPresentation;
import org.eclipse.sapphire.util.ListFactory;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class WithImpliedPart extends ContainerPart<FormComponentPart>
{
    private ModelPath path;
    private Element element;
    
    @Override
    protected void init()
    {
        final WithDef def = (WithDef) this.definition;
        
        this.path = new ModelPath( substituteParams( def.getPath().text() ) );
        this.element = getModelElement();
        
        for( int i = 0, n = this.path.length(); i < n; i++ )
        {
            final ModelPath.Segment segment = this.path.segment( i );
            
            if( segment instanceof ModelPath.ModelRootSegment )
            {
                this.element = this.element.root();
            }
            else if( segment instanceof ModelPath.ParentElementSegment )
            {
                this.element = this.element.parent().element();
            }
            else if( segment instanceof ModelPath.PropertySegment )
            {
                final Property property = this.element.property( ( (ModelPath.PropertySegment) segment ).getPropertyName() );
                
                if( property != null && property.definition() instanceof ImpliedElementProperty )
                {
                    this.element = ( (ElementHandle<?>) property ).content();
                }
                else
                {
                    throw new RuntimeException( this.path.toString() );
                }
            }
            else
            {
                throw new RuntimeException( this.path.toString() );
            }
        }
    }
    
    protected Children initChildren()
    {
        return new Children()
        {
            @Override
            protected void init( final ListFactory<FormComponentPart> childPartsListFactory )
            {
                final Element element = getLocalModelElement();
                final WithDef def = definition();
                final FormDef formdef = def.getCases().get( 0 );
                
                for( final FormComponentDef childPartDef : formdef.getContent() )
                {
                    childPartsListFactory.add( (FormComponentPart) createWithoutInit( WithImpliedPart.this, element, childPartDef, WithImpliedPart.this.params ) );
                }
            }
        };
    }

    @Override
    public WithDef definition()
    {
        return (WithDef) super.definition();
    }
    
    public ModelPath getPath()
    {
        return this.path;
    }
    
    @Override
    public Element getLocalModelElement()
    {
        return this.element;
    }
    
    @Override
    
    public boolean setFocus( final ModelPath path )
    {
        if( this.path.isPrefixOf( path ) )
        {
            super.setFocus( path.makeRelativeTo( this.path ) );
        }
        
        return false;
    }
    
    @Override
    public FormComponentPresentation createPresentation( final SwtPresentation parent, final Composite composite )
    {
        return new FormPresentation( this, parent, composite );
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        this.path = null;
        this.element = null;
    }

}
