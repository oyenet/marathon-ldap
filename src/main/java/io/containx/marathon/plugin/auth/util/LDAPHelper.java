package io.containx.marathon.plugin.auth.util;

import com.sun.jndi.ldap.LdapCtxFactory;
import io.containx.marathon.plugin.auth.type.LDAPConfig;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.naming.directory.SearchControls.SUBTREE_SCOPE;

public final class LDAPHelper {

    private static final Logger LOG = Logger.getLogger(LDAPHelper.class.getName());
    private static final String LDAP_HOSTNAME = "ldap://%s/";

    public static Set<String> validate(String username, String password, LDAPConfig config ) {

        if (config == null) {
            LOG.warning("LDAP Configuration not defined.  Skipping LDAP authentication");
            return null;
        }

        Hashtable<String, String> props = new Hashtable<>();
        String principal = toPrincipal(username, config.getDomain());
        props.put(Context.SECURITY_PRINCIPAL, principal);
        props.put(Context.SECURITY_CREDENTIALS, password);

        try {
            DirContext context = LdapCtxFactory.getLdapCtxInstance(String.format(LDAP_HOSTNAME, config.getServer()), props);

            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Auth succeeded - Principal: " + principal);
            }

            SearchControls controls = new SearchControls();
            controls.setSearchScope(SUBTREE_SCOPE);

            NamingEnumeration<SearchResult> renum = context.search(toDC(config.getDomain()), "(& (userPrincipalName=" + principal + ")(objectClass=user))", controls);
            if (!renum.hasMore()) {
                LOG.warning("Cannot locate user information for " + username);
                return null;
            }
            SearchResult result = renum.next();

            Attribute memberOf = result.getAttributes().get("memberOf");
            Set<String> memberships = new HashSet<>();

            if (memberOf != null) {// null if this user belongs to no group at all
                for (int i = 0; i < memberOf.size(); i++) {
                    try {
                        Attributes atts = context.getAttributes(memberOf.get(i).toString(), new String[]{"CN"});
                        Attribute att = atts.get("CN");
                        memberships.add(att.get().toString());
                    } catch (PartialResultException e) {
                        // ignore
                    }
                }
            }

            context.close();
            return memberships;

        } catch (NamingException e) {
            LOG.log(Level.SEVERE, "LDAP Error: " + e.getMessage());
        }
        return null;
    }

    private static String toDC(String domainName) {
        StringBuilder buf = new StringBuilder();
        for (String token : domainName.split("\\.")) {
            if (token.length() == 0)
                continue; // defensive check
            if (buf.length() > 0)
                buf.append(",");
            buf.append("DC=").append(token);
        }
        return buf.toString();
    }

    private static String toPrincipal(String user, String domain) {
        return user + "@" + domain;
    }

}
