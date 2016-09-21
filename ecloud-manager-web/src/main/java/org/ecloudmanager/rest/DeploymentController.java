package org.ecloudmanager.rest;

import com.google.common.base.Strings;
import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.history.DeploymentAttempt;
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

    public ResponseContext getDeployment(RequestContext request, String id) {
        ApplicationDeploymentRepository repository = CDI.current().select(ApplicationDeploymentRepository.class).get();
        ApplicationDeployment ad = repository.get(id);
        if (ad == null) {
            return new ResponseContext().status(Response.Status.NOT_FOUND);
        }
        return new ResponseContext().status(Response.Status.OK).entity(ad.getName());
    }

    public ResponseContext deploy(RequestContext request, String id) {
        ApplicationDeploymentRepository repository = CDI.current().select(ApplicationDeploymentRepository.class).get();
        ApplicationDeploymentService service = CDI.current().select(ApplicationDeploymentService.class).get();

        ApplicationDeployment ad = repository.get(id);
        if (ad == null) {
            return new ResponseContext().status(Response.Status.NOT_FOUND);
        }
        DeploymentAttempt attempt = service.getDeployAction(ad);
        ActionFuture future = new ActionFuture();
        String taskId = Tasks.addTask(future);
        service.execute(attempt, future, future);
        return new ResponseContext().status(Response.Status.ACCEPTED).entity(taskId);
    }
}