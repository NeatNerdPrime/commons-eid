/*
 * Commons eID Project.
 * Copyright (C) 2012-2013 FedICT.
 * Copyright (C) 2017-2020 e-Contract.be BV.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version
 * 3.0 as published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, see 
 * http://www.gnu.org/licenses/.
 */

package be.fedict.commons.eid.jca.ssl;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.fedict.commons.eid.jca.BeIDKeyStoreParameter;

/**
 * eID specific {@link X509ExtendedKeyManager}.
 * 
 * @see BeIDKeyManagerFactory
 * @author Frank Cornelis
 * 
 */
public class BeIDX509KeyManager extends X509ExtendedKeyManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(BeIDX509KeyManager.class);

	private KeyStore keyStore;

	public BeIDX509KeyManager() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		this(null);
	}

	public BeIDX509KeyManager(final BeIDManagerFactoryParameters beIDSpec)
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		LOGGER.debug("constructor");
		this.keyStore = KeyStore.getInstance("BeID");
		BeIDKeyStoreParameter beIDKeyStoreParameter;
		if (null == beIDSpec) {
			beIDKeyStoreParameter = null;
		} else {
			beIDKeyStoreParameter = new BeIDKeyStoreParameter();
			beIDKeyStoreParameter.setLocale(beIDSpec.getLocale());
			beIDKeyStoreParameter.setParentComponent(beIDSpec.getParentComponent());
			beIDKeyStoreParameter.setAutoRecovery(beIDSpec.getAutoRecovery());
			beIDKeyStoreParameter.setCardReaderStickiness(beIDSpec.getCardReaderStickiness());
		}
		this.keyStore.load(beIDKeyStoreParameter);
	}

	@Override
	public String chooseClientAlias(final String[] keyTypes, final Principal[] issuers, final Socket socket) {
		LOGGER.debug("chooseClientAlias");
		for (String keyType : keyTypes) {
			LOGGER.debug("key type: {}", keyType);
			if ("RSA".equals(keyType)) {
				return "beid";
			}
			if ("EC".equals(keyType)) {
				return "beid";
			}
		}
		return null;
	}

	@Override
	public String chooseServerAlias(final String keyType, final Principal[] issuers, final Socket socket) {
		LOGGER.debug("chooseServerAlias");
		return null;
	}

	@Override
	public X509Certificate[] getCertificateChain(final String alias) {
		LOGGER.debug("getCertificateChain: {}", alias);
		if ("beid".equals(alias)) {
			Certificate[] certificateChain;
			try {
				certificateChain = this.keyStore.getCertificateChain("Authentication");
			} catch (final KeyStoreException e) {
				LOGGER.error("BeID keystore error: " + e.getMessage(), e);
				return null;
			}
			final X509Certificate[] x509CertificateChain = new X509Certificate[certificateChain.length];
			for (int idx = 0; idx < certificateChain.length; idx++) {
				x509CertificateChain[idx] = (X509Certificate) certificateChain[idx];
			}
			return x509CertificateChain;
		}
		return null;
	}

	@Override
	public String[] getClientAliases(final String keyType, final Principal[] issuers) {
		LOGGER.debug("getClientAliases");
		return null;
	}

	@Override
	public PrivateKey getPrivateKey(final String alias) {
		LOGGER.debug("getPrivateKey: {}", alias);
		if ("beid".equals(alias)) {
			PrivateKey privateKey;
			try {
				privateKey = (PrivateKey) this.keyStore.getKey("Authentication", null);
			} catch (final Exception e) {
				LOGGER.error("getKey error: " + e.getMessage(), e);
				return null;
			}
			return privateKey;
		}
		return null;
	}

	@Override
	public String[] getServerAliases(final String keyType, final Principal[] issuers) {
		LOGGER.debug("getServerAliases");
		return null;
	}

	@Override
	public String chooseEngineClientAlias(final String[] keyType, final Principal[] issuers, final SSLEngine engine) {
		LOGGER.debug("chooseEngineClientAlias");
		return super.chooseEngineClientAlias(keyType, issuers, engine);
	}

	@Override
	public String chooseEngineServerAlias(final String keyType, final Principal[] issuers, final SSLEngine engine) {
		LOGGER.debug("chooseEngineServerAlias");
		return super.chooseEngineServerAlias(keyType, issuers, engine);
	}
}
