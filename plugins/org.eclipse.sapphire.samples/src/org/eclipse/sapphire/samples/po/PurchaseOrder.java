/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.samples.po;

import java.math.BigDecimal;
import java.util.Date;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.CountConstraint;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Derived;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface PurchaseOrder extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( PurchaseOrder.class );
    
    // *** Id ***
    
    @Label( standard = "ID" )
    @Required
    
    ValueProperty PROP_ID = new ValueProperty( TYPE, "Id" );
    
    Value<String> getId();
    void setId( String value );
    
    // *** Customer ***
    
    @Label( standard = "customer" )
    @Required
    
    ValueProperty PROP_CUSTOMER = new ValueProperty( TYPE, "Customer" );
    
    Value<String> getCustomer();
    void setCustomer( String value );
    
    // *** InitialQuoteDate ***
    
    @Type( base = Date.class )
    @Label( standard = "initial quote date" )
    
    ValueProperty PROP_INITIAL_QUOTE_DATE = new ValueProperty( TYPE, "InitialQuoteDate" );
    
    Value<Date> getInitialQuoteDate();
    void setInitialQuoteDate( String value );
    void setInitialQuoteDate( Date value );
    
    // *** OrderDate ***
    
    @Type( base = Date.class )
    @Label( standard = "order date" )
    
    ValueProperty PROP_ORDER_DATE = new ValueProperty( TYPE, "OrderDate" );
    
    Value<Date> getOrderDate();
    void setOrderDate( String value );
    void setOrderDate( Date value );
    
    // *** FulfillmentDate ***
    
    @Type( base = Date.class )
    @Label( standard = "fulfillment date" )
    
    ValueProperty PROP_FULFILLMENT_DATE = new ValueProperty( TYPE, "FulfillmentDate" );
    
    Value<Date> getFulfillmentDate();
    void setFulfillmentDate( String value );
    void setFulfillmentDate( Date value );
    
    // *** BillingInformation ***
    
    @Type( base = BillingInformation.class )
    @Label( standard = "billing information" )
    @XmlBinding( path = "BillingInformation" ) // TODO: This should not be necessary.
    
    ImpliedElementProperty PROP_BILLING_INFORMATION = new ImpliedElementProperty( TYPE, "BillingInformation" );
    
    BillingInformation getBillingInformation();
    
    // *** ShippingInformation ***
    
    @Type( base = ShippingInformation.class )
    @Label( standard = "shipping information" )
    @XmlBinding( path = "ShippingInformation" ) // TODO: This should not be necessary.
    
    ImpliedElementProperty PROP_SHIPPING_INFORMATION = new ImpliedElementProperty( TYPE, "ShippingInformation" );
    
    ShippingInformation getShippingInformation();
    
    // *** Entries ***
    
    @Type( base = PurchaseOrderEntry.class )
    @Label( standard = "entries" )
    @CountConstraint( min = 1 )
    
    ListProperty PROP_ENTRIES = new ListProperty( TYPE, "Entries" );
    
    ModelElementList<PurchaseOrderEntry> getEntries();
    
    // *** Subtotal ***
    
    @Type( base = BigDecimal.class )
    @Label( standard = "subtotal" )
    @Derived( text = "${ Sum( Entries, 'Total' ) }" )
    
    ValueProperty PROP_SUBTOTAL = new ValueProperty( TYPE, "Subtotal" );
    
    Value<BigDecimal> getSubtotal();
    
    // *** Discount ***
    
    @Type( base = BigDecimal.class )
    @Label( standard = "discount" )
    @DefaultValue( text = "0.00" )
    
    ValueProperty PROP_DISCOUNT = new ValueProperty( TYPE, "Discount" );
    
    Value<BigDecimal> getDiscount();
    void setDiscount( String value );
    void setDiscount( BigDecimal value );
    
    // *** Delivery ***
    
    @Type( base = BigDecimal.class )
    @Label( standard = "delivery" )
    @DefaultValue( text = "0.00" )
    
    ValueProperty PROP_DELIVERY = new ValueProperty( TYPE, "Delivery" );
    
    Value<BigDecimal> getDelivery();
    void setDelivery( String value );
    void setDelivery( BigDecimal value );
    
    // *** Total ***
    
    @Type( base = BigDecimal.class )
    @Label( standard = "total" )
    @Derived( text = "${ Scale( Subtotal, 2 ) - Scale( Discount, 2 ) + Scale( Delivery, 2 ) }" )
    
    ValueProperty PROP_TOTAL = new ValueProperty( TYPE, "Total" );
    
    Value<BigDecimal> getTotal();
    
}
