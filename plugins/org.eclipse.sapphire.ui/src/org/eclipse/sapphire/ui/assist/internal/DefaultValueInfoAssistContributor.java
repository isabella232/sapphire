/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - showing labels for named values && [bugzilla 329114] rewrite context help binding feature
 ******************************************************************************/

package org.eclipse.sapphire.ui.assist.internal;

import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.util.internal.SapphireCommonUtil;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContext;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContribution;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContributor;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistSection;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class DefaultValueInfoAssistContributor

    extends PropertyEditorAssistContributor
    
{
    public DefaultValueInfoAssistContributor()
    {
        setId( ID_DEFAULT_VALUE_CONTRIBUTOR );
        setPriority( PRIORITY_DEFAULT_VALUE_CONTRIBUTOR );
    }
    
    @Override
    public void contribute( final PropertyEditorAssistContext context )
    {
        final IModelElement element = context.getModelElement();
        final ModelProperty property = context.getProperty();
        
        if( property instanceof ValueProperty && 
            element.read( (ValueProperty) property ).getText( false ) != null )
        {
            final ValueProperty valprop = (ValueProperty) property;
            final String defaultValue = SapphireCommonUtil.getDefaultValueLabel(element, valprop);
            if( defaultValue != null )
            {
                String label = NLS.bind( Resources.defaultValueInfoMessage, defaultValue );
                label = "<p>" + escapeForXml( label ) + "</p>";

                final PropertyEditorAssistContribution contribution = new PropertyEditorAssistContribution();
                contribution.setText( label );
                
                final PropertyEditorAssistSection section = context.getSection( SECTION_ID_INFO );
                section.addContribution( contribution );
            }
        }
    }
    
    private static final class Resources
    
        extends NLS
    
    {
        public static String defaultValueInfoMessage;
        
        static
        {
            initializeMessages( DefaultValueInfoAssistContributor.class.getName(), Resources.class );
        }
    }
    
}
