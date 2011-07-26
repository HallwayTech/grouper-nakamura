#
# Create users in Grouper so the OAE-Builder
# can run and be synced properly.
#
# Erik Froese <e@hallwaytech.com>

grouperSession = GrouperSession.startRootSession()

addSubject("user1", "person", "user1");
addSubject("user2", "person", "user2");
addSubject("user3", "person", "user3");
addSubject("user4", "person", "user4");
addSubject("user5", "person", "user5");
