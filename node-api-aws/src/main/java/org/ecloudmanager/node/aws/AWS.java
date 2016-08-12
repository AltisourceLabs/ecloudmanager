package org.ecloudmanager.node.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53Client;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * Created by vship on 7/19/2016.
 */
public class AWS {
    private static LoadingCache<RegionWithCredentials, AmazonEC2> clientCache = CacheBuilder.newBuilder().build(new CacheLoader<RegionWithCredentials,
            AmazonEC2>() {
        @Override
        public AmazonEC2 load(RegionWithCredentials rws) throws Exception {
            return rws.getRegion().createClient(AmazonEC2Client.class, rws.getCredentialsProvider(), null);
        }
    });

    private static AWSCredentialsProvider getCredentialsProvider(String accessKey, String secretKey) {
        return new AWSCredentialsProvider() {
            AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

            @Override
            public AWSCredentials getCredentials() {
                return credentials;
            }

            @Override
            public void refresh() {
            }

            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof AWSCredentialsProvider))
                    return false;

                AWSCredentialsProvider other = (AWSCredentialsProvider) obj;
                return this.credentials.getAWSAccessKeyId().equals(other.getCredentials().getAWSAccessKeyId()) && this.credentials.getAWSSecretKey().equals(other.getCredentials().getAWSSecretKey());
            }

            @Override
            public int hashCode() {
                return Objects.hash(credentials.getAWSAccessKeyId(), credentials.getAWSSecretKey());
            }
        };
    }

    private static AmazonEC2 ec2(AWSCredentialsProvider credentials, Region region) throws ExecutionException {
        return clientCache.get(new RegionWithCredentials(region, credentials));
    }

    public static AmazonEC2 ec2(String accessKey, String secretKey, String region) throws ExecutionException, RegionNotExistException {
        AWSCredentialsProvider credentials = getCredentialsProvider(accessKey, secretKey);
        Region awsRegion = RegionUtils.getRegion(region);
        if (awsRegion == null) {
            throw new RegionNotExistException(region);
        }
        return clientCache.get(new RegionWithCredentials(awsRegion, credentials));
    }

    public static AmazonRoute53 route53(String accessKey, String secretKey) {
        AWSCredentialsProvider credentials = getCredentialsProvider(accessKey, secretKey);
        return new AmazonRoute53Client(credentials);
    }

    public static AmazonIdentityManagementClient identityManagementClient(String accessKey, String secretKey) {
        return new AmazonIdentityManagementClient(getCredentialsProvider(accessKey, secretKey));
    }

    private static class RegionWithCredentials {
        private Region region;
        private AWSCredentialsProvider credentialsProvider;

        RegionWithCredentials(Region region, AWSCredentialsProvider credentialsProvider) {
            this.region = region;
            this.credentialsProvider = credentialsProvider;
        }

        Region getRegion() {
            return region;
        }

        AWSCredentialsProvider getCredentialsProvider() {
            return credentialsProvider;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof RegionWithCredentials))
                return false;

            RegionWithCredentials other = (RegionWithCredentials) obj;
            return this.region.equals(other.region) && this.credentialsProvider.equals(other.credentialsProvider);
        }

        @Override
        public int hashCode() {
            return Objects.hash(region, credentialsProvider);
        }
    }

    public static class RegionNotExistException extends Exception {
        private String region;

        public RegionNotExistException(String region) {
            super("Region not exist: " + region);
            this.region = region;
        }

        public String getRegion() {
            return region;
        }

    }


}
