#
# Recursively delete a stem and everything below it.
#
# From https://bugs.internet2.edu/jira/browse/GRP-187
#

grouperSession = GrouperSession.startRootSession();

stem = StemFinder.findByName(grouperSession, "edu");
for(child : stem.getChildGroups(Stem.Scope.SUB)) { System.out.println("deleting: "+  child.getName()); child.delete(); }
stemList = new ArrayList(stem.getChildStems(Stem.Scope.SUB));

Collections.sort(stemList);

Collections.reverse(stemList);

for(childStem : stemList) {System.out.println("deleting: " + childStem.getName()); childStem.delete();}
stem.delete();

StemFinder.findByName(grouperSession, "edu");
