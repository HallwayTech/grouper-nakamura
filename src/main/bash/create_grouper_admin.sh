#!/bin/bash

ADMIN_PASS="admin"
HOST="localhost:8080"

curl -w '%{http_code}' -u admin:$ADMIN_PASS -F:name=grouper-admin -F:pwd=grouper -F:pwdConfirm=grouper http://$HOST/system/userManager/group.create.json

curl -w '%{http_code}' -u admin:$ADMIN_PASS -F:member=grouper-admin http://$HOST/system/userManager/group/administrators.update.json
