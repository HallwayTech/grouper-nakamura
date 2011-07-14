#!/bin/bash

ADMIN_PASS="admin"
HOST="localhost:8080"

curl -w '%{http_code}' -u admin:$ADMIN_PASS -F:name=grouper-admin -Fpwd=grouper -FpwdConfirm=grouper http://$HOST/system/userManager/user.create.json

curl -w '%{http_code}' -u admin:$ADMIN_PASS -F:member=grouper-admin http://$HOST/system/userManager/group/administrators.update.json
