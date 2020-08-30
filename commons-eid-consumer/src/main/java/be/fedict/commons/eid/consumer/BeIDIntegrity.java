/*
 * Commons eID Project.
 * Copyright (C) 2008-2013 FedICT.
 * Copyright (C) 2009-2020 e-Contract.be BV.
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

package be.fedict.commons.eid.consumer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.fedict.commons.eid.consumer.tlv.TlvParser;

/**
 * Utility class for various eID related integrity checks.
 * 
 * @author Frank Cornelis
 * 
 */
public class BeIDIntegrity {

	private final static Logger LOGGER = LoggerFactory.getLogger(BeIDIntegrity.class);

	private final CertificateFactory certificateFactory;

	/**
	 * Default constructor.
	 */
	public BeIDIntegrity() {
		try {
			this.certificateFactory = CertificateFactory.getInstance("X.509");
		} catch (final CertificateException cex) {
			throw new RuntimeException("X.509 algo", cex);
		}
	}

	/**
	 * Loads a DER-encoded X509 certificate from a byte array.
	 * 
	 * @param encodedCertificate
	 * @return
	 */
	public X509Certificate loadCertificate(final byte[] encodedCertificate) {
		X509Certificate certificate;
		try {
			certificate = (X509Certificate) this.certificateFactory
					.generateCertificate(new ByteArrayInputStream(encodedCertificate));
		} catch (final CertificateException cex) {
			throw new RuntimeException("X509 decoding error: " + cex.getMessage(), cex);
		}
		return certificate;
	}

	/**
	 * Gives back a parsed identity file after integrity verification.
	 * 
	 * @param identityFile
	 * @param identitySignatureFile
	 * @param rrnCertificate
	 * @return
	 */
	public Identity getVerifiedIdentity(final byte[] identityFile, final byte[] identitySignatureFile,
			final X509Certificate rrnCertificate) {
		final Identity identity = this.getVerifiedIdentity(identityFile, identitySignatureFile, null, rrnCertificate);
		return identity;
	}

	/**
	 * Gives back a parsed identity file after integrity verification including the
	 * eID photo.
	 * 
	 * @param identityFile
	 * @param identitySignatureFile
	 * @param photo
	 * @param rrnCertificate
	 * @return
	 */
	public Identity getVerifiedIdentity(final byte[] identityFile, final byte[] identitySignatureFile,
			final byte[] photo, final X509Certificate rrnCertificate) {
		final PublicKey publicKey = rrnCertificate.getPublicKey();
		boolean result;
		try {
			result = verifySignature(rrnCertificate.getSigAlgName(), identitySignatureFile, publicKey, identityFile);
		} catch (final Exception ex) {
			throw new SecurityException("identity signature verification error: " + ex.getMessage(), ex);
		}
		if (false == result) {
			throw new SecurityException("signature integrity error");
		}
		final Identity identity = TlvParser.parse(identityFile, Identity.class);
		if (null != photo) {
			final byte[] expectedPhotoDigest = identity.getPhotoDigest();
			final byte[] actualPhotoDigest = digest(getDigestAlgo(expectedPhotoDigest.length), photo);
			if (false == Arrays.equals(expectedPhotoDigest, actualPhotoDigest)) {
				throw new SecurityException("photo digest mismatch");
			}
		}
		return identity;
	}

	/**
	 * Gives back a parsed address file after integrity verification.
	 * 
	 * @param addressFile
	 * @param identitySignatureFile
	 * @param addressSignatureFile
	 * @param rrnCertificate
	 * @return
	 */
	public Address getVerifiedAddress(final byte[] addressFile, final byte[] identitySignatureFile,
			final byte[] addressSignatureFile, final X509Certificate rrnCertificate) {
		final byte[] trimmedAddressFile = trimRight(addressFile);
		final PublicKey publicKey = rrnCertificate.getPublicKey();
		boolean result;
		try {
			result = verifySignature(rrnCertificate.getSigAlgName(), addressSignatureFile, publicKey,
					trimmedAddressFile, identitySignatureFile);
		} catch (final Exception ex) {
			throw new SecurityException("address signature verification error: " + ex.getMessage(), ex);
		}
		if (false == result) {
			throw new SecurityException("address integrity error");
		}
		final Address address = TlvParser.parse(addressFile, Address.class);
		return address;

	}

