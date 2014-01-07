CSHLDAP
=======

CSH LDAP library implemented in Java

CSHLDAP is a Java 6 library designed to simplify interaction with LDAP.  Credit to [gambogi]https://github.com/gambogi/CSHLDAP for the idea.

## Installation

No need to install, just add the Java file to your project

## Basic Usage

Create a LDAP Object

    LDAP ldap = new LDAP(username, password);

Get a listing of all members:
   Member m = ldap.getMembers()

Note:  This is fairly slow and should be avoided

Get a specific member:
    HashMap<String, String> searchAttrs = new HashMap<String, String>();
    searchAttrs.put("uid", "userToLookFor")
    ldap.search(searchAttrs)

This will return a list of members matching what is searched

See JavaDocs in the code or email ahanes@csh.rit.edu for additional assistance
