/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.addon.restaddondemo.core.entity.compkey;

import com.haulmont.chile.core.annotations.Composition;
import com.haulmont.cuba.core.entity.BaseIdentityIdEntity;
import com.haulmont.addon.restaddondemo.core.entity.identity.IdentityCustomer;
import com.haulmont.addon.restaddondemo.core.entity.identity.IdentityOrderLine;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity(name = "ref$CompKeyOrder")
@Table(name = "REF_CK_ORDER")
public class CompKeyOrder extends CompKeyBaseEntity {

    private static final long serialVersionUID = -1051339073882808493L;

    @Column(name = "ORDER_DATE")
    private Date orderDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "CUSTOMER_TENANT_ID", referencedColumnName = "TENANT_ID"),
            @JoinColumn(name = "CUSTOMER_ENTITY_ID", referencedColumnName = "ENTITY_ID")
    })
    private CompKeyCustomer customer;

    @OneToMany(mappedBy = "order")
    @OrderBy("id.entityId")
    @Composition
    private List<CompKeyOrderLine> orderLines;

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public CompKeyCustomer getCustomer() {
        return customer;
    }

    public void setCustomer(CompKeyCustomer customer) {
        this.customer = customer;
    }

    public List<CompKeyOrderLine> getOrderLines() {
        return orderLines;
    }

    public void setOrderLines(List<CompKeyOrderLine> orderLines) {
        this.orderLines = orderLines;
    }
}
