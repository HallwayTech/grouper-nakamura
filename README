This bundle sends mirrors Group actions in SakaiOAE to a Grouper server via web services.

# Information Flow #
+ SakaiOAE (nakamura) emits OSGi events for Authorizable objects. 
The GrouperJMSMessageProducer is notified of OSGi events and adds them to a JMS queue as messages.
+ The GrouperJMSMessageConsumer listens for JMS messages on that queue and makes calls to a GrouperManager.
+ The GrouperManagerImpl sends HTTP POSTs to Grouper web services to add/remove groups and group memberships.
+ If the message has been successfully processed it is removed from the queue and we proceed to the next message.

When we add a group in sakai we also create a group in grouper. We store a property on the Authorizable: grouper:name => stema:stemb:stemc:groupname. Unfortunately this causes another authorizables/UPDATED event to fire. To get around this we filter out messages caused by a specific user. In testing we've used grouper-admin. This is configurable, see below.

# Prerequisites #
1. SakaiOAE running version 0.10+
2. Grouper server version 1.6.3+ (tested with 1.6.3 and 1.7.0)
3. Grouper Web Services. We use only the RESTful JSON web services.

# Installation #
## Create an admin user for Grouper ##
Create a user in Grouper for the SakaiOAE application server. The process for this will vary depending on 
how you have Grouper authentication set up. The user should have a stem where it has permission to create
new stems and groups. We refer to this in the code as the base stem.

## Install this bundle ##
Use mvn to install the bundle or you can build it and install it by hand using the felix web admin console.

	git clone git://github.com/HallwayTech/grouper-nakamura.git
	cd grouper-nakamura
    mvn clean install -Pdeploy

## Configure the bundle ##

You can use the felix web admin console to configure this bundle. Log in, go to the configuration tab, and look for 
Sakai Nakamura :: Grouper Client. Once you've configured it the way you'd like you can copy the configuration to 
your nakamura/load directory or wherever you've configured fileinstall to read configurations

    cp nakamura/sling/config/edu/nyu/grouper/GrouperConfigurationImpl.config \
        nakamura/load/edu.nyu.grouper.GrouperConfigurationImpl.config
 
# Links #
Nakamura, the SakaiOAE back-end built on Apache Sling:
http://sakaiproject.org/projects/sakai-3
https://confluence.sakaiproject.org/display/KERNDOC/Nakamura+Documentation

Grouper:
https://spaces.internet2.edu/display/Grouper/
https://spaces.internet2.edu/display/Grouper/Grouper+Web+Services
