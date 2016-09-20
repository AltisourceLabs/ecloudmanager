package org.ecloudmanager.rest;

import com.google.common.base.Strings;
import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.node.model.APIInfo;
import org.ecloudmanager.repository.deployment.ApplicationDeploymentRepository;
import org.ecloudmanager.service.deployment.ApplicationDeploymentService;
import org.picketlink.Identity;

import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeploymentController {
    public ResponseContext getInfo(RequestContext request) {
        try {
            Identity identity = CDI.current().select(Identity.class).get();
            return new ResponseContext().status(Response.Status.OK).entity(new APIInfo().id(identity.getAccount().getId()));
        } catch (Exception e) {
            return new ResponseContext()
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage());
        }
    }

    public ResponseContext getDeployments(RequestContext request, String name) {
        try {
            Identity identity = CDI.current().select(Identity.class).get();
            ApplicationDeploymentRepository repository = CDI.current().select(ApplicationDeploymentRepository.class).get();
            ApplicationDeploymentService service = CDI.current().select(ApplicationDeploymentService.class).get();
            List<ApplicationDeployment> deployments = repository.getAll();
            Stream<ApplicationDeployment> deploymentsStream = Strings.isNullOrEmpty(name) ? deployments.stream() : deployments.stream().filter(d -> name.equals(d.getName()));
            List<String> ids = deploymentsStream.map(d -> d.getId().toString()).collect(Collectors.toList());

            return new ResponseContext().status(Response.Status.OK).entity(ids);
        } catch (Exception e) {
            return new ResponseContext()
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage());
        }
    }
}
