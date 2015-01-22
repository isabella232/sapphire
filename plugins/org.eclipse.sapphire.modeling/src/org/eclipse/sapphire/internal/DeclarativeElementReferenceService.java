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

package org.eclipse.sapphire.internal;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementReference;
import org.eclipse.sapphire.ElementReferenceService;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.ReferenceValue;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * {@link org.eclipse.sapphire.services.ReferenceService} implementation that derives its behavior 
 * from @{@link ElementReference} annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class DeclarativeElementReferenceService extends ElementReferenceService
{
    private ElementList<?> list;
    private String key;
    
    @Override
    protected void initReferenceService()
    {
        final Element element = context( Element.class );
        final ElementReference elementReferenceAnnotation = context( PropertyDef.class ).getAnnotation( ElementReference.class );
        
        this.list = (ElementList<?>) element.property( elementReferenceAnnotation.list() );
        this.key = elementReferenceAnnotation.key();
        
        super.initReferenceService();
    }

    @Override
    public ElementList<?> list()
    {
        return this.list;
    }

    @Override
    public String key()
    {
        return this.key;
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        this.list = null;
        this.key = null;
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final ReferenceValue<?,?> ref = context.find( ReferenceValue.class );
            return ( ref != null ) && ref.definition().hasAnnotation( ElementReference.class );
        }
    }

}
