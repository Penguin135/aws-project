package awsTEst;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Scanner;
import java.util.ArrayList;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;

public class awsTest {

	static AmazonEC2 ec2;

	private static void init() throws Exception {
		// �⺻���� ������ ��ġ�� credential ���ϰ� config ���Ϸκ��� �ڰ����� ����
		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		try {
			credentialsProvider.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (~/.aws/credentials), and is in valid format.", e);
		}
		// �ڰ������ available region�� 'ap-northeast-2'�� ec2 ������ ����
		ec2 = AmazonEC2ClientBuilder.standard().withCredentials(credentialsProvider).withRegion("ap-northeast-2")
				.build();

	}

	// ec2�� ������ ����ӽŵ��� ��� ���
	public static void listInstances() {
		System.out.println("Listing instances....");
		boolean done = false;
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		while (!done) {
			DescribeInstancesResult response = ec2.describeInstances(request);
			for (Reservation reservation : response.getReservations()) {
				for (Instance instance : reservation.getInstances()) {
					if (instance.getState().getName().equals("terminated")) {// terminated ������ instance ����ó��
						System.out.print("terminatied instance");
						continue;
					}
					if (instance.getTags().isEmpty())// Instance�� Tag Name�� ��������� null ���
						System.out.printf("[Name] %12s, ", "null");
					else {// Instance�� Tag Name ���
						for (Tag tag : instance.getTags()) {
							System.out.print(String.format("[%s] %12s, ", tag.getKey(), tag.getValue()));
						}
					}
					// �ν��Ͻ��� id, ami, type, state, key pair name, security group, mornitoring state
					// ���
					System.out.printf(
							"[id] %s, " + "[AMI] %s, " + "[type] %s, " + "[state] %10s, " + "[key pair name] %10s, "
									+ "[security group] %15s, " + "[monitoring state] %s",
							instance.getInstanceId(), instance.getImageId(), instance.getInstanceType(),
							instance.getState().getName(), instance.getKeyName(),
							instance.getSecurityGroups().get(0).getGroupName(), instance.getMonitoring().getState());
				}
				System.out.println();

			}
			request.setNextToken(response.getNextToken());// ���� �ν��Ͻ�

			// ���̻� �ν��Ͻ��� ������ �ݺ��� ����
			if (response.getNextToken() == null) {
				done = true;
			}
		}
		return;
	}

	// instance ����
	public static void startInstance() {
		boolean success = false;
		Scanner input_id = new Scanner(System.in);
		String instance_id;// ������ instance�� id �Է�
		System.out.print("Enter instance id : ");
		
		instance_id = input_id.nextLine();

		// �׽�Ʈ�� �ϴ� dry run ��û. ��û�� ��ȯ ���� false�̸� �׽�Ʈ ����
		DryRunSupportedRequest<StartInstancesRequest> dry_request = () -> {
			StartInstancesRequest request = new StartInstancesRequest().withInstanceIds(instance_id);

			return request.getDryRunRequest();
		};

		try {
			// dry run�� ����� ec2���� ����
			DryRunResult dry_response = ec2.dryRun(dry_request);
			// dry run �� ����� (�׽�Ʈ �����)�� success�� ����
			success = dry_response.isSuccessful();

		} catch (Exception e) {
			System.out.printf("%s", e);
			return;
		}

		if (!success) {
			System.out.printf("Failed dry run to start instance %s", instance_id);
			return;
		}

		// success�� ���� true�̹Ƿ� �Է��� instance_id�� �ν��Ͻ� ���� ��û
		StartInstancesRequest request = new StartInstancesRequest().withInstanceIds(instance_id);

		try {
			// ����� ec2���� ��û ����(�ν��Ͻ� ����)
			ec2.startInstances(request);

		} catch (Exception e) {
			System.out.printf("%s", e);
			return;
		}
		System.out.printf("Successfully started instance %s", instance_id);
		return;
	}

