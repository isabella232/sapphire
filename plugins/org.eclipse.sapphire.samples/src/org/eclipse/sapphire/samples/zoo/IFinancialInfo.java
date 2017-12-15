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

package org.eclipse.sapphire.samples.zoo;

import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NumericRange;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.IModelElementForXml;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateXmlBinding( elementPath = "financials" )

public interface IFinancialInfo

    extends IModelElementForXml

{
    ModelElementType TYPE = new ModelElementType( IFinancialInfo.class );
    
    // *** CurrentYearIncome ***

    @Type( base = Integer.class )
    @Label( standard = "current year income" )
    @NumericRange( min = "0" )
    @XmlBinding( path = "income" )

    ValueProperty PROP_CURRENT_YEAR_INCOME = new ValueProperty( TYPE, "CurrentYearIncome" );

    Value<Integer> getCurrentYearIncome();
    void setCurrentYearIncome( String currentYearIncome );
    void setCurrentYearIncome( Integer currentYearIncome );

    // *** CurrentYearExpenses ***

    @Type( base = Integer.class )
    @Label( standard = "current year expenses" )
    @NumericRange( min = "0" )
    @XmlBinding( path = "expenses" )

    ValueProperty PROP_CURRENT_YEAR_EXPENSES = new ValueProperty( TYPE, "CurrentYearExpenses" );

    Value<Integer> getCurrentYearExpenses();
    void setCurrentYearExpenses( String currentYearExpenses );
    void setCurrentYearExpenses( Integer currentYearExpenses );

}
