## IaaS deployment

   IaaS deployment is a solution which enables to use any provider. It improves flexibility and reduces the provider dependencies by giving the migration very easily to do from one provider to another.
   The multi-IaaS connector enables to do CRUD operations on different infrastructures on public or private Cloud (AWS EC2, Openstack, VMWare, Docker, etc). It is connected with those infrastructure interfaces in order to manage their lifecycle on virtual machines.

## Manage infrastructures

### List supported infrastructures

    $ curl -k -X GET http://{IP_ADDRESS}/connector-iaas/infrastructures

### Save an infrastructure 

Generic information for saving an infrastructure are :
- id: the name of the given infrastructure in order to identify it
- type: the type is cloud nature or the hypervisor used (openstack-nova, aws-ec2, vmware, etc.)
- endpoint: it’s the reference to the interface which manage the infrastructure    
- credentials: the information which enables to connect

#### Openstack

For openstack, credentials information are made of the login and the password.

For saving an openstack infrastructure (in JSON), the information are :

```javascript
{
  "id": "openstack-infra-id",

  "type": "openstack-nova",

  "endpoint": "http://ip_address:5000/v2.0/",

  "credentials": {

    "username": "NAME:LOGIN",

    "password": "PWD"

  }
}
```

The curl command for save this infrastructure with the IaaS connector is :

    $ curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X POST -d '{"id": "openstack-infra-id","type": "openstack-nova","endPoint": "http://IP_ADDRESS:5000/v2.0/", "credentials": { "username": "NAME:LOGIN", "password": "PWD" }}' http://IP_ADDRESS:9080/connector-iaas/infrastructures

#### VMware

For saving a VMware infrastructure (in JSON), the information are :

```javascript
{
  "id": "vmware-infra-id",

  "type": "vmware",

  "endpoint": "https://IP_ADDRESS/sdk",

  "credentials": {

    "username": "NAME",

    "password": "PWD"
  }
}
```

	curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X POST -d '{"id": "vmware-infra-id","type": "vmware","endPoint": "https://ip_address/sdk", "credentials": { "username": "NAME", "password": "PWD" }}' http://IP_ADDRESS:9080/connector-iaas/infrastructures


#### AWS-EC2
An infrastructure AWS-EC2 needs to have an AWS account. Once the account is created, the user will have an AWS-key (used as a login) and a AWS-secret-key (used as a password). Unlike Openstack, VMware, the AWS-EC2 infrastructure creation doesn’t need to reference to the interface which enables to manage the infrastructure (ip adress and port). 

For saving a EC2 infrastructure (in JSON), the information are :

```javascript
{

  "id": "aws-infrastructure-id",

  "type": "aws-ec2",

  "credentials": {

    "username": "NAME",

    "password": "PWD"

  }
}
```

	$ curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X POST -d '{"id": "aws-infrastructure-id","type": "aws-ec2","credentials": { "username": "NAME", "password": "PWD" }}' http://IP_ADDRESS:9080/connector-iaas/infrastructures

### Manage the lifecycle of virtual machines
Once the infrastructure is saved, the virtual machines can be managed.

### Create an instance
The generic information for creating one or several instances are :
- tag: the instance name for identifying them
- image: the image to use
- number: the number of instances to deploy
- hardware: the information related to the ram or the cpu