	// instance ����
	public static void stopInstance() {
		boolean success = false;
		Scanner input_id = new Scanner(System.in);
		String instance_id;// ������ instance�� id�Է�
		System.out.print("Enter instance id : ");
		instance_id = input_id.nextLine();

		// �׽�Ʈ�� �ϴ� dry run ��û. ��û�� ��ȯ ���� false�̸� �׽�Ʈ ����
		DryRunSupportedRequest<StopInstancesRequest> dry_request = () -> {
			StopInstancesRequest request = new StopInstancesRequest().withInstanceIds(instance_id);
			return request.getDryRunRequest();
		};

		try {
		DryRunResult dry_response = ec2.dryRun(dry_request);
		success = dry_response.isSuccessful();
		}catch(Exception e) {
			System.out.printf("%s", e);
			return;
		}
		
		if (!success) {
			System.out.printf("Failed dry run to stop instance %s", instance_id);
			return;
		}
		
		try {
			// instance ���� ��û�� �ν��Ͻ� id�� ��û��
			StopInstancesRequest request = new StopInstancesRequest().withInstanceIds(instance_id);
			// ����� ec2���� �Է��� �ν��Ͻ� id�� �ش��ϴ� �ν��Ͻ��� ������
			ec2.stopInstances(request);
		} catch (Exception e) {
			System.out.printf("%s", e);
			return;
		}
		System.out.printf("Successfully stop instance %s", instance_id);
		return;
	}

	// instance �����
	public static void rebootInstance() {
		Scanner input_id = new Scanner(System.in);
		String instance_id;// ����� �� �ν��Ͻ��� id �Է�
		System.out.print("Enter instance id : ");
		instance_id = input_id.nextLine();
		try {
			// ����� ��û�� �ν��Ͻ� id�� ��û
			RebootInstancesRequest request = new RebootInstancesRequest().withInstanceIds(instance_id);
			// ����� ec2���� �ν��Ͻ� �����
			RebootInstancesResult response = ec2.rebootInstances(request);
		} catch (Exception e) {
			System.out.printf("%s", e);
			return;
		}
		System.out.printf("Successfully rebooted instance %s", instance_id);
		return;
	}

	// �̹��� ��� ���
	public static void imageList() {
		// AmazonIdentityManagementClient ��ü�� ����Ͽ� ������ȣ�� �˳���
		AmazonIdentityManagementClient iamClient = new AmazonIdentityManagementClient();
		String accountNumber = iamClient.getUser().getUser().getArn().split(":")[4];

		// ������ȣ�� �������� ������ ec2������ Image ����Ʈ�� ������
		DescribeImagesRequest request = new DescribeImagesRequest().withOwners(accountNumber);
		DescribeImagesResult response = ec2.describeImages(request);

		// Image �������� ���
		for (Image reservation : response.getImages()) {
			System.out.printf("[ImageID] %s, [State] %s, [Owner] %s, [Name] %s", reservation.getImageId(),
					reservation.getState(), reservation.getOwnerId(), reservation.getName());
			System.out.println();

		}
	}

	// instance ����
	public static void createInstance() {
		Scanner input = new Scanner(System.in);
		String image_id;// ������ ����� image id
		String keypair_name;// ������ ����� keypair �̸�
		String security_group_name;// ������ ����� ���ȱ׷� �̸�
		String instance_name;// ������ �ν��Ͻ��� �̸�

		Instance save_instance;
		
		System.out.print("Enter image id : ");
		image_id = input.nextLine();
		System.out.print("Enter key pair name : ");
		keypair_name = input.nextLine();
		System.out.print("Enter security group name : ");
		security_group_name = input.nextLine();
		System.out.print("Enter instance name you want : ");
		instance_name = input.nextLine();

		//�̹��� id, T2Micro(����Ƽ�� Ÿ��), ������ �Ѱ�, Ű��� �̸�, ���ȱ׷� �̸����� �ν��Ͻ� ���� ��û�� ��
		RunInstancesRequest run_request = new RunInstancesRequest().withImageId(image_id)
				.withInstanceType(com.amazonaws.services.ec2.model.InstanceType.T2Micro).withMaxCount(1).withMinCount(1)
				.withKeyName(keypair_name).withSecurityGroups(security_group_name);

		try {
			//����� ec2���� �ν��Ͻ�����
			RunInstancesResult run_response = ec2.runInstances(run_request);
			// ������ �ν��Ͻ��� �ش��ϴ� �ν��Ͻ��� ����
			save_instance = run_response.getReservation().getInstances().get(0);
		} catch (Exception e) {
			System.out.printf("%s", e);
			return;
		}

		// ������ �ν��Ͻ��� id�� string���� ������
		String instance_id = save_instance.getInstanceId();

		// ������ �ν��Ͻ��� 'Name' Tag�� ������ �Է��� �ν��Ͻ� �̸� ����
		CreateTagsRequest createTagsRequest = new CreateTagsRequest().withResources(save_instance.getInstanceId())
				.withTags(new Tag("Name", instance_name));
		ec2.createTags(createTagsRequest);// �±� ����

		System.out.printf("Successfully started EC2 instance [Name] %s, [Id] %s, [Base AMI] %s", instance_name,
				instance_id, image_id);
	}