	/**
	 * Verifies a SHA1withRSA signature.
	 * 
	 * @param signatureData
	 * @param publicKey
	 * @param data
	 * @return
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 */
	public boolean verifySignature(final byte[] signatureData, final PublicKey publicKey, final byte[]... data)
			throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		LOGGER.debug("public key algorithm: {}", publicKey.getAlgorithm());
		String signatureAlgo;
		if ("EC".equals(publicKey.getAlgorithm())) {
			signatureAlgo = "SHA256withECDSA";
		} else {
			signatureAlgo = "SHA1withRSA";
		}
		return this.verifySignature(signatureAlgo, signatureData, publicKey, data);
	}

	/**
	 * Verifies a signature.
	 * 
	 * @param signatureAlgo
	 * @param signatureData
	 * @param publicKey
	 * @param data
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	public boolean verifySignature(final String signatureAlgo, final byte[] signatureData, final PublicKey publicKey,
			final byte[]... data) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature signature;
		signature = Signature.getInstance(signatureAlgo);
		signature.initVerify(publicKey);
		for (byte[] dataItem : data) {
			signature.update(dataItem);
		}
		if (null == signatureData) {
			throw new SignatureException("missing signature data");
		}
		final boolean result = signature.verify(signatureData);
		return result;
	}

	private byte[] digest(final String algoName, final byte[] data) {
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance(algoName);
		} catch (final NoSuchAlgorithmException nsaex) {
			throw new RuntimeException(algoName);
		}
		final byte[] digestValue = messageDigest.digest(data);
		return digestValue;
	}

	private byte[] trimRight(final byte[] addressFile) {
		int idx;
		for (idx = 0; idx < addressFile.length; idx++) {
			if (0 == addressFile[idx]) {
				break;
			}
		}
		final byte[] result = new byte[idx];
		System.arraycopy(addressFile, 0, result, 0, idx);
		return result;
	}

	/**
	 * Verifies an authentication signature.
	 * 
	 * @param toBeSigned
	 * @param signatureValue
	 * @param authnCertificate
	 * @return
	 */
	public boolean verifyAuthnSignature(final byte[] toBeSigned, byte[] signatureValue,
			final X509Certificate authnCertificate) {
		final PublicKey publicKey = authnCertificate.getPublicKey();
		if ("EC".equals(publicKey.getAlgorithm())) {
			try {
				signatureValue = toDERSignature(signatureValue);
			} catch (IOException e) {
				LOGGER.warn("DER encoding error: " + e.getMessage(), e);
				return false;
			}
		}
		boolean result;
		try {
			result = this.verifySignature(signatureValue, publicKey, toBeSigned);
		} catch (final InvalidKeyException ikex) {
			LOGGER.warn("invalid key: " + ikex.getMessage(), ikex);
			return false;
		} catch (final NoSuchAlgorithmException nsaex) {
			LOGGER.warn("no such algo: " + nsaex.getMessage(), nsaex);
			return false;
		} catch (final SignatureException sigex) {
			LOGGER.warn("signature error: " + sigex.getMessage(), sigex);
			return false;
		}
		return result;
	}

	/**
	 * Converts a RAW EC R||S signature to DER encoded format.
	 * 
	 * @param rawSign
	 * @return
	 * @throws IOException
	 */
	private byte[] toDERSignature(byte[] rawSign) throws IOException {
		int len = rawSign.length / 2;

		byte[] r = new byte[len];
		byte[] s = new byte[len];
		System.arraycopy(rawSign, 0, r, 0, len);
		System.arraycopy(rawSign, len, s, 0, len);

		ASN1EncodableVector v = new ASN1EncodableVector();
		v.add(new ASN1Integer(new BigInteger(1, r)));
		v.add(new ASN1Integer(new BigInteger(1, s)));

		DERSequence seq = new DERSequence(v);
		return seq.getEncoded();
	}

	/**
	 * Verifies a non-repudiation signature.
	 * 
	 * @param expectedDigestValue
	 * @param signatureValue
	 * @param certificate
	 * @return
	 */
	public boolean verifyNonRepSignature(final byte[] expectedDigestValue, final byte[] signatureValue,
			final X509Certificate certificate) {
		try {
			return __verifyNonRepSignature(expectedDigestValue, signatureValue, certificate);
		} catch (final InvalidKeyException ikex) {
			LOGGER.warn("invalid key: " + ikex.getMessage(), ikex);
			return false;
		} catch (final NoSuchAlgorithmException nsaex) {
			LOGGER.warn("no such algo: " + nsaex.getMessage(), nsaex);
			return false;
		} catch (final NoSuchPaddingException nspex) {
			LOGGER.warn("no such padding: " + nspex.getMessage(), nspex);
			return false;
		} catch (final BadPaddingException bpex) {
			LOGGER.warn("bad padding: " + bpex.getMessage(), bpex);
			return false;
		} catch (final IOException ioex) {
			LOGGER.warn("IO error: " + ioex.getMessage(), ioex);
			return false;
		} catch (final IllegalBlockSizeException ibex) {
			LOGGER.warn("illegal block size: " + ibex.getMessage(), ibex);
			return false;
		}
	}

	private boolean __verifyNonRepSignature(final byte[] expectedDigestValue, final byte[] signatureValue,
			final X509Certificate certificate) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
		final PublicKey publicKey = certificate.getPublicKey();

		final Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, publicKey);
		final byte[] actualSignatureDigestInfoValue = cipher.doFinal(signatureValue);

		final ASN1InputStream asnInputStream = new ASN1InputStream(actualSignatureDigestInfoValue);
		final DigestInfo actualSignatureDigestInfo = new DigestInfo((ASN1Sequence) asnInputStream.readObject());
		asnInputStream.close();

		final byte[] actualDigestValue = actualSignatureDigestInfo.getDigest();
		return Arrays.equals(expectedDigestValue, actualDigestValue);
	}

	private String getDigestAlgo(final int hashSize) throws SecurityException {
		switch (hashSize) {
		case 20:
			return "SHA-1";
		case 28:
			return "SHA-224";
		case 32:
			return "SHA-256";
		case 48:
			return "SHA-384";
		case 64:
			return "SHA-512";
		}

		throw new SecurityException("Failed to find guess algorithm for hash size of " + hashSize + " bytes");
	}
}
