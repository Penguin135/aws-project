package awsTEst;
/*
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.acmpca.model.Tag;
import com.amazonaws.services.applicationdiscovery.model.CreateTagsRequest;
import com.amazonaws.services.applicationdiscovery.model.CreateTagsResult;
import com.amazonaws.services.codedeploy.model.InstanceType;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DryRunResult;
import com.amazonaws.services.ec2.model.DryRunSupportedRequest;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.RebootInstancesResult;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;

import com.amazonaws.services.ec2.model.DescribeImageAttributeRequest;
import com.amazonaws.services.ec2.model.*;
/**
 * Welcome to your new AWS Java SDK based project!
 *
 * This class is meant as a starting point for your console-based application that
 * makes one or more calls to the AWS services supported by the Java SDK, such as EC2,
 * SimpleDB, and S3.
 *
 * In order to use the services in this sample, you need:
 *
 *  - A valid Amazon Web Services account. You can register for AWS at:
 *       https://aws-portal.amazon.com/gp/aws/developer/registration/index.html
 *
 *  - Your account's Access Key ID and Secret Access Key:
 *       http://aws.amazon.com/security-credentials
 *
 *  - A subscription to Amazon EC2. You can sign up for EC2 at:
 *       http://aws.amazon.com/ec2/
 *
 *  - A subscription to Amazon SimpleDB. You can sign up for Simple DB at:
 *       http://aws.amazon.com/simpledb/
 *
 *  - A subscription to Amazon S3. You can sign up for S3 at:
 *       http://aws.amazon.com/s3/
 */
public class awsTest {
	
	static List<String> id_list = new ArrayList<String>();
	
	
    /*
     * Before running the code:
     *      Fill in your AWS access credentials in the provided credentials
     *      file template, and be sure to move the file to the default location
     *      (~/.aws/credentials) where the sample code will load the
     *      credentials from.
     *      https://console.aws.amazon.com/iam/home?#security_credential
     *
     * WARNING:
     *      To avoid accidental leakage of your credentials, DO NOT keep
     *      the credentials file in your source directory.
     */

    static AmazonEC2      ec2;

    /**
     * The only information needed to create a client are security credentials
     * consisting of the AWS Access Key ID and Secret Access Key. All other
     * configuration, such as the service endpoints, are performed
     * automatically. Client parameters, such as proxies, can be specified in an
     * optional ClientConfiguration object when constructing a client.
     *
     * @see com.amazonaws.auth.BasicAWSCredentials
     * @see com.amazonaws.auth.PropertiesCredentials
     * @see com.amazonaws.ClientConfiguration
     */
    private static void init() throws Exception {

        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
            
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        ec2 = AmazonEC2ClientBuilder.standard()
            .withCredentials(credentialsProvider)
            .withRegion("ap-northeast-2")
            .build();
       
    }

    
    public static void listInstances(){
    	System.out.println("Listing instances....");
    	boolean done = false;
    	DescribeInstancesRequest request = new DescribeInstancesRequest();
    	while(!done) {
    			DescribeInstancesResult response = ec2.describeInstances(request);
    			for(Reservation reservation : response.getReservations()) {
    				for(Instance instance : reservation.getInstances()) {
    					System.out.printf("[id] %s, " + "[AMI] %s, " +  "[type] %s, " + "[state] %10s, " +  "[monitoring state] %s",
    							instance.getInstanceId(),
    							instance.getImageId(),
    							instance.getInstanceType(),
    							instance.getState().getName(),
    							instance.getMonitoring().getState());
    					id_list.add(instance.getInstanceId());
    				}
    				System.out.println();
    				
    			}
    			request.setNextToken(response.getNextToken());
    			if(response.getNextToken() == null) {
    				done = true;
    			}
    	}
    	return;
    }
    
    //instance Ω√¿€
    public static void startInstance()
    {
    	System.out.print("id list : ");
    	for (int i=0; i<id_list.size(); i++){
    		System.out.format("%s ", id_list.get(i));
    	}
    	System.out.println();
    	Scanner input_id = new Scanner(System.in);
    	String instance_id;
    	System.out.print("Enter instance id : ");
    	instance_id = input_id.nextLine();
    	
    	
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DryRunSupportedRequest<StartInstancesRequest> dry_request =
            () -> {
            StartInstancesRequest request = new StartInstancesRequest()
                .withInstanceIds(instance_id);

            return request.getDryRunRequest();
        };

        DryRunResult dry_response = ec2.dryRun(dry_request);

        if(!dry_response.isSuccessful()) {
            System.out.printf("Failed dry run to start instance %s", instance_id);
            //throw dry_response.getDryRunResponse();
        }

        StartInstancesRequest request = new StartInstancesRequest()
            .withInstanceIds(instance_id);

        ec2.startInstances(request);

        System.out.printf("Successfully started instance %s", instance_id);
    }