	// avaibable_zones ���
	public static void available_zones() {
		try {
			// ������ ec2 �������� ���� ��밡���� zone ����� �޾ƿ�
			DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();

			// �޾ƿ� zone�� �̸��� zone�� id ���
			for (int i = 0; i < availabilityZonesResult.getAvailabilityZones().size(); i++) {
				System.out.print("[Zone name] " + availabilityZonesResult.getAvailabilityZones().get(i).getZoneName()
						+ ", [Zone id] " + availabilityZonesResult.getAvailabilityZones().get(i).getZoneId());
				System.out.println();

			}

		} catch (AmazonServiceException ase) {
			System.out.println("Caught Exception: " + ase.getMessage());
			System.out.println("Reponse Status Code: " + ase.getStatusCode());
			System.out.println("Error Code: " + ase.getErrorCode());
			System.out.println("Request ID: " + ase.getRequestId());
		}

	}

	// available region ���. available zone���� available region�� �ִµ�, �ߺ��Ǵ� region��
	// �����Ͽ� ���
	public static void available_regions() {
		ArrayList<String> save = new ArrayList<String>();// �ߺ� ���Ÿ� ���� region �̸� ����
		try {
			DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();
			for (int i = 0; i < availabilityZonesResult.getAvailabilityZones().size(); i++) {
				// save �迭�� ����Ϸ��� region�̸��� �ȵ�� ������ ����� ��, save �迭�� �߰�
				if (!save.contains(availabilityZonesResult.getAvailabilityZones().get(i).getRegionName())) {
					System.out.print("[Available region] "
							+ availabilityZonesResult.getAvailabilityZones().get(i).getRegionName());
					save.add(availabilityZonesResult.getAvailabilityZones().get(i).getRegionName());
					System.out.println();
				}
			}
		} catch (AmazonServiceException ase) {
			System.out.println("Caught Exception: " + ase.getMessage());
			System.out.println("Reponse Status Code: " + ase.getStatusCode());
			System.out.println("Error Code: " + ase.getErrorCode());
			System.out.println("Request ID: " + ase.getRequestId());
		}

	}

	// keypair ����
	static void createKeypair() {
		Scanner input = new Scanner(System.in);
		String key_name;// keypair�� �̸�
		System.out.print("Enter keypair name : ");
		key_name = input.nextLine();

		// Ű��� ������ ��û�� ��, ����� ec2������ Ű��� ������ ��
		try {
			CreateKeyPairRequest request = new CreateKeyPairRequest().withKeyName(key_name);
			CreateKeyPairResult response = ec2.createKeyPair(request);
		} catch (Exception e) {// �̹� �����ϴ� Ű����� �̸��� ���� �̸��� �Է��ϸ� ����ó��
			System.out.printf("%s", e);
			return;
		}
		System.out.printf("Successfully created key pair named %s", key_name);
		return;
	}

	// Ű��� ��� ���
	static void keypairList() {
		// Ű�� ���� ������ ��û�� �� �޾ƿ�
		DescribeKeyPairsRequest request = new DescribeKeyPairsRequest();
		DescribeKeyPairsResult response = ec2.describeKeyPairs(request);

		// �޾ƿ� Ű����� �̸��� ���.
		for (int i = 0; i < response.getKeyPairs().size(); i++) {
			System.out.print("[key pair name] " + response.getKeyPairs().get(i).getKeyName() + '\n');
		}
	}

