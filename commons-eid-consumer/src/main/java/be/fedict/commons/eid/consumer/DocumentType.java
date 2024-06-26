/*
 * Commons eID Project.
 * Copyright (C) 2008-2013 FedICT.
 * Copyright (C) 2018-2024 e-Contract.be BV.
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Enumeration for eID Document Type.
 * 
 * @author Frank Cornelis
 * @see Identity
 */
public enum DocumentType implements Serializable {

	BELGIAN_CITIZEN("1"),

	KIDS_CARD("6"),

	BOOTSTRAP_CARD("7"),

	HABILITATION_CARD("8"),

	/**
	 * Bewijs van inschrijving in het vreemdelingenregister ??? Tijdelijk verblijf
	 */
	FOREIGNER_A("11", "33"),

	/**
	 * Bewijs van inschrijving in het vreemdelingenregister
	 */
	FOREIGNER_B("12", "34"),

	/**
	 * Identiteitskaart voor vreemdeling
	 */
	FOREIGNER_C("13"),

	/**
	 * EG-Langdurig ingezetene
	 */
	FOREIGNER_D("14"),

	/**
	 * (Verblijfs)kaart van een onderdaan van een lidstaat der EEG Verklaring van
	 * inschrijving
	 */
	FOREIGNER_E("15"),

	/**
	 * Document ter staving van duurzaam verblijf van een EU onderdaan
	 */
	FOREIGNER_E_PLUS("16"),

	/**
	 * Kaart voor niet-EU familieleden van een EU-onderdaan of van een Belg
	 * Verblijfskaart van een familielid van een burger van de Unie
	 */
	FOREIGNER_F("17", "35"),

	/**
	 * Duurzame verblijfskaart van een familielid van een burger van de Unie
	 */
	FOREIGNER_F_PLUS("18", "36"),

	/**
	 * H. Europese blauwe kaart. Toegang en verblijf voor onderdanen van derde
	 * landen.
	 */
	EUROPEAN_BLUE_CARD_H("19"),

	/**
	 * I. New types of foreigner cards (I and J cards) will be issued for employees
	 * that are transferred within their company (EU directive 2014/66/EU)
	 */
	FOREIGNER_I("20"),

	/**
	 * J. New types of foreigner cards (I and J cards) will be issued for employees
	 * that are transferred within their company (EU directive 2014/66/EU)
	 */
	FOREIGNER_J("21"),

	FOREIGNER_M("22"),

	FOREIGNER_N("23"),

	/**
	 * ETABLISSEMENT
	 */
	FOREIGNER_K("27"),

	/**
	 * RESIDENT DE LONGUE DUREE – UE
	 */
	FOREIGNER_L("28"),

	FOREIGNER_EU("31"),

	FOREIGNER_EU_PLUS("32"),

	FOREIGNER_KIDS_EU("61"),

	FOREIGNER_KIDS_EU_PLUS("62"),

	FOREIGNER_KIDS_A("63"),

	FOREIGNER_KIDS_B("64"),

	FOREIGNER_KIDS_K("65"),

	FOREIGNER_KIDS_L("66"),

	FOREIGNER_KIDS_F("67"),

	FOREIGNER_KIDS_F_PLUS("68"),

	FOREIGNER_KIDS_M("69");

	private final Set<Integer> keys;

	DocumentType(final String... valueList) {
		this.keys = new HashSet<>();
		for (String value : valueList) {
			this.keys.add(toKey(value));
		}
	}

	private int toKey(final String value) {
		final char c1 = value.charAt(0);
		int key = c1 - '0';
		if (2 == value.length()) {
			key *= 10;
			final char c2 = value.charAt(1);
			key += c2 - '0';
		}
		return key;
	}

	private static int toKey(final byte[] value) {
		int key = value[0] - '0';
		if (2 == value.length) {
			key *= 10;
			key += value[1] - '0';
		}
		return key;
	}

	private static Map<Integer, DocumentType> documentTypes;

	static {
		final Map<Integer, DocumentType> documentTypes = new HashMap<>();
		for (DocumentType documentType : DocumentType.values()) {
			for (Integer key : documentType.keys) {
				if (documentTypes.containsKey(key)) {
					throw new RuntimeException("duplicate document type enum: " + key);
				}
				documentTypes.put(key, documentType);
			}
		}
		DocumentType.documentTypes = documentTypes;
	}

	public static DocumentType toDocumentType(final byte[] value) {
		final int key = DocumentType.toKey(value);
		/*
		 * If the key is unknown, we simply return null.
		 */
		return DocumentType.documentTypes.get(key);
	}

	public static String toString(final byte[] documentTypeValue) {
		return Integer.toString(DocumentType.toKey(documentTypeValue));
	}
}