    public static void stopInstance()
    {
    	Scanner input_id = new Scanner(System.in);
    	String instance_id;
    	System.out.print("Enter instance id : ");
    	instance_id = input_id.nextLine();
    	
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DryRunSupportedRequest<StopInstancesRequest> dry_request =
            () -> {
            StopInstancesRequest request = new StopInstancesRequest()
                .withInstanceIds(instance_id);

            return request.getDryRunRequest();
        };

        DryRunResult dry_response = ec2.dryRun(dry_request);

        if(!dry_response.isSuccessful()) {
            System.out.printf(
                "Failed dry run to stop instance %s", instance_id);
            throw dry_response.getDryRunResponse();
        }

        StopInstancesRequest request = new StopInstancesRequest()
            .withInstanceIds(instance_id);

        ec2.stopInstances(request);

        System.out.printf("Successfully stop instance %s", instance_id);
    }

    public static void rebootInstance() {
    	Scanner input_id = new Scanner(System.in);
    	String instance_id;
    	System.out.print("Enter instance id : ");
    	instance_id = input_id.nextLine();
    	
    	 final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

         RebootInstancesRequest request = new RebootInstancesRequest()
             .withInstanceIds(instance_id);

         RebootInstancesResult response = ec2.rebootInstances(request);

         System.out.printf(
             "Successfully rebooted instance %s", instance_id);
    }
    
    
    public static void imageList() {

    	//DescribeImagesRequest request = new DescribeImagesRequest()
    	boolean done= false;
    	DescribeImagesRequest request = new DescribeImagesRequest().withOwners("395389474517");
    	DescribeImagesResult response = ec2.describeImages(request);
    			for(Image reservation : response.getImages()) {    				
    				System.out.printf("[ImageID] %s, [Name] %s, [Owner] %s", reservation.getImageId(), reservation.getName(), reservation.getOwnerId());
    				System.out.println();
    			}


    			
    			
    			
    }
    
    public static void createInstance() {
    	Scanner input = new Scanner(System.in);
    	String image_id;

    	System.out.print("Enter instance id : ");
    	image_id = input.nextLine();
    	 final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

         RunInstancesRequest run_request = new RunInstancesRequest()
             .withImageId(image_id)
             .withInstanceType(com.amazonaws.services.ec2.model.InstanceType.T2Micro)
             .withMaxCount(1)
             .withMinCount(1);

         RunInstancesResult run_response = ec2.runInstances(run_request);

         String reservation_id = run_response.getReservation().getInstances().get(0).getInstanceId();



         System.out.printf(
             "Successfully started EC2 instance %s based on AMI %s",
             reservation_id, image_id);
     }
    
    
    
    public static void main(String[] args) throws Exception {

        System.out.println("===========================================");
        System.out.println("Welcome to the AWS Java SDK!");
        System.out.println("===========================================");

        init();

        Scanner menu = new Scanner(System.in);
        Scanner id_string = new Scanner(System.in);
        int number = 0;
        
        /*
         * Amazon EC2
         *
         * The AWS EC2 client allows you to create, delete, and administer
         * instances programmatically.
         *
         * In this sample, we use an EC2 client to get a list of all the
         * availability zones, and all instances sorted by reservation id.
         */
        
        while(true)
        {
        System.out.println(" ");
        System.out.println(" ");
        System.out.println("------------------------------------------------------------");
        System.out.println(" Amazon AWS Control Panel using SDK ");
        System.out.println(" ");
        System.out.println(" Cloud Computing, Computer Science Department ");
        System.out.println(" at Chungbuk National University ");
        System.out.println("------------------------------------------------------------");
        System.out.println(" 1. list instance 2. available zones ");
        System.out.println(" 3. start instance 4. available regions ");
        System.out.println(" 5. stop instance 6. create instance ");
        System.out.println(" 7. reboot instance 8. list images ");
        System.out.println(" 99. quit ");
        System.out.println("------------------------------------------------------------");
        System.out.print("Enter an integer: ");
        
        number=menu.nextInt();
        switch(number) {
        case 1:
        listInstances();
        break;
        case 3:
        	startInstance();
        break;
        case 5:
        	stopInstance();
        	break;
        case 6:
        	createInstance();
        	break;
        case 7:
        	rebootInstance();
        	break;
        case 8:
        	imageList();
        	break;
        }
        }
        /*
        try {
            DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();
            System.out.println("You have access to " + availabilityZonesResult.getAvailabilityZones().size() +
                    " Availability Zones.");

            DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
            List<Reservation> reservations = describeInstancesRequest.getReservations();
            Set<Instance> instances = new HashSet<Instance>();

            for (Reservation reservation : reservations) {
                instances.addAll(reservation.getInstances());
            }

            System.out.println("You have " + instances.size() + " Amazon EC2 instance(s) running.");
        } catch (AmazonServiceException ase) {
                System.out.println("Caught Exception: " + ase.getMessage());
                System.out.println("Reponse Status Code: " + ase.getStatusCode());
                System.out.println("Error Code: " + ase.getErrorCode());
                System.out.println("Request ID: " + ase.getRequestId());
        }
        */




    }
    
    
}
