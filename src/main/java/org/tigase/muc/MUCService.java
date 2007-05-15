/*
 * tigase-muc Copyright (C) 2007 by Bartosz M. Małkowski
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * $Id$
 */
package org.tigase.muc;

import java.util.Map;
import java.util.logging.Logger;

import tigase.db.RepositoryFactory;
import tigase.db.UserRepository;
import tigase.server.AbstractMessageReceiver;
import tigase.server.Packet;
import tigase.server.xmppsession.SessionManagerConfig;
import tigase.util.DNSResolver;
import tigase.xml.Element;

/**
 * Implements MUC service for tigase server.
 * <p>
 * Created: 2007-01-24 13:02:15
 * </p>
 * 
 * @author bmalkow
 * @version $Rev$
 */
public class MUCService extends AbstractMessageReceiver {

    private static final String MUC_REPO_CLASS_PROP_KEY = "muc-repo-class";

    private static final String MUC_REPO_CLASS_PROP_VAL = "tigase.db.xml.XMLRepository";

    private static final String MUC_REPO_URL_PROP_KEY = "muc-repo-url";

    private static final String MUC_REPO_URL_PROP_VAL = "muc-repository.xml";

    /**
     * Default service name. Will be set in 'from' stanza attribute when 'ftom'
     * was <code>null</code>.
     */
    private String defaultServiceHost;

    private Logger log = Logger.getLogger(this.getClass().getName());

    /**
     * Construct MUC service.
     */
    public MUCService() {

    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Object> getDefaults(Map<String, Object> params) {
        Map<String, Object> props = super.getDefaults(params);
        String[] hostnamesPropVal = null;
        if (params.get("--virt-hosts") != null) {
            hostnamesPropVal = ((String) params.get("--virt-hosts")).split(",");
        } else {
            hostnamesPropVal = DNSResolver.getDefHostNames();
        }
        for (int i = 0; i < hostnamesPropVal.length; i++) {
            hostnamesPropVal[i] = "muc." + hostnamesPropVal[i];
        }

        props.put(MUC_REPO_CLASS_PROP_KEY, MUC_REPO_CLASS_PROP_VAL);
        props.put(MUC_REPO_URL_PROP_KEY, MUC_REPO_URL_PROP_VAL);

        props.put(SessionManagerConfig.HOSTNAMES_PROP_KEY, hostnamesPropVal);
        return props;
    }

    /** {@inheritDoc} */
    @Override
    public void processPacket(Packet packet) {
        // XXX
    }



    /** {@inheritDoc} */
    @Override
    public void setProperties(Map<String, Object> props) {
        super.setProperties(props);
        try {
            String cls_name = (String) props.get(MUC_REPO_CLASS_PROP_KEY);
            String res_uri = (String) props.get(MUC_REPO_URL_PROP_KEY);

            log.config("Initialized " + cls_name + " as user repository: " + res_uri);
        } catch (Exception e) {
            log.severe("Can't initialize user repository: " + e);
            e.printStackTrace();
            System.exit(1);
        }
        String[] hostnames = (String[]) props.get(SessionManagerConfig.HOSTNAMES_PROP_KEY);
        clearRoutings();
        for (String host : hostnames) {
            if (defaultServiceHost == null) {
                defaultServiceHost = host;
            }
            addRouting(host);
        }
    }
}
