Authorize LPE user:

* append '@lymingtonprecision.co.uk' to username
* bind as user using provided password

Authorize non-LPE user:

* bind as OAuth user
* lookup contact with provided email address
* hash provided password
* compare hashed password against password attribute of contact

LDAP Data structure:

```
oauth/
      users/
            <email addr> {:objectClass "contact"
                          :name
                          :givenName
                          :sn
                          :mail
                          :userPassword
                          :expirationTime
                          :o ["type=supplier,id=50001"]
                          :memberOf}
      apps/
           <app name> {:objectClass "organizationalUnit"
                       :name
                       :objectGUID // client-id
                       :userPassword // client-secret
                       :url
                       :memberOf}
                      /
                       <group> {:objectClass "group"
                                :name
                                :members}

users/
      <employee name> {:name
                       :givenName
                       :sn
                       :mail
                       :userAccountControl // hex value
                                           // if bit-and with 0x0002 <> 0 then
                                           // account is disabled
                       :memberOf //scoped to groups in the oauth tree}
```

Would need a "fixed" admin group for the OAuth portal management that grants
access to:

* create external users
* create apps
* modify groups associated with apps
* add external users to app groups
* add internal users to app groups

App structure:

* OAuth API/Web
  * authorize
  * validate
  --
  * provide userinfo

* Management API
  * register apps
  * register users
* Management Web
  * expose management api functionality

Need to:
  * find users
    * find a specific user {username/email dn}
    * find users in an organization
    * find active users
  * create users
  * modify users {password name organization}
  * disable users
  * validate passwords

  * create apps
  * list apps
  * modify apps {name secret}

  * create app groups
  * modify app groups {name}
  * add users to groups
  * remove users from groups