	// Ű��� ����
	static void deleteKeypair() {
		Scanner input = new Scanner(System.in);
		String key_name;// ������ Ű����� �̸�
		System.out.print("Enter keypair name to delete : ");
		key_name = input.nextLine();
		try {
			DeleteKeyPairRequest request = new DeleteKeyPairRequest().withKeyName(key_name);
			DeleteKeyPairResult response = ec2.deleteKeyPair(request);

		} catch (Exception e) {
			System.out.printf("%s", e);
			return;
		}
		System.out.printf("Successfully deleted key pair named %s", key_name);
	}

	// ���ȱ׷� ��� ���
	static void securitygroupList() {
		// ���ȱ׷� ������ ��û�Ͽ� �޾ƿ�
		DescribeSecurityGroupsRequest request = new DescribeSecurityGroupsRequest();
		DescribeSecurityGroupsResult response = ec2.describeSecurityGroups(request);

		// �޾ƿ� ���ȱ׷���� id�� name�� ���
		for (SecurityGroup group : response.getSecurityGroups()) {
			System.out.printf("[security group id] %20s, " + "[security group name] %15s ", group.getGroupId(),
					group.getGroupName());
			System.out.println();
		}
	}

	// ���ȱ׷� ����
	static void createSecuritygroup() {
		Scanner input = new Scanner(System.in);
		System.out.print("enter security group name : ");
		String group_name = input.nextLine();// ������ ���� �׷��� �̸�

		System.out.print("enter description for security group : ");
		String group_desc = input.nextLine();// ������ ���� �׷��� description �Է�(���ȱ׷쿡 �Է��� �� �ִ� ����)

		try {
			// ���ȱ׷� ���� ��û�� ���ȱ׷��� �̸��� ���ȱ׷��� description���� ��û��
			CreateSecurityGroupRequest create_request = new CreateSecurityGroupRequest().withGroupName(group_name)
					.withDescription(group_desc);

			// ����� ec2 ��������� ����
			CreateSecurityGroupResult create_response = ec2.createSecurityGroup(create_request);
		} catch (Exception e) {
			System.out.printf("%s", e);
			return;
		}

		// ���� �׷쿡 �����ϸ� �����ߴٴ� �޽��� ���
		System.out.printf("Successfully created security group named %s", group_name);

		// ���ȱ׷쿡 tcp��� ��Ʈ�� 80, ssh ��� ��Ʈ�� 22 ��Ģ�� �߰��Ͽ� ���ȱ׷� ����
		IpRange ip_range = new IpRange().withCidrIp("0.0.0.0/0");

		IpPermission ip_perm = new IpPermission().withIpProtocol("tcp").withToPort(80).withFromPort(80)
				.withIpv4Ranges(ip_range);

		IpPermission ip_perm2 = new IpPermission().withIpProtocol("tcp").withToPort(22).withFromPort(22)
				.withIpv4Ranges(ip_range);

		try {
			AuthorizeSecurityGroupIngressRequest auth_request = new AuthorizeSecurityGroupIngressRequest()
					.withGroupName(group_name).withIpPermissions(ip_perm, ip_perm2);

			AuthorizeSecurityGroupIngressResult auth_response = ec2.authorizeSecurityGroupIngress(auth_request);
		} catch (Exception e) {
			System.out.printf("%s", e);
			return;
		}
		System.out.print("Successfully added ingress policy to security group" + group_name);
		return;
	}

	// ���ȱ׷� ����
	static void deleteSecuritygroup() {
		Scanner input = new Scanner(System.in);
		System.out.print("Enter security group name to be deleted : ");
		String group_name = input.nextLine();// ������ ���� �׷��� �̸� �Է�

		try {
			DeleteSecurityGroupRequest delete_request = new DeleteSecurityGroupRequest().withGroupName(group_name);
			DeleteSecurityGroupResult delete_response = ec2.deleteSecurityGroup(delete_request);
		} catch (Exception e) {
			System.out.printf("%s", e);
			return;
		}
		System.out.printf("Successfully deleted security group named %s", group_name);
		return;
	}

