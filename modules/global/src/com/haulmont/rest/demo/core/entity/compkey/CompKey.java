/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.rest.demo.core.entity.compkey;

import com.haulmont.cuba.core.entity.EmbeddableEntity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class CompKey extends EmbeddableEntity {

    private static final long serialVersionUID = -3693454341496457485L;

    @Column(name = "TENANT_ID")
    private Integer tenantId;

    @Column(name = "ENTITY_ID")
    private Long entityId;

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompKey compKey = (CompKey) o;
        return Objects.equals(tenantId, compKey.tenantId) &&
                Objects.equals(entityId, compKey.entityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, entityId);
    }

    @Override
    public String toString() {
        return tenantId + "-" + entityId;
    }
}
