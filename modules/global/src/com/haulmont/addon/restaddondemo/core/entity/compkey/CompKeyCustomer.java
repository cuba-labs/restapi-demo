/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.addon.restaddondemo.core.entity.compkey;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.HasUuid;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@Entity(name = "ref$CompKeyCustomer")
@Table(name = "REF_CK_CUSTOMER")
@NamePattern("%s|name")
public class CompKeyCustomer extends CompKeyBaseEntity implements HasUuid {

    private static final long serialVersionUID = 8972087517473770548L;

    @Column(name = "NAME")
    private String name;

    @Column(name = "UUID")
    private UUID uuid;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