	// instance ����
	public static void deleteInstance() {
		Scanner input = new Scanner(System.in);
		System.out.print("Enter id to be deleted : ");
		String delete_instance_id = input.nextLine();// ������ instance�� id �Է�

		// aws ec2������ instance�� ������ terminate��. �׷��� TerminateInstance�� instance�� id��
		// ��û��.
		try {
			TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest()
					.withInstanceIds(delete_instance_id);
			ec2.terminateInstances(terminateRequest);
		} catch (Exception e) {
			System.out.printf("%s", e);
			return;
		}
		System.out.printf("Successfully deleted security group id %s", delete_instance_id);
		return;
	}

	// AMI �̹��� ����
	public static void createImage() {
		Scanner input = new Scanner(System.in);
		System.out.print("Enter image name : ");
		String image_name = input.nextLine();// ������ �̹����� �̸� �Է�

		System.out.print("Enter source instance id : ");
		String source_instance_id = input.nextLine();// �̹����� ����� �� instance�� id �Է�

		try {
			// �̹��� ���� ��û�� ������ �̹����� �̸��� �̹����� ����� �� ����ӽ��� id�� ��û��
			CreateImageRequest create_request = new CreateImageRequest().withName(image_name)
					.withInstanceId(source_instance_id);

			// ec2�� ami ����
			CreateImageResult create_response = ec2.createImage(create_request);
		} catch (Exception e) {
			System.out.printf("%s", e);
			return;
		}
		System.out.printf("Successfully created image named %s", image_name);
		return;
	}

	// AMI �̹��� ����
	public static void deleteImage() {
		Scanner input = new Scanner(System.in);
		System.out.print("Enter image id to be deleted : ");
		String image_id = input.nextLine();// ������ �̹����� id �Է�

		try {
			// Image�� Deregister(��ϻ���)��û�� ������ �̹����� id�� ��û�Ѵ�.
			DeregisterImageRequest deregister_request = new DeregisterImageRequest().withImageId(image_id);

			// ����� ec2�������� ��û�� ���� �̹��� id�� �ش��ϴ� �̹����� �����Ѵ�.
			DeregisterImageResult deregister_response = ec2.deregisterImage(deregister_request);
		} catch (Exception e) {
			System.out.printf("%s", e);
			return;
		}
		System.out.printf("Successfully deleted image");
		return;

	}

	public static void main(String[] args) throws Exception {

		System.out.println("===========================================");
		System.out.println("made by 2015041031 �����");
		System.out.println("===========================================");

		init();

		Scanner menu = new Scanner(System.in);
		int number = 0;
		boolean roop = true;
		while (roop) {
			System.out.println(" ");
			System.out.println(" ");
			System.out.println("------------------------------------------------------------");
			System.out.println(" Amazon AWS Control Panel using AWS SDK for JAVA ");
			System.out.println(" ");
			System.out.println(" Cloud Computing, Computer Science Department ");
			System.out.println(" at Chungbuk National University ");
			System.out.println(" Developed by Kim Kyeon Jun");
			System.out.println("------------------------------------------------------------");
			System.out.println(" 1. list instance 		2. available zones ");
			System.out.println(" 3. start instance 		4. available regions ");
			System.out.println(" 5. stop instance 		6. create instance ");
			System.out.println(" 7. reboot instance 		8. list images ");
			System.out.println(" 9. create key pair 		10. key pair list");
			System.out.println(" 11. delete key pair 		12. security group list");
			System.out.println(" 13. create security group	14. delete security group");
			System.out.println(" 15. delete instance		16. create image");
			System.out.println(" 17. delete image");
			System.out.println(" 99. quit ");
			System.out.println("------------------------------------------------------------");
			System.out.print("Enter an integer: ");

			number = menu.nextInt();
			switch (number) {
			case 1:
				listInstances();
				break;
			case 2:
				available_zones();
				break;
			case 3:
				startInstance();
				break;
			case 4:
				available_regions();
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
			case 9:
				createKeypair();
				break;
			case 10:
				keypairList();
				break;
			case 11:
				deleteKeypair();
				break;
			case 12:
				securitygroupList();
				break;
			case 13:
				createSecuritygroup();
				break;
			case 14:
				deleteSecuritygroup();
				break;
			case 15:
				deleteInstance();
				break;
			case 16:
				createImage();
				break;
			case 17:
				deleteImage();
				break;
			case 99:
				roop = false;
				break;
			}
		}
		System.out.print("Bye~");
	}

}