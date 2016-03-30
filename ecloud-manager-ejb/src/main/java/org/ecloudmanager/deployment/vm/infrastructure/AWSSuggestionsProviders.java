/*
 * MIT License
 *
 * Copyright (c) 2016  Altisource
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.ecloudmanager.deployment.vm.infrastructure;

import org.ecloudmanager.deployment.core.Config;
import org.ecloudmanager.deployment.core.ConstraintFieldSuggestionsProvider;
import org.ecloudmanager.deployment.core.DeploymentConstraint;
import org.ecloudmanager.service.aws.AWSVmFieldsCompletionService;

import javax.enterprise.inject.spi.CDI;
import java.util.List;

public class AWSSuggestionsProviders {
    public static class RegionSuggestionsProvider implements ConstraintFieldSuggestionsProvider {
        @Override
        public List<String> getSuggestions(DeploymentConstraint deploymentConstraint) {
            AWSVmFieldsCompletionService completionService = CDI.current().select(AWSVmFieldsCompletionService.class).get();
            return completionService.getRegions();
        }
    }

    public static RegionSuggestionsProvider createRegionSuggestionsProvider() {
        return new RegionSuggestionsProvider();
    }

    public static class SubnetSuggestionsProvider implements ConstraintFieldSuggestionsProvider {
        @Override
        public List<String> getSuggestions(DeploymentConstraint deploymentConstraint) {
            AWSVmFieldsCompletionService completionService = CDI.current().select(AWSVmFieldsCompletionService.class).get();
            String region = AWSInfrastructureDeployer.getAwsRegion((Config) deploymentConstraint);
            return completionService.getSubnets(region);
        }
    }

    public static SubnetSuggestionsProvider createSubnetSuggestionsProvider() {
        return new SubnetSuggestionsProvider();
    }

    public static class AmiSuggestionsProvider implements ConstraintFieldSuggestionsProvider {
        @Override
        public List<String> getSuggestions(DeploymentConstraint deploymentConstraint) {
            AWSVmFieldsCompletionService completionService = CDI.current().select(AWSVmFieldsCompletionService.class).get();
            String region = AWSInfrastructureDeployer.getAwsRegion((Config) deploymentConstraint);
            return completionService.getAmis(region);
        }
    }

    public static AmiSuggestionsProvider createAmiSuggestionsProvider() {
        return new AmiSuggestionsProvider();
    }

    public static class InstanceTypeSuggestionsProvider implements ConstraintFieldSuggestionsProvider {
        @Override
        public List<String> getSuggestions(DeploymentConstraint deploymentConstraint) {
            AWSVmFieldsCompletionService completionService = CDI.current().select(AWSVmFieldsCompletionService.class).get();
            return completionService.getInstanceTypes();
        }
    }

    public static InstanceTypeSuggestionsProvider createInstanceTypeSuggestionsProvider() {
        return new InstanceTypeSuggestionsProvider();
    }

    public static class KeypairSuggestionsProvider implements ConstraintFieldSuggestionsProvider {
        @Override
        public List<String> getSuggestions(DeploymentConstraint deploymentConstraint) {
            AWSVmFieldsCompletionService completionService = CDI.current().select(AWSVmFieldsCompletionService.class).get();
            String region = AWSInfrastructureDeployer.getAwsRegion((Config) deploymentConstraint);
            return completionService.getKeypairs(region);
        }
    }

    public static KeypairSuggestionsProvider createKeypairSuggestionsProvider() {
        return new KeypairSuggestionsProvider();
    }

    public static class HostedZoneSuggestionsProvider implements ConstraintFieldSuggestionsProvider {
        @Override
        public List<String> getSuggestions(DeploymentConstraint deploymentConstraint) {
            AWSVmFieldsCompletionService completionService = CDI.current().select(AWSVmFieldsCompletionService.class).get();
            return completionService.getHostedZones();
        }
    }

    public static HostedZoneSuggestionsProvider createHostedZoneSuggestionsProvider() {
        return new HostedZoneSuggestionsProvider();
    }

    public static class TagSuggestionsProvider implements ConstraintFieldSuggestionsProvider {
        private final String key;

        public TagSuggestionsProvider(String key) {
            this.key = key;
        }

        @Override
        public List<String> getSuggestions(DeploymentConstraint deploymentConstraint) {
            AWSVmFieldsCompletionService completionService = CDI.current().select(AWSVmFieldsCompletionService.class).get();
            String region = AWSInfrastructureDeployer.getAwsRegion((Config) deploymentConstraint);
            return completionService.getTagValues(region, key);
        }
    }

    public static TagSuggestionsProvider createTagSuggestionsProvider(String key) {
        return new TagSuggestionsProvider(key);
    }

    public static class SecurityGroupSuggestionsProvider implements ConstraintFieldSuggestionsProvider {
        @Override
        public List<String> getSuggestions(DeploymentConstraint deploymentConstraint) {
            AWSVmFieldsCompletionService completionService = CDI.current().select(AWSVmFieldsCompletionService.class).get();
            String region = AWSInfrastructureDeployer.getAwsRegion((Config) deploymentConstraint);
            return completionService.getSecurityGroups(region);
        }
    }

    public static SecurityGroupSuggestionsProvider createSecurityGroupSuggestionsProvider() {
        return new SecurityGroupSuggestionsProvider();
    }
}
