#!/bin/bash

curl -k -I https://admin:admin@127.0.0.1:9443/carbon/admin/login.jsp

#curl -Lk -XGET -u "${API_USER}:${API_HASH}" -b cookies.txt -c cookies.txt -- "http://admin:admin@127.0.0.1:9443/carbon/admin/login.jsp"

#curl -k --location --head 'https://admin:admin@127.0.0.1:9443/carbon/admin/login.jsp' \
#--header 'Cookie: JSESSIONID=61B94F3F47102869805DA53CFEB67379'
