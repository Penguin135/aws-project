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
		// 기본으로 설정된 위치의 credential 파일과 config 파일로부터 자격증명 시작
		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		try {
			credentialsProvider.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (~/.aws/credentials), and is in valid format.", e);
		}
		// 자격증명과 available region인 'ap-northeast-2'로 ec2 계정과 연결
		ec2 = AmazonEC2ClientBuilder.standard().withCredentials(credentialsProvider).withRegion("ap-northeast-2")
				.build();

	}

	// ec2에 생성된 가상머신들의 목록 출력
	public static void listInstances() {
		System.out.println("Listing instances....");
		boolean done = false;
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		while (!done) {
			DescribeInstancesResult response = ec2.describeInstances(request);
			for (Reservation reservation : response.getReservations()) {
				for (Instance instance : reservation.getInstances()) {
					if (instance.getState().getName().equals("terminated")) {// terminated 상태인 instance 예외처리
						System.out.print("terminatied instance");
						continue;
					}
					if (instance.getTags().isEmpty())// Instance의 Tag Name이 비어있으면 null 출력
						System.out.printf("[Name] %12s, ", "null");
					else {// Instance의 Tag Name 출력
						for (Tag tag : instance.getTags()) {
							System.out.print(String.format("[%s] %12s, ", tag.getKey(), tag.getValue()));
						}
					}
					// 인스턴스의 id, ami, type, state, key pair name, security group, mornitoring state
					// 출력
					System.out.printf(
							"[id] %s, " + "[AMI] %s, " + "[type] %s, " + "[state] %10s, " + "[key pair name] %10s, "
									+ "[security group] %15s, " + "[monitoring state] %s",
							instance.getInstanceId(), instance.getImageId(), instance.getInstanceType(),
							instance.getState().getName(), instance.getKeyName(),
							instance.getSecurityGroups().get(0).getGroupName(), instance.getMonitoring().getState());
				}
				System.out.println();

			}
			request.setNextToken(response.getNextToken());// 다음 인스턴스

			// 더이상 인스턴스가 없으면 반복문 종료
			if (response.getNextToken() == null) {
				done = true;
			}
		}
		return;
	}

	// instance 시작
	public static void startInstance() {
		boolean success = false;
		Scanner input_id = new Scanner(System.in);
		String instance_id;// 시작할 instance의 id 입력
		System.out.print("Enter instance id : ");
		
		instance_id = input_id.nextLine();

		// 테스트를 하는 dry run 요청. 요청의 반환 값이 false이면 테스트 실패
		DryRunSupportedRequest<StartInstancesRequest> dry_request = () -> {
			StartInstancesRequest request = new StartInstancesRequest().withInstanceIds(instance_id);

			return request.getDryRunRequest();
		};

		try {
			// dry run을 연결된 ec2에서 실행
			DryRunResult dry_response = ec2.dryRun(dry_request);
			// dry run 의 결과값 (테스트 결과값)을 success에 저장
			success = dry_response.isSuccessful();

		} catch (Exception e) {
			System.out.printf("%s", e);
			return;
		}

		if (!success) {
			System.out.printf("Failed dry run to start instance %s", instance_id);
			return;
		}

		// success의 값이 true이므로 입력한 instance_id로 인스턴스 실행 요청
		StartInstancesRequest request = new StartInstancesRequest().withInstanceIds(instance_id);

		try {
			// 연결된 ec2에서 요청 실행(인스턴스 실행)
			ec2.startInstances(request);

		} catch (Exception e) {
			System.out.printf("%s", e);
			return;
		}
		System.out.printf("Successfully started instance %s", instance_id);
		return;
	}

	// instance 정지
	public static void stopInstance() {
		boolean success = false;
		Scanner input_id = new Scanner(System.in);
		String instance_id;// 정지할 instance의 id입력
		System.out.print("Enter instance id : ");
		instance_id = input_id.nextLine();

		// 테스트를 하는 dry run 요청. 요청의 반환 값이 false이면 테스트 실패
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
			// instance 정지 요청을 인스턴스 id로 요청함
			StopInstancesRequest request = new StopInstancesRequest().withInstanceIds(instance_id);
			// 연결된 ec2에서 입력한 인스턴스 id에 해당하는 인스턴스를 정지함
			ec2.stopInstances(request);
		} catch (Exception e) {
			System.out.printf("%s", e);
			return;
		}
		System.out.printf("Successfully stop instance %s", instance_id);
		return;
	}

	// instance 재시작
	public static void rebootInstance() {
		Scanner input_id = new Scanner(System.in);
		String instance_id;// 재시작 할 인스턴스의 id 입력
		System.out.print("Enter instance id : ");
		instance_id = input_id.nextLine();
		try {
			// 재시작 요청을 인스턴스 id로 요청
			RebootInstancesRequest request = new RebootInstancesRequest().withInstanceIds(instance_id);
			// 연결된 ec2에서 인스턴스 재시작
			RebootInstancesResult response = ec2.rebootInstances(request);
		} catch (Exception e) {
			System.out.printf("%s", e);
			return;
		}
		System.out.printf("Successfully rebooted instance %s", instance_id);
		return;
	}

	// 이미지 목록 출력
	public static void imageList() {
		// AmazonIdentityManagementClient 객체를 사용하여 계정번호를 알나냄
		AmazonIdentityManagementClient iamClient = new AmazonIdentityManagementClient();
		String accountNumber = iamClient.getUser().getUser().getArn().split(":")[4];

		// 계정번호를 기준으로 연결한 ec2계정의 Image 리스트를 가져옴
		DescribeImagesRequest request = new DescribeImagesRequest().withOwners(accountNumber);
		DescribeImagesResult response = ec2.describeImages(request);

		// Image 정보들을 출력
		for (Image reservation : response.getImages()) {
			System.out.printf("[ImageID] %s, [State] %s, [Owner] %s, [Name] %s", reservation.getImageId(),
					reservation.getState(), reservation.getOwnerId(), reservation.getName());
			System.out.println();

		}
	}

	// instance 생성
	public static void createInstance() {
		Scanner input = new Scanner(System.in);
		String image_id;// 생성에 사용할 image id
		String keypair_name;// 생성에 사용할 keypair 이름
		String security_group_name;// 생성에 사용할 보안그룹 이름
		String instance_name;// 생성할 인스턴스의 이름

		Instance save_instance;
		
		System.out.print("Enter image id : ");
		image_id = input.nextLine();
		System.out.print("Enter key pair name : ");
		keypair_name = input.nextLine();
		System.out.print("Enter security group name : ");
		security_group_name = input.nextLine();
		System.out.print("Enter instance name you want : ");
		instance_name = input.nextLine();

		//이미지 id, T2Micro(프리티어 타입), 개수는 한개, 키페어 이름, 보안그룹 이름으로 인스턴스 생성 요청을 함
		RunInstancesRequest run_request = new RunInstancesRequest().withImageId(image_id)
				.withInstanceType(com.amazonaws.services.ec2.model.InstanceType.T2Micro).withMaxCount(1).withMinCount(1)
				.withKeyName(keypair_name).withSecurityGroups(security_group_name);

		try {
			//연결된 ec2에서 인스턴스생성
			RunInstancesResult run_response = ec2.runInstances(run_request);
			// 생성한 인스턴스에 해당하는 인스턴스를 저장
			save_instance = run_response.getReservation().getInstances().get(0);
		} catch (Exception e) {
			System.out.printf("%s", e);
			return;
		}

		// 생성된 인스턴스의 id를 string으로 가져옴
		String instance_id = save_instance.getInstanceId();

		// 생성된 인스턴스의 'Name' Tag에 위에서 입력한 인스턴스 이름 지정
		CreateTagsRequest createTagsRequest = new CreateTagsRequest().withResources(save_instance.getInstanceId())
				.withTags(new Tag("Name", instance_name));
		ec2.createTags(createTagsRequest);// 태그 생성

		System.out.printf("Successfully started EC2 instance [Name] %s, [Id] %s, [Base AMI] %s", instance_name,
				instance_id, image_id);
	}

	// avaibable_zones 출력
	public static void available_zones() {
		try {
			// 연결한 ec2 계정으로 부터 사용가능한 zone 목록을 받아옴
			DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();

			// 받아온 zone의 이름과 zone의 id 출력
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

	// available region 출력. available zone마다 available region이 있는데, 중복되는 region을
	// 제거하여 출력
	public static void available_regions() {
		ArrayList<String> save = new ArrayList<String>();// 중복 제거를 위한 region 이름 저장
		try {
			DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();
			for (int i = 0; i < availabilityZonesResult.getAvailabilityZones().size(); i++) {
				// save 배열에 출력하려는 region이름이 안들어 있으면 출력한 뒤, save 배열에 추가
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

	// keypair 생성
	static void createKeypair() {
		Scanner input = new Scanner(System.in);
		String key_name;// keypair의 이름
		System.out.print("Enter keypair name : ");
		key_name = input.nextLine();

		// 키페어 생성을 요청한 뒤, 연결된 ec2계정에 키페어 생성을 함
		try {
			CreateKeyPairRequest request = new CreateKeyPairRequest().withKeyName(key_name);
			CreateKeyPairResult response = ec2.createKeyPair(request);
		} catch (Exception e) {// 이미 존재하는 키페어의 이름과 같은 이름을 입력하면 예외처리
			System.out.printf("%s", e);
			return;
		}
		System.out.printf("Successfully created key pair named %s", key_name);
		return;
	}

	// 키페어 목록 출력
	static void keypairList() {
		// 키페어에 대한 정보를 요청한 뒤 받아옴
		DescribeKeyPairsRequest request = new DescribeKeyPairsRequest();
		DescribeKeyPairsResult response = ec2.describeKeyPairs(request);

		// 받아온 키페어의 이름을 출력.
		for (int i = 0; i < response.getKeyPairs().size(); i++) {
			System.out.print("[key pair name] " + response.getKeyPairs().get(i).getKeyName() + '\n');
		}
	}

	// 키페어 삭제
	static void deleteKeypair() {
		Scanner input = new Scanner(System.in);
		String key_name;// 삭제할 키페어의 이름
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

	// 보안그룹 목록 출력
	static void securitygroupList() {
		// 보안그룹 정보를 요청하여 받아옴
		DescribeSecurityGroupsRequest request = new DescribeSecurityGroupsRequest();
		DescribeSecurityGroupsResult response = ec2.describeSecurityGroups(request);

		// 받아온 보안그룹들의 id와 name을 출력
		for (SecurityGroup group : response.getSecurityGroups()) {
			System.out.printf("[security group id] %20s, " + "[security group name] %15s ", group.getGroupId(),
					group.getGroupName());
			System.out.println();
		}
	}

	// 보안그룹 생성
	static void createSecuritygroup() {
		Scanner input = new Scanner(System.in);
		System.out.print("enter security group name : ");
		String group_name = input.nextLine();// 생성할 보안 그룹의 이름

		System.out.print("enter description for security group : ");
		String group_desc = input.nextLine();// 생성할 보안 그룹의 description 입력(보안그룹에 입력할 수 있는 설명)

		try {
			// 보안그룹 생성 요청을 보안그룹의 이름과 보안그룹의 description으로 요청함
			CreateSecurityGroupRequest create_request = new CreateSecurityGroupRequest().withGroupName(group_name)
					.withDescription(group_desc);

			// 연결된 ec2 생성결과를 받음
			CreateSecurityGroupResult create_response = ec2.createSecurityGroup(create_request);
		} catch (Exception e) {
			System.out.printf("%s", e);
			return;
		}

		// 보안 그룹에 성공하면 성공했다는 메시지 출력
		System.out.printf("Successfully created security group named %s", group_name);

		// 보안그룹에 tcp통신 포트인 80, ssh 통신 포트인 22 규칙을 추가하여 보안그룹 생성
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

	// 보안그럽 삭제
	static void deleteSecuritygroup() {
		Scanner input = new Scanner(System.in);
		System.out.print("Enter security group name to be deleted : ");
		String group_name = input.nextLine();// 삭제할 보안 그룹의 이름 입력

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

	// instance 삭제
	public static void deleteInstance() {
		Scanner input = new Scanner(System.in);
		System.out.print("Enter id to be deleted : ");
		String delete_instance_id = input.nextLine();// 삭제할 instance의 id 입력

		// aws ec2에서는 instance의 삭제가 terminate임. 그래서 TerminateInstance를 instance의 id로
		// 요청함.
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

	// AMI 이미지 생성
	public static void createImage() {
		Scanner input = new Scanner(System.in);
		System.out.print("Enter image name : ");
		String image_name = input.nextLine();// 생성할 이미지의 이름 입력

		System.out.print("Enter source instance id : ");
		String source_instance_id = input.nextLine();// 이미지의 대상이 될 instance의 id 입력

		try {
			// 이미지 생성 요청을 생성할 이미지의 이름과 이미지의 대상이 될 가상머신의 id로 요청함
			CreateImageRequest create_request = new CreateImageRequest().withName(image_name)
					.withInstanceId(source_instance_id);

			// ec2에 ami 생성
			CreateImageResult create_response = ec2.createImage(create_request);
		} catch (Exception e) {
			System.out.printf("%s", e);
			return;
		}
		System.out.printf("Successfully created image named %s", image_name);
		return;
	}

	// AMI 이미지 삭제
	public static void deleteImage() {
		Scanner input = new Scanner(System.in);
		System.out.print("Enter image id to be deleted : ");
		String image_id = input.nextLine();// 삭제할 이미지의 id 입력

		try {
			// Image를 Deregister(등록삭제)요청을 삭제할 이미지의 id로 요청한다.
			DeregisterImageRequest deregister_request = new DeregisterImageRequest().withImageId(image_id);

			// 연결된 ec2계정에서 요청에 사용된 이미지 id에 해당하는 이미지를 삭제한다.
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
		System.out.println("made by 2015041031 김경준");
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