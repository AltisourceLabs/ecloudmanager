package org.ecloudmanager.tmrk.cloudapi.service.organization;

import org.ecloudmanager.tmrk.cloudapi.CloudapiEndpointTestSupport;
import org.ecloudmanager.tmrk.cloudapi.model.OrganizationsType;
import org.junit.Assert;
import org.junit.Test;

public class OrganizationServiceTest extends CloudapiEndpointTestSupport<OrganizationService> {


    @Test
    public void shouldGetOrganizations() {
        // given organization service endpoint
        // when request organizations
        OrganizationsType result = endpoint.getOrganizations();
        // then result is not null
        Assert.assertNotNull(result);
        // and href matching expected
        Assert.assertEquals("/cloudapi/ecloud/organizations/", result.getHref());

        print(result);
    }
}
