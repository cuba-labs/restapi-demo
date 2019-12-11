package spec.rest.demo.rest

import com.haulmont.cuba.security.entity.PermissionType
import com.haulmont.cuba.security.entity.RoleType
import com.haulmont.masquerade.Connectors
import com.haulmont.rest.demo.http.api.DataSet
import com.haulmont.rest.demo.http.rest.jmx.WebConfigStorageJmxService
import groovy.sql.Sql
import org.apache.http.HttpStatus
import spock.lang.Specification

import static org.hamcrest.CoreMatchers.notNullValue
import static org.hamcrest.CoreMatchers.nullValue
import static spec.rest.demo.rest.DataUtils.*
import static spec.rest.demo.rest.DbUtils.getSql
import static spec.rest.demo.rest.RestSpecsUtils.createRequest
import static spec.rest.demo.rest.RestSpecsUtils.getAuthToken

class SecurityProfileEntityControllerFT extends Specification {

    private Sql sql
    private DataSet dirtyData = new DataSet()

    private String userPassword = "password"
    private String userLogin = "user1"

    private UUID carId

    void setup() {
        sql = getSql() as Sql

        def groupId = createGroup(dirtyData, sql, 'Group')

        UUID userId = createUser(dirtyData, sql,
                userLogin, userPassword, groupId)

        UUID roleId = createRole(dirtyData, sql, 'TestStrictDenyingRole', RoleType.STRICTLY_DENYING)

        //access REST API
        createPermission(dirtyData, sql, roleId, PermissionType.SPECIFIC, 'cuba.restApi.enabled', 1)

        createUserRoleWithProfile(dirtyData, sql, userId, roleId, "REST")

        UUID colorId = createColor(dirtyData, sql, 'Black')

        carId = createCarWithColour(dirtyData, sql, 'carVin1', colorId)

    }

    void cleanup() {
        dirtyData.cleanup(sql.connection)
        if (sql != null) {
            sql.close()
        }
    }

    def "user has access to load car"() {
        when:

        def request = createRequest(getAuthToken(userLogin, userPassword)).param('view', 'carEdit')
        def response = request.with().get("/entities/ref_Car/$carId")

        then:

        response.then().statusCode(HttpStatus.SC_OK)
                .body('vin', notNullValue())
                .body("id", notNullValue())
                .body("colour", notNullValue())
    }

    def "user hasn't access to load car with security profile REST"() {
        setup:

        Connectors.jmx(WebConfigStorageJmxService.class)
                .setAppProperty("cuba.rest.securityScope", "REST")

        when:

        def request = createRequest(getAuthToken(userLogin, userPassword)).param('view', 'carEdit')
        def response = request.with().get("/entities/ref_Car/$carId")

        then:

        response.then().statusCode(HttpStatus.SC_FORBIDDEN)

        cleanup:
        Connectors.jmx(WebConfigStorageJmxService.class)
                .setAppProperty("cuba.rest.securityProfile", null)
    }
}
