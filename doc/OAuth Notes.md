Authorization API
=================

* Performs Authorisation Grants and Implicit Grants

* Performs Client Credential Grants

* Validates issued tokens
  (see https://developers.google.com/accounts/docs/OAuth2UserAgent#validatetoken
  for inspiration)

* Answers claims queries regarding access rights

Rest APIs
=========

* Require an auth token for non-public uris/methods
  Either:
    * HTTP `Authorization` header
    * a uri parameter (`access_token`)
    * a POST parameter (`access_token`)
  Per http://tools.ietf.org/html/draft-ietf-oauth-v2-bearer-08

* Validate auth tokens for every request
  *May* cache token validation results

* Query for access rights claimed by token user

* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

API Authentication:

* client ID
* client secret

User Authentication:

* username
* password

**APIs are _clients_ Users are _users_**

API Flow
========

```
User -> Web Page -> Server : API -> API {ID, secret}
                                 <-     {access token}
                        (OAuth Client Credentials Grant)
                                     |
                                     v
                                    IDP/validate
                                     |
                                     v
                                    IDP/claims
                                     |
                                     v
                                    has_claim? <access right>
```

APIs can authenticate by performing a Client Credentials Grant
with their own client ID and secret.

User Flow
=========

```
User -> Web Page -> Server
        Login    <-
User -> IDP -> /login {username, password}
      (OAuth Authorisation Grant) (or Implicit Grant)
                  |
                  v
               Server/auth?code=<code> -> IDP/validate
                                                |
                                                v
                                          IDP/claims
                                                |
                                                v
                                          has_claim? <access right>
                                                |
Web Page <---------------------------------------
   |
   ------> API {user,token} -> API {user,token}
            |                   |
            v                   v
          IDP/validate        IDP/validate
            |                   |
            v                   v
          IDP/claims          IDP/claims
            |                   |
            v                   v
          has_claim?          has_claim?
```

Users authenticate and generate a bearer token for the "app" that they
are authenticating for.

__Can this token be passed onwards to other APIs to authenticate the user?
Is that a case for an Implicit Grant?__

Authenticating an LPE employee
==============================

Needs:

* Active Directory server address
* User credentials (username and password)

Things users can do:

* login
* logout
* view profile (name, email address, group membership)
