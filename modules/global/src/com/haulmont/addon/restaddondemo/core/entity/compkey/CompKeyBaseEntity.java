/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.addon.restaddondemo.core.entity.compkey;

import com.haulmont.cuba.core.entity.BaseGenericIdEntity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.NumberIdSource;

import javax.annotation.PostConstruct;
import javax.persistence.EmbeddedId;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class CompKeyBaseEntity extends BaseGenericIdEntity<CompKey> {

    private static final long serialVersionUID = 8667997566696787448L;

    @EmbeddedId
    private CompKey id;

    @PostConstruct
    public void init() {
        id = new CompKey();
        id.setTenantId(1);
        id.setEntityId(AppBeans.get(NumberIdSource.class).createLongId(getClass().getSimpleName()));
    }

    @Override
    public void setId(CompKey id) {
        this.id = id;
    }

    @Override
    public CompKey getId() {
        return id;
    }
}
