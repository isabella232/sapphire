/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.services.internal;

import java.util.List;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StandardElementDocumentationService extends StandardDocumentationService
{
    @Override
    protected void initStandardDocumentationService( final StringBuilder content,
                                                     final List<Topic> topics )
    {
        final ElementType type = context( ElementType.class );
        final Documentation docAnnotation = type.getAnnotation( Documentation.class );
        
        if( docAnnotation != null )
        {
            final LocalizationService localization = type.getLocalizationService();
            final String docAnnotationContent = localization.text( docAnnotation.content().trim(), CapitalizationType.NO_CAPS, false );
            
            if( docAnnotationContent.length() > 0 )
            {
                content.append( docAnnotationContent );
            }
            
            topics.addAll( convert( docAnnotation.topics(), localization ) );
        }
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            return context.find( ElementType.class ).hasAnnotation( Documentation.class );
        }
    }
    
}
