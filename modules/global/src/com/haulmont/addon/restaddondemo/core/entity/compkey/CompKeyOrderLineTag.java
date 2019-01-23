/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.addon.restaddondemo.core.entity.compkey;

import javax.persistence.*;

@Entity(name = "ref$CompKeyOrderLineTag")
@Table(name = "REF_CK_ORDER_LINE_TAG")
public class CompKeyOrderLineTag extends CompKeyBaseEntity {

    private static final long serialVersionUID = 120575782544351747L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "ORDER_LINE_TENANT_ID", referencedColumnName = "TENANT_ID"),
            @JoinColumn(name = "ORDER_LINE_ENTITY_ID", referencedColumnName = "ENTITY_ID")
    })
    private CompKeyOrderLine orderLine;

    @Column(name = "NAME")
    private String name;

    public CompKeyOrderLine getOrderLine() {
        return orderLine;
    }

    public void setOrderLine(CompKeyOrderLine orderLine) {
        this.orderLine = orderLine;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
