/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.rest.demo.core.entity.compkey;

import com.haulmont.chile.core.annotations.Composition;

import javax.persistence.*;
import java.util.List;

@Entity(name = "ref$CompKeyOrderLine")
@Table(name = "REF_CK_ORDER_LINE")
public class CompKeyOrderLine extends CompKeyBaseEntity {

    private static final long serialVersionUID = -8482106923770393181L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "ORDER_TENANT_ID", referencedColumnName = "TENANT_ID"),
            @JoinColumn(name = "ORDER_ENTITY_ID", referencedColumnName = "ENTITY_ID")
    })
    private CompKeyOrder order;

    @Column(name = "PRODUCT")
    private String product;

    @OneToMany(mappedBy = "orderLine")
    @OrderBy("id.entityId")
    @Composition
    private List<CompKeyOrderLineTag> orderLineTags;

    public CompKeyOrder getOrder() {
        return order;
    }

    public void setOrder(CompKeyOrder order) {
        this.order = order;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public List<CompKeyOrderLineTag> getOrderLineTags() {
        return orderLineTags;
    }

    public void setOrderLineTags(List<CompKeyOrderLineTag> orderLineTags) {
        this.orderLineTags = orderLineTags;
    }
}
