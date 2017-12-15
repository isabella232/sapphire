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

import org.eclipse.sapphire.Since;
import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.VersionCompatibilityTarget;
import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementHandle;
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
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Service( impl = PurchaseOrderResourceConversionService.class )
@VersionCompatibilityTarget( version = "${ Version }", versioned = "Purchase Order" )
@GenerateImpl

public interface PurchaseOrder extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( PurchaseOrder.class );
    
    // *** Version ***
    
    @Type( base = Version.class )
    @DefaultValue( text = "2.0" )

    ValueProperty PROP_VERSION = new ValueProperty( TYPE, "Version" );
    
    Value<Version> getVersion();
    void setVersion( String value );
    void setVersion( Version value );
    
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
    @Since( "1.5" )
    
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
    @Since( "2.0" )
    
    ValueProperty PROP_FULFILLMENT_DATE = new ValueProperty( TYPE, "FulfillmentDate" );
    
    Value<Date> getFulfillmentDate();
    void setFulfillmentDate( String value );
    void setFulfillmentDate( Date value );
    
    // *** BillingInformation ***
    
    @Type( base = BillingInformation.class )
    @Label( standard = "billing information" )
    
    ImpliedElementProperty PROP_BILLING_INFORMATION = new ImpliedElementProperty( TYPE, "BillingInformation" );
    
    BillingInformation getBillingInformation();
    
    // *** ShippingInformation ***
    
    @Type( base = ShippingInformation.class )
    @Label( standard = "shipping information" )
    @Since( "2.0" )
    
    ImpliedElementProperty PROP_SHIPPING_INFORMATION = new ImpliedElementProperty( TYPE, "ShippingInformation" );
    
    ShippingInformation getShippingInformation();
    
    // *** Payment ***
    
    @Type( base = Payment.class, possible = { CreditCardPayment.class, CheckPayment.class, CashPayment.class } )
    @Label( standard = "payment" )
    @Since( "1.5" )
    
    ElementProperty PROP_PAYMENT = new ElementProperty( TYPE, "Payment" );
    
    ModelElementHandle<Payment> getPayment();
    
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
