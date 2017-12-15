/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - showing labels for named values
 ******************************************************************************/

package org.eclipse.sapphire.ui.assist.internal;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.EnumValueType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.LabelTransformer;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueKeyword;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.NamedValues;
import org.eclipse.sapphire.modeling.annotations.NamedValues.NamedValue;
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
            ( (Value<?>) property.invokeGetterMethod( element ) ).getText( false ) != null )
        {
            final ValueProperty valprop = (ValueProperty) property;
            String defaultValue = element.service().getDefaultValue( valprop );
            
            if( defaultValue != null )
            {
                if( property.isOfType( Enum.class ) )
                {
                    final EnumValueType enumValueType = new EnumValueType( property.getTypeClass() );
                    
                    for( Enum<?> item : enumValueType.getItems() )
                    {
                        if( item.toString().equals( defaultValue ) )
                        {
                            defaultValue = enumValueType.getLabel( item, true, CapitalizationType.NO_CAPS, false );
                            break;
                        }
                    }
                }
                else
                {
                    ValueKeyword keyword = valprop.getKeyword( defaultValue );
                    
                    if( keyword != null )
                    {
                        defaultValue = keyword.toDisplayString();
                    }
                    else if ( property.hasAnnotation( NamedValues.class ) ) 
                    {
                        final NamedValues namedValuesAnnotation = property.getAnnotation( NamedValues.class );
                        final NamedValue[] namedValueAnnotations = namedValuesAnnotation.namedValues();
                        final String propName = property.getName();

                        for( int i = 0, n = namedValueAnnotations.length; i < n; i++ )
                        {
                            final NamedValue x = namedValueAnnotations[ i ];
                            
                            if ( defaultValue.equals(x.value()) ) 
                            {
                                String namedValueLabel = property.getResource( propName + ".namedValue." + x.value() );
                                namedValueLabel = LabelTransformer.transform( namedValueLabel, CapitalizationType.NO_CAPS, true );
                                defaultValue = namedValueLabel + " (" + x.value() + ")";
                                break;
                            }
                        }
                    }
                }
                
                if( ! ( property.isOfType( Integer.class ) ||
                        property.isOfType( Long.class ) ||
                        property.isOfType( Float.class ) ||
                        property.isOfType( Double.class ) ||
                        property.isOfType( BigInteger.class ) ||
                        property.isOfType( BigDecimal.class ) ||
                        property.isOfType( Boolean.class ) ) )
                {
                    defaultValue = "\"" + defaultValue + "\"";
                }
                
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
