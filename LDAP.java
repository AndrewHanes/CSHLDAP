/**
 * @author Andrew Hanes
 *         Date: 1/5/14
 *         Time: 4:44 PM
 */

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.*;
import java.util.*;

public class LDAP {

    private String base ="ou=Users,dc=csh,dc=rit,dc=edu";
    private String bind="ou=Apps,dc=csh,dc=rit,dc=edu";
    private InitialDirContext context;

    /**
     * Constructor for CSH LDAP
     * @param username Username to use
     * @param pass Password to use
     * @throws OperationNotSupportedException
     * @throws NamingException
     */
    public LDAP(String username, String pass) throws NamingException {

        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
        properties.put(Context.PROVIDER_URL, "ldap://ldap.csh.rit.edu");
        properties.put(Context.REFERRAL, "ignore");
        properties.put(Context.SECURITY_AUTHENTICATION, "simple");

        properties.put(Context.SECURITY_PRINCIPAL, "uid="+username+","+base);
        properties.put(Context.SECURITY_CREDENTIALS, pass);
        this.context = new InitialDirContext(properties);
    }

    /**
     * Constructor for SASL
     *
     * See http://docs.oracle.com/javase/tutorial/jndi/ldap/sasl.html for more info
     * @param username
     * @param pass
     * @param sasl
     */
    public LDAP(String username, String pass, String sasl) throws NamingException{
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
        properties.put(Context.PROVIDER_URL, "ldap://ldap.csh.rit.edu");
        properties.put(Context.REFERRAL, "ignore");
        properties.put(Context.SECURITY_PRINCIPAL, "uid="+username+","+base);
        properties.put(Context.SECURITY_AUTHENTICATION, sasl);
        properties.put(Context.SECURITY_CREDENTIALS, pass);
        this.context = new InitialDirContext( properties);
    }

    /**
     * Gets all members
     *
     * **SLOW**
     *
     * @return list of all members and attributes.
     * @throws NamingException
     */
    public List<Member> getMembers() throws NamingException {
        return ldapSearch("(uid=*)");
    }

    /**
     * searches for users with attributes matching the ones specified in filters
     *
     * @param filters Hash map to use for searches
     * @return List of members matching criteria
     * @throws NamingException
     */
    public List<Member> search(HashMap<String, String> filters) throws NamingException {
        String search = "";
        for(String key : filters.keySet()) {
            search+="("+key+'='+filters.get(key)+")";
        }
        search = "(&"+search+')';
        return ldapSearch(search);
    }

    /**
     * Gets a list of memners with uid
     *
     * This method is simply a short cut for ldapsearch("(uid="<UID>")"
     *
     * @param uid The uid to search for
     * @return
     * @throws NamingException
     */
    public List<Member> getMember(String uid) throws NamingException {
        return this.ldapSearch(("(uid="+uid+")"));
    }

    /**
     * Performs an ldap search
     * @param searchString String to use for searching
     * @return List of members that match the search string
     * @throws NamingException
     */
    public List<Member> ldapSearch(String searchString) throws NamingException {
        List<Member> l = new ArrayList<Member>();
        SearchControls search = new SearchControls();
        search.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchFilter = searchString;
        String[] arr = {"memberOf", "*"};
        search.setReturningAttributes(arr);
        NamingEnumeration a = context.search(base, searchFilter, search);
        //System.out.println(a.toString());
        while(a.hasMore()) {
            SearchResult r = (SearchResult) a.nextElement();
            l.add(new Member(r));
            //System.out.println(r.getAttributes().get("uid"));
        }
        return l;
    }

    /**
     * Modify a user's attributes to what is supplied in modify
     * key -> Attribute name
     * value -> Attribute value
     *
     * @param member
     * @param modify
     * @throws NamingException
     */
    public void modifyMember(Member member, HashMap<String, String> modify) throws NamingException {
        ModificationItem[] m = new ModificationItem[modify.keySet().size()];
        int ptr = 0;
        for(String key : modify.keySet()) {
            m[ptr] = new ModificationItem(InitialDirContext.REPLACE_ATTRIBUTE, new BasicAttribute(key,
                    modify.get(key)));
            ptr++;
        }
        String s = "uid="+member.lookup("uid")+",ou=Users,dc=csh,dc=rit,dc=edu";
        this.context.modifyAttributes(s, m);
    }

    /**
     * Class representing an individual member
     */
    public class Member {
        private SearchResult sr;
        private List<String> atts = null;

        /**
         * Built in ldap search
         * @param r
         * @throws NamingException
         */
        private Member(SearchResult r) throws NamingException {
            this.sr = r;
        }

        /**
         * Gets a list of all attributes
         * @return list of all attributes as strings (Note: NOT attribute values)
         * @throws NamingException
         */
        public List<String> getAttributes() throws NamingException {
            NamingEnumeration<String> a = sr.getAttributes().getIDs();
            List lst = new ArrayList<String>();
            while(a.hasMore()) {
                lst.add(a.nextElement());
            }
            return lst;
        }

        /**
         * Looks up an attribute
         * @param key The key to use for lookup
         * @return Value looked up
         * @precondition Key must exist (use getAttributes().contains)
         */
        public String lookup(String key) {
            return sr.getAttributes().get(key).toString().split(": ")[1];
        }
    }
}
