package org.ecloudmanager.service.aws;

import com.amazonaws.regions.RegionUtils;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListHostedZonesResult;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.Logger;
import org.ecloudmanager.deployment.core.ConstraintFieldSuggestion;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AWSVmFieldsCompletionService {
    @Inject
    private AWSClientService awsClientService;
    @Inject
    private Logger log;

    public List<String> getRegions() {
        return Stream.of(Regions.values())
                .map(Regions::getName)
                .collect(Collectors.toList());
    }

    public List<ConstraintFieldSuggestion> getSubnets(String region) {
        if (region == null) {
            return Collections.emptyList();
        }
        AmazonEC2 amazonEC2 = awsClientService.getAmazonEC2(RegionUtils.getRegion(region));
        DescribeSubnetsResult describeSubnetsResult = amazonEC2.describeSubnets();
        return describeSubnetsResult.getSubnets().stream()
                .map(subnet -> new ConstraintFieldSuggestion(getSubnetLabel(subnet), subnet.getSubnetId()))
                .collect(Collectors.toList());
    }

    private String getSubnetLabel(Subnet subnet) {
        String subnetName = subnet.getTags().stream()
                .filter(t -> t.getKey().equals("Name"))
                .map(Tag::getValue)
                .findAny()
                .orElse("");

        return subnet.getSubnetId() + " (" + subnet.getCidrBlock() + " | " + subnetName + ")";
    }

    public List<String> getAmis(String region) {
        if (region == null) {
            return Collections.emptyList();
        }
        // TODO - too many results to display
        AmazonEC2 amazonEC2 = awsClientService.getAmazonEC2(RegionUtils.getRegion(region));
        DescribeImagesResult describeImagesResult = amazonEC2.describeImages();
        return describeImagesResult.getImages().stream()
                .map(Image::getImageId)
                .collect(Collectors.toList());
    }

    public List<String> getKeypairs(String region) {
        if (region == null) {
            return Collections.emptyList();
        }
        AmazonEC2 amazonEC2 = awsClientService.getAmazonEC2(RegionUtils.getRegion(region));
        DescribeKeyPairsResult describeKeyPairsResult = amazonEC2.describeKeyPairs();
        return describeKeyPairsResult.getKeyPairs().stream()
                .map(KeyPairInfo::getKeyName)
                .collect(Collectors.toList());
    }

    public List<String> getInstanceTypes() {
        return Stream.of(AWSInstanceType.values())
                .map(AWSInstanceType::getInstanceTypeName)
                .collect(Collectors.toList());
    }

    public List<String> getHostedZones() {
        AmazonRoute53 route53Client = awsClientService.getRoute53Client();
        ListHostedZonesResult listHostedZonesResult = route53Client.listHostedZones();
        return listHostedZonesResult.getHostedZones().stream()
                .map(HostedZone::getName)
                .collect(Collectors.toList());
    }

    public List<String> getTagValues(String region, String key) {
        if (region == null) {
            return Collections.emptyList();
        }
        AmazonEC2 amazonEC2 = awsClientService.getAmazonEC2(RegionUtils.getRegion(region));
        DescribeTagsResult describeTagsResult = amazonEC2.describeTags(new DescribeTagsRequest().withFilters(new Filter("key", Lists.newArrayList(key))));
        return describeTagsResult.getTags().stream()
                .map(TagDescription::getValue)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<ConstraintFieldSuggestion> getSecurityGroups(String region, String subnetId) {
        if (region == null || subnetId == null) {
            return Collections.emptyList();
        }
        AmazonEC2 amazonEC2 = awsClientService.getAmazonEC2(RegionUtils.getRegion(region));
        DescribeSubnetsResult describeSubnetsResult = amazonEC2.describeSubnets(new DescribeSubnetsRequest().withSubnetIds(subnetId));
        String vpcId = describeSubnetsResult.getSubnets().get(0).getVpcId();

        DescribeSecurityGroupsRequest securityGroupsRequest = new DescribeSecurityGroupsRequest().withFilters(new Filter("vpc-id").withValues(vpcId));
        DescribeSecurityGroupsResult describeSecurityGroupsResult = amazonEC2.describeSecurityGroups(securityGroupsRequest);
        return describeSecurityGroupsResult.getSecurityGroups().stream()
                .map(sg -> new ConstraintFieldSuggestion(sg.getGroupId() + " (" + sg.getGroupName() + " | " + sg.getDescription() + ")", sg.getGroupId()))
                .collect(Collectors.toList());
    }
}
