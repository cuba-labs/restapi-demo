/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package spec.addon.restaddondemo.rest

import com.haulmont.cuba.core.sys.encryption.BCryptEncryptionModule
import com.haulmont.cuba.core.sys.encryption.EncryptionModule
import com.haulmont.cuba.core.sys.persistence.PostgresUUID
import com.haulmont.cuba.security.entity.ConstraintCheckType
import com.haulmont.addon.restaddondemo.http.api.DataSet
import groovy.sql.Sql

class DataUtils {

    private static EncryptionModule encryption = new BCryptEncryptionModule()

    static UUID createGroup(DataSet dataSet, Sql sql, String groupName) {
        def groupId = dataSet.createGroupUuid()
        sql.dataSet('sec_group').add(
                id: new PostgresUUID(groupId),
                version: 1,
                name: groupName
        )
        return groupId
    }

    static UUID createConstraint(DataSet dataSet, Sql sql,
                                 ConstraintCheckType checkType,
                                 String metaClass,
                                 String expression,
                                 UUID groupId) {
        def constraintId = dataSet.createConstraintUuid()
        sql.dataSet('sec_constraint').add(
                id: new PostgresUUID(constraintId),
                version: 1,
                check_type: checkType.getId(),
                entity_name: metaClass,
                groovy_script: expression,
                group_id: new PostgresUUID(groupId)
        )
        return constraintId
    }

    static UUID createUser(DataSet dataSet, Sql sql,
                           String login,
                           String password,
                           UUID groupId) {
        def userId = dataSet.createUserUuid()
        sql.dataSet('sec_user').add(
                id: new PostgresUUID(userId),
                version: 1,
                login: login,
                password: encryption.getPasswordHash(userId, password),
                password_encryption: encryption.hashMethod,
                login_lc: login.toLowerCase(),
                group_id: new PostgresUUID(groupId)
        )
        return userId
    }

    static UUID createRole(DataSet dataSet, Sql sql, String name) {
        def roleId = dataSet.createRoleUuid()
        sql.dataSet('sec_role').add(
                id: new PostgresUUID(roleId),
                version: 1,
                name: name
        )
        return roleId
    }

    static UUID createCar(DataSet dataSet, Sql sql, String vin) {
        def carId = dataSet.createCarUuid()
        sql.dataSet('ref_car').add(
                id: new PostgresUUID(carId),
                version: 1,
                vin: vin
        )
        return carId
    }

    static UUID createInsuranceCase(DataSet dataSet, Sql sql, String description, UUID carId) {
        def caseId = dataSet.createInsuranceCaseUuid()
        sql.dataSet('ref_insurance_case').add(
                id: new PostgresUUID(caseId),
                version: 1,
                description: description,
                car_id: new PostgresUUID(carId)
        )
        return caseId
    }

    static void updateInsuranceCase(Sql sql, UUID caseId, String description) {
        sql.executeUpdate('update ref_insurance_case set description = ? where id = ?',
                [description, new PostgresUUID(caseId)])
    }

    static UUID createPlant(DataSet dataSet, Sql sql, String name) {
        def plantId = dataSet.createPlantUuid()
        sql.dataSet('ref_plant').add(
                id: new PostgresUUID(plantId),
                version: 1,
                name: name,
                dtype: 'ref$CustomExtPlant'
        )
        return plantId
    }

    static UUID createModel(DataSet dataSet, Sql sql, String name) {
        def modelId = dataSet.createModelUuid()
        sql.dataSet('ref_model').add(
                id: new PostgresUUID(modelId),
                version: 1,
                name: name,
                dtype: 'ref$ExtModel'
        )
        return modelId
    }

    static void createPlantModelLink(Sql sql, UUID plantId, UUID modelId) {
        sql.dataSet('ref_plant_model_link').add(
                plant_id: new PostgresUUID(plantId),
                model_id: new PostgresUUID(modelId)
        )
    }

    static void updateModel(Sql sql, UUID modelId, String name) {
        sql.executeUpdate('update ref_model set name = ? where id = ?',
                [name, new PostgresUUID(modelId)])
    }
}
