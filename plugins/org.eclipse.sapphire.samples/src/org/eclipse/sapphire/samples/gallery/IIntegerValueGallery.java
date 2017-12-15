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

package org.eclipse.sapphire.samples.gallery;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NumericRange;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface IIntegerValueGallery

    extends IModelElement

{
    ModelElementType TYPE = new ModelElementType( IIntegerValueGallery.class );

    // *** Simple ***

    @Type( base = Integer.class )
    @Label( standard = "simple" )
    @XmlBinding( path = "simple" )

    ValueProperty PROP_SIMPLE = new ValueProperty( TYPE, "Simple" );

    Value<Integer> getSimple();
    void setSimple( String val );
    void setSimple( Integer val );
    
    // *** Positive ***
    
    @Type( base = Integer.class )
    @Label( standard = "positive" )
    @NumericRange( min = "0" )
    @XmlBinding( path = "positive" )

    ValueProperty PROP_POSITIVE = new ValueProperty( TYPE, "Positive" );

    Value<Integer> getPositive();
    void setPositive( String val );
    void setPositive( Integer val );

    // *** RangeConstrainedWithDefault ***
    
    @Type( base = Integer.class )
    @Label( standard = "range constrained with default" )
    @NumericRange( min = "5", max = "7000" )
    @DefaultValue( text = "1000" )
    @XmlBinding( path = "range-constrained-with-default" )

    ValueProperty PROP_RANGE_CONSTRAINED_WITH_DEFAULT = new ValueProperty( TYPE, "RangeConstrainedWithDefault" );

    Value<Integer> getRangeConstrainedWithDefault();
    void setRangeConstrainedWithDefault( String val );
    void setRangeConstrainedWithDefault( Integer val );
 
}
