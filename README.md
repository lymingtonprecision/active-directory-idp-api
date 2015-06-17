active-directory-idp-api (ad-idp-api)
=====================================

A library to facilitate the use of Active Directory as an OAuth IDP.

## Status

Technically a work-in-progress though it actually amounts to some code
I wrote once, some time ago, and haven't been able to return to since.

The basic functionality of looking up users and authenticating them is
complete and REPL tested. Management of the accounts is still
something that needs to happen using the Active Directory Users and
Computers management console.

Methods for managing Clients, applications that need to authenticate
for access to other applications, are "feature complete" and REPL
tested.

See the [documentation] or [API Codox] for exact details of the
currently implemented API.

[documentation]: http://lymingtonprecision.github.io/active-directory-idp-api/doc/uberdoc.html
[API Codox]: http://lymingtonprecision.github.io/active-directory-idp-api/doc/api/index.html

## Intended Scope

The intended scope of this project is to provide an API that can be
used as the _backend_ of an OAuth IDP but to not actually deal
directly with any OAuth specifics. As such it's something of a generic
user/client application authentication API that uses Active Directory
as the store that it is explicitly tied to OAuth.

Actual OAuth/OpenID endpoints need to be built on top of this.

## Licence

Copyright © 2015 Lymington Precision Engineers Co. Ltd.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
